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
        float x, y;
        float scale = 0.7f;

        boolean visible = true;
        boolean captured = false;

        int startTime = -1;
        int captureStartTime = -1;

        float vx;
        float vy;

        float startX;
        float startY;
        boolean goRight;

        int frameIndex = 0;
        int lastFrameTime = -1;

        // 🔹 이 면발이 담당하는 노트 구간 (USER_PRESS_TIMES_INT 기준 인덱스)
        int firstNoteIndex;   // 포함
        int lastNoteIndex;    // 포함

        int successCount = 0; // Good/Perfect 횟수
        boolean failed = false; // 이 턴에서 Miss 한 번이라도 나면 true
    }





    // 🔵 Stage2 전용 블랙홀 GIF
    private ImageIcon blackholeGif;
    private boolean blackholeVisible = false;

    // 블랙홀 등장 애니메이션 (커지면서 등장)
    private long blackholeStartTimeMs = -1;      // 언제부터 키우기 시작했는지
    private float blackholeScale = 0.1f;         // 시작 스케일
    private static final float BLACKHOLE_MAX_SCALE = 0.7f;   // 최종 스케일
    private static final int BLACKHOLE_GROW_DURATION = 1000; // 몇 ms 동안 커질지 (1초)


    // 공기포 크기 조절 (1.0f = 원본 크기)
    private float boomScale = 0.7f;   // 70% 크기

    // 53초 구간 전환 타이밍 (53.139초)
    private static final int PHASE_CHANGE_TIME_53 = 53139;  // 53.139 * 1000

    // 🔹 키 가이드 고정 타이밍 (초 단위)
    private static final double[] GUIDE_TIMES_SEC = {
            27.0,   // 27초에 첫 가이드
            34,   // 34초에 두 번째 가이드
            39.7,   // ...

    };

    // 플레이어 입력 보정 (ms)
    // +면 판정선을 뒤로(늦게), -면 앞으로(일찍) 이동
    private static final int[] USER_INPUT_BIAS_MS = {
            -40,  // musicIndex 0 : 평균 40ms 정도 일찍 침
            0,    // musicIndex 1 : 아직 데이터 없으면 0
            80,   // musicIndex 2 : 이전 로그 기준 80ms 정도 늦게 침
            0     // musicIndex 3
    };


    // 🔹 각 시간에 어떤 키를 보여줄지
    private static final int[] GUIDE_KEYS = {
            KeyEvent.VK_A,   // 27.0초에는 A 키 가이드
            KeyEvent.VK_D,   // 34.5초에는 D 키 가이드
            KeyEvent.VK_W,   // 40.0초에는 W 키 가이드

    };

    // 👉 인스턴스용 ms 배열
    private final int[] guideTimesMs;



    // 🔹 각 가이드가 화면에 유지될 시간 (ms)
    private static final int GUIDE_SHOW_DURATION_MS = 2500;  // 1.5초 동안 표시



    private boolean phaseChangedAt53 = false;

    //면발 크기 조절
    private float noodleScale = 0.5f;   // 50% 크기


    // 생성 효과음이 이미 재생됐는지
    private boolean blackholeSpawnSfxPlayed = false;

    // 🔧 위치 조정용 오프셋 (원하는 값으로 수정해서 쓰면 됨)
    private int blackholeOffsetX = 0;   // +면 오른쪽, -면 왼쪽
    private int blackholeOffsetY = 100; // +면 아래, -면 위

    // ===================== 면발 궤도 관련 =====================
    private static final int NOODLE_FRAME_COUNT = 4;   // 🔹 프레임 개수

    // 면발 애니메이션 프레임 (오른쪽 아래, 왼쪽 아래)
    private Image[] noodleRightFrames = new Image[NOODLE_FRAME_COUNT];
    private Image[] noodleLeftFrames  = new Image[NOODLE_FRAME_COUNT];

    // 현재 면발 위치
    private float noodleX;
    private float noodleY;

    // 좌우로 움직일 때의 속도 (px/sec)
    private float noodleSpeed = 200f;   // 필요하면 나중에 조정
    private int noodleDir = 1;          // 1: 오른쪽, -1: 왼쪽

    // 상태
    private boolean noodleVisible = true;
    private boolean noodleCaptured = false;    // UFO에 끌려가는 중인지


    // 면발 생성 한 번만 실행하기 위한 플래그
    private boolean noodleSpawn1 = false;
    private boolean noodleSpawn2 = false;
    private boolean noodleSpawn3 = false;
    private boolean noodleSpawn4 = false;


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
        int[] noteTimes = userPressTimesMs;

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

    // 🔹 현재 시간 t 기준으로 근처 노트 인덱스만 가져오기
    private int getNearestNoteIndexForNow(int windowMs) {
        return getNearestNoteIndex(currentMusicTimeMs, windowMs);
    }


    private int[] noteKeys;

    // ======== 🔹 슬로우 구간 정보 (초 단위)
    private static final double SLOW1_END_SEC = 31.050;  // 슬로우1 끝
    private static final double SLOW2_END_SEC = 48.055;  // 슬로우2 끝

    // 🔹 슬로우 이후 밀림량 (ms)
    private static final int OFFSET_AFTER_SLOW1_MS = 609;  // 0.609초
    private static final int OFFSET_AFTER_SLOW2_MS = 477;  // 0.477초

    private static int safeMusicIndex() {
        int idx = StageManager.musicIndex;
        if (idx < 0 || idx >= USER_PRESS_TIMES_SEC_BY_MUSIC.length) {
            idx = 0;
        }
        return idx;
    }


    // 🔹 논리 시간(sec)을 실제 판정 시간(ms)로 변환
    private static int toJudgeMs(double tSec) {
        int base = (int) Math.round(tSec * 1000.0);

        int idx = StageManager.musicIndex;
        if (idx < 0) {
            return base;
        }

        int result = base;

        switch (idx) {
            case 0:
                // 0번 곡은 슬로우 없음
                break;

            case 1:
                if (tSec > SLOW1_END_SEC) {
                    result = base + OFFSET_AFTER_SLOW1_MS;
                }
                break;

            case 2:
                if (tSec > SLOW2_END_SEC) {
                    result = base + OFFSET_AFTER_SLOW2_MS;
                }
                break;

            default:
                break;
        }

        // ✅ 마지막에 사용자 입력 편차 보정 적용
        if (idx >= 0 && idx < USER_INPUT_BIAS_MS.length) {
            result += USER_INPUT_BIAS_MS[idx];
        }

        return result;
    }


    // 슬로우 보정 없이 초 → ms 만
    private static int[] toMs(double[] secs) {
        int[] result = new int[secs.length];
        for (int i = 0; i < secs.length; i++) {
            result[i] = (int) Math.round(secs[i] * 1000.0);
        }
        return result;
    }


    // 🔹 더블 배열(초)을 ms 배열로 한 번에 변환
    private static int[] buildJudgeTimes(double[] secs) {
        int[] result = new int[secs.length];
        for (int i = 0; i < secs.length; i++) {
            result[i] = toJudgeMs(secs[i]);  // ← 여기서 슬로우/밀림을 반영
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


    private final int inputEnableTimeMs;

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
    //외계인이 자동으로 “눌렀다”고 연출되는 시
    // 곡별 외계인 손 타이밍 (초 단위)
    private static final double[][] ALIEN_PRESS_TIMES_SEC_BY_MUSIC = {
            // musicIndex = 0용
            {
                    28.285, 28.505, 28.725,
                    31.675, 31.995,
                    35.146, 35.366, 35.576,
                    41.793, 42.002, 43.282, 43.502, 43.722, 43.942, 45.135, 45.559, 46.083
            },
            // musicIndex = 1용 (변화O)
            {
                    28.285, 28.505, 28.725,
                    32.100, 32.320,
                    35.055, 35.375, 35.495,
                    42.202, 42.411, 43.691, 43.911, 44.131, 44.351, 45.944, 46.268, 46.692
            },
            // musicIndex = 2용 (4번쨰 변화)
            {
                    28.285, 28.505, 28.725,
                    31.675, 31.995,
                    35.146, 35.366, 35.576,
                    41.793, 42.002, 43.282, 43.502, 43.722, 43.942, 45.435, 45.859, 46.583
            },
            // musicIndex = 3용
            {
                    28.285, 28.505, 28.725,
                    31.675, 31.995,
                    35.146, 35.366, 35.576,
                    41.793, 42.002, 43.282, 43.502, 43.722, 43.942, 45.135, 45.559, 46.083
            }
    };


    // ✅ 인스턴스용
    private final int[] alienPressTimesMs;


    // ✅ 판정 정답 타이밍 (SpaceAnimation에 넘기는 타이밍)
    // ✅ 논리적인 노트 시간 (초 단위) — DAW에서 읽은 값 그대로
    // 음악마다 다른 리듬 판정 타이밍 (초 단위)
    private static final double[][] USER_PRESS_TIMES_SEC_BY_MUSIC = {

            // musicIndex = 0
            {
                    29.983, 30.203, 30.423,
                    33.410, 33.850,
                    37.718, 37.928, 38.138,
                    48.649, 48.858, 50.138, 50.358, 50.578, 50.798, 52.290, 52.715, 53.139
            },

            // musicIndex = 1
            {
                    29.983, 30.203, 30.423,
                    34.019, 34.459,
                    38.327, 38.537, 38.747,
                    49.258, 49.467, 50.747, 50.967, 51.187, 51.407, 52.899, 53.324, 53.748

            },

            // musicIndex = 2
            {
                    29.983, 30.203, 30.423,
                    33.410, 33.850,
                    37.718, 37.928, 38.138,
                    49.126, 49.335, 50.615, 50.835, 51.055, 51.275, 52.767, 53.192, 53.616

            },

            // musicIndex = 3
            {
                    29.983, 30.203, 30.423,
                    33.410, 33.850,
                    37.718, 37.928, 38.138,
                    48.649, 48.858, 50.138, 50.358, 50.578, 50.798, 52.290, 52.715, 53.139
            }
    };


    // ✅ 인스턴스용 ms 배열
    private final int[] userPressTimesMs;




    // 딴 패턴이 시작하는 시점(첫 딴 타이밍, 초 단위)
    private static final double[][] DDAN_START_TIMES_SEC_BY_MUSIC = {

            // musicIndex 0
            {
                    29.983, 33.410, 37.718, 48.649
            },

            // musicIndex 1
            {
                    29.983, 33.410, 37.718, 48.649
            },

            // musicIndex 2
            {
                    29.983, 33.410, 37.718, 48.649
            },

            // musicIndex 3
            {
                    29.983, 33.410, 37.718, 48.649
            },
    };




    // 외계인 손 release
    private final int[] alienReleaseTimes;


    // ✅ 각 노트 타이밍에 대한 "정답 키" 배열
// USER_PRESS_TIMES_INT와 길이가 같아야 함
    // USER_PRESS_TIMES_SEC와 길이 100% 동일해야 함
    // 곡마다 정답 키 패턴
    private static final int[][] NOTE_KEYS_BY_MUSIC = {

            // musicIndex = 0
            {
                    KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,   // 0,1,2 → A
                    KeyEvent.VK_D, KeyEvent.VK_D,                 // 3,4   → D 로 변경
                    KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,  // 5,6,7 → D
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,  // 8,9,10 → W
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,  // 11,12,13 → W
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W   // 14,15,16 → W
            },

            // musicIndex = 1
            {
                    KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,   // 0,1,2 → A
                    KeyEvent.VK_D, KeyEvent.VK_D,             // 3,4   → D 로 변경
                    KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,  // 5,6,7 → D
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,  // 8,9,10 → W
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,  // 11,12,13 → W
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W   // 14,15,16 → W
            },

            // musicIndex = 2
            {
                    KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,   // 0,1,2 → A
                    KeyEvent.VK_D, KeyEvent.VK_D,             // 3,4   → D 로 변경
                    KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,  // 5,6,7 → D
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,  // 8,9,10 → W
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,  // 11,12,13 → W
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W   // 14,15,16 → W
            },

            // musicIndex = 3
            {
                    KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.VK_A,   // 0,1,2 → A
                    KeyEvent.VK_D, KeyEvent.VK_D,           // 3,4   → D 로 변경
                    KeyEvent.VK_D, KeyEvent.VK_D, KeyEvent.VK_D,  // 5,6,7 → D
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,  // 8,9,10 → W
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W,  // 11,12,13 → W
                    KeyEvent.VK_W, KeyEvent.VK_W, KeyEvent.VK_W   // 14,15,16 → W
            },
    };


    // 외계인 손이 alien2로 바뀐 후 돌아오는 타이밍
    private final int ALIEN_RELEASE_DELAY_MS = 50;

    // int[] -> long[] 변환 헬퍼
    private static long[] convertToLongArray(int[] array) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }


    public SpaceStage2() {
        // ✅ super는 "무조건" 첫 줄 + static 메서드/상수만 사용
        super(convertToLongArray(
                buildJudgeTimes(
                        USER_PRESS_TIMES_SEC_BY_MUSIC[safeMusicIndex()]
                )
        ));

        // ⬇️ 이제 여기부터는 자유롭게 지역 변수 써도 됨

        int mi = safeMusicIndex();

        // 음악별 USER TIME 선택
        double[] selectedUserTimes = USER_PRESS_TIMES_SEC_BY_MUSIC[mi];
        this.userPressTimesMs = buildJudgeTimes(selectedUserTimes);

        // 음악별 NOTE_KEYS 선택
        this.noteKeys = NOTE_KEYS_BY_MUSIC[mi];

        // ---- 여기부터 곡별 외계인 타이밍 선택 ----
        int idx = StageManager.musicIndex;
        if (idx < 0 || idx >= ALIEN_PRESS_TIMES_SEC_BY_MUSIC.length) {
            idx = 0; // 안전용 디폴트
        }
        double[] alienRaw = ALIEN_PRESS_TIMES_SEC_BY_MUSIC[idx];
        // 슬로우 보정 X, 그냥 mp3 기준 시간 그대로
        this.alienPressTimesMs = toMs(alienRaw);

        // ---- 여기까지 ----

        this.guideTimesMs      = buildJudgeTimes(GUIDE_TIMES_SEC);

        this.inputEnableTimeMs = userPressTimesMs[0] - 50;

        disableSpaceKeyFromBase();

        this.alienReleaseTimes = new int[alienPressTimesMs.length];
        for (int i = 0; i < alienPressTimesMs.length; i++) {
            alienReleaseTimes[i] = alienPressTimesMs[i] + ALIEN_RELEASE_DELAY_MS;
        }


        // 이미지 로드
        alien1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/hologram_alien1.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/hologram_alien2.png")).getImage();
        cat1   = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand01.png")).getImage();
        cat2   = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand02.png")).getImage();
        cannon = new ImageIcon(Main.class.getResource("../images/alienStage_image/cannon01.png")).getImage();
        currentUser = cat1;

        stage2Banner = new ImageIcon(Main.class.getResource("../images/alienStage_image/space_stage2.png")).getImage();

        // 🔸 면발 애니메이션 이미지 로드 (R1~R4, L1~L4)
        for (int i = 0; i < NOODLE_FRAME_COUNT; i++) {
            var urlR = Main.class.getResource("../images/alienStage_image/noodle_R" + (i + 1) + ".png");
            var urlL = Main.class.getResource("../images/alienStage_image/noodle_L" + (i + 1) + ".png");

            if (urlR == null) {
                System.err.println("noodle_R" + (i + 1) + ".png 못 찾음");
            } else {
                noodleRightFrames[i] = new ImageIcon(urlR).getImage();
            }

            if (urlL == null) {
                System.err.println("noodle_L" + (i + 1) + ".png 못 찾음");
            } else {
                noodleLeftFrames[i] = new ImageIcon(urlL).getImage();
            }
        }



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



        // ✅ Stage2 전용: WASD 눌렀을 때만 판정 + Boom 실행
        // ✅ Stage2 전용: WASD 눌렀을 때만 판정 + Boom 실행
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                // 🔹 사용자가 누른 키 로그
                System.out.println("[KEY] Pressed: " + KeyEvent.getKeyText(code) + " (code=" + code + "), time=" + currentMusicTimeMs);

                boolean fireLeft = false;
                boolean fireRight = false;

                // 🔊 효과음 + 공기포 방향
                switch (code) {
                    case KeyEvent.VK_A:
                        playShotSfx();
                        fireLeft = true;
                        break;
                    case KeyEvent.VK_D:
                        playShotSfx();
                        fireRight = true;
                        break;
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_S:
                        playShotSfx();
                        fireLeft = true;
                        fireRight = true;
                        break;
                    default:
                        // WASD 말고 다른 키는 그냥 무시 (판정도 안 함)
                        return;
                }

                changeStageImageOnPress();
                repaint();
                startBoomAnimation(fireLeft, fireRight);

                // ===== 여기부터 리듬 판정 =====

                // 0) 시간 블록되면 바로 MISS
                if (isTimeInputBlocked()) {
                    registerMissFromStage2(-1);
                    return;
                }

                boolean isHit = false;    // 기본값: 실패
                int targetNoteIndex = -1; // 어느 노트를 기준으로 MISS/HIT 할지

                // 1) 아직 입력 허용 시간 전 → 강제 MISS
                if (currentMusicTimeMs < inputEnableTimeMs) {
                    registerMissFromStage2(-1);
                    return;
                }

                // 2) 지금 시간 근처 노트 찾기
                int noteIdx = getNearestNoteIndexForNow(NOTE_SEARCH_WINDOW_MS);
                targetNoteIndex = noteIdx;

                if (noteIdx >= 0) {
                    int expectedKey = noteKeys[noteIdx];
                    int noteTime    = userPressTimesMs[noteIdx];
                    int diff        = Math.abs(currentMusicTimeMs - noteTime);

                    // 🔹 디버그용 로그
                    System.out.println("Input Key: " + KeyEvent.getKeyText(code)
                            + " | Expected: " + KeyEvent.getKeyText(expectedKey));
                    System.out.println("Input Time: " + currentMusicTimeMs);
                    System.out.println("Closest Correct Time: " + noteTime);
                    System.out.println("Measured Difference (minDiff): " + diff);
                    System.out.println("------------------------------------");


                    // ✔︎ "박자에 맞고" + "맞는 키" 인 경우에만 HIT
                    if (code == expectedKey && diff <= JUDGE_GOOD_MS) {
                        // ✅ Stage1과 동일한 판정 시스템 사용 (Perfect/Good/Miss 결정)
                        SpaceStage2.super.processSpaceKeyPressLogic();

                        // ✅ 면발 턴에 성공 1개 기록
                        registerHitToNoodleTurn(noteIdx);
                        isHit = true;
                    }
                }

                // 3) 위 조건을 통과 못했다면 전부 MISS
                if (!isHit) {
                    registerMissFromStage2(targetNoteIndex);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
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
        if (currentMusicTimeMs < inputEnableTimeMs) return false;

        final int LARGE_WINDOW_MS = 1500;

        int idx = getNearestNoteIndex(currentMusicTimeMs, LARGE_WINDOW_MS);
        if (idx == -1) return false;

        int noteTime    = userPressTimesMs[idx];
        int expectedKey = noteKeys[idx];


        return keyCode == expectedKey;
    }




    // 🔻 Stage2 에서 오답일 때 강제 MISS + 턴 실패
    private void registerMissFromStage2(int noteIndex) {
        // 1) 리듬 MISS 등록 (MISS 텍스트)
        registerForcedMiss();

        // 1.5) 점수 MISS 반영
        if (judgementManager != null) {
            judgementManager.forceMiss(currentMusicTimeMs);
        }

        // 2) 이 노트를 담당하는 면발 턴을 실패로 표시
        if (noteIndex < 0) return;

        for (Noodle n : noodles) {
            if (!n.visible) continue;
            if (n.captured) continue;
            if (n.failed) continue;

            if (noteIndex >= n.firstNoteIndex && noteIndex <= n.lastNoteIndex) {
                n.failed = true;
                // 실패한 면발은 그냥 계속 떨어지게 둘지, 바로 사라지게 할지 선택
                // 예: n.visible = false; 하면 바로 사라짐
                break;
            }
        }
    }


    // 블랙홀 애니메이션 리셋 (크기/시간 초기화)
    private void resetBlackhole(int t) {
        blackholeVisible = true;
        blackholeStartTimeMs = t;
        blackholeScale = 0.09f;  // 처음에는 작게
    }

    private static final float NOODLE_SPEED_X = 40f;  // 좌우 속도
    private static final float NOODLE_SPEED_Y = 60f;  // 아래로 속도
    private static final int   NOODLE_FRAME_DELAY_MS = 300; // 프레임 전환 간격


    // 🔹 근처 노트를 찾을 시간 범위 (±500ms 안에 있는 노트만 대상으로)
    private static final int NOTE_SEARCH_WINDOW_MS = 500;

    // 🔹 이 안에 들어오면 "성공"으로 볼 시간 범위 (원하는 대로 조절)
    private static final int JUDGE_GOOD_MS = 230;   // ±230ms


    // 🔵 수정된 spawnNoodle
    private void spawnNoodle(int currentTime, boolean goRight, int firstNoteIdx, int lastNoteIdx) {
        if (blackholeGif == null) return;

        int originalW = blackholeGif.getIconWidth();
        int originalH = blackholeGif.getIconHeight();

        int drawW = (int) (originalW * blackholeScale);
        int drawH = (int) (originalH * blackholeScale);

        int baseX = getWidth() / 2 - 370;
        int baseY = getHeight() / 2 - 270;

        int x = baseX - drawW / 2 + blackholeOffsetX;
        int y = baseY - drawH / 2 + blackholeOffsetY;

        int centerX = x + drawW / 2 + 400;
        int centerY = y + drawH / 2 + 80;

        Noodle n = new Noodle();
        n.x = centerX;
        n.y = centerY;
        n.startTime = currentTime;
        n.goRight = goRight;
        n.vx = 0f;
        n.vy = NOODLE_SPEED_Y;

        n.frameIndex = 0;
        n.lastFrameTime = currentTime;

        // 🔹 이 면발이 어떤 노트들을 담당하는지 저장
        n.firstNoteIndex = firstNoteIdx;
        n.lastNoteIndex = lastNoteIdx;

        noodles.add(n);
    }




    // ✅ 키 가이드
    private void updateKeyGuideByTime(int t) {
        if (phaseChangedAt53) {
            currentKeyGuideImage = null;
            return;
        }

        currentKeyGuideImage = null;

        for (int i = 0; i < guideTimesMs.length; i++) {
            int start = guideTimesMs[i];
            int end   = start + GUIDE_SHOW_DURATION_MS;

            if (t >= start && t <= end) {
                int keyCode = GUIDE_KEYS[i];
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
                }
                return;
            }
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

    // ✅ Stage2용: 정답 키일 때만 부모 판정 + 턴 체크
    private void triggerJudgeAndBoomFromStage2(int noteIndex) {
        // 부모(SpaceAnimation)의 판정/점수 처리
        SpaceStage2.super.processSpaceKeyPressLogic();

        // 🔹 이 판정이 속한 면발 턴에 “성공 한 개” 추가
        registerHitToNoodleTurn(noteIndex);
    }

    // 이 노트를 담당하는 면발을 찾아서 성공 카운트 올리고,
    // 턴 전체가 다 성공하면 그제서야 captured = true
    private void registerHitToNoodleTurn(int noteIndex) {
        if (noteIndex < 0) return;

        for (Noodle n : noodles) {
            if (!n.visible) continue;
            if (n.captured) continue;
            if (n.failed) continue;

            if (noteIndex >= n.firstNoteIndex && noteIndex <= n.lastNoteIndex) {
                n.successCount++;

                int required = n.lastNoteIndex - n.firstNoteIndex + 1;
                if (n.successCount >= required) {
                    // 🔥 이 턴의 모든 노트를 Good/Perfect로 맞췄다고 보고 UFO로 이동 시작
                    n.captured = true;
                }
                break;
            }
        }
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
        for (int pressTime : alienPressTimesMs) {   // ✅ ALIEN_PRESS_TIMES_INT → alienPressTimesMs
            if (t >= pressTime && t < pressTime + 50) {
                if (currentAlien == alien1) currentAlien = alien2;
                break;
            }
        }
        for (int releaseTime : alienReleaseTimes) { // ✅ ALIEN_RELEASE_TIMES → alienReleaseTimes
            if (t >= releaseTime && t < releaseTime + 50) {
                if (currentAlien == alien2) currentAlien = alien1;
                break;
            }
        }

        // ===== 면발 생성  타이밍 =====
        // 외계인 예시에는 안생기고 내 박자에만 생성
        // 29.000초 근처에서 한 번만
        if (!noodleSpawn1 && t >= 29000) {
            spawnNoodle(t, false, 0, 2);
            noodleSpawn1 = true;
        }

        // 32.000초 근처에서 한 번만
        if (!noodleSpawn2 && t >= 32000) {
            spawnNoodle(t, false, 3, 5);
            noodleSpawn2 = true;
        }

        // 36.800초 근처에서 한 번만 (예: 10~12번 노트 담당 이런 식)
        if (!noodleSpawn3 && t >= 36800) {
            spawnNoodle(t, true, 10, 12);
            noodleSpawn3 = true;
        }


        // 48.000초 근처에서 한 번만 (오른쪽/왼쪽에 각각 다른 범위)
        if (!noodleSpawn4 && t >= 48000) {
            spawnNoodle(t, false, 16, 17);  // 예: 첫 면발
            spawnNoodle(t, true, 18, 22); // 예: 두 번째 면발
            noodleSpawn4 = true;
        }


        // ===== 프레임 간 시간 계산 (ms) =====
        int dt = 0;
        if (lastUpdateTimeMs < 0) {
            lastUpdateTimeMs = t;
        } else {
            dt = t - lastUpdateTimeMs;
            lastUpdateTimeMs = t;
        }

        // ===================== 면발 이동 + 프레임 애니메이션 =====================
        if (dt > 0) {
            float dtSec = dt / 1000f;

            for (Noodle n : noodles) {

                if (!n.visible) continue;

                if (!n.captured) {
                    // 🔹 캡처되지 않은 상태: 방향대로 직선 이동
                    n.x += n.vx * dtSec;
                    n.y += n.vy * dtSec;

                    // 🔹 프레임 애니메이션 (R1→R2→R3, L1→L2→L3)
                    if (n.lastFrameTime < 0) {
                        n.lastFrameTime = t;
                    }
                    if (t - n.lastFrameTime >= NOODLE_FRAME_DELAY_MS) {
                        n.frameIndex = (n.frameIndex + 1) % NOODLE_FRAME_COUNT;
                        n.lastFrameTime = t;
                    }

                    // 화면 밖으로 나가면 제거
                    int margin = 100;
                    if (n.x < -margin || n.x > getWidth() + margin || n.y > getHeight() + margin) {
                        n.visible = false;
                    }

                } else {
                    // 🔹 캡쳐된 상태: UFO 쪽으로 빨려들어가는 애니메이션 (기존 곡선 로직 유지)
                    if (n.captureStartTime < 0) {
                        n.captureStartTime = t;
                        n.startX = n.x;
                        n.startY = n.y;
                    }

                    int elapsed = t - n.captureStartTime;
                    float duration = 700f;
                    float rawP = Math.min(1f, elapsed / duration);
                    float p = rawP * rawP * rawP;

                    float p0x = n.startX;
                    float p0y = n.startY;
                    float p2x = ufoTargetX;
                    float p2y = ufoTargetY;
                    float p1x = (p0x + p2x) / 2f;
                    float p1y = Math.min(p0y, p2y) - 120;

                    float u = 1f - p;

                    n.x = u * u * p0x + 2 * u * p * p1x + p * p * p2x;
                    n.y = u * u * p0y + 2 * u * p * p1y + p * p * p2y;

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
            int baseX = getWidth() / 2 - 330  ;
            int baseY = getHeight() / 2 - 310 ;

            int x = baseX - drawW / 2 + blackholeOffsetX;
            int y = baseY - drawH / 2 + blackholeOffsetY;

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(blackholeGif.getImage(), x, y, drawW, drawH, this);
            g2.dispose();
        }

        // 공기포 - 왼쪽
        if (boomLeftImage != null) {
            int origW = boomLeftImage.getWidth(this);
            int origH = boomLeftImage.getHeight(this);

            // 🔹 스케일 적용된 크기
            int drawW = (int) (origW * boomScale);
            int drawH = (int) (origH * boomScale);

            // 예시: 화면 중앙 기준 왼쪽
            int x = getWidth() / 2 - drawW + 260 ;
            int y = getHeight() / 2 - drawH / 2 + 30 ;

            g.drawImage(boomLeftImage, x, y, drawW, drawH, this);
        }

        // 공기포 - 오른쪽
        if (boomRightImage != null) {
            int origW = boomRightImage.getWidth(this);
            int origH = boomRightImage.getHeight(this);

            int drawW = (int) (origW * boomScale);
            int drawH = (int) (origH * boomScale);

            int x = getWidth() / 2  - 270 ;
            int y = getHeight() / 2 - drawH / 2 + 30  ;

            g.drawImage(boomRightImage, x, y, drawW, drawH, this);
        }


        // ✅ 4. 키 힌트 이미지 (화면 오른쪽 아래에 예시로 표시)
        if (currentKeyGuideImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();

            float alpha = 0.65f; // 0.0 = 완전 투명, 1.0 = 불투명
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            float scale = 0.18f; // 30% 크기로 줄이기 (원하면 0.2, 0.4 등 조절)
            int w = (int)(currentKeyGuideImage.getWidth(this) * scale);
            int h = (int)(currentKeyGuideImage.getHeight(this) * scale);

            int padding = 40;
            int x = getWidth() - w - padding - 550;
            int y = getHeight() - h - padding - 150;

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
        // 1) 면발 먼저 그리기
        for (Noodle n : noodles) {
            if (!n.visible) continue;

            Image[] frames = n.goRight ? noodleRightFrames : noodleLeftFrames;
            Image frame = frames[n.frameIndex];

            int w = (int)(frame.getWidth(this) * n.scale);
            int h = (int)(frame.getHeight(this) * n.scale);

            g.drawImage(frame, (int)(n.x - w / 2), (int)(n.y - h / 2), w, h, this);
        }

        // 2) 그 위에 cannon 직접 그리기 (여기서부터는 Stage2 전용)
        if (cannon != null) {
            g.drawImage(cannon, 0, 0, null);
        }
    }


    @Override
    public Image getCannon() {
        // Stage2에서는 부모가 cannon을 그리지 않도록 막기
        return null;
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
