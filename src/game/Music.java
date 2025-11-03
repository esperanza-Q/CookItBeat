package game;

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
}
