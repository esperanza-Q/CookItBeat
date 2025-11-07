package game.Space;

import game.Main;

import javax.swing.*;
import java.awt.*;

public class SpaceStage2 extends SpaceAnimation {

    // 이미지
    private Image alien1;
    private Image alien2;
    private Image cat1;
    private Image cat2;
    private Image cannon;

    // 현재 보여줄 이미지
    private Image currentUser;

    // 이벤트 발동 여부
    private boolean event1Triggered = false;
    private boolean event2Triggered = false;
    private boolean event3Triggered = false;

    // 전환 타이밍 (ms 기준)
//    private final int EVENT_TIME_1 = 2 * 1000;   // 0:02
//    private final int EVENT_TIME_2 = 4 * 1000;   // 0:04
//    private final int EVENT_TIME_3 = 8 * 1000;   // 0:09
//    private final int EVENT_TIME_4 = 12 * 1000;   // 0:12

    public SpaceStage2() {
        // 이미지 로드
        alien1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alienHand01.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alienHand02.png")).getImage();
        cat1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand01.png")).getImage();
        cat2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand02.png")).getImage();

        cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01.png")).getImage();

        // 초기 상태 이미지
        currentUser = alien1;
    }

    /**
     * 음악 시간에 따라 이미지 변경
     */
    @Override
    public void updateByMusicTime(int t) {

        // ✅ 부모 클래스의 음악 진행 바 로직을 먼저 실행
        super.updateByMusicTime(t);

//        if (!event1Triggered && t >= EVENT_TIME_1) {
//            event1Triggered = true;
//            currentUser = alien1;
//        }
//
//        if (!event2Triggered && t >= EVENT_TIME_2) {
//            event2Triggered = true;
//            currentUser = alien2;
//        }
//
//        if (!event3Triggered && t >= EVENT_TIME_3) {
//            event3Triggered = true;
//            currentUser = cat1;
//        }
//
//        if (!event3Triggered && t >= EVENT_TIME_4) {
//            event3Triggered = true;
//            currentUser = cat2;
//        }
    }

    /**
     * 실제 화면에 그려질 내용
     */
    @Override
    public void drawStageObjects(Graphics g) {

//        super.paintComponent(g);

        // 공통 배경 그리기
//        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        g.drawImage(currentUser, 0, 0, null);
//        g.drawImage(cannon, 0, 0, null);
    }

    // ✅ SpaceAnimation의 getCannon() 메서드를 오버라이드
    @Override
    public Image getCannon() {
        return cannon; // cannon01.png를 가리킵니다.
    }
}