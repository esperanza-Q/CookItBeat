package game.Space;

import game.Main;
import game.Music;
import game.rhythm.RhythmJudgementManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;

public class SpaceStage1 extends SpaceAnimation {

    // 이미지
    private Image alien1;
    private Image alien2;
    private Image cat1;
    private Image cat2;
    private Image cannon;

    // ‼️ 기존: 현재 보여줄 이미지 (cat1으로 고정)
    private Image currentUser;

    // ✅ [추가] 외계인 손 현재 이미지
    private Image currentAlien;

    private Image info_space = new ImageIcon(Main.class.getResource("../images/mainUI/info_space.png")).getImage();

    // ✅ [추가] 물총 애니메이션 관련 변수
    private Image currentWaterImage = null;
    private Timer waterAnimationTimer;
    private int waterFrameIndex = 0;
    private final int WATER_ANIMATION_DELAY = 50; // 물총 이미지 전환 속도 (ms)

    private boolean isHolding = false;
    private long pressTime;
    private boolean autoReverse = false;

    private Image L_currentControlImage;
    private Image R_currentControlImage;
    private Image ufo;

    private boolean isAnimating = false; // 컨트롤러 중복 애니메이션 방지
    private Timer forwardTimer, reverseTimer;
    private int frameIndex = 0;
    private Image[] rightFrames; // 컨트롤러 애니메이션 프레임 배열

    // 이벤트 발동 여부
    private boolean event1Triggered = false;
    private boolean event2Triggered = false;
    private boolean event3Triggered = false;
    private boolean event4Triggered = false;
    private boolean event5Triggered = false;
    private boolean event6Triggered = false;
    private boolean event7Triggered = false;
    private boolean event8Triggered = false;

    // 전환 타이밍 (ms 기준)
    private final int ALIEN_APPEAR_TIME_1 = 2 * 1000; // 0:02
    private final int ALIEN_APPEAR_TIME_2 = 4 * 1000; // 0:04
    private final int ALIEN_APPEAR_TIME_3 = (int) (6.7 * 1000);  // 0:06.7
    private final int ALIEN_APPEAR_TIME_4 = 10 * 1000; // 0:10
    private final int ALIEN_APPEAR_TIME_5 = 14 * 1000; // 0:14
    private final int ALIEN_APPEAR_TIME_6 = 17 * 1000;  // 0:17
    private final int ALIEN_APPEAR_TIME_7 = (int) (20.4 * 1000); // 0:20.4
    private final int ALIEN_APPEAR_TIME_8 = 22 * 1000; // 0:22

    // ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경
    private static final int[] ALIEN_PRESS_TIMES_INT = {
            // 외계인 손을 움직이는 타이밍은 여기 입력
            3000,
            6853, 7117, 7551, 7709,
            14131, 16283, 16493, 16714,
            20984, 21233, 21863
    };

    // ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경 (판정 정답 타이밍)
    private static final int[] USER_PRESS_TIMES_INT = {
            // 6.5초 딴 (6.417s)
            6417,
            // 13초 따단따단 (10.284s, 10.547s, 10.942s, 11.140s)
            10284, 10547, 10942, 11140,
            // 17초 딴, 19초 딴딴딴 (17.563s, 19.704s, 19.931s, 20.140s)
            17563, 19704, 19931, 20140,
            // 22초 딴딴 딴 (22.711s, 22.938s, 23.533s)
            22711, 22938, 23533
    };

    // ✅ 외계인 손이 alien2로 바뀐 후 돌아오는 타이밍
    private final int ALIEN_RELEASE_DELAY_MS = 50;
    // ‼️ 인스턴스 변수이므로 super() 호출 후 초기화해야 함
    private final int[] ALIEN_RELEASE_TIMES;


    // ✅ [추가] static 헬퍼 메서드: int[]를 long[]으로 변환 (생성자 오류 해결)
    private static long[] convertToLongArray(int[] array) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public SpaceStage1() {
        // 1. super() 호출을 첫 줄로 배치하고, static 헬퍼 메서드를 통해 인자를 준비합니다.
        // ‼️ 판정 타이밍 배열(USER_PRESS_TIMES_INT)을 부모 클래스에 전달합니다.
        super(convertToLongArray(USER_PRESS_TIMES_INT));


        // 2. 인스턴스 변수인 ALIEN_RELEASE_TIMES 초기화 (super() 호출 후 가능)
        ALIEN_RELEASE_TIMES = new int[ALIEN_PRESS_TIMES_INT.length];

        // ✅ 외계인 손 이미지 전환 해제 타이밍 계산
        for (int i = 0; i < ALIEN_PRESS_TIMES_INT.length; i++) {
            ALIEN_RELEASE_TIMES[i] = ALIEN_PRESS_TIMES_INT[i] + ALIEN_RELEASE_DELAY_MS;
        }

        // 3. 이미지 로드
        alien1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/hologram_alien1.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/hologram_alien2.png")).getImage();
        cat1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand01.png")).getImage();
        cat2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand02.png")).getImage();

        cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon02.png")).getImage();

        // ‼️ currentUser는 cat1으로 고정 (사용자가 SpaceBar 누를 때만 cat2로 변경)
        currentUser = cat1;
        // ‼️ 외계인 손은 초기엔 alien1 또는 null로 설정 (화면에 표시 여부는 processStageEvents에서 제어)
        currentAlien = alien1; // 초기에는 보이지 않도록 null로 설정

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_SPACE) {

                    Music.playEffect("water_pong2.mp3");
                    isHolding = true;
                    pressTime = System.currentTimeMillis();
                    autoReverse = false;

                    startForwardAnimation();

                    // ✅ [수정] 스페이스바 로직(판정 처리 포함)을 처리하는 protected 메서드 호출
                    processSpaceKeyPressLogic();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && isHolding) {
                    isHolding = false;
                }
            }
        });

        // ✅ [추가] 물총 애니메이션 타이머 설정
        setupWaterAnimationTimer();
    }

    // ✅ [추가] 물총 애니메이션 타이머 설정 메서드
    private void setupWaterAnimationTimer() {
        waterAnimationTimer = new Timer(WATER_ANIMATION_DELAY, e -> {
            waterFrameIndex++;
            if (waterFrameIndex < waterFrames.length) {
                currentWaterImage = waterFrames[waterFrameIndex];
            } else {
                // 애니메이션 종료 후 이미지 null로 설정
                waterAnimationTimer.stop();
                currentWaterImage = null;
            }
            repaint();
        });
        waterAnimationTimer.setRepeats(true);
    }

    // ✅ [추가] 물총 애니메이션 시작 메서드
    protected void startWaterAnimation() {
        if (waterAnimationTimer.isRunning()) {
            waterAnimationTimer.stop(); // 중복 방지 및 리셋
        }
        waterFrameIndex = 0;
        currentWaterImage = waterFrames[waterFrameIndex];
        waterAnimationTimer.start();
        repaint();
    }

    // ✅ [오버라이드] SpaceAnimation에서 추출한 메서드를 오버라이드하여 물총 애니메이션 실행
    @Override
    protected void processSpaceKeyPress() {
        // ‼️ 부모 클래스(SpaceAnimation)에서 판정 처리 후 이 메서드가 호출됨
        startWaterAnimation(); // SpaceStage1의 물총 애니메이션 시작 메서드 호출
    }


    @Override
    public void updateByMusicTime(int t) {
        super.updateByMusicTime(t); // SpaceAnimation의 점수 업데이트 및 기본 로직 호출

        // ✅ 외계인 손 자동 동작 타이밍 확인 (ALIEN_PRESS_TIMES_INT 사용)
        for (int pressTime : ALIEN_PRESS_TIMES_INT) {
            if (t >= pressTime && t < pressTime + 50) {
                if (currentAlien == alien1) currentAlien = alien2;
                break;
            }
        }

        for (int releaseTime : ALIEN_RELEASE_TIMES) {
            if (t >= releaseTime && t < releaseTime + 50) {
                if (currentAlien == alien2) currentAlien = alien1;
                break;
            }
        }
    }

    @Override
    public void drawStageObjects(Graphics g) {
        // ‼️ 고양이 손은 현재 위치 그대로 그립니다.
        g.drawImage(currentUser, 0, 0, null);


        int desiredHeight = 50;
        int originalWidth = info_space.getWidth(null);
        int originalHeight = info_space.getHeight(null);
        int newWidth = (int) ((double) originalWidth * desiredHeight / originalHeight);

        // ✅ 외계인 손을 왼쪽 y축 중간에 작게 그립니다.
        if (currentAlien != null) {
            g.drawImage(currentAlien, 0, -10, getWidth(), getHeight(), null);
        }

        g.drawImage(info_space, 320, 425, newWidth, desiredHeight, null);

        // ✅ [추가] 물총 그리기 (Stage1에만 적용)
        if (currentWaterImage != null) {
            // 물총 이미지 크기 및 위치 조정 (화면 전체에 맞춤)
            int w = getWidth();
            int h = getHeight();
            g.drawImage(currentWaterImage, 25, -190, w, h, this);
        }
    }

    @Override
    public Image getCannon() {
        return cannon;
    }

    @Override
    protected void changeStageImageOnPress() {
        // ‼️ currentUser가 cat1일 때만 cat2로 변경
        if (currentUser == cat1) this.currentUser = cat2;
    }

    @Override
    protected void changeStageImageOnRelease() {
        // ‼️ currentUser가 cat2일 때만 cat1으로 변경
        if (currentUser == cat2) this.currentUser = cat1;
    }

    @Override
    protected void processStageEvents(int t) {
        // ‼️ 이벤트 타이밍에 따라 currentAlien (외계인 손)의 보이기/숨기기 및 이미지를 제어합니다.

        // 1. 초기화 (초기 상태)
        if (t < ALIEN_APPEAR_TIME_1 && currentAlien != null) { currentAlien = alien1; }

        // 2. 외계인 손 등장 및 이미지 변경 로직
        // 외계인 손이 등장하는 시점에 alien1로 설정
        if (!event1Triggered && t >= ALIEN_APPEAR_TIME_1) { event1Triggered = true; currentAlien = alien1; }
        if (!event2Triggered && t >= ALIEN_APPEAR_TIME_2) { event2Triggered = true; currentAlien = alien1; }
        if (!event3Triggered && t >= ALIEN_APPEAR_TIME_3) { event3Triggered = true; currentAlien = alien1; }
        if (!event4Triggered && t >= ALIEN_APPEAR_TIME_4) { event4Triggered = true; currentAlien = alien1; }
        if (!event5Triggered && t >= ALIEN_APPEAR_TIME_5) { event5Triggered = true; currentAlien = alien1; }
        if (!event6Triggered && t >= ALIEN_APPEAR_TIME_6) { event6Triggered = true; currentAlien = alien1; }
        if (!event7Triggered && t >= ALIEN_APPEAR_TIME_7) { event7Triggered = true; currentAlien = alien1; }
        if (!event8Triggered && t >= ALIEN_APPEAR_TIME_8) { event8Triggered = true; currentAlien = alien1; }
    }

    @Override
    protected boolean isTimeInputBlocked() {
        // ‼️ 입력 차단 로직 제거 요청에 따라 항상 false 반환
        return false;
    }
}