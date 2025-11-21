package game.Cake;

import java.util.List;

public class CakeStageData {
    private final int stageNumber;
    private final String musicFileName;
    private final String backgroundPath;
    private final List<Long> correctTimings;
    private final String stageName;

    public CakeStageData(int stageNumber, String stageName, String musicFileName, String backgroundPath, List<Long> correctTimings) {
        this.stageNumber = stageNumber;
        this.stageName = stageName;
        this.musicFileName = musicFileName;
        this.backgroundPath = backgroundPath;
        this.correctTimings = correctTimings;
    }

    public int getStageNumber() { return stageNumber; }
    public String getMusicFileName() { return musicFileName; }
    public String getBackgroundPath() { return backgroundPath; }
    public List<Long> getCorrectTimings() { return correctTimings; }
    public String getStageName() { return stageName; }
}