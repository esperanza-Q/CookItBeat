package game;

import javax.swing.*;

import game.DBConnection;

import java.awt.*;

public class SignupPanel extends JPanel {
    private GameFrame gameFrame;
    private JTextField usernameField;

    public SignupPanel(GameFrame frame) {
        this.gameFrame = frame;

        // ✅ 로비 BGM 유지
        LobbyBgmManager.start();

        setLayout(new GridBagLayout());
        setBackground(new Color(240, 240, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 타이틀
        JLabel titleLabel = new JLabel("회원가입");
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

        // 비밀번호 확인 라벨
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel confirmLabel = new JLabel("비밀번호 확인:");
        confirmLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        add(confirmLabel, gbc);

        // 비밀번호 확인 입력
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        add(confirmPasswordField, gbc);
*/
        // 안내 문구
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel infoLabel = new JLabel("* 아이디: 4-20자 영문, 숫자");
        infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        infoLabel.setForeground(Color.GRAY);
        add(infoLabel, gbc);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton signupButton = new JButton("가입하기");
        signupButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        signupButton.setPreferredSize(new Dimension(100, 40));
        signupButton.addActionListener(e -> signup());

        JButton backButton = new JButton("뒤로가기");
        backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        backButton.setPreferredSize(new Dimension(100, 40));
        backButton.addActionListener(e -> gameFrame.showHomeScreen());

        buttonPanel.add(signupButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 10, 10, 10);
        add(buttonPanel, gbc);

        // Enter 키로 회원가입
        usernameField.addActionListener(e -> signup());
    }

    private void signup() {
        String username = usernameField.getText().trim();

        // 1. 빈 값 체크
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "모든 항목을 입력해주세요!",
                    "입력 오류",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. 아이디 유효성 검사 (4-20자, 영문+숫자만)
        if (!username.matches("^[a-zA-Z0-9]{4,20}$")) {
            JOptionPane.showMessageDialog(this,
                    "아이디는 4-20자 영문과 숫자만 사용 가능합니다!",
                    "아이디 오류",
                    JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }
/*
        // 3. 비밀번호 길이 체크
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "비밀번호는 6자 이상이어야 합니다!",
                    "비밀번호 오류",
                    JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        // 4. 비밀번호 일치 확인
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "비밀번호가 일치하지 않습니다!",
                    "비밀번호 오류",
                    JOptionPane.WARNING_MESSAGE);
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }
*/

        // 5. 데이터베이스에 회원가입
        boolean success = DBConnection.registerUser(username);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "회원가입이 완료되었습니다!\n로그인 화면으로 이동합니다.",
                    "가입 성공",
                    JOptionPane.INFORMATION_MESSAGE);
            gameFrame.showLoginScreen();
        } else {
            JOptionPane.showMessageDialog(this,
                    "이미 존재하는 아이디입니다!",
                    "가입 실패",
                    JOptionPane.ERROR_MESSAGE);
            usernameField.setText("");
            usernameField.requestFocus();
        }


    }
}
