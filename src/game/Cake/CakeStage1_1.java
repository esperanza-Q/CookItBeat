package game.Cake;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CakeStage1_1 extends CakeAnimation {

    private CakePanel controller;

    // ✂️ [추가] Stage 1-1 전용 상태 및 위치
    private boolean isScissorsActive = false; // 현재 그려질 가위 이미지 (false: scissorsImage1, true: scissorsImage2)
    private static final int SCISSORS_SIZE = 250;
    protected int scissorsX = 400;
    protected int scissorsY = 400;

    // ✂️ [추가] 마우스 리스너 인스턴스
    private ScissorsMouseListener mouseListener;

    public CakeStage1_1(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        // 마우스 리스너 초기화 및 포커스 리스너 추가
        mouseListener = new ScissorsMouseListener();
        addFocusListener(new StageFocusListener());
    }

    @Override
    protected void loadStageSpecificResources() {
        // ... (로드 로직 유지) ...
        guideCardImage = loadImage("../images/cakeStage_image/stage1/Card01_stage1-1.png");
        scissorsImage1 = loadImage("../images/cakeStage_image/stage1/Scissors01_stage1-1.png");
        scissorsImage2 = loadImage("../images/cakeStage_image/stage1/Scissors02_stage1-1.png");
        strawberryBodyImage = loadImage("../images/cakeStage_image/stage1/Strawberry_stage1-1.png");
        shadowImage = loadImage("../images/cakeStage_image/stage1/StrawberryShadow_stage1-1.png");
    }

    @Override
    protected void drawStageObjects(Graphics2D g2) {

        // 🖼️ 가이드 카드병정 이미지
        if (guideCardImage != null) {
            g2.drawImage(guideCardImage, 0, 0, getWidth(), getHeight(), null);
        }

        // --------------------------------------------------------
        // ✂️ 마우스 상태에 따른 가위 이미지 그리기
        // --------------------------------------------------------
        Image currentScissorsImage = isScissorsActive ? scissorsImage2 : scissorsImage1;

        if (currentScissorsImage != null) {
            // ✂️ CakeAnimation에서 정의된 상수가 아닌, 이 클래스에서 정의된 상수를 사용합니다.
            g2.drawImage(
                    currentScissorsImage,
                    scissorsX,
                    scissorsY,
                    SCISSORS_SIZE,
                    SCISSORS_SIZE,
                    null
            );
        }
    }

    // ✂️ [추가] 마우스 리스너 내부 클래스
    private class ScissorsMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            isScissorsActive = true;
            // 가위 위치를 마우스 위치로 업데이트
            scissorsX = e.getX() - (SCISSORS_SIZE / 2);
            scissorsY = e.getY() - (SCISSORS_SIZE / 2);
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isScissorsActive = false;
            repaint();
        }
    }

    // ✂️ [추가] 포커스 리스너: 스테이지가 활성화될 때만 리스너를 등록합니다.
    private class StageFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            // CakeStage1_1이 화면에 나타나 포커스를 얻을 때 리스너 활성화
            addMouseListener(mouseListener);
            System.out.println("Stage 1-1 활성화: 마우스 리스너 등록됨.");
        }

        @Override
        public void focusLost(FocusEvent e) {
            // CakeStage1_1이 화면에서 사라져 포커스를 잃을 때 리스너 비활성화 (제거)
            removeMouseListener(mouseListener);
            isScissorsActive = false; // 상태 초기화
            System.out.println("Stage 1-1 비활성화: 마우스 리스너 제거됨.");
        }
    }
}