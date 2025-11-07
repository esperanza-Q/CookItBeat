package game.Space;

import game.Main;

import javax.swing.*;
import java.awt.*;

public class SpaceStage1 extends SpaceAnimation {

    private Image alien1;
    private Image alien2;
    private Image cat1;
    private Image cat2;

    public SpaceStage1() {
        alien1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alienHand01.png")).getImage();
        alien2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alienHand02.png")).getImage();
        cat1 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand01.png")).getImage();
        cat2 = new ImageIcon(Main.class.getResource("../images/alienStage_image/alien_catHand02.png")).getImage();
    }

    @Override
    protected void drawStageObjects(Graphics g) {
        // ✅ 여기서 Stage 1에 필요한 요소만 그리면 됨
        g.drawImage(alien1, 0, 0, null);
    }
}
