package game;

//메인 로비 화면
import javax.swing.*;
import java.awt.*;

public class LobbyPanel extends JPanel {
    private GameFrame gameFrame;
    private String username;
    private Image background;
    private Image alienTxt;
    private Image cakeTxt;

    // 💡 버튼과 텍스트의 영역을 정의
    private final Rectangle ALIEN_BUTTON_BOUNDS = new Rectangle(30, 0, 590, 580);
    private final Rectangle CAKE_BUTTON_BOUNDS = new Rectangle(700, 200, 500, 450);
    // 💡 뒤로가기 버튼 영역 추가 (오른쪽 상단)
    private final Rectangle BACK_BUTTON_BOUNDS = new Rectangle(950, 10, 300, 100);

    public LobbyPanel(GameFrame frame, String username) {
        this.gameFrame = frame;
        this.username = username;

        setLayout(null);
        frame.setLayout(new BorderLayout());

        LobbyBgmManager.start();

        background = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage1/Background_stage1-1.png")).getImage();
        alienTxt = new ImageIcon(Main.class.getResource("../images/mainUI/alienStage_txt.png")).getImage();
        cakeTxt = new ImageIcon(Main.class.getResource("../images/mainUI/cakeStage_txt.png")).getImage();

        ImageIcon alien1 = new ImageIcon(getClass().getResource("../images/mainUI/alienStage_unselected.png"));
        ImageIcon alien2 = new ImageIcon(getClass().getResource("../images/mainUI/alienStage_selected.png"));
        ImageIcon cake1 = new ImageIcon(getClass().getResource("../images/mainUI/cakeStage_unselected.png"));
        ImageIcon cake2 = new ImageIcon(getClass().getResource("../images/mainUI/cakeStage_selected.png"));
        ImageIcon back1 = new ImageIcon(getClass().getResource("../images/mainUI/Buttons/Signup_back_unselected.png"));
        ImageIcon back2 = new ImageIcon(getClass().getResource("../images/mainUI/Buttons/Signup_back_selected.png"));

        // 💡 외계인 텍스트를 위한 JLabel (버튼 위에 겹쳐서 표시)
        JLabel alienTextLabel = createTextLabel(alienTxt, ALIEN_BUTTON_BOUNDS);
        //텍스트 레이블의 마우스 이벤트를 비활성화합니다.
        alienTextLabel.setIgnoreRepaint(true); // 페인트 이벤트를 무시하여 성능 개선
        alienTextLabel.setOpaque(false);
        add(alienTextLabel);
        // --- 외계인 버튼 ---
        JButton alienButton = createStageButton(alien1, alien2);
        alienButton.setBounds(ALIEN_BUTTON_BOUNDS); // 💡 위치와 크기 설정
        add(alienButton);
        // 💡 외계인 버튼 클릭 이벤트 리스너 추가
        alienButton.addActionListener(e -> {
            // 버튼 클릭 시 수행할 동작: 외계인 스테이지 인트로 패널을 실행
            gameFrame.showSpaceIntroScreen();
        });

        // 💡 케이크 텍스트를 위한 JLabel
        JLabel cakeTextLabel = createTextLabel(cakeTxt, CAKE_BUTTON_BOUNDS);
        cakeTextLabel.setIgnoreRepaint(true);
        cakeTextLabel.setOpaque(false);
        add(cakeTextLabel);
        // --- 케이크 버튼 ---
        JButton cakeButton = createStageButton(cake1, cake2);
        cakeButton.setBounds(CAKE_BUTTON_BOUNDS); // 💡 위치와 크기 설정
        add(cakeButton);
        // 💡 케이크 버튼 클릭 이벤트 리스너 추가
        cakeButton.addActionListener(e -> {
            // 버튼 클릭 시 수행할 동작: 케이크 스테이지 패널을 실행
            gameFrame.showCakeScreen();
        });
    // --- 뒤로가기 버튼 ---
        JButton backButton = createStageButton(back1, back2);
        backButton.setBounds(BACK_BUTTON_BOUNDS); // 💡 오른쪽 상단 위치와 크기 설정
        add(backButton);

        // 💡 뒤로가기 버튼 클릭 이벤트 리스너 추가: 홈 화면으로 이동
        backButton.addActionListener(e -> {
            // 버튼 클릭 시 수행할 동작: 홈 화면으로 이동
            gameFrame.showHomeScreen();
        });

    }
    // 헬퍼 메서드: 버튼 생성 로직 중복 제거
    private JButton createStageButton(ImageIcon defaultIcon, ImageIcon rolloverIcon) {
        JButton button = new JButton();
        button.setIcon(defaultIcon);
        button.setRolloverIcon(rolloverIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }

    // 헬퍼 메서드: 텍스트 레이블 생성
    private JLabel createTextLabel(Image image, Rectangle bounds) {
        JLabel label = new JLabel(new ImageIcon(image));
        label.setBounds(bounds); // 버튼과 동일한 위치에 배치
        return label;
    }


    // 3. paintComponent 오버라이드
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 💡 필수 호출

        // 배경 이미지만 그립니다.
        // 버튼(JButton)과 텍스트(JLabel)는 add() 메서드로 추가되었으므로 Swing이 알아서 그려줍니다.
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
    }
}