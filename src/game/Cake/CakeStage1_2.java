package game.Cake;

import game.rhythm.RhythmJudgementManager;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CakeStage1_2 extends CakeAnimation {

    private CakePanel controller;

    private boolean isCatHandActive = false;
    private RhythmJudgementManager judgementManager;


    // ⚔️ [타이밍] 그림자 생성 (가이드) 타이밍
    private static final java.util.List<Long> GUIDE_TIMES_MS = Arrays.asList(
            41308L, 41519L, 41736L, 42159L, 42386L, 42600L, // 41초 따따따 따따따
            44731L, 45173L, 45607L, 46025L, // 44초 따아~ 따 따아~ 따
            48372L, 48581L, 49250L, 49450L // 48초 (따묵음)따따 (따묵음)따따
    );

    // ⚔️ [타이밍] 딸기 생성 (유저 정답) 타이밍
    private static final List<Long> CORRECT_TIMES_MS = Arrays.asList(
            43026L, 43250L, 43441L, 43880L, 44100L, 44305L, // 43초 따따따 따따따
            46498L, 46885L, 47307L, 47732L, // 46초 따아~ 따 따아~ 따
            50122L, 50403L, 50965L, 51174L  // 50초 (따묵음)따따 (따묵음)따따
    );

    // ⚔️ [유지] 카드 이미지 전환 지속 시간 (깜빡임용)
    private static final int CARD_TRANSITION_DURATION_MS = 50;

    // ⚔️ [유지] 싱크 맞춤 오프셋
    private static final int SYNC_OFFSET_MS = -50;

    public CakeStage1_2(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        super(controller, stageData, initialScoreOffset);
        this.controller = controller;

        // 💡 오프셋 값 정의 (재정의)
        final long OFFSET_MS = 100;

        // ‼️ [핵심 수정] judgementManager를 오프셋이 적용된 리스트로 한 번만 초기화합니다.
        this.judgementManager = new RhythmJudgementManager(
                // 1. 오프셋 적용된 리스트 생성
                CORRECT_TIMES_MS.stream()
                        .map(startTime -> startTime + OFFSET_MS)
                        .collect(Collectors.toList()),
                // 2. 초기 점수 오프셋을 두 번째 인수로 전달
                initialScoreOffset
        );

        // ❌ [제거 필요] 이 코드는 초기화가 중복됩니다.
        // this.judgementManager = new RhythmJudgementManager(CORRECT_TIMES_MS);

        // ‼️ [핵심 추가] KeyListener 등록
        this.addKeyListener(new catHandListener());
        // ‼️ [핵심 추가] 키 이벤트를 받기 위해 포커스 요청
        this.requestFocusInWindow();
    }

    @Override
    protected void loadStageSpecificResources() {
        // 가이드 카드병정 이미지 로드
        guideCardImage1 = loadImage("../images/cakeStage_image/stage1/Card01_stage1-2.png");
        guideCardImage2 = loadImage("../images/cakeStage_image/stage1/Card02_stage1-2.png");

        catHandImage1 = loadImage("../images/cakeStage_image/stage1/CatHand01_stage1-2.png");
        catHandImage2 = loadImage("../images/cakeStage_image/stage1/CatHand02_stage1-2.png");

        // 1단계 기본 도구 (가위) 로드 (필요없지만 필드가 CakeAnimation에 남아있으므로 로딩만 유지)
        playerToolImage = loadImage("../images/cakeStage_image/stage1/Scissors01_stage1-1.png");

        // 재료 이미지 로드 (필요없지만 필드가 CakeAnimation에 남아있으므로 로딩만 유지)
        strawberryBodyImage = loadImage("../images/cakeStage_image/stage1/Strawberry_stage1-1.png");
        shadowImage = loadImage("../images/cakeStage_image/stage1/StrawberryShadow_stage1-1.png");
    }

    // ‼️ [수정] 그리기 메서드 (리스트 접근 전 동기화 블록 추가)
    @Override
    protected void drawStageObjects(Graphics2D g2) {

        long adjustedMusicTimeMs = currentMusicTimeMs + SYNC_OFFSET_MS;

        // 💡 새로운 오프셋 값 (예: 500ms)
        final long ADDITIONAL_OFFSET_MS = 100;

        // 1. ⚔️ 카드 이미지 전환 로직 (유지)
        boolean isPulseActive = false;
        for (Long startTime : GUIDE_TIMES_MS) {
            // 루프 안에서 'startTime'에 추가 오프셋을 더해서 사용
            long offsetStartTime = startTime + ADDITIONAL_OFFSET_MS; // <--- 여기에 추가!

            long endTime = offsetStartTime + CARD_TRANSITION_DURATION_MS; // offsetStartTime 사용

            if (adjustedMusicTimeMs >= offsetStartTime && adjustedMusicTimeMs < endTime) { // offsetStartTime 사용
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

        // 3. 고양이 손
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

    // ✂️ [핵심 수정] 마우스 리스너 내부 클래스 -> 키 리스너로 변환 (스페이스바)
    private class catHandListener extends KeyAdapter {

        // 💡 [수정] mousePressed -> keyPressed로 변경
        @Override
        public void keyPressed(KeyEvent e) {
            // ‼️ [핵심 추가] 눌린 키가 스페이스바(VK_SPACE)인지 확인
            if (e.getKeyCode() != KeyEvent.VK_SPACE) {
                return;
            }

            // ‼️ [수정] 스페이스바가 눌렸을 때 isCatHandActive를 true로 설정
            isCatHandActive = true;

            // ‼️ [수정] 판정 시간 계산 시, JUDGEMENT_OFFSET_MS(30ms)를 사용
            // (이하 로직은 mousePressed와 동일하게 유지)
            long clickTime = currentMusicTimeMs + JUDGEMENT_OFFSET_MS;

            // ‼️ [핵심 로그 추가] ‼️
            long adjustedMusicTime = clickTime; // 이미 SYNC_OFFSET_MS가 적용된 시간
            System.out.println("--------------------------------------------------");
            System.out.println("[INPUT] Spacebar Pressed!"); // 로그 메시지 수정
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

            // ‼️ [추가] 판정 결과를 상위 클래스 필드에 저장 및 표시 시간 업데이트
            lastJudgementResult = judgementResultString;
            judgementDisplayStartTime = currentMusicTimeMs;

            repaint();
        }

        // 💡 [수정] mouseReleased -> keyReleased로 변경
        @Override
        public void keyReleased(KeyEvent e) {
            // ‼️ [핵심 추가] 뗀 키가 스페이스바(VK_SPACE)인지 확인
            if (e.getKeyCode() != KeyEvent.VK_SPACE) {
                return;
            }

            // ‼️ [수정] 스페이스바가 떼어졌을 때 isCatHandActive를 false로 설정
            isCatHandActive = false;
            repaint();
        }
    }

    // 키 입력 시 실행할 스테이지 고유의 추가 로직 제거
    // @Override
    // protected void processKeyInput(int keyCode) { ... }
}