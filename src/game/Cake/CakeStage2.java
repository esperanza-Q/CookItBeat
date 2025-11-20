package game.Cake;

import game.Main;

import javax.swing.*;
import java.awt.*;

//일단 거의 이미지 불러오기만 해뒀습니다
public class CakeStage2 {
    private Image background;
    private Image bowl;
    private Image green1;
    private Image green2;
    private Image blue1;
    private Image blue2;

    private Image currentUser;
    private Image currentGuide;

    public CakeStage2() {
        background = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage2/dough_background.png")).getImage();
        bowl = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage2/dough_bowl.png")).getImage();
        green1 = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage2/whipping_green.png")).getImage();
        green2 = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage2/whipping_green_doughO.png")).getImage();
        blue1 = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage2/whipping_blue.png")).getImage();
        blue2 = new ImageIcon(Main.class.getResource("../images/cakeStage_image/stage2/whipping_blue_doughO.png")).getImage();

        currentUser = null;
        currentGuide = green1;

    }


    public void drawStageObjects(Graphics g) {
        g.drawImage(currentGuide, 0,0,null);
        if (currentUser != null) {
            g.drawImage(currentUser, 0,0,null);
        }
    }

    private void changeStageImageOnPress() {
        if (currentUser == blue1) this.currentUser = blue2;
    }
    private void changeStageImageOnRelease() {
        if (currentUser == blue2) this.currentUser = blue1;
    }
}
