package game;

//홈 화면
import javax.swing.*;
import java.awt.*;


public class HomePanel extends JPanel {
    private GameFrame gameFrame;
    //private CardLayout cardLayout = new CardLayout();
    private Image background;

    public HomePanel(GameFrame frame) {
        this.gameFrame = frame;
        setLayout(new BorderLayout());

        LobbyBgmManager.start();
        // 💡 목표 크기 설정
        final int BUTTON_WIDTH = 320; // 원하는 너비
        final int BUTTON_HEIGHT = 65; // 원하는 높이

        // 타이틀
        background = new ImageIcon(Main.class.getResource("../images/mainUI/mainTitle.png")).getImage();

        ImageIcon exit_off = scaleImage(
                new ImageIcon(getClass().getResource("../images/mainUI/Buttons/ExitButton_unselected.png")),
                BUTTON_WIDTH, BUTTON_HEIGHT
        );
        ImageIcon exit_on = scaleImage(
                new ImageIcon(getClass().getResource("../images/mainUI/Buttons/ExitButton_selected.png")),
                BUTTON_WIDTH, BUTTON_HEIGHT
        );
        ImageIcon login_off = scaleImage(
                new ImageIcon(getClass().getResource("../images/mainUI/Buttons/LoginButton_unselected.png")),
                BUTTON_WIDTH, BUTTON_HEIGHT
        );
        ImageIcon login_on = scaleImage(
                new ImageIcon(getClass().getResource("../images/mainUI/Buttons/LoginButton_selected.png")),
                BUTTON_WIDTH, BUTTON_HEIGHT
        );
        ImageIcon start_off = scaleImage(
                new ImageIcon(getClass().getResource("../images/mainUI/Buttons/NonmemberButton_unselected.png")),
                BUTTON_WIDTH, BUTTON_HEIGHT
        );
        ImageIcon start_on = scaleImage(
                new ImageIcon(getClass().getResource("../images/mainUI/Buttons/NonmemberButton_selected.png")),
                BUTTON_WIDTH, BUTTON_HEIGHT
        );
        ImageIcon signup_off = scaleImage(
                new ImageIcon(getClass().getResource("../images/mainUI/Buttons/SignupButton_unselected.png")),
                BUTTON_WIDTH, BUTTON_HEIGHT
        );
        ImageIcon signup_on = scaleImage(
                new ImageIcon(getClass().getResource("../images/mainUI/Buttons/SignupButton_selected.png")),
                BUTTON_WIDTH, BUTTON_HEIGHT
        );

        // --- 🎯 버튼들을 담고 정렬할 컨테이너 패널 생성 ---
        JPanel buttonPanel = new JPanel();
        // 💡 버튼들만 보이게 하기 위해 투명하게 설정
        buttonPanel.setOpaque(false);
        // 💡 버튼들을 수직으로 쌓기 위해 BoxLayout 사용 (또는 GridLayout(4, 1) 사용 가능)
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // --- 회원가입 버튼 ---
        JButton SignupButton = createStageButton(signup_off, signup_on);
        SignupButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        buttonPanel.add(SignupButton);
        buttonPanel.add(Box.createVerticalStrut(3)); // 버튼 사이에 간격 추가 (5px)
        SignupButton.addActionListener(e -> { gameFrame.showSignupScreen(); });

        // --- 로그인 버튼 ---
        JButton LoginButton = createStageButton(login_off, login_on);
        LoginButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        buttonPanel.add(LoginButton);
        buttonPanel.add(Box.createVerticalStrut(3)); // 버튼 사이에 간격 추가
        LoginButton.addActionListener(e -> { gameFrame.showLoginScreen(); });

        // --- 바로시작 버튼 ---
        JButton StartButton = createStageButton(start_off, start_on);
        StartButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        buttonPanel.add(StartButton);
        buttonPanel.add(Box.createVerticalStrut(3)); // 버튼 사이에 간격 추가
        StartButton.addActionListener(e -> { gameFrame.showLobbyScreen("nonmember"); });

        // --- 나가기 버튼 ---
        JButton ExitButton = createStageButton(exit_off, exit_on);
        ExitButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        buttonPanel.add(ExitButton);
        ExitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(Box.createVerticalStrut(15));
        // --- 🎯 HomePanel의 남쪽에 버튼 컨테이너 배치 ---
        // buttonPanel이 HomePanel의 중앙 영역을 차지하고, BoxLayout으로 버튼들을 수직 중앙 정렬합니다.
        add(buttonPanel, BorderLayout.SOUTH);
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
    // 이미지를 원하는 크기로 조정하는 헬퍼 메서드
    private ImageIcon scaleImage(ImageIcon icon, int newWidth, int newHeight) {
        if (icon == null) return null;
        Image img = icon.getImage();
        // Image.SCALE_SMOOTH를 사용하여 품질을 유지하며 크기 조정
        Image scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(background, 0, 0, null);
    }
}