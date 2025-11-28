package game;

import game.Cake.CakePanel;
import game.Space.SpaceIntroPanel;
import game.Space.SpacePanel;
import game.Space.StageManager;


import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private String currentUser; // 로그인한 사용자 정보
    private Image screenImage;
    private Graphics screenGraphic;

    public GameFrame() {

        setTitle("Game");
        setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
        // ✅ [핵심] 게임 시작 시 이전 상태 정리 및 초기화
        StageManager.resetGame();

//        // ✅ 게임 시작 시 음악 & 기습 여부 설정
//        StageManager.initializeStage();
//
//        // ✅ StageManager를 통해 음악 스레드를 한 번만 안전하게 시작
//        StageManager.startMusicAndTimer();
//
//        // ✅ 확인 출력 (개발 중 디버그용)
//        System.out.println("[선택된 음악 index] " + StageManager.musicIndex);
//        System.out.println("[기습 활성 여부] " + StageManager.ambushEnabled);

        //한 번 만들어진 게임창은 사용자가 임의적으로 줄이거나 키울 수 없음
        setResizable(false);
        //실행 시 게임창이 컴퓨터 정 중앙에 뜸
        setLocationRelativeTo(null);
        //게임창 닫으면 게임 종료
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // SpacePanel 자체가 CardLayout 화면 컨트롤러
        //add(new SpacePanel());

        // 메인 home화면부터 실행
        showHomeScreen();

        setVisible(true);

    }

    public void showHomeScreen() {
        getContentPane().removeAll(); // 기존 화면 제거
        getContentPane().add(new HomePanel(this));
        revalidate(); // 레이아웃 다시 계산
        repaint(); // 화면 다시 그리기
    }

    public void showLoginScreen() {
        getContentPane().removeAll();
        getContentPane().add(new LoginPanel(this));
        revalidate();
        repaint();
    }

    public void showSignupScreen() {
        getContentPane().removeAll();
        getContentPane().add(new SignupPanel(this));
        revalidate();
        repaint();
    }

    public void showLobbyScreen(String username) {
        this.currentUser = username;
        getContentPane().removeAll();
        getContentPane().add(new LobbyPanel(this, username));
        revalidate();
        repaint();
    }

    public void showSpaceScreen() {
        LobbyBgmManager.stop();
        getContentPane().removeAll();
        getContentPane().add(new SpacePanel(this));  // ✅ frame 넘겨주기
        revalidate();
        repaint();
    }

    public void showSpaceIntroScreen() {
        LobbyBgmManager.stop();
        getContentPane().removeAll();
        getContentPane().add(new SpaceIntroPanel(this));  // ✅ frame 넘겨주기
        revalidate();
        repaint();
    }

    public void showCakeScreen() {
        LobbyBgmManager.stop();
        getContentPane().removeAll(); // 기존 화면 제거
        getContentPane().add(new CakePanel(this));
        revalidate(); // 레이아웃 다시 계산
        repaint(); // 화면 다시 그리기
    }

    public void showCakeIntroScreen() {
        LobbyBgmManager.stop();
        getContentPane().removeAll(); // 기존 화면 제거
        //getContentPane().add(new CakeIntroPanel(this));
        revalidate(); // 레이아웃 다시 계산
        repaint(); // 화면 다시 그리기
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
