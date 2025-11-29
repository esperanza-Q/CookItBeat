package game.Cake;

import game.GameFrame;
import game.Music;

import javax.swing.*;
import java.awt.*;

public class CakePanel extends JPanel implements Runnable {

    // ⚠️ 주의: CakeStage1_2, CakeStage2, CakeStage3_1, CakeStage3_2, CakeStage2_oven, CakeResultPanel, CakeStage1_1
    // 클래스들을 모두 프로젝트에 정의해야 합니다.

    private GameOverPanel gameOverPanel;

    private GameFrame gameFrame;
    private CardLayout cardLayout = new CardLayout();
    private CakeAnimation currentStagePanel; // ‼️ Stage Animation 타입만 저장

    private Thread gameThread;

    private Music backgroundMusic;
    private boolean resultShown = false;

    private static final String STAGE1_1_NAME = "Stage1-1";
    private static final String STAGE1_2_NAME = "Stage1-2";
    private static final String STAGE2_NAME = "Stage2";
    private static final String STAGE2_OVEN_NAME = "Stage2_oven";
    private static final String STAGE3_1_NAME = "Stage3-1";
    private static final String STAGE3_2_NAME = "Stage3-2";

    private static final String RESULT_NAME = "CakeResult";

    // 💡 [추가] 기습 스테이지 관련 필드
    private static final String SURPRISE_NAME = "SurpriseStage";
    private boolean isSurpriseActive = false;


    public CakePanel( GameFrame frame) {

        this.gameFrame = frame;
        setLayout(cardLayout);
        setFocusable(true);

        CakeStageManager.resetGame();
        CakeStageManager.startFirstStage();

        // 1. 스테이지 데이터 초기화
        CakeStageManager.startFirstStage();
        int initialScoreOffset = 0;

        // 2. 음악 설정 및 시작 (Stage 1-1 음악 시작)
        CakeStageData firstStageData = CakeStageManager.stageDataList.get(0);
        String musicFileName = firstStageData.getMusicFileName();

        try {
            backgroundMusic = new Music(musicFileName, true);
            backgroundMusic.start();
            CakeStageManager.setMusic(backgroundMusic);
            System.out.println("🎵 Stage 1-1 음악 시작: " + musicFileName);
        } catch (Exception e) {
            System.err.println("🔴 [CakePanel] 음악 초기화 실패. 경로를 확인하세요.");
            e.printStackTrace();
            backgroundMusic = null;
        }


        // 3. Stage Panel 인스턴스 생성 및 CardLayout에 추가
        // Stage 1-1
        CakeStageData stage1_1Data = CakeStageManager.stageDataList.get(0);
        // ⚠️ CakeStage1_1 클래스가 정의되어 있어야 합니다.
        CakeStage1_1 stage1_1 = new CakeStage1_1(this, stage1_1Data, initialScoreOffset);
        stage1_1.setName(STAGE1_1_NAME);
        add(stage1_1, STAGE1_1_NAME);

        // 💡 [수정] 기습 스테이지 패널 추가 (SurprisePanel 클래스 사용)
        SurprisePanel surprisePanel = new SurprisePanel(this);
        surprisePanel.setName(SURPRISE_NAME);
        add(surprisePanel, SURPRISE_NAME);


        // 4. 현재 스테이지 설정 및 표시
        currentStagePanel = stage1_1;
        cardLayout.show(this, STAGE1_1_NAME);

        // 5. 게임 루프 시작
        gameThread = new Thread(this);
        gameThread.start();

        // 6. 시작 시 포커스 주기
        SwingUtilities.invokeLater(() -> currentStagePanel.requestFocusInWindow());
    }


    // ✅ ResultPanel에서 부를 로비 이동 함수
    public void goToLobby() {
        CakeStageManager.stopMusic();
        close();

        if (gameFrame != null) {
            gameFrame.showLobbyScreen(gameFrame.getCurrentUser());
        }
    }


    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double FPS = 60.0;
        final double timePerTick = 1000000000 / FPS;
        double delta = 0;

        // 게임 루프 조건
        while (!resultShown &&
                CakeStageManager.getCurrentStage() <= CakeStageManager.stageDataList.size()) {

            long now = System.nanoTime();
            delta += (now - lastTime) / timePerTick;
            lastTime = now;

            if (delta >= 1) {
                updateGameLogic();

                // ‼️ [수정] currentStagePanel이 null이 아닐 때만 repaint 호출
                if (currentStagePanel != null) {
                    currentStagePanel.repaint();
                } else {
                    // 기습 스테이지 (JPanel 또는 GameOverPanel)가 표시 중일 때 repaint
                    repaint();
                }
                delta--;
            }

            try { Thread.sleep(1); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }


        // 게임 종료 시
        CakeStageManager.stopMusic();
        System.out.println("게임 종료 또는 음악 중단됨.");
        SwingUtilities.invokeLater(() -> {
            // ... (종료 후 로비 이동 로직) ...
        });
    }

    private void showResultPanel(int totalScore) {
        CakeResultPanel resultPanel = null;

        for (Component comp : getComponents()) {
            if (comp instanceof CakeResultPanel) {
                resultPanel = (CakeResultPanel) comp;
                break;
            }
        }

        if (resultPanel == null) {
            // ⚠️ CakeResultPanel 클래스가 정의되어 있어야 합니다.
            resultPanel = new CakeResultPanel(this);
            resultPanel.setName(RESULT_NAME);
            add(resultPanel, RESULT_NAME);
        }

        resultPanel.setResult(totalScore);

        cardLayout.show(this, RESULT_NAME);

        CakeResultPanel finalResultPanel = resultPanel;
        SwingUtilities.invokeLater(finalResultPanel::requestFocusInWindow);
    }


    private void updateGameLogic() {
        Music music = CakeStageManager.getMusic();

        // ‼️ [수정] 기습 스테이지가 활성화된 동안은 Stage Logic 업데이트를 건너뜁니다.
        if (currentStagePanel != null && !isSurpriseActive) {
            currentStagePanel.updateStageLogic();
        }

        // 1. 🚨 기습 스테이지 강제 전환 로직 제거
        if (isSurpriseActive) {
            // ‼️ SurprisePanel에서 미션 성공/실패가 확정될 때까지 대기합니다.
            return;
        }

        // ‼️ 기습 스테이지 활성 상태가 아닐 때만 음악 시간 체크
        if (music == null) return;

        int currentMusicTime = music.getTime();
        long stageEndTime = CakeStageManager.getCurrentStageEndTime();

        // ✅ 2) 정상적으로 endTime 도달한 경우
        if (stageEndTime != -1 && currentMusicTime >= stageEndTime) {

            int totalScore = CakeStageManager.getCumulativeScore();

            // 🚨 Stage 1-1 종료 시점 처리
            if (CakeStageManager.getCurrentStage() == 1) {

                CakeStageManager.checkSurpriseStage();

                if (CakeStageManager.isSurpriseStageOccurred()) {
                    // ‼️ 기습 스테이지 발생 시 Stage 1-1 음악 중단
                    CakeStageManager.stopMusic();
                    backgroundMusic = null;

                    // ‼️ 기습 스테이지 활성화 로직
                    isSurpriseActive = true;

                    cardLayout.show(this, SURPRISE_NAME);
                    currentStagePanel = null;

                    // ‼️ SurprisePanel에 타이머 시작 요청
                    SurprisePanel surprisePanel = (SurprisePanel) getComponentByName(SURPRISE_NAME);
                    if (surprisePanel != null) {
                        surprisePanel.startMissionTimer();
                    }

                    System.out.println("🚨 기습 스테이지 활성화. 미션 타이머 시작 요청.");
                    return;
                }
                // ‼️ [핵심 변경] 기습 미발생 시, 음악 중단 및 null 설정 없이 아래로 진행
                System.out.println("✅ Stage 1-1 종료 (기습 미발생). 음악 유지하며 1-2로 전환.");
            }

            // 3. 다음 스테이지로 전환 (기습 스테이지가 없었을 때)
            if (CakeStageManager.nextStage()) {
                int nextStageIndex = CakeStageManager.getCurrentStage();
                String nextStageCardName = "";

                if (nextStageIndex == 2) nextStageCardName = STAGE1_2_NAME;
                else if (nextStageIndex == 3) nextStageCardName = STAGE2_NAME;
                else if (nextStageIndex == 4) nextStageCardName = STAGE2_OVEN_NAME;
                else if (nextStageIndex == 5) nextStageCardName = STAGE3_1_NAME;
                else if (nextStageIndex == 6) nextStageCardName = STAGE3_2_NAME;

                if (!nextStageCardName.isEmpty()) {
                    switchToNextStagePanel(nextStageCardName, totalScore);
                }

            } else {
                finishToResult();
            }
            return;
        }

        // ✅ 4) 음악이 먼저 끝나버린 경우 (중간 스테이지 강제 전환 로직)
        if (!music.isAlive() && !resultShown) {

            int currentStage = CakeStageManager.getCurrentStage();
            int totalStages = CakeStageManager.stageDataList.size();

            System.out.println("🛑 음악이 예상보다 먼저 종료되었습니다. 다음 스테이지로 강제 전환을 시도합니다.");

            if (currentStage >= totalStages) {
                // 4-1. 마지막 스테이지인 경우: 게임 종료
                finishToResult();
            } else {
                // 4-2. 중간 스테이지인 경우: 다음 스테이지로 강제 전환
                int totalScore = CakeStageManager.getCumulativeScore();
                if (CakeStageManager.nextStage()) {
                    int nextStageIndex = CakeStageManager.getCurrentStage();
                    String nextStageCardName = "";

                    if (nextStageIndex == 3) nextStageCardName = STAGE2_NAME;
                    else if (nextStageIndex == 4) nextStageCardName = STAGE2_OVEN_NAME;
                    else if (nextStageIndex == 5) nextStageCardName = STAGE3_1_NAME;
                    else if (nextStageIndex == 6) nextStageCardName = STAGE3_2_NAME;

                    if (!nextStageCardName.isEmpty()) {
                        switchToNextStagePanel(nextStageCardName, totalScore);
                    }
                }
            }
        }
    }

    private void finishToResult() {
        System.out.println("게임 완료! 결과 패널로 이동");

        int totalScore = CakeStageManager.getCumulativeScore();
        showResultPanel(totalScore);
        resultShown = true;

        if (backgroundMusic != null) {
            backgroundMusic.close();
            backgroundMusic = null;
        }
    }


    public void switchToNextStagePanel(String cardName, int totalScore) {

        // 💡 [핵심] Stage 1-2로 전환할 때 음악 교체 및 재생 시작 (기습 발생 시)
        if (cardName.equals(STAGE1_2_NAME) && CakeStageManager.getMusic() == null) {
            String nextMusicFile = CakeStageManager.getNextMusicFileName();

            try {
                // 새로운 음악 파일을 로드하고 재생 시작
                backgroundMusic = new Music(nextMusicFile, true);
                backgroundMusic.start();
                CakeStageManager.setMusic(backgroundMusic); // StageManager 갱신
                System.out.println("🎵 Stage 1-2부터 새로운 음악 시작: " + nextMusicFile);
            } catch (Exception e) {
                System.err.println("🔴 다음 스테이지 음악 로드 실패: " + nextMusicFile);
                e.printStackTrace();
            }
        }


        CakeAnimation nextStage = null;

        // 1. 이미 생성된 패널인지 확인
        for (Component comp : getComponents()) {
            if (comp.getName() != null && comp.getName().equals(cardName)) {
                if (comp instanceof CakeAnimation) {
                    nextStage = (CakeAnimation) comp;
                    break;
                }
            }
        }

        // 2. 패널이 없으면 (처음 전환하는 경우), 현재 점수를 넣어 새로 생성
        if (nextStage == null) {
            CakeStageData stageData = CakeStageManager.getCurrentStageData();

            if (cardName.equals(STAGE1_2_NAME)) {
                // ⚠️ CakeStage1_2 클래스가 정의되어 있어야 합니다.
                nextStage = new CakeStage1_2(this, stageData, totalScore);
            }
            else if (cardName.equals(STAGE2_NAME)) {
                // ⚠️ CakeStage2 클래스가 정의되어 있어야 합니다.
                nextStage = new CakeStage2(this, stageData, totalScore);
            } else if (cardName.equals(STAGE2_OVEN_NAME)) {
                // ⚠️ CakeStage2_oven 클래스가 정의되어 있어야 합니다.
                nextStage = new CakeStage2_oven(this, stageData, totalScore);
            } else if (cardName.equals(STAGE3_1_NAME)) {
                // ⚠️ CakeStage3_1 클래스가 정의되어 있어야 합니다.
                nextStage = new CakeStage3_1(this, stageData, totalScore);
            } else if (cardName.equals(STAGE3_2_NAME)) {
                // ⚠️ CakeStage3_2 클래스가 정의되어 있어야 합니다.
                nextStage = new CakeStage3_2(this, stageData, totalScore);
            }

            if (nextStage != null) {
                nextStage.setName(cardName);
                add(nextStage, cardName);
            } else {
                System.err.println("🔴 다음 스테이지(" + cardName + ") 생성 실패. (클래스 정의 확인 필요)");
                return;
            }
        }

        // 3. 패널 전환 및 포커스 요청
        cardLayout.show(this, cardName);
        currentStagePanel = nextStage;

        // 4. CakeStageManager의 누적 점수 업데이트 (유지)
        CakeStageManager.setCumulativeScore(totalScore);

        SwingUtilities.invokeLater(() -> currentStagePanel.requestFocusInWindow());
    }

    public void close() {
        CakeStageManager.stopMusic();
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    // ----------------------------------------------------
    // ‼️ [핵심 추가] SurprisePanel에서 미션 실패 시 호출됨 (GAME OVER)
    // ----------------------------------------------------
    public void switchToGameOverScreen() {
        isSurpriseActive = false; // 플래그 즉시 해제

        if (gameOverPanel == null) {
            // ⚠️ GameOverPanel 클래스가 정의되어 있어야 합니다.
            gameOverPanel = new GameOverPanel(this);
            this.add(gameOverPanel, "GameOver"); // "GameOver"는 CardLayout의 이름입니다.
        }

        CardLayout cl = (CardLayout) this.getLayout();
        cl.show(this, "GameOver"); // GameOverPanel로 화면 전환
        System.out.println("🚨 미션 실패! GameOverPanel로 전환되었습니다.");
    }

    // ----------------------------------------------------
    // ‼️ [핵심 추가] SurprisePanel에서 미션 성공 시 호출됨 (다음 스테이지로)
    // ----------------------------------------------------
    public void switchNextStageOnSuccess() {
        isSurpriseActive = false; // 플래그 즉시 해제

        // 음악 정지 및 재시작 로직 (SurprisePanel에서 CakeStageManager.stopMusic()을 호출했지만,
        // CakePanel의 backgroundMusic 필드도 null 처리해야 다음 스테이지 음악 로드 시 중복 방지)
        CakeStageManager.stopMusic();
        backgroundMusic = null;

        int totalScore = CakeStageManager.getCumulativeScore();
        System.out.println("✅ 기습 스테이지 미션 성공. Stage 1-2로 전환 시작.");

        // Stage 1-2로 전환 로직 실행
        if(CakeStageManager.nextStage()) {
            switchToNextStagePanel(STAGE1_2_NAME, totalScore);
        } else {
            finishToResult();
        }
    }

    // ‼️ [추가] getComponentByName 헬퍼 함수
    private Component getComponentByName(String name) {
        for (Component comp : getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
        }
        return null;
    }
}