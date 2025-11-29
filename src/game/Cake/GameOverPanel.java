package game.Cake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameOverPanel extends JPanel {

    // ‼️ 주의: CakePanel에 돌아가기 로직(예: switchToMainScreen)을 호출해야 합니다.
    private CakePanel cakePanel;

    private static final String GAME_OVER_TEXT = "GAME OVER";
    private final Font FONT_LARGE = new Font("Arial", Font.BOLD, 80);

    public GameOverPanel(CakePanel panel) {
        this.cakePanel = panel;

        // 1. 패널 기본 설정
        setLayout(new GridBagLayout()); // 요소를 중앙에 배치하기 위해 사용
        setBackground(Color.BLACK);

        // 2. 구성 요소 추가
        addGameOverLabel();
        // addRestartButton(); // 필요하다면 버튼을 추가할 수 있습니다.
    }

    /**
     * "GAME OVER" 텍스트 레이블을 생성하고 중앙에 배치합니다.
     */
    private void addGameOverLabel() {
        JLabel gameOverLabel = new JLabel(GAME_OVER_TEXT);
        gameOverLabel.setFont(FONT_LARGE);
        gameOverLabel.setForeground(Color.RED);

        // GridBagLayout의 제약 조건 설정 (중앙 배치)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        add(gameOverLabel, gbc);
    }

    /*
     * ‼️ (선택 사항) 재시작 버튼을 추가하고 CakePanel의 메서드를 호출하는 예시
     * private void addRestartButton() {
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
     * add(restartButton, gbc);
     * }
     */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 배경을 검은색으로 유지합니다.
    }
}