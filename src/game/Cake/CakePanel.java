package game.Cake;

import game.GameFrame;
import game.Music;

import javax.swing.*;
import java.awt.*;

public class CakePanel extends JPanel implements Runnable {

    // ⚠️ 주의: 실제 프로젝트에서는 GameFrame, CakeStage1_2, CakeStage2, CakeStage3_1, CakeStage3_2
    // 클래스들이 프로젝트에 정의되어 있어야 합니다.

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
    private static final int SURPRISE_DURATION_MS = 10000; // 10초 대기
    private long surpriseStartTime = 0;
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
        CakeStage1_1 stage1_1 = new CakeStage1_1(this, stage1_1Data, initialScoreOffset);
        stage1_1.setName(STAGE1_1_NAME);
        add(stage1_1, STAGE1_1_NAME);

        // 💡 [추가] 기습 스테이지 패널 추가 (더미 가정)
//        JPanel surprisePanel = new JPanel();
//        surprisePanel.setBackground(Color.RED);
//        JLabel surpriseLabel = new JLabel("기습 스테이지! 10초 후 Stage 1-2로 전환됩니다.", SwingConstants.CENTER);
//        surpriseLabel.setFont(new Font("Arial", Font.BOLD, 40));
//        surpriseLabel.setForeground(Color.WHITE);
//        surprisePanel.setLayout(new GridBagLayout());
//        surprisePanel.add(surpriseLabel);
//        surprisePanel.setName(SURPRISE_NAME);
//        add(surprisePanel, SURPRISE_NAME);
        // 💡 [수정] 기습 스테이지 패널 추가 (별도의 클래스 파일 사용)
        SurprisePanel surprisePanel = new SurprisePanel(this); // ‼️ 새로 만든 클래스 사용
        surprisePanel.setName(SURPRISE_NAME);
        add(surprisePanel, SURPRISE_NAME);


        // 나머지 스테이지는 필요 시 다음 로직에서 생성됨 (switchToNextStagePanel 참조)

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
                    // 기습 스테이지 (JPanel)가 표시 중일 때 repaint
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

        // ‼️ [수정] 기습 스테이지가 아닐 때만 Stage Logic 업데이트
        if (currentStagePanel != null && !isSurpriseActive) {
            currentStagePanel.updateStageLogic();
        }

        // 1. 🚨 기습 스테이지 대기 시간 체크 및 전환 로직
        if (isSurpriseActive) {
            long currentTime = System.currentTimeMillis();

            if (currentTime >= surpriseStartTime + SURPRISE_DURATION_MS) {
                // 10초 경과: Stage 1-2로 전환
                isSurpriseActive = false;

                int totalScore = CakeStageManager.getCumulativeScore();
                System.out.println("✅ 기습 스테이지 종료. Stage 1-2로 전환 시작.");

                // ‼️ Stage 1-2로 전환 및 음악 로드 로직 실행

                // CakeStageManager에서 다음 스테이지 인덱스(2)로 이동시키는 로직 수행
                if(CakeStageManager.nextStage()) {
                    switchToNextStagePanel(STAGE1_2_NAME, totalScore);
                } else {
                    finishToResult();
                }
            }
            return; // 기습 스테이지가 활성화된 동안은 아래 음악/스테이지 종료 시간 체크를 건너뜀
        }

        // ‼️ 기습 스테이지 활성 상태가 아닐 때만 음악 시간 체크
        if (music == null) return;

        int currentMusicTime = music.getTime();
        long stageEndTime = CakeStageManager.getCurrentStageEndTime();

        // ✅ 2) 정상적으로 endTime 도달한 경우
        if (stageEndTime != -1 && currentMusicTime >= stageEndTime) {

            // 💡 [수정] totalScore를 여기서 선언하여 아래의 모든 블록에서 사용 가능하게 함
            int totalScore = CakeStageManager.getCumulativeScore();

            // 🚨 Stage 1-1 종료 시점 처리
            if (CakeStageManager.getCurrentStage() == 1) {

                CakeStageManager.checkSurpriseStage();

                if (CakeStageManager.isSurpriseStageOccurred()) {
                    // ‼️ [수정] 기습 스테이지 발생 시에만 음악을 중단하고 null로 설정
                    CakeStageManager.stopMusic();
                    backgroundMusic = null;

                    // ‼️ 기습 스테이지 활성화 로직
                    isSurpriseActive = true;
                    surpriseStartTime = System.currentTimeMillis();

                    cardLayout.show(this, SURPRISE_NAME);
                    currentStagePanel = null;

                    System.out.println("🚨 기습 스테이지 활성화. 10초 대기 시작.");
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
                // ‼️ [핵심 복구] 4-2. 중간 스테이지인 경우: 다음 스테이지로 강제 전환 (Stage 1-2 -> Stage 2)
                int totalScore = CakeStageManager.getCumulativeScore();
                if (CakeStageManager.nextStage()) {
                    int nextStageIndex = CakeStageManager.getCurrentStage();
                    String nextStageCardName = "";

                    // 현재 스테이지가 Stage 1-2(2)였고 다음 스테이지(3)로 넘어갈 때
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
                // ‼️ [수정] Stage 1-2 생성 로직 활성화
                nextStage = new CakeStage1_2(this, stageData, totalScore);
            }
            else if (cardName.equals(STAGE2_NAME)) {
                 nextStage = new CakeStage2(this, stageData, totalScore);
            } else if (cardName.equals(STAGE2_OVEN_NAME)) {
                 nextStage = new CakeStage2_oven(this, stageData, totalScore);
            } else if (cardName.equals(STAGE3_1_NAME)) {
                 nextStage = new CakeStage3_1(this, stageData, totalScore);
            } else if (cardName.equals(STAGE3_2_NAME)) {
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
}