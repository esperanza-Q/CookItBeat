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

    // 스테이지3에서 사용할 전체 진행 시각
    public static int progressTime = 0;

    // ✅ [추가] 스테이지 간에 공유할 점수 변수
    private static int totalScore = 0;

    // ✅ 추가: 판정 카운트 누적용
    private static int perfectCount = 0;
    private static int goodCount = 0;
    private static int missCount = 0;

    // ✅ 누적 추가 메서드
    public static void addPerfect() { perfectCount++; }
    public static void addGood()    { goodCount++; }
    public static void addMiss()    { missCount++; }

    // ✅ Getter
    public static int getPerfectCount() { return perfectCount; }
    public static int getGoodCount()    { return goodCount; }
    public static int getMissCount()    { return missCount; }





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

        ufoImagePath = "../images/alienStage_image/ufo.png"; // ✅ UFO도 기본으로

        totalScore = 0;
        perfectCount = 0;
        goodCount = 0;
        missCount = 0;
    }

    // ✅ 타이머 설정 (StageManager에 한 번만 생성)
    public static void setupSyncTimer() {
        if (syncTimer == null) {
            syncTimer = new Timer(16, e -> {
                if (spaceBackgroundMusic == null) return;

                int t = spaceBackgroundMusic.getTime();
                progressTime = t;

                SpaceAnimation current = getCurrentStage();
                if (current != null) {
                    current.updateByMusicTime(t);
                }

                // ✅ 음악 스레드가 끝났으면 여기서 결과 화면 요청
                if (!spaceBackgroundMusic.isAlive() && current != null) {
                    current.onMusicEnded();   // 아래 2번에서 추가할 메서드
                    syncTimer.stop();         // 타이머도 종료
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

    // ✅ [추가] 점수를 설정하는 메서드
    public static void setTotalScore(int score) {
        totalScore = score;
    }

    // ✅ [추가] 점수를 가져오는 메서드
    public static int getTotalScore() {
        return totalScore;
    }


    // ✅ UFO 이미지 경로 (스테이지 전환용)
    private static String ufoImagePath = "../images/alienStage_image/ufo.png";

    // ✅ Setter / Getter
    public static void setUfoImagePath(String path) {
        ufoImagePath = path;
    }
    public static String getUfoImagePath() {
        return ufoImagePath;
    }

}
