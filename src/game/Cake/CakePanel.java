package game.Cake;

import game.GameFrame;
import game.Music;

import javax.swing.*;
import java.awt.*;

public class CakePanel extends JPanel implements Runnable {

    // ⚠️ 주의: 실제 프로젝트에서는 GameFrame, CakeStage1_2, CakeStage2, CakeStage3_1, CakeStage3_2
    // 클래스들이 프로젝트에 정의되어 있어야 합니다.

    private GameFrame gameFrame; // 필요하다면 이 필드를 사용하기 위해 생성자 또는 setter 필요
    private CardLayout cardLayout = new CardLayout();
    private CakeAnimation currentStagePanel;

    private Thread gameThread;

    // ‼️ [필수] Music 객체를 CakePanel의 필드로 선언
    private Music backgroundMusic;

    private static final String STAGE1_1_NAME = "Stage1-1";
    private static final String STAGE1_2_NAME = "Stage1-2";
    private static final String STAGE2_NAME = "Stage2";
    private static final String STAGE3_1_NAME = "Stage3-1";
    private static final String STAGE3_2_NAME = "Stage3-2";


    public CakePanel(/* GameFrame frame */) {
        setLayout(cardLayout);
        setFocusable(true);
        // this.gameFrame = frame; // GameFrame을 사용하는 경우 주석 해제

        // 1. 스테이지 데이터 초기화
        // ⚠️ CakeStageManager 클래스가 정의되어 있고, stageDataList를 가지고 있다고 가정
        CakeStageManager.startFirstStage();
        int initialScoreOffset = 0;

        // 2. 음악 설정 및 시작
        CakeStageData firstStageData = CakeStageManager.stageDataList.get(0);
        String musicFileName = firstStageData.getMusicFileName();

        try {
            backgroundMusic = new Music(musicFileName, true); // isLoop=true
            backgroundMusic.start(); // 새로운 스레드에서 재생 시작
            CakeStageManager.setMusic(backgroundMusic); // StageManager에 Music 객체 등록
        } catch (Exception e) {
            System.err.println("🔴 [CakePanel] 음악 초기화 실패. 경로를 확인하세요.");
            e.printStackTrace();
            backgroundMusic = null;
        }


        // 3. Stage Panel 인스턴스 생성 및 CardLayout에 추가
        // ⚠️ Stage1_2, Stage2, Stage3_1, Stage3_2 클래스가 정의되어 있다고 가정

        // Stage 1-1 (현재 작업 중인 스테이지)
        CakeStageData stage1_1Data = CakeStageManager.stageDataList.get(0);
        CakeStage1_1 stage1_1 = new CakeStage1_1(this, stage1_1Data, initialScoreOffset);
        stage1_1.setName(STAGE1_1_NAME);
        add(stage1_1, STAGE1_1_NAME);

        // 나머지 스테이지 (더미 객체로 가정)
//        if (CakeStageManager.stageDataList.size() > 1) {
//            CakeStageData stage1_2Data = CakeStageManager.stageDataList.get(1);
//            CakeAnimation stage1_2 = new CakeStage1_2(this, stage1_2Data, initialScoreOffset); // ⚠️ CakeStage1_2 필요
//            stage1_2.setName(STAGE1_2_NAME);
//            add(stage1_2, STAGE1_2_NAME);
//        }
//        if (CakeStageManager.stageDataList.size() > 2) {
//            CakeStageData stage2Data = CakeStageManager.stageDataList.get(2);
//            CakeAnimation stage2 = new CakeStage2(this, stage2Data, initialScoreOffset); // ⚠️ CakeStage2 필요
//            stage2.setName(STAGE2_NAME);
//            add(stage2, STAGE2_NAME);
//        }
//        if (CakeStageManager.stageDataList.size() > 3) {
//            CakeStageData stage3_1Data = CakeStageManager.stageDataList.get(3);
//            CakeAnimation stage3_1 = new CakeStage3_1(this, stage3_1Data, initialScoreOffset); // ⚠️ CakeStage3_1 필요
//            stage3_1.setName(STAGE3_1_NAME);
//            add(stage3_1, STAGE3_1_NAME);
//        }
//        if (CakeStageManager.stageDataList.size() > 4) {
//            CakeStageData stage3_2Data = CakeStageManager.stageDataList.get(4);
//            CakeAnimation stage3_2 = new CakeStage3_2(this, stage3_2Data, initialScoreOffset); // ⚠️ CakeStage3_2 필요
//            stage3_2.setName(STAGE3_2_NAME);
//            add(stage3_2, STAGE3_2_NAME);
//        }


        // 4. 현재 스테이지 설정 및 표시
        currentStagePanel = stage1_1;
        cardLayout.show(this, STAGE1_1_NAME);

        // 5. 게임 루프 시작
        gameThread = new Thread(this);
        gameThread.start();

        // 6. 시작 시 포커스 주기 (마우스 리스너 작동을 위함)
        SwingUtilities.invokeLater(() -> currentStagePanel.requestFocusInWindow());
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double FPS = 60.0;
        final double timePerTick = 1000000000 / FPS;
        double delta = 0;

        // 게임 루프 조건
        while (CakeStageManager.getCurrentStage() <= CakeStageManager.stageDataList.size() &&
                backgroundMusic != null && backgroundMusic.isAlive()) {

            long now = System.nanoTime();
            delta += (now - lastTime) / timePerTick;
            lastTime = now;

            if (delta >= 1) {
                updateGameLogic();
                if (currentStagePanel != null) {
                    currentStagePanel.repaint();
                }
                delta--;
            }
            try {
                // 게임 스레드의 CPU 점유율을 낮추기 위한 sleep
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 게임 종료 시
        CakeStageManager.stopMusic();
        System.out.println("게임 종료 또는 음악 중단됨.");
        SwingUtilities.invokeLater(() -> {
            // ⚠️ [GameFrame 필요]
            /*
            if (gameFrame != null) {
                gameFrame.showLobbyScreen(gameFrame.getCurrentUser());
            }
            */
        });
    }

    private void updateGameLogic() {
        Music music = CakeStageManager.getMusic();

        // 1. ‼️ [핵심 로직] 현재 스테이지의 업데이트 로직 호출 (그림자 생성/소멸 등)
        if (currentStagePanel != null) {
            currentStagePanel.updateStageLogic();
        }

        // 2. 스테이지 전환 체크
        if (music != null && music.isAlive()) {
            int currentMusicTime = music.getTime();
            long stageEndTime = CakeStageManager.getCurrentStageEndTime();

            if (stageEndTime != -1 && currentMusicTime >= stageEndTime) {

                if (CakeStageManager.nextStage()) {
                    int nextStageIndex = CakeStageManager.getCurrentStage();
                    String nextStageCardName = "";

                    // ‼️ [핵심 수정]: 누적 점수를 가져옵니다.
                    int totalScore = CakeStageManager.getCumulativeScore();

                    // StageManager에 따라 카드 이름 지정
                    if (nextStageIndex == 2) {
                        nextStageCardName = STAGE1_2_NAME;
                    } else if (nextStageIndex == 3) {
                        nextStageCardName = STAGE2_NAME;
                    } else if (nextStageIndex == 4) {
                        nextStageCardName = STAGE3_1_NAME;
                    } else if (nextStageIndex == 5) {
                        nextStageCardName = STAGE3_2_NAME;
                    }

                    if (!nextStageCardName.isEmpty()) {
                        // ‼️ [핵심 수정]: totalScore를 전달합니다.
                        switchToNextStagePanel(nextStageCardName, totalScore);
                    }
                } else {
                    System.out.println("게임 완료! (음악 종료)");
                }
            }
        }
    }

    public void switchToNextStagePanel(String cardName, int totalScore) {
//        cardLayout.show(this, cardName);
//
//        for (Component comp : getComponents()) {
//            if (comp instanceof CakeAnimation && comp.getName() != null && comp.getName().equals(cardName)) {
//                currentStagePanel = (CakeAnimation) comp;
//
//                // ‼️ [필수 수정]: CakeStageManager의 누적 점수를 업데이트합니다.
//                // 이렇게 해야 다음 스테이지 생성자가 totalScore를 받아 초기화할 수 있습니다.
//                CakeStageManager.setCumulativeScore(totalScore);
//
//                // 포커스 이동을 통해 이전 스테이지 리스너 비활성화/새 리스너 활성화
//                SwingUtilities.invokeLater(() -> currentStagePanel.requestFocusInWindow());
//                break;
//            }
//        }
        CakeAnimation nextStage = null;

        // 1. 이미 생성된 패널인지 확인 (Stage 1-1은 이미 있을 수 있음)
        for (Component comp : getComponents()) {
            if (comp.getName() != null && comp.getName().equals(cardName)) {
                nextStage = (CakeAnimation) comp;
                break;
            }
        }

        // 2. 패널이 없으면 (처음 전환하는 경우), 현재 점수를 넣어 새로 생성
        if (nextStage == null) {
            // ‼️ [핵심]: 여기서 totalScore를 initialScoreOffset으로 사용합니다.
            CakeStageData stageData = CakeStageManager.getCurrentStageData();

            if (cardName.equals(STAGE1_2_NAME)) {
                nextStage = new CakeStage1_2(this, stageData, totalScore);
            }
            else if (cardName.equals(STAGE2_NAME)) {
                nextStage = new CakeStage2(this, stageData, totalScore);
            } else if (cardName.equals(STAGE3_1_NAME)) {
                nextStage = new CakeStage3_1(this, stageData, totalScore);
            } else if (cardName.equals(STAGE3_2_NAME)) {
                nextStage = new CakeStage3_2(this, stageData, totalScore);
            }

            if (nextStage != null) {
                nextStage.setName(cardName);
                add(nextStage, cardName);
            } else {
                System.err.println("🔴 다음 스테이지(" + cardName + ") 생성 실패.");
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

//    public void setInitialScoreOffset(int initialScoreOffset) {
//        initialScoreOffset = initialScoreOffset;
//    }
}