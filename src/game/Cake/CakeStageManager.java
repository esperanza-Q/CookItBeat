package game.Cake;

import game.Music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CakeStageManager {
    private static CakeStageData currentStageData;  // ✅ 추가

    // ✅ [추가] 판정 카운트
    private static int perfectCount = 0;
    private static int goodCount = 0;
    private static int missCount = 0;

    private static int currentStage = 0;
    private static int cumulativeScore = 0;
    public static List<CakeStageData> stageDataList;

    // ‼️ [복구] Music 객체를 저장할 필드 (CakePanel이 시작한 인스턴스를 저장)
    private static Music currentMusic;

    private static final String GAME_MUSIC_FILE = "cakeBackgroundMusic.mp3";

    private static final List<Long> STAGE_END_TIMES_MS = Arrays.asList(
            41000L,  // Stage 1-1 종료 시간 (40초)
            52000L,  // Stage 1-2 종료 시간 (9.5초)
            79000L, // Stage 2 종료 시간 (15.5초)
            // 오븐 구간 종료 시간 87000
            87000L,
            96000L, // Stage 3-1 종료 시간 (20.8초)
            108000L  // Stage 3-2 종료 시간 (26.0초, 최종 종료)
    );

    // 💡 [추가] 기습 스테이지 발생 여부 플래그
    private static boolean isSurpriseStageOccurred = false;

    // 💡 [추가] 기습 스테이지 발생 확률 (예: 30%)
    private static final double SURPRISE_CHANCE = 1.0;

    // 💡 [추가] 기습 스테이지 발생을 결정하는 메서드
    public static void checkSurpriseStage() {
        // 1-1 -> 1-2 넘어갈 때 한 번만 체크
        if (currentStage == 1) {
            if (Math.random() < SURPRISE_CHANCE) {
                isSurpriseStageOccurred = true;
                System.out.println("🚨 기습 스테이지 발생!");
            } else {
                isSurpriseStageOccurred = false;
                System.out.println("✅ 기습 스테이지 없이 진행.");
            }
        }
    }

    // 💡 [추가] 기습 스테이지 발생 여부 Getter
    public static boolean isSurpriseStageOccurred() {
        return isSurpriseStageOccurred;
    }

    // 💡 [추가] 다음 스테이지부터 사용할 음악 파일명 반환
    public static String getNextMusicFileName() {
        // Stage 2 (Stage 1-2)부터 사용할 음악 파일을 결정합니다.
        if (isSurpriseStageOccurred) {
            return "cakeBackgroundMusic_surpriseAfter.mp3";
        }
        // 기습 스테이지가 없으면, 원래 음악 파일의 뒷부분을 계속 사용합니다.
        return GAME_MUSIC_FILE;
    }

    private static void initializeStageData() {
        stageDataList = new ArrayList<>();
        List<Long> dummyTimings = Arrays.asList(0L);

        // ... (CakeStageData 초기화 로직 유지) ...
        stageDataList.add(new CakeStageData(1, "재료 손질 (꼭지)", GAME_MUSIC_FILE, "../images/cakeStage_image/stage1/Background_stage1-1.png", dummyTimings));
        stageDataList.add(new CakeStageData(2, "재료 손질 (넣기)", GAME_MUSIC_FILE, "../images/cakeStage_image/stage1/Background_stage1-2.png", dummyTimings));
        stageDataList.add(new CakeStageData(3, "반죽 만들기", GAME_MUSIC_FILE, "../images/cakeStage_image/stage2/dough_background_bowlO.png", dummyTimings));
        stageDataList.add(new CakeStageData(4, "오븐", GAME_MUSIC_FILE, "../images/cakeStage_image/oven/oven_background.png", dummyTimings));
        stageDataList.add(new CakeStageData(5, "몽환 케이크 데코 (휘핑)", GAME_MUSIC_FILE, "../images/cakeStage_image/stage3/Background2_stage3-1.png", dummyTimings));
        stageDataList.add(new CakeStageData(6, "몽환 케이크 데코 (딸기)", GAME_MUSIC_FILE, "../images/cakeStage_image/stage3/Background_stage3-2.png", dummyTimings));
    }

    public static void startFirstStage() {
        // ✅ stageDataList가 없으면 초기화
        if (stageDataList == null || stageDataList.isEmpty()) {
            initializeStageData();
        }

        currentStage = 1;
        currentStageData = stageDataList.get(0);  // ✅ 확정 세팅
    }


    // ‼️ [추가] CakePanel에서 시작된 Music 객체를 등록하는 Setter
    public static void setMusic(Music music) {
        currentMusic = music;
    }


    public static boolean nextStage() {
        currentStage++;

        if (currentStage <= stageDataList.size()) {
            currentStageData = stageDataList.get(currentStage - 1); // ✅ 갱신
            loadStage(currentStage);
            return true;
        } else {
            currentStage = stageDataList.size() + 1;
            return false;
        }
    }


    private static void loadStage(int stageNumber) {
        if (stageNumber > stageDataList.size() || stageNumber < 1) return;
    }

    // ‼️ [수정] Music 객체를 안전하게 종료
    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.close();
            currentMusic = null;
        }
    }

    public static void resetGame() {
        currentStage = 1;
        currentStageData = null;
        cumulativeScore = 0;
        perfectCount = goodCount = missCount = 0;

        // stageDataList는 유지할지/새로 로드할지 너 구조에 맞게
    }


    public static void resetScore() {
        cumulativeScore = 0;

        // ✅ [추가] 카운트도 리셋
        perfectCount = 0;
        goodCount = 0;
        missCount = 0;
    }

    // ✅ [추가] 카운트 증가
    public static void addPerfect() { perfectCount++; }
    public static void addGood()    { goodCount++; }
    public static void addMiss()    { missCount++; }

    // ✅ [추가] Getter
    public static int getPerfectCount() { return perfectCount; }
    public static int getGoodCount()    { return goodCount; }
    public static int getMissCount()    { return missCount; }

    // 💡 [추가] 누적 점수 설정/획득 Getter/Setter
    public static int getCumulativeScore() {
        return cumulativeScore;
    }

    // ‼️ [핵심 수정] 판정 매니저의 점수를 받아 전체 누적 점수를 업데이트
    public static void setCumulativeScore(int newScore) {
        cumulativeScore = newScore;
    }

    // ‼️ [복구] CakeAnimation에서 시간을 가져오기 위한 Getter
    public static Music getMusic() { return currentMusic; }
    public static int getCurrentStage() { return currentStage; }
    public static CakeStageData getCurrentStageData() {
        if (currentStageData != null) return currentStageData; // ✅ 우선 반환

        if (stageDataList == null || stageDataList.isEmpty()) return null;
        int idx = Math.max(0, currentStage - 1);
        if (idx >= stageDataList.size()) idx = stageDataList.size() - 1;
        return stageDataList.get(idx);
    }



    // 💡 Stage 1-1의 끝 시간 오프셋 값 (기습 발생 시 Stage 1-2에서 음악이 새로 시작되는 지점)
    private static final long TIME_OFFSET_MS = 41000L;

    public static long getCurrentStageEndTime() {
        int index = currentStage - 1;
        if (index >= 0 && index < STAGE_END_TIMES_MS.size()) {
            long endTime = STAGE_END_TIMES_MS.get(index);

            // ‼️ [핵심] Stage 1-2 (인덱스 1)부터는 기습 발생 시 오프셋을 적용
            if (index >= 1 && isSurpriseStageOccurred) {
                return endTime - TIME_OFFSET_MS;
            }

            return endTime;
        }
        return -1;
    }

    // 💡 [추가] 기습 스테이지의 길이 (10초)
    private static final long SURPRISE_PANEL_DURATION = 11000L;

    // 💡 [추가] 토끼가 멈췄다가 이어 달릴 때, 총 음악 길이에서 빼야 할 시간
    public static long getMusicLengthAdjustment() {
        if (isSurpriseStageOccurred()) {
            // 기습 스테이지가 발생하면 총 음악 길이에서 10초를 뺀 길이로 계산합니다.
            return SURPRISE_PANEL_DURATION;
        }
        return 0;
    }

    // 💡 [추가] 토끼가 멈췄다가 이어 달릴 때, 현재 음악 시간에 더해야 할 시간
// 기습 스테이지가 끝난 후 Stage 1-2의 시작 시간 오프셋입니다.
    public static long getMusicTimeOffset() {
        if (isSurpriseStageOccurred()) {
            // Stage 1-2로 전환될 때, 음악이 41000ms부터 시작한다고 가정하고,
            // 이 41000ms만큼의 오프셋을 시간 계산에 적용해야 합니다.
            return TIME_OFFSET_MS; // TIME_OFFSET_MS는 41000L입니다.
        }
        return 0;
    }

    // ‼️ [추가] 기습 스테이지 플래그 해제 (CakePanel에서 Stage 1-2로 전환될 때 호출)
    public static void endSurpriseStage() {
        isSurpriseStageOccurred = false;
        // 이 시점에 Stage 1-2로 전환되어 음악이 다시 시작되어야 합니다.
    }


}