package game.Space;

import game.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

public class SpaceAnimation extends JPanel {

    private Image background;
    private Image controller;
    private Image L_control01, L_control02, L_control03, L_control04, L_control05;
    private Image R_control01, R_control02, R_control03, R_control04;
    private Image L_currentControlImage;
    private Image R_currentControlImage;

    private boolean isAnimating = false; // 중복 애니메이션 방지
    private Timer forwardTimer, reverseTimer;
    private int frameIndex = 0;
    private Image[] rightFrames; // 애니메이션 프레임 배열

    //‼️애니메이션 버전
    private Image planets1;
    private double t = 0;
    private double speed = 0.05;

    private boolean isHolding = false;
    private long pressTime;
    private final long TAP_THRESHOLD = 250; // 0.15초 이하면 "짧게 누름"
    private boolean autoReverse = false;

    //애니메이션 버전
    public SpaceAnimation() {

        //배경
        background = new ImageIcon(Main.class.getResource("../images/alienStage_image/Background(deco_x).png")).getImage();
        planets1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/Background_deco2.png")).getImage();
//        planets2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/Background_deco3.png")).getImage();

        //조종칸
        controller = new ImageIcon(Main.class.getResource("../images/alienStage_image/controller.png")).getImage();

        //왼쪽 컨트롤러
        L_control01 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control01.png")).getImage();
        L_control02 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control02.png")).getImage();
        L_control03 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control03.png")).getImage();
        L_control04 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control04.png")).getImage();
        L_control05 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control05.png")).getImage();

        //오른쪽 컨트롤러
        R_control01 = new ImageIcon(Main.class.getResource("../images/alienStage_image/R_control01.png")).getImage();
        R_control02 = new ImageIcon(Main.class.getResource("../images/alienStage_image/R_control02.png")).getImage();
        R_control03 = new ImageIcon(Main.class.getResource("../images/alienStage_image/R_control03.png")).getImage();
        R_control04 = new ImageIcon(Main.class.getResource("../images/alienStage_image/R_control04.png")).getImage();

        L_currentControlImage = L_control01;
        rightFrames = new Image[]{R_control01, R_control02, R_control03, R_control04};
        R_currentControlImage = R_control01;


        Timer timer = new Timer(8, e -> {  // 60FPS
            t += speed;
            repaint();
        });
        timer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        L_currentControlImage = L_control05;
                        break;
                    case KeyEvent.VK_A:
                        L_currentControlImage = L_control02;
                        break;
                    case KeyEvent.VK_S:
                        L_currentControlImage = L_control04;
                        break;
                    case KeyEvent.VK_D:
                        L_currentControlImage = L_control03;
                        break;
                }
                repaint();
            }


            @Override
            public void keyReleased(KeyEvent e) {
                // 키를 떼면 무조건 기본 이미지로 복구
                L_currentControlImage = L_control01;
                repaint();
            }
        });

        // ✅ 스페이스바 입력 이벤트
        addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && !isHolding) {
                    isHolding = true;
                    pressTime = System.currentTimeMillis();
                    autoReverse = false;   // 초기화

                    startForwardAnimation(); // 순방향 시작
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && isHolding) {
                    isHolding = false;
                    long duration = System.currentTimeMillis() - pressTime;

                    if (duration <= TAP_THRESHOLD) {
                        // ✅ 짧게 눌렀음 → forward 끝나면 자동 reverse
                        autoReverse = true;

                    } else {
                        // ✅ 길게 눌렀음 → 바로 역방향
                        startReverseAnimation();
                    }
                }
            }
        });

        setupAnimationTimers();

        // 🔥 여기서 포커스 설정
        setFocusable(true);
        requestFocus();  // 패널이 그려지는 시점에 포커스를 받도록


    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 배경
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        // 행성
        double period = 50;
        double progress = (t % period) / period;  // 0~1

        // 부드럽게 커지는 이징
        double eased1 = (progress * progress * progress);
//        double eased1 = Math.pow(progress, 1.5); // 1.2 ~ 1.7 사이에서 조절 가능
        double scale1 = 0.00001 + eased1 * 3;
        if (scale1 < 0.001) scale1 = 0.001;

        // 회전과 스케일을 포함한 변환 행렬 생성
        Graphics2D g2 = (Graphics2D) g.create();

        // 부드러운 이미지 스케일링
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ★ 회전 중심 (절대 위치로 고정 — 떨림 방지)
        double pivotX = getWidth() / 2.0;
        double pivotY = getHeight() / 2.0 - 80; // 원한다면 중심을 위로 올림

        AffineTransform at = new AffineTransform();

        // 1) 회전
        at.rotate(t * 0.08, pivotX, pivotY);

        // 2) 스케일 (pivot을 기준으로 스케일하려면 translate 필요)
        at.translate(pivotX - (planets1.getWidth(null) * scale1) / 2,
                pivotY - (planets1.getHeight(null) * scale1) / 2);

        // 3) 스케일 적용
        at.scale(scale1, scale1);

        // 그리기
        g2.drawImage(planets1, at, this);
        g2.dispose();


        // 컨트롤러 (원하는 위치에 그리기)
        g.drawImage(controller, 0, 0, getWidth(), getHeight(), this);

        //왼쪽 컨트롤러
        g.drawImage(L_currentControlImage, 0, 0, getWidth(), getHeight(), this);

        //오른쪽 컨트롤러
        g.drawImage(R_currentControlImage, 0, 0, getWidth(), getHeight(), this);

        // ✅ Stage별 추가요소 hook
        drawStageObjects(g);
    }

    //스페이스바 관련
    private void startForwardAnimation() {
        if (isAnimating) return;
        isAnimating = true;
        reverseTimer.stop();
        forwardTimer.start();
    }

    private void startReverseAnimation() {
        if (isAnimating) return;
        isAnimating = true;
        forwardTimer.stop();
        reverseTimer.start();
    }

    private void setupAnimationTimers() {

        // 눌렀을 때 (1 → 4 순차)
        forwardTimer = new Timer(32, e -> {
            if (frameIndex < rightFrames.length - 1) {
                frameIndex++;
                R_currentControlImage = rightFrames[frameIndex];
                repaint();
            } else {
                forwardTimer.stop();
                isAnimating = false;

                // ✅ forward 애니 끝났고, 짧게 눌렀다면 자동 reverse 실행
                if (autoReverse) {
                    autoReverse = false;
                    startReverseAnimation();
                }
            }
        });

        reverseTimer = new Timer(70, e -> {
            if (frameIndex > 0) {
                frameIndex--;
                R_currentControlImage = rightFrames[frameIndex];
                repaint();
            } else {
                reverseTimer.stop();
                isAnimating = false;
            }
        });
    }


//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//
//        // 배경
//        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
//
//        // 공통 UI
//        g.drawImage(controller, 0, 0, getWidth(), getHeight(), this);
//        g.drawImage(L_currentControlImage, 0, 0, getWidth(), getHeight(), this);
//        g.drawImage(R_currentControlImage, 0, 0, getWidth(), getHeight(), this);
//
//
//    }

    protected void drawStageObjects(Graphics g) {
        // 기본은 아무 것도 안 그림
    }

}