package game.Space;

import game.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

    // ✅ [추가] 물총 애니메이션 관련 변수
    private Image currentWaterImage = null;
    private Timer waterAnimationTimer;
    private int waterFrameIndex = 0;
    private final int WATER_ANIMATION_DELAY = 50; // 물총 이미지 전환 속도 (ms)

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

    // ✅ 외계인 손 이미지 변경 타이밍 (alien1 -> alien2 -> alien1)
    private final int[] ALIEN_PRESS_TIMES = {
            3000, 6853, 7117, 7511, 7709,
            14131, 16283, 16493, 16714, 20984,
            21233, 21863
    };

    // ✅ 외계인 손이 alien2로 바뀐 후 돌아오는 타이밍
    private final int ALIEN_RELEASE_DELAY_MS = 50;
    private final int[] ALIEN_RELEASE_TIMES = new int[ALIEN_PRESS_TIMES.length];


    public SpaceStage1() {
        // 부모 클래스에서 waterFrames 포함 모든 초기화 진행
        super();

        // 이미지 로드
        alien1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/hologram_alien1.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/hologram_alien2.png")).getImage();
        cat1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand01.png")).getImage();
        cat2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand02.png")).getImage();

        cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon02.png")).getImage();

        // ‼️ currentUser는 cat1으로 고정 (사용자가 SpaceBar 누를 때만 cat2로 변경)
        currentUser = cat1;
        // ‼️ 외계인 손은 초기엔 alien1 또는 null로 설정 (화면에 표시 여부는 processStageEvents에서 제어)
        currentAlien = null; // 초기에는 보이지 않도록 null로 설정

        // ✅ 외계인 손 이미지 전환 해제 타이밍 계산
        for (int i = 0; i < ALIEN_PRESS_TIMES.length; i++) {
            ALIEN_RELEASE_TIMES[i] = ALIEN_PRESS_TIMES[i] + ALIEN_RELEASE_DELAY_MS;
        }

        // ✅ [추가] 물총 애니메이션 타이머 설정
        setupWaterAnimationTimer();
    }

    // ✅ [추가] 물총 애니메이션 타이머 설정 메서드 (SpaceAnimation에서 이동)
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

    // ✅ [추가] 물총 애니메이션 시작 메서드 (SpaceAnimation의 빈 메서드 오버라이드)
    // ‼️ [수정] 이 메서드를 오버라이드하려고 하면 오류가 발생할 수 있으므로,
    //           로직은 그대로 유지하되, @Override를 제거합니다.
    //           하지만 더 안전하게는 이 메서드를 processSpaceKeyPress()로 대체하여 사용합니다.
    protected void startWaterAnimation() {
        if (waterAnimationTimer.isRunning()) {
            waterAnimationTimer.stop(); // 중복 방지 및 리셋
        }
        waterFrameIndex = 0;
        currentWaterImage = waterFrames[waterFrameIndex];
        waterAnimationTimer.start();
        repaint();
    }

    // ✅ [오버라이드] SpaceBar 입력 시 물총 애니메이션 실행 추가
//    @Override
//    public void keyPressed(KeyEvent e) {
//        super.keyPressed(e);
//        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
//            startWaterAnimation();
//        }
//    }

    // ✅ [오버라이드] SpaceAnimation에서 추출한 메서드를 오버라이드하여 물총 애니메이션 실행
    @Override
    protected void processSpaceKeyPress() {
        startWaterAnimation(); // SpaceStage1의 물총 애니메이션 시작 메서드 호출
    }


    @Override
    public void updateByMusicTime(int t) {
        super.updateByMusicTime(t);

        // ✅ 외계인 손 자동 동작 타이밍 확인
        for (int pressTime : ALIEN_PRESS_TIMES) {
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

        // ✅ 외계인 손을 왼쪽 y축 중간에 작게 그립니다.
        if (currentAlien != null) {
//            int alienWidth = (int) (alien1.getWidth(null) * 0.4); // 적절한 크기로 조정 (예: 40%)
//            int alienHeight = (int) (alien1.getHeight(null) * 0.4);
//            int alienX = 50; // 왼쪽 여백
//            int alienY = getHeight() / 2 - alienHeight / 2; // Y축 중간

            g.drawImage(currentAlien, 0, 0, getWidth(), getHeight(), null);
        }

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
        if (t < ALIEN_APPEAR_TIME_1 && currentAlien != null) { currentAlien = null; }

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