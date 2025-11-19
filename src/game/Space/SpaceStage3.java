package game.Space;

import game.Main;
import game.rhythm.RhythmJudgementManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.ArrayList;

public class SpaceStage3 extends SpaceAnimation {

    // 이미지
    private Image alien1;
    private Image alien2;
    private Image cat1;
    private Image cat2;
    private Image cannon;
    
    private Image stage3Banner;      // 53초에 띄울 이미지
    private boolean bannerVisible = false;
    private int bannerHideAtMs = 0;
    private boolean bannerShown = false; // 한 번만 띄우기
    
    public static final int MAT_SPEED = 5;	// 재료 떨어지는 속도
    public static final int SLEEP_TIME = 10;
    
    ArrayList<Material> matList = new ArrayList<Material>();
    
    // ‼️ 기존: 현재 보여줄 이미지 (cat1으로 고정)
    private Image currentUser;

    // ✅ [추가] 외계인 손 현재 이미지
    private Image currentAlien;

    /* ✅ [추가] 물총 애니메이션 관련 변수 -> 레이저 애니메이션으로 교체 예정
    private Image currentWaterImage = null;
    private Timer waterAnimationTimer;
    private int waterFrameIndex = 0;
    private final int WATER_ANIMATION_DELAY = 50; // 물총 이미지 전환 속도 (ms)
    */
    
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
    private final int ALIEN_APPEAR_TIME_1 = 55 * 1000; // 0:55
    private final int ALIEN_APPEAR_TIME_2 = (int) (56.3 * 1000); // 0:56.3
    private final int ALIEN_APPEAR_TIME_3 = (int) (58.5 * 1000); // 0:58.5
    private final int ALIEN_APPEAR_TIME_4 = (int) (61.5 * 1000); // 1:01.5
    private final int ALIEN_APPEAR_TIME_5 = 69 * 1000; // 1:09
    private final int ALIEN_APPEAR_TIME_6 = 72 * 1000;  // 1:12
    private final int ALIEN_APPEAR_TIME_7 = (int) (75.5 * 1000); // 1:15.5
    private final int ALIEN_APPEAR_TIME_8 = (int) (78.5 * 1000); // 1:18.5
    // 음원 버전에 따라 전환 타이밍 및 각종 타이밍 변경
    
    
    // ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경
    private static final int[] ALIEN_PRESS_TIMES_INT = {
            // 외계인 손을 움직이는 타이밍은 여기 입력
    		55723, 55938, 56153,
    		59129, 59350, 59571, 60845, 61299,
    		69432, 69647, 69856, 70072, 
    		70281, 70496, 70706, 70921, 
    		71136, 71351, 71561, 71776,
    		76715
    };

    // ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경 (판정 정답 타이밍)
    private static final int[] USER_PRESS_TIMES_INT = {
            // 57초 딴딴딴 (56.563, 56.778, 56.994)
            56563, 56778, 56994,
            // 1분 1초 딴딴딴 딴 딴	(1m 02.554, 1m 02.775, 1m 02.996, 1m 04.270, 1m 04.724)
            62554, 62775, 62996, 64270, 64724,
            // 1분 12초 딴""	(1m 12.849, 1m 13.064, 1m 13.273, 1m 13.489,/ 1m 13.698, 1m 13.913, 1m 14.123, 1m 14.338, 1m 14.553, 1m 14.768, 1m 14.978, 1m 15.193)
            72849, 73064, 73273, 73489, 73698, 73913, 74123, 74338, 74553, 74768, 74978, 75193,
            // 1분 20초 딴	(1m 20.147)
            80147,
    }; // 우주쓰레기 타이밍은 따로 구현, 슬로우 구간에 따른 타이밍 변환 구현 예정

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

    public SpaceStage3() {
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
        
        // 이미지 교체 예정
        stage3Banner = new ImageIcon(Main.class.getResource("../images/alienStage_image/space_stage3.png")).getImage();
        
        // ‼️ currentUser는 cat1으로 고정 (사용자가 SpaceBar 누를 때만 cat2로 변경)
        currentUser = cat1;
        // ‼️ 외계인 손은 초기엔 alien1 또는 null로 설정 (화면에 표시 여부는 processStageEvents에서 제어)
        currentAlien = null; // 초기에는 보이지 않도록 null로 설정
        /* 레이저로 수정예정
        // ✅ [추가] 물총 애니메이션 타이머 설정
        setupWaterAnimationTimer();*/
        dropMats();
    }
    
    /* 레이저로 수정 예정
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
    }*/


    @Override
    public void updateByMusicTime(int t) {
        super.updateByMusicTime(t); // SpaceAnimation의 점수 업데이트 및 기본 로직 호출
        
        // 53초에 한 번만 켜기 (표시 시간은 1.5초 예시)
        if (!bannerShown && t >= 53500) {
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
        
        // ✅ 외계인 손 자동 동작 타이밍 확인 (ALIEN_PRESS_TIMES_INT 사용)
        for (int pressTime : ALIEN_PRESS_TIMES_INT) {
            if (t >= pressTime && t < pressTime + 50) {	// 50ms동안 가이드 동작
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
    	// System.out.println("✅ drawStageObjects 호출됨!");
        // ‼️ 고양이 손은 현재 위치 그대로 그립니다.
        g.drawImage(currentUser, 0, 0, null);
        
     // 배너 오버레이 (맨 위)
        if (bannerVisible && stage3Banner != null) {
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
            g2.drawImage(stage3Banner, x, y, targetWidth, targetHeight, null);
            g2.dispose();
        }
    
        
        // ✅ 외계인 손을 왼쪽 y축 중간에 작게 그립니다.
        if (currentAlien != null) {
            g.drawImage(currentAlien, 0, 0, getWidth(), getHeight(), null);
        }
        /*레이저로 변경 예정
        // ✅ [추가] 물총 그리기 (Stage1에만 적용)
        if (currentWaterImage != null) {
            // 물총 이미지 크기 및 위치 조정 (화면 전체에 맞춤)
            int w = getWidth();
            int h = getHeight();
            g.drawImage(currentWaterImage, 25, -190, w, h, this);
        }
        */
        
        if (matList != null) {
            // System.out.println("현재 matList 크기: " + matList.size());
        } else {
            System.out.println("⚠️ 오류: matList가 null입니다.");
        }
        
        for(int i = 0; i < matList.size(); i++) {
        	Material mat = matList.get(i);
        	mat.screenDraw(g);
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
    
    public void dropMats() {
    	Material chili = new Material(200, 200, "chili");
    	chili.start();
    	matList.add(chili);
    }
}

class Material extends Thread{
	private Image chiliImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/chili01.png")).getImage();
	private Image eggImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/egg.png")).getImage();
	private Image mushroomImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/mushroom01.png")).getImage();
	private Image welshonion1Image = new ImageIcon(Main.class.getResource("../images/alienStage_image/welshonion01.png")).getImage();
	private Image welshonion2Image = new ImageIcon(Main.class.getResource("../images/alienStage_image/welshonion02.png")).getImage();
	private Image soupImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/soup01.png")).getImage();
	
	private Image slicedChiliImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/chili02.png")).getImage();
	private Image FriedEggImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/egg.png")).getImage();
	private Image slicedMushroomImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/mushroom02.png")).getImage();
	private Image slicedWelshonion1Image = new ImageIcon(Main.class.getResource("../images/alienStage_image/welshonion03.png")).getImage();
	private Image slicedWelshonion2Image = new ImageIcon(Main.class.getResource("../images/alienStage_image/welshonion04.png")).getImage();
	private Image slicedSoupImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/soup02.png")).getImage();
	
	private int x, y;
	private String matType; // 어떤 재료인지
	
	public Material(int x, int y, String matType) {
		this.x = x;
		this.y = y;
		this.matType = matType;
	}

	public void screenDraw(Graphics g) {
		switch(matType) {
			case "chili":
				g.drawImage(chiliImage, x, y, null);
				break;
			case "egg":
				g.drawImage(eggImage, x, y, null);
				break;
			case "mushroom":
				g.drawImage(mushroomImage, x, y, null);
				break;
			case "welshonion1":
				g.drawImage(welshonion1Image, x, y, null);
				break;
			case "welshonion2":
				g.drawImage(welshonion2Image, x, y, null);
				break;
			case "soup":
				g.drawImage(soupImage, x, y, null);
				break;
		}
	}
	
	public void drop() {
		x += SpaceStage3.MAT_SPEED;
		y += SpaceStage3.MAT_SPEED;
		//System.out.println("Note Position: (" + x + ", " + y + ")");
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				drop();
				Thread.sleep(SpaceStage3.SLEEP_TIME);
			}
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
}
