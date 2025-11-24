package game;

//로그인 화면
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private GameFrame gameFrame;
    private JTextField usernameField;

    public LoginPanel(GameFrame frame) {
        this.gameFrame = frame;

        // ✅ 로비 BGM 유지 (Home에서 켜졌으면 그대로, 아니면 여기서 켜짐)
        LobbyBgmManager.start();

        setLayout(new GridBagLayout());
        setBackground(new Color(240, 240, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 타이틀
        JLabel titleLabel = new JLabel("로그인");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 40, 10);
        add(titleLabel, gbc);

        // 아이디 라벨
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 10, 10, 10);
        JLabel userLabel = new JLabel("아이디:");
        userLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        add(userLabel, gbc);

        // 아이디 입력
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        add(usernameField, gbc);
/*
        // 비밀번호 라벨
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passLabel = new JLabel("비밀번호:");
        passLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        add(passLabel, gbc);

        // 비밀번호 입력
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        add(passwordField, gbc);
*/
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton loginButton = new JButton("로그인");
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(100, 40));
        loginButton.addActionListener(e -> login());

        JButton backButton = new JButton("뒤로가기");
        backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        backButton.setPreferredSize(new Dimension(100, 40));
        backButton.addActionListener(e -> gameFrame.showHomeScreen());

        buttonPanel.add(loginButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 10, 10, 10);
        add(buttonPanel, gbc);

        // Enter 키로 로그인
        usernameField.addActionListener(e -> login());
    }

    private void login() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "아이디를 입력하세요!",
                    "입력 오류",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 데이터베이스 검증
        if (DBConnection.validateLogin(username)){
            JOptionPane.showMessageDialog(this,
                    "로그인 성공!",
                    "성공",
                    JOptionPane.INFORMATION_MESSAGE);
            gameFrame.showLobbyScreen(username);
        } else {
            JOptionPane.showMessageDialog(this,
                    "아이디 또는 비밀번호가 올바르지 않습니다!",
                    "로그인 실패",
                    JOptionPane.ERROR_MESSAGE);
            usernameField.setText("");
        }

    }
}