package game.Cake;

import game.rhythm.RhythmJudgementManager;
import game.Music;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CakeStage2 extends CakeAnimation {

    private CakePanel controller;
    private RhythmJudgementManager judgementManager;

    // ====== 이미지 ======
    private Image dough_bowl;
    private Image guide_whipping;
    private Image my_whipping;

    private static final String MIX_SFX = "bowl_mixing2.mp3";

    // ✅ 키 가이드 이미지 (추가)
    private Image keyAImage, keyDImage, keySImage, keyWImage;
    private Image currentKeyGuideImage = null;

    // 🔹 가이드가 화면에 유지될 시간 (ms) (SpaceStage2와 동일)
    private static final int GUIDE_SHOW_DURATION_MS = 2500;

    // ====== Stage2 내부 처리 기록 ======
    private final Set<Integer> processedIndices = new HashSet<>();

    // ====== 회전 애니메이션 설정 ======
    // ====== 회전 애니메이션 설정 ======
    private static final int WHIP_ROT_DURATION_MS = 220;

    // 기존 30도는 가이드용으로 유지
    private static final double GUIDE_WHIP_ROT_ANGLE = Math.toRadians(30);

    // ✅ 내 휘핑은 더 작게 (예: 15~20도 사이 추천)
    private static final double MY_WHIP_ROT_ANGLE = Math.toRadians(18);



    // 가이드/내 휘핑 회전 상태
    private long guideWhipStartMs = -1;
    private long myWhipStartMs = -1;

    // ✅ 내 휘핑 방향 상태 (WASD에 따라 바뀜)
    private double myWhipBaseAngleRad = 0.0; // 시작 각도
    private double myWhipRotSign = 1.0;      // +1 시계 / -1 반시계

    // ✅ 가이드도 WASD처럼 방향/회전 상태를 가짐
    private double guideWhipBaseAngleRad = Math.toRadians(-20); // 기본값
    private double guideWhipRotSign = 1.0;

    // ✅ 가이드 비트가 어떤 키(W/A/S/D)인지 지정
    private static final List<Integer> GUIDE_KEYS = Arrays.asList(
            // ── 구간 1 (7개, W)
            KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,

            // ── 구간 2 (9개, D)
            KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,
            KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,

            // ── 구간 3 (5개, A)
            KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,

            // ── 구간 4 (4개, S)
            KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S
    );


    // ✅ 유저 정답 노트가 어떤 키인지 지정
    private static final List<Integer> CORRECT_KEYS = Arrays.asList(
            // ── 구간 1 (7개, W)
            KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,

            // ── 구간 2 (9개, D)
            KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,
            KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,

            // ── 구간 3 (5개, A)
            KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,

            // ── 구간 4 (4개, S)
            KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S
    );

    private static final int CARD_TRANSITION_DURATION_MS = 80;
    private static final int GOOD_TIMING_MS = 150;
    private static final int SYNC_OFFSET_MS = -50;

    // ====== 화면 배치 ======
    private static final int GUIDE_WHIP_X = 140, GUIDE_WHIP_Y = 100, GUIDE_WHIP_W = 1280, GUIDE_WHIP_H = 720;
    private static final int MY_WHIP_X = 140, MY_WHIP_Y = 100, MY_WHIP_W = 1280, MY_WHIP_H = 720;

    // ⚔️ [타이밍] 가이드
    private static final List<Long> ORIGINAL_GUIDE_TIMES_MS = Arrays.asList(
            55114L, 55519L, 55967L, 56880L, 56967L, 57170L, 57687L,
            61983L, 62377L, 62752L, 63158L, 63703L, 63746L, 64012L, 64109L, 64455L,
            68732L, 69174L, 69604L, 70028L, 70452L,
            75691L, 76037L, 76461L, 76982L
    );

    // ⚔️ [타이밍] 유저 정답
    private static final List<Long> ORIGINAL_CORRECT_TIMES_MS = Arrays.asList(
            58660L, 58895L, 59395L, 60191L, 60380L, 60615L, 61027L,
            65424L, 65830L, 66218L, 66611L, 67017L, 67157L, 67447L, 67538L, 67878L,
            72221L, 72591L, 73015L, 73457L, 73881L,
            77400L, 77734L, 78231L, 78686L
    );

    // 💡 [추가] 오프셋 상수 정의 (Stage 1-1 종료 시간)
    private static final long TIME_OFFSET_MS = 41000L;

    // 💡 [수정] 오프셋이 적용된 최종 타이밍 리스트를 저장할 필드
    private final List<Long> GUIDE_TIMES_MS;
    private final List<Long> CORRECT_TIMES_MS;

    public CakeStage2(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        // ‼️ [핵심 수정] final 키워드를 사용하여 finalOffset을 실질적으로 final로 만듭니다.
// ‼️ 값을 단 한 번만 할당하며, 그 이후에는 변경되지 않습니다.
        final long finalOffset = CakeStageManager.isSurpriseStageOccurred() ? TIME_OFFSET_MS : 0;

        if (CakeStageManager.isSurpriseStageOccurred()) {
            System.out.println("🎵 Stage 2: 기습 스테이지 발생으로 타이밍 오프셋 -" + finalOffset + "ms 적용.");
        } else {
            System.out.println("🎵 Stage 2: 기습 스테이지 미발생. 타이밍 오프셋 미적용.");
        }

// 1. 가이드 타이밍 리스트 초기화
        GUIDE_TIMES_MS = ORIGINAL_GUIDE_TIMES_MS.stream()
                .map(time -> time - finalOffset) // 👈 finalOffset은 이제 final입니다.
                .collect(Collectors.toList());

// 2. 정답 타이밍 리스트 초기화
        CORRECT_TIMES_MS = ORIGINAL_CORRECT_TIMES_MS.stream()
                .map(time -> time - finalOffset) // 👈 finalOffset은 이제 final입니다.
                .collect(Collectors.toList());

        final long OFFSET_MS = 100;

        this.judgementManager = new RhythmJudgementManager(
                CORRECT_TIMES_MS.stream()
                        .map(t -> t + OFFSET_MS)
                        .collect(Collectors.toList()),
                initialScoreOffset
        );

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_W || code == KeyEvent.VK_A
                        || code == KeyEvent.VK_S || code == KeyEvent.VK_D) {

                    long inputTimeMs = currentMusicTimeMs + SYNC_OFFSET_MS;

                    // ✅ 이번 입력이 어떤 노트에 해당하는지 직접 찾기
                    List<Long> timings = judgementManager.getCorrectTimings();

                    int closestIdx = -1;
                    long minDiff = Long.MAX_VALUE;

                    for (int i = 0; i < timings.size(); i++) {
                        if (processedIndices.contains(i)) continue;

                        long diff = Math.abs(inputTimeMs - timings.get(i));
                        if (diff <= GOOD_TIMING_MS && diff < minDiff) {
                            minDiff = diff;
                            closestIdx = i;
                        }
                    }

                    if (closestIdx != -1) {
                        Music.playEffect(MIX_SFX);  // ✅ 내 휘핑 타이밍에서만 재생
                    }

                    if (closestIdx == -1) {
                        judgementManager.forceMiss((int) inputTimeMs);
                        registerJudgement("MISS");

                    } else {
                        int expectedKey = CORRECT_KEYS.get(closestIdx);

                        if (code != expectedKey) {
                            judgementManager.forceMiss((int) inputTimeMs);
                            processedIndices.add(closestIdx);
                            registerJudgement("MISS");

                        } else {
                            int idx = judgementManager.handleInput((int) inputTimeMs);
                            if (idx != -1) processedIndices.add(idx);

                            registerJudgement(judgementManager.getLastJudgement());
                        }
                    }

                    // ✅ 점수 누적 저장
                    int currentTotalScore = judgementManager.getScore();
                    CakeStageManager.setCumulativeScore(currentTotalScore);

                    // ✅ 내 휘핑 방향/애니메이션
                    setMyWhipDirection(code);
                    myWhipStartMs = currentMusicTimeMs;

                    repaint();
                }
            }
        });
    }

    // ✅ 가이드도 WASD → 시작각/회전방향 매핑
    private void setGuideWhipDirection(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W:
                guideWhipBaseAngleRad = Math.toRadians(-90);
                guideWhipRotSign = 1.0;
                break;
            case KeyEvent.VK_A:
                guideWhipBaseAngleRad = Math.toRadians(180);
                guideWhipRotSign = 1.0;
                break;
            case KeyEvent.VK_S:
                guideWhipBaseAngleRad = Math.toRadians(90);
                guideWhipRotSign = 1.0;
                break;
            case KeyEvent.VK_D:
                guideWhipBaseAngleRad = Math.toRadians(0);
                guideWhipRotSign = 1.0;
                break;
        }
    }

    // ✅ WASD → 시작각/회전방향 매핑
    private void setMyWhipDirection(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W:
                myWhipBaseAngleRad = Math.toRadians(-90);
                myWhipRotSign = 1.0;
                break;
            case KeyEvent.VK_A:
                myWhipBaseAngleRad = Math.toRadians(180);
                myWhipRotSign = 1.0;
                break;
            case KeyEvent.VK_S:
                myWhipBaseAngleRad = Math.toRadians(90);
                myWhipRotSign = 1.0;
                break;
            case KeyEvent.VK_D:
                myWhipBaseAngleRad = Math.toRadians(0);
                myWhipRotSign = 1.0;
                break;
        }
    }

    private void triggerGuideWhipIfNeeded(long nowMs) {
        for (int i = 0; i < GUIDE_TIMES_MS.size(); i++) {
            long t = GUIDE_TIMES_MS.get(i);

            if (Math.abs(nowMs - t) <= 30) {
                int expectedGuideKey = GUIDE_KEYS.get(i);
                setGuideWhipDirection(expectedGuideKey);

                if (guideWhipStartMs < 0 || nowMs - guideWhipStartMs > WHIP_ROT_DURATION_MS) {
                    guideWhipStartMs = nowMs;
                }
                break;
            }
        }
    }

    // ✅ [추가] 가이드 타이밍에 맞춰 키 이미지 선택
    private void updateKeyGuideByTime(long t) {
        currentKeyGuideImage = null;

        for (int i = 0; i < GUIDE_TIMES_MS.size(); i++) {
            long start = GUIDE_TIMES_MS.get(i);
            long end = start + GUIDE_SHOW_DURATION_MS;

            if (t >= start && t <= end) {
                int keyCode = GUIDE_KEYS.get(i);
                switch (keyCode) {
                    case KeyEvent.VK_A:
                        currentKeyGuideImage = keyAImage;
                        break;
                    case KeyEvent.VK_D:
                        currentKeyGuideImage = keyDImage;
                        break;
                    case KeyEvent.VK_S:
                        currentKeyGuideImage = keySImage;
                        break;
                    case KeyEvent.VK_W:
                        currentKeyGuideImage = keyWImage;
                        break;
                }
                return;
            }
        }
    }

    @Override
    protected void loadStageSpecificResources() {
        guide_whipping = loadImage("../images/cakeStage_image/stage2/whipping_green_doughO.png");
        my_whipping    = loadImage("../images/cakeStage_image/stage2/whipping_blue_doughO.png");

        // ✅ [추가] 케이크 스테이지용 키 가이드 이미지 로드
        keyAImage = loadImage("../images/cakeStage_image/stage2/cake_keyA.png");
        keyDImage = loadImage("../images/cakeStage_image/stage2/cake_keyD.png");
        keySImage = loadImage("../images/cakeStage_image/stage2/cake_keyS.png");
        keyWImage = loadImage("../images/cakeStage_image/stage2/cake_keyW.png");
    }

    @Override
    protected void drawStageObjects(Graphics2D g2) {
        long adjustedMusicTimeMs = currentMusicTimeMs + SYNC_OFFSET_MS;

        triggerGuideWhipIfNeeded(adjustedMusicTimeMs);

        // ✅ [추가] 키 가이드 갱신
        updateKeyGuideByTime(adjustedMusicTimeMs);

        // 가이드: 중앙 회전 그대로
        drawRotatingWhip(g2, guide_whipping,
                GUIDE_WHIP_X, GUIDE_WHIP_Y, GUIDE_WHIP_W, GUIDE_WHIP_H,
                guideWhipStartMs, adjustedMusicTimeMs,
                guideWhipBaseAngleRad, guideWhipRotSign,
                WHIP_ROT_DURATION_MS,
                GUIDE_WHIP_ROT_ANGLE,
                0.5, 0.5
        );

// ✅ 내 휘핑: 손잡이 쪽(예: 아래쪽)으로 pivot 이동
        drawRotatingWhip(g2, my_whipping,
                MY_WHIP_X, MY_WHIP_Y, MY_WHIP_W, MY_WHIP_H,
                myWhipStartMs, adjustedMusicTimeMs,
                myWhipBaseAngleRad, myWhipRotSign,
                WHIP_ROT_DURATION_MS,
                MY_WHIP_ROT_ANGLE,
                0.50, 0.70   // ← 여기 숫자 바꾸면서 맞추면 됨 (y가 클수록 아래쪽 기준)
        );
        // ✅ [추가] 키 가이드 이미지 그리기 (SpaceStage2 느낌 그대로)
        if (currentKeyGuideImage != null) {
            Graphics2D gGuide = (Graphics2D) g2.create();

            float alpha = 0.9f;
            gGuide.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            float scale = 0.85f; // 크기 필요하면 조절
            int w = (int)(currentKeyGuideImage.getWidth(this) * scale);
            int h = (int)(currentKeyGuideImage.getHeight(this) * scale);

            int padding = 40;
            int x = getWidth() - w - 200;  // 오른쪽에서 200px
            int y = getHeight() - h - 400; // 아래에서 400px

            gGuide.drawImage(currentKeyGuideImage, x, y, w, h, this);
            gGuide.dispose();
        }
    }

    private void drawRotatingWhip(Graphics2D g2, Image img,
                                  int x, int y, int w, int h,
                                  long startMs, long nowMs,
                                  double baseAngleRad,
                                  double rotSign,
                                  long visibleMs,
                                  double rotAngleRad,
                                  double pivotRelX, double pivotRelY) {  // ✅ 추가

        if (img == null || startMs < 0) return;

        long dt = nowMs - startMs;
        if (dt < 0 || dt > visibleMs) return;

        double t = Math.min(1.0, dt / (double) WHIP_ROT_DURATION_MS);
        double angle = baseAngleRad + rotSign * rotAngleRad * t;

        AffineTransform oldTx = g2.getTransform();
        g2.setTransform(new AffineTransform());

        // ✅ pivot을 중앙이 아니라 상대 위치로
        double pivotX = x + w * pivotRelX;
        double pivotY = y + h * pivotRelY;

        g2.rotate(angle, pivotX, pivotY);
        g2.drawImage(img, x, y, w, h, null);

        g2.setTransform(oldTx);
    }



    private void updateAutoMiss(long nowMs) {
        List<Long> timings = judgementManager.getCorrectTimings();

        for (int i = 0; i < timings.size(); i++) {
            if (processedIndices.contains(i)) continue;

            long correctTime = timings.get(i);

            if (nowMs > correctTime + GOOD_TIMING_MS) {
                judgementManager.forceMiss((int) nowMs);
                processedIndices.add(i);

                registerJudgement("MISS");
            }
        }
    }
}
