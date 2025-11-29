package game.Cake;

import game.Main; // Main 클래스의 loadImage를 사용하기 위해 임포트
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.TimerTask;
import java.util.Timer; // java.util.Timer 사용 (클릭 후 복구 타이머)

public class SurprisePanel extends JPanel {

    private CakePanel cakePanel;
    private Image backgroundImage;
    private Image shadowImage;

    // 🍓 애니메이션 관련 이미지
    private Image spearImage;
    private Image strawberryImage;

    // 🍓 이미지 경로 상수
    private static final String BG_PATH = "../images/cakeStage_image/stage1/Background_stage1-1.png";
    private static final String SHADOW_PATH = "../images/cakeStage_image/surprise/shadow_surprise.png";
    private static final String SPEAR_01_PATH = "../images/cakeStage_image/surprise/Spear01_surprise.png";
    private static final String SPEAR_02_PATH = "../images/cakeStage_image/surprise/Spear02_surprise.png";
    private static final String STRAW_01_PATH = "../images/cakeStage_image/surprise/BigStrawberry01_surprise.png";
    private static final String STRAW_02_PATH = "../images/cakeStage_image/surprise/BigStrawberry02_surprise.png";

    // 🍓 애니메이션 및 상태 변수
    private int strawberryY = -100; // 초기 Y 위치 (화면 밖)
    private int STRAWBERRY_TARGET_Y = 150; // 동적으로 설정될 변수
    private final int STRAWBERRY_SPEED = 2; // 떨어지는 속도

    private boolean isSpearClicked = false;
    private boolean isStrawberryClicked = false;
    private final long CLICK_DISPLAY_DURATION = 200; // 0.2초 동안 이미지 변경 유지

    // 🍓 로드된 이미지 저장소
    private Image spear01;
    private Image spear02;
    private Image straw01;
    private Image straw02;


    public SurprisePanel(CakePanel panel) {
        this.cakePanel = panel;

        // 1. 이미지 로드 및 초기화
        loadImages();

        // 초기 이미지 설정
        spearImage = spear01;
        strawberryImage = straw01;

        // 2. 패널 기본 설정
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        // 3. 라벨 (주석 처리됨)
//        JLabel surpriseLabel = new JLabel("기습 스테이지! 10초 후 Stage 1-2로 전환됩니다.", SwingConstants.CENTER);
//        surpriseLabel.setFont(new Font("Arial", Font.BOLD, 40));
//        surpriseLabel.setForeground(Color.WHITE);
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.weighty = 0.3;
//        gbc.anchor = GridBagConstraints.CENTER;
//        add(surpriseLabel, gbc);

        // 4. 애니메이션 타이머 시작
        startStrawberryAnimation();

        // 5. 마우스 이벤트 리스너 등록
        addMouseListener(new SurpriseMouseListener());
    }

    // ----------------------------------------------------
    // 🖼️ 리소스 로드 로직
    // ----------------------------------------------------

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

    private void loadImages() {
        backgroundImage = loadImage(BG_PATH);
        shadowImage = loadImage(SHADOW_PATH);

        spear01 = loadImage(SPEAR_01_PATH);
        spear02 = loadImage(SPEAR_02_PATH);
        straw01 = loadImage(STRAW_01_PATH);
        straw02 = loadImage(STRAW_02_PATH);

        if (backgroundImage == null || shadowImage == null || spear01 == null || straw01 == null) {
            System.err.println("🔴 SurprisePanel 이미지 로드 실패!");
        }
    }

    // ----------------------------------------------------
    // 🍓 애니메이션 로직
    // ----------------------------------------------------

    private void startStrawberryAnimation() {
        // ‼️ [목표 Y 계산] 딸기가 그림자 위에 멈추도록 목표 Y 좌표를 계산
        if (strawberryImage != null && shadowImage != null) {
            int shadowH = shadowImage.getHeight(this);
            int strawH = strawberryImage.getHeight(this);
            int panelH = getHeight(); // 이 값은 초기에는 0일 수 있습니다.

            // 안전한 계산을 위해 충분한 높이가 확보되었을 때만 계산합니다.
            if (panelH > 0 && panelH > strawH + shadowH) {
                // 패널 바닥 - 그림자 높이 - 딸기 높이
                STRAWBERRY_TARGET_Y = panelH - shadowH - strawH;
            } else {
                STRAWBERRY_TARGET_Y = 400; // 임시 값
            }
        } else {
            STRAWBERRY_TARGET_Y = 400;
        }

        // javax.swing.Timer를 사용하여 15ms마다 액션 수행 (EDT 안전)
        new javax.swing.Timer(15, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 딸기 위치 업데이트 (떨어지는 속도)
                if (strawberryY < STRAWBERRY_TARGET_Y) {
                    strawberryY += STRAWBERRY_SPEED;
                    if (strawberryY > STRAWBERRY_TARGET_Y) {
                        strawberryY = STRAWBERRY_TARGET_Y;

                        // 목표 지점에 도달하면 타이머를 멈춥니다.
                        ((javax.swing.Timer) e.getSource()).stop();
                    }
                }
                repaint(); // 다시 그리기를 요청하여 애니메이션 효과 구현
            }
        }).start();
    }

    // ----------------------------------------------------
    // 🐭 내부 마우스 리스너 (클릭 처리)
    // ----------------------------------------------------

    private class SurpriseMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {

            Point clickPoint = e.getPoint();

            // ‼️ [Spear 클릭 영역]
            int spearW = spearImage.getWidth(SurprisePanel.this);
            int spearH = spearImage.getHeight(SurprisePanel.this);
            int spearX = (getWidth() - spearW) / 2;
            int spearY = getHeight() / 2 - 50;
            Rectangle spearBounds = new Rectangle(spearX, spearY, spearW, spearH);

            // ‼️ [Strawberry 클릭 영역]
            int strawW = strawberryImage.getWidth(SurprisePanel.this);
            int strawH = strawberryImage.getHeight(SurprisePanel.this);
            Rectangle strawBounds = new Rectangle(getWidth()/2 - strawW/2, strawberryY, strawW, strawH);

            boolean imageChanged = false;

            // 1. Spear 클릭 처리
            if (spearBounds.contains(clickPoint) && !isSpearClicked) {
                spearImage = spear02;
                isSpearClicked = true;
                imageChanged = true;
            }

            // 2. Strawberry 클릭 처리
            if (strawBounds.contains(clickPoint) && !isStrawberryClicked) {
                strawberryImage = straw02;
                isStrawberryClicked = true;
                imageChanged = true;
            }

            // 3. 이미지 변경 후 복구 타이머 시작
            if (imageChanged) {
                repaint();

                // javax.swing.Timer를 사용하여 짧은 시간 후 원래대로 복구
                new javax.swing.Timer((int)CLICK_DISPLAY_DURATION, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        if (isSpearClicked) {
                            spearImage = spear01;
                            isSpearClicked = false;
                        }
                        if (isStrawberryClicked) {
                            strawberryImage = straw01;
                            isStrawberryClicked = false;
                        }
                        repaint();
                        ((javax.swing.Timer)evt.getSource()).stop();
                    }
                }).start();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // 현재는 추가 로직 없음
        }
    }

    // ----------------------------------------------------
    // 🎨 그리기 로직
    // ----------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. 배경 이미지 그리기
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. 그림자 이미지 그리기 (바닥 중앙)
        if (shadowImage != null) {
            g2.drawImage(shadowImage, 0, 0, getWidth(), getHeight(), this);
        }

        // 3. Spear 이미지 그리기 (화면 중앙 근처 고정)
        if (spearImage != null) {
            g2.drawImage(spearImage, 0, 0, getWidth(), getHeight(), this);
        }

        // 4. Strawberry 이미지 그리기 (애니메이션 위치)
        if (strawberryImage != null) {
            g2.drawImage(strawberryImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}