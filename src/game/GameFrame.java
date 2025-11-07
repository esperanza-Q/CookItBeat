package game;

import game.Space.SpacePanel;
import game.Space.StageManager;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private Image screenImage;
    private Graphics screenGraphic;

    public GameFrame() {

        setTitle("Game");
        setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
// ✅ [핵심] 게임 시작 시 이전 상태 정리 및 초기화
        StageManager.resetGame();

        // ✅ 게임 시작 시 음악 & 기습 여부 설정
        StageManager.initializeStage();

        // ✅ StageManager를 통해 음악 스레드를 한 번만 안전하게 시작
        StageManager.startMusicAndTimer();

        // ✅ 확인 출력 (개발 중 디버그용)
        System.out.println("[선택된 음악 index] " + StageManager.musicIndex);
        System.out.println("[기습 활성 여부] " + StageManager.ambushEnabled);

        //한 번 만들어진 게임창은 사용자가 임의적으로 줄이거나 키울 수 없음
        setResizable(false);
        //실행 시 게임창이 컴퓨터 정 중앙에 뜸
        setLocationRelativeTo(null);
        //게임창 닫으면 게임 종료
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // SpacePanel 자체가 CardLayout 화면 컨트롤러
        add(new SpacePanel());

        setVisible(true);

    }


}
