package game.Cake;

import game.Music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CakeStageManager {

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
            88000L, // Stage 3-1 종료 시간 (20.8초)
            116000L  // Stage 3-2 종료 시간 (26.0초, 최종 종료)
    );

    private static void initializeStageData() {
        stageDataList = new ArrayList<>();
        List<Long> dummyTimings = Arrays.asList(0L);

        // ... (CakeStageData 초기화 로직 유지) ...
        stageDataList.add(new CakeStageData(1, "재료 손질 (꼭지)", GAME_MUSIC_FILE, "../images/cakeStage_image/stage1/Background_stage1-1.png", dummyTimings));
        stageDataList.add(new CakeStageData(2, "재료 손질 (넣기)", GAME_MUSIC_FILE, "../images/cakeStage_image/stage1/Background_stage1-2.png", dummyTimings));
        stageDataList.add(new CakeStageData(3, "반죽 만들기", GAME_MUSIC_FILE, "../images/cakeStage_image/stage2/dough_background.png", dummyTimings));
        stageDataList.add(new CakeStageData(4, "몽환 케이크 데코 (휘핑)", GAME_MUSIC_FILE, "../images/cakeStage_image/stage3/Background_stage3-1.png", dummyTimings));
        stageDataList.add(new CakeStageData(5, "몽환 케이크 데코 (딸기)", GAME_MUSIC_FILE, "../images/cakeStage_image/stage3/Background_stage3-2.png", dummyTimings));
    }

    public static void startFirstStage() {
        if (stageDataList == null) {
            initializeStageData();
        }
        resetScore();
        currentStage = 1;

        // 💡 음악 시작 로직은 CakePanel로 이관되었으므로 여기서는 제거

        loadStage(currentStage);
    }

    // ‼️ [추가] CakePanel에서 시작된 Music 객체를 등록하는 Setter
    public static void setMusic(Music music) {
        currentMusic = music;
    }


    public static boolean nextStage() {
        currentStage++;

        if (currentStage <= stageDataList.size()) {
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

    public static void resetScore() {
        cumulativeScore = 0;
    }

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
        if (currentStage > 0 && currentStage <= stageDataList.size()) {
            return stageDataList.get(currentStage - 1);
        }
        return null;
    }

    public static long getCurrentStageEndTime() {
        int index = currentStage - 1;
        if (index >= 0 && index < STAGE_END_TIMES_MS.size()) {
            return STAGE_END_TIMES_MS.get(index);
        }
        return -1;
    }
}