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
    private Color rankColor = Color.WHITE;


    // ✅ controller 받는 생성자!
    public CakeResultPanel(CakePanel controller) {
        this.controller = controller;  // ✅ 제대로 저장
        setFocusable(true);
        setLayout(null);

        // ✅ 버튼 먼저 생성
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
                File fontFile = new File("C:\\HYKY\\CookItBeat\\src\\fonts\\LAB디지털.ttf");
                InputStream fis = new FileInputStream(fontFile);
                baseFont = Font.createFont(Font.TRUETYPE_FONT, fis);
                fis.close();
            }

            scoreFont  = baseFont.deriveFont(Font.BOLD, 32f);
            detailFont = baseFont.deriveFont(Font.BOLD, 26f);
            rankFont   = baseFont.deriveFont(Font.BOLD, 35f);

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
        if (score >= 2500) {
            resultText = "Perfect RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/cakeStage_image/Result_Perfect.png"
            )).getImage();
            rankColor = new Color(255, 230, 0); // 연노랑

        } else if (score >= 1500) {
            resultText = "Good RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/cakeStage_image/Result_Good.png"
            )).getImage();
            rankColor = new Color(255, 165, 0); // 오렌지

        } else {
            resultText = "Bad RANK!";
            resultImage = new ImageIcon(Main.class.getResource(
                    "../images/cakeStage_image/Result_Bad.png"
            )).getImage();
            rankColor = new Color(255, 70, 70); // 레드
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
        int safeW = (int) (w * 0.875);
        int safeH = (int) (h * 0.917);
        int safeX = (w - safeW) / 2;
        int safeY = (h - safeH) / 2;

        int pad = (int) (safeW * 0.04);

        // ✅ 박스 레이아웃
        int leftBoxW = (int) (safeW * 0.55);
        int leftBoxH = (int) (safeH * 0.85);
        int leftBoxX = safeX + pad;
        int leftBoxY = safeY + (safeH - leftBoxH) / 2;

        int rightBoxW = (int) (safeW * 0.33);
        int rightBoxH = (int) (safeH * 0.40);
        int rightBoxX = safeX + safeW - rightBoxW - pad;
        int rightBoxY = safeY + (int) (safeH * 0.10);

        int bottomBoxW = rightBoxW;
        int bottomBoxH = (int) (safeH * 0.22);
        int bottomBoxX = rightBoxX;
        int bottomBoxY = rightBoxY + rightBoxH + (int) (safeH * 0.04);

        // ✅ 박스 반투명 배경
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(leftBoxX, leftBoxY, leftBoxW, leftBoxH, 20, 20);
        g2.fillRoundRect(rightBoxX, rightBoxY, rightBoxW, rightBoxH, 20, 20);
        g2.fillRoundRect(bottomBoxX, bottomBoxY, bottomBoxW, bottomBoxH, 20, 20);

        // ------------------------------------------------
        // 1) 왼쪽: 결과 이미지
        // ------------------------------------------------
        // ------------------------------------------------
        // 1) 왼쪽: 결과 이미지 (더 크게 표시)
        // ------------------------------------------------
        if (resultImage != null) {

            // 기존 박스 대비 1.5배 확대
            double scale = 1.15;

            int imgW = (int) (leftBoxW * scale);
            int imgH = (int) (leftBoxH * scale);

            // 박스 중앙에 맞게 위치 조정
            int imgX = leftBoxX - (imgW - leftBoxW) / 2;
            int imgY = leftBoxY - (imgH - leftBoxH) / 2 + 10;

            g2.drawImage(resultImage, imgX, imgY, imgW, imgH, this);

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

// 🔹 제목은 더 두껍고 큰 rankFont 사용
        g2.setFont(rankFont);
        int textX = rightBoxX + pad;
        int textY = rightBoxY + pad + 5;
        int lineGap = 40;

        g2.drawString("점수 합산", textX, textY);
        textY += lineGap;

// 🔹 실제 수치는 detailFont로 다시 변경
        g2.setFont(detailFont);

        int perfectCount = CakeStageManager.getPerfectCount();
        int goodCount = CakeStageManager.getGoodCount();
        int missCount = CakeStageManager.getMissCount();

        g2.drawString("Perfect : " + perfectCount, textX, textY);
        textY += lineGap;
        g2.drawString("Good    : " + goodCount, textX, textY);
        textY += lineGap;
        g2.drawString("Miss    : " + missCount, textX, textY);

        // ------------------------------------------------
        // 3) 오른쪽 아래: 최종 등급 + 점수 (부분색 적용)
        // ------------------------------------------------
        g2.setFont(scoreFont);

        // === 공통 치수 계산 ===
        FontMetrics fm = g2.getFontMetrics(scoreFont);
        int lineHeight = fm.getHeight();
        int lineGap2 = 6; // 두 줄 사이 간격(px) – 더 붙이고 싶으면 0~4, 더 띄우고 싶으면 키우기

        int totalLinesHeight = lineHeight * 2 + lineGap2;

        // bottomBox 안에서 두 줄 전체를 세로 가운데 정렬
        int firstBaseY = bottomBoxY + (bottomBoxH - totalLinesHeight) / 2 + fm.getAscent();
        int secondBaseY = firstBaseY + lineHeight + lineGap2;

        // === 1) 최종 등급 부분 ===
        String baseRankLabel = "최종 등급 : ";
        String rankValue = resultText;   // ex) "Bad RANK!"

        g2.setColor(Color.WHITE);

        // 중앙 정렬을 위해 전체 폭 구함
        int totalRankWidth = fm.stringWidth(baseRankLabel + rankValue);
        int startX = bottomBoxX + (bottomBoxW - totalRankWidth) / 2;
        int rankLabelWidth = fm.stringWidth(baseRankLabel);

        // 1-1. "최종 등급 : " (흰색)
        g2.drawString(baseRankLabel, startX, firstBaseY);

        // 1-2. "Bad RANK!" 부분만 색 적용
        g2.setColor(rankColor);
        g2.drawString(rankValue, startX + rankLabelWidth, firstBaseY);

        // === 2) 점수 부분 (숫자만 색 적용) ===
        String scoreLabel = "점수 : ";
        String scoreValue = String.valueOf(finalScore);

        int totalScoreWidth = fm.stringWidth(scoreLabel + scoreValue);
        int startX2 = bottomBoxX + (bottomBoxW - totalScoreWidth) / 2;
        int scoreLabelWidth = fm.stringWidth(scoreLabel);

        // 2-1. "점수 : " (흰색)
        g2.setColor(Color.WHITE);
        g2.drawString(scoreLabel, startX2, secondBaseY);

        // 2-2. 숫자만 색 적용
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

        // ✅ 기존 Cake 음악 종료
        CakeStageManager.stopMusic();

        // ✅ 결과 화면 전용 음악
        resultMusic = new Music("result_cake.mp3", false);
        resultMusic.start();
    }
}
