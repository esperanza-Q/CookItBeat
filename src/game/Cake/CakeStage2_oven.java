package game.Cake;

import game.Music;
import game.rhythm.RhythmJudgementManager;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CakeStage2_oven extends CakeAnimation {

    private CakePanel controller;

    // ====== 오븐 리소스 ======
    private Image oven_base;
    private Image oven_judgment;  // fallback용

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

    // ====== 비트 타이밍(ms) ======
    private static final long[] BEAT_TIMES_MS = {
            82551, 82920, 83396, 83970, 84023, 84272, 84620, 85016
    };

    // 1분25초 띵(끝)
    private static final long DING_TIME_MS = 85549;

    public CakeStage2_oven(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

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
                            // MISS 패널티(-10) 무시하고 PERFECT 점수로 덮기
                            judgementManager.setScore(beforeScore + 100);

                        } else if (diffToDing <= LENIENT_GOOD_MS) {
                            timingJudge = "GOOD";
                            judgementManager.setScore(beforeScore + 50);

                        } else {
                            timingJudge = "MISS"; // 윈도우 안인데도 애매하면 MISS 유지
                            judgementManager.setScore(beforeScore - 10);
                        }
                    }

                    // ---- 3) 부모 판정 이미지 시스템에 전달 ----
                    lastJudgementResult = timingJudge;
                    judgementDisplayStartTime = t;

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

        // ❌ 텍스트 판정 출력 제거 (이제 CakeAnimation.drawUI가 이미지로 처리)
    }

    // updateStageLogic() 안에서
    private static final int AUTO_MISS_GRACE_MS = 700; // 200 → 700 정도로


    @Override
    public void updateStageLogic() {
        long t = currentMusicTimeMs;

        // ✅ ENTER 안 누르면 자동 MISS (유예 후)
        if (!judged && !autoMissDone && t >= DING_TIME_MS + AUTO_MISS_GRACE_MS) {
            lastJudgementResult = "MISS";
            judgementDisplayStartTime = t;

            judged = true;
            autoMissDone = true;
        }
    }
}
