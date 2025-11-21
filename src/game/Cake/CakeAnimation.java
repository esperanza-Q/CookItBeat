package game.Cake;

import game.Main;
import game.Music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;

public abstract class CakeAnimation extends JPanel {

    protected Image background;
    protected Image guideCardImage; // 가이드 카드병정 이미지 (정병이)

    // 재료 이미지 변수를 protected로 복원 (사용하지 않더라도 하위 클래스에서 에러가 나지 않도록 유지)
    protected Image strawberryBodyImage;
    protected Image climbingStraberryImage;
    protected Image eggImage;
    protected Image scissorsImage1;
    protected Image scissorsImage2;
    protected Image playerToolImage;
    protected Image shadowImage;

    protected Image decoStrawberry; // 플레이어 도구, 데코 딸기
    protected Image decoCream;
    protected Image[] guideLights;
    protected Image guideStick;
    protected Image creamPiping1;
    protected Image creamPiping2; // 플레이어 도구 짤주머니

    protected int currentMusicTimeMs = 0; // 현재 음악 재생 시간

    // ✂️ [추가] 가위 상태 및 위치 변수
    protected boolean isScissorsActive = false; // 현재 그려질 가위 이미지 (false: scissorsImage1, true: scissorsImage2)
    protected static final int SCISSORS_SIZE = 250;
    protected int scissorsX = 400;
    protected int scissorsY = 400;
    // ✂️ [추가] 가위 위치 상수 설정 (원하는 위치로 변경 가능)

    // UI 위치 및 크기 상수 (간소화)
    private static final int BAR_WIDTH = 500;
    private static final int BAR_X = Main.SCREEN_WIDTH - BAR_WIDTH - 20;
    private static final int BAR_Y = 20;

    // 진행 바 관련
    private Image rabbitBar;
    private Image rabbitIcon;
    private Font scoreFont; // 폰트 로딩은 유지

    public CakeAnimation(CakePanel controller, CakeStageData stageData, int initialScoreOffset) {
        setLayout(null);
        setBackground(Color.BLACK);
        setFocusable(true);

        // ✂️ [추가] 마우스 리스너 설정
        addMouseListener(new ScissorsMouseListener());

        loadResources(stageData);
        setupFont();
    }

    // 외부에서 점수를 동기화할 필요가 없어짐
    // public void syncScoreFromManager(int totalScore) { ... }

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

        // 판정 이미지 로드 제거

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

    // ----------------------------------------------------------------------
    // 📊 UI 그리기 (시간바, 점수박스 포함)
    // ----------------------------------------------------------------------
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
            // 토끼 아이콘이 바깥으로 나가지 않도록 경계 설정
            rabbitIconX = Math.max(BAR_X, Math.min(BAR_X + BAR_WIDTH - 50, rabbitIconX));

            g2.drawImage(rabbitIcon, rabbitIconX, BAR_Y, 40, 40, null);
        }

        if(scoreFont != null) {
            g2.setFont(scoreFont.deriveFont(Font.BOLD, 25f));
            String scoreStr = "0"; // 고정된 점수 또는 점수 없음
            FontMetrics fm = g2.getFontMetrics();
            int scoreWidth = fm.stringWidth(scoreStr);
            int scoreX = BAR_X + BAR_WIDTH - 80 ;
            g2.drawString(scoreStr, scoreX, BAR_Y + 35);
        }
    }

    // ✂️ [추가] 마우스 리스너 내부 클래스
    private class ScissorsMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            // 마우스 버튼을 누르는 순간 isScissorsActive를 true (가위 이미지 2)로 설정
            isScissorsActive = true;

            // ✂️ [추가] 가위 위치를 마우스 위치로 업데이트 (선택 사항)
            scissorsX = e.getX() - (SCISSORS_SIZE / 2);
            scissorsY = e.getY() - (SCISSORS_SIZE / 2);

            // UI를 즉시 다시 그리도록 요청하여 이미지 전환
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // 마우스 버튼을 떼는 순간 isScissorsActive를 false (가위 이미지 1)로 설정
            isScissorsActive = false;

            // UI를 즉시 다시 그리도록 요청하여 이미지 전환
            repaint();
        }
    }
}