package game.Cake;

import game.Main;
import game.rhythm.RhythmJudgementManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class CakeStage3_1 extends CakeAnimation {

    private CakePanel controller;
    protected RhythmJudgementManager judgementManager;
    private static final int JUDGEMENT_OFFSET_MS = -180;

    private Image cardImage = guideCardImage1;

    private static final int[] GUIDE_TIMES_INT = {
            89290, 89495, 89700, 89910,
            90142, 90350, 90557, 90767, // 짜기
            90993, 91430, 91845 // 바르기
    };

    // ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경 (판정 정답 타이밍)
    private static final int[] USER_PRESS_TIMES_INT = {
            92730, 92935, 93140, 93350,
            93582, 93790, 93997, 94207,
            94433, 94870, 95285
    };

    // 판정 타이밍 가이드 이미지 크기 정의
    private static final int GUIDE_LIGHT_WIDTH = 200;
    private static final int GUIDE_LIGHT_HEIGHT = 200;

    private static final int[][] GUIDE_FIXED_POSITIONS = {
            // 크림 가이드의 고유 위치 (예시 좌표, 실제 레이아웃에 맞게 수정 필요)
            {250, 280}, {443, 280}, {636, 280}, {830, 280},
            {250, 380}, {443, 380}, {636, 380}, {830, 380},
            {346, 480}, {539, 480}, {732, 480}
    };

    private static java.util.List<Long> convertToLongArray(int[] array) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }

        List<Long> timingsList = new ArrayList<>();
        for (long time : result) {
            timingsList.add(time);
        }

        return timingsList;
    }

    private static final long GUIDE_START = 89290; // 크림 가이드 시작 (첫 번째 타이밍)
    private static final long GUIDE_ANIMATION_START = 90993; // 반복 애니메이션 시작
    private static final long GUIDE_END = 92500;   // ‼️ 가이드 이미지가 최종 사라지는 시점

    private static final long END_TIME = 95999; // 스테이지1 끝나는 시점

    private static final int ANIMATION_FRAME_RATE = 150; // 애니메이션 프레임 전환 속도 (ms)


    public CakeStage3_1(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        judgementManager = new RhythmJudgementManager(convertToLongArray(USER_PRESS_TIMES_INT), initialScoreOffset);
    }

    @Override
    protected void loadStageSpecificResources() {
        guideCardImage1 = loadImage("../images/cakeStage_image/stage1/Card01_stage1-2.png");
        guideCardImage2 = loadImage("../images/cakeStage_image/stage1/Card02_stage1-2.png");

        guideStick = loadImage("../images/cakeStage_image/stage3/Guide_stage3.png");
        guideLights = new Image[3];
        for (int i = 0; i < 3; i++) {
            guideLights[i] = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage3/GuideLight0" + (i + 1) + "_stage3.png")).getImage();
        }
        guideA = loadImage("../images/cakeStage_image/stage3/guideA.png");
        guideS = loadImage("../images/cakeStage_image/stage3/guideS.png");
        guideD = loadImage("../images/cakeStage_image/stage3/guideD.png");
        guideF = loadImage("../images/cakeStage_image/stage3/guideF.png");
        guideKeyImage = new Image[11];
        guideKeyImage[0] = guideA;
        guideKeyImage[1] = guideS;
        guideKeyImage[2] = guideD;
        guideKeyImage[3] = guideF;
        guideKeyImage[4] = guideA;
        guideKeyImage[5] = guideS;
        guideKeyImage[6] = guideD;
        guideKeyImage[7] = guideF;
        guideKeyImage[8] = guideS;
        guideKeyImage[9] = guideF;
        guideKeyImage[10] = guideF;

        creamPiping1 = loadImage("../images/cakeStage_image/stage3/Cat01_stage3-1.png");
        creamPiping2 = loadImage("../images/cakeStage_image/stage3/Cat02_stage3-1.png");

        decoCream = loadImage("../images/cakeStage_image/stage3/Cream_stage3-1.png");

        creamCat = loadImage("../images/cakeStage_image/stage3/creamCat.png");
        cakeCream = new Image[3];
        for (int i = 0; i < 3; i++) {
            cakeCream[i] = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage3/cakeCream0" + (i + 1) + ".png")).getImage();
        }
    }

    @Override
    protected void drawStageObjects(Graphics2D g2) {

        // 🖼️ 가이드 카드병정 이미지
        if (guideCardImage1 != null && cardImage != null) {
            for (int i = 0; i < GUIDE_TIMES_INT.length - 1; i++) {
                if (i % 2 == 0 && currentMusicTimeMs >= GUIDE_TIMES_INT[i] && currentMusicTimeMs <= GUIDE_TIMES_INT[i + 1])
                    cardImage = guideCardImage2;
                if (i % 2 == 1 && currentMusicTimeMs >= GUIDE_TIMES_INT[i] && currentMusicTimeMs <= GUIDE_TIMES_INT[i + 1])
                    cardImage = guideCardImage1;
            }
            for (int i = 0; i < USER_PRESS_TIMES_INT.length - 1; i++) {
                if (i % 2 == 0 && currentMusicTimeMs >= USER_PRESS_TIMES_INT[i] && currentMusicTimeMs <= USER_PRESS_TIMES_INT[i + 1])
                    cardImage = guideCardImage2;
                if (i % 2 == 1 && currentMusicTimeMs >= USER_PRESS_TIMES_INT[i] && currentMusicTimeMs <= USER_PRESS_TIMES_INT[i + 1])
                    cardImage = guideCardImage1;
            }

            if (currentMusicTimeMs >= END_TIME) cardImage = guideCardImage1;

            g2.drawImage(cardImage, 20, -30, getWidth(), getHeight(), null);
            AffineTransform originalTransform = g2.getTransform();
            g2.translate(getWidth(), 0);
            g2.scale(-1.0, 1.0);
            g2.drawImage(cardImage, 20, -30, getWidth(), getHeight(), null);
            g2.setTransform(originalTransform);
        }

        long currentTime = currentMusicTimeMs;

        //Image currentPipingImage = isPipingActive ? creamPiping2 : creamPiping1;

        // --- A. 크림 가이드 및 애니메이션 구간 (96140ms ~ 99000ms) ---
        if (currentTime >= GUIDE_START && currentTime < GUIDE_END) {

            // 2-1. 고정 표시 구간 (96140ms ~ 97837ms 미만)
            if (currentTime < GUIDE_ANIMATION_START) {
                // ‼️ 6개의 판정 가이드 이미지를 각각의 위치에 guideLights[0]으로 계속 표시
                for (int i = 0; i < 8; i++) {
                    long flashTime = GUIDE_TIMES_INT[i];

                    if (currentTime >= flashTime) {
                        int x = GUIDE_FIXED_POSITIONS[i][0];
                        int y = GUIDE_FIXED_POSITIONS[i][1];

                        // guideLights[0]을 크림 가이드 6개 위치에 그립니다.
                        g2.drawImage(guideKeyImage[i], x + GUIDE_LIGHT_WIDTH / 4, y + GUIDE_LIGHT_HEIGHT / 4, GUIDE_LIGHT_WIDTH - GUIDE_LIGHT_WIDTH / 2, GUIDE_LIGHT_HEIGHT - GUIDE_LIGHT_HEIGHT / 2, null);
                        g2.drawImage(guideLights[0], x, y, GUIDE_LIGHT_WIDTH, GUIDE_LIGHT_HEIGHT, null);
                        if (currentTime <= flashTime + 200)
                            g2.drawImage(guideStick, x + GUIDE_LIGHT_WIDTH, y - GUIDE_LIGHT_HEIGHT, 500, 400, null);
                    }

                }
            }

            // 2-2. 반복 애니메이션 구간 (97837ms ~ 99000ms 미만)
            else {
                // ‼️ 고정 가이드 이미지들은 모두 사라지고, 애니메이션만 재생됩니다.
                long elapsedSinceAnimStart = currentTime - GUIDE_ANIMATION_START;
                int frameIndex = (int) (elapsedSinceAnimStart / ANIMATION_FRAME_RATE) % 3;

                Image animationImage = guideLights[frameIndex];

                for (int i = 0; i < 11; i++) {
                    if (i >= 8) {
                        long flashTime = GUIDE_TIMES_INT[i];
                        if (currentTime < flashTime) {
                            continue;
                        }
                    }

                    int x = GUIDE_FIXED_POSITIONS[i][0];
                    int y = GUIDE_FIXED_POSITIONS[i][1];

                    // guideLights[0]을 크림 가이드 6개 위치에 그립니다.
                    g2.drawImage(guideKeyImage[i], x + GUIDE_LIGHT_WIDTH / 4, y + GUIDE_LIGHT_HEIGHT / 4, GUIDE_LIGHT_WIDTH - GUIDE_LIGHT_WIDTH / 2, GUIDE_LIGHT_HEIGHT - GUIDE_LIGHT_HEIGHT / 2, null);
                    g2.drawImage(animationImage, x, y, GUIDE_LIGHT_WIDTH, GUIDE_LIGHT_HEIGHT, null);
                    if (i >= 8) {
                        long flashTime = GUIDE_TIMES_INT[i];
                        if (currentTime <= flashTime + 200)
                            g2.drawImage(guideStick, x + GUIDE_LIGHT_WIDTH, y - GUIDE_LIGHT_HEIGHT, 500, 400, null);
                    }
                }
            }
        }


        // 키 입력 시 실행할 스테이지 고유의 추가 로직 제거
        // @Override
        // protected void processKeyInput(int keyCode) { ... }
    }
}