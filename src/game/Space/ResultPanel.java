package game.Space;

import game.Main;
import game.Music;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;

public class ResultPanel extends JPanel {

    private final SpacePanel controller;   // ✅ 컨트롤러 참조 추가
    private JButton lobbyButton;
    private Image background;

    private Image resultImage;
    private String resultText = "";
    private int finalScore;

    private Font scoreFont;
    private Font detailFont;
    private Font rankFont;

    private Music resultMusic;
    private boolean musicPlayed = false;
    private Color rankColor = Color.WHITE;  // ✅ 등급/숫자 색

    // ✅ controller 받는 생성자로 변경
    public ResultPanel(SpacePanel controller) {
        this.controller = controller;
        setFocusable(true);
        setLayout(null);

        ImageIcon tolobby1 = new ImageIcon(getClass().getResource("../../images/mainUI/Buttons/tolobbyButton_unselected.png"));
        ImageIcon tolobby2 = new ImageIcon(getClass().getResource("../../images/mainUI/Buttons/tolobbyButton_selected.png"));
        lobbyButton = new JButton();    // ✅ 로비 버튼 생성
        lobbyButton.setText(null);
        lobbyButton.setIcon(tolobby1);
        lobbyButton.setRolloverIcon(tolobby2);
        lobbyButton.setBorderPainted(false);
        lobbyButton.setContentAreaFilled(false);
        lobbyButton.setFocusPainted(false);
        lobbyButton.setBounds(880, 600, 300, 60);

        lobbyButton.addActionListener(e -> {
            if (resultMusic != null) {
                resultMusic.close();
                resultMusic = null;
            }
            if (controller != null) {
                controller.goToLobby();
            }
        });

        add(lobbyButton);

        background = new ImageIcon(Main.class.getResource(
                "../images/alienStage_image/result_background.png"
        )).getImage();

        // ✅ 폰트 로드
        try {
            InputStream is = Main.class.getResourceAsStream("../fonts/LAB디지털.ttf");
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            scoreFont  = baseFont.deriveFont(Font.BOLD, 32f);
            detailFont = baseFont.deriveFont(Font.BOLD, 26f);
            rankFont   = baseFont.deriveFont(Font.BOLD, 40f);
        } catch (Exception e) {
            System.err.println("폰트 로딩 실패: " + e.getMessage());
            scoreFont  = new Font("Dialog", Font.BOLD, 32);
            detailFont = new Font("Dialog", Font.BOLD, 26);
            rankFont   = new Font("Dialog", Font.BOLD, 40);
        }
    }

    public void setResult(int score) {
        this.finalScore = score;

        if (score >= 2000) {
            resultText = "Perfect RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/alienStage_image/Result_Perfect02.png"
            )).getImage();
            rankColor = new Color(255, 230, 0); // 연노랑

        } else if (score >= 1000) {
            resultText = "Good RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/alienStage_image/Result_Good.png"
            )).getImage();
            rankColor = new Color(255, 165, 0); // 주황

        } else {
            resultText = "Bad RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/alienStage_image/Result_Bad.png"
            )).getImage();
            rankColor = new Color(255, 70, 70);  // 빨강
        }

        playResultMusic();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        if (background != null) {
            g.drawImage(background, 0, 0, w, h, this);
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int safeW = (int)(w * 0.875);
        int safeH = (int)(h * 0.917);
        int safeX = (w - safeW) / 2;
        int safeY = (h - safeH) / 2;

        int pad = (int)(safeW * 0.04);

        int leftBoxW = (int)(safeW * 0.62);
        int leftBoxH = (int)(safeH * 0.78);
        int leftBoxX = safeX + pad - 40 ;
        int leftBoxY = safeY + (safeH - leftBoxH) / 2;

        // ✅ right 박스 줄이고 bottom 박스 키움 (Cake와 맞춤)
        int rightBoxW = (int)(safeW * 0.33);
        int rightBoxH = (int)(safeH * 0.40);   // 0.55 → 0.40
        int rightBoxX = safeX + safeW - rightBoxW - pad;
        int rightBoxY = safeY + (int)(safeH * 0.10);

        int bottomBoxW = rightBoxW;
        int bottomBoxH = (int)(safeH * 0.22);  // 0.12 → 0.22
        int bottomBoxX = rightBoxX;
        int bottomBoxY = rightBoxY + rightBoxH + (int)(safeH * 0.04);

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(leftBoxX, leftBoxY, leftBoxW, leftBoxH, 20, 20);
        g2.fillRoundRect(rightBoxX, rightBoxY, rightBoxW, rightBoxH, 20, 20);
        g2.fillRoundRect(bottomBoxX, bottomBoxY, bottomBoxW, bottomBoxH, 20, 20);

        // ---------- 왼쪽 결과 이미지 ----------
        if (resultImage != null) {

            int imgW0 = resultImage.getWidth(this);
            int imgH0 = resultImage.getHeight(this);

            if (imgW0 > 0 && imgH0 > 0) {
                double sizeScale = 1.13;

                double baseScale = Math.min(
                        (double) leftBoxW / imgW0,
                        (double) leftBoxH / imgH0
                );

                double finalScale = baseScale * sizeScale;

                int imgW = (int) (imgW0 * finalScale);
                int imgH = (int) (imgH0 * finalScale);

                int imgX = leftBoxX + (leftBoxW - imgW) / 2;
                int imgY = leftBoxY + (leftBoxH - imgH) / 2;

                g2.drawImage(resultImage, imgX, imgY, imgW, imgH, this);
            }

        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(rankFont);
            drawCenteredString(g2, "REACTION",
                    new Rectangle(leftBoxX, leftBoxY, leftBoxW, leftBoxH));
        }

        // ---------- 오른쪽 상단: 합산 정보 ----------
        g2.setColor(Color.WHITE);

        // 제목 Bold 크게
        g2.setFont(rankFont);
        int textX = rightBoxX + pad;
        int textY = rightBoxY + pad + 10;
        int lineGap = 40;

        g2.drawString("점수 합산", textX, textY);
        textY += lineGap;

        // 수치는 detailFont
        g2.setFont(detailFont);
        int perfectCount = StageManager.getPerfectCount();
        int goodCount = StageManager.getGoodCount();
        int missCount = StageManager.getMissCount();

        g2.drawString("Perfect : " + perfectCount, textX, textY); textY += lineGap;
        g2.drawString("Good    : " + goodCount,   textX, textY); textY += lineGap;
        g2.drawString("Miss    : " + missCount,   textX, textY);

        // ---------- 오른쪽 하단: 최종 등급 + 점수 (부분 색, 두 줄 붙이기) ----------
        g2.setFont(scoreFont);  // ✅ Bold 폰트 (최종 등급 / 점수 라벨 & 값)

        FontMetrics fm = g2.getFontMetrics(scoreFont);
        int lineHeight   = fm.getHeight();
        int lineGap2     = 6;   // 두 줄 사이 간격
        int totalLinesHeight = lineHeight * 2 + lineGap2;

        int firstBaseY  = bottomBoxY + (bottomBoxH - totalLinesHeight) / 2 + fm.getAscent();
        int secondBaseY = firstBaseY + lineHeight + lineGap2;

        // === 1) 최종 등급 ===
        String baseRankLabel = "최종 등급 : ";
        String rankValue     = resultText;           // (예: Bad RANK!)

        int totalRankWidth   = fm.stringWidth(baseRankLabel + rankValue);
        int startX           = bottomBoxX + (bottomBoxW - totalRankWidth) / 2;
        int rankLabelWidth   = fm.stringWidth(baseRankLabel);

        // 라벨(흰색)
        g2.setColor(Color.WHITE);
        g2.drawString(baseRankLabel, startX, firstBaseY);

        // 값(색 - 빨/주/노)
        g2.setColor(rankColor);
        g2.drawString(rankValue, startX + rankLabelWidth, firstBaseY);

        // === 2) 점수 ===
        String scoreLabel = "점수 : ";
        String scoreValue = String.valueOf(finalScore);

        int totalScoreWidth = fm.stringWidth(scoreLabel + scoreValue);
        int startX2         = bottomBoxX + (bottomBoxW - totalScoreWidth) / 2;
        int scoreLabelWidth = fm.stringWidth(scoreLabel);

        // 라벨(흰색)
        g2.setColor(Color.WHITE);
        g2.drawString(scoreLabel, startX2, secondBaseY);

        // 숫자만 색
        g2.setColor(rankColor);
        g2.drawString(scoreValue, startX2 + scoreLabelWidth, secondBaseY);

        g2.dispose();
    }

    private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
    }

    private void playResultMusic() {
        if (musicPlayed) return;
        musicPlayed = true;

        if (StageManager.spaceBackgroundMusic != null) {
            StageManager.spaceBackgroundMusic.close();
        }

        if(resultText == "Perfect RANK!"){
            resultMusic = new Music("alien_perfectResult.mp3", false);
        } else {
            resultMusic = new Music("result_alien.mp3", false);
        }

        resultMusic.start();
    }
}
