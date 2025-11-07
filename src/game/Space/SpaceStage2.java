package game.Space;

import game.Main;

import javax.swing.*;
import java.awt.*;

public class SpaceStage2 extends SpaceAnimation {

    private Image portal;

    public SpaceStage2() {
        portal = new ImageIcon(Main.class.getResource("../images/stage2/portal.png")).getImage();
    }

    @Override
    protected void drawStageObjects(Graphics g) {
        g.drawImage(portal, 700, 200, null);
    }
}