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

    private static final String MIX_SFX = "bowl_mixing.mp3";

    // ====== Stage2 내부 처리 기록 ======
    private final Set<Integer> processedIndices = new HashSet<>();

    // ====== 회전 애니메이션 설정 ======
    private static final int WHIP_ROT_DURATION_MS = 220;
    private static final double WHIP_ROT_ANGLE = Math.toRadians(30);

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
            KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,
            KeyEvent.VK_W, KeyEvent.VK_W,
            KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,
            KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,
            KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S,
            KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S
    );

    // ✅ 유저 정답 노트가 어떤 키인지 지정
    private static final List<Integer> CORRECT_KEYS = Arrays.asList(
            KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,
            KeyEvent.VK_W, KeyEvent.VK_W,
            KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,
            KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,
            KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S,
            KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.VK_S
    );

    private static final int CARD_TRANSITION_DURATION_MS = 80;
    private static final int GOOD_TIMING_MS = 150;
    private static final int SYNC_OFFSET_MS = -50;

    // ====== 화면 배치 ======
    private static final int GUIDE_WHIP_X = 140, GUIDE_WHIP_Y = 100, GUIDE_WHIP_W = 1280, GUIDE_WHIP_H = 720;
    private static final int MY_WHIP_X = 140, MY_WHIP_Y = 100, MY_WHIP_W = 1280, MY_WHIP_H = 720;

    // ⚔️ [타이밍] 가이드
    private static final List<Long> GUIDE_TIMES_MS = Arrays.asList(
            55114L, 55519L, 55967L, 56880L, 56967L, 57170L, 57687L,
            61983L, 62377L, 62752L, 63158L, 63703L, 63746L, 64012L, 64109L, 64455L,
            68732L, 69174L, 69604L, 70028L, 70452L,
            75691L, 76037L, 76461L, 76982L
    );

    // ⚔️ [타이밍] 유저 정답
    private static final List<Long> CORRECT_TIMES_MS = Arrays.asList(
            58660L, 58895L, 59395L, 60191L, 60380L, 60615L, 61027L,
            65424L, 65830L, 66218L, 66611L, 67017L, 67157L, 67447L, 67538L, 67878L,
            72221L, 72591L, 73015L, 73457L, 73881L,
            77400L, 77734L, 78231L, 78686L
    );

    public CakeStage2(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

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

    @Override
    protected void loadStageSpecificResources() {
        guide_whipping = loadImage("../images/cakeStage_image/stage2/whipping_green_doughO.png");
        my_whipping = loadImage("../images/cakeStage_image/stage2/whipping_blue_doughO.png");
    }

    @Override
    protected void drawStageObjects(Graphics2D g2) {
        long adjustedMusicTimeMs = currentMusicTimeMs + SYNC_OFFSET_MS;

        //updateAutoMiss(adjustedMusicTimeMs);
        triggerGuideWhipIfNeeded(adjustedMusicTimeMs);

        drawRotatingWhip(
                g2, guide_whipping,
                GUIDE_WHIP_X, GUIDE_WHIP_Y, GUIDE_WHIP_W, GUIDE_WHIP_H,
                guideWhipStartMs, adjustedMusicTimeMs,
                guideWhipBaseAngleRad,
                guideWhipRotSign,
                WHIP_ROT_DURATION_MS
        );

        drawRotatingWhip(
                g2, my_whipping,
                MY_WHIP_X, MY_WHIP_Y, MY_WHIP_W, MY_WHIP_H,
                myWhipStartMs, adjustedMusicTimeMs,
                myWhipBaseAngleRad,
                myWhipRotSign,
                WHIP_ROT_DURATION_MS
        );
    }

    private void drawRotatingWhip(Graphics2D g2, Image img,
                                  int x, int y, int w, int h,
                                  long startMs, long nowMs,
                                  double baseAngleRad,
                                  double rotSign,
                                  long visibleMs) {

        if (img == null || startMs < 0) return;

        long dt = nowMs - startMs;
        if (dt < 0) return;
        if (dt > visibleMs) return;

        double t = Math.min(1.0, dt / (double) WHIP_ROT_DURATION_MS);
        double angle = baseAngleRad + rotSign * WHIP_ROT_ANGLE * t;

        AffineTransform oldTx = g2.getTransform();
        g2.setTransform(new AffineTransform());

        double pivotX = x + w / 2.0;
        double pivotY = y + h / 2.0;

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

                // ✅ 자동 MISS도 카운트/판정UI 등록
                registerJudgement("MISS");
            }
        }
    }
}
