package game.Cake;

import game.Main; // Main 클래스의 loadImage를 사용하기 위해 임포트
import game.Music; // 💡 [추가] Music 클래스 임포트
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SurprisePanel extends JPanel {

    // ‼️ [수정] 미션 제한 시간을 10초로 변경
    private final long MISSION_DURATION_MS = 10000; // ‼️ 10초 미션 시간
    // ‼️ SUCCESS_DISPLAY_DURATION을 5000ms로 사용자 요청에 따라 수정
    private final long SUCCESS_DISPLAY_DURATION = 5000;
    private Timer gameTimer; // 미션 시간 카운트다운 타이머
    private long startTime; // 미션 시작 시간

    // ‼️ 주의: CakePanel에 switchNextStageOnSuccess() 메서드를 호출해야 합니다.
    private CakePanel cakePanel;
    private Image backgroundImage;
    private Image shadowImage;

    // 💡 [추가] 안내 이미지
    private Image info_click;

    // 🍓 애니메이션 관련 이미지
    private Image spearImage;
    private Image strawberryImage;

    // 🍓 이미지 경로 상수 (유지)
    private static final String BG_PATH = "../images/cakeStage_image/stage1/Background_stage1-1.png";
    private static final String SHADOW_PATH = "../images/cakeStage_image/surprise/shadow_surprise.png";
    private static final String SPEAR_01_PATH = "../images/cakeStage_image/surprise/Spear01_surprise.png";
    private static final String SPEAR_02_PATH = "../images/cakeStage_image/surprise/Spear02_surprise.png";
    private static final String STRAW_01_PATH = "../images/cakeStage_image/surprise/BigStrawberry01_surprise.png";
    private static final String STRAW_02_PATH = "../images/cakeStage_image/surprise/BigStrawberry02_surprise.png";

    // 💡 [추가] 안내 이미지 경로
    private static final String INFO_CLICK_PATH = "../images/cakeStage_image/cakeInfo_click.png";

    // 💡 [추가] 음악 파일 경로 상수
    private static final String SURPRISE_MUSIC_FILE = "../music/cakeSurprise.mp3";
    private static final String SUCCESS_SOUND_FILE = "../music/success_sound_surprise.mp3"; // 💡 [추가] 성공 효과음 파일 경로
    private static final String SPEAR_CLICK_SOUND = "../music/spear1.mp3"; // 💡 [추가] 창 클릭 효과음 파일 경로

    private Music surpriseMusic; // 💡 [추가] 서프라이즈 패널 전용 배경 음악 객체
    private Music successSound;  // 💡 [추가] 성공 효과음 객체 (단발성)

    // 🍓 애니메이션 및 상태 변수 (유지)
    private int strawberryY = -100; // 초기 Y 위치 (화면 밖)
    private int STRAWBERRY_TARGET_Y = 150;
    private final int STRAWBERRY_SPEED = 2;

    private boolean isSpearClicked = false;
    private boolean isStrawberryClicked = false;
    private final long CLICK_DISPLAY_DURATION = 200; // 0.2초 동안 이미지 변경 유지

    // 🍓 로드된 이미지 저장소 (유지)
    private Image spear01;
    private Image spear02;
    private Image straw01;
    private Image straw02;

    // 🍓 미션 관련 상수 및 변수 (유지)
    private final int REQUIRED_CLICKS = 20; // ‼️ 20회 클릭

    private int clickCount = 0;
    private boolean isMissionActive = true;

    private String missionResultText = null; // 미션 결과를 표시할 텍스트

    // 폰트 변수
    private Font customFont;
    private final int FONT_SIZE = 30; // ‼️ 폰트 크기를 상수로 유지


    public SurprisePanel(CakePanel panel) {
        this.cakePanel = panel;

        // 1. 이미지 로드 및 초기화
        loadImages();
        loadCustomFont(); // 폰트 로드

        // 초기 이미지 설정
        spearImage = spear01;
        strawberryImage = straw01;

        // 2. 패널 기본 설정
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        // 3. 마우스 이벤트 리스너 등록
        addMouseListener(new SurpriseMouseListener());
    }

    // ----------------------------------------------------
    // ‼️ [추가] 창 클릭 효과음 재생 로직
    // ----------------------------------------------------
    private void playSpearClickSound() {
        try {
            // 클릭 효과음은 단발성이므로 Music 객체를 새로 생성하고 재생합니다.
            // 이전 객체를 닫지 않으면 소리가 겹칠 수 있습니다.
            Music clickSound = new Music(SPEAR_CLICK_SOUND, false);
            clickSound.start();
            System.out.println("🔊 창 클릭 효과음 재생: " + SPEAR_CLICK_SOUND);

            // 짧은 효과음이므로 Music 클래스의 내부 구현에 따라 소리가 끝나면 리소스를 해제하도록 합니다.

        } catch (Exception e) {
            System.err.println("🔴 창 클릭 효과음 로드 또는 재생 실패.");
        }
    }

    // ----------------------------------------------------
    // ‼️ [수정] 외부 호출용 타이머 시작 로직 (음악 추가)
    // ----------------------------------------------------

    public void startMissionTimer() {
        if (!isMissionActive) return;

        // 💡 [추가] 배경 음악 재생 로직
        try {
            surpriseMusic = new Music(SURPRISE_MUSIC_FILE, false); // 루프 아님
            surpriseMusic.start();
            System.out.println("🎵 기습 스테이지 배경 음악 시작: " + SURPRISE_MUSIC_FILE);
        } catch (Exception e) {
            System.err.println("🔴 기습 스테이지 배경 음악 로드 실패.");
            surpriseMusic = null;
        }


        startTime = System.currentTimeMillis();

        // 100ms 마다 타이머 업데이트
        gameTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isMissionActive) {
                    gameTimer.stop();
                    return;
                }

                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= MISSION_DURATION_MS) {
                    gameTimer.stop();
                    handleMissionFailure(); // ‼️ 10초 초과 시 실패 처리
                } else {
                    repaint(); // 남은 시간 표시를 위해 호출
                }
            }
        });
        gameTimer.start();
        System.out.println("✅ 기습 스테이지 타이머가 시작되었습니다. (10초)");
    }

    // 💡 [추가] 배경 음악 중지 로직
    private void stopSurpriseMusic() {
        if (surpriseMusic != null) {
            surpriseMusic.close();
            surpriseMusic = null;
        }
    }

    // 💡 [추가] 성공 효과음 재생 로직 (한 번만 재생)
    private void playSuccessSound() {
        // 이미 재생 중이면 다시 재생하지 않도록 체크할 수 있지만, 여기서는 매번 새로 생성/재생합니다.
        if (successSound != null) {
            successSound.close(); // 혹시 이전 소리가 남아있다면 닫아줍니다.
        }

        try {
            successSound = new Music(SUCCESS_SOUND_FILE, false); // 루프 아님
            successSound.start();
            System.out.println("🔊 미션 성공 효과음 재생 (단발성): " + SUCCESS_SOUND_FILE);

            // 효과음 재생 후 Music 객체가 닫히는 것은 Music 클래스의 구현에 맡깁니다.
            // 명시적으로 닫는 타이머 로직은 사용자 요청에 따라 제거했습니다.

        } catch (Exception e) {
            System.err.println("🔴 성공 효과음 로드 또는 재생 실패.");
            successSound = null;
        }
    }


    // ‼️ [수정] 미션 실패 처리 (음악 중지 추가)
    private void handleMissionFailure() {
        if (!isMissionActive) return;

        isMissionActive = false;
        missionResultText = "GAME OVER"; // ‼️ 게임 오버 메시지 설정
        System.out.println("🚨 미션 실패! GAME OVER.");

        stopSurpriseMusic(); // 💡 [추가] 배경 음악 중지

        // ‼️ [핵심] 게임 오버 화면으로 전환 요청
        if (cakePanel != null) {
            cakePanel.switchToGameOverScreen();
            System.out.println("🚨 게임 오버 화면으로 전환 요청");
        }
        repaint();
    }

    // ----------------------------------------------------
    // 🖼️ 리소스 로드 로직 (유지)
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

        info_click = loadImage(INFO_CLICK_PATH); // 안내 이미지 로드

        spear01 = loadImage(SPEAR_01_PATH);
        spear02 = loadImage(SPEAR_02_PATH);
        straw01 = loadImage(STRAW_01_PATH);
        straw02 = loadImage(STRAW_02_PATH);

        if (backgroundImage == null || shadowImage == null || spear01 == null || straw01 == null) {
            System.err.println("🔴 SurprisePanel 이미지 로드 실패!");
        }
    }

    // ----------------------------------------------------
    // 폰트 로드 로직 (유지)
    // ----------------------------------------------------

    private void loadCustomFont() {
        // 1. 기본 폰트 설정 (대체 폰트)
        customFont = new Font("Arial", Font.BOLD, FONT_SIZE);

        // 2. 커스텀 폰트 로드 시도
        try {
            // ⚠️ 파일 경로를 프로젝트 구조에 맞게 수정하세요.
            File fontFile = new File("src/fonts/LAB디지털.ttf");

            // InputStream을 사용하여 로드
            try (InputStream is = new FileInputStream(fontFile)) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

                // 필요한 크기로 파생시켜 최종 폰트 객체에 저장
                customFont = baseFont.deriveFont(Font.BOLD, (float)FONT_SIZE);
                System.out.println("✅ 커스텀 폰트 로드 성공.");
            }

        } catch (IOException | FontFormatException e) {
            // 로드 실패 시
            System.err.println("❌ 폰트 로드 실패. Arial 기본 폰트를 사용합니다.");
        }
    }

    // ----------------------------------------------------
    // 🐭 내부 마우스 리스너 (클릭 처리) - (수정: 음악 중지 및 성공 효과음 재생 추가)
    // ----------------------------------------------------
    private class SurpriseMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {

            if (!isMissionActive) {
                return;
            }

            // ... (클릭 영역 및 이미지 변경 로직 생략) ...
            Point clickPoint = e.getPoint();
            int spearW = spearImage.getWidth(SurprisePanel.this);
            int spearH = spearImage.getHeight(SurprisePanel.this);
            int spearX = (getWidth() - spearW) / 2;
            int spearY = getHeight() / 2 - 50;
            Rectangle spearBounds = new Rectangle(spearX, spearY, spearW, spearH);
            int strawW = strawberryImage.getWidth(SurprisePanel.this);
            int strawH = strawberryImage.getHeight(SurprisePanel.this);
            Rectangle strawBounds = new Rectangle(getWidth()/2 - strawW/2, strawberryY, strawW, strawH);
            boolean imageChanged = false;

            if (spearBounds.contains(clickPoint) && !isSpearClicked) {
                spearImage = spear02;
                isSpearClicked = true;
                imageChanged = true;

                // 💡 [핵심 추가] 창(Spear) 클릭 시 효과음 재생
                playSpearClickSound();
            }

            if (strawBounds.contains(clickPoint) && !isStrawberryClicked) {
                clickCount++;
                System.out.println("Click! Count: " + clickCount);

                strawberryImage = straw02;
                isStrawberryClicked = true;
                imageChanged = true;

                if (clickCount >= REQUIRED_CLICKS) {
                    if (!isMissionActive) return;

                    isMissionActive = false;
                    if (gameTimer != null) gameTimer.stop();

                    stopSurpriseMusic(); // 💡 [추가] 배경 음악 중지
                    playSuccessSound(); // 💡 [추가] 성공 효과음 재생

                    missionResultText = "Success!";
                    repaint();

                    new javax.swing.Timer((int)SUCCESS_DISPLAY_DURATION, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            missionResultText = null;
                            repaint();
                            ((javax.swing.Timer)evt.getSource()).stop();

                            if (cakePanel != null) {
                                cakePanel.switchNextStageOnSuccess();
                                System.out.println("✅ 미션 성공! CakePanel에 다음 스테이지 전환 요청.");
                            }
                        }
                    }).start();
                }
            }

            if (imageChanged) {
                repaint();

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
        }
    }


    // ----------------------------------------------------
    // 🎨 그리기 로직 (유지)
    // ----------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. 배경/그림자/딸기/창 이미지 그리기 (순서 유지)
        if (backgroundImage != null) { g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this); }
        if (shadowImage != null)     { g2.drawImage(shadowImage, 0, 0, getWidth(), getHeight(), this); }
        if (strawberryImage != null) { g2.drawImage(strawberryImage, 0, 0, getWidth(), getHeight(), this); }
        if (spearImage != null)      { g2.drawImage(spearImage, 0, 0, getWidth(), getHeight(), this); }

        // ----------------------------------------------------
        // 2. 💡 오른쪽 상단 타이틀 및 안내 이미지
        // ----------------------------------------------------
        if (isMissionActive) {

            // 타이틀 폰트 설정 (40f)
            g2.setFont(customFont.deriveFont(Font.BOLD, 40f));
            g2.setColor(Color.WHITE);

            String titleText1 = "대왕딸기 습격!!!!";
            String titleText2 = "10초 안에 대왕 딸기를 부수세요!";

            int margin = 20;
            int textY1 = margin + g2.getFontMetrics().getHeight();
            int textY2 = textY1 + g2.getFontMetrics().getHeight() - 10; // 줄 간격 조정

            // 텍스트 오른쪽 정렬을 위한 X 좌표 계산
            int textX1 = getWidth() - margin - g2.getFontMetrics().stringWidth(titleText1);
            int textX2 = getWidth() - margin - g2.getFontMetrics().stringWidth(titleText2);

            g2.drawString(titleText1, textX1, textY1);
            g2.drawString(titleText2, textX2, textY2);

            // 안내 이미지 표시 (텍스트 아래)
            if (info_click != null) {
                int infoW = info_click.getWidth(this);
                int infoH = info_click.getHeight(this);
                int infoX = getWidth() - margin - infoW;
                int infoY = textY2 + margin;

                g2.drawImage(info_click, infoX, infoY, infoW, infoH, this);
            }
        }

        // ----------------------------------------------------
        // 3. 미션 카운트 및 결과 표시
        // ----------------------------------------------------

        // 미션 정보 표시 폰트 설정 (기본 FONT_SIZE 30)
        g2.setFont(customFont.deriveFont(Font.BOLD, (float)FONT_SIZE));

        if (isMissionActive) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long remainingTime = MISSION_DURATION_MS - elapsedTime;
            String timeStatus = remainingTime > 0 ? String.format("%.1f", remainingTime / 1000.0) : "0.0";

            // 미션 카운트 표시
            String status = String.format("Click : %d / %d  |  Time : %s초", clickCount, REQUIRED_CLICKS, timeStatus);
            g2.setColor(Color.YELLOW);

            int textX = (getWidth() - g2.getFontMetrics().stringWidth(status)) / 2;
            int textY = getHeight() - 50;
            g2.drawString(status, textX, textY);

        } else if (missionResultText != null) {
            // 미션 종료 후 결과 표시
            g2.setFont(customFont.deriveFont(Font.BOLD, 50f)); // 결과 메시지는 더 크게

            if (missionResultText.equals("Success!")) {
                g2.setColor(Color.GREEN);
            } else if (missionResultText.equals("GAME OVER")) {
                g2.setColor(Color.RED);
            }

            int textX = (getWidth() - g2.getFontMetrics().stringWidth(missionResultText)) / 2;
            int textY = getHeight() / 2 + 100; // 중앙보다 조금 아래
            g2.drawString(missionResultText, textX, textY);
        }
    }
}