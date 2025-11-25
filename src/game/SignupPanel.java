package game;

import javax.swing.*;

import game.DBConnection;

import java.awt.*;

public class SignupPanel extends JPanel {
    private GameFrame gameFrame;
    private JTextField usernameField;
    private Image background;

    public SignupPanel(GameFrame frame) {
        this.gameFrame = frame;
        background = new ImageIcon(Main.class.getResource("../images/mainUI/background_paper.png")).getImage();

        // ✅ 로비 BGM 유지
        LobbyBgmManager.start();

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 타이틀 (이미지로 대체)
        ImageIcon titleIcon = new ImageIcon(Main.class.getResource("../images/mainUI/Signup_txt.png"));
        JLabel titleLabel;
        if (titleIcon != null) {
            titleLabel = new JLabel(titleIcon);
        } else {
            // 이미지를 찾지 못하면 기존 텍스트로 대체
            titleLabel = new JLabel("회원가입");
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

        // 안내 문구
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel infoLabel = new JLabel("* 아이디: 4-20자 영문, 숫자");
        infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
        infoLabel.setForeground(Color.GRAY);
        add(infoLabel, gbc);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

// ----------------- 가입하기 버튼 -----------------
        ImageIcon signup_off = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/Signup_txt_unselected.png"));
        ImageIcon signup_on = new ImageIcon(Main.class.getResource("../images/mainUI/Buttons/Signup_txt_selected.png"));

        JButton signupButton = new JButton();
        if (signup_off != null) {
            signupButton.setIcon(signup_off);
            if (signup_on != null) {
                signupButton.setRolloverIcon(signup_on); // 롤오버 아이콘 설정
            }
            // 이미지를 사용하므로 텍스트, 경계, 배경을 숨김
            signupButton.setBorderPainted(false);
            signupButton.setContentAreaFilled(false);
            signupButton.setFocusPainted(false);
            // 버튼 크기는 아이콘 크기에 맞춰지므로 setPreferredSize는 필요 없습니다.
        } else {
            // 이미지 로드 실패 시 대체
            signupButton.setText("가입하기");
            signupButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            signupButton.setPreferredSize(new Dimension(100, 40));
        }
        signupButton.addActionListener(e -> signup());


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
    public void paintComponent(Graphics g) {
        g.drawImage(background, 0, 0, null);
    }
}
