package game;

public class LobbyBgmManager {

    private static Music lobbyMusic;
    private static boolean playing = false;

    // 로비 BGM 시작(이미 켜져있으면 재시작 X)
    public static void start() {
        if (playing) return;

        // 너 Music 생성자/메서드 방식에 맞춰서 loop 켜기
        lobbyMusic = new Music("lobby_sound.mp3", true);
        lobbyMusic.start();
        playing = true;
    }

    // 로비 BGM 종료
    public static void stop() {
        if (lobbyMusic != null) {
            lobbyMusic.close();   // Music에 stop/close가 있다면 그걸로
            lobbyMusic = null;
        }
        playing = false;
    }

    public static boolean isPlaying() {
        return playing;
    }
}
