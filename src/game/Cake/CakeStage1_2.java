package game.Cake;

import game.rhythm.RhythmJudgementManager;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CakeStage1_2 extends CakeAnimation {

    private CakePanel controller;

    private boolean isCatHandActive = false;
    private RhythmJudgementManager judgementManager;

    private Image info_space = loadImage("../images/cakeStage_image/cakeInfo_space.png");

    // 🍳 [추가] 계란 애니메이션 상태 필드
    private boolean isEggDropped = false;
    private long eggDropStartTime = 0;
    private static final int EGG_DROP_DURATION = 200; // 계란이 떨어지는 애니메이션 시간 (0.5초)
    private static final int EGG_DROP_DISTANCE = 450; // 계란이 Y축으로 떨어지는 최대 거리 (픽셀)

    // 💡 [추가] 오프셋 상수 정의 (Stage 1-1 종료 시간)
    private static final long TIME_OFFSET_MS = 41000L;

    // ⚔️ [타이밍] 그림자 생성 (가이드) 타이밍
    private static final java.util.List<Long> ORIGINAL_GUIDE_TIMES_MS = Arrays.asList(
            41308L, 41519L, 41736L, 42159L, 42386L, 42600L,
            44731L, 45173L, 45607L, 46025L,
            48372L, 48581L, 49250L, 49450L
    );

    // ⚔️ [타이밍] 유저 정답 타이밍 (계란 떨어지는 타이밍)
    private static final List<Long> ORIGINAL_CORRECT_TIMES_MS = Arrays.asList(
            43026L, 43250L, 43441L, 43880L, 44100L, 44305L,
            46498L, 46885L, 47307L, 47732L,
            50122L, 50403L, 50965L, 51174L
    );



    private Image box;
    private Image bowl;
    private Image egg;
    private Image dropEgg;


    // 💡 [수정] 오프셋이 적용된 최종 타이밍 리스트를 저장할 필드
    private final List<Long> GUIDE_TIMES_MS;
    private final List<Long> CORRECT_TIMES_MS;

    // ⚔️ [유지] 카드 이미지 전환 지속 시간 (깜빡임용)
    private static final int CARD_TRANSITION_DURATION_MS = 50;

    // ⚔️ [유지] 싱크 맞춤 오프셋
    private static final int SYNC_OFFSET_MS = -50;

//    private static final int JUDGEMENT_OFFSET_MS = -190;

    public CakeStage1_2(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        // ‼️ [핵심 수정] final 키워드를 사용하여 finalOffset을 실질적으로 final로 만듭니다.
// ‼️ 값을 단 한 번만 할당하며, 그 이후에는 변경되지 않습니다.
        final long finalOffset = CakeStageManager.isSurpriseStageOccurred() ? TIME_OFFSET_MS : 0;

        if (CakeStageManager.isSurpriseStageOccurred()) {
            System.out.println("🎵 Stage 1-2: 기습 스테이지 발생으로 타이밍 오프셋 -" + finalOffset + "ms 적용.");
        } else {
            System.out.println("🎵 Stage 1-2: 기습 스테이지 미발생. 타이밍 오프셋 미적용.");
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
                        .map(startTime -> startTime + OFFSET_MS)
                        .collect(Collectors.toList()),
                initialScoreOffset
        );

        this.addKeyListener(new catHandListener());
        this.requestFocusInWindow();
    }

    @Override
    protected void loadStageSpecificResources() {
        // 배경 오브젝트 로드
        box = loadImage("../images/cakeStage_image/stage1/boxAndCookingbowl_back.png");
        bowl = loadImage("../images/cakeStage_image/stage1/cookingbowl_front.png");
        egg = loadImage("../images/cakeStage_image/stage1/Egg01_stage1-2.png");
        dropEgg = loadImage("../images/cakeStage_image/stage1/Egg02_stage1-2.png");

        // 가이드 카드병정 이미지 로드
        guideCardImage1 = loadImage("../images/cakeStage_image/stage1/Card01_stage1-2.png");
        guideCardImage2 = loadImage("../images/cakeStage_image/stage1/Card02_stage1-2.png");

        catHandImage1 = loadImage("../images/cakeStage_image/stage1/CatHand01_stage1-2.png");
        catHandImage2 = loadImage("../images/cakeStage_image/stage1/CatHand02_stage1-2.png");

        // 나머지 필드 로딩 (사용되지 않더라도 부모 클래스 필드 유지 위해 로딩)
        playerToolImage = loadImage("../images/cakeStage_image/stage1/Scissors01_stage1-1.png");
        strawberryBodyImage = loadImage("../images/cakeStage_image/stage1/Strawberry_stage1-1.png");
        shadowImage = loadImage("../images/cakeStage_image/stage1/StrawberryShadow_stage1-1.png");
    }

    // ‼️ [수정] 그리기 메서드
    @Override
    protected void drawStageObjects(Graphics2D g2) {

        long adjustedMusicTimeMs = currentMusicTimeMs + SYNC_OFFSET_MS;

        int desiredHeight = 80;
        int originalWidth = info_space.getWidth(null);
        int originalHeight = info_space.getHeight(null);
        int newWidth = (int) ((double) originalWidth * desiredHeight / originalHeight);

        g2.drawImage(info_space, 1100, 150, newWidth, desiredHeight, null);

        final long ADDITIONAL_OFFSET_MS = 100;

        // 1. ⚔️ 카드 이미지 전환 로직
        boolean isPulseActive = false;
        for (Long startTime : GUIDE_TIMES_MS) {
            long offsetStartTime = startTime + ADDITIONAL_OFFSET_MS;
            long endTime = offsetStartTime + CARD_TRANSITION_DURATION_MS;

            if (adjustedMusicTimeMs >= offsetStartTime && adjustedMusicTimeMs < endTime) {
                isPulseActive = true;
                break;
            }
        }

        // 2. 🖼️ 가이드 카드병정 이미지 그리기
        Image currentGuideImage = isPulseActive ? guideCardImage2 : guideCardImage1;
        guideCardImage = currentGuideImage;

        if (guideCardImage != null) {
            g2.drawImage(guideCardImage, 0, 0, getWidth(), getHeight(), null);
        }

        // 3. 배경 오브젝트 (박스)
        if (box != null) {
            g2.drawImage(box, 0, 0, getWidth(), getHeight(), null);
        }

        g2.drawImage(egg, 0, 0, getWidth(), getHeight(), null);

        // 4. 🥚 계란 드롭 애니메이션 처리 (박스 앞에, 볼 뒤에)
        long timeElapsed = currentMusicTimeMs - eggDropStartTime;

        if (isEggDropped && timeElapsed < EGG_DROP_DURATION && dropEgg != null) {
            // 애니메이션 진행률 (0.0에서 1.0)
            double progress = (double)timeElapsed / EGG_DROP_DURATION;

            // 떨어지는 Y 위치 계산
            // 원본 이미지의 계란 위치(y)를 기준으로 아래로 떨어지게 설정
            int startY = 40; // 이미지의 최상단
            int dropY = (int) (startY + (EGG_DROP_DISTANCE * progress));

            // 드롭 계란 이미지를 그립니다.
            g2.drawImage(dropEgg, 550, dropY, 250, 250, null);
        } else if (isEggDropped && timeElapsed >= EGG_DROP_DURATION) {
            // 애니메이션이 끝나면 상태 리셋
            isEggDropped = false;
        }


        // 5. 🥚 기본 계란 이미지 (드롭 애니메이션이 끝났거나 Miss일 때)
        // 드롭 애니메이션이 진행 중이지 않을 때만 원래 계란 이미지를 그립니다.
//        if (!isEggDropped || timeElapsed >= EGG_DROP_DURATION) {
//            if (egg != null) {
//                g2.drawImage(egg, 0, 0, getWidth(), getHeight(), null);
//            }
//        }

        // 6. 🥣 볼 (박스와 계란 위에)
        if (bowl != null) {
            g2.drawImage(bowl, 0, 0, getWidth(), getHeight(), null);
        }

        // 7. 고양이 손
        Image currentCatHandImage = isCatHandActive ? catHandImage2 : catHandImage1;

        if (currentCatHandImage != null) {
            g2.drawImage(
                    currentCatHandImage,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    null
            );
        }
    }

    // ✂️ [핵심 수정] 키 리스너 내부 클래스
    private class catHandListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() != KeyEvent.VK_SPACE) {
                return;
            }

            isCatHandActive = true;

            long clickTime = currentMusicTimeMs;

            // 1. 판정 실행 및 판정 성공 인덱스 획득
            int judgedIndex = judgementManager.handleInput((int)clickTime);

            // 💡 [핵심 추가] judgementManager의 현재 점수를 StageManager에 저장
            int currentTotalScore = judgementManager.getScore();
            CakeStageManager.setCumulativeScore(currentTotalScore);

            // 2. 판정 결과 문자열 획득
            String judgementResultString = judgementManager.getLastJudgement();

            // ‼️ [핵심 수정] 판정이 Good 이상일 때만 계란 드롭 상태 업데이트
            if (judgementResultString.equals("PERFECT!") ||
                    judgementResultString.equals("GREAT!") ||
                    judgementResultString.equals("GOOD")) {

                isEggDropped = true;
                eggDropStartTime = currentMusicTimeMs;

            } else {
                // MISS인 경우 아무것도 떨어지지 않습니다.
                isEggDropped = false;
            }

            // ‼️ [추가] 판정 결과를 상위 클래스 필드에 저장 및 표시 시간 업데이트
            lastJudgementResult = judgementResultString;
            judgementDisplayStartTime = currentMusicTimeMs;

            repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() != KeyEvent.VK_SPACE) {
                return;
            }

            isCatHandActive = false;
            repaint();
        }
    }
}