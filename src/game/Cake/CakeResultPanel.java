package game.Cake;

import game.Main;
import game.Music;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class CakeResultPanel extends JPanel {

    private final CakePanel controller;   // ✅ 유지
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

    // ✅ controller 받는 생성자!
    public CakeResultPanel(CakePanel controller) {
        this.controller = controller;  // ✅ 제대로 저장
        setFocusable(true);
        setLayout(null);

        // ✅ 버튼 먼저 생성
        lobbyButton = new JButton("로비로 돌아가기");
        lobbyButton.setFocusPainted(false);
        lobbyButton.setBounds(880, 630, 300, 60); // 원하는 위치면 여기만 바꾸면 됨

        lobbyButton.addActionListener(e -> {
            // 결과 음악 끄기
            if (resultMusic != null) {
                resultMusic.close();
                resultMusic = null;
            }

            // ✅ 로비로 이동
            if (controller != null) {
                controller.goToLobby();
            }
        });

        add(lobbyButton);

        // ✅ 케이크 결과 배경 (원하는걸로 교체 가능)
        background = new ImageIcon(Main.class.getResource(
                "../images/cakeStage_image/result_background.png"
        )).getImage();

        // ✅ 폰트 로드 (Cake 쪽 폰트 위치에 맞춰서)
        try {
            InputStream is = Main.class.getResourceAsStream("../fonts/LAB디지털.ttf");
            Font baseFont;

            if (is != null) {
                baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                is.close();
            } else {
                // fallback: Space처럼 절대경로 쓰고 싶으면 여기서 교체
                File fontFile = new File("C:\\HYKY\\CookItBeat\\src\\fonts\\LAB디지털.ttf");
                InputStream fis = new FileInputStream(fontFile);
                baseFont = Font.createFont(Font.TRUETYPE_FONT, fis);
                fis.close();
            }

            scoreFont  = baseFont.deriveFont(Font.BOLD, 32f);
            detailFont = baseFont.deriveFont(Font.BOLD, 26f);
            rankFont   = baseFont.deriveFont(Font.BOLD, 40f);

        } catch (Exception e) {
            System.err.println("폰트 로딩 실패. 기본 폰트 사용: " + e.getMessage());
            scoreFont  = new Font("Dialog", Font.BOLD, 32);
            detailFont = new Font("Dialog", Font.BOLD, 26);
            rankFont   = new Font("Dialog", Font.BOLD, 40);
        }
    }

    public void setResult(int score) {
        this.finalScore = score;

        // ✅ 점수 컷은 너가 원하는대로 조정하면 됨
        if (score >= 2000) {
            resultText = "Perfect RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/cakeStage_image/Result_Perfect.png"
            )).getImage();

        } else if (score >= 1000) {
            resultText = "Good RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/cakeStage_image/Result_Good.png"
            )).getImage();

        } else {
            resultText = "Bad RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/cakeStage_image/Result_Bad.png"
            )).getImage();
        }

        playResultMusic();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        // 배경
        if (background != null) {
            g.drawImage(background, 0, 0, w, h, this);
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ✅ 안전 UI 영역 비율은 Space와 동일하게
        int safeW = (int)(w * 0.875);
        int safeH = (int)(h * 0.917);
        int safeX = (w - safeW) / 2;
        int safeY = (h - safeH) / 2;

        int pad = (int)(safeW * 0.04);

        // ✅ 박스 레이아웃
        int leftBoxW = (int)(safeW * 0.55);
        int leftBoxH = (int)(safeH * 0.48);
        int leftBoxX = safeX + pad;
        int leftBoxY = safeY + (safeH - leftBoxH) / 2;

        int rightBoxW = (int)(safeW * 0.33);
        int rightBoxH = (int)(safeH * 0.55);
        int rightBoxX = safeX + safeW - rightBoxW - pad;
        int rightBoxY = safeY + (int)(safeH * 0.10);

        int bottomBoxW = rightBoxW;
        int bottomBoxH = (int)(safeH * 0.12);
        int bottomBoxX = rightBoxX;
        int bottomBoxY = rightBoxY + rightBoxH + (int)(safeH * 0.04);

        // ✅ 박스 반투명 배경
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(leftBoxX, leftBoxY, leftBoxW, leftBoxH, 20, 20);
        g2.fillRoundRect(rightBoxX, rightBoxY, rightBoxW, rightBoxH, 20, 20);
        g2.fillRoundRect(bottomBoxX, bottomBoxY, bottomBoxW, bottomBoxH, 20, 20);

        // ------------------------------------------------
        // 1) 왼쪽: 결과 이미지
        // ------------------------------------------------
        if (resultImage != null) {
            g2.drawImage(resultImage, leftBoxX, leftBoxY, leftBoxW, leftBoxH, this);
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(rankFont);
            drawCenteredString(g2, "REACTION",
                    new Rectangle(leftBoxX, leftBoxY, leftBoxW, leftBoxH));
        }

        // ------------------------------------------------
        // 2) 오른쪽 위: 상세 정보 (Cake에 카운트 없어서 우선 점수만)
        // ------------------------------------------------
        g2.setColor(Color.WHITE);
        g2.setFont(detailFont);

        int textX = rightBoxX + pad;
        int textY = rightBoxY + pad + 10;
        int lineGap = 40;

        g2.drawString("점수 합산", textX, textY);
        textY += lineGap;

        // ✅ CakeStageManager에 카운트가 없으니 일단 누적점수만 표시
        int perfectCount = CakeStageManager.getPerfectCount();
        int goodCount = CakeStageManager.getGoodCount();
        int missCount = CakeStageManager.getMissCount();

        g2.drawString("Perfect : " + perfectCount, textX, textY); textY += lineGap;
        g2.drawString("Good    : " + goodCount, textX, textY); textY += lineGap;
        g2.drawString("Miss    : " + missCount, textX, textY);

        // ------------------------------------------------
        // 3) 오른쪽 아래: 최종 등급 + 점수
        // ------------------------------------------------
        g2.setFont(scoreFont);

        String rankLine = "최종 등급 : " + resultText;
        String scoreLine = "점수 : " + finalScore;

        drawCenteredString(g2, rankLine,
                new Rectangle(bottomBoxX, bottomBoxY, bottomBoxW, bottomBoxH/2));
        drawCenteredString(g2, scoreLine,
                new Rectangle(bottomBoxX, bottomBoxY + bottomBoxH/2, bottomBoxW, bottomBoxH/2));

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

        // ✅ 기존 Cake 음악 종료
        CakeStageManager.stopMusic();

        // ✅ 결과 화면 전용 음악
        resultMusic = new Music("result_cake.mp3", false);
        resultMusic.start();
    }
}
