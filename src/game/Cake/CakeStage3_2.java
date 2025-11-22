package game.Cake;

import game.Main;
import game.Music;
import game.rhythm.RhythmJudgementManager;
import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CakeStage3_2 extends CakeAnimation {

    private CakePanel controller;
    protected RhythmJudgementManager judgementManager;
    private static final int JUDGEMENT_OFFSET_MS = -180;
    private Image currentJudgementImage = null;
    protected String currentJudgementText = null;
    private Timer judgementTimer;
    private final int JUDGEMENT_DISPLAY_TIME_MS = 1000; // 판정 결과 표시 시간 (1초)

    private Image clickImage;

    // 성공적으로 클릭된 모든 좌표를 저장할 리스트
    private List<Point> successfulClicks = new ArrayList<>();

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

    private static List<Long> convertToLongArray(int[] array) {
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

    private Ellipse cakeBound = new Ellipse(635, 455, 420, 345);

    public CakeStage3_2(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        judgementManager = new RhythmJudgementManager(convertToLongArray(USER_PRESS_TIMES_INT), initialScoreOffset);

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

            @Override
            public void mouseClicked(MouseEvent e) {
                int clickX = e.getX();
                int clickY = e.getY();


                // 충돌 판정 루프
                    if (cakeBound.contains(clickX, clickY)) {
                        //Music.playEffect("laser02.mp3");
                        processSpaceKeyPressLogic(); // 판정 로직
                        if (!lastJudgementResult.equals("NONE") && !lastJudgementResult.equals("MISS")) {
                            if(currentMusicTimeMs >= CREAM_GUIDE_END && currentMusicTimeMs < STRAWBERRY_GUIDE_START){
                                clickImage = decoCream; // (미리 로드된 이미지 객체)//
                            } else if(currentMusicTimeMs >= STRAWBERRY_GUIDE_END && currentMusicTimeMs <= STRAWBERRY_END_TIME ){
                                clickImage = decoStrawberry;
                            }

                            successfulClicks.add(new Point(clickX, clickY));
                        }

                    }
                repaint();
            }
        });

        // 이 컴포넌트가 마우스 이벤트를 받을 수 있도록 focusable 설정
        this.setFocusable(true);
    }

    protected void processSpaceKeyPressLogic() {
        // 1. 판정 로직 수행
        if (judgementManager != null) {

            // ‼️ 오프셋 적용된 음악 시간 계산: 입력 시간을 47ms 앞으로 당겨서 보정
            int adjustedMusicTime = currentMusicTimeMs + JUDGEMENT_OFFSET_MS;

            // ‼️ [핵심 로그 추가] ‼️ <--- 여기에 추가
            System.out.println("--------------------------------------------------");
            System.out.println("[INPUT] Space Bar Pressed!");
            System.out.println("[MUSIC] Raw Music Time (ms): " + currentMusicTimeMs);
            System.out.println("[JUDGE] Adjusted Time (ms):  " + adjustedMusicTime);
            System.out.println("--------------------------------------------------");

            // ‼️ 조정된 시간을 판정 함수에 전달
            judgementManager.handleInput(adjustedMusicTime);

            // 💡 [핵심 추가] judgementManager의 현재 점수를 StageManager에 저장
            int currentTotalScore = judgementManager.getScore();
            CakeStageManager.setCumulativeScore(currentTotalScore);

            lastJudgementResult = judgementManager.getLastJudgement();
            judgementDisplayStartTime = currentMusicTimeMs;
        }

    }


    @Override
    protected void loadStageSpecificResources() {
        // 가이드 카드병정 이미지 로드
        guideCardImage1 = loadImage("../images/cakeStage_image/stage1/Card01_stage1-1.png");

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
        if (guideCardImage1 != null) {
            g2.drawImage(guideCardImage1, 0,0, getWidth(), getHeight(), null);
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

            /*
            // 1. 색상 설정 (파란색)
            g2.setColor(Color.BLUE);

            // 2. 타원을 그릴 영역(바운딩 박스) 정의
            // drawOval(x, y, width, height)
            // (x, y) = 타원을 감싸는 사각형의 좌측 상단 좌표
            int x = 215;
            int y = 110;
            int width = 840;  // 가로 길이 (장축 또는 단축)
            int height = 800; // 세로 길이 (장축 또는 단축)

            g2.setColor(Color.BLACK);
            g2.drawOval(x, y, width, height);

            g2.setColor(Color.BLUE);

            // 두 좌표 (x1, y1)와 (x2, y2)를 잇는 선을 그립니다.
            g2.drawLine(635, 110, 635, 800);
            g2.drawLine(215, 455, 1055, 455);*/

            if (clickImage != null) {
                for (Point p : successfulClicks) {

                    int x = p.x;
                    int y = p.y;
                    int width, height;
                    if(clickImage == decoCream){
                        width = 300;
                        height = 300;
                    } else {
                        width = 460;
                        height = 410;
                    }
                    // 이미지를 중앙에 정렬하여 그리기 (이미지 크기가 30x30이라고 가정)
                    // 클릭 지점(x, y)을 이미지의 중심에 오도록 조정합니다.
                    g2.drawImage(clickImage, x - width/2, y - height/2, width, height, null);
                }
            }
        }



    }

    public class Ellipse {
        // 타원의 중심 좌표 (Center)
        private double centerX;
        private double centerY;

        // 타원의 반지름 (Radius)
        private double radiusX; // 가로 반지름
        private double radiusY; // 세로 반지름

        public Ellipse(double cx, double cy, double rx, double ry) {
            this.centerX = cx;
            this.centerY = cy;
            this.radiusX = rx;
            this.radiusY = ry;

            // 반지름은 음수가 될 수 없으므로, 필요에 따라 여기서 예외 처리나 절대값 변환을 할 수 있습니다.
        }

        /**
         * 주어진 좌표가 타원의 경계 내부에 있는지 확인합니다.
         * @param clickX 클릭된 X 좌표
         * @param clickY 클릭된 Y 좌표
         * @return 타원 내부에 있으면 true, 아니면 false
         */
        public boolean contains(double clickX, double clickY) {
            // 1. 중심으로부터의 X, Y 거리(차이)를 계산합니다.
            double dx = clickX - centerX;
            double dy = clickY - centerY;

            // 2. 타원 방정식을 사용하여 내부 여부를 확인합니다.
            // (dx^2 / radiusX^2) + (dy^2 / radiusY^2) <= 1

            // 주의: radiusX나 radiusY가 0이면 ZeroDivision 오류가 발생할 수 있으므로
            // 실제 코드에서는 0이 아닌지 확인하는 로직이 필요할 수 있습니다.
            if (radiusX == 0 || radiusY == 0) {
                // 예외 처리 (0인 경우 타원으로 인정하지 않거나, 점으로 처리)
                return false;
            }

            // 3. 타원 방정식 계산
            double result = (dx * dx) / (radiusX * radiusX) +
                    (dy * dy) / (radiusY * radiusY);

            // 4. 결과가 1.0보다 작거나 같으면 타원 내부에 있습니다.
            return result <= 1.0;
        }
    }

    // 키 입력 시 실행할 스테이지 고유의 추가 로직 제거
    // @Override
    // protected void processKeyInput(int keyCode) { ... }
}