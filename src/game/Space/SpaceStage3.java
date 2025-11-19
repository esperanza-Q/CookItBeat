package game.Space;

import game.Main;
import game.rhythm.RhythmJudgementManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

/*
 * 재료 떨어지는 로직 흐름
 * 게임 실행 -> y -100에 재료 생성 -> 특정 시간이 지나면 각 재료들이 출발 -> 판정 타이밍에 맞춰 y 100에 도착
 * 
 * (Stage3 생성자 내부) gameTimer -> 출발 시간이 되면 10ms마다 matList안의 재료들 drop()시킴
 * (Stage3 생성자 내부) dropMats(...) -> 재료 객체 생성 후 matList에 저장
 * (dropMats() 내부) calculateInitialAndTime(...) -> 출발 시간 및 x좌표 계산
 * drawStageObjects() -> 16ms마다 화면에 재료 그림
 * 
 * 
 */
public class SpaceStage3 extends SpaceAnimation {

	// 이미지
	private Image alien1;
	private Image alien2;
	private Image cat1;
	private Image cat2;
	private Image cannon;

	private Image stage3Banner; // 53초에 띄울 이미지
	private boolean bannerVisible = false;
	private int bannerHideAtMs = 0;
	private boolean bannerShown = false; // 한 번만 띄우기

	public static final int SLEEP_TIME = 10;
	private final int FIXED_START_Y = -300; // 모든 재료의 초기 Y 좌표 (화면 밖)
	private final int JUDGEMENT_TARGET_Y = 100; // 판정선 Y 좌표

	ArrayList<Material> matList = new ArrayList<Material>();

	private Timer gameTimer;

	// ✅ 현재 진행 시간(게임 시작 후 지난 시간)
	public static int progressTime;

	// ‼️ 기존: 현재 보여줄 이미지 (cat1으로 고정)
	private Image currentUser;

	// ✅ [추가] 외계인 손 현재 이미지
	private Image currentAlien;

	
	// ✅ [추가] 레이저 애니메이션 관련 변수
	public static Image currentLaserImage = null;
	private Timer laserAnimationTimer;
	private int laserFrameIndex = 0;
	private final int LASER_ANIMATION_DELAY = 50; // 레이저 이미지 전환 속도 (ms)
	

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
	private final int ALIEN_APPEAR_TIME_6 = 72 * 1000; // 1:12
	private final int ALIEN_APPEAR_TIME_7 = (int) (75.5 * 1000); // 1:15.5
	private final int ALIEN_APPEAR_TIME_8 = (int) (78.5 * 1000); // 1:18.5
	// 음원 버전에 따라 전환 타이밍 및 각종 타이밍 변경

	// 재료 배열 (파, 고추, 버섯)
	private String[] materialNames = { "chili", "mushroom", "welshonion1", "welshonion2" };

	// ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경
	private static final int[] ALIEN_PRESS_TIMES_INT = {
			// 외계인 손을 움직이는 타이밍은 여기 입력
			55723, 55938, 56153, 59129, 59350, 59571, 60845, 61299, 69432, 69647, 69856, 70072, 70281, 70496, 70706,
			70921, 71136, 71351, 71561, 71776, 76715 };

	// ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경 (판정 정답 타이밍)
	private static final int[] USER_PRESS_TIMES_INT = {
			// 57초 딴딴딴 (56.563, 56.778, 56.994)
			56563, 56778, 56994,
			// 1분 1초 딴딴딴 딴 딴 (1m 02.554, 1m 02.775, 1m 02.996, 1m 04.270, 1m 04.724)
			62554, 62775, 62996, 64270, 64724,
			// 1분 12초 딴"" (1m 12.849, 1m 13.064, 1m 13.273, 1m 13.489,/ 1m 13.698, 1m
			// 13.913, 1m 14.123, 1m 14.338, 1m 14.553, 1m 14.768, 1m 14.978, 1m 15.193)
			72849, 73064, 73273, 73489, 73698, 73913, 74123, 74338, 74553, 74768, 74978, 75193,
			// 1분 20초 딴 (1m 20.147)
			80147, }; // 우주쓰레기 타이밍은 따로 구현, 슬로우 구간에 따른 타이밍 변환 구현 예정

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

		Random random = new Random();
		// 이미지 교체 예정
		stage3Banner = new ImageIcon(Main.class.getResource("../images/alienStage_image/space_stage2.png")).getImage();

		// ‼️ currentUser는 cat1으로 고정 (사용자가 SpaceBar 누를 때만 cat2로 변경)
		currentUser = cat1;
		// ‼️ 외계인 손은 초기엔 alien1 또는 null로 설정 (화면에 표시 여부는 processStageEvents에서 제어)
		currentAlien = null; // 초기에는 보이지 않도록 null로 설정
		
		// ✅ [추가] 물총 애니메이션 타이머 설정
		setupLaserAnimationTimer();
		
		
		// ✅ [추가] 스테이지3 이벤트 처리
        addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        	    int clickX = e.getX();
        	    int clickY = e.getY();
        	    
        	    int materialIndex = -1;
        	    
        	    // 충돌 판정 루프
        	    for (int i = 0; i < matList.size(); i++) {
        	    	Material mat = matList.get(i);
        	    	
        	        if (mat.getBounds().contains(clickX, clickY)) {
        	            
        	        	materialIndex = i; // ⭐️ 클릭된 재료의 인덱스 저장
                        
                        // 1. 레이저 이미지 설정 요청 (인덱스 기반)
                        updateLaserFramesByMaterialIndex(materialIndex);
                        
        	            // ⭐️ 1. 타이머 시작 요청
        	            startLaserAnimation(); 
        	            
        	            // ⭐️ 2. 이미지 회전 방향 설정 요청
        	            //mat.setTargetDirection(clickX, clickY);
        	            
        	            // 한 번에 하나만 처리
        	            break;
        	        }
        	    }
        	    repaint();
        	}
        });
        
		// 1. 10ms 간격으로 타이머 설정
		gameTimer = new Timer(SLEEP_TIME, e -> {
			// 2. 타이머 틱마다 모든 재료의 좌표를 업데이트
			updateMaterialPositions();
		});

		gameTimer.start();

		// 정답타이밍, 재료타입, x속도, y속도, x도착좌표, y도착좌표
		dropMats(56563, materialNames[random.nextInt(3)], 3, 4, 400);
		dropMats(56778, materialNames[random.nextInt(3)], 0, 4, 530);
		dropMats(56994, materialNames[random.nextInt(3)], -3, 4, 700);

		dropMats(62554, materialNames[random.nextInt(3)], -3, 4, 700);
		dropMats(62775, materialNames[random.nextInt(3)], 0, 4, 530);
		dropMats(62996, materialNames[random.nextInt(3)], 3, 4, 400);
		dropMats(64270, materialNames[random.nextInt(3)], 1, 4, 430);
		dropMats(64724, materialNames[random.nextInt(3)], -1, 4, 630);

		dropMats(72849, "soup", 0, 4, 530);

		dropMats(80147, "egg", 0, 4, 530);

		// 타이머 시작
		gameTimer.start();

	}
	
	protected void updateLaserFramesByMaterialIndex(int materialIndex) {
	    // 인덱스 그룹 A: 1, 4, 8, 9 -> laser01, laser02 사용
	    Integer[] groupA = {1, 4, 8, 9};
	    // 인덱스 그룹 B: 0, 5, 6 -> laser03, laser04 사용
	    Integer[] groupB = {0, 5, 6};
	    Integer[] groupC = {2, 3, 7};

	    String baseFileName;

	    if (Arrays.asList(groupA).contains(materialIndex)) {
	        baseFileName = "laser0"; // 파일명: laser01.png, laser02.png
	    } else if (Arrays.asList(groupB).contains(materialIndex)) {
	        baseFileName = "laser0"; // 파일명: laser03.png, laser04.png (실제 파일명이 laser03, laser04인 경우)
	    } else {
	        // 기본값 또는 다른 그룹 설정 (예: 나머지 인덱스는 laser05, laser06)
	        baseFileName = "laser0"; // 기본값 파일명: laser01.png, laser02.png 사용
	    }
	    
	    // **가정**: 
	    // Group A는 laser01.png, laser02.png 사용
	    // Group B는 laser03.png, laser04.png 사용

	    if (Arrays.asList(groupA).contains(materialIndex)) {
	        laserFrames[0] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser01.png")).getImage();
	        laserFrames[1] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser02.png")).getImage();
	    } else if (Arrays.asList(groupB).contains(materialIndex)) {
	        laserFrames[0] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser03.png")).getImage();
	        laserFrames[1] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser04.png")).getImage();
	    } else if (Arrays.asList(groupC).contains(materialIndex)) {
	        // 나머지 인덱스의 기본값 처리
	        laserFrames[0] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser05.png")).getImage();
	        laserFrames[1] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser06.png")).getImage();
	    }
	}

	// ✅ [추가] 레이저 애니메이션 타이머 설정 메서드
    private void setupLaserAnimationTimer() {
        laserAnimationTimer = new Timer(LASER_ANIMATION_DELAY, e -> {
            laserFrameIndex++;
            if (laserFrameIndex < laserFrames.length) {
                currentLaserImage = laserFrames[laserFrameIndex];
            } else {
                // 애니메이션 종료 후 이미지 null로 설정
                laserAnimationTimer.stop();
                currentLaserImage = null;
            }
            repaint();
        });
        laserAnimationTimer.setRepeats(true);
    }
    
    // ✅ 레이저 애니메이션 시작 메서드
    protected void startLaserAnimation() {
        if (laserAnimationTimer.isRunning()) {
            laserAnimationTimer.stop(); // 중복 방지 및 리셋
        }
        laserFrameIndex = 0;
        currentLaserImage = laserFrames[laserFrameIndex];
        laserAnimationTimer.start();
        repaint();
    }


	@Override
	public void updateByMusicTime(int t) {
		super.updateByMusicTime(t); // SpaceAnimation의 점수 업데이트 및 기본 로직 호출

		this.progressTime = t;

		// 53.5초에 한 번만 켜기 (표시 시간은 1.5초 예시)
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
			if (t >= pressTime && t < pressTime + 50) { // 50ms동안 가이드 동작
				if (currentAlien == alien1)
					currentAlien = alien2;
				break;
			}
		}

		for (int releaseTime : ALIEN_RELEASE_TIMES) {
			if (t >= releaseTime && t < releaseTime + 50) {
				if (currentAlien == alien2)
					currentAlien = alien1;
				break;
			}
		}
	}

	@Override
	public void drawStageObjects(Graphics g) {
		// ‼️ 고양이 손은 현재 위치 그대로 그립니다.
		g.drawImage(currentUser, 0, 0, null);

		// 배너 오버레이 (맨 위)
		if (bannerVisible && stage3Banner != null) {
			Graphics2D g2 = (Graphics2D) g.create();

			// 원하는 크기 (픽셀 단위)
			int targetWidth = 300; // 폭
			int targetHeight = 250; // 높이

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
		/*
		 * 레이저로 변경 예정 // ✅ [추가] 물총 그리기 (Stage1에만 적용) if (currentWaterImage != null) { //
		 * 물총 이미지 크기 및 위치 조정 (화면 전체에 맞춤) int w = getWidth(); int h = getHeight();
		 * g.drawImage(currentWaterImage, 25, -190, w, h, this); }
		 */

		for (int i = 0; i < matList.size(); i++) {
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
		if (currentUser == cat1)
			this.currentUser = cat2;
	}

	@Override
	protected void changeStageImageOnRelease() {
		// ‼️ currentUser가 cat2일 때만 cat1으로 변경
		if (currentUser == cat2)
			this.currentUser = cat1;
	}

	@Override
	protected void processStageEvents(int t) {
		// ‼️ 이벤트 타이밍에 따라 currentAlien (외계인 손)의 보이기/숨기기 및 이미지를 제어합니다.

		// 1. 초기화 (초기 상태)
		if (t < ALIEN_APPEAR_TIME_1 && currentAlien != null) {
			currentAlien = null;
		}

		// 2. 외계인 손 등장 및 이미지 변경 로직
		// 외계인 손이 등장하는 시점에 alien1로 설정
		if (!event1Triggered && t >= ALIEN_APPEAR_TIME_1) {
			event1Triggered = true;
			currentAlien = alien1;
		}
		if (!event2Triggered && t >= ALIEN_APPEAR_TIME_2) {
			event2Triggered = true;
			currentAlien = alien1;
		}
		if (!event3Triggered && t >= ALIEN_APPEAR_TIME_3) {
			event3Triggered = true;
			currentAlien = alien1;
		}
		if (!event4Triggered && t >= ALIEN_APPEAR_TIME_4) {
			event4Triggered = true;
			currentAlien = alien1;
		}
		if (!event5Triggered && t >= ALIEN_APPEAR_TIME_5) {
			event5Triggered = true;
			currentAlien = alien1;
		}
		if (!event6Triggered && t >= ALIEN_APPEAR_TIME_6) {
			event6Triggered = true;
			currentAlien = alien1;
		}
		if (!event7Triggered && t >= ALIEN_APPEAR_TIME_7) {
			event7Triggered = true;
			currentAlien = alien1;
		}
		if (!event8Triggered && t >= ALIEN_APPEAR_TIME_8) {
			event8Triggered = true;
			currentAlien = alien1;
		}
	}

	@Override
	protected boolean isTimeInputBlocked() {
		// ‼️ 입력 차단 로직 제거 요청에 따라 항상 false 반환
		return false;
	}
	
	
	// answerTimeMs : 정답 타이밍
	public void dropMats(long answerTimeMs, String matType, int speedX, int speedY, int destX) {

		// 1. 초기 좌표와 출발 시간 계산
		long[] posAndTime = calculateInitialAndTime(answerTimeMs, speedX, speedY, destX);
		int startX = (int) posAndTime[0];
		long dropStartTime = posAndTime[1];

		// 2. Material 객체 생성 (고정 Y 좌표와 계산된 X, 시간 사용)
		Material newMat = new Material(startX, FIXED_START_Y, matType, speedX, speedY, answerTimeMs, dropStartTime);

		// 3. 리스트에 추가
		matList.add(newMat);
	}

	private long[] calculateInitialAndTime(long answerTimeMs, int speedX, int speedY, int destX) {

		// 1. 이동 거리 계산 (Y축)
		double distanceY = JUDGEMENT_TARGET_Y - FIXED_START_Y;

		// 2. Y축 이동에 필요한 틱 수 및 시간 계산
		double totalTicks = distanceY / (double) speedY;
		long travelTimeMs = (long) (totalTicks * SLEEP_TIME);

		// 3. X축 이동 거리 계산 (도착 시간을 맞추기 위해 Y축 시간과 동일하게 사용)
		int distanceX = (int) (speedX * totalTicks);

		// 4. 초기 X 좌표 계산 (Initial = Center - Distance)
		// 재료가 중앙에 도착하도록 X좌표 역산
		int initialX = destX - distanceX;

		// 5. 드롭 시작 시간 계산 (Start = Answer Time - Travel Time)
		long dropStartTime = answerTimeMs - travelTimeMs;

		return new long[] { initialX, dropStartTime };
	}

	private void updateMaterialPositions() {
		// 1. Iterator를 사용하여 matList를 순회
		Iterator<Material> iterator = matList.iterator();

		// 2. 재료를 확인하며 움직임 및 제거 로직 실행
		while (iterator.hasNext()) {
			Material mat = iterator.next();

			// --- [기존 로직: 재료 이동] ---
			if (progressTime >= mat.actualDropStartTime) {
				mat.drop();
			}

			// --- [추가 로직: 화면 이탈 확인 및 제거] ---

			// ⭐️ 재료가 화면 밖(Y축 기준)으로 완전히 벗어났는지 확인
			final int SCREEN_HEIGHT = this.getHeight(); // 패널의 현재 높이를 가져옴
			final int MATERIAL_HEIGHT = 300; // 재료 이미지의 높이 (실제 값으로 대체 필요)

			// 재료의 Y 좌표가 화면 하단 + 재료 높이보다 커지면 제거
			if (mat.getY() > SCREEN_HEIGHT + MATERIAL_HEIGHT) {

				// ⭐️ 판정에 성공하지 못하고 화면을 벗어난 경우의 패널티 로직 (필요하다면 추가)

				// ⭐️ Iterator의 remove() 메서드를 사용하여 안전하게 제거
				iterator.remove();
			}
		}
	}

}

//✅ 재료 클래스: 떨어지는 모션 구현 
class Material {
	private Image chiliImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/chili01.png"))
			.getImage();
	private Image eggImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/egg.png")).getImage();
	private Image mushroomImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/mushroom01.png"))
			.getImage();
	private Image welshonion1Image = new ImageIcon(
			Main.class.getResource("../images/alienStage_image/welshonion01.png")).getImage();
	private Image welshonion2Image = new ImageIcon(
			Main.class.getResource("../images/alienStage_image/welshonion02.png")).getImage();
	private Image soupImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/soup01.png")).getImage();

	private Image slicedChiliImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/chili02.png"))
			.getImage();
	private Image FriedEggImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/egg.png"))
			.getImage();
	private Image slicedMushroomImage = new ImageIcon(
			Main.class.getResource("../images/alienStage_image/mushroom02.png")).getImage();
	private Image slicedWelshonion1Image = new ImageIcon(
			Main.class.getResource("../images/alienStage_image/welshonion03.png")).getImage();
	private Image slicedWelshonion2Image = new ImageIcon(
			Main.class.getResource("../images/alienStage_image/welshonion04.png")).getImage();
	private Image slicedSoupImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/soup02.png"))
			.getImage();

	private int x, y; // 생성 위치
	private int width, height;
	public String matType; // 어떤 재료인지
	private int xSpeed, ySpeed;

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	// ⭐️ 목표 도착 시간 (정답 타이밍)
	private long targetArriveTime;

	// ⭐️ 실제 움직임을 시작해야 할 게임 시간 (핵심 필드)
	public long actualDropStartTime;

	public double rotationAngle = 0; // ⭐️ 회전 각도 (라디안 또는 도)

	public Material(int x, int y, String matType, int xSpeed, int ySpeed, long targetArriveTime, long dropStartTime) {
		this.x = x; // 생성 좌표
		this.y = y;
		this.matType = matType;
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		this.targetArriveTime = targetArriveTime;
		this.actualDropStartTime = dropStartTime; // 👈 재료가 움직이기 시작할 시간
	}

	public void screenDraw(Graphics g) {
		switch (matType) {
		case "chili":
			g.drawImage(chiliImage, x, y, 100, 200, null);
			width = 100;
			height = 200;
			break;
		case "egg":
			g.drawImage(eggImage, x, y, 212, 192, null);
			width = 212;
			height = 192;
			break;
		case "mushroom":
			g.drawImage(mushroomImage, x, y, 150, 100, null);
			width = 150;
			height = 100;
			break;
		case "welshonion1":
			g.drawImage(welshonion1Image, x, y, 100, 100, null);
			width = 100;
			height = 100;
			break;
		case "welshonion2":
			g.drawImage(welshonion2Image, x, y, 100, 100, null);
			width = 100;
			height = 100;
			break;
		case "soup":
			g.drawImage(soupImage, x, y, 220, 271, null);
			width = 220;
			height = 271;
			break;
		}

		if (SpaceStage3.currentLaserImage != null) {
			g.drawImage(SpaceStage3.currentLaserImage, 0, 0, null);
			g.dispose();
		}
	}

	public void drop() {
		x += this.xSpeed;
		y += this.ySpeed;
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}

	// ⭐️ 회전 각도 설정 메서드
	public void setTargetDirection(int launcherX, int launcherY) {
		// 이미지가 중앙에서 회전한다고 가정하고, 이미지의 중심 좌표를 계산
		int targetX = x + (width / 2);
		int targetY = y + (height / 2);
		
		// 1. x, y 축 거리(차이) 계산
	    double dx = targetX - launcherX;
	    double dy = targetY - launcherY;

	    // 2. atan2를 사용하여 라디안(Radian) 각도 계산
	    // atan2(dy, dx)는 x, y를 고려하여 -π ~ π 범위의 각도를 정확하게 반환합니다.
	    double angleInRadians = Math.atan2(dy, dx);

	    // 3. 라디안을 도(Degree)로 변환
	    this.rotationAngle = Math.toDegrees(angleInRadians);
	    
	}

}
