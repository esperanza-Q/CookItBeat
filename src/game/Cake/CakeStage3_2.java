package game.Cake;

import game.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CakeStage3_2 extends CakeAnimation {

    private CakePanel controller;

    private static final int[] GUIDE_TIMES_INT = {
            96140, 96340, 96575, 96997, 97201, 97428, // 크림 가이드
            102988, 103204, 103430, 103861, 104076, 104281 // 딸기 가이드
    };

    // ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경 (판정 정답 타이밍)
    private static final int[] USER_PRESS_TIMES_INT = {
            99552, 99778, 99994, 100425, 100618, 100845, // 크림
            106414, 106618, 106844, 107275, 107502, 107695 // 딸기
    };

    // ‼️ 6개의 판정 타이밍 가이드 이미지 각각의 고유 위치 정의
    private static final int GUIDE_LIGHT_WIDTH = 200;
    private static final int GUIDE_LIGHT_HEIGHT = 200;

    private static final int[][] GUIDE_FIXED_POSITIONS = {
            // 크림 가이드 6개의 고유 위치 (예시 좌표, 실제 레이아웃에 맞게 수정 필요)
            {400, 300}, {550, 300}, {700, 300}, {400, 400}, {550, 400}, {700, 400},
            // 딸기 가이드 6개의 고유 위치 (예시 좌표)
            {400, 300}, {550, 300}, {700, 300}, {400, 400}, {550, 400}, {700, 400}
    };

    private static final long CREAM_GUIDE_START = 96140; // 크림 가이드 시작 (첫 번째 타이밍)
    private static final long CREAM_ANIMATION_START = 97837; // 반복 애니메이션 시작
    private static final long CREAM_GUIDE_END = 99000;   // ‼️ 가이드 이미지가 최종 사라지는 시점

    private static final long CREAM_END_TIME = 102988;

    private static final long STRAWBERRY_GUIDE_START = 102988; // 딸기 가이드 시작
    private static final long STRAWBERRY_ANIMATION_START = 104700; // 반복 애니메이션 시작
    private static final long STRAWBERRY_GUIDE_END = 106000; // 딸기 데코 시작 (가이드 숨김 시점)

    private static final long STRAWBERRY_END_TIME = 108000;  // 딸기 데코 구간 끝

    private static final int ANIMATION_FRAME_RATE = 150; // 애니메이션 프레임 전환 속도 (ms)

    private int mouseX = 0;
    private int mouseY = 0;

    private boolean isPipingActive = false;

    public CakeStage3_2(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        // ‼️ 마우스 리스너 초기화 호출
        initializeMouseTracking();
    }

    // ‼️ 리스너를 위한 초기화 메소드 (생성자에서 호출)
    private void initializeMouseTracking() {
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // 마우스가 움직일 때마다 좌표를 업데이트합니다.
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // 마우스를 드래그할 때도 좌표를 업데이트합니다.
                mouseX = e.getX();
                mouseY = e.getY();
            }

        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Mouse Pressed! isPipingActive is now true");
                isPipingActive = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPipingActive = false;
                repaint();
            }
        });

        // 이 컴포넌트가 마우스 이벤트를 받을 수 있도록 focusable 설정
        this.setFocusable(true);
    }


    @Override
    protected void loadStageSpecificResources() {
        // 가이드 카드병정 이미지 로드
        guideCardImage = loadImage("../images/cakeStage_image/stage1/Card01_stage1-1.png");

        // 재료 이미지 로드 (필요없지만 필드가 CakeAnimation에 남아있으므로 로딩만 유지)
        strawberryBodyImage = loadImage("../images/cakeStage_image/stage1/Strawberry_stage1-1.png");
        shadowImage = loadImage("../images/cakeStage_image/stage1/StrawberryShadow_stage1-1.png");

        guideLights = new Image[3];
        for (int i = 0; i < 3; i++) {
            guideLights[i] = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage3/GuideLight0" + (i + 1) + "_stage3.png")).getImage();
        }
        decoStrawberry = loadImage("../images/cakeStage_image/stage3/Strawberry_stage3-2.png");
        decoCream = loadImage("../images/cakeStage_image/stage3/Cream_stage3-1.png");
        guideStick = loadImage("../images/cakeStage_image/stage3/Guide_stage3.png");
        creamPiping1 = loadImage("../images/cakeStage_image/stage3/Cat01_stage3-1.png");
        creamPiping2 = loadImage("../images/cakeStage_image/stage3/Cat02_stage3-1.png");
    }


    @Override
    protected void drawStageObjects(Graphics2D g2) {
        // 🖼️ 가이드 카드병정 이미지
        if (guideCardImage != null) {

            g2.drawImage(guideCardImage, 0,0, getWidth(), getHeight(), null);
        }

        long currentTime = currentMusicTimeMs;

        Image currentPipingImage = isPipingActive ? creamPiping2 : creamPiping1;

        // --- A. 크림 가이드 및 애니메이션 구간 (96140ms ~ 99000ms) ---
        if (currentTime >= CREAM_GUIDE_START && currentTime < CREAM_GUIDE_END) {

            // 2-1. 고정 표시 구간 (96140ms ~ 97837ms 미만)
            if (currentTime < CREAM_ANIMATION_START) {
                // ‼️ 6개의 판정 가이드 이미지를 각각의 위치에 guideLights[0]으로 계속 표시
                for (int i = 0; i < 6; i++) {
                    long flashTime = GUIDE_TIMES_INT[i];

                    if (currentTime >= flashTime) {
                        int x = GUIDE_FIXED_POSITIONS[i][0];
                        int y = GUIDE_FIXED_POSITIONS[i][1];

                        // guideLights[0]을 크림 가이드 6개 위치에 그립니다.
                        g2.drawImage(guideLights[0], x, y, GUIDE_LIGHT_WIDTH, GUIDE_LIGHT_HEIGHT, null);
                        if(currentTime <= flashTime + 200)
                            g2.drawImage(guideStick, x+GUIDE_LIGHT_WIDTH, y-GUIDE_LIGHT_HEIGHT, 500, 400, null);
                    }

                }
            }

            // 2-2. 반복 애니메이션 구간 (97837ms ~ 99000ms 미만)
            else {
                // ‼️ 고정 가이드 이미지들은 모두 사라지고, 애니메이션만 재생됩니다.
                long elapsedSinceAnimStart = currentTime - CREAM_ANIMATION_START;
                int frameIndex = (int) (elapsedSinceAnimStart / ANIMATION_FRAME_RATE) % 3;

                Image animationImage = guideLights[frameIndex];

                for (int i = 0; i < 6; i++) {
                    int x = GUIDE_FIXED_POSITIONS[i][0];
                    int y = GUIDE_FIXED_POSITIONS[i][1];

                    // guideLights[0]을 크림 가이드 6개 위치에 그립니다.
                    g2.drawImage(animationImage, x, y, GUIDE_LIGHT_WIDTH, GUIDE_LIGHT_HEIGHT, null);
                }
            }
        }

        if (currentTime >= STRAWBERRY_GUIDE_START && currentTime < STRAWBERRY_GUIDE_END) {

            // 2-1. 고정 표시 구간 (96140ms ~ 97837ms 미만)
            if (currentTime < STRAWBERRY_ANIMATION_START) {
                // ‼️ 6개의 판정 가이드 이미지를 각각의 위치에 guideLights[0]으로 계속 표시
                for (int i = 6; i < 12; i++) {
                    long flashTime = GUIDE_TIMES_INT[i];

                    if (currentTime >= flashTime) {
                        int x = GUIDE_FIXED_POSITIONS[i][0];
                        int y = GUIDE_FIXED_POSITIONS[i][1];

                        // guideLights[0]을 크림 가이드 6개 위치에 그립니다.
                        g2.drawImage(guideLights[0], x, y, GUIDE_LIGHT_WIDTH, GUIDE_LIGHT_HEIGHT, null);
                        if(currentTime <= flashTime + 200)
                            g2.drawImage(guideStick, x+GUIDE_LIGHT_WIDTH, y-GUIDE_LIGHT_HEIGHT-200, 250, 200, null);
                    }
                }
            }

            // 2-2. 반복 애니메이션 구간 (97837ms ~ 99000ms 미만)
            else {
                // ‼️ 고정 가이드 이미지들은 모두 사라지고, 애니메이션만 재생됩니다.
                long elapsedSinceAnimStart = currentTime - STRAWBERRY_ANIMATION_START;
                int frameIndex = (int) (elapsedSinceAnimStart / ANIMATION_FRAME_RATE) % 3;

                Image animationImage = guideLights[frameIndex];

                for (int i = 6; i < 12; i++) {
                    int x = GUIDE_FIXED_POSITIONS[i][0];
                    int y = GUIDE_FIXED_POSITIONS[i][1];

                    // guideLights[0]을 크림 가이드 6개 위치에 그립니다.
                    g2.drawImage(animationImage, x, y, GUIDE_LIGHT_WIDTH, GUIDE_LIGHT_HEIGHT, null);
                }
            }
        }

        // --- C. 마우스 따라다니기 로직 (수정됨) ---

        Image imageToFollow = null;

        // 1. 🍓 딸기 데코 구간 (102988ms ~ 108000ms 미만)
        // ‼️ 두 구간이 겹치므로, 딸기 이미지를 우선하여 검사합니다.
        if (currentTime >= STRAWBERRY_GUIDE_START && currentTime < STRAWBERRY_END_TIME) {
            imageToFollow = decoStrawberry;
        }

        // 2. 🍦 크림 데코 구간 (96140ms ~ 102988ms 미만)
        // ‼️ 딸기 구간과 겹치는 102988ms에서는 딸기가 선택됩니다.
        else if (currentTime >= CREAM_GUIDE_START && currentTime < CREAM_END_TIME) {
            imageToFollow = currentPipingImage;
        }

        // 3. 이미지 그리기
        if (imageToFollow != null) {
            // 마우스 커서 중앙에 이미지가 오도록 좌표를 조정합니다.
            int TOOL_SIZE_x = 0;
            int TOOL_SIZE_y = 0;
            int drawX = 0;
            int drawY = 0;
            if(imageToFollow == currentPipingImage){
                TOOL_SIZE_x = 225;
                TOOL_SIZE_y = 275;
                drawX = mouseX - TOOL_SIZE_x + 30;
                drawY = mouseY - TOOL_SIZE_y + 30 ;
            } else {
                TOOL_SIZE_x = 230;
                TOOL_SIZE_y = 210;
                drawX = mouseX - TOOL_SIZE_x / 2;
                drawY = mouseY - TOOL_SIZE_y / 2;
            }

            g2.drawImage(imageToFollow, drawX, drawY, TOOL_SIZE_x, TOOL_SIZE_y, null);
        }

    }



    // 키 입력 시 실행할 스테이지 고유의 추가 로직 제거
    // @Override
    // protected void processKeyInput(int keyCode) { ... }
}