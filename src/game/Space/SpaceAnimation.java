package game.Space;

import game.Main;
import game.Music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import static game.Space.StageManager.spaceBackgroundMusic;

public class SpaceAnimation extends JPanel {

    private Image background;
    private Image controller;
    private Image L_control01, L_control02, L_control03, L_control04, L_control05;
    private Image R_control01, R_control02, R_control03, R_control04;
    private Image L_currentControlImage;
    private Image R_currentControlImage;
    private Image ufo;

    private boolean isAnimating = false; // 컨트롤러 중복 애니메이션 방지
    private Timer forwardTimer, reverseTimer;
    private int frameIndex = 0;
    private Image[] rightFrames; // 컨트롤러 애니메이션 프레임 배열

    // ‼️ [유지] 물총 이미지 배열만 protected로 유지 (하위 클래스 사용 목적)
    protected Image[] waterFrames;
    // ‼️ 물총 관련 Timer, Index, Image 변수 및 로직은 SpaceStage1로 이동

    //‼️애니메이션 버전
    private Image planets1;
    private double t = 0;
    private double speed = 0.05;

    private boolean isHolding = false;
    private long pressTime;
    private final long TAP_THRESHOLD = 250;
    private boolean autoReverse = false;

    // ✅ 현재 음악 재생 시간 (ms)을 저장
    protected int currentMusicTimeMs = 0;

    // ✅ 음악 진행 바 관련
    private Image progressBarBackground;
    private Image spaceshipIcon;
    private int spaceshipX;

    // ✅ 바 너비/높이는 그대로 유지
    private final int BAR_WIDTH = 450;
    private final int BAR_HEIGHT = 40;
    private final int BAR_Y = 20;

    // ✅ 스테이지 전환 시간 설정 (예: 음악 시작 후 25초)
    protected final int NEXT_STAGE_TIME_MS = 25 * 1000;

    private boolean isTransitionTriggered = false;
    private boolean isAutoPlaying = false;
    protected int[] autoPressTimes = {};
    private int nextAutoPressIndex = 0;

    public SpaceAnimation() {

        // ✅ 물총 이미지 프레임 초기화 (하위 클래스에서 사용)
        waterFrames = new Image[4];
        for (int i = 0; i < 4; i++) {
            waterFrames[i] = new ImageIcon(Main.class.getResource("../images/alienStage_image/water0" + (i + 1) + ".png")).getImage();
        }

        // ‼️ 물총 애니메이션 타이머 설정 및 로직 제거

        // ✅ 우주선은 진행바 왼쪽에서 시작 (왼→오 이동)
        this.spaceshipX = 0;

        background = new ImageIcon(Main.class.getResource("../images/alienStage_image/Background(deco_x).png")).getImage();
        planets1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/Background_deco2.png")).getImage();

        controller = new ImageIcon(Main.class.getResource("../images/alienStage_image/controller.png")).getImage();

        L_control01 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control01.png")).getImage();
        L_control02 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control02.png")).getImage();
        L_control03 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control03.png")).getImage();
        L_control04 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control04.png")).getImage();
        L_control05 = new ImageIcon(Main.class.getResource("../images/alienStage_image/L_control05.png")).getImage();

        R_control01 = new ImageIcon(Main.class.getResource("../images/alienStage_image/R_control01.png")).getImage();
        R_control02 = new ImageIcon(Main.class.getResource("../images/alienStage_image/R_control02.png")).getImage();
        R_control03 = new ImageIcon(Main.class.getResource("../images/alienStage_image/R_control03.png")).getImage();
        R_control04 = new ImageIcon(Main.class.getResource("../images/alienStage_image/R_control04.png")).getImage();

        L_currentControlImage = L_control01;
        rightFrames = new Image[]{R_control01, R_control02, R_control03, R_control04};
        R_currentControlImage = R_control01;

        ufo = new ImageIcon(Main.class.getResource("../images/alienStage_image/ufo.png")).getImage();

        // ✅ 진행 바 & 우주선 아이콘 로드
        progressBarBackground = new ImageIcon(Main.class.getResource("../images/mainUI/alienStage_progBar.png")).getImage();
        spaceshipIcon = new ImageIcon(Main.class.getResource("../images/mainUI/alienStage_progIcon.png")).getImage();

        Timer timer = new Timer(8, e -> { t += speed; repaint(); });
        timer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: L_currentControlImage = L_control05; break;
                    case KeyEvent.VK_A: L_currentControlImage = L_control02; break;
                    case KeyEvent.VK_S: L_currentControlImage = L_control04; break;
                    case KeyEvent.VK_D: L_currentControlImage = L_control03; break;
                }
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                L_currentControlImage = L_control01;
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_SPACE) {

                    Music.playEffect("water4.mp3");
                    isHolding = true;
                    pressTime = System.currentTimeMillis();
                    autoReverse = false;

                    startForwardAnimation();

                    // ‼️ [제거] 물총 애니메이션 시작 로직 제거. 하위 클래스에서 오버라이딩하여 호출해야 함.
                    // startWaterAnimation();
                    // ✅ [수정] 스페이스바 로직을 처리하는 protected 메서드 호출
                    processSpaceKeyPress();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && isHolding) {
                    isHolding = false;
                }
            }
        });

        setupAnimationTimers();
        setFocusable(true);
        requestFocus();
    }

    // ‼️ 물총 애니메이션 타이머 설정 메서드 제거
    // private void setupWaterAnimationTimer() {...}

    // ‼️ 물총 애니메이션 시작 메서드 제거 (하위 클래스에서 필요하다면 오버라이드할 수 있도록)
//    protected void startWaterAnimation() {}

    // ✅ [추가] SpaceBar 키 입력 시 하위 클래스가 로직을 추가할 수 있도록 메서드 추출
    protected void processSpaceKeyPress() {
        // SpaceStage1에서 이 메서드를 오버라이드하여 물총 애니메이션을 추가할 것입니다.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // ✅ 바를 화면 오른쪽 끝으로 이동시키기 위한 X 계산
        int barX = getWidth() - BAR_WIDTH - 20;

        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        double period = 50;
        double progress = (t % period) / period;
        double eased1 = (progress * progress * progress);
        double scale1 = 0.00001 + eased1 * 3;
        if (scale1 < 0.001) scale1 = 0.001;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double pivotX = getWidth() / 2.0;
        double pivotY = getHeight() / 2.0 - 80;

        AffineTransform at = new AffineTransform();
        at.rotate(t * 0.08, pivotX, pivotY);
        at.translate(pivotX - (planets1.getWidth(null) * scale1) / 2,
                pivotY - (planets1.getHeight(null) * scale1) / 2);
        at.scale(scale1, scale1);
        g2.drawImage(planets1, at, this);
        g2.dispose();

        g.drawImage(ufo, 0, 0, getWidth(), getHeight(), this);

        SpaceAnimation current = StageManager.getCurrentStage();
        if (current != null) {
            Image cannonImage = current.getCannon();
            if (cannonImage != null) g.drawImage(cannonImage, 0, 0, null);
        }

        g.drawImage(controller, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(L_currentControlImage, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(R_currentControlImage, 0, 0, getWidth(), getHeight(), this);

        // ✅ 진행 바 그리기 (오른쪽)
        g.drawImage(progressBarBackground, barX, BAR_Y, BAR_WIDTH, BAR_HEIGHT, this);

        // ✅ 우주선 그리기
        if (spaceshipIcon != null) {
            int iconSize = BAR_HEIGHT + 10;
            int iconY = BAR_Y + (BAR_HEIGHT - iconSize) / 2;
            g.drawImage(spaceshipIcon, spaceshipX - iconSize / 2, iconY, iconSize, iconSize, this);
        }

        drawStageObjects(g);

        // ‼️ [제거] 물총 그리는 로직 제거
    }

    private void startForwardAnimation() {
        if (forwardTimer.isRunning()) return;
        if (reverseTimer.isRunning()) reverseTimer.stop();

        frameIndex = 0;
        R_currentControlImage = rightFrames[frameIndex];
        changeStageImageOnPress();
        isAnimating = true;
        forwardTimer.start();
        repaint();
    }

    private void startReverseAnimation() {
        if (isAnimating) return;
        isAnimating = true;
        forwardTimer.stop();
        reverseTimer.start();
    }

    private void setupAnimationTimers() {

        forwardTimer = new Timer(2, e -> {
            if (frameIndex < rightFrames.length - 1) {
                frameIndex++;
                R_currentControlImage = rightFrames[frameIndex];
                repaint();
            } else {
                forwardTimer.stop();
                isAnimating = false;
                startReverseAnimation();
            }
        });

        reverseTimer = new Timer(2, e -> {
            if (frameIndex > 0) {
                frameIndex--;
                R_currentControlImage = rightFrames[frameIndex];
                repaint();
            } else {
                reverseTimer.stop();
                isAnimating = false;
                changeStageImageOnRelease();
            }
        });
    }

    protected void updateByMusicTime(int t) {
        this.currentMusicTimeMs = t;

        int totalLength = StageManager.musicLengthMs;
        if (totalLength > 0) {

            double progress = (double) t / totalLength;

            // ✅ barX를 여기서도 다시 계산
            int barX = getWidth() - BAR_WIDTH - 20;

            // ✅ 왼 → 오 이동 (BAR_WIDTH 사용)
            this.spaceshipX = barX + (int) (progress * BAR_WIDTH);

            processStageEvents(t);

            if (!isTransitionTriggered && t >= NEXT_STAGE_TIME_MS) {
                isTransitionTriggered = true;
                SwingUtilities.invokeLater(this::requestStageChange);
            }
        }

        repaint();
    }

    protected void drawStageObjects(Graphics g) {}

    private void requestStageChange() {
        Container parent = this.getParent();
        if (parent instanceof SpacePanel) ((SpacePanel) parent).switchToNextPanel();
        else System.err.println("Error: SpaceAnimation's parent is not SpacePanel.");
    }

    public Image getCannon() { return null; }

    protected void changeStageImageOnPress() {}
    protected void changeStageImageOnRelease() {}
    protected void processStageEvents(int t) {}
    protected boolean isTimeInputBlocked() { return false; }
}