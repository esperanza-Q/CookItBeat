package game.Space;

import game.Main;
import game.Music;
import game.rhythm.RhythmJudgementManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    
    // ✅ [추가] 레이저 이미지 배열
    protected Image[] laserFrames;
    
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
    protected final int NEXT_STAGE_2_TIME_MS = 25 * 1000;
    protected final int NEXT_STAGE_3_TIME_MS = (int) (53.5 * 1000);

    private boolean isTransitionTriggered = false;
    private boolean isAutoPlaying = false;
    protected int[] autoPressTimes = {}; // ‼️ 이제 이 변수는 사용하지 않습니다.

    // ✅ [추가] 판정 시스템 관련
    protected RhythmJudgementManager judgementManager;
    private Image[] judgementImages = new Image[3]; // PERFECT, GREAT, MISS 이미지
    private Image currentJudgementImage = null;
    private String currentJudgementText = null;
    private Timer judgementTimer;
    private final int JUDGEMENT_DISPLAY_TIME_MS = 1000; // 판정 결과 표시 시간 (1초)

    private final int GLOBAL_JUDGEMENT_OFFSET_MS = -260;

    // ✅ [추가] 점수 오프셋 (이전 스테이지에서 이월된 점수)
    private int scoreOffset = 0;
    // ✅ [유지] 점수 표시 관련 변수 (단, 초기값은 0으로 유지)
    private Font scoreFont;
    private int currentScore = 0;


    // ‼️ [수정] 정답 타이밍을 받아 RhythmJudgementManager 초기화
    public SpaceAnimation(long[] correctTimings) {

        // ✅ [수정] StageManager에서 이월된 점수를 로드
        this.scoreOffset = StageManager.getTotalScore();
        // 초기 currentScore 설정
        this.currentScore = this.scoreOffset; // 화면 표시 점수 초기화

        // ✅ [수정] RhythmJudgementManager 초기화 시, 누적 점수(scoreOffset)를 함께 전달합니다.
        List<Long> timingsList = new ArrayList<>();
        for (long time : correctTimings) {
            timingsList.add(time);
        }
        // ‼️ [핵심 수정] judgementManager에 이월된 점수(scoreOffset)를 전달
        this.judgementManager = new RhythmJudgementManager(timingsList, this.scoreOffset);

        // ✅ 물총 이미지 프레임 초기화 (하위 클래스에서 사용)
        waterFrames = new Image[4];
        for (int i = 0; i < 4; i++) {
            waterFrames[i] = new ImageIcon(Main.class.getResource("../images/alienStage_image/water0" + (i + 1) + ".png")).getImage();
        }
        
        // ✅ 레이저 이미지 프레임 초기화 (하위 클래스에서 사용)
        laserFrames = new Image[2];
        for (int i = 0; i < 2; i++) {
            laserFrames[i] = new ImageIcon(Main.class.getResource("../images/alienStage_image/laser0" + (i + 1) + ".png")).getImage();
        }

        // ✅ [추가] 판정 이미지 로드
        judgementImages[0] = new ImageIcon(Main.class.getResource("../images/mainUI/acc_perfect.png")).getImage(); // PERFECT
        judgementImages[1] = new ImageIcon(Main.class.getResource("../images/mainUI/acc_good.png")).getImage();    // GOOD (GREAT 포함)
        judgementImages[2] = new ImageIcon(Main.class.getResource("../images/mainUI/acc_miss.png")).getImage();     // MISS


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

        // ✅ [추가] 점수 표시 폰트 설정
        // ✅ [추가] 폰트 파일을 로드하여 scoreFont 설정
        try {
            // 1. 폰트 파일을 InputStream으로 로드합니다. (프로젝트 내부 리소스 접근 방식)
            InputStream is = Main.class.getResourceAsStream("../fonts/LAB디지털.ttf");

            // ‼️ 주의: 폰트 파일 경로는 'game.Main' 클래스 위치 기준 상대 경로로 설정해야 합니다.
            // 예를 들어, 폰트 파일이 'src/fonts' 폴더에 있다면 위와 같이 경로를 지정합니다.

            if (is == null) {
                // 파일 로드 실패 시 예외 처리
                System.err.println("LAB디지털.ttf 폰트 파일을 찾을 수 없습니다. 기본 폰트를 사용합니다.");
                scoreFont = new Font("Arial", Font.BOLD, 24);
            } else {
                // 2. 파일을 기반으로 폰트 객체를 생성합니다 (PLAIN 스타일, 크기는 24)
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

                // 3. 원하는 스타일과 크기를 적용하여 최종 폰트 설정
                scoreFont = baseFont.deriveFont(Font.BOLD, 24f);
                is.close(); // InputStream 닫기
            }

        } catch (Exception e) {
            System.err.println("폰트 로딩 중 오류 발생: " + e.getMessage());
            scoreFont = new Font("Arial", Font.BOLD, 24); // 오류 발생 시 기본 폰트 사용
        }

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

                    Music.playEffect("water_pong2.mp3");
                    isHolding = true;
                    pressTime = System.currentTimeMillis();
                    autoReverse = false;

                    startForwardAnimation();

                    // ✅ [수정] 스페이스바 로직(판정 처리 포함)을 처리하는 protected 메서드 호출
                    processSpaceKeyPressLogic();
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
        setupJudgementTimer(); // ✅ 판정 결과 출력 타이머 초기화
        setFocusable(true);
        requestFocus();
    }

    // ‼️ 기본 생성자 (하위 클래스에서 호출하지 않음)
    public SpaceAnimation() {
        this(new long[]{});
    }


    // ✅ [추가] 판정 결과 출력 타이머 초기화
    private void setupJudgementTimer() {
        judgementTimer = new Timer(JUDGEMENT_DISPLAY_TIME_MS, e -> {
            currentJudgementImage = null;
            currentJudgementText = null;
            judgementTimer.stop();
            repaint();
        });
        judgementTimer.setRepeats(false);
    }

    // ✅ [수정] SpaceBar 키 입력 시 처리 로직
    protected void processSpaceKeyPressLogic() {
        // 1. 판정 로직 수행
        if (judgementManager != null) {

            // ‼️ 오프셋 적용된 음악 시간 계산: 입력 시간을 47ms 앞으로 당겨서 보정
            int adjustedMusicTime = currentMusicTimeMs + GLOBAL_JUDGEMENT_OFFSET_MS;

            // ‼️ 조정된 시간을 판정 함수에 전달
            judgementManager.handleInput(adjustedMusicTime);

            String judgement = judgementManager.getLastJudgement();


            // 2. 판정 결과 이미지 및 텍스트 설정
            switch (judgement) {
                case "PERFECT!":
                    currentJudgementImage = judgementImages[0];
                    break;
                case "GREAT!":
                case "GOOD":
                    currentJudgementImage = judgementImages[1];
                    break;
                case "MISS":
                    currentJudgementImage = judgementImages[2];
                    break;
            }
            currentJudgementText = judgement;

            // 3. 타이머 재시작 (1초간 이미지 표시)
            if (judgementTimer.isRunning()) {
                judgementTimer.stop();
            }
            judgementTimer.start();
        }

        // 4. 하위 클래스(Stage1)의 추가 애니메이션 로직 호출
        processSpaceKeyPress();
    }

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

        // ✅ [추가] 점수 그리기
        g.setColor(Color.WHITE); // 점수 글자 색상
        g.setFont(scoreFont);

        // 점수 텍스트 생성
        String scoreText = String.format("%d", currentScore);

        // 오른쪽 상단에 위치 설정 (오른쪽에서 20px, 위쪽에서 60px 떨어진 곳)
        FontMetrics fm = g.getFontMetrics(scoreFont);
        int scoreX = getWidth() - fm.stringWidth(scoreText) - 40;
        int scoreY = 48; // 진행 바보다 아래에 위치

        // 그림자 효과를 위해 살짝 아래와 오른쪽에 검은색으로 먼저 그림
        g.setColor(new Color(0, 0, 0, 150)); // 반투명 검은색
        g.drawString(scoreText, scoreX + 3, scoreY + 3);

        // 실제 점수 그리기
        g.setColor(Color.WHITE);
        g.drawString(scoreText, scoreX, scoreY);
        
        drawStageObjects(g);
        drawJudgement(g); // ✅ 판정 결과 그리기
    }

    // ✅ [추가] 판정 결과 이미지를 화면 중앙에 그리는 메서드
    private void drawJudgement(Graphics g) {
        if (currentJudgementImage != null) {
            int imgWidth = 200; // 적절한 크기
            int imgHeight = 200;
            int imgX = (getWidth() - imgWidth) / 2;
            int imgY = (getHeight() - imgHeight) / 2;
            g.drawImage(currentJudgementImage, imgX, imgY, imgWidth, imgHeight, this);

            // 텍스트는 이미지 아래에 작게 출력 (선택 사항)
//            if (currentJudgementText != null) {
//                g.setColor(Color.WHITE);
//                g.setFont(new Font("Arial", Font.BOLD, 24));
//                FontMetrics fm = g.getFontMetrics();
//                int textX = (getWidth() - fm.stringWidth(currentJudgementText)) / 2;
//                int textY = imgY + imgHeight + 30; // 이미지 아래
//                g.drawString(currentJudgementText, textX, textY);
//            }
        }
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
//        super.updateByMusicTime(t);

        this.currentMusicTimeMs = t;

        int totalLength = StageManager.musicLengthMs;
        if (totalLength > 0) {

            double progress = (double) t / totalLength;

            // ✅ barX를 여기서도 다시 계산
            int barX = getWidth() - BAR_WIDTH - 20;

            // ✅ 왼 → 오 이동 (BAR_WIDTH 사용)
            this.spaceshipX = barX + (int) (progress * BAR_WIDTH);

            processStageEvents(t);
            
            if (!isTransitionTriggered && t >= NEXT_STAGE_2_TIME_MS && t < NEXT_STAGE_3_TIME_MS) {
                isTransitionTriggered = true;
                SwingUtilities.invokeLater(this::requestStage2Change);
            }
            
            if (isTransitionTriggered && t >= NEXT_STAGE_3_TIME_MS) {
                isTransitionTriggered = false;
                SwingUtilities.invokeLater(this::requestStage3Change);
            }
        }

        // ✅ [수정] 현재 점수 갱신 및 StageManager에 저장
        if (judgementManager != null) {
            int currentStageScore = judgementManager.getScore();

            // ‼️ [수정] 이월된 점수 + 현재 스테이지 점수를 합산하여 전체 점수를 StageManager에 저장합니다.
            int totalGameScore = this.scoreOffset + currentStageScore;
            StageManager.setTotalScore(totalGameScore);

            // ‼️ [추가] 로컬 currentScore 변수를 갱신하여 paintComponent에서 올바른 점수를 그리도록 합니다.
            this.currentScore = totalGameScore;
        }

        repaint();
    }

    protected void drawStageObjects(Graphics g) {}
    
    private void requestStage2Change() {
        Container parent = this.getParent();
        if (parent instanceof SpacePanel) ((SpacePanel) parent).switchToStage2Panel();
        else System.err.println("Error: SpaceAnimation's parent is not SpacePanel.");
    }
    
    // ✅ [추가] 스테이지3으로의 변환 메서드를 추가
    private void requestStage3Change() {
        Container parent = this.getParent();
        if (parent instanceof SpacePanel) ((SpacePanel) parent).switchToStage3Panel();
        else System.err.println("Error: SpaceAnimation's parent is not SpacePanel.");
    }


    public Image getCannon() { return null; }

    protected void changeStageImageOnPress() {}
    protected void changeStageImageOnRelease() {}
    protected void processStageEvents(int t) {}
    protected boolean isTimeInputBlocked() { return false; }
}