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

    private Image stage2Banner;      // 25초에 띄울 이미지
    private boolean bannerVisible = false;
    private int bannerHideAtMs = 0;
    private boolean bannerShown = false; // 한 번만 띄우기

    // 현재 보여줄 이미지
    private Image currentUser;

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
    private final int EVENT_TIME_1 = 27 * 1000;   // 0:27
    private final int EVENT_TIME_2 = 29 * 1000;   // 0:29
    private final double EVENT_TIME_3 = (double) (30.5 * 1000);   // 0:30.5
    private final int EVENT_TIME_4 = 32 * 1000;   // 0:32
    private final int EVENT_TIME_5 = 34 * 1000;   // 0:34
    private final int EVENT_TIME_6 = 36 * 1000;   // 0:36
    private final int EVENT_TIME_7 = 39 * 1000;   // 0:39
    private final int EVENT_TIME_8 = 47 * 1000;   // 0:47


    // ✅ [새로 추가] 스페이스바가 자동으로 눌릴 시간 (ms)을 박자에 맞춰 정의합니다.
// 예: 1초, 2.5초, 4초, 5.5초, 6.5초...
    private final int[] RHYTHM_TIMES = {
            28285, 28505, 28725,
            31280, 31720,
            35146, 35366, 35576,
            35577,
            41793, 42002, 43282, 43502, 43722, 43942, 45435, 45859, 46283
            // ... 음악 박자에 맞춰 더 추가합니다.
    };

    // ‼️ [수정] 스페이스바 입력을 막을 시간 구간 정의 (총 4개 예시)
    // {시작 시간(ms), 끝 시간(ms)} 쌍으로 묶인 배열
    private final int[][] BLOCK_TIMES = {
            {28000, 28900},  // 예시 1: 28초부터 28.9초까지
            {31200, 31800}, // 예시 2: 31.2초부터 31.8초까지
            {35000, 35600}, // 예시 3: 35초부터 35.6초까지
            {41600, 46500}  // 예시 4: 41.6초부터 46.5초까지
    };


    public SpaceStage2() {
        // 이미지 로드
        alien1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alienHand01.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alienHand02.png")).getImage();
        cat1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand01.png")).getImage();
        cat2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand02.png")).getImage();

        cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01.png")).getImage();

        // 초기 상태 이미지
        currentUser = alien1;

        // 25초에 띄울 배너 이미지
        stage2Banner = new ImageIcon(Main.class.getResource("../images/alienStage_image/space_stage2.png")).getImage();


        // ✅ 부모 클래스의 자동 입력 타이밍 배열 설정
        this.autoPressTimes = RHYTHM_TIMES;
    }

    /**
     * 음악 시간에 따라 이미지 변경
     */
    @Override
    public void updateByMusicTime(int t) {

        // ✅ 부모 클래스의 음악 진행 바 로직을 먼저 실행
        super.updateByMusicTime(t);

        // 25초에 한 번만 켜기 (표시 시간은 1.5초 예시)
        if (!bannerShown && t >= 25_000) {
            bannerShown = true;
            bannerVisible = true;
            bannerHideAtMs = t + 1500; // 1.5초 뒤 자동 숨김
            repaint();
        }

        // 자동 숨김
        if (bannerVisible && t >= bannerHideAtMs) {
            bannerVisible = false;
            repaint();
        }
    }

    /**
     * 실제 화면에 그려질 내용
     */
    @Override
    public void drawStageObjects(Graphics g) {

        g.drawImage(currentUser, 0, 0, null);

        // 배너 오버레이 (맨 위)
        if (bannerVisible && stage2Banner != null) {
            Graphics2D g2 = (Graphics2D) g.create();

            // 원하는 크기 (픽셀 단위)
            int targetWidth = 300;   // 폭
            int targetHeight = 250;  // 높이

            // 화면 중앙 정렬
            int x = (getWidth() - targetWidth) / 2;
            int y = 50; // 위에서 조금 아래쪽

            // 고화질 렌더링 (픽셀 깨짐 방지)
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // 이미지 그리기
            g2.drawImage(stage2Banner, x, y, targetWidth, targetHeight, null);
            g2.dispose();
        }
    }

    // ✅ SpaceAnimation의 getCannon() 메서드를 오버라이드
    @Override
    public Image getCannon() {
        return cannon; //
    }

    @Override
    protected void changeStageImageOnPress() {
        // 현재 타겟이 고양이 손일 경우에만 고양이 눌림 모션(cat2)으로 변경
        if (currentUser == cat1) {
            this.currentUser = cat2; // 사용자 입력 (고양이 눌림)
        }
        // 자동 입력(isAutoPlaying=true) 시에는 외계인 손(alien1)이므로, 외계인 눌림 모션으로 변경
        else if (currentUser == alien1) {
            this.currentUser = alien2; // 자동 입력 (외계인 눌림)
        }
    }

    @Override
    protected void changeStageImageOnRelease() {
        // 눌린 모션이었을 경우에만 기본 모션으로 복구
        if (currentUser == cat2) {
            this.currentUser = cat1; // 사용자 입력 (고양이 기본)
        }
        // 현재 눌림 모션이 외계인 손일 경우에만 외계인 기본 모션(alien1)으로 복구
        else if (currentUser == alien2) {
            this.currentUser = alien1; // 자동 입력 (외계인 기본)
        }
    }

    @Override
    protected void processStageEvents(int t) {
        // 스페이스바 입력과는 별개로, 시간에 따라 게임의 타겟(currentUser)을 전환합니다.

        if (!event1Triggered && t >= EVENT_TIME_1) {
            event1Triggered = true;
            this.currentUser = alien1;
        }

        if (!event2Triggered && t >= EVENT_TIME_2) {
            event2Triggered = true;
            this.currentUser = cat1;
        }

        if (!event3Triggered && t >= EVENT_TIME_3) {
            event3Triggered = true;
            this.currentUser = alien1;
        }

        if (!event4Triggered && t >= EVENT_TIME_4) {
            event4Triggered = true; // ✅ 수정
            this.currentUser = cat1;
        }

        if (!event5Triggered && t >= EVENT_TIME_5) {
            event5Triggered = true; // ✅ 수정
            this.currentUser = alien1;
        }

        if (!event6Triggered && t >= EVENT_TIME_6) {
            event6Triggered = true; // ✅ 수정
            this.currentUser = cat1;
        }

        if (!event7Triggered && t >= EVENT_TIME_7) {
            event7Triggered = true; // ✅ 수정
            this.currentUser = alien1;
        }

        if (!event8Triggered && t >= EVENT_TIME_8) {
            event8Triggered = true; // ✅ 수정
            this.currentUser = cat1;
        }
    }
    /**
     * ✅ [구현] 현재 음악 시간에 따라 스페이스바 입력을 막을지 결정합니다.
     */
    @Override
    protected boolean isTimeInputBlocked() {
        int t = this.currentMusicTimeMs;

        // 모든 차단 구간을 순회합니다.
        for (int[] block : BLOCK_TIMES) {
            int blockStart = block[0];
            int blockEnd = block[1];

            // 현재 시간(t)이 구간 시작(포함)과 끝(미포함) 사이에 있는지 확인
            if (t >= blockStart && t < blockEnd) {
                return true;
            }
        }
        return false;

    }

}