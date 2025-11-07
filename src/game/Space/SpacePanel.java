package game.Space;

import javax.swing.*;
import java.awt.*;

public class SpacePanel extends JPanel {

    private CardLayout cardLayout = new CardLayout();

    public SpacePanel() {
        setLayout(cardLayout);

        // 화면 1 : 애니메이션 패널
        SpaceAnimation card1 = new SpaceStage1(); // 여기 타입을 SpaceAnimation으로!
        JButton nextButton = new JButton("Next");
        card1.setLayout(null);
        nextButton.setBounds(20, 20, 100, 40);
        card1.add(nextButton);

        // 화면 2 : 다른 화면
        JPanel card2 = new JPanel();
        card2.setBackground(Color.BLACK);
        JButton backButton = new JButton("Back");
        card2.add(backButton);

        add(card1, "Panel1");
        add(card2, "Panel2");

        // 화면 전환 버튼
        nextButton.addActionListener(e -> cardLayout.show(this, "Panel2"));
        backButton.addActionListener(e -> {
            cardLayout.show(this, "Panel1");

            // 🔥 다시 Panel1으로 돌아왔을 때 포커스 재획득
            SwingUtilities.invokeLater(() -> card1.requestFocusInWindow());
        });

        // 🔥 처음 실행될 때 SpaceAnimation 패널이 키 입력을 받을 수 있도록 포커스 주기
        SwingUtilities.invokeLater(() -> card1.requestFocusInWindow());
    }
}
