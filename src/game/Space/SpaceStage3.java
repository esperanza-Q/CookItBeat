package game.Space;

import game.Main;
import game.Music;
import game.rhythm.RhythmJudgementManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.InputStream;
import java.util.List;
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
    private final int JUDGEMENT_TARGET_Y = 150; // 판정선 Y 좌표

    ArrayList<Material> matList = new ArrayList<Material>();

    private Timer gameTimer;

    // ✅ 현재 진행 시간(게임 시작 후 지난 시간)
    public static int progressTime;

    // ‼️ 기존: 현재 보여줄 이미지 (cat1으로 고정)
    private Image currentUser;

    // ✅ [추가] 외계인 손 현재 이미지
    private Image currentAlien;

    private static int offset = 500;

    private static int click = 0;

    // ✅ [추가] 레이저 애니메이션 관련 변수
    public static Image currentLaserImage = null;
    private Timer laserAnimationTimer;
    private int laserFrameIndex = 0;
    private final int LASER_ANIMATION_DELAY = 50; // 레이저 이미지 전환 속도 (ms)

    // ✅ [추가] 폭발 애니메이션 관련 변수
    private Timer boomAnimationTimer;
    private int boomFrameIndex = 0;
    private Image currentBoomImage; // 현재 폭발 프레임 이미지
    private final int BOOM_ANIMATION_DELAY = 150; // 예시 딜레이 (ms)
    // 공기포 크기 조절 (1.0f = 원본 크기)
    private float boomScale = 1.8f;   // 70% 크기
    private int boomDrawX = -1;
    private int boomDrawY = -1;

    private final double DIFFICULTY_FACTOR = 0.5; // 난이도 조절 계수 (0.5 = 50% 속도)

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
    private final int ALIEN_APPEAR_TIME_1 = toJudgeMs(55 * 1000); // 0:55
    private final int ALIEN_APPEAR_TIME_2 = toJudgeMs((int) (56.3 * 1000)); // 0:56.3
    private final int ALIEN_APPEAR_TIME_3 = toJudgeMs((int) (58.5 * 1000)); // 0:58.5
    private final int ALIEN_APPEAR_TIME_4 = toJudgeMs((int) (61.5 * 1000)); // 1:01.5
    private final int ALIEN_APPEAR_TIME_5 = toJudgeMs(69 * 1000); // 1:09
    private final int ALIEN_APPEAR_TIME_6 = toJudgeMs(72 * 1000); // 1:12
    private final int ALIEN_APPEAR_TIME_7 = toJudgeMs((int) (75.5 * 1000)); // 1:15.5
    private final int ALIEN_APPEAR_TIME_8 = toJudgeMs((int) (78.5 * 1000)); // 1:18.5
    // 음원 버전에 따라 전환 타이밍 및 각종 타이밍 변경

    // 재료 배열 (파, 고추, 버섯)
    private String[] materialNames = {"chili", "mushroom", "welshonion1", "welshonion2"};

    // ✅ [추가] 잔해(Fragments)를 저장할 리스트
    private List<Material> fragmentList = new ArrayList<>();

    // ✅ [추가] 잔해 드롭 속도 상수
    private final double FRAGMENT_SPEED = 4.0; // 잔해가 아래로 떨어지는 기본 속도
    private final double FRAGMENT_SPREAD = 3.0; // 잔해가 좌우로 퍼지는 정도

    // ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경
    private static final int[] ALIEN_PRESS_TIMES_SEC = {
            // 외계인 손을 움직이는 타이밍은 여기 입력
            55723, 55938, 56153, 59129, 59350, 59571, 60845, 61299, 69432, 69647, 69856, 70072, 70281, 70496, 70706,
            70921, 71136, 71351, 71561, 71776, 76715};

    // ‼️ [수정] static으로 선언하여 super() 호출 전에 접근 가능하도록 변경 (판정 정답 타이밍)
    private static final int[] USER_PRESS_TIMES_SEC = {
            // 57초 딴딴딴 (56.563, 56.778, 56.994)
            56563, 56778, 56994,
            // 1분 1초 딴딴딴 딴 딴 (1m 02.554, 1m 02.775, 1m 02.996, 1m 04.270, 1m 04.724)
            62554, 62775, 62996, 64270, 64724,
            // 1분 12초 딴"" (1m 12.849, 1m 13.064, 1m 13.273, 1m 13.489,/ 1m 13.698, 1m
            // 13.913, 1m 14.123, 1m 14.338, 1m 14.553, 1m 14.768, 1m 14.978, 1m 15.193)
            72849, 73064, 73273, 73489, 73698, 73913, 74123, 74338, 74553, 74768, 74978, 75193,
            // 1분 20초 딴 (1m 20.147)
            80147,}; // 우주쓰레기 타이밍은 따로 구현, 슬로우 구간에 따른 타이밍 변환 구현 예정

    // ✅ 실제 판정에 쓰는 ms 배열 (슬로우 보정 적용된 값)
    private static final int[] USER_PRESS_TIMES_INT = buildJudgeTimes(USER_PRESS_TIMES_SEC);
    private static final int[] ALIEN_PRESS_TIMES_INT = buildJudgeTimes(ALIEN_PRESS_TIMES_SEC);

    // ✅ 외계인 손이 alien2로 바뀐 후 돌아오는 타이밍
    private final int ALIEN_RELEASE_DELAY_MS = 50;
    // ‼️ 인스턴스 변수이므로 super() 호출 후 초기화해야 함
    private final int[] ALIEN_RELEASE_TIMES;

    // ✅ [추가] 수프 멈춤/재개 타이밍 상수
    private final int SOUP_STOP_TIME = USER_PRESS_TIMES_INT[8] + offset - 250;   // 72.5초에 정지 조건 활성화
    private final int SOUP_RESUME_TIME = USER_PRESS_TIMES_INT[8] + offset - 250 + 3000; // 75.5초에 재개

    // ✅ [추가] static 헬퍼 메서드: int[]를 long[]으로 변환 (생성자 오류 해결)
    private static long[] convertToLongArray(int[] array) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] + offset;
        }

        result[0] = result[0] + 450 - 100;
        result[1] = result[1] + 450 - 100;
        result[2] = result[2] + 450 - 100;

        result[3] = result[3] - 400;
        result[4] = result[4] - 400;
        result[5] = result[5] - 400;
        result[6] = result[6] - 50 - 400;
        result[7] = result[7] - 50 - 400;
        for (int i = 8; i < 20; i++) {
            result[i] = result[i] - 180;
        }
        result[20] = result[20] - 477;

        return result;
    }

    public SpaceStage3() {
        // 1. super() 호출을 첫 줄로 배치하고, static 헬퍼 메서드를 통해 인자를 준비합니다.
        // ‼️ 판정 타이밍 배열(USER_PRESS_TIMES_INT)을 부모 클래스에 전달합니다.
        super(convertToLongArray(USER_PRESS_TIMES_INT));

        // ‼️ [추가] 마우스 이벤트 수신을 위해 포커스 가능 설정
        this.setFocusable(true);
        this.requestFocusInWindow(); // 윈도우 포커스 요청

        GLOBAL_JUDGEMENT_OFFSET_MS = 0;

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

        cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01_M.png")).getImage();

        Random random = new Random();
        // 이미지 교체 예정
        stage3Banner = new ImageIcon(Main.class.getResource("../images/alienStage_image/space_stage3.png")).getImage();

        // ‼️ currentUser는 cat1으로 고정 (사용자가 SpaceBar 누를 때만 cat2로 변경)
        currentUser = cat1;
        // ‼️ 외계인 손은 초기엔 alien1 또는 null로 설정 (화면에 표시 여부는 processStageEvents에서 제어)
        currentAlien = null; // 초기에는 보이지 않도록 null로 설정

        // ✅ [추가] 레이저 애니메이션 타이머 설정
        setupLaserAnimationTimer();

        setupBoomAnimationTimer();

        // 정답타이밍, 재료타입, x속도, y속도, x도착좌표, y도착좌표
        dropMats(USER_PRESS_TIMES_INT[0] + offset + 450, materialNames[random.nextInt(3)], 2.7 * DIFFICULTY_FACTOR, 3.6 * DIFFICULTY_FACTOR, 400);
        dropMats(USER_PRESS_TIMES_INT[1] + offset + 450, materialNames[random.nextInt(3)], 0, 3.6 * DIFFICULTY_FACTOR, 530);
        dropMats(USER_PRESS_TIMES_INT[2] + offset + 450, materialNames[random.nextInt(3)], -2.7 * DIFFICULTY_FACTOR, 3.6 * DIFFICULTY_FACTOR, 700);

        dropMats(USER_PRESS_TIMES_INT[3] + offset, materialNames[random.nextInt(3)], -2.7 * DIFFICULTY_FACTOR, 3.6 * DIFFICULTY_FACTOR, 700);
        dropMats(USER_PRESS_TIMES_INT[4] + offset, materialNames[random.nextInt(3)], 0, 3.6 * DIFFICULTY_FACTOR, 530);
        dropMats(USER_PRESS_TIMES_INT[5] + offset, materialNames[random.nextInt(3)], 2.7 * DIFFICULTY_FACTOR, 3.6 * DIFFICULTY_FACTOR, 400);
        dropMats(USER_PRESS_TIMES_INT[6] + offset - 50, materialNames[random.nextInt(3)], 0.9 * DIFFICULTY_FACTOR, 3.6 * DIFFICULTY_FACTOR, 430);
        dropMats(USER_PRESS_TIMES_INT[7] + offset - 50, materialNames[random.nextInt(3)], -0.9 * DIFFICULTY_FACTOR, 3.6 * DIFFICULTY_FACTOR, 630);

        dropMats(USER_PRESS_TIMES_INT[8] + offset - 250, "soup", 0, 4, 530);

        dropMats(USER_PRESS_TIMES_INT[20] + offset, "egg", 0, 4 * DIFFICULTY_FACTOR, 530);

        // ✅ [추가] 스테이지3 이벤트 처리
        addMouseListener(new MouseAdapter() {/*
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickX = e.getX();
                int clickY = e.getY();
                System.out.println("마우스 클릭됨");
                int materialIndex = -1;

                // 충돌 판정 루프
                for (int i = 0; i < matList.size(); i++) {
                    Material mat = matList.get(i);

                    System.out.println("Checking: " + mat.matType + " Bounds: " + mat.getBounds());
                    System.out.println("Click: (" + clickX + ", " + clickY + ")");

                    if (mat.getBounds().contains(clickX, clickY)) {
                        Music.playEffect("laser02.mp3");

                        processSpaceKeyPressLogic(); // 판정 로직

                        // 1. 레이저 이미지 설정 요청 (인덱스 기반) -> 클릭 좌표 기반으로 수정
                        updateLaserFramesByClickX(clickX);

                        // ⭐️ 타이머 시작 요청 -> 레이저 발사
                        startLaserAnimation();

                        if (currentJudgementText != null && !currentJudgementText.equals("MISS")) {
                            boolean shouldExplode = true; // 기본적으로 폭발

                            // ⭐️ [수정] 수프 재료 특수 로직: 정지 상태이고, 5회 미만 클릭일 때
                            if (mat.isSoup && mat.isStopped) {
                                mat.currentHits++; // 성공 횟수 증가

                                if (mat.currentHits < mat.REQUIRED_HITS) {
                                    // 5회 미만이면 폭발하지 않고 카운트만 증가
                                    shouldExplode = false;
                                }
                            }

                            if (shouldExplode) {
                                boomDrawX = clickX;
                                boomDrawY = clickY;

                                createAndDropFragments(mat, clickX);
                                // ‼️ [수정] 즉시 제거(matList.remove(i)) 대신 제거 플래그 설정
                                mat.isDead = true;

                                // ⭐️ 폭발 애니메이션 시작
                                startBoomAnimation(); // <-- 이름 변경 적용
                            }
                        }

                        // 한 번에 하나만 처리
                        break;
                    }
                }
                repaint();
            }*/

            @Override
            public void mousePressed(MouseEvent e) {
                int clickX = e.getX();
                int clickY = e.getY();

                // 충돌 판정 루프
                for (int i = 0; i < matList.size(); i++) {
                    Material mat = matList.get(i);


                    if (mat.getBounds().contains(clickX, clickY)) {
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                // 백그라운드 스레드에서 실행
                                Music.playEffect("laser02.mp3"); // 🎵 I/O 작업 분리
                                processSpaceKeyPressLogic();     // 🎮 무거운 게임 로직 분리

                                // createAndDropFragments(mat, clickX); // 🧩 객체 생성/초기화 작업 분리
                                // **주의:** GUI 객체(mat)의 상태를 변경하는 작업은 doInBackground에서 직접 하지 마세요.

                                return null;
                            }
                        }.execute();

                        // 1. 레이저 이미지 설정 요청 (인덱스 기반) -> 클릭 좌표 기반으로 수정
                        updateLaserFramesByClickX(clickX);

                        // ⭐️ 타이머 시작 요청 -> 레이저 발사
                        startLaserAnimation();

                        if (currentJudgementText != null && !currentJudgementText.equals("MISS")) {
                            boolean shouldExplode = true; // 기본적으로 폭발

                            // ⭐️ [수정] 수프 재료 특수 로직: 정지 상태이고, 5회 미만 클릭일 때
                            if (mat.isSoup && mat.isStopped) {
                                mat.currentHits++; // 성공 횟수 증가

                                if (mat.currentHits < mat.REQUIRED_HITS) {
                                    // 5회 미만이면 폭발하지 않고 카운트만 증가
                                    shouldExplode = false;
                                }
                            }

                            if (shouldExplode) {
                                boomDrawX = clickX;
                                boomDrawY = clickY;

                                createAndDropFragments(mat, clickX);
                                // ‼️ [수정] 즉시 제거(matList.remove(i)) 대신 제거 플래그 설정
                                mat.isDead = true;

                                // ⭐️ 폭발 애니메이션 시작
                                startBoomAnimation(); // <-- 이름 변경 적용
                            }
                        }

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

    }

    protected void updateLaserFramesByClickX(int clickX) {
        final int MIN_X = 436;
        final int MAX_X = 831;
        final int RANGE_WIDTH = MAX_X - MIN_X; // 831 - 436 = 395

        // 2. 영역 3등분을 위한 경계 계산
        // 유효 영역을 3등분하여 왼쪽, 가운데, 오른쪽 영역을 정의합니다.
        int leftBoundary = MIN_X + (RANGE_WIDTH / 3);       // 436 + (395 / 3) ≈ 436 + 131 = 567
        int rightBoundary = MIN_X + (RANGE_WIDTH * 2 / 3);  // 436 + (395 * 2 / 3) ≈ 436 + 263 = 699

        // 2. 클릭 위치에 따른 이미지 설정
        if (clickX < leftBoundary) {
            // ⭐️ 왼쪽 1/3 영역에 클릭됨
            // Group B 이미지 (laser03, laser04) 사용
            laserFrames[0] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser03.png")).getImage();
            laserFrames[1] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser04.png")).getImage();

            cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01_L.png")).getImage();
            //System.out.println("Laser Direction: Left (3, 4)");

        } else if (clickX >= rightBoundary) {
            // ⭐️ 오른쪽 1/3 영역에 클릭됨
            // Group C 이미지 (laser05, laser06) 사용
            laserFrames[0] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser05.png")).getImage();
            laserFrames[1] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser06.png")).getImage();

            cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01_R.png")).getImage();
            //System.out.println("Laser Direction: Right (5, 6)");

        } else {
            // ⭐️ 가운데 1/3 영역에 클릭됨
            // Group A 이미지 (laser01, laser02) 사용
            laserFrames[0] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser01.png")).getImage();
            laserFrames[1] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser02.png")).getImage();

            cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01_M.png")).getImage();
            //System.out.println("Laser Direction: Center (1, 2)");
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

                // ‼️ [추가] 레이저 애니메이션 종료 시 대포 이미지 원상 복구
                cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01_M.png")).getImage();
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

    private void setupBoomAnimationTimer() {
        boomAnimationTimer = new Timer(BOOM_ANIMATION_DELAY, e -> {
            boomFrameIndex++;
            if (boomFrameIndex < BoomFrames.length) {
                currentBoomImage = BoomFrames[boomFrameIndex];
            } else {
                // 애니메이션 종료 후 이미지 null로 설정
                boomAnimationTimer.stop();
                currentBoomImage = null;
            }
            repaint();
        });
        boomAnimationTimer.setRepeats(true);
    }

    // 애니메이션 시작 메서드
    private void startBoomAnimation() {
        boomFrameIndex = 0;

        if (boomAnimationTimer.isRunning()) {
            boomAnimationTimer.stop();
        }
        boomAnimationTimer.start();
    }

    // ======== 🔹 슬로우 구간 정보 (ms단위)
    private static final double SLOW1_END_SEC = 31050;  // 슬로우1 끝
    private static final double SLOW2_END_SEC = 48055;  // 슬로우2 끝
    private static final double SLOW3_END_SEC = 75606;  // 슬로우3 끝

    // 🔹 슬로우 이후 밀림량 (ms)
    private static final int OFFSET_AFTER_SLOW1_MS = 609;  // 0.609초
    private static final int OFFSET_AFTER_SLOW2_MS = 477;  // 0.477초
    private static final int OFFSET_AFTER_SLOW3_MS = 206;

    // 🔹 논리 시간(sec)을 실제 판정 시간(ms)로 변환
    private static int toJudgeMs(int tSec) {
        int base = tSec; // 기존 인자: double tSec

        int idx = StageManager.musicIndex;  // 어떤 곡인지

        // 아직 음악 선택 전(default -1)이면 그냥 원본 시간 사용
        if (idx < 0) {
            return base;
        }

        switch (idx) {
            case 0:
                // 🎵 0번 곡: 슬로우 없음
                return base;

            case 1:
                // 🎵 1번 곡: 슬로우 1만 적용 (31.050 이후 +0.609초)
                if (tSec > SLOW1_END_SEC) {
                    return base + OFFSET_AFTER_SLOW1_MS;
                }
                return base;

            case 2:
                // 🎵 2번 곡: 슬로우 2만 적용 (48.055 이후 +0.477초)
                if (tSec > SLOW2_END_SEC) {
                    return base + OFFSET_AFTER_SLOW2_MS;
                }
                return base;

            case 3:
                // 🎵 3번 곡
                if (tSec > SLOW3_END_SEC) {
                    return base + OFFSET_AFTER_SLOW3_MS;
                }
                return getTransformedTime(tSec);

            default:
                // 🎵 그 외: 슬로우 없음 버전으로 처리
                return base;
        }
    }

    private static int getTransformedTime(int tSec) {
        switch (tSec) {
            case 73698:
                return 73714;
            case 73913:
                return 73953;
            case 74123:
                return 74187;
            case 74338:
                return 74427;
            case 74553:
                return 74667;
            case 74768:
                return 74907;
            case 74978:
                return 75141;
            case 75193:
                return 75381;
            default:
                // 매핑된 값이 없는 경우, 원래 값을 그대로 반환합니다.
                return tSec;
        }
    }

    // 🔹 더블 배열(초)을 ms 배열로 한 번에 변환
    private static int[] buildJudgeTimes(int[] secs) {
        int[] result = new int[secs.length];
        for (int i = 0; i < secs.length; i++) {
            result[i] = toJudgeMs(secs[i]);  // ← 여기서 슬로우/밀림을 반영
        }
        return result;
    }

    private void createAndDropFragments(Material originalMat, int clickX) {
        Random random = new Random();

        // 재료를 2개의 조각(잘린 단면)으로 나눕니다.
        // MatType에 따라 Sliced 이미지를 사용하도록 처리합니다.

        // 1. 왼쪽 조각 (matType_left)
        Material fragmentLeft = new Material(originalMat, true); // true: 잘린 조각

        // 2. 오른쪽 조각 (matType_right)
        Material fragmentRight = new Material(originalMat, true); // true: 잘린 조각

        // 3. 잔해 속도 및 방향 설정 (중앙을 기준으로 바깥쪽으로 퍼지면서 아래로 떨어지게)

        // ⭐️ 왼쪽 조각: 왼쪽(음수)으로 퍼지게
        double xSpeedLeft = -(FRAGMENT_SPREAD + random.nextDouble());
        double ySpeedLeft = FRAGMENT_SPEED + random.nextDouble();
        fragmentLeft.setXSpeed(xSpeedLeft);
        fragmentLeft.setYSpeed(ySpeedLeft);

        // ⭐️ 오른쪽 조각: 오른쪽(양수)으로 퍼지게
        double xSpeedRight = (FRAGMENT_SPREAD + random.nextDouble());
        double ySpeedRight = FRAGMENT_SPEED + random.nextDouble();
        fragmentRight.setXSpeed(xSpeedRight);
        fragmentRight.setYSpeed(ySpeedRight);

        // ⭐️ 시작 위치를 중앙에서 살짝 분리하여 폭발 효과 부여
        fragmentLeft.setX(originalMat.getX() - 5);
        fragmentRight.setX(originalMat.getX() + 5);

        fragmentList.add(fragmentLeft);
        fragmentList.add(fragmentRight);
    }


    private void drawBoom(Graphics g) {
        int origW = currentBoomImage.getWidth(this);
        int origH = currentBoomImage.getHeight(this);

        // 🔹 스케일 적용된 크기
        int drawW = (int) (origW * boomScale);
        int drawH = (int) (origH * boomScale);

        // 클릭된 좌표에 그려짐
        int x = boomDrawX - drawW / 2;
        int y = boomDrawY - drawH / 2;

        g.drawImage(currentBoomImage, x, y, drawW, drawH, this);

    }

    // answerTimeMs : 정답 타이밍
    public void dropMats(long answerTimeMs, String matType, double speedX, double speedY, int destX) {
        /*
        // 1. 초기 좌표와 출발 시간 계산
        SpeedResult result = calculateInitialAndTime(answerTimeMs, speedX, speedY, destX);
        double startX = result.getNewSpeedX();
        long dropStartTime = result.getTimestamp();

        // 2. Material 객체 생성 (고정 Y 좌표와 계산된 X, 시간 사용)
        Material newMat = new Material(startX, FIXED_START_Y, matType, speedX, speedY, answerTimeMs, dropStartTime);

        // 3. 리스트에 추가
        matList.add(newMat);

        */
        // 1. 이동 거리 계산 (Y축)
        double distanceY = JUDGEMENT_TARGET_Y - FIXED_START_Y;

        // 2. ⭐️ [수정 핵심] 속도를 '픽셀/ms' 단위로 변환합니다.
        double speedY_ms = speedY / (double) SLEEP_TIME;
        double speedX_ms = speedX / (double) SLEEP_TIME;

        // 3. ⭐️ 이동 시간 (ms) 계산
        // travelTimeMs = distanceY (px) / speedY_ms (px/ms)
        long travelTimeMs = (long) (distanceY / speedY_ms); // 틱을 사용하지 않고 순수 ms로 계산

        // 4. 드롭 시작 시간 계산 (Start = Answer Time - Travel Time)
        long dropStartTime = answerTimeMs - travelTimeMs;

        // 5. X축 이동 거리 계산 (travelTimeMs 사용)
        double distanceX = speedX_ms * travelTimeMs; // 픽셀/ms * ms

        // 6. 초기 X 좌표 계산
        double startX = destX - distanceX;

        // 7. Material 객체 생성 (이때 speedX, speedY는 여전히 '픽셀/틱' 단위여야 합니다.
        //    Material은 이 속도를 updateMaterialPositions에서 사용하기 때문입니다.)
        Material newMat = new Material(startX, FIXED_START_Y, matType, speedX, speedY, answerTimeMs, dropStartTime);

        matList.add(newMat);
    }

    private SpeedResult calculateInitialAndTime(long answerTimeMs, double speedX, double speedY, int destX) {

        // 1. 이동 거리 계산 (Y축)
        double distanceY = JUDGEMENT_TARGET_Y - FIXED_START_Y;

        // 2. Y축 이동에 필요한 틱 수 및 시간 계산
        double totalTicks = distanceY / speedY;
        long travelTimeMs = (long) (totalTicks * SLEEP_TIME);

        // 3. X축 이동 거리 계산 (도착 시간을 맞추기 위해 Y축 시간과 동일하게 사용)
        double distanceX = speedX * totalTicks;

        // 4. 초기 X 좌표 계산 (Initial = Center - Distance)
        // 재료가 중앙에 도착하도록 X좌표 역산
        double initialX = destX - distanceX;

        // 5. 드롭 시작 시간 계산 (Start = Answer Time - Travel Time)
        long dropStartTime = answerTimeMs - travelTimeMs;

        return new SpeedResult(initialX, dropStartTime);
    }

    public class SpeedResult {
        private final double newSpeedX;
        private final long timestamp;

        public SpeedResult(double newSpeedX, long timestamp) {
            this.newSpeedX = newSpeedX;
            this.timestamp = timestamp;
        }

        public double getNewSpeedX() {
            return newSpeedX;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private void updateMaterialPositions() {
        // 1. Iterator를 사용하여 matList를 순회
        Iterator<Material> iterator = matList.iterator();

        // 2. 재료를 확인하며 움직임 및 제거 로직 실행
        while (iterator.hasNext()) {
            Material mat = iterator.next();

            // ⭐️ [추가] isDead 플래그 확인 및 안전하게 제거
            if (mat.isDead) {
                iterator.remove(); // ⭐️ Iterator의 remove()를 사용하여 안전하게 제거
                continue;
            }

            // ⭐️ [추가] 수프 재료 정지/재개 로직
            if (mat.isSoup) {
                //System.out.println(수프정지);
                // 1. 정지 조건: 72.5초가 지났고, Y좌표가 150에 도달했을 때
                if (!mat.isStopped && progressTime >= SOUP_STOP_TIME && mat.getY() >= mat.STOP_Y && progressTime < SOUP_RESUME_TIME) {
                    mat.isStopped = true;
                    mat.setY(mat.STOP_Y); // 정확히 150에 고정
                    System.out.println("수프 정지");
                }

                // 2. 재개 조건: 75.5초가 지났을 때
                if (mat.isStopped && progressTime >= SOUP_RESUME_TIME) {
                    mat.isStopped = false;
                    System.out.println("수프 재개");
                }

            }
            //System.out.println("현재 시간: " + StageManager.progressTime);
            //System.out.println("낙하 시작 시간: " + mat.actualDropStartTime);
            // --- [기존 로직: 재료 이동] ---
            if (StageManager.progressTime >= mat.actualDropStartTime) {
                // 1. 실제 경과 시간 (ms) 계산
                long finalElapsedTime = progressTime - mat.actualDropStartTime;

                // 2. 픽셀/틱 속도를 픽셀/ms 속도로 변환
                double speedX_ms = mat.getXSpeed() / (double) SLEEP_TIME;
                double speedY_ms = mat.getYSpeed() / (double) SLEEP_TIME;

                // 3. 현재 위치 설정: 시작 위치 + (픽셀/ms 속도 * 경과 시간)

                // 3-1. Y축 위치 계산
                double currentY = FIXED_START_Y + (speedY_ms * finalElapsedTime);
                mat.setY(currentY);

                // 3-2. X축 위치 계산
                double currentX = mat.getInitialX() + (speedX_ms * finalElapsedTime);
                mat.setX(currentX);

                //System.out.println(mat.matType + " -> x : " + mat.getX() + ", y : " + mat.getY() + "t : " + currentMusicTimeMs);

                // mat.drop(); -> 기존

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

        // ⭐️ 2. 잔해 리스트 업데이트 로직 추가
        Iterator<Material> fragmentIterator = fragmentList.iterator();
        while (fragmentIterator.hasNext()) {
            Material frag = fragmentIterator.next();

            // 잔해는 즉시 떨어지기 시작합니다. (별도의 start time 체크 불필요)
            frag.drop();

            if (frag.getY() > 182) {
                fragmentIterator.remove(); // 잔해 제거
                continue; // 제거 후 다음 반복으로 이동
            }
        }
    }

    @Override
    public void updateByMusicTime(int t) {
        super.updateByMusicTime(t); // SpaceAnimation의 점수 업데이트 및 기본 로직 호출

        this.progressTime = t;

        // 53.5초에 한 번만 켜기 (표시 시간은 1.5초 예시)
        if (!bannerShown && t >= toJudgeMs(53500)) {
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
/*
        // 2. 선의 색상 설정 (예: 빨간색)
        g.setColor(Color.RED);


        // 4. 선 그리기
        // 화면의 가장 왼쪽(0)부터 가장 오른쪽(getWidth())까지 선을 그립니다.
        // JUDGMENT_LINE_Y는 150입니다.
        int screenWidth = getWidth(); // SpaceStage3의 너비 (Panel의 너비)
        int yPos = JUDGEMENT_TARGET_Y;

        g.drawLine(0, yPos, screenWidth, yPos);
*/
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
            g.drawImage(currentAlien, 0, -10, getWidth(), getHeight(), null);
        }

        for (int i = 0; i < matList.size(); i++) {
            Material mat = matList.get(i);
            mat.screenDraw(g);
        }

        // 잔해(Fragment) 리스트 그리기
        for (Material frag : fragmentList) {
            frag.screenDraw(g);
        }

        if (currentBoomImage != null && boomDrawX != -1 && boomDrawY != -1) drawBoom(g);


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

    private double x, y; // 생성 위치
    private int width, height;
    public String matType; // 어떤 재료인지
    private double xSpeed, ySpeed;
    private double initialX;

    public boolean isFragment = false;

    // ✅ [추가] 수프 전용 필드
    public final int STOP_Y = 150; // 멈출 Y 좌표
    public final int REQUIRED_HITS = 7; // 필요한 클릭 횟수

    public boolean isSoup = false; // 수프 재료인지 여부
    public boolean isStopped = false; // 현재 멈춰있는지
    public int currentHits = 0; // 현재 성공한 클릭 횟수 (멈춰있을 때만 증가)

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getXSpeed() {
        return this.xSpeed;
    }

    public double getYSpeed() {
        return this.ySpeed;
    }

    // ⭐️ X, Y 속도 및 위치 설정 메서드 (잔해 조각에 속성 부여용)
    public void setXSpeed(double xSpeed) {
        this.xSpeed = xSpeed;
    }

    public void setYSpeed(double ySpeed) {
        this.ySpeed = ySpeed;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getInitialX() {
        return this.initialX;
    }


    // ⭐️ 목표 도착 시간 (정답 타이밍)
    private long targetArriveTime;

    // ⭐️ 실제 움직임을 시작해야 할 게임 시간 (핵심 필드)
    public long actualDropStartTime;

    public double rotationAngle = 0; // ⭐️ 회전 각도 (라디안 또는 도)

    public boolean isDead = false; // 제거 대상으로 표시

    public Material(double x, double y, String matType, double xSpeed, double ySpeed, long targetArriveTime, long dropStartTime) {
        this.initialX = x;
        this.x = x; // 생성 좌표
        this.y = y;
        this.matType = matType;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.targetArriveTime = targetArriveTime;
        this.actualDropStartTime = dropStartTime; // 👈 재료가 움직이기 시작할 시간

        this.isFragment = false;

        this.isSoup = matType.equals("soup");

    }

    // 파편 생성자
    public Material(Material original, boolean isFragment) {
        // 기본 속성 복사
        this.x = original.x;
        this.y = original.y;
        this.matType = original.matType;
        this.width = original.width;
        this.height = original.height;
        this.rotationAngle = original.rotationAngle;

        this.isFragment = isFragment;

    }

    public void screenDraw(Graphics g) {
		/*
		switch (matType) {
		case "chili":
			width = 157;
			height = 300;
			g.drawImage(chiliImage, (int)Math.round(x), (int)Math.round(y), width, height, null);
			break;
		case "egg":
			width = 212;
			height = 192;
			g.drawImage(eggImage, (int)Math.round(x), (int)Math.round(y), width, height, null);
			break;
		case "mushroom":
			width = 170;
			height = 113;
			g.drawImage(mushroomImage, (int)Math.round(x), (int)Math.round(y), width, height, null);
			break;
		case "welshonion1":
			width = 200;
			height = 200;
			g.drawImage(welshonion1Image, (int)Math.round(x), (int)Math.round(y), width, height, null);
			break;
		case "welshonion2":
			width = 200;
			height = 200;
			g.drawImage(welshonion2Image, (int)Math.round(x), (int)Math.round(y), width, height, null);
			break;
		case "soup":
			width = 220;
			height = 271;
			g.drawImage(soupImage, (int)Math.round(x), (int)Math.round(y), width, height, null);
			break;
		}
	*/
        // ⭐️ Graphics2D 객체 준비 (회전 및 변환을 위해 필요)
        Graphics2D g2d = (Graphics2D) g.create();

        // ⭐️ 그려야 할 이미지 객체 선택
        Image imageToDraw = null;

        if (this.isFragment) {
            // 잔해 조각일 경우: Sliced 이미지 사용
            switch (matType) {
                case "chili":
                    imageToDraw = slicedChiliImage;
                    break;
                case "egg":
                    imageToDraw = FriedEggImage;
                    break; // 잔해 이미지로 변경 필요
                case "mushroom":
                    imageToDraw = slicedMushroomImage;
                    break;
                case "welshonion1":
                    imageToDraw = slicedWelshonion1Image;
                    break;
                case "welshonion2":
                    imageToDraw = slicedWelshonion2Image;
                    break;
                case "soup":
                    imageToDraw = slicedSoupImage;
                    break;
                default:
                    imageToDraw = null;
            }
        } else {
            // 일반 재료일 경우: 기본 이미지 사용
            switch (matType) {
                case "chili":
                    imageToDraw = chiliImage;
                    break;
                case "egg":
                    imageToDraw = eggImage;
                    break;
                case "mushroom":
                    imageToDraw = mushroomImage;
                    break;
                case "welshonion1":
                    imageToDraw = welshonion1Image;
                    break;
                case "welshonion2":
                    imageToDraw = welshonion2Image;
                    break;
                case "soup":
                    imageToDraw = soupImage;
                    break;
                default:
                    imageToDraw = null;
            }
        }

        if (this.isFragment) {
            // 1. 이미지 타입에 따라 크기(width, height) 결정
            switch (matType) {
                case "chili":
                    width = 120;
                    height = 167;
                    break;
                case "egg":
                    width = 212;
                    height = 192;
                    break;
                case "mushroom":
                    width = 85;
                    height = 60;
                    break;
                case "welshonion1":
                case "welshonion2":
                    width = 116;
                    height = 98;
                    break;
                case "soup":
                    width = 110;
                    height = 135;
                    break;
                default:
                    width = 0;
                    height = 0;
            }
        } else {
            switch (matType) {
                case "chili":
                    width = 126;
                    height = 240;
                    break;
                case "egg":
                    width = 212;
                    height = 192;
                    break;
                case "mushroom":
                    width = 204;
                    height = 136;
                    break;
                case "welshonion1":
                case "welshonion2":
                    width = 200;
                    height = 200;
                    break;
                case "soup":
                    width = 220;
                    height = 271;
                    break;
                default:
                    width = 0;
                    height = 0;
            }
        }

        if (imageToDraw != null) {

            if (this.isFragment) {
                // ⭐️ 잔해 조각: 회전 및 중앙 정렬 적용
                //if (matType != "soup") {
                // 이미지의 중심 좌표로 변환
                g2d.translate(x + width / 2, y + height / 2);
                // 회전 적용 (rotationAngle은 Material 클래스 내부에서 업데이트 필요)
                g2d.rotate(Math.toRadians(rotationAngle));

                // 중심 기준으로 이미지 그리기
                g2d.drawImage(imageToDraw, -width / 2, -height / 2, width, height, null);

                // ⭐️ 잔해가 떨어지는 동안 회전 각도를 증가시킵니다.
                this.rotationAngle = (this.rotationAngle + 5) % 360;
                //}
            } else {
                // ⭐️ 일반 재료: 기존 방식대로 정위치에 그리기
                //g2d.drawImage(imageToDraw, (int) Math.round(x), (int) Math.round(y) - height/2, width, height, null);
                g2d.drawImage(imageToDraw, (int) Math.round(x), (int) Math.round(y), width, height, null);
            }
        }

        g2d.dispose(); // 생성된 Graphics2D 객체 해제

        if (SpaceStage3.currentLaserImage != null) {
            g.drawImage(SpaceStage3.currentLaserImage, 0, 0, null);
            g.dispose();
        }

    }

    public void drop() {
        if (!isStopped) {
            x += this.xSpeed;
            y += this.ySpeed;
            //System.out.println(matType + " -> x : " + x + ", y : " + y);
        }
    }

    public Rectangle getBounds() {
        /*
        int padding = 10; // ⭐️ 판정 영역을 10픽셀씩 확장 (클릭 쉽게)

        return new Rectangle((int) Math.round(x) - padding,  // X 시작점을 패딩만큼 왼쪽으로 이동
                (int) Math.round(y) - padding,  // Y 시작점을 패딩만큼 위로 이동
                width + (padding * 2),         // 너비를 양쪽 패딩만큼 확장
                height + (padding * 2));*/
        return new Rectangle((int) Math.round(x),  // X 시작점을 패딩만큼 왼쪽으로 이동
                (int) Math.round(y),  // Y 시작점을 패딩만큼 위로 이동
                width,         // 너비를 양쪽 패딩만큼 확장
                height);
    }

}
