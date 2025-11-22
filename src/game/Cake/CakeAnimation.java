package game.Cake;

import game.Main;
import game.Music;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class CakeAnimation extends JPanel {

    protected Image background;
    protected Image guideCardImage;
    protected Image guideCardImage1;
    protected Image guideCardImage2;

    protected Image strawberryBodyImage;
    protected Image climbingStraberryImage;
    protected Image eggImage;
    protected Image scissorsImage1;
    protected Image scissorsImage2;
    protected Image playerToolImage;
    protected Image shadowImage;

    protected Image strawberryTopImage;

    // ‼️ [추가] 판정 이미지 관련 필드
    protected Image[] judgementImages = new Image[3];
    protected String lastJudgementResult = "NONE"; // 마지막 판정 결과 (문자열)
    protected long judgementDisplayStartTime = 0;   // 판정 이미지가 표시되기 시작한 시간

    protected int currentMusicTimeMs = 0;
    private final int GLOBAL_JUDGEMENT_OFFSET_MS = -120;
    private static final int JUDGEMENT_DISPLAY_DURATION = 600; // 0.8초간 표시

    // 🍓 [동기화 대상] Shadow 객체를 담는 리스트
    protected List<Shadow> shadowList = new ArrayList<>();
    // 🍓 [동기화 대상] 떨어지는 딸기 객체를 담는 리스트
    protected List<StrawberryNote> strawberryList = new ArrayList<>();

    // UI 위치 및 크기 상수 (유지)
    private static final int BAR_WIDTH = 500;
    private static final int BAR_X = Main.SCREEN_WIDTH - BAR_WIDTH - 20;
    private static final int BAR_Y = 20;

    // 진행 바 관련
    private Image rabbitBar;
    private Image rabbitIcon;
    private Font scoreFont;

    public CakeAnimation(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        setLayout(null);
        setBackground(Color.BLACK);
        setFocusable(true);

        loadResources(stageData);
        setupFont();
    }

    public Image loadImage(String path) {
        try {
            java.net.URL url = Main.class.getResource(path);
            if (url == null) {
                System.err.println("🔴 리소스 로드 실패: 경로에 파일이 없습니다 -> " + path);
                return null;
            }
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            System.err.println("🔴 이미지 로드 중 예외 발생: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private void loadResources(CakeStageData stageData) {
        background = loadImage(stageData.getBackgroundPath());
        rabbitBar = loadImage("../images/mainUI/cakeStage_progBar.png");
        rabbitIcon = loadImage("../images/mainUI/cakeStage_progIcon.png");

        // ‼️ [추가] 판정 이미지 로드 (제공된 경로 사용)
        judgementImages[0] = loadImage("../images/mainUI/acc_perfect.png"); // PERFECT
        judgementImages[1] = loadImage("../images/mainUI/acc_good.png");    // GOOD, GREAT
        judgementImages[2] = loadImage("../images/mainUI/acc_miss.png");     // MISS

        loadStageSpecificResources();
    }

    private void setupFont() {
        try {
            InputStream is = Main.class.getResourceAsStream("../fonts/LAB디지털.ttf");

            if (is == null) {
                scoreFont = new Font("Arial", Font.BOLD, 24);
            } else {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                scoreFont = baseFont.deriveFont(Font.BOLD, 24f);
                is.close();
            }

        } catch (Exception e) {
            scoreFont = new Font("Arial", Font.BOLD, 24);
        }
    }

    protected abstract void loadStageSpecificResources();
    protected abstract void drawStageObjects(Graphics2D g2);

    public void updateStageLogic() {}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (background != null) {
            g2.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }

        if (CakeStageManager.getMusic() != null && CakeStageManager.getMusic().isAlive()) {
            currentMusicTimeMs = CakeStageManager.getMusic().getTime();

            drawStageObjects(g2);
            drawUI(g2);
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            g2.drawString("게임 준비 중...", getWidth() / 2 - 100, getHeight() / 2);
        }
    }

    private void drawUI(Graphics2D g2) {
        // 1. 시간 바 (토끼 진행 바)
        if (rabbitBar != null && rabbitIcon != null) {
            g2.drawImage(rabbitBar, BAR_X, BAR_Y, BAR_WIDTH, 50, null);

            String musicFileName = CakeStageManager.getCurrentStageData().getMusicFileName();
            int musicLengthSec = Music.getMusicLength(musicFileName);
            int musicLengthMs = musicLengthSec * 1000;

            double progress = (double) currentMusicTimeMs / musicLengthMs;
            progress = Math.min(1.0, Math.max(0.0, progress));

            int rabbitIconX = (int) (BAR_X + ((BAR_WIDTH-130) * progress)) - (rabbitIcon.getWidth(null) / 2);
            rabbitIconX = Math.max(BAR_X, Math.min(BAR_X + BAR_WIDTH - 50, rabbitIconX));

            g2.drawImage(rabbitIcon, rabbitIconX, BAR_Y, 40, 40, null);
        }

        if(scoreFont != null) {
            g2.setFont(scoreFont.deriveFont(Font.BOLD, 25f));
            // 💡 [핵심 수정] CakeStageManager에서 누적 점수를 가져옵니다.
            int scoreValue = CakeStageManager.getCumulativeScore();
            String scoreStr = String.format("%d", scoreValue);

            FontMetrics fm = g2.getFontMetrics();

            // 점수 위치 설정
            int scoreX = BAR_X + BAR_WIDTH - fm.stringWidth(scoreStr) - 20 ; // 오른쪽 정렬
            int scoreY = BAR_Y + 30;

            // 그림자 효과
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(scoreStr, scoreX -2, scoreY + 2);

            // 실제 점수 그리기
            g2.setColor(Color.WHITE);
            g2.drawString(scoreStr, scoreX, scoreY);
        }

        // 2. 🎯 판정 이미지 표시 로직
        long currentTime = currentMusicTimeMs;
        if (currentTime < judgementDisplayStartTime + JUDGEMENT_DISPLAY_DURATION) {

            Image judgementImage = null;

            // ‼️ 판정 결과 문자열에 따라 표시할 이미지 선택
            if (lastJudgementResult.equals("PERFECT!")) {
                judgementImage = judgementImages[0]; // PERFECT
            } else if (lastJudgementResult.equals("GREAT!") || lastJudgementResult.equals("GOOD")) {
                judgementImage = judgementImages[1]; // GOOD/GREAT
            } else if (lastJudgementResult.equals("MISS")) {
                judgementImage = judgementImages[2]; // MISS
            }

            if (judgementImage != null) {
                int imgW = 200; // 이미지 너비 (조절 가능)
                int imgH = 60;  // 이미지 높이 (조절 가능)
                int imgX = (Main.SCREEN_WIDTH / 2) - (imgW / 2); // 화면 중앙
                int imgY = 100; // Y 위치 (조절 가능)

                // ‼️ 이미지 투명도 조절 (점점 사라지는 애니메이션)
                float alpha = 1.0f - (float)(currentTime - judgementDisplayStartTime) / JUDGEMENT_DISPLAY_DURATION;
                alpha = Math.max(0.0f, alpha);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.drawImage(judgementImage, imgX, imgY, imgW, imgH, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // 투명도 리셋
            }
        }

    }

    // ----------------------------------------------------------------------
    // 🍓 [수정됨] 딸기 노트 클래스 (낙하 제거, 컷팅 판정 추가)
    // ----------------------------------------------------------------------
    protected class StrawberryNote {
        private final Image bodyImage;
        private final Image topImage;
        private final long startTimeMs; // ‼️ 외부에 노출되어야 함

        private final int noteIndex; // ‼️ [추가] 이 노트가 몇 번째 노트인지 저장

        // 딸기가 생성될 위치 (그림자 위치)
        private final Point targetPos;

        // ‼️ [추가] 컷팅 상태를 저장하는 필드 (true: 분리됨, false: 붙어있음)
        private boolean isCut = false;

        private static final int STRAWBERRY_SIZE = 100;
        // ‼️ [추가] 컷팅 시 꼭지가 분리될 거리 (Y축 기준)
        private static final int CUT_OFFSET_Y = 50;

        public StrawberryNote(Image bodyImage, Image topImage, long startTimeMs, Point targetPos, int noteIndex) {
            this.bodyImage = bodyImage;
            this.topImage = topImage;
            this.startTimeMs = startTimeMs;
            this.targetPos = targetPos;
            this.noteIndex = noteIndex; // ‼️ [추가] 초기화
        }

        // ‼️ [추가] 인덱스 Getter
        public int getNoteIndex() {
            return noteIndex;
        }

        // ‼️ [추가] 딸기의 생성 시간을 반환하는 Getter
        public long getStartTimeMs() {
            return startTimeMs;
        }

        public void setCut(boolean cut) {
            this.isCut = cut;
        }

        public boolean shouldBeRemoved(long currentTimeMs) {
            // 딸기는 CLEAR_TIMES_MS 타이밍에 외부에서 일괄 제거됩니다.
            return false;
        }

        public void draw(Graphics2D g2, long currentTimeMs) {
            // 낙하 애니메이션 없이 즉시 그림자 위치에 표시
            int bodyX = targetPos.x+30;
            int bodyY = targetPos.y+30;

            int topX = targetPos.x+30;
            int topY = targetPos.y+30;

            // ‼️ [핵심 수정] isCut 상태에 따라 꼭지 위치 조정
            if (isCut) {
                // 컷팅된 경우, 꼭지를 위로 분리하여 그립니다.
                topY += CUT_OFFSET_Y;
            }

            // 몸통 그리기 (항상 같은 위치)
            if (bodyImage != null) {
                g2.drawImage(bodyImage, bodyX, bodyY, STRAWBERRY_SIZE, STRAWBERRY_SIZE, null);
            }

            // 꼭지 그리기 (컷팅 여부에 따라 Y 위치가 달라짐)
            if (topImage != null) {
                g2.drawImage(topImage, topX, topY, STRAWBERRY_SIZE, STRAWBERRY_SIZE, null);
            }
        }
    }

    // ----------------------------------------------------------------------
    // 🍓 [유지] Shadow 객체 클래스 (위치 정보 저장용)
    // ----------------------------------------------------------------------
    protected class Shadow {
        private final Image image;
        private final int x, y, width, height;

        public Shadow(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() { return x; }
        public int getY() { return y; }

        public void draw(Graphics2D g2) {
            if (image != null) {
                g2.drawImage(image, x, y, width, height, null);
            }
        }
    }

    // 🍓 [유지] 슬롯 정보를 기억하는 Shadow 객체
    protected class SlotShadow extends Shadow {
        private final int slotIndex;

        public SlotShadow(Image image, int x, int y, int width, int height, int slotIndex) {
            super(image, x, y, width, height);
            this.slotIndex = slotIndex;
        }

        public int getSlotIndex() {
            return slotIndex;
        }
    }
}