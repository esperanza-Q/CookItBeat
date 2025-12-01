package game.Cake;

import game.Music;
import game.rhythm.RhythmJudgementManager;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CakeStage2_oven extends CakeAnimation {

    private CakePanel controller;

    // ====== 오븐 리소스 ======
    private Image oven_base;
    private Image oven_judgment;  // fallback용

    // ✅ [추가] ENTER 키 가이드
    private Image keyEnterGuide;

    // ✅ judgement1~9 프레임
    private static final int JUDGEMENT_FRAME_COUNT = 9;
    private Image[] judgementFrames;

    // ====== 판정 매니저 (그대로 사용) ======
    private RhythmJudgementManager judgementManager;
    private boolean judged = false;
    private boolean autoMissDone = false;

    // ====== 위치 ======
    private static final int BASE_X = 0;
    private static final int BASE_Y = 0;

    // 💡 [추가] 오프셋 상수 정의 (Stage 1-1 종료 시간)
    private static final long TIME_OFFSET_MS = 41000L;

    // 💡 [수정] 오프셋이 적용된 최종 타이밍 리스트를 저장할 필드
    private final long[] BEAT_TIMES_MS;
    private final long DING_TIME_MS;

    // ====== 비트 타이밍(ms) ======
    private static final long[] ORIGINAL_BEAT_TIMES_MS = {
            82551, 82920, 83396, 83970, 84023, 84272, 84620, 85016
    };

    // 1분25초 띵(끝)
    private static final long ORIGINAL_DING_TIME_MS = 85549;

    public CakeStage2_oven(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        // ‼️ [핵심 수정] final 키워드를 사용하여 finalOffset을 실질적으로 final로 만듭니다.
// ‼️ 값을 단 한 번만 할당하며, 그 이후에는 변경되지 않습니다.
        final long finalOffset = CakeStageManager.isSurpriseStageOccurred() ? TIME_OFFSET_MS : 0;

        if (CakeStageManager.isSurpriseStageOccurred()) {
            System.out.println("🎵 Stage oven: 기습 스테이지 발생으로 타이밍 오프셋 -" + finalOffset + "ms 적용.");
        } else {
            System.out.println("🎵 Stage oven: 기습 스테이지 미발생. 타이밍 오프셋 미적용.");
        }

// 1. 가이드 타이밍 리스트 초기화
        BEAT_TIMES_MS = Arrays.stream(ORIGINAL_BEAT_TIMES_MS)
                .map(time -> time - finalOffset)
                .toArray();

// 2. 정답 타이밍 리스트 초기화
        DING_TIME_MS = ORIGINAL_DING_TIME_MS - finalOffset;


        List<Long> guideTimesMs = new ArrayList<>(stageData.getCorrectTimings());
        guideTimesMs.add(DING_TIME_MS); // 85549도 판정 노트로 추가
        Collections.sort(guideTimesMs);

        this.judgementManager = new RhythmJudgementManager(guideTimesMs, initialScoreOffset);

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (judged) return;

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    long t = currentMusicTimeMs;

                    // ---- 1) 기존 타이밍 판정 ----
                    int beforeScore = judgementManager.getScore();
                    judgementManager.handleInput((int) t);
                    int afterScore = judgementManager.getScore();

                    String timingJudge = judgementManager.getLastJudgement();
                    if (timingJudge == null) timingJudge = "NONE";
                    String up = timingJudge.toUpperCase();

                    // ✅ NONE/애매한 값이면 강제로 MISS
                    if (!(up.contains("PERFECT") || up.contains("GREAT") || up.contains("GOOD") || up.contains("MISS"))) {
                        timingJudge = "MISS";
                        up = "MISS";
                    }

                    // ---- 2) ✅ 오븐(딩 타이밍) 전용 널널 판정 ----
                    final long LENIENT_WINDOW_MS = 500;   // 딩 기준 ±500ms 까지 허용
                    final long LENIENT_PERFECT_MS = 120;  // 120ms 이내면 PERFECT
                    final long LENIENT_GOOD_MS = 300;     // 300ms 이내면 GOOD

                    long diffToDing = Math.abs(t - DING_TIME_MS);

                    // 매니저가 MISS 줬는데, 딩 근처라면 오븐 규칙으로 승급
                    if (timingJudge.equals("MISS") && diffToDing <= LENIENT_WINDOW_MS) {

                        if (diffToDing <= LENIENT_PERFECT_MS) {
                            timingJudge = "PERFECT!";
                            judgementManager.setScore(beforeScore + 100);

                        } else if (diffToDing <= LENIENT_GOOD_MS) {
                            timingJudge = "GOOD";
                            judgementManager.setScore(beforeScore + 50);

                        } else {
                            timingJudge = "MISS";
                            judgementManager.setScore(beforeScore - 10);
                        }
                    }

                    // ---- 3) 부모 판정 이미지 시스템에 전달 ----
                    registerJudgement(timingJudge);
                    judged = true;

                    // ---- 4) 띵 사운드 ----
                    String r = timingJudge.toUpperCase();
                    if (r.contains("PERFECT") || r.contains("GREAT") || r.contains("GOOD")) {
                        try {
                            Music dingSound = new Music("oven_Thing.mp3", false);
                            dingSound.start();
                        } catch (Exception ex) {
                            System.err.println("오븐 띵 소리 재생 실패: oven_Thing.mp3");
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void loadStageSpecificResources() {
        oven_base = loadImage("../images/cakeStage_image/oven/oven_judgmentX.png");
        oven_judgment = loadImage("../images/cakeStage_image/oven/oven_judgment.png");

        // ✅ judgement1~9 로드
        judgementFrames = new Image[JUDGEMENT_FRAME_COUNT];
        for (int i = 0; i < JUDGEMENT_FRAME_COUNT; i++) {
            String path = "../images/cakeStage_image/oven/judgement" + (i + 1) + ".png";
            judgementFrames[i] = loadImage(path);
        }

        // ✅ [추가] ENTER 키 가이드 로드
        keyEnterGuide = loadImage("../images/cakeStage_image/stage2/cake_keyEnter.png");
    }

    // 현재 시간이 몇 번째 비트 구간인지 (0~7)
    private int getBeatIndex(long t) {
        int idx = 0;
        while (idx < BEAT_TIMES_MS.length - 1 && t >= BEAT_TIMES_MS[idx + 1]) {
            idx++;
        }
        return idx;
    }

    // ✅ judgement 프레임 인덱스(0~8)
    private int getJudgementFrameIndex(long t) {
        if (t <= BEAT_TIMES_MS[BEAT_TIMES_MS.length - 1]) {
            return Math.min(getBeatIndex(t), 7); // judgement1~8
        }
        return 8; // judgement9
    }

    @Override
    protected void drawStageObjects(Graphics2D g2) {

        // 1) base 원본 위치/크기 그대로
        if (oven_base != null) {
            g2.drawImage(oven_base, BASE_X, BASE_Y, null);
        }

        // 2) judgement 프레임 선택 (없으면 fallback)
        Image frameToDraw = null;
        if (judgementFrames != null && judgementFrames.length == JUDGEMENT_FRAME_COUNT) {
            int fIdx = getJudgementFrameIndex(currentMusicTimeMs);
            frameToDraw = judgementFrames[fIdx];
        }
        if (frameToDraw == null) frameToDraw = oven_judgment;

        // ✅ 회전 없이 그냥 원본 위치에 그리기
        if (frameToDraw != null) {
            g2.drawImage(frameToDraw, 0, 0, null);
        }

        // ✅ [추가] ENTER 키 가이드 오버레이 (투명도 낮게)
        // 판정되기 전까지만 보여주기
        if (!judged && keyEnterGuide != null) {
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));


            float scale = 0.75f;
            int x = 875;   // 원하는 위치
            int y = 80;


            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            at.scale(scale, scale);

            // 원본 PNG가 위치 포함이면 0,0에 그대로
            g2.drawImage(keyEnterGuide, x, y, null);

            g2.setComposite(old);
        }
    }

    // updateStageLogic() 안에서
    private static final int AUTO_MISS_GRACE_MS = 700; // 200 → 700 정도로

    @Override
    public void updateStageLogic() {
        long t = currentMusicTimeMs;

        // ✅ ENTER 안 누르면 자동 MISS (유예 후)
        if (!judged && !autoMissDone && t >= DING_TIME_MS + AUTO_MISS_GRACE_MS) {

            // ✅ 자동 MISS 카운트 등록
            registerJudgement("MISS");

            judged = true;
            autoMissDone = true;
        }
    }
}
