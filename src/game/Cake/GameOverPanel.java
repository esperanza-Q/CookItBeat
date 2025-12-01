package game.Cake;

import game.Main;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {

    // ‼️ 주의: CakePanel에 돌아가기 로직(예: switchToMainScreen)을 호출해야 합니다.
    private CakePanel cakePanel;

    // 사용하지 않게 되었습니다.
    //private static final String GAME_OVER_TEXT = "GAME OVER";
    //private final Font FONT_LARGE = new Font("Arial", Font.BOLD, 80);

    // 게임오버 화면 꾸밈 요소 추가
    private Image background;


    public GameOverPanel(CakePanel panel) {
        this.cakePanel = panel;

        background = new ImageIcon(Main.class.getResource("../images/mainUI/Gameover.png")).getImage();
        ImageIcon lobby1 = new ImageIcon(getClass().getResource("../../images/mainUI/Buttons/tolobbyButton_unselected.png"));
        ImageIcon lobby2 = new ImageIcon(getClass().getResource("../../images/mainUI/Buttons/tolobbyButton_selected.png"));
        ImageIcon restart1 = new ImageIcon(getClass().getResource("../../images/mainUI/Buttons/RestartButton_unselected.png"));
        ImageIcon restart2 = new ImageIcon(getClass().getResource("../../images/mainUI/Buttons/RestartButton_selected.png"));

        // 1. 패널 기본 설정
        //setLayout(new GridBagLayout()); // 요소를 중앙에 배치하기 위해 사용
        //setBackground(Color.BLACK);
        setLayout(null);

        JButton restartButton = createStageButton(restart1, restart2);
        restartButton.setBounds(950, 550, 300, 100);
        add(restartButton);

        // 💡 버튼 클릭 이벤트 리스너 추가: 케이크 화면으로 이동
        restartButton.addActionListener(e -> {
            // 버튼 클릭 시 수행할 동작: 케이크 화면으로 이동
            cakePanel.switchToCakeScreen();
        });

        JButton lobbyButton = createStageButton(lobby1, lobby2);
        lobbyButton.setBounds(10, 550, 300, 100);
        add(lobbyButton);

        // 💡 버튼 클릭 이벤트 리스너 추가: 홈 화면으로 이동
        lobbyButton.addActionListener(e -> {
            // 버튼 클릭 시 수행할 동작: 홈 화면으로 이동
            cakePanel.switchToMainScreen();
        });

        // 2. 구성 요소 추가
        //addGameOverLabel();
        // addRestartButton(); // 필요하다면 버튼을 추가할 수 있습니다.
    }

    /**
     * "GAME OVER" 텍스트 레이블을 생성하고 중앙에 배치합니다.
     */
//    private void addGameOverLabel() {
//        JLabel gameOverLabel = new JLabel(GAME_OVER_TEXT);
//        gameOverLabel.setFont(FONT_LARGE);
//        gameOverLabel.setForeground(Color.RED);
//
//        // GridBagLayout의 제약 조건 설정 (중앙 배치)
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.anchor = GridBagConstraints.CENTER;
//
//        add(gameOverLabel, gbc);
//    }


     // ‼️ (선택 사항) 재시작 버튼을 추가하고 CakePanel의 메서드를 호출하는 예시
     /* private void addRestartButton() {
     * JButton restartButton = new JButton("메인 화면으로");
     * restartButton.setFont(new Font("Arial", Font.BOLD, 24));
     * restartButton.addActionListener(new ActionListener() {
     * @Override
     * public void actionPerformed(ActionEvent e) {
     * if (cakePanel != null) {
     * // ‼️ CakePanel의 메인 화면 전환 메서드를 호출해야 합니다.
     * // cakePanel.switchToMainScreen();
     * System.out.println("메인 화면 전환 요청");
     * }
     * }
     * });
     * * GridBagConstraints gbc = new GridBagConstraints();
     * gbc.gridx = 0;
     * gbc.gridy = 1; // GAME OVER 아래에 배치
     * gbc.insets = new Insets(50, 0, 0, 0); // 상단 여백
      add(restartButton, gbc);
     } */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 배경을 검은색으로 유지합니다.
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
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
}