package game.Space;

import javax.swing.*;
import java.awt.*;

public class SpacePanel extends JPanel {

    private CardLayout cardLayout = new CardLayout();
    private SpaceAnimation currentStage; // 현재 스테이지 인스턴스 참조용

    public SpacePanel() {
        setLayout(cardLayout);

        // 화면 1 : Stage 1
        SpaceStage1 stage1 = new SpaceStage1();
        currentStage = stage1; // 현재 스테이지 참조

        // ✅ StageManager에 현재 스테이지 등록 및 애니메이션 시작
        StageManager.setCurrentStage(stage1);
        stage1.setLayout(null);

        
        // 화면 2 : Stage 2 (다음 스테이지 객체를 미리 생성)
        SpaceStage2 stage2 = new SpaceStage2();
        stage2.setLayout(null); // Layout Manager 설정 (필요하다면)

        
        // 화면 3 : Stage 3 (다음 스테이지 객체를 미리 생성)
        SpaceStage3 stage3 = new SpaceStage3();
        stage3.setLayout(null); // Layout Manager 설정 (필요하다면)
        
        
        add(stage1, "Stage1"); // 이름 변경
        add(stage2, "Stage2"); // 이름 변경
        add(stage3, "Stage3"); // 이름 변경

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

    // ✅ SpaceAnimation에서 호출할 다음 스테이지 전환 메서드
    public void switchToStage2Panel() {
        // "Stage1"에서 "Stage2"로 전환
        cardLayout.show(this, "Stage2");

        // **매우 중요:** 전환된 새 패널(Stage2)에 포커스를 주고 애니메이션을 시작
        SpaceAnimation nextStage = (SpaceAnimation) getComponent(1); // Stage2를 가져옴
        currentStage = nextStage; // 참조 업데이트

        // ✅ 여기 추가: Stage1에서 쌓인 totalScore를 Stage2로 동기화
        nextStage.syncScoreFromManager();

        StageManager.setCurrentStage(nextStage); // StageManager에 새 스테이지 등록 (음악 시간 동기화 계속)

        SwingUtilities.invokeLater(() -> nextStage.requestFocusInWindow());
    }
    
    // ✅ [추가] SpaceAnimation에서 호출할 다음 스테이지(3) 전환 메서드
    public void switchToStage3Panel() {
        // "Stage2"에서 "Stage3"로 전환
        cardLayout.show(this, "Stage3");

        // **매우 중요:** 전환된 새 패널(Stage2)에 포커스를 주고 애니메이션을 시작
        SpaceAnimation nextStage = (SpaceAnimation) getComponent(2); // Stage2를 가져옴
        currentStage = nextStage; // 참조 업데이트

        // ✅ 여기 추가
        nextStage.syncScoreFromManager();

        StageManager.setCurrentStage(nextStage); // StageManager에 새 스테이지 등록 (음악 시간 동기화 계속)

        SwingUtilities.invokeLater(() -> nextStage.requestFocusInWindow());
    }
}
