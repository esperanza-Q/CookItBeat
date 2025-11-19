package game.Space;

import game.Main;
import game.Music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;


public class SpaceStage2 extends SpaceAnimation {

    // 이미지
    private Image alien1;
    private Image alien2;
    private Image cat1;
    private Image cat2;
    private Image cannon;

    private List<Noodle> noodles = new ArrayList<>();
    // ===================== 면발 오브젝트 클래스 =====================
    private static class Noodle {
        float x, y;          // 위치
        float scale = 0.5f;  // 크기

        boolean visible = true;
        boolean captured = false;   // true면 UFO로 끌려가는 상태

        int startTime = -1;
        int captureStartTime = -1;

        // 🔹 이동 속도 (px/s)
        float vx;  // x 방향 속도
        float vy;  // y 방향 속도

        // 🔹 캡쳐 시작 시점의 위치를 따로 저장
        float startX;
        float startY;
    }



    // 🔵 Stage2 전용 블랙홀 GIF
    private ImageIcon blackholeGif;
    private boolean blackholeVisible = false;

    // 블랙홀 등장 애니메이션 (커지면서 등장)
    private long blackholeStartTimeMs = -1;      // 언제부터 키우기 시작했는지
    private float blackholeScale = 0.1f;         // 시작 스케일
    private static final float BLACKHOLE_MAX_SCALE = 0.7f;   // 최종 스케일
    private static final int BLACKHOLE_GROW_DURATION = 1000; // 몇 ms 동안 커질지 (1초)


    // 53초 구간 전환 타이밍 (53.139초)
    private static final int PHASE_CHANGE_TIME_53 = 53139;  // 53.139 * 1000

    private boolean phaseChangedAt53 = false;

    //면발 크기 조절
    private float noodleScale = 0.5f;   // 50% 크기


    // 생성 효과음이 이미 재생됐는지
    private boolean blackholeSpawnSfxPlayed = false;

    // 🔧 위치 조정용 오프셋 (원하는 값으로 수정해서 쓰면 됨)
    private int blackholeOffsetX = 0;   // +면 오른쪽, -면 왼쪽
    private int blackholeOffsetY = 100; // +면 아래, -면 위

    // ===================== 면발 궤도 관련 =====================

    // 면발 이미지 (작은 면발 조각 PNG 추천)
    private Image noodleImage;

    // 현재 면발 위치
    private float noodleX;
    private float noodleY;

    // 좌우로 움직일 때의 속도 (px/sec)
    private float noodleSpeed = 200f;   // 필요하면 나중에 조정
    private int noodleDir = 1;          // 1: 오른쪽, -1: 왼쪽

    // 상태
    private boolean noodleVisible = true;
    private boolean noodleCaptured = false;    // UFO에 끌려가는 중인지

    // UFO(라면 그릇 / 블랙홀) 목표 위치
// 나중에 화면 보면서 숫자 조정하면 됨
    private int ufoTargetX = 640;   // 화면 가로 1280 기준 중앙 예시
    private int ufoTargetY = 400;   // UFO 중심 Y 대충

    // 궤도 꺾이는 애니메이션용
    private int noodleCaptureStartTimeMs = -1;
    private int noodleCaptureDuration = 700;  // 0.7초 동안 UFO로 이동

    private float noodleStartX;
    private float noodleStartY;

    // 지난 프레임 시간 (delta 계산용)
    private int lastUpdateTimeMs = -1;


    // 🔹 현재 시간 t 기준으로 "가장 가까운 노트 인덱스" 찾기
    private int getNearestNoteIndex(int t, int windowMs) {
        int[] noteTimes = USER_PRESS_TIMES_INT;

        int nearestIdx = -1;
        int nearestDiff = Integer.MAX_VALUE;

        for (int i = 0; i < noteTimes.length; i++) {
            int diff = Math.abs(t - noteTimes[i]);
            if (diff < nearestDiff) {
                nearestDiff = diff;
                nearestIdx = i;
            }
        }

        if (nearestIdx == -1 || nearestDiff > windowMs) {
            return -1; // 근처에 노트 없음
        }
        return nearestIdx;
    }



    // ======== 🔹 슬로우 구간 정보 (초 단위)
    private static final double SLOW1_END_SEC = 31.050;  // 슬로우1 끝
    private static final double SLOW2_END_SEC = 48.055;  // 슬로우2 끝

    // 🔹 슬로우 이후 밀림량 (ms)
    private static final int OFFSET_AFTER_SLOW1_MS = 609;  // 0.609초
    private static final int OFFSET_AFTER_SLOW2_MS = 477;  // 0.477초

    // 🔹 논리 시간(sec)을 실제 판정 시간(ms)로 변환
    private static int toJudgeMs(double tSec) {
        int base = (int) Math.round(tSec * 1000.0);

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

            default:
                // 🎵 그 외(3번 등): 일단 슬로우 없음 버전으로 처리
                return base;
        }
    }


    // 🔹 더블 배열(초)을 ms 배열로 한 번에 변환
    private static int[] buildJudgeTimes(double[] secs) {
        int[] result = new int[secs.length];
        for (int i = 0; i < secs.length; i++) {
            result[i] = toJudgeMs(secs[i]);
        }
        return result;
    }

    // ✅ 키 힌트 이미지
    private Image keyAImage;
    private Image keyDImage;
    private Image keyWImage;

    private Image currentKeyGuideImage;

    //stage2라고 뜨는 배너
    private Image stage2Banner;      // 25초에 띄울 이미지
    private boolean bannerVisible = false;
    private int bannerHideAtMs = 0;
    private boolean bannerShown = false; // 한 번만 띄우기

    // 현재 보여줄 이미지
    private Image currentUser;

    // 외계인 손 현재 이미지
    private Image currentAlien;

    // 공기포 애니메이션 관련 변수
    private Image boomLeftImage = null;
    private Image boomRightImage = null;
    private Timer boomTimer;
    private int boomFrameIndex = 0;
    private final int BOOM_ANIMATION_DELAY = 50; // 공기포 이미지 전환 속도 (ms)

    private boolean leftBoomActive = false;
    private boolean rightBoomActive = false;

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
    private final int ALIEN_APPEAR_TIME_1 = 27 * 1000;   // 0:27
    private final int ALIEN_APPEAR_TIME_2 = 29 * 1000;   // 0:29
    private final double ALIEN_APPEAR_TIME_3 = (double) (30.5 * 1000);   // 0:30.5
    private final int ALIEN_APPEAR_TIME_4 = 32 * 1000;   // 0:32
    private final int ALIEN_APPEAR_TIME_5 = 34 * 1000;   // 0:34
    private final int ALIEN_APPEAR_TIME_6 = 36 * 1000;   // 0:36
    private final int ALIEN_APPEAR_TIME_7 = 39 * 1000;   // 0:39
    private final int ALIEN_APPEAR_TIME_8 = 47 * 1000;   // 0:47

    // 외계인 손 자동 동작 타이밍
    private static final double[] ALIEN_PRESS_TIMES_SEC = {
            28.285, 28.505, 28.725,
            31.280, 31.720,
            35.146, 35.366, 35.576,
            35.577,
            41.793, 42.002,
            43.282, 43.502, 43.722, 43.942,
            45.435, 45.859, 46.283
    };

    private final int[] ALIEN_PRESS_TIMES_INT = buildJudgeTimes(ALIEN_PRESS_TIMES_SEC);


    // ✅ 판정 정답 타이밍 (SpaceAnimation에 넘기는 타이밍)
    // ✅ 논리적인 노트 시간 (초 단위) — DAW에서 읽은 값 그대로
    private static final double[] USER_PRESS_TIMES_SEC = {
            // 예시: 네가 적어둔 초 단위 타이밍들(삡/딴 구간 중 "판정용" 것들만)
            28.285, 28.505, 28.725,
            29.983, 30.203, 30.423,
            31.280, 31.720,
            33.410, 33.850,
            35.146, 35.366, 35.576,
            37.718, 37.928, 38.138,
            48.649, 48.858,
            50.138, 50.358, 50.578, 50.798,
            52.290, 52.715, 53.139
    };

    // ✅ 실제 판정에 쓰는 ms 배열 (슬로우 보정 적용된 값)
    private static final int[] USER_PRESS_TIMES_INT = buildJudgeTimes(USER_PRESS_TIMES_SEC);


    // 딴 패턴이 시작하는 시점(첫 딴 타이밍, 초 단위)
    private static final double[] DDAN_START_TIMES_SEC = {
            29.983,  // 30초 딴딴딴
            33.410,  // 33초 딴딴
            37.718,  // 37초 딴딴딴
            48.649   // 47초 딴딴...
    };

    // 슬로우 보정이 적용된 ms 타이밍
    private static final int[] DDAN_START_TIMES = buildJudgeTimes(DDAN_START_TIMES_SEC);

    // ✅ 각 노트 타이밍에 대한 "정답 키" 배열
// USER_PRESS_TIMES_INT와 길이가 같아야 함
    // USER_PRESS_TIMES_SEC와 길이 100% 동일해야 함
    private static final int[] NOTE_KEYS = {
            // 28.285, 28.505, 28.725
            KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,

            // 29.983, 30.203, 30.423
            KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,

            // 31.280, 31.720
            KeyEvent.VK_A, KeyEvent.VK_A,

            // 33.410, 33.850
            KeyEvent.VK_D, KeyEvent.VK_D,

            // 35.146, 35.366, 35.576
            KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,

            // 37.718, 37.928, 38.138
            KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,

            // 48.649, 48.858
            KeyEvent.VK_W, KeyEvent.VK_W,

            // 50.138, 50.358, 50.578, 50.798
            KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,

            // 52.290, 52.715, 53.139
            KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W
    };


    // 외계인 손이 alien2로 바뀐 후 돌아오는 타이밍
    private final int ALIEN_RELEASE_DELAY_MS = 50;
    private final int[] ALIEN_RELEASE_TIMES;

    // int[] -> long[] 변환 헬퍼
    private static long[] convertToLongArray(int[] array) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }





    public SpaceStage2() {
        // 판정 타이밍을 부모에게 전달
        super(convertToLongArray(USER_PRESS_TIMES_INT));

        disableSpaceKeyFromBase();

        // 외계인 손 release 타이밍 계산
        ALIEN_RELEASE_TIMES = new int[ALIEN_PRESS_TIMES_INT.length];
        for (int i = 0; i < ALIEN_PRESS_TIMES_INT.length; i++) {
            ALIEN_RELEASE_TIMES[i] = ALIEN_PRESS_TIMES_INT[i] + ALIEN_RELEASE_DELAY_MS;
        }

        // 이미지 로드
        alien1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/hologram_alien1.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/hologram_alien2.png")).getImage();
        cat1   = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand01.png")).getImage();
        cat2   = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand02.png")).getImage();
        cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01.png")).getImage();
        currentUser = cat1;

        stage2Banner = new ImageIcon(Main.class.getResource("../images/alienStage_image/space_stage2.png")).getImage();

        // 🔸 면발 이미지 로드
        noodleImage = new ImageIcon(Main.class.getResource("../images/alienStage_image/noodle02.png")).getImage();

        // ✅ 키 힌트 이미지 로드
        keyAImage = new ImageIcon(Main.class.getResource("../images/mainUI/key_A.png")).getImage();
        keyDImage = new ImageIcon(Main.class.getResource("../images/mainUI/key_D.png")).getImage();
        keyWImage = new ImageIcon(Main.class.getResource("../images/mainUI/key_W.png")).getImage();

        // 화면 기준으로 대략 초기 위치 설정 (중앙 위쪽 궤도)
        // 여기 값은 나중에 눈으로 보면서 맞추면 됨
        noodleX = 200;     // 왼쪽에서 시작
        noodleY = 250;     // 블랙홀보다 약간 위 또는 아래

        lastUpdateTimeMs = -1;

        // 🔵 블랙홀 GIF 로드 (Stage2에서만 사용)
        blackholeGif = new ImageIcon(Main.class.getResource("../images/alienStage_image/Ramen_blackhole.gif"));
        blackholeVisible = false;   // 처음에는 안 보이게

        // 🔴 공기포 타이머 세팅
        setupBoomAnimationTimer();



        // ✅ Stage2 전용: WASD 눌렀을 때만 정답 판정 + Boom 실행
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                boolean fireLeft = false;
                boolean fireRight = false;

                // 🔊 효과음 재생은 "유효한 키(WASD)"일 때만 실행
                switch (code) {
                    case KeyEvent.VK_A:        // A: 왼쪽만
                        playShotSfx();         // ← 효과음
                        fireLeft = true;
                        break;
                    case KeyEvent.VK_D:        // D: 오른쪽만
                        playShotSfx();         // ← 효과음
                        fireRight = true;
                        break;
                    case KeyEvent.VK_W:        // W: 양쪽
                    case KeyEvent.VK_S:        // S: 양쪽
                        playShotSfx();         // ← 효과음
                        fireLeft = true;
                        fireRight = true;
                        break;
                    default:
                        // WASD 아닌 키는 공기포도, 소리도, 판정도 안 함
                        return;
                }

                // ✅ 여기서 cat1 → cat2 로 변경 (Stage1과 동일한 로직 재사용)
                changeStageImageOnPress();
                repaint();

                // 1) 공기포 이펙트 먼저 발사
                startBoomAnimation(fireLeft, fireRight);

                // 2) 판정(점수)은 "정답 키 + 정답 타이밍"일 때만
                if (!isCorrectKeyForCurrentTime(code)) {
                    return;
                }

                // 3) 정답일 때만 판정 로직 실행
                triggerJudgeAndBoomFromStage2();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();

                // ✅ WASD 키가 떼졌을 때 cat2 → cat1 으로 복귀
                if (code == KeyEvent.VK_W ||
                        code == KeyEvent.VK_A ||
                        code == KeyEvent.VK_S ||
                        code == KeyEvent.VK_D) {

                    changeStageImageOnRelease();
                    repaint();
                }
            }
        });



    }


    // ✅ 현재 음악 시간 근처의 노트에 대해, keyCode가 정답인지 확인
    private boolean isCorrectKeyForCurrentTime(int keyCode) {
        final int ALLOW_WINDOW_MS = 300; // 판정 윈도우

        int idx = getNearestNoteIndex(currentMusicTimeMs, ALLOW_WINDOW_MS);
        if (idx == -1) return false;

        return keyCode == NOTE_KEYS[idx];
    }



    // 블랙홀 애니메이션 리셋 (크기/시간 초기화)
    private void resetBlackhole(int t) {
        blackholeVisible = true;
        blackholeStartTimeMs = t;
        blackholeScale = 0.09f;  // 처음에는 작게
    }

    private void spawnNoodle(int currentTime) {
        if (blackholeGif == null) return;

        int originalW = blackholeGif.getIconWidth();
        int originalH = blackholeGif.getIconHeight();

        int drawW = (int) (originalW * blackholeScale);
        int drawH = (int) (originalH * blackholeScale);

        int baseX = getWidth() / 2 - 370;
        int baseY = getHeight() / 2 - 270;

        int centerX = baseX + blackholeOffsetX;
        int centerY = baseY + blackholeOffsetY;

        Noodle n = new Noodle();
        n.x = centerX;
        n.y = centerY;
        n.startTime = currentTime;

        // 🔹 대각선 방향 설정
        float speed = 250f;          // 전체 속도 (원하면 나중에 조절)
        boolean goRight = Math.random() < 0.5;  // true면 ↘, false면 ↙

        n.vy = speed * 0.9f;         // 아래로 (y+ 방향)
        if (goRight) {
            n.vx = speed;            // 오른쪽 아래 ↘
        } else {
            n.vx = -speed;           // 왼쪽 아래 ↙
        }

        noodles.add(n);
    }


    // ✅ 현재 음악 시간 기준으로 "다음에 눌러야 할 키" 힌트 업데이트
    // ✅ 현재 음악 시간 기준으로 "지금 눌러야 할 키" 힌트 업데이트
    private void updateKeyGuideByTime(int t) {
        if (phaseChangedAt53) {
            currentKeyGuideImage = null;
            return;
        }

        // 힌트는 판정보다 살짝 넓게(예: ±600ms) 잡아도 됨
        final int HINT_WINDOW_MS = 600;

        int idx = getNearestNoteIndex(t, HINT_WINDOW_MS);
        if (idx == -1) {
            currentKeyGuideImage = null;
            return;
        }

        int keyCode = NOTE_KEYS[idx];

        switch (keyCode) {
            case KeyEvent.VK_A:
                currentKeyGuideImage = keyAImage;
                break;
            case KeyEvent.VK_D:
                currentKeyGuideImage = keyDImage;
                break;
            case KeyEvent.VK_W:
                currentKeyGuideImage = keyWImage;
                break;
            default:
                currentKeyGuideImage = null;
        }
    }




    // 🔊 공기포 샷 효과음
    private void playShotSfx() {
        // ✅ 파일 이름만 넘겨야 함
        Music.playEffect("balloon-pop.mp3");
    }


    // 공기포 애니메이션 타이머 설정
    private void setupBoomAnimationTimer() {
        boomTimer = new Timer(BOOM_ANIMATION_DELAY, e -> {
            // 둘 다 꺼져있으면 타이머 정지
            if (!leftBoomActive && !rightBoomActive) {
                boomTimer.stop();
                return;
            }

            boomFrameIndex++;

            if (boomFrameIndex >= BoomFrames.length) {
                // 애니메이션 한 사이클 끝나면 종료
                boomTimer.stop();
                boomFrameIndex = 0;

                leftBoomActive = false;
                rightBoomActive = false;
                boomLeftImage = null;
                boomRightImage = null;
            } else {
                if (leftBoomActive) {
                    boomLeftImage = BoomFrames[boomFrameIndex];
                }
                if (rightBoomActive) {
                    boomRightImage = BoomFrames[boomFrameIndex];
                }
            }

            repaint();
        });
        boomTimer.setRepeats(true);
    }



    // 공기포 애니메이션 시작
    // 특정 방향만 공기포 발사
    private void startBoomAnimation(boolean left, boolean right) {
        if (!left && !right) return; // 아무 쪽도 아니면 무시

        boomFrameIndex = 0;

        leftBoomActive = left;
        rightBoomActive = right;

        boomLeftImage = left ? BoomFrames[0] : null;
        boomRightImage = right ? BoomFrames[0] : null;

        if (boomTimer == null) {
            setupBoomAnimationTimer();
        }

        if (boomTimer.isRunning()) {
            boomTimer.stop();
        }
        boomTimer.start();

        repaint();
    }





    // SpaceAnimation에서 판정 후 호출되는 훅
    @Override
    protected void processSpaceKeyPressLogic() {
        // 🔇 Stage2에서는 Space 키로는 아무것도 하지 않음
        // (SpaceAnimation의 Space KeyListener가 이걸 호출해도 여기서 끝)
    }

    // 부모의 판정 훅이 호출해도 공기포는 여기서 안 쏘도록
    @Override
    protected void processSpaceKeyPress() {
        // Stage2에서는 Boom을 WASD 키 리스너에서만 처리
    }

    // ✅ Stage2용: 정답 키일 때만 부모의 판정 + Boom 로직 실행
    private void triggerJudgeAndBoomFromStage2() {
        // 부모(SpaceAnimation)의 원본 로직 실행 (점수, 판정 처리)
        SpaceStage2.super.processSpaceKeyPressLogic();

        // 가장 가까운 면발 하나를 캡처
        captureNearestNoodle();
    }

    // 면발 캡쳐 시작
    private void captureNearestNoodle() {
        Noodle target = null;

        for (Noodle n : noodles) {
            if (!n.visible) continue;
            if (n.captured) continue;

            target = n;
            break;
        }

        if (target != null) {
            target.captured = true;
        }
    }



    // 🔴 Stage2에서는 부모의 SPACE KeyListener 제거
    private void disableSpaceKeyFromBase() {
        KeyListener[] listeners = getKeyListeners();
        // SpaceAnimation에서 addKeyListener를 2번 했으니, 0: WASD컨트롤 / 1: SPACE
        if (listeners.length >= 2) {
            removeKeyListener(listeners[1]); // 두 번째 리스너 제거 → SPACE 리스너
        }
    }


    @Override
    public void updateByMusicTime(int t) {
        super.updateByMusicTime(t);

        // 25초 배너
        if (!bannerShown && t >= 25_000) {
            bannerVisible = true;
            bannerShown = true;
            bannerHideAtMs = t + 3000;

            // 🔵 배너가 나오는 순간 블랙홀 시작
            resetBlackhole(t);

            // 🔊 블랙홀 생성 효과음은 한 번만
            if (!blackholeSpawnSfxPlayed) {
                Music.playEffect("blackhole_effect.mp3");
                blackholeSpawnSfxPlayed = true;
            }
        }


        if (bannerVisible && t >= bannerHideAtMs) {
            bannerVisible = false;
        }

        // 🔵 블랙홀 커지는 애니메이션 (배너랑 상관 없이 계속 성장)
        if (blackholeVisible && blackholeStartTimeMs > 0) {
            int elapsed = t - (int) blackholeStartTimeMs;
            if (elapsed < 0) elapsed = 0;

            float progress = Math.min(1f, elapsed / (float) BLACKHOLE_GROW_DURATION);
            // 0.1배 → 1.0배로 서서히 커지게
            blackholeScale = 0.1f + (BLACKHOLE_MAX_SCALE - 0.1f) * progress;
        }

        // 외계인 손 자동 동작
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

        // ===== 면발 생성 =====
        // 예시: 30초, 32초, 34초에 면발 생성
        if (t >= 30000 && t < 30000 + 30) spawnNoodle(t);
        if (t >= 32000 && t < 32000 + 30) spawnNoodle(t);
        if (t >= 34000 && t < 34000 + 30) spawnNoodle(t);

        // ===== 프레임 간 시간 계산 (ms) =====
        int dt = 0;
        if (lastUpdateTimeMs < 0) {
            lastUpdateTimeMs = t;
        } else {
            dt = t - lastUpdateTimeMs;
            lastUpdateTimeMs = t;
        }

        // ===================== 면발 이동 로직 =====================
        if (dt > 0) {
            float dtSec = dt / 1000f;

            for (Noodle n : noodles) {

                if (!n.visible) continue;

                if (!n.captured) {
                    // 🔹 캡처되지 않은 상태: 블랙홀에서 대각선으로 날아감
                    n.x += n.vx * dtSec;
                    n.y += n.vy * dtSec;

                    // 화면 밖으로 나가면 제거
                    int margin = 100;
                    if (n.x < -margin || n.x > getWidth() + margin || n.y > getHeight() + margin) {
                        n.visible = false;
                    }

                } else {
                    // 🔹 캡쳐된 상태: UFO 쪽으로 빨려들어가는 애니메이션

                    // 💡 최초 1번만 캡쳐 시작 시간 + 시작 위치를 저장
                    if (n.captureStartTime < 0) {
                        n.captureStartTime = t;
                        n.startX = n.x;   // 출발점 고정
                        n.startY = n.y;
                    }

                    int elapsed = t - n.captureStartTime;
                    // 전체 이동 시간 (ms)
                    float duration = 700f;

                    // 0 ~ 1 사이 진행도
                    float rawP = Math.min(1f, elapsed / duration);

                    // 이징 적용 (처음엔 천천히, 나중에 빠르게 → 더 자연스러움)
                    float p = rawP * rawP * rawP; // p^3 사용 (원하면 p^2로 바꿔도 됨)

                    // 🎯 출발점: 캡쳐 시점 위치
                    float p0x = n.startX;
                    float p0y = n.startY;

                    // 🎯 도착점: UFO / 라면 그릇 위치
                    float p2x = ufoTargetX;
                    float p2y = ufoTargetY;

                    // 🎯 중간 제어점: 살짝 위로 휘어 들어가게
                    float p1x = (p0x + p2x) / 2f;
                    float p1y = Math.min(p0y, p2y) - 120;  // 곡선 튀는 정도 조절

                    float u = 1f - p;

                    // ✔ 부드러운 2차(또는 3차) 곡선 경로
                    n.x = u * u * p0x + 2 * u * p * p1x + p * p * p2x;
                    n.y = u * u * p0y + 2 * u * p * p1y + p * p * p2y;

                    // 도착 후 안 보이게
                    if (p >= 1f) {
                        n.visible = false;
                    }
                }
            }
        }




        // ===================== 53초 구간 전환 =====================
        if (!phaseChangedAt53 && t >= PHASE_CHANGE_TIME_53) {
            phaseChangedAt53 = true;

            // 1) 기존 Stage2 오브젝트 정리
            blackholeVisible = false;      // 블랙홀 숨기기
            noodles.clear();               // 날아다니는 면발 제거
            bannerVisible = false;         // 배너 숨기기
            // 필요하면 공기포 이미지도 정리
            boomLeftImage = null;
            boomRightImage = null;
            leftBoomActive = false;
            rightBoomActive = false;

        }

        // 🔚 맨 마지막에 힌트 이미지 갱신
        updateKeyGuideByTime(t);

    }


    @Override
    public void drawStageObjects(Graphics g) {
        // 1. 고양이 손
        g.drawImage(currentUser, 0, 0, null);

        // 2. 배너
        if (bannerVisible && stage2Banner != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            int targetWidth = 300;
            int targetHeight = 250;
            int x = (getWidth() - targetWidth) / 2;
            int y = 50;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(stage2Banner, x, y, targetWidth, targetHeight, null);
            g2.dispose();
        }


        // 🔵4. 블랙홀 GIF (배너와 함께 등장, 점점 커짐)
        if (blackholeVisible && blackholeGif != null) {
            Graphics2D g2 = (Graphics2D) g.create();

            int originalW = blackholeGif.getIconWidth();
            int originalH = blackholeGif.getIconHeight();

            // 스케일 적용
            int drawW = (int) (originalW * blackholeScale);
            int drawH = (int) (originalH * blackholeScale);

            // 기준 위치: 화면 중앙 기준 + 오프셋
            int baseX = getWidth() / 2 - 370  ;
            int baseY = getHeight() / 2 - 270 ;

            int x = baseX - drawW / 2 + blackholeOffsetX;
            int y = baseY - drawH / 2 + blackholeOffsetY;

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(blackholeGif.getImage(), x, y, drawW, drawH, this);
            g2.dispose();
        }

        // 공기포 - 왼쪽
        if (boomLeftImage != null) {
            int imgW = boomLeftImage.getWidth(this);
            int imgH = boomLeftImage.getHeight(this);

            // 예시: 화면 중앙 기준 왼쪽
            int x = getWidth() / 2 - imgW + 485 ;
            int y = getHeight() / 2 - imgH / 2 - 50;

            g.drawImage(boomLeftImage, x, y, imgW, imgH, this);
        }

        // 공기포 - 오른쪽
        if (boomRightImage != null) {
            int imgW = boomRightImage.getWidth(this);
            int imgH = boomRightImage.getHeight(this);

            // 예시: 화면 중앙 기준 오른쪽
            int x = getWidth() /2 - 485 ;
            int y = getHeight() / 2 - imgH / 2 - 50;

            g.drawImage(boomRightImage, x, y, imgW, imgH, this);
        }

        // ✅ 4. 키 힌트 이미지 (화면 오른쪽 아래에 예시로 표시)
        if (currentKeyGuideImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();

            float alpha = 0.7f; // 0.0 = 완전 투명, 1.0 = 불투명
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            float scale = 0.3f; // 30% 크기로 줄이기 (원하면 0.2, 0.4 등 조절)
            int w = (int)(currentKeyGuideImage.getWidth(this) * scale);
            int h = (int)(currentKeyGuideImage.getHeight(this) * scale);

            int padding = 40;
            int x = getWidth() - w - padding;
            int y = getHeight() - h - padding;

            g2.drawImage(currentKeyGuideImage, x, y, w, h, this);

            g2.dispose();
        }

        // 외계인 손
        if (currentAlien != null) {
            g.drawImage(currentAlien, 0, 0, getWidth(), getHeight(), null);
        }
    }

    @Override
    protected void drawStageObjectsUnderController(Graphics g) {
        // 🔹 컨트롤러 아래 레이어에 그릴 오브젝트: 면발만
        //    (블랙홀도 아래로 내리고 싶으면 여기로 옮겨도 됨)

        for (Noodle n : noodles) {
            if (!n.visible) continue;

            int w = (int)(noodleImage.getWidth(this) * n.scale);
            int h = (int)(noodleImage.getHeight(this) * n.scale);

            // 중심 기준 회전을 위해 (0,0)을 면발 중심으로 맞춰서 그릴 것
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                // 🔹 면발 중심으로 이동
                g2.translate(n.x, n.y);

                // 🔹 45도 회전 (시계 방향). 반대로 기울이고 싶으면 -45로 바꿔도 됨
                g2.rotate(Math.toRadians(45));

                // 🔹 중심 기준으로 이미지 그리기
                g2.drawImage(noodleImage, -w / 2, -h / 2, w, h, this);
            } finally {
                g2.dispose();
            }
        }
    }


    @Override
    public Image getCannon() {
        return cannon;
    }

    @Override
    protected void changeStageImageOnPress() {
        if (currentUser == cat1) this.currentUser = cat2;
    }

    @Override
    protected void changeStageImageOnRelease() {
        if (currentUser == cat2) this.currentUser = cat1;
    }

    @Override
    protected void processStageEvents(int t) {
        if (t < ALIEN_APPEAR_TIME_1 && currentAlien != null) { currentAlien = null; }

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
        // 53초 이후에는 리듬 입력 막기 (필요 없으면 그대로 false 유지)
        return phaseChangedAt53;
    }
}
