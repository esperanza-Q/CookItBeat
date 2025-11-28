package game;

//로그인 화면
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private GameFrame gameFrame;
    private JTextField usernameField;
    private Image background;

    public LoginPanel(GameFrame frame) {
        this.gameFrame = frame;
        background = new ImageIcon(Main.class.getResource("../images/mainUI/background_paper.png")).getImage();

        // ✅ 로비 BGM 유지 (Home에서 켜졌으면 그대로, 아니면 여기서 켜짐)
        LobbyBgmManager.start();

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 타이틀 (이미지로 대체)
        ImageIcon titleIcon = new ImageIcon(Main.class.getResource("../images/mainUI/Login_txt.png"));
        JLabel titleLabel;
        if (titleIcon != null) {
            titleLabel = new JLabel(titleIcon);
        } else {
            // 이미지를 찾지 못하면 기존 텍스트로 대체
            titleLabel = new JLabel("로그인");
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 40, 10);
        add(titleLabel, gbc);

        // 아이디 라벨 (이미지로 대체)
        ImageIcon userIcon = new ImageIcon(Main.class.getResource("../images/mainUI/id_txt.png"));
        JLabel userLabel;

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 10, 10, 10);

        if (userIcon != null) {
            userLabel = new JLabel(userIcon);
        } else {
            userLabel = new JLabel("아이디:");
            userLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        }
        add(userLabel, gbc);

        // 아이디 입력
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
        add(usernameField, gbc);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

// ----------------- 로그인 버튼 -----------------
        ImageIcon login_off = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/Login_unselected.png"));
        ImageIcon login_on = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/Login_selected.png"));

        JButton loginButton = new JButton();
        if (login_off != null) {
            loginButton.setIcon(login_off);
            if (login_on != null) {
                loginButton.setRolloverIcon(login_on); // 롤오버 아이콘 설정
            }
            // 이미지를 사용하므로 텍스트, 경계, 배경을 숨김
            loginButton.setBorderPainted(false);
            loginButton.setContentAreaFilled(false);
            loginButton.setFocusPainted(false);
            // 버튼 크기는 아이콘 크기에 맞춰지므로 setPreferredSize는 필요 없습니다.
        } else {
            // 이미지 로드 실패 시 대체
            loginButton.setText("로그인");
            loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            loginButton.setPreferredSize(new Dimension(100, 40));
        }
        loginButton.addActionListener(e -> login());


// ----------------- 뒤로가기 버튼 -----------------
        ImageIcon back_off = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/Signup_back_unselected.png"));
        ImageIcon back_on = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/Signup_back_selected.png"));

        JButton backButton = new JButton();
        if (back_off != null) {
            backButton.setIcon(back_off);
            if (back_on != null) {
                backButton.setRolloverIcon(back_on); // 롤오버 아이콘 설정
            }
            // 이미지를 사용하므로 텍스트, 경계, 배경을 숨김
            backButton.setBorderPainted(false);
            backButton.setContentAreaFilled(false);
            backButton.setFocusPainted(false);
        } else {
            // 이미지 로드 실패 시 대체
            backButton.setText("뒤로가기");
            backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            backButton.setPreferredSize(new Dimension(100, 40));
        }
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
    public void paintComponent(Graphics g) {
        g.drawImage(background, 0, 0, null);
    }
}