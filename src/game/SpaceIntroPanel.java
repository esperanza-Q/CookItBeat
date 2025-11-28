package game;

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
    private Image ufo;
    private Image alien;
    private final Rectangle START_BUTTON_BOUNDS = new Rectangle(950, 10, 300, 100);
    private String[] dialogues = { // 💡 대사 목록 배열
            """
삐리삐리... 생체 신호 분석. 털 색이 하얀 걸 보니 '우주적 공복' 상태로군, 지구 고양이!
나는 라면 행성 라메니아의 지성, [우주 라면 실험실]의 소장 '후루룩 깐따삐야'다!
평범한 요리는 거부한다. 내 위대한 '맛의 융합 실험'에 참여하게 된 걸 영광으로 알아라!
자, 네 혀를 자극할 완벽한 레시피 데이터를 주입해주마!""",
            """
1단계, 베이스 용액 추출! 맹물은 실험 실패의 지름길이다. 엔진 코어의 열기를 이용해라!
게이지가 정확한 박자에 도달했을 때 **[SPACE]**를 눌러 100도 고열수를 투하해라!
오차는 용납하지 않는다, 삐리!""",
            """
2단계, 고밀도 탄수화물 확보! 저기 '블랙홀'의 사건 지평선에서 쫄깃한 면발 데이터를 수집해야 한다.
공기포 가동! 면발이 날아오는 리듬을 분석하고 키를 눌러라!
왼쪽은 [A], 오른쪽은 [D], 양방향 간섭은 [W]다! 쫄깃한 텐션을 유지해!""",
            """
3단계, 맛의 화룡점정! 우주를 떠도는 문명의 잔해를 '풍미 촉매제'로 변환한다!
버섯, 대파, 계란... 반응 물질이 보이면 박자에 맞춰 **[마우스 클릭]**으로 레이저를 쏴서 포획해라!
이것이 바로 연금술이자 과학이다!""",
            "백문이 불여일식(食)! 이론 수업은 끝났다. 내 완벽한 시범 시뮬레이션을 가동할 테니, 데이터를 눈에 새기고 그대로 복제해라!\n"+
            "자, 진정한 우주의 맛을 증명해 봐라, 실험체 수냥이! 위대한 실험 개시, 깐따삐야!!"
    };
    private int currentDialogueIndex = 0; // 💡 현재 대사 인덱스
    private Font dialogue; // 폰트 로딩을 생성자에서 수행

    public SpaceIntroPanel(GameFrame frame) {
    this.gameFrame = frame;
    setLayout(null);
    frame.setLayout(new BorderLayout());

    background = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_background_decoO.png")).getImage();
    info = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_info.png")).getImage();
    ufo = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_ufo.png")).getImage();
    alien = new ImageIcon(Main.class.getResource("../images/alienStage_image/intro/Intro_alien01.png")).getImage();
    ImageIcon skip_off = new ImageIcon(getClass().getResource("../images/mainUI/Buttons/SkipButton_unselected.png"));
    ImageIcon skip_on = new ImageIcon(getClass().getResource("../images/mainUI/Buttons/SkipButton_selected.png"));

    JButton startButton = createStageButton(skip_off, skip_on);
    startButton.setBounds(START_BUTTON_BOUNDS); // 💡 오른쪽 상단 위치와 크기 설정
    add(startButton);
// 💡 버튼 클릭 이벤트 리스너 추가: 바로 Space 시작
    startButton.addActionListener(e -> { gameFrame.showSpaceScreen(); });

        // --- 폰트 로딩 (생성자에서 1회만 실행) ---
        try {
            // ClassLoader를 사용하여 리소스 경로로 폰트를 로드하는 것이 안정적입니다.
            // 경로는 `Main.class`나 `SpaceIntroPanel.class` 기준으로 설정해야 합니다.
            // 예시: 폰트 파일이 `resources/fonts/LAB디지털.ttf`에 있다고 가정
            // 정확한 경로는 프로젝트 구조에 따라 달라질 수 있으므로, 예시에서는 `Main.class` 기준으로 수정합니다.

            // 💡 파일 경로 수정: 리소스로부터 InputStream을 얻습니다.
            // 이 경로는 실제 폰트 파일의 위치에 맞게 수정해야 합니다.
            InputStream is = Main.class.getResourceAsStream("/fonts/LAB디지털.ttf");

            if (is == null) {
                // 리소스 로딩 실패 시 예외 발생 또는 기본 폰트 설정
                throw new IOException("Font resource not found: /fonts/LAB디지털.ttf");
            }

            // 1. InputStream으로부터 폰트 객체 생성
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

            // 2. 폰트 크기 설정
            dialogue = baseFont.deriveFont(Font.BOLD, 25f);

            is.close(); // InputStream 닫기

        } catch (FontFormatException | IOException e) { // 💡 catch 블록 문법 수정
            System.err.println("폰트 로딩 실패. 기본 폰트 사용: " + e.getMessage());
            dialogue  = new Font("Dialog", Font.BOLD, 25); // 기본 폰트 사용
        }

        // --- 키 리스너 설정 ---
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) { // 스페이스 바를 눌렀을 때
                    if (currentDialogueIndex < dialogues.length - 1) {
                        // 다음 대사가 남아있으면 인덱스 증가
                        currentDialogueIndex++;
                        repaint(); // 💡 paintComponent를 다시 호출하여 화면 갱신
                    } else {
                        // 모든 대사가 끝났으면 게임 화면으로 전환
                        gameFrame.showSpaceScreen();
                    }
                }
            }
        });
        // 💡 늦은 포커스 요청 (핵심 수정)
        // 패널이 프레임에 추가된 후 포커스를 확실히 얻도록 보장
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // requestFocusInWindow() 또는 requestFocus()를 사용
                if (!SpaceIntroPanel.this.requestFocusInWindow()) {
                    System.out.println("포커스 요청 실패 또는 지연됨");
                } else {
                    System.out.println("포커스 성공적으로 획득");
                }
            }
        });

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 1. 배경 및 이미지 그리기
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        g.drawImage(ufo, 0, 0, getWidth(), getHeight(), null);
        g.drawImage(info, 0, 0, getWidth(), getHeight(), null);
        g.drawImage(alien, 0, 0, getWidth(), getHeight(), null);

        // 2. 현재 대사 출력
        if (currentDialogueIndex < dialogues.length) {
            String currentDialogue = dialogues[currentDialogueIndex];

            // 3. 폰트 설정 (이미 생성자에서 로딩했으므로 여기서 설정만 합니다)
            g.setFont(dialogue); // 💡 이미 로드된 dialogue 폰트 사용

            // 4. 줄 바꿈 및 그리기 로직
            FontMetrics fm = g.getFontMetrics();

            // 🚀 마진 값 설정
            int LEFT_MARGIN = 500; // 왼쪽 마진 (시작 X 좌표)
            int RIGHT_MARGIN = 50; // 오른쪽 마진 (화면 끝으로부터 거리)

            // 🚀 텍스트 시작 X 좌표 (좌측 정렬 기준)
            int startX = LEFT_MARGIN;
            // 🚀 텍스트가 넘어가서는 안되는 최대 너비
            int maxWidth = getWidth() - LEFT_MARGIN - RIGHT_MARGIN;
            // 대화가 시작될 Y 좌표
            int initialY = getHeight() - 260;
            int lineHeight = fm.getHeight() + 5; // 줄 간격 (+5 픽셀 추가)

            // 줄 바꿈 처리 함수 호출
            drawWrappedString(g, currentDialogue, startX, initialY, maxWidth, lineHeight);
        }
    }

    // 헬퍼 메서드: 버튼 생성 로직 중복 제거
    private JButton createStageButton(ImageIcon defaultIcon, ImageIcon rolloverIcon) {
        JButton button = new JButton();
        button.setIcon(defaultIcon);
        button.setRolloverIcon(rolloverIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        // 💡 이 코드를 추가하여 버튼이 키 이벤트를 가로채는 것을 방지합니다.
        button.setFocusable(false);
        return button;
    }
    /**
     * 주어진 Graphics 객체에 텍스트를 지정된 너비로 줄 바꿈하여 그리는 헬퍼 함수
     * @param g Graphics 객체
     * @param text 그릴 전체 텍스트
     * @param x 시작 X 좌표 (좌측 정렬 기준)
     * @param y 시작 Y 좌표 (첫 줄의 베이스 라인)
     * @param maxWidth 텍스트가 넘어가서는 안되는 최대 너비
     * @param lineHeight 줄 높이
     */
    private void drawWrappedString(Graphics g, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();

        // 텍스트를 공백 또는 줄 바꿈 문자(\n) 기준으로 분리
        String[] words = text.split("(?<=\\s)|(?=\\n)"); // 공백과 \n을 기준으로 분리

        StringBuilder currentLine = new StringBuilder();
        int currentY = y;

        for (String word : words) {
            // 단어에 \n이 포함되어 있으면 강제 개행
            if (word.contains("\n")) {
                // 현재까지 쌓인 줄을 그리고
                g.drawString(currentLine.toString().trim(), x, currentY);

                // 다음 줄로 이동
                currentY += lineHeight;
                currentLine.setLength(0); // 줄 초기화

                // \n 이후의 남은 문자(공백 등)를 처리
                String nextWord = word.replace("\n", "").trim();
                if (!nextWord.isEmpty()) {
                    currentLine.append(nextWord).append(" ");
                }
                continue;
            }

            // 현재 줄 + 다음 단어의 너비 측정
            String testLine = currentLine.toString() + word;
            int testWidth = fm.stringWidth(testLine);

            if (testWidth <= maxWidth) {
                // 너비가 초과되지 않으면 단어를 현재 줄에 추가
                currentLine.append(word);
            } else {
                // 너비가 초과되면 현재까지 쌓인 줄을 그리고
                g.drawString(currentLine.toString().trim(), x, currentY);

                // 다음 줄로 이동하고 새 줄에 현재 단어를 시작
                currentY += lineHeight;
                currentLine.setLength(0); // 줄 초기화
                currentLine.append(word);
            }
        }

        // 마지막으로 남아있는 줄을 그리기
        if (currentLine.length() > 0) {
            g.drawString(currentLine.toString().trim(), x, currentY);
        }
    }
}
