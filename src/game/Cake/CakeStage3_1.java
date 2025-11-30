package game.Cake;

import game.Main;
import game.rhythm.RhythmJudgementManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CakeStage3_1 extends CakeAnimation {

    private CakePanel controller;
    protected RhythmJudgementManager judgementManager;
    private static final int JUDGEMENT_OFFSET_MS = -80;

    private Image cardImage = guideCardImage1;
    private Image currentPipingImage = creamRePiping1;
    private Image currentCatImage = creamCat;
    private boolean keyPressed;

    private boolean aPressed, dPressed, sPressed, fPressed;

    private static final int[] ORIGINAL_GUIDE_TIMES_MS = {
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

    private static long GUIDE_START = 89290; // 크림 가이드 시작 (첫 번째 타이밍)
    private static long GUIDE_ANIMATION_START = 90993; // 반복 애니메이션 시작
    private static long GUIDE_END = 92500;   // ‼️ 가이드 이미지가 최종 사라지는 시점
    private static long bigCream_START = 94400;   // ‼️ 가이드 이미지가 최종 사라지는 시점

    private static long END_TIME = 95999; // 스테이지1 끝나는 시점

    private static final int ANIMATION_FRAME_RATE = 150; // 애니메이션 프레임 전환 속도 (ms)

    public class RhythmNote {
        public long targetTime; // 이 노트를 쳐야 할 시스템 시간 (ms)
        public int requiredKey; // VK_A, VK_S 등
        public int finalDrawX;  // 이 노트를 성공적으로 쳤을 때 그려질 X 위치
        public int finalDrawY;  // 이 노트를 성공적으로 쳤을 때 그려질 Y 위치
        public Image image;     // 성공적으로 쳤을 때 그려질 이미지

        public RhythmNote(long time, int key, int x, int y, Image img) {
            this.targetTime = time;
            this.requiredKey = key;
            this.finalDrawX = x;
            this.finalDrawY = y;
            this.image = img;
        }
    }

    // 쳐야 할 노트 (악보)
    private List<RhythmNote> beatMap = new ArrayList<>();
    // 성공적으로 쳐서 화면에 남아있어야 할 이미지들 (크림 레이어)
    private List<HitResult> drawnCreams = new ArrayList<>();
    private List<HitResult> drawnBigCreams = new ArrayList<>();
    private final int SUCCESS_WINDOW = 150; // 판정 시간 창 (ms)

    private static final long TIME_OFFSET_MS = 41000L;

    private final List<Long> GUIDE_TIMES_INT;
    private final List<Long> CORRECT_TIMES_MS;

    // (예시) beatMap 초기화: 각 시점과 위치를 미리 정의
    public void setupBeatMap() {
        // 1번째 A: 1초 후 (X=100)
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(0), KeyEvent.VK_A, 250, 380, decoCream));
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(1), KeyEvent.VK_S, 443, 430, decoCream));
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(2), KeyEvent.VK_D, 636, 380, decoCream));
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(3), KeyEvent.VK_F, 830, 430, decoCream));

        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(4), KeyEvent.VK_A, 250, 480, decoCream));
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(5), KeyEvent.VK_S, 443, 530, decoCream));
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(6), KeyEvent.VK_D, 636, 480, decoCream));
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(7), KeyEvent.VK_F, 830, 530, decoCream));

        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(8), KeyEvent.VK_S, 0, 0, cakeCream[0]));
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(9), KeyEvent.VK_F, 0, 0, cakeCream[1]));
        beatMap.add(new RhythmNote(CORRECT_TIMES_MS.get(10), KeyEvent.VK_F, 0, 0, cakeCream[2]));
    }

    public class HitResult {
        public Image image;
        public int x;
        public int y;

        public HitResult(Image img, int x, int y) {
            this.image = img;
            this.x = x;
            this.y = y;
        }
    }


    public CakeStage3_1(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);

        final long finalOffset = CakeStageManager.isSurpriseStageOccurred() ? TIME_OFFSET_MS : 0;

        if (CakeStageManager.isSurpriseStageOccurred()) {
            System.out.println("🎵 Stage 1-2: 기습 스테이지 발생으로 타이밍 오프셋 -" + finalOffset + "ms 적용.");
        } else {
            System.out.println("🎵 Stage 1-2: 기습 스테이지 미발생. 타이밍 오프셋 미적용.");
        }

// 1. 가이드 타이밍 리스트 초기화
        GUIDE_TIMES_INT = convertToLongArray(ORIGINAL_GUIDE_TIMES_MS).stream()
                .map(time -> time - finalOffset) // 👈 finalOffset은 이제 final입니다.
                .collect(Collectors.toList());

// 2. 정답 타이밍 리스트 초기화
        CORRECT_TIMES_MS = convertToLongArray(USER_PRESS_TIMES_INT).stream()
                .map(time -> time - finalOffset) // 👈 finalOffset은 이제 final입니다.
                .collect(Collectors.toList());

        GUIDE_START -= finalOffset; // 크림 가이드 시작 (첫 번째 타이밍)
        GUIDE_ANIMATION_START -= finalOffset; // 반복 애니메이션 시작
        GUIDE_END -= finalOffset;   // ‼️ 가이드 이미지가 최종 사라지는 시점
        bigCream_START -= finalOffset; // 큰 크림 타이밍 시작

        END_TIME -= finalOffset; // 스테이지1 끝나는 시점

        this.controller = controller;

        judgementManager = new RhythmJudgementManager(CORRECT_TIMES_MS, initialScoreOffset);
        setupBeatMap();
        initializeKeyTracking();
    }

    private void initializeKeyTracking() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // *** 핵심: 눌린 키 코드를 확인하여 분기 처리합니다. ***
                int keyCode = e.getKeyCode();
                keyPressed = true;
                if (keyCode == KeyEvent.VK_A) {
                    processSpaceKeyPressLogic();
                    if (!lastJudgementResult.equals("NONE") && !lastJudgementResult.equals("MISS")) {
                        // 1. VK_A를 요구하는 가장 가까운 노트 찾기
                        RhythmNote targetNote = findClosestNote(currentMusicTimeMs, KeyEvent.VK_A);
                        HitResult result = new HitResult(
                                targetNote.image,
                                targetNote.finalDrawX,
                                targetNote.finalDrawY
                        );
                        // 4. HitResult 목록에 추가 (이것이 그림을 유지시킵니다)
                        if(currentMusicTimeMs <= bigCream_START) drawnCreams.add(result);
                        else drawnBigCreams.add(result);

                        // 5. 이미 처리된 노트는 악보에서 제거 (중복 처리 방지)
                        beatMap.remove(targetNote);

                        repaint(); // 화면 갱신
                    }

                } else if (keyCode == KeyEvent.VK_S) {
                    processSpaceKeyPressLogic();
                    if (!lastJudgementResult.equals("NONE") && !lastJudgementResult.equals("MISS")) {
                        // 1. VK_A를 요구하는 가장 가까운 노트 찾기
                        RhythmNote targetNote = findClosestNote(currentMusicTimeMs, KeyEvent.VK_S);
                        HitResult result = new HitResult(
                                targetNote.image,
                                targetNote.finalDrawX,
                                targetNote.finalDrawY
                        );
                        // 4. HitResult 목록에 추가 (이것이 그림을 유지시킵니다)
                        if(currentMusicTimeMs <= bigCream_START) drawnCreams.add(result);
                        else drawnBigCreams.add(result);

                        // 5. 이미 처리된 노트는 악보에서 제거 (중복 처리 방지)
                        beatMap.remove(targetNote);

                        repaint(); // 화면 갱신
                    }

                } else if (keyCode == KeyEvent.VK_D) {
                    processSpaceKeyPressLogic();
                    if (!lastJudgementResult.equals("NONE") && !lastJudgementResult.equals("MISS")) {
                        // 1. VK_A를 요구하는 가장 가까운 노트 찾기
                        RhythmNote targetNote = findClosestNote(currentMusicTimeMs, KeyEvent.VK_D);
                        HitResult result = new HitResult(
                                targetNote.image,
                                targetNote.finalDrawX,
                                targetNote.finalDrawY
                        );
                        // 4. HitResult 목록에 추가 (이것이 그림을 유지시킵니다)
                        if(currentMusicTimeMs <= bigCream_START) drawnCreams.add(result);
                        else drawnBigCreams.add(result);

                        // 5. 이미 처리된 노트는 악보에서 제거 (중복 처리 방지)
                        beatMap.remove(targetNote);

                        repaint(); // 화면 갱신
                    }

                } else if (keyCode == KeyEvent.VK_F) {
                    processSpaceKeyPressLogic();
                    if (!lastJudgementResult.equals("NONE") && !lastJudgementResult.equals("MISS")) {
                        // 1. VK_A를 요구하는 가장 가까운 노트 찾기
                        RhythmNote targetNote = findClosestNote(currentMusicTimeMs, KeyEvent.VK_F);
                        HitResult result = new HitResult(
                                targetNote.image,
                                targetNote.finalDrawX,
                                targetNote.finalDrawY
                        );
                        // 4. HitResult 목록에 추가 (이것이 그림을 유지시킵니다)
                        if(currentMusicTimeMs <= bigCream_START) drawnCreams.add(result);
                        else drawnBigCreams.add(result);

                        // 5. 이미 처리된 노트는 악보에서 제거 (중복 처리 방지)
                        beatMap.remove(targetNote);

                        repaint(); // 화면 갱신
                    }

                }

                // 키 이벤트 처리 후, 화면을 갱신해야 할 경우 호출
                // repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // 키에서 손을 뗄 때 처리 (예: 움직임을 멈추거나 애니메이션 종료)
                keyPressed = false;
            }
        });
    }

    protected void processSpaceKeyPressLogic() {
        // 1. 판정 로직 수행
        if (judgementManager != null) {

            // ‼️ 오프셋 적용된 음악 시간 계산: 입력 시간을 47ms 앞으로 당겨서 보정
            int adjustedMusicTime = currentMusicTimeMs + JUDGEMENT_OFFSET_MS;

            // ‼️ [핵심 로그 추가] ‼️ <--- 여기에 추가
//            System.out.println("--------------------------------------------------");
//            System.out.println("[INPUT] Space Bar Pressed!");
//            System.out.println("[MUSIC] Raw Music Time (ms): " + currentMusicTimeMs);
//            System.out.println("[JUDGE] Adjusted Time (ms):  " + adjustedMusicTime);
//            System.out.println("--------------------------------------------------");

            // ‼️ 조정된 시간을 판정 함수에 전달
            judgementManager.handleInput(adjustedMusicTime);

            // 💡 [핵심 추가] judgementManager의 현재 점수를 StageManager에 저장
            int currentTotalScore = judgementManager.getScore();
            CakeStageManager.setCumulativeScore(currentTotalScore);


            // 판정 문자열 가져오기
            String judgement = judgementManager.getLastJudgement();

            // ✅ 여기서 공통 처리
            //    - perfect/good/miss 카운트 증가
            //    - (구현에 따라) lastJudgementResult, 판정 이펙트 처리
            registerJudgement(judgement);

            lastJudgementResult = judgementManager.getLastJudgement();
            judgementDisplayStartTime = currentMusicTimeMs;
        }

    }

    private RhythmNote findClosestNote(long currentTime, int requiredKey) {
        for (RhythmNote note : beatMap) {
            if (note.requiredKey == requiredKey && (note.targetTime - currentTime) <= SUCCESS_WINDOW) {
                return note;
            }
        }
        return null;
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

        creamRePiping1 = loadImage("../images/cakeStage_image/stage3/reverseCat01_stage3-1.png");
        creamRePiping2 = loadImage("../images/cakeStage_image/stage3/reverseCat02_stage3-1.png");

        decoCream = loadImage("../images/cakeStage_image/stage3/Cream_stage3-1.png");

        creamCat = loadImage("../images/cakeStage_image/stage3/creamCat.png");
        creamCat2 = loadImage("../images/cakeStage_image/stage3/creamCat2.png");
        cakeCream = new Image[3];
        for (int i = 0; i < 3; i++) {
            cakeCream[i] = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage3/cakeCream0" + (i + 1) + ".png")).getImage();
        }
    }

    @Override
    protected void drawStageObjects(Graphics2D g2) {

        long currentTime = currentMusicTimeMs;

        //Image currentPipingImage = isPipingActive ? creamPiping2 : creamPiping1;

        // --- A. 크림 가이드 및 애니메이션 구간 (96140ms ~ 99000ms) ---
        if (currentTime >= GUIDE_START && currentTime < GUIDE_END) {

            // 2-1. 고정 표시 구간 (96140ms ~ 97837ms 미만)
            if (currentTime < GUIDE_ANIMATION_START) {
                // ‼️ 6개의 판정 가이드 이미지를 각각의 위치에 guideLights[0]으로 계속 표시
                for (int i = 0; i < 8; i++) {
                    long flashTime = GUIDE_TIMES_INT.get(i);

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
                        long flashTime = GUIDE_TIMES_INT.get(i);
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
                        long flashTime = GUIDE_TIMES_INT.get(i);
                        if (currentTime <= flashTime + 200)
                            g2.drawImage(guideStick, x + GUIDE_LIGHT_WIDTH, y - GUIDE_LIGHT_HEIGHT, 500, 400, null);
                    }
                }
            }
        }
        if(keyPressed) {
            currentPipingImage = creamRePiping2;
            currentCatImage = creamCat2;
        } else {
            currentPipingImage = creamRePiping1;
            currentCatImage = creamCat;
        }

        if (currentTime > GUIDE_END && currentTime <= END_TIME) {
            for (HitResult result : drawnCreams) {
                if (result.image != null) {
                    // result 객체에 저장된 finalDrawX, finalDrawY 위치에 그립니다.
                    g2.drawImage(result.image, result.x, result.y, 200, 200, this);
                }
            }
            g2.drawImage(currentPipingImage, 600, 270, 495, 405, null);
        }//550 450

        if (currentTime > bigCream_START && currentTime <= END_TIME) {
            for (HitResult result : drawnBigCreams) {
                if (result.image != null) {
                    // result 객체에 저장된 finalDrawX, finalDrawY 위치에 그립니다.
                    g2.drawImage(result.image, result.x, result.y, this);
                }
            }
            g2.drawImage(currentCatImage, 650, 250, 600, 600, null);
        }

        // 키 입력 시 실행할 스테이지 고유의 추가 로직 제거
        // @Override
        // protected void processKeyInput(int keyCode) { ... }

        // 🖼️ 가이드 카드병정 이미지
        if (guideCardImage1 != null && cardImage != null) {
            for (int i = 0; i < GUIDE_TIMES_INT.size() - 1 - 1; i++) {
                if (i % 2 == 0 && currentMusicTimeMs >= GUIDE_TIMES_INT.get(i) && currentMusicTimeMs <= GUIDE_TIMES_INT.get(i+1))
                    cardImage = guideCardImage2;
                if (i % 2 == 1 && currentMusicTimeMs >= GUIDE_TIMES_INT.get(i) && currentMusicTimeMs <= GUIDE_TIMES_INT.get(i+1))
                    cardImage = guideCardImage1;
            }
            for (int i = 0; i < CORRECT_TIMES_MS.size() - 1; i++) {
                if (i % 2 == 0 && currentMusicTimeMs >= CORRECT_TIMES_MS.get(i) && currentMusicTimeMs <= CORRECT_TIMES_MS.get(i+1))
                    cardImage = guideCardImage2;
                if (i % 2 == 1 && currentMusicTimeMs >= CORRECT_TIMES_MS.get(i) && currentMusicTimeMs <= CORRECT_TIMES_MS.get(i+1))
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
    }


}
