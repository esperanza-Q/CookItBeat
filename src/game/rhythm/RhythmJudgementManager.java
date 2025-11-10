package game.rhythm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RhythmJudgementManager {

    // ✅ 판정 기준 (밀리초)
    private static final int PERFECT_TIMING = 50;
    private static final int GREAT_TIMING = 100;
    private static final int GOOD_TIMING = 150; // 판정 범위

    // ✅ 게임 상태 (멤버 변수)
    private final List<Long> correctTimings;
    // ‼️ judgementTimes: 판정 타이밍 리스트 (현재 코드에서는 correctTimings와 동일하게 사용됨)
    private List<Long> judgementTimes;
    private int score = 0;
    private String lastJudgement = "NONE";
    private long lastJudgementTime;

    // ✅ 정답 및 입력 기록
    // ‼️ matchedTimings: 이미 판정된 정답 타이밍의 인덱스를 저장 (NullPointerException 방지)
    private Set<Integer> matchedTimings;

    // ----------------------------------------------------------------------
    // ‼️ [수정] 이월된 점수(offset)를 받는 생성자 (스코어 누적 핵심)
    // ----------------------------------------------------------------------
    public RhythmJudgementManager(List<Long> correctTimings, int initialScoreOffset) {
        this.correctTimings = correctTimings;
        this.judgementTimes = new ArrayList<>(correctTimings);

        // ‼️ [핵심] 스테이지 간 이월된 점수로 score 초기화
        this.score = initialScoreOffset;

        // ‼️ [핵심] matchedTimings를 초기화하여 NullPointerException 방지
        this.matchedTimings = new HashSet<>();
    }


    // ‼️ 기존 생성자 (initialScoreOffset을 0으로 설정하여 오버로드)
    public RhythmJudgementManager(List<Long> correctTimings) {
        this(correctTimings, 0);
    }
    // ----------------------------------------------------------------------

    /**
     * 플레이어의 입력 시간을 받아 판정을 수행합니다.
     * @param currentTimeMs 현재 음악 재생 시간 (밀리초)
     */
    public void handleInput(int currentTimeMs) {
        long currentInputTime = (long) currentTimeMs; // int를 long으로 사용

        int closestIndex = -1;
        long minDiff = Long.MAX_VALUE;

        // 1. 가장 가까운 정답 타이밍 찾기
        for (int i = 0; i < correctTimings.size(); i++) {
            // ‼️ 이미 매칭된 타이밍이면 건너뜁니다.
            if (matchedTimings.contains(i)) {
                continue;
            }

            long correctTime = correctTimings.get(i);
            long diff = Math.abs(currentInputTime - correctTime);

            // ‼️ 정답 시간이 현재 입력 시간보다 너무 과거이면 무시
            // 이미 GOOD_TIMING(150ms)보다 훨씬 많이 지난 정답은 매칭 시도하지 않음
            if (correctTime < currentInputTime - 200) {
                continue;
            }

            // 판정 범위 내에서 가장 가까운 것 찾기
            if (diff <= GOOD_TIMING && diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }

        // 2. 판정 처리 및 점수 추가
        if (closestIndex != -1) {
            // ‼️ [추가] 로그 확인 ‼️ (디버깅용)
            long correctTime = correctTimings.get(closestIndex);
            System.out.println("Input Time: " + currentInputTime);
            System.out.println("Closest Correct Time: " + correctTime);
            System.out.println("Measured Difference (minDiff): " + minDiff);

            matchedTimings.add(closestIndex);

            // lastJudgementTime은 화면 표시 타이머에 영향을 주지 않으므로, 판정 성공 시간을 기록
            lastJudgementTime = currentInputTime;

            if (minDiff <= PERFECT_TIMING) {
                lastJudgement = "PERFECT!";
                score += 100;
            } else if (minDiff <= GREAT_TIMING) {
                lastJudgement = "GREAT!";
                score += 70;
            } else { // minDiff <= GOOD_TIMING
                lastJudgement = "GOOD";
                score += 50;
            }
        } else {
            // 가장 가까운 정답이 판정 범위 밖에 있었을 경우 (MISS)
            // (이 로직은 입력이 들어올 때마다 호출되므로, 정확한 MISS 처리는 별도 로직이 필요할 수 있으나, 현재 코드 구조 유지)
            lastJudgement = "MISS";
            lastJudgementTime = currentInputTime;
        }
    }

    // ✅ Getter 메서드 (SpaceAnimation에서 화면 표시 및 상태 확인을 위해 사용)
    public int getScore() { return score; }
    public String getLastJudgement() { return lastJudgement; }
    public long getLastJudgementTime() { return lastJudgementTime; }
    public int getMatchedCount() { return matchedTimings.size(); }
    public int getTotalCount() { return correctTimings.size(); }
    public List<Long> getCorrectTimings() { return correctTimings; }
}