package game.Cake;

import game.rhythm.RhythmJudgementManager;

import game.Music; // 💡 [추가] Music 클래스 임포트
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CakeStage1_1 extends CakeAnimation {

    private CakePanel controller;
    private RhythmJudgementManager judgementManager;

    private Image info_click = loadImage("../images/cakeStage_image/cakeInfo_click.png");

    // ✂️ [유지] 가위 상태 및 위치
    private boolean isScissorsActive = false;
    private static final int SCISSORS_SIZE = 250;
    protected int scissorsX = 400;
    protected int scissorsY = 400;

    // ⚔️ [유지] 쉐도우 이미지 위치 및 크기 상수
    private static final int SHADOW_SIZE_W = 180;
    private static final int SHADOW_SIZE_H = 180;

    // ✂️ [유지] 마우스 리스너 인스턴스
    private ScissorsMouseListener mouseListener;

    // 💡 [추가] 가위 클릭 효과음 파일 경로
    private static final String SCISSORS_SOUND_FILE = "../music/sissors.mp3";

    // ⚔️ [타이밍] 그림자 생성 (가이드) 타이밍
    private static final List<Long> SHADOW_CREATION_TIMES_MS = Arrays.asList(
            14031L, 14313L, 14735L, 15221L, 15592L,
            20846L, 21162L, 21587L, 22080L, 22446L, 22871L, 23296L,
            27688L, 27871L, 28038L, 28213L, 28510L, 28677L, 28875L, 29065L, 29316L, 29758L, 30222L,
            34551L, 34939L, 35369L, 35745L, 36179L, 36288L, 36613L, 37084L
    );

    // ⚔️ [타이밍] 딸기 생성 (유저 정답) 타이밍
    private static final List<Long> STRAWBERRY_CREATION_TIMES_MS = Arrays.asList(
            17371L, 17723L, 18164L, 18590L, 18980L,
            24200L, 24610L, 25010L, 25450L, 25820L, 26240L, 26700L,
            31127L, 31241L, 31480L, 31698L, 31865L, 32020L, 32300L, 32510L, 32720L, 33200L, 33646L,
            38003L, 38310L, 38808L, 39161L, 39560L, 39780L, 40048L, 40518L
    );

    // ⚔️ [타이밍] 그림자 및 딸기 일괄 소멸 타이밍
    private static final List<Long> CLEAR_TIMES_MS = Arrays.asList(
            19900L,
            27244L,
            34024L,
            41000L
    );

    // ⚔️ [유지] 카드 이미지 전환 지속 시간 (깜빡임용)
    private static final int CARD_TRANSITION_DURATION_MS = 50;

    // ⚔️ [유지] 싱크 맞춤 오프셋
    private static final int SYNC_OFFSET_MS = -50;

    // ⚔️ [핵심 수정] 유저 입력 판정 전용 오프셋 (30ms로 설정)
//    private static final int JUDGEMENT_OFFSET_MS = -190;

    // ⚔️ [제거] 낙하 관련 상수 모두 제거
    // private static final int STRAWBERRY_FALL_DURATION_MS = 20;
    // private static final int STRAWBERRY_FALL_START_OFFSET_MS = -(STRAWBERRY_FALL_DURATION_MS + 200);

    protected static final int JUDGEMENT_OFFSET_MS = 100; // 원하는 값으로 설정 (예: -30ms)

    // ⚔️ [최종 수정됨] 최대 11개의 고정된 슬롯 위치 (첫 줄 8개, 둘째 줄 3개)
    private static final Point[] SLOT_POSITIONS = {

// ➡️ 첫 번째 줄 (총 7개, 가로로 넓게 배치, Y=300~360)

            new Point(100, 360), // 매우 왼쪽
            new Point(250, 330), // 왼쪽
            new Point(400, 300), // 중앙 왼쪽
            new Point(550, 300), // 중앙 오른쪽
            new Point(700, 330), // 오른쪽
            new Point(850, 360), // 매우 오른쪽
            new Point(1000, 390), // 왼쪽
            new Point(475, 390), // 첫 줄과 둘째 줄 사이 중앙 (총 7개)

            // ⬇️ 두 번째 줄 (총 4개, Y=460~530)
            new Point(400, 460), // 중앙 왼쪽
            new Point(600, 460), // 중앙 오른쪽
            new Point(800, 500) // 오른쪽 (총 4개)
    };
    // ⚔️ [추가] 현재 사용 가능한 슬롯의 인덱스를 저장하는 리스트
    private final List<Integer> availableSlots = new LinkedList<>();

    // ⚔️ [추가] 현재 생성된 그림자의 위치 리스트 (딸기 노트에 순서대로 전달하기 위함)
    private final List<Point> shadowTargetPositions = new LinkedList<>();

    // ⚔️ [인덱스]
    private int nextShadowCreationIndex = 0;
    private int nextStrawberryCreationIndex = 0;
    private int nextClearIndex = 0;


    public CakeStage1_1(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        judgementManager = new RhythmJudgementManager(STRAWBERRY_CREATION_TIMES_MS, initialScoreOffset);

        for (int i = 0; i < SLOT_POSITIONS.length; i++) {
            availableSlots.add(i);
        }

        mouseListener = new ScissorsMouseListener();
        addFocusListener(new StageFocusListener());
    }

    @Override
    protected void loadStageSpecificResources() {
        guideCardImage1 = loadImage("../images/cakeStage_image/stage1/Card01_stage1-1.png");
        guideCardImage2 = loadImage("../images/cakeStage_image/stage1/Card02_stage1-1.png");
        scissorsImage1 = loadImage("../images/cakeStage_image/stage1/Scissors01_stage1-1.png");
        scissorsImage2 = loadImage("../images/cakeStage_image/stage1/Scissors02_stage1-1.png");
        strawberryBodyImage = loadImage("../images/cakeStage_image/stage1/Strawberry_stage1-1.png");
        shadowImage = loadImage("../images/cakeStage_image/stage1/StrawberryShadow_stage1-1.png");

        strawberryTopImage = loadImage("../images/cakeStage_image/stage1/StrawberryTop_stage1-1.png");
    }

    // 💡 [추가] 가위 클릭 효과음 재생 로직
    private void playScissorsClickSound() {
        try {
            // 클릭 효과음은 단발성이므로 Music 객체를 새로 생성하고 재생합니다.
            Music clickSound = new Music(SCISSORS_SOUND_FILE, false);
            clickSound.start();
//            System.out.println("🔊 가위 클릭 효과음 재생: " + SCISSORS_SOUND_FILE);

        } catch (Exception e) {
            System.err.println("🔴 가위 클릭 효과음 로드 또는 재생 실패.");
        }
    }

    // ‼️ [수정] 게임 로직 업데이트 메서드 (리스트 수정 전 동기화 블록 추가)
    @Override
    public void updateStageLogic() {
        long adjustedMusicTimeMs = currentMusicTimeMs + SYNC_OFFSET_MS;

        // 1. 🖼️ 그림자 생성 로직 (SHADOW_CREATION_TIMES_MS)
        if (nextShadowCreationIndex < SHADOW_CREATION_TIMES_MS.size() && !availableSlots.isEmpty()) {
            Long creationTime = SHADOW_CREATION_TIMES_MS.get(nextShadowCreationIndex);

            if (adjustedMusicTimeMs >= creationTime) {
                // ‼️ [동기화] shadowList 및 관련 리스트 수정 전 동기화
                synchronized (shadowList) {
                    spawnShadowInNextSlot();
                }
                nextShadowCreationIndex++;
            }
        }

        // 2. 🍓 딸기 노트 생성 로직 (STRAWBERRY_CREATION_TIMES_MS)
        if (nextStrawberryCreationIndex < STRAWBERRY_CREATION_TIMES_MS.size() && !shadowTargetPositions.isEmpty()) {
            Long creationTime = STRAWBERRY_CREATION_TIMES_MS.get(nextStrawberryCreationIndex);

            if (adjustedMusicTimeMs >= creationTime-200) {
                // ‼️ [동기화] strawberryList 수정 전 동기화
                synchronized (strawberryList) {
                    spawnStrawberryNote(creationTime);
                }
                nextStrawberryCreationIndex++;
            }
        }

        // 3. 💣 일괄 소멸 로직 (CLEAR_TIMES_MS)
        if (nextClearIndex < CLEAR_TIMES_MS.size()) {
            Long clearTime = CLEAR_TIMES_MS.get(nextClearIndex);

            if (adjustedMusicTimeMs >= clearTime) {
                // ‼️ [추가] 딸기 노트 일괄 제거
                synchronized (strawberryList) {
                    // clearTime에 도달한 딸기 노트들을 모두 제거합니다.
                    strawberryList.clear();
                }

                // ‼️ [기존] 그림자 일괄 제거 및 슬롯 리셋
                synchronized (shadowList) {
                    clearShadowsAndResetSlots();
                }
                nextClearIndex++;
            }
        }

        // 4. 🍓 딸기 객체 정리 로직 (shouldBeRemoved가 false로 설정되어 있으므로 현재는 아무 일도 일어나지 않음)
        synchronized (strawberryList) {
            strawberryList.removeIf(strawberry -> strawberry.shouldBeRemoved(adjustedMusicTimeMs));
        }

        // 5. 💔 미스 처리 (유지)
    }

    // ‼️ [유지] 슬롯 기반 그림자 생성
    private void spawnShadowInNextSlot() {
        if (availableSlots.isEmpty()) return;

        int slotIndex = availableSlots.remove(0);
        Point position = SLOT_POSITIONS[slotIndex];

        // 그림자 생성 (위치 고정)
        shadowList.add(new SlotShadow(
                shadowImage,
                position.x,
                position.y,
                SHADOW_SIZE_W,
                SHADOW_SIZE_H,
                slotIndex
        ));

        // ‼️ 딸기 노트가 이 위치를 따라 떨어지도록 위치 리스트에 순서대로 추가
        shadowTargetPositions.add(position);
    }

    // ‼️ [수정] 딸기 노트 생성 (낙하 없이 즉시 생성)
    private void spawnStrawberryNote(long creationTime) {
        if (shadowTargetPositions.isEmpty()) return;

        Point targetPos = shadowTargetPositions.remove(0);

        // ‼️ [수정] 낙하 시작 시간 필드는 이제 딸기가 생성된 시간(정답 타이밍)을 저장합니다.
        long spawnTime = creationTime;

        strawberryList.add(new StrawberryNote(
                strawberryBodyImage,
                strawberryTopImage,
                spawnTime,
                targetPos,
                nextStrawberryCreationIndex // ‼️ 인덱스를 전달
        ));
    }

    // ‼️ [유지] 그림자 소멸 및 슬롯 리셋
    private void clearShadowsAndResetSlots() {
        for (Shadow shadow : shadowList) {
            if (shadow instanceof SlotShadow) {
                availableSlots.add(((SlotShadow) shadow).getSlotIndex());
            }
        }
        shadowList.clear();
        availableSlots.sort(null);

        shadowTargetPositions.clear();
    }

    // ‼️ [수정] 그리기 메서드 (리스트 접근 전 동기화 블록 추가)
    @Override
    protected void drawStageObjects(Graphics2D g2) {

        long adjustedMusicTimeMs = currentMusicTimeMs + SYNC_OFFSET_MS;

        int desiredHeight = 80;
        int originalWidth = info_click.getWidth(null);
        int originalHeight = info_click.getHeight(null);
        int newWidth = (int) ((double) originalWidth * desiredHeight / originalHeight);

        g2.drawImage(info_click, 1110, 180, newWidth, desiredHeight, null);

        // 1. ⚔️ 카드 이미지 전환 로직 (유지)
        boolean isPulseActive = false;
        for (Long startTime : SHADOW_CREATION_TIMES_MS) {
            long endTime = startTime + CARD_TRANSITION_DURATION_MS;

            if (adjustedMusicTimeMs >= startTime && adjustedMusicTimeMs < endTime) {
                isPulseActive = true;
                break;
            }
        }

        // 2. 🖼️ 가이드 카드병정 이미지 그리기 (유지)
        Image currentGuideImage = isPulseActive ? guideCardImage2 : guideCardImage1;
        guideCardImage = currentGuideImage;

        if (guideCardImage != null) {
            g2.drawImage(guideCardImage, 0, 0, getWidth(), getHeight(), null);
        }

        // 3. 🍓 누적된 그림자 객체 모두 그리기
        // ‼️ [동기화] shadowList 읽기 전 동기화
        synchronized (shadowList) {
            for (Shadow shadow : shadowList) {
                shadow.draw(g2);
            }
        }

        // 4. 🍓 떨어지는 딸기 노트 모두 그리기
        // ‼️ [동기화] strawberryList 읽기 전 동기화
        synchronized (strawberryList) {
            for (StrawberryNote strawberry : strawberryList) {
                strawberry.draw(g2, adjustedMusicTimeMs);
            }
        }


        // 5. ✂️ 마우스 상태에 따른 가위 이미지 그리기 (유지)
        Image currentScissorsImage = isScissorsActive ? scissorsImage2 : scissorsImage1;

        if (currentScissorsImage != null) {
            g2.drawImage(
                    currentScissorsImage,
                    scissorsX,
                    scissorsY,
                    SCISSORS_SIZE,
                    SCISSORS_SIZE,
                    null
            );
        }
    }

    // ✂️ [핵심 수정] 마우스 리스너 내부 클래스 (클릭 판정 로직 수정)
    private class ScissorsMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            isScissorsActive = true;
            scissorsX = e.getX() - (SCISSORS_SIZE / 2);
            scissorsY = e.getY() - (SCISSORS_SIZE / 2);

            // 💡 [핵심 추가] 가위 클릭 효과음 재생
            playScissorsClickSound();

            // ‼️ [수정] 판정 시간 계산 시, JUDGEMENT_OFFSET_MS(30ms)를 사용
            long clickTime = currentMusicTimeMs + JUDGEMENT_OFFSET_MS;

            // ‼️ [핵심 로그 추가] ‼️
            long adjustedMusicTime = clickTime; // 이미 SYNC_OFFSET_MS가 적용된 시간
            System.out.println("--------------------------------------------------");
            System.out.println("[INPUT] Click Pressed!");
            System.out.println("[MUSIC] Raw Music Time (ms): " + currentMusicTimeMs);
            System.out.println("[JUDGE] Adjusted Time (ms):  " + adjustedMusicTime);
            System.out.println("--------------------------------------------------");

            // 1. 판정 실행 및 판정 성공 인덱스 획득
            int judgedIndex = judgementManager.handleInput((int)clickTime);

            // 💡 [핵심 추가] judgementManager의 현재 점수를 StageManager에 저장
            int currentTotalScore = judgementManager.getScore();
            CakeStageManager.setCumulativeScore(currentTotalScore);

            // 2. 판정 결과 문자열 획득
            String judgementResultString = judgementManager.getLastJudgement();

            // ✅ 카운트/판정UI 등록 통일
            registerJudgement(judgementResultString);

            // ‼️ [추가] 판정 결과를 상위 클래스 필드에 저장 및 표시 시간 업데이트
            lastJudgementResult = judgementResultString;
            judgementDisplayStartTime = currentMusicTimeMs;

            // 3. 판정이 성공했고, 컷팅할 딸기 노트를 찾아 상태 업데이트
            if (judgedIndex != -1) {
                // ‼️ [핵심 수정] 판정된 인덱스 judgedIndex를 사용하여 딸기 노트를 찾습니다.

                // 4. 판정 결과 문자열에 따라 컷팅 상태 업데이트
                boolean isCut = judgementResultString.equals("GOOD") ||
                        judgementResultString.equals("GREAT!") ||
                        judgementResultString.equals("PERFECT!");

                if (isCut) {
                    synchronized (strawberryList) {
                        // ‼️ judgedIndex와 동일한 인덱스를 가진 노트를 찾아 컷팅
                        for (StrawberryNote strawberry : strawberryList) {
                            if (strawberry.getNoteIndex() == judgedIndex) {
                                strawberry.setCut(true);
                                break;
                            }
                        }
                    }
                }
            } else {
                // 판정에 실패했더라도 MISS 판정은 이미 judgementManager에서 설정됨
                // 따라서 lastJudgementResult는 "MISS"가 됩니다.
            }

            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isScissorsActive = false;
            repaint();
        }
    }

    // ✂️ [유지] 포커스 리스너
    private class StageFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            addMouseListener(mouseListener);
            System.out.println("Stage 1-1 활성화: 마우스 리스너 등록됨.");
        }

        @Override
        public void focusLost(FocusEvent e) {
            removeMouseListener(mouseListener);
            isScissorsActive = false;
            System.out.println("Stage 1-1 비활성화: 마우스 리스너 제거됨.");
        }
    }
}