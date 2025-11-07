package game;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class Music extends Thread{

    private Player player;
    private boolean isLoop;
    private boolean isPlaying = false;
    private File file;
    private FileInputStream fis;
    private BufferedInputStream bis;

    public Music(String name, boolean isLoop) {
        try{
            this.isLoop = isLoop;
            file = new File(Main.class.getResource("../music/"+name).toURI());
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            player = new Player(bis);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    //음악 일시정지
    public int getTime() {
        if(player==null){
            return 0;
        }
        return player.getPosition();
    }

    //음악 멈춤
    public void close() {
        isLoop = false;
        player.close();
        this.interrupt();
    }

    //음악 실행
    @Override
    public void run() {
        try {
            do {
                player.play();
            } while(isLoop);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    // ✅ MP3 전체 길이 (초) 구하기
    public static int getMusicLength(String filename) {
        try {
            File file = new File(Main.class.getResource("../music/" + filename).toURI());
            FileInputStream fis = new FileInputStream(file);
            Bitstream bitstream = new Bitstream(fis);
            Header header = bitstream.readFrame();

            if (header == null) return 0;

            int bitrate = header.bitrate(); // bps
            long fileSizeBits = file.length() * 8;

            int lengthInSeconds = (int) (fileSizeBits / bitrate);

            bitstream.close();
            fis.close();
            return lengthInSeconds;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ✅ mm:ss 포맷 변환
    public static String formatTime(int sec) {
        int min = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", min, s);
    }
}
