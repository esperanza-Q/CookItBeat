package game.Space;

import game.Main;

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

//    protected Timer syncTimer;

    // ✅ 음악 진행 바 관련
    private Image progressBarBackground; // 위에서 올린 progress bar 배경 (검은색 바와 3개의 청록색 블록)
    private Image spaceshipIcon;         // 위에서 올린 우주선 아이콘 (혹은 적절한 아이콘)
    private int spaceshipX;              // 우주선 아이콘의 X 좌표
    private final int BAR_X = 20;        // 바를 그릴 시작 X 좌표
    private final int BAR_Y = 20;        // 바를 그릴 시작 Y 좌표
    private final int BAR_WIDTH = 450;   // 바의 너비 (조정 필요)
    private final int BAR_HEIGHT = 40;   // 바의 높이 (조정 필요)

    // ✅ 스테이지 전환 시간 설정 (예: 음악 시작 후 10초)
    protected final int NEXT_STAGE_TIME_MS = 25 * 1000;

    private boolean isTransitionTriggered = false; // 전환 중복 방지 플래그

    //애니메이션 버전
    public SpaceAnimation() {
        // ✅ 우주선 위치 초기화 추가
        // 우주선은 바의 오른쪽 끝에서 시작합니다.
        this.spaceshipX = BAR_X + BAR_WIDTH;

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

        // ✅ 음악 진행 바 이미지 로드
        progressBarBackground = new ImageIcon(Main.class.getResource("../images/mainUI/alienStage_progBar.png")).getImage(); // 파일명을 적절히 변경하세요.
        spaceshipIcon = new ImageIcon(Main.class.getResource("../images/mainUI/alienStage_progIcon.png")).getImage(); // 파일명을 적절히 변경하세요.


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

        addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) { // isHolding 검사 제거
                    isHolding = true;
                    // pressTime, autoReverse 초기화 로직은 그대로 유지 (다른 로직에 영향 줄 수 있음)
                    pressTime = System.currentTimeMillis();
                    autoReverse = false;   // 초기화

                    startForwardAnimation(); // 순방향 시작
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && isHolding) {
                    isHolding = false;

                    // autoReverse = true; 로직 제거
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

        // ----------------------------------------------------------------------
        // ✅ [핵심 수정 위치] 캐논 그리기 (행성 위에, 컨트롤러보다 뒤에)
        // ----------------------------------------------------------------------
        SpaceAnimation current = StageManager.getCurrentStage();
        if (current != null) {
            // 현재 스테이지가 제공하는 고유의 캐논 이미지를 가져옴
            Image cannonImage = current.getCannon();

            if (cannonImage != null) {
                g.drawImage(cannonImage, 0, 0, null); // ‼️ 캐논을 행성 위에 그립니다.
            }
        }
        // ----------------------------------------------------------------------


        // 컨트롤러 (원하는 위치에 그리기)
        g.drawImage(controller, 0, 0, getWidth(), getHeight(), this);

        //왼쪽 컨트롤러
        g.drawImage(L_currentControlImage, 0, 0, getWidth(), getHeight(), this);

        //오른쪽 컨트롤러
        g.drawImage(R_currentControlImage, 0, 0, getWidth(), getHeight(), this);

        // ----------------------------------------------------------------------
        // ✅ 음악 진행 바 및 우주선 그리기 (새로 추가)
        // ----------------------------------------------------------------------

        // 1. 진행 바 배경 그리기
        if (progressBarBackground != null) {
            g.drawImage(progressBarBackground, BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, this);
        }

        // 2. 우주선 아이콘 그리기
        if (spaceshipIcon != null) {
            int iconSize = BAR_HEIGHT + 10; // 아이콘 크기를 바 높이보다 약간 크게 설정
            int iconY = BAR_Y + (BAR_HEIGHT - iconSize) / 2; // 바 중앙에 오도록 Y 좌표 계산

            // spaceshipX는 바의 *진행* 좌표이고, 실제 우주선은 그 중앙에 위치해야 함
            g.drawImage(spaceshipIcon, spaceshipX - iconSize / 2, iconY, iconSize, iconSize, this);
        }
        // ----------------------------------------------------------------------


        // ✅ Stage별 추가요소 hook
        drawStageObjects(g);

    }

    private void startForwardAnimation() {

        // 1. 순방향 애니메이션이 이미 진행 중이면 무시 (중간에 리셋되는 것을 방지)
        if (forwardTimer.isRunning()) {
            return;
        }

        // 2. ✅ [수정] 역방향 애니메이션이 진행 중이라면 즉시 중단
        if (reverseTimer.isRunning()) {
            reverseTimer.stop();
            isAnimating = false; // 전체 애니메이션 잠금 해제
        }

        // 3. 애니메이션을 항상 시작 프레임(R_control01)으로 리셋
        frameIndex = 0;
        R_currentControlImage = rightFrames[frameIndex];

        // 4. 순방향 애니메이션 시작
        isAnimating = true;
        forwardTimer.start();

        // 강제 repaint로 키 입력에 대한 시각적 반응 속도 증가
        repaint();
    }

    private void startReverseAnimation() {
        if (isAnimating) return;
        isAnimating = true;
        forwardTimer.stop();
        reverseTimer.start();
    }

    private void setupAnimationTimers() {

        // 눌렀을 때 (1 → 4 순차)
        forwardTimer = new Timer(2, e -> {
            if (frameIndex < rightFrames.length - 1) {
                frameIndex++;
                R_currentControlImage = rightFrames[frameIndex];
                repaint();
            } else {
                // 🔥 순방향 애니메이션이 끝에 도달했을 때 (frameIndex == 3)
                forwardTimer.stop();
                isAnimating = false; // 현재 순방향 애니메이션 종료

                // ✅ [수정 핵심] 길게 누름/짧게 누름 상관없이 무조건 역방향 시작
                startReverseAnimation();

                // 기존의 autoReverse 로직은 제거되었습니다.
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
            }
        });
    }

    // 🔥 스테이지마다 오버라이드해서 쓰는 메서드 (공통 진행 바 로직 포함)
    protected void updateByMusicTime(int t) {
        int totalLength = StageManager.musicLengthMs;

        // 1. ✅ 우주선 위치 갱신 로직 재추가
        if (totalLength > 0) {
            // 진행률 계산: 0.0 (시작) ~ 1.0 (끝)
            double progress = (double) t / totalLength;

            int startX = BAR_X + BAR_WIDTH; // 바의 오른쪽 끝 (0% 진행)
            int endX = BAR_X;               // 바의 왼쪽 끝 (100% 진행)

            // 오른쪽에서 왼쪽으로 이동하는 좌표 계산
            this.spaceshipX = startX - (int) (progress * BAR_WIDTH);

            // -------------------------------------------------------------
            // 2. 스테이지 전환 로직 (음악이 끝나기 전)
            if (!isTransitionTriggered && t >= NEXT_STAGE_TIME_MS) {
                isTransitionTriggered = true;

                // 화면 전환 요청
                SwingUtilities.invokeLater(this::requestStageChange);
            }
        }

        // ✅ 우주선 위치가 바뀌었으므로 화면 갱신
        repaint();
    }

    protected void drawStageObjects(Graphics g) {}

    // ✅ 화면 전환을 요청하는 메서드 (기존 로직 재활용)
    private void requestStageChange() {
        Container parent = this.getParent();
        if (parent instanceof SpacePanel) {
            // SpacePanel의 다음 스테이지 전환 메서드 호출
            ((SpacePanel) parent).switchToNextPanel();
        } else {
            System.err.println("Error: SpaceAnimation's parent is not SpacePanel.");
        }
    }

    // ✅ 스테이지별 캐논 이미지를 반환하는 메서드 추가
// 스테이지에 따라 구현이 달라지므로, 하위 클래스에서 오버라이드해야 합니다.
    public Image getCannon() {
        // 기본적으로 null을 반환하거나, StageManager에 있는 기본 이미지를 반환할 수 있습니다.
        // 여기서는 Stage별 구현을 강제하기 위해 기본적으로 null을 반환합니다.
        return null;
    }
}