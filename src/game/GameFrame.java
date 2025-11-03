package game;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private Image screenImage;
    private Graphics screenGraphic;

    public GameFrame() {
        setTitle("Game");
        setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
        //한 번 만들어진 게임창은 사용자가 임의적으로 줄이거나 키울 수 없음
        setResizable(false);
        //실행 시 게임창이 컴퓨터 정 중앙에 뜸
        setLocationRelativeTo(null);
        //게임창 닫으면 게임 종료
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //게임창 보이게
        setVisible(true);
    }
}
