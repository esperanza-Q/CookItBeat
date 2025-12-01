package game.Space;

import game.GameFrame;

import javax.swing.*;
import java.awt.*;
import game.Music;

public class SpacePanel extends JPanel {

    private CardLayout cardLayout = new CardLayout();
    private SpaceAnimation currentStage; // 현재 스테이지 인스턴스 참조용

    private final GameFrame gameFrame;

    private SpaceStage1 stage1;   // ← 추가
    private SpaceStage2 stage2;   // ← 추가
    private SpaceStage3 stage3;   // ← 추가
    private ResultPanel resultPanel; // ✅ 추가

    private Music resultMusic;
    private boolean musicPlayed = false;

    public SpacePanel(GameFrame frame) {
        this.gameFrame = frame;
        setLayout(cardLayout);

        // 화면 1 : Stage 1
        stage1 = new SpaceStage1();
        currentStage = stage1; // 현재 스테이지 참조

        // ----------GamFrame에서 SpacePanel로 이동한 코드들
        // ✅ 게임 시작 시 음악 & 기습 여부 설정
        StageManager.initializeStage();

        // ✅ 확인 출력 (개발 중 디버그용)
        System.out.println("[선택된 음악 index] " + StageManager.musicIndex);
        System.out.println("[기습 활성 여부] " + StageManager.ambushEnabled);

        // ✅ StageManager를 통해 음악 스레드를 한 번만 안전하게 시작
        StageManager.startMusicAndTimer();

        // ✅ StageManager에 현재 스테이지 등록 및 애니메이션 시작
        StageManager.setCurrentStage(stage1);
        stage1.setLayout(null);

        
        // 화면 2 : Stage 2 (다음 스테이지 객체를 미리 생성)
        stage2 = new SpaceStage2();
        stage2.setLayout(null); // Layout Manager 설정 (필요하다면)

        
        // 화면 3 : Stage 3 (다음 스테이지 객체를 미리 생성)
        stage3 = new SpaceStage3();
        stage3.setLayout(null); // Layout Manager 설정 (필요하다면)

        resultPanel = new ResultPanel(this); // 결과 추가

        add(stage1, "Stage1"); // 이름 변경
        add(stage2, "Stage2"); // 이름 변경
        add(stage3, "Stage3"); // 이름 변경
        add(resultPanel, "result"); // ✅ 결과 화면 등록

        // 화면 전환 버튼 (Stage2에서 Stage1로 돌아오는 Back 버튼)
        // Stage2가 SpaceAnimation을 상속받았다면 KeyListener를 다시 설정해야 합니다.
        JButton backButton = new JButton("Back");
        stage2.add(backButton);

        backButton.addActionListener(e -> {
            cardLayout.show(this, "Stage1");
            currentStage = stage1; // 참조 업데이트
            SwingUtilities.invokeLater(() -> stage1.requestFocusInWindow());
        });

        // 🔥 처음 실행될 때 포커스 주기
        SwingUtilities.invokeLater(() -> stage1.requestFocusInWindow());
    }


    public void goToLobby() {
        // ✅ StageManager.stopMusic() 대신 직접 끄기
        if (StageManager.spaceBackgroundMusic != null) {
            StageManager.spaceBackgroundMusic.close();
            StageManager.spaceBackgroundMusic = null;
        }

        if (gameFrame != null) {
            gameFrame.showLobbyScreen(gameFrame.getCurrentUser());
        }
    }

    // ✅ SpaceAnimation에서 호출할 다음 스테이지 전환 메서드
    public void switchToStage2Panel() {

        stage2.reloadUfoFromManager();   // ← 오류 사라짐
        cardLayout.show(this, "Stage2");

        currentStage = stage2;

        stage2.syncScoreFromManager();
        StageManager.setCurrentStage(stage2);

        SwingUtilities.invokeLater(() -> stage2.requestFocusInWindow());
    }


    // ✅ [추가] SpaceAnimation에서 호출할 다음 스테이지(3) 전환 메서드
    public void switchToStage3Panel() {

        stage3.reloadUfoFromManager();
        cardLayout.show(this, "Stage3");

        currentStage = stage3;

        stage3.syncScoreFromManager();
        StageManager.setCurrentStage(stage3);

        SwingUtilities.invokeLater(() -> stage3.requestFocusInWindow());
    }


    // ✅ [추가] 결과 화면 전환
    public void switchToResultPanel() {
        int finalScore = StageManager.getTotalScore();
        resultPanel.setResult(finalScore); // 점수에 따라 결과 세팅
        cardLayout.show(this, "result");
        resultPanel.requestFocusInWindow();
    }
}
