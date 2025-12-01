package game.Space;

import game.GameFrame;
import game.Main;
import game.Music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.IOException;

public class SpaceIntroPanel extends JPanel {
    private GameFrame gameFrame;
    private Image background;
    private Image info;
    private Image backDeco;   // 떠다니는 행성 데코
    private Image ufo;
    private Image alien1;
    private Image alien2;
    private boolean useAlien2 = false;   // 대사 바뀔 때마다 토글

    private final Rectangle START_BUTTON_BOUNDS = new Rectangle(950, 10, 300, 100);

    // ✅ 대사 배열 (너가 준 텍스트 기반)
    private String[] dialogues = {
            """
후루룩 깐따삐야 (수석 연구원):
삐리삐리... 생체 신호 분석. 털 색이 하얀 걸 보니 
'우주적 공복' 상태로군, 지구 고양이!
나는 라면 행성 라메니아의 지성, [우주 라면 실험실]의 소장 
'후루룩 깐따삐야'다!
""",
            """
후루룩 깐따삐야 (수석 연구원):
평범한 요리는 거부한다. 내 위대한 '맛의 융합 실험'에 
참여하게 된 걸 영광으로 알아라!
자, 네 혀를 자극할 완벽한 레시피 데이터를 주입해주마!
""",

            // 1단계
            """
후루룩 깐따삐야 (수석 연구원):
1단계, 베이스 용액 추출! 맹물은 실험 실패의 지름길이다. 
엔진 코어의 열기를 이용해라!
게이지가 정확한 박자에 도달했을 때 [SPACE]를 눌러 고열수를 투하해라!
오차는 용납하지 않는다, 삐리!
""",

            //기습 요소 언급
            """
후루룩 깐따삐야  (수석 연구원):
(지직... 지직...) 삐리...?! 돌발 변수 발생!
강력한 '우주 태양풍'으로 통신 링크 불안정... 
잠시 시공간이 왜곡되어... 노래가... 느...려...질... 거...다.
당황하지 마라! 늘어진 템포조차 완벽하게 계산해서 반응해 
삐리릿!!

""",

            // 2단계
            """
후루룩 깐따삐야 (수석 연구원):
2단계, 고밀도 탄수화물 확보! 저기 '블랙홀'의 사건 지평선에서 
쫄깃한 면발 데이터를 수집해야 한다.
공기포 가동! 면발이 날아오는 리듬을 분석하고 키를 눌러라!
왼쪽은 [A], 오른쪽은 [D], 양방향 간섭은 [W]다! 
쫄깃한 텐션을 유지해!
""",

            // 3단계
            """
후루룩 깐따삐야  (수석 연구원):
3단계, 맛의 화룡점정! 우주를 떠도는 문명의 잔해를 
'풍미 촉매제'로 변환한다!
버섯, 대파, 계란... 반응 물질이 보이면 박자에 맞춰 [마우스 클릭]으로 레이저를 쏴서 포획해라!
이것이 바로 연금술이자 과학이다!
""",

            //기습 요소 언급
            """
후루룩 깐따삐야  (수석 연구원):
경고! 전방에 고점도 우주 쓰레기 포착! 
놈들이 시야를 가리는 즉시 [SPACE]를 10번 연타해서 
물리적으로 떼어내라! 
단 한순간도 긴장을 늦추지 마라, 삐리!

""",


            // 마무리
            """
후루룩 깐따삐야  (수석 연구원):
백문이 불여일식! 이론 수업은 끝났다. 
내 완벽한 시범 시뮬레이션을 가동할 테니,데이터를 눈에 
새기고 그대로 복제해라!
자, 진정한 우주의 맛을 증명해 봐라, 실험체 수냥이! 
위대한 실험 개시, 깐따삐야!!
"""
    };

    private int currentDialogueIndex = 0;
    private Font dialogue;

    // ✅ 음악 관련
    private Music bgmMusic;       // aline_intro.mp3 (loop)
    private Music alienVoice;     // aline_sound.mp3 (각 대사마다)

    // ✅ 타자 효과 관련
    private Timer typeTimer;      // 글자 한 자씩 출력
    private Timer autoNextTimer;  // 한 대사 끝난 뒤 자동 다음 대사
    private int visibleCharCount = 0;

    private static final int TYPE_DELAY_MS = 35;         // 글자 하나 나오는 속도
    private static final int AUTO_NEXT_DELAY_MS = 1500;  // 한 대사 끝난 뒤 다음으로 넘어가기 전 대기

    // ✅ 색상
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 128);
    private static final Color SPEAKER_COLOR = new Color(190, 190, 190); // 살짝 회색

    // ✅ 배경 애니메이션용
    private Timer animTimer;
    private double backDecoPhase = 0;
    private static final int BACK_DECO_AMPLITUDE = 15;
    private int backDecoBaseX = 0;
    private int backDecoBaseY = 40;

    private int ufoX;
    private int ufoY = 10;
    private int ufoDir = 1;
    private static final int UFO_SPEED = 2;
    private int ufoMinX = 100;
    private int ufoMaxX = 300;

    public SpaceIntroPanel(GameFrame frame) {
        this.gameFrame = frame;
        setLayout(null);
        frame.setLayout(new BorderLayout());

        background = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_background_decoX.png")).getImage();
        info = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_info.png")).getImage();
        backDeco = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_back_deco.png")).getImage();
        ufo = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_ufo.png")).getImage();
        alien1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_alien01.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_alien02.png")).getImage();

        ImageIcon skip_off = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/SkipButton_unselected.png"));
        ImageIcon skip_on = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/SkipButton_selected.png"));

        JButton startButton = createStageButton(skip_off, skip_on);
        startButton.setBounds(START_BUTTON_BOUNDS);
        add(startButton);

        // 스킵 → 바로 게임 시작
        startButton.addActionListener(e -> goToSpaceGame());

        // --- 폰트 로딩 ---
        try {
            InputStream is = Main.class.getResourceAsStream("/fonts/LAB디지털.ttf");
            if (is == null) {
                throw new IOException("Font resource not found: /fonts/LAB디지털.ttf");
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            dialogue = baseFont.deriveFont(Font.BOLD, 22f);
            is.close();
        } catch (FontFormatException | IOException e) {
            System.err.println("폰트 로딩 실패. 기본 폰트 사용: " + e.getMessage());
            dialogue = new Font("Dialog", Font.BOLD, 22);
        }

        // ✅ 배경 BGM 시작 (loop)
        try {
            bgmMusic = new Music("aline_intro.mp3", true);
            bgmMusic.start();
        } catch (Exception ex) {
            System.err.println("인트로 BGM 시작 실패: " + ex.getMessage());
        }

        // ✅ UFO 초기 위치
        ufoX = (ufoMinX + ufoMaxX) / 2;

        // ✅ 배경 애니메이션 타이머 (행성 떠다니기 + UFO 좌우 이동)
        animTimer = new Timer(30, e -> {
            backDecoPhase += 0.05;
            ufoX += ufoDir * UFO_SPEED;
            if (ufoX < ufoMinX || ufoX > ufoMaxX) {
                ufoDir *= -1;
            }
            repaint();
        });
        animTimer.start();

        // ✅ 첫 대사부터 타자 효과 시작
        startDialogue(0);

        // --- 키 리스너 (SPACE: 스킵/다음) ---
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    handleSpacePress();
                }
            }
        });

        SwingUtilities.invokeLater(() -> {
            if (!SpaceIntroPanel.this.requestFocusInWindow()) {
                System.out.println("포커스 요청 실패 또는 지연됨");
            } else {
                System.out.println("포커스 성공적으로 획득");
            }
        });
    }

    // ✅ SPACE 눌렀을 때 동작
    private void handleSpacePress() {
        String full = dialogues[currentDialogueIndex];
        boolean isTyping = typeTimer != null && typeTimer.isRunning();

        if (isTyping) {
            // 타자 중이면 → 바로 전체 대사 표시
            typeTimer.stop();
            visibleCharCount = full.length();
            repaint();

            // 자동 다음 대사 타이머 시작
            startAutoNextTimer();
        } else {
            // 이미 다 나온 상태 → 다음 대사 또는 게임 시작
            goNextDialogueOrGame();
        }
    }

    // ✅ 특정 인덱스의 대사를 타자 효과와 함께 시작
    private void startDialogue(int index) {
        // 타이머 정리
        if (typeTimer != null) typeTimer.stop();
        if (autoNextTimer != null) autoNextTimer.stop();

        currentDialogueIndex = index;
        visibleCharCount = 0;

        // 외계인 이미지 토글 (대사 바뀔 때마다 깜빡이는 느낌)
        useAlien2 = !useAlien2;

        // 효과음 재생
        playAlienVoice();

        // 타자 타이머 설정
        String full = dialogues[currentDialogueIndex];
        typeTimer = new Timer(TYPE_DELAY_MS, e -> {
            if (visibleCharCount < full.length()) {
                visibleCharCount++;
                repaint();
            } else {
                typeTimer.stop();
                // 전체 다 나오면 자동으로 다음 대사로 넘어가기 위한 타이머
                startAutoNextTimer();
            }
        });
        typeTimer.start();
    }

    // ✅ 한 대사가 끝난 뒤 일정 시간 후에 다음 대사로
    private void startAutoNextTimer() {
        if (autoNextTimer != null) autoNextTimer.stop();

        autoNextTimer = new Timer(AUTO_NEXT_DELAY_MS, e -> {
            autoNextTimer.stop();
            goNextDialogueOrGame();
        });
        autoNextTimer.setRepeats(false);
        autoNextTimer.start();
    }

    // ✅ 다음 대사로 가거나, 마지막이면 게임 시작
    private void goNextDialogueOrGame() {
        if (currentDialogueIndex < dialogues.length - 1) {
            startDialogue(currentDialogueIndex + 1);
        } else {
            goToSpaceGame();
        }
    }

    // ✅ 인트로에서 게임 화면으로 넘어갈 때 음악/타이머 정리
    private void goToSpaceGame() {
        if (typeTimer != null) typeTimer.stop();
        if (autoNextTimer != null) autoNextTimer.stop();
        if (alienVoice != null) {
            alienVoice.close();
            alienVoice = null;
        }
        if (bgmMusic != null) {
            bgmMusic.close();
            bgmMusic = null;
        }
        gameFrame.showSpaceScreen();
    }

    // ✅ 대사 한 줄 재생할 때마다 효과음 재생
    private void playAlienVoice() {
        try {
            if (alienVoice != null) {
                alienVoice.close();
                alienVoice = null;
            }
            alienVoice = new Music("aline_sound.mp3", false);
            alienVoice.start();
        } catch (Exception ex) {
            System.err.println("에일리언 보이스 재생 실패: " + ex.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. 배경
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);

        // 2. 행성 데코 (위아래로 떠다니기)
        if (backDeco != null) {
            int decoY = backDecoBaseY + (int)(Math.sin(backDecoPhase) * BACK_DECO_AMPLITUDE);
            g.drawImage(backDeco, backDecoBaseX, decoY,
                    backDeco.getWidth(null), backDeco.getHeight(null), null);
        }

        // 3. UFO (좌우로 이동)
        if (ufo != null) {
            g.drawImage(ufo, ufoX, ufoY,
                    ufo.getWidth(null), ufo.getHeight(null), null);
        }

        // 4. 정보 오버레이
        g.drawImage(info, 0, 0, getWidth(), getHeight(), null);

        // 5. 외계인(깜빡이기)
        Image currentAlien = useAlien2 ? alien2 : alien1;
        g.drawImage(currentAlien, 0, 0, getWidth(), getHeight(), null);

        // 6. 대사 출력
        if (currentDialogueIndex < dialogues.length) {
            String full = dialogues[currentDialogueIndex];
            int len = Math.min(visibleCharCount, full.length());
            String visible = full.substring(0, len);

            g.setFont(dialogue);
            FontMetrics fm = g.getFontMetrics();

            int LEFT_MARGIN = 560;           // 오른쪽으로 조금
            int RIGHT_MARGIN = 50;
            int startX = LEFT_MARGIN;
            int maxWidth = getWidth() - LEFT_MARGIN - RIGHT_MARGIN;
            int initialY = getHeight() - 240; // 아래로 조금
            int lineHeight = fm.getHeight() + 5;

            drawWrappedStringWithHighlight(g, visible, startX, initialY, maxWidth, lineHeight);
        }
    }

    // 헬퍼 메서드: 버튼 생성
    private JButton createStageButton(ImageIcon defaultIcon, ImageIcon rolloverIcon) {
        JButton button = new JButton();
        button.setIcon(defaultIcon);
        button.setRolloverIcon(rolloverIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFocusable(false); // 버튼이 키포커스 안 가져가게
        return button;
    }

    /**
     * 줄바꿈 + 키 토큰 하이라이트 + 화자 회색 처리
     */
    private void drawWrappedStringWithHighlight(Graphics g, String text,
                                                int x, int y, int maxWidth, int lineHeight) {
        int maxX = x + maxWidth;

        String[] lines = text.split("\n");
        int currentY = y;

        for (String lineTextRaw : lines) {
            String lineText = lineTextRaw.trim();
            if (lineText.isEmpty()) {
                currentY += lineHeight;
                continue;
            }

            // 앞에 "후루룩 깐따삐야" 로 시작하면 화자 줄로 인식 (공백 개수 차이 방지)
            boolean isSpeakerLine = lineText.startsWith("후루룩 깐따삐야");

            String[] tokens = lineText.split(" ");
            int currentX = x;

            for (String rawToken : tokens) {
                if (rawToken.isEmpty()) continue;

                // ✅ 폰트 먼저 설정
                if (isSpeakerLine) {
                    g.setFont(dialogue.deriveFont(Font.BOLD, 25f));
                } else {
                    g.setFont(dialogue);
                }

                // ✅ 폰트 설정 후에 매번 FontMetrics 다시 가져오기
                FontMetrics fm = g.getFontMetrics();

                String tokenWithSpace = rawToken + " ";
                int tokenWidth = fm.stringWidth(tokenWithSpace);

                // 줄바꿈 체크
                if (currentX + tokenWidth > maxX) {
                    currentY += lineHeight;
                    currentX = x;
                }

                // ✅ 색상: 키 하이라이트 > 화자 회색 > 기본 흰색
                if (isHighlightToken(rawToken)) {
                    g.setColor(HIGHLIGHT_COLOR);
                } else if (isSpeakerLine) {
                    g.setColor(SPEAKER_COLOR);
                } else {
                    g.setColor(TEXT_COLOR);
                }

                // ✅ 실제로는 공백까지 포함해서 그려줌
                g.drawString(tokenWithSpace, currentX, currentY);
                currentX += tokenWidth;
            }

            currentY += lineHeight;
        }
    }


    // ✅ 어떤 토큰을 하이라이트 할지
    private boolean isHighlightToken(String token) {
        return token.contains("[SPACE]")
                || token.contains("[A]")
                || token.contains("[D]")
                || token.contains("[W]")
                || token.contains("[마우스") || token.contains("클릭]")
                || token.contains("10");

    }
}