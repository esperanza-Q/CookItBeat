package game.Cake;

import game.GameFrame;
import game.Main;
import game.Music;

import javax.swing.*;
        import java.awt.*;
        import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.IOException;

public class CakeIntro extends JPanel {
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
겁쟁이 병정 (스페이드 A):
"거..거기 쫑긋한 귀... 서, 설마 네가 소문의 수냥이야?
살았다... 지금 여왕님의 '악몽 티타임 파티'까지 딱 5분 남았는데...
주방장이 무섭다고 도망가 버렸어!!
""",
            """
겁쟁이 병정 (스페이드 A):
이대로면 우린 전원 참수... 아니, 케이크 대신 목이 날아갈 거야!
네 솜씨가 좋다고 들었어. 제발 우리 좀 살려줘!
지금부터 케이크 만들기에 대해 아..알려줄께!
""",

            // 1단계
            """
겁쟁이 병정 (스페이드 A):
1단계! 딸기! 딸기가 필요해!
예쁘게 잘라줘야 해! 여왕님은 못생긴 딸기를 제일 싫어하신단 말이야!
딸기 그림자를 잘 듣고 박자에 맞춰 [마우스 클릭]을 눌러서 잘라줘!
촵! 촵! 제발 오차 없이 부탁해... 내 목숨이 달렸어!
""",

            // 2단계
            """
겁쟁이 병정 (스페이드 A):
2단계, 계란이야! 껍질 들어가면 우린 바로 지하 감옥행이야!
게이지가 노란색 칸에 왔을 때! [SPACE] 키로 제대로 넣어줘.
손 떨지 마... (덜덜) 타이밍 잘 맞춰줘!
""",

            // 3단계
            """
겁쟁이 병정 (스페이드 A):
3단계, 반죽 섞기! 시간이 없어!
폭풍처럼! 구름처럼 부풀 때까지 내가 하는 걸 잘 보고 [WASD]로 마구 돌려!
더 빨리! 더! 더! 여왕님 발소리가 들리는 것 같아!
""",

            // 4단계
            """
겁쟁이 병정 (스페이드 A):
4단계, 집중해줘! 타면 끝장이야... 냄새나면 여왕님이 바로 알아챈다고!
오븐 바늘이 빨간 눈금에 닿는 순간! [ENTER]로 타이밍을 멈춰줘!!
숨 참아...
""",
            // 5단계
            """
겁쟁이 병정 (스페이드 A):
5단계, 빵이 구워졌어! 이제 크림 발라야 해!
울퉁불퉁하면 안 돼... 빙판길처럼 매끄럽게 발라야 산다고!
크림이 빈 곳이 없게 [ASDF]로 펴 발라줘! 빨리빨리!
""",


            // 6단계
            """
겁쟁이 병정 (스페이드 A):
6단계, 이제 꾸밀 시간이야! 여왕님 마음에 쏙 들어야 해!
일정한 간격! 일정한 높이! 리듬에 맞춰서 [클릭], [클릭], [클릭]!
각 잡고 짜줘! 삐뚤어지면 우린 유죄야...
""",
            // 7단계
            """
겁쟁이 병정 (스페이드 A):
마지막이야, 수냥이... (긴장)
 이 케이크의 심장! 가장 예쁜 딸기를 올릴 차례야.
 정중앙 확인하고... 손 떨지 말고 조심스럽게... [클릭]으로 투하!!
""",

            // 마무리
            """
겁쟁이 병정 (스페이드 A):
이제 실전이야. 저기 복도에서 또각또각 발소리가 들려...
실패하면 우린 끝장이야!
자, 너만 믿을게. 제발 우리 목숨을 구해줘!! 작전 개시!!
"""


    };

    private int currentDialogueIndex = 0;

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
    private static final Color TEXT_COLOR = Color.BLACK;                    // 기본 글자색: 검정
    private static final Color HIGHLIGHT_COLOR = new Color(255, 105, 180);  // 하이라이트: 핑크
    private static final Color SPEAKER_COLOR = new Color(150, 150, 150);    // 화자: 회색

    // info 이미지 배율 (예: 1.0f = 원본, 1.5f = 150%, 0.8f = 80%)
    private float INFO_SCALE = 1.5f;


    // ====== 폰트 크기 설정 ======
    private static final float DIALOGUE_FONT_SIZE = 18f;   // 기본 대사 폰트 크기
    private static final float SPEAKER_FONT_SIZE  = 20f;   // 화자 줄 폰트 크기

    // ====== 폰트 ======
    private Font dialogue;
    private Font dialogueSpeaker;

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

    public CakeIntro(GameFrame frame) {
        this.gameFrame = frame;
        setLayout(null);

        background = new ImageIcon(Main.class.getResource("../images/cakeStage_image/intro/background_cake_info.png")).getImage();
        info = new ImageIcon(Main.class.getResource("../images/cakeStage_image/intro/Info2.png")).getImage();
        backDeco = new ImageIcon(Main.class.getResource("../images/cakeStage_image/intro/tea.png")).getImage();
        ufo = new ImageIcon(Main.class.getResource("../images/cakeStage_image/intro/card.png")).getImage();
        alien1 = new ImageIcon(Main.class.getResource("../images/cakeStage_image/intro/soldier.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/cakeStage_image/intro/soldier2.png")).getImage();

        ImageIcon skip_off = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/SkipButton_unselected.png"));
        ImageIcon skip_on = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/SkipButton_selected.png"));

        JButton startButton = createStageButton(skip_off, skip_on);
        startButton.setBounds(START_BUTTON_BOUNDS);
        add(startButton);

        // 스킵 → 바로 게임 시작
        startButton.addActionListener(e -> goToCakeGame());

        // --- 폰트 로딩 ---
        try {
            InputStream is = Main.class.getResourceAsStream("/fonts/ThinDungGeunMo.ttf");
            if (is == null) {
                throw new IOException("Font resource not found: /fonts/ThinDungGeunMo.ttf");
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

            // ✅ 기본 대사 / 화자 줄 폰트 분리
            dialogue = baseFont.deriveFont(Font.PLAIN, DIALOGUE_FONT_SIZE);
            dialogueSpeaker = baseFont.deriveFont(Font.BOLD, SPEAKER_FONT_SIZE);

            is.close();
        } catch (FontFormatException | IOException e) {
            System.err.println("폰트 로딩 실패. 기본 폰트 사용: " + e.getMessage());
            dialogue = new Font("Dialog", Font.PLAIN, (int) DIALOGUE_FONT_SIZE);
            dialogueSpeaker = new Font("Dialog", Font.BOLD, (int) SPEAKER_FONT_SIZE);
        }


        // ✅ 배경 BGM 시작 (loop)
        try {
            bgmMusic = new Music("cake_intro.mp3", true);
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
            if (!game.Cake.CakeIntro.this.requestFocusInWindow()) {
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
            goToCakeGame();
        }
    }

    // ✅ 인트로에서 게임 화면으로 넘어갈 때 음악/타이머 정리
    private void goToCakeGame() {
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

        // ✅ 인트로 끝나면 케이크 1-1으로 이동
        gameFrame.showCakeScreen();
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

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

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
            FontMetrics fm = g.getFontMetrics(dialogue);
            int lineHeight = fm.getHeight() + 5;

            int LEFT_MARGIN = 560;           // 오른쪽으로 조금
            int RIGHT_MARGIN = 50;
            int startX = LEFT_MARGIN;
            int maxWidth = getWidth() - LEFT_MARGIN - RIGHT_MARGIN;
            int initialY = getHeight() - 240; // 아래로 조금


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
                                                int x, int y, int maxWidth, int baseLineHeight) {

        String[] lines = text.split("\n");
        int currentY = y;

        for (String lineTextRaw : lines) {
            String lineText = lineTextRaw.trim();
            if (lineText.isEmpty()) {
                currentY += baseLineHeight;
                continue;
            }

            // 🔁 화자 줄 체크
            boolean isSpeakerLine = lineText.startsWith("겁쟁이 병정 (스페이드 A):");

            // 이 줄에서 쓸 폰트/메트릭 결정 (줄 단위로 고정)
            Font lineFont = isSpeakerLine ? dialogueSpeaker : dialogue;
            g.setFont(lineFont);
            FontMetrics fm = g.getFontMetrics(lineFont);
            int lineHeight = fm.getHeight() + 5;

            String[] tokens = lineText.split(" ");
            int currentX = x;
            int maxX = x + maxWidth;

            for (String rawToken : tokens) {
                if (rawToken.isEmpty()) continue;

                String tokenWithSpace = rawToken + " ";
                int tokenWidth = fm.stringWidth(tokenWithSpace);

                if (currentX + tokenWidth > maxX) {
                    currentY += lineHeight;
                    currentX = x;
                }

                // 색상 우선순위: 키 하이라이트 > 화자 회색 > 기본 검정
                if (isHighlightToken(rawToken)) {
                    g.setColor(HIGHLIGHT_COLOR);
                } else if (isSpeakerLine) {
                    g.setColor(SPEAKER_COLOR);
                } else {
                    g.setColor(TEXT_COLOR);
                }

                g.drawString(rawToken, currentX, currentY);
                currentX += tokenWidth;
            }

            currentY += lineHeight;
        }
    }



    // ✅ 어떤 토큰을 하이라이트 할지 (대사에 맞춰 수정)
    private boolean isHighlightToken(String token) {
        String upper = token.toUpperCase();

        // 영문 키워드
        if (upper.contains("[SPACE]")) return true;
        if (upper.contains("[ENTER]")) return true;
        if (upper.contains("[WASD]")) return true;
        if (upper.contains("[ASDF]")) return true;

        // 혹시 개별 키로 쪼개질 경우 대비 (ASDF, WASD)
        if (upper.contains("[A]") || upper.contains("[S]") ||
                upper.contains("[D]") || upper.contains("[F]") ||
                upper.contains("[W]")) {
            return true;
        }

        // 마우스 클릭 관련 한글 토큰
        if (token.contains("[마우스") || token.contains("클릭]")
            || token.contains("[클릭]"))
        {
            return true;
        }

        return false;
    }

}