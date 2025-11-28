package game.Space;

import game.Main;
import game.Music;

import javax.swing.*;
import java.awt.*;
import java.io.File;
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

    // ✅ controller 받는 생성자로 변경
    public ResultPanel(SpacePanel controller) {
        this.controller = controller;
        setFocusable(true);
        setLayout(null);

        ImageIcon tolobby1 = new ImageIcon(getClass().getResource("../../images/mainUI/Buttons/tolobbyButton_unselected.png"));
        ImageIcon tolobby2 = new ImageIcon(getClass().getResource("../../images/mainUI/Buttons/tolobbyButton_selected.png"));
        lobbyButton = new JButton();    // ✅ 로비 버튼 생성
        // 1. 버튼에서 기본 텍스트 제거
        lobbyButton.setText(null);

        // 2. 버튼의 기본 아이콘 설정 (unselected)
        lobbyButton.setIcon(tolobby1);

        // 3. 마우스가 올라갔을 때(rollover) 아이콘 설정 (selected)
        lobbyButton.setRolloverIcon(tolobby2);

        // 4. 버튼 배경과 테두리를 투명하게 설정하여 이미지 자체만 보이도록 합니다.
        lobbyButton.setBorderPainted(false);      // 테두리 제거
        lobbyButton.setContentAreaFilled(false);  // 내용 영역 채우기 제거 (배경 투명화)
        // ------------------ 👆 [수정/추가됨] 이미지 및 스타일 설정 👆 ------------------

        lobbyButton.setFocusPainted(false);
        lobbyButton.setBounds(880, 600, 300, 60); // 원하는 위치면 여기만 바꾸면 됨

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

        background = new ImageIcon(Main.class.getResource(
                "../images/alienStage_image/result_background.png"
        )).getImage();

        // ✅ 폰트 로드
        try {
            File fontFile = new File("C:\\Users\\SAMSUNG\\Desktop\\project_cookItBeat\\CookItBeat\\src\\fonts\\LAB디지털.ttf");
            InputStream is = new FileInputStream(fontFile);

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            is.close();

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

        if (score >= 700) {
            resultText = "Perfect RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/alienStage_image/Result_Perfect02.png"
            )).getImage();

        } else if (score >= 500) {
            resultText = "Good RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/alienStage_image/Result_Good.png"
            )).getImage();

        } else {
            resultText = "Bad RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/alienStage_image/Result_Bad.png"
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

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(leftBoxX, leftBoxY, leftBoxW, leftBoxH, 20, 20);
        g2.fillRoundRect(rightBoxX, rightBoxY, rightBoxW, rightBoxH, 20, 20);
        g2.fillRoundRect(bottomBoxX, bottomBoxY, bottomBoxW, bottomBoxH, 20, 20);

        if (resultImage != null) {
            g2.drawImage(resultImage, leftBoxX, leftBoxY, leftBoxW, leftBoxH, this);
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(rankFont);
            drawCenteredString(g2, "REACTION",
                    new Rectangle(leftBoxX, leftBoxY, leftBoxW, leftBoxH));
        }

        g2.setColor(Color.WHITE);
        g2.setFont(detailFont);

        int textX = rightBoxX + pad;
        int textY = rightBoxY + pad + 10;
        int lineGap = 40;

        g2.drawString("점수 합산", textX, textY);
        textY += lineGap;

        int perfectCount = StageManager.getPerfectCount();
        int goodCount = StageManager.getGoodCount();
        int missCount = StageManager.getMissCount();

        g2.drawString("Perfect : " + perfectCount, textX, textY); textY += lineGap;
        g2.drawString("Good    : " + goodCount, textX, textY); textY += lineGap;
        g2.drawString("Miss    : " + missCount, textX, textY);

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

        if (StageManager.spaceBackgroundMusic != null) {
            StageManager.spaceBackgroundMusic.close();
        }

        resultMusic = new Music("result_alien.mp3", false);
        resultMusic.start();
    }
}
