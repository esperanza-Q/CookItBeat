package game.Cake;

import java.awt.*;
import java.awt.event.KeyEvent;

public class CakeStage3_2 extends CakeAnimation {

    private CakePanel controller;

    public CakeStage3_2(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;
    }

    @Override
    protected void loadStageSpecificResources() {
        // 가이드 카드병정 이미지 로드
        guideCardImage1 = loadImage("../images/cakeStage_image/stage1/Card01_stage1-1.png");

        // 재료 이미지 로드 (필요없지만 필드가 CakeAnimation에 남아있으므로 로딩만 유지)
        strawberryBodyImage = loadImage("../images/cakeStage_image/stage1/Strawberry_stage1-1.png");
        shadowImage = loadImage("../images/cakeStage_image/stage1/StrawberryShadow_stage1-1.png");
    }

    @Override
    protected void drawStageObjects(Graphics2D g2) {
//

        // 🖼️ 가이드 카드병정 이미지
        if (guideCardImage1 != null) {

            g2.drawImage(guideCardImage1, 0,0, getWidth(), getHeight(), null);
        }

        // --------------------------------------------------------
        // 2. 🍓 재료 이미지 및 ✂️ 플레이어 도구 그리기 로직 제거
        // --------------------------------------------------------
    }

    // 키 입력 시 실행할 스테이지 고유의 추가 로직 제거
    // @Override
    // protected void processKeyInput(int keyCode) { ... }
}