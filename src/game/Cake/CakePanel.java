package game.Cake;

import game.GameFrame;
import game.Music;

import javax.swing.*;
import java.awt.*;

public class CakePanel extends JPanel implements Runnable {

    private GameFrame gameFrame;
    private CardLayout cardLayout = new CardLayout();
    private CakeAnimation currentStagePanel;

    private Thread gameThread;

    // ‼️ [유지] Music 객체를 CakePanel의 필드로 선언
    private Music backgroundMusic;

    private static final String STAGE1_1_NAME = "Stage1-1";
    private static final String STAGE1_2_NAME = "Stage1-2";
    private static final String STAGE2_NAME = "Stage2";
    private static final String STAGE3_1_NAME = "Stage3-1";
    private static final String STAGE3_2_NAME = "Stage3-2";


    public CakePanel() {
        setLayout(cardLayout);
        setFocusable(true);

        // 1. 스테이지 데이터 초기화
        CakeStageManager.startFirstStage();
        int initialScoreOffset = 0;

        // ‼️ [핵심 수정] 음악을 CakePanel에서 딱 한 번 시작하고, StageManager에 등록
        CakeStageData firstStageData = CakeStageManager.stageDataList.get(0);
        String musicFileName = firstStageData.getMusicFileName();

        try {
            // 1. isLoop=true로 설정 (배경음악은 반복 재생)
            backgroundMusic = new Music(musicFileName, true);

            // 2. run() 대신 start()를 호출하여 새로운 스레드에서 재생
            backgroundMusic.start();

            CakeStageManager.setMusic(backgroundMusic); // ‼️ StageManager에 Music 객체 등록
        } catch (Exception e) {
            // 경로 문제 등 Music 생성 실패 시에도 게임은 계속 진행되도록 처리
            System.err.println("🔴 [CakePanel] 음악 초기화 실패. 경로를 확인하세요.");
            e.printStackTrace();
            backgroundMusic = null; // 실패했으면 null로 설정하여 run() 루프에서 안전하게 건너뛰도록 함.
        }


        // 2. Stage Panel 인스턴스 생성 및 CardLayout에 추가
        CakeStageData stage1_1Data = CakeStageManager.stageDataList.get(0);
        CakeStage1_1 stage1_1 = new CakeStage1_1(this, stage1_1Data, initialScoreOffset);
        stage1_1.setName(STAGE1_1_NAME);
        add(stage1_1, STAGE1_1_NAME);

        CakeStageData stage1_2Data = CakeStageManager.stageDataList.get(1);
        CakeStage1_2 stage1_2 = new CakeStage1_2(this, stage1_2Data, initialScoreOffset);
        stage1_2.setName(STAGE1_2_NAME);
        add(stage1_2, STAGE1_2_NAME);

        CakeStageData stage2Data = CakeStageManager.stageDataList.get(2);
        CakeStage2 stage2 = new CakeStage2(this, stage2Data, initialScoreOffset);
        stage2.setName(STAGE2_NAME);
        add(stage2, STAGE2_NAME);

        CakeStageData stage3_1Data = CakeStageManager.stageDataList.get(3);
        CakeStage3_1 stage3_1 = new CakeStage3_1(this, stage3_1Data, initialScoreOffset);
        stage3_1.setName(STAGE3_1_NAME);
        add(stage3_1, STAGE3_1_NAME);

        CakeStageData stage3_2Data = CakeStageManager.stageDataList.get(4);
        CakeStage3_2 stage3_2 = new CakeStage3_2(this, stage3_2Data, initialScoreOffset);
        stage3_2.setName(STAGE3_2_NAME);
        add(stage3_2, STAGE3_2_NAME);

        // 3. 현재 스테이지 설정 및 표시
        currentStagePanel = stage1_1;
        cardLayout.show(this, STAGE1_1_NAME);

        // 4. 게임 루프 시작
        gameThread = new Thread(this);
        gameThread.start();

        // 5. 시작 시 포커스 주기
        SwingUtilities.invokeLater(() -> currentStagePanel.requestFocusInWindow());
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double FPS = 60.0;
        final double timePerTick = 1000000000 / FPS;
        double delta = 0;

        // ‼️ [수정] 게임 루프 종료 조건: 모든 스테이지를 통과하거나 음악 스레드가 종료될 때
        // backgroundMusic이 null이 아니며, 스레드가 살아있을 때만 루프를 돕니다.
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
        CakeStageManager.stopMusic(); // ‼️ StageManager를 통해 음악 종료
        System.out.println("게임 종료 또는 음악 중단됨.");
        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null) {
                gameFrame.showLobbyScreen(gameFrame.getCurrentUser());
            }
        });
    }

    private void updateGameLogic() {
        Music music = CakeStageManager.getMusic();

        // ‼️ [수정] 음악이 null이 아니고 실행 중일 때만 시간 체크
        if (music != null && music.isAlive()) {
            int currentMusicTime = music.getTime();
            long stageEndTime = CakeStageManager.getCurrentStageEndTime();

            if (stageEndTime != -1 && currentMusicTime >= stageEndTime) {

                if (CakeStageManager.nextStage()) {
                    int nextStageIndex = CakeStageManager.getCurrentStage();
                    String nextStageCardName = "";
                    int dummyScore = 0;

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
                        switchToNextStagePanel(nextStageCardName, dummyScore);
                    }
                } else {
                    System.out.println("게임 완료! (음악 종료)");
                }
            }
        }
    }

    public void switchToNextStagePanel(String cardName, int totalScore) {
        cardLayout.show(this, cardName);

        for (Component comp : getComponents()) {
            if (comp instanceof CakeAnimation && comp.getName() != null && comp.getName().equals(cardName)) {
                currentStagePanel = (CakeAnimation) comp;
                SwingUtilities.invokeLater(() -> currentStagePanel.requestFocusInWindow());
                break;
            }
        }
    }

    public void close() {
        CakeStageManager.stopMusic(); // ‼️ StageManager를 통해 음악 종료
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }
}