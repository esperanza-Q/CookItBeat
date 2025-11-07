package game.Space;


import game.Music;

import javax.swing.*;


//음악 관련 세팅
public class StageManager {
    public static int musicIndex = -1;       // 0~3 중 랜덤 선택됨
    public static Music spaceBackgroundMusic;
    public static String spaceBackgroundMusicTitle;
    public static boolean ambushEnabled = false; // 기습 발동여부
    public static String length = "";
    public static int musicLengthMs = 0; // ✅ 음악 길이 (ms) 추가

    private static boolean isMusicStarted = false; // ✅ 플래그 추가

    private static Timer syncTimer; // ✅ 타이머를 StageManager로 이동 (static)

    private static SpaceAnimation currentStage;

    public static void setCurrentStage(SpaceAnimation stage) {
        currentStage = stage;
        // stage.startAnimation() 호출 제거: 타이머는 StageManager가 영구적으로 관리합니다.
    }

    public static SpaceAnimation getCurrentStage() {
        return currentStage;
    }

    // 초기 설정 함수
    public static void initializeStage() {
        // 음악 4개 중 하나 랜덤 선택
        musicIndex = (int) (Math.random() * 4);

        spaceBackgroundMusicTitle = bgmList[musicIndex];
        spaceBackgroundMusic = new Music(spaceBackgroundMusicTitle, false);



// ✅ 음악 길이 (ms) 계산 추가
        int len = Music.getMusicLength(spaceBackgroundMusicTitle);
        length = Music.formatTime(len);
        musicLengthMs = len * 1000; // 초를 밀리초로 변환


        // 기습 발생 여부 (원한다면 확률 조정 가능)
//        ambushEnabled = Math.random() < 0.5; // 50% 확률
    }

    // 음악 파일 목록
    public static String[] bgmList = {
            //기습X
            "spaceBackgroundMusic4.mp3",
            //기습O
            "spaceBackgroundMusic1.mp3",
            "spaceBackgroundMusic2.mp3",
            "spaceBackgroundMusic3.mp3"

    };

    // ✅ 게임 종료 및 초기화 시 호출할 메서드 (게임 껐다 켤 때 리셋)
    public static void resetGame() {
        if (spaceBackgroundMusic != null) {
            spaceBackgroundMusic.close(); // 이전 음악 스레드 종료
        }
        if (syncTimer != null) {
            syncTimer.stop(); // 타이머 중지
        }
        isMusicStarted = false;
        // musicLengthMs 등 필요한 static 변수를 여기서 초기화할 수 있습니다.
    }

    // ✅ 타이머 설정 (StageManager에 한 번만 생성)
    public static void setupSyncTimer() {
        if (syncTimer == null) {
            // 딜레이를 16ms로 변경 (약 60FPS)
            syncTimer = new Timer(16, e -> {
                if (spaceBackgroundMusic != null && spaceBackgroundMusic.isAlive()) {
                    int t = spaceBackgroundMusic.getTime();

                    // 현재 활성화된 스테이지에만 updateByMusicTime 호출
                    SpaceAnimation current = getCurrentStage();
                    if (current != null) {
                        current.updateByMusicTime(t);
                        // repaint는 updateByMusicTime 내부에서 우주선 위치 갱신 후 호출되거나,
                        // paintComponent가 별도의 Timer에 의해 호출되어야 합니다.
                        // SpaceAnimation 내부의 행성 타이머가 repaint를 담당하므로 여기서는 제거합니다.
                        // current.repaint(); // 이 부분을 제거합니다.
                    }
                }
            });
        }
    }

    // ✅ 음악 및 타이머 시작 (GameFrame에서 한 번 호출)
    public static void startMusicAndTimer() {
        setupSyncTimer(); // 타이머 준비
        if (spaceBackgroundMusic != null && !isMusicStarted) {
            spaceBackgroundMusic.start();
            syncTimer.start(); // 타이머 시작
            isMusicStarted = true;
        }
    }
}
