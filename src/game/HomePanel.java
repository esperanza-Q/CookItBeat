package game;

//홈 화면
import javax.swing.*;
import java.awt.*;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {
    private GameFrame gameFrame;
    //private CardLayout cardLayout = new CardLayout();

    private Image title;
    private Image tool1;
    private Image tool2;
    private Image hat;
    private Image cloud;
    private Image background;

    public HomePanel(GameFrame frame) {
        this.gameFrame = frame;
        setLayout(new BorderLayout());

        // 타이틀
        background = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage1/Background_stage1-1.png")).getImage();
        cloud = new ImageIcon(Main.class.getResource("../images/mainUI/main_cloud.png")).getImage();
        hat = new ImageIcon(Main.class.getResource("../images/mainUI/main_hat.png")).getImage();
        tool1 = new ImageIcon(Main.class.getResource("../images/mainUI/main_tool1.png")).getImage();
        tool2 = new ImageIcon(Main.class.getResource("../images/mainUI/main_tool2.png")).getImage();
        title = new ImageIcon(Main.class.getResource("../images/mainUI/main_txt.png")).getImage();

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);

        JButton loginButton = createStyledButton("로그인");
        JButton signupButton = createStyledButton("회원가입");  // 추가
        JButton nonmemberButton = createStyledButton("비회원 플레이"); //회원가입 DB 구현 부진... 바로 로비로 넘어가는 버튼 생성해뒀습니다.
        JButton exitButton = createStyledButton("종료");

        // 버튼 클릭 이벤트
        loginButton.addActionListener(e -> gameFrame.showLoginScreen());
        signupButton.addActionListener(e -> gameFrame.showSignupScreen());  // 추가
        exitButton.addActionListener(e -> System.exit(0));
        nonmemberButton.addActionListener(e -> gameFrame.showLobbyScreen("nonMember"));

        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(signupButton);  // 추가
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(nonmemberButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(exitButton);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(200, 50));
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        return button;
    }
    public void paintComponent(Graphics g) {
        g.drawImage(background, 0, 0, null);
        g.drawImage(cloud, 150,10,927, 567, this);
        g.drawImage(hat, 150,10, 927, 567, this);
        g.drawImage(tool1,150,10,927, 567, this);
        g.drawImage(tool2,150,10,927, 567, this);
        g.drawImage(title,150,10,927, 567, this);
    }
}

