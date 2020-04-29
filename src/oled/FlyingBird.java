package oled;

import oled.OLED.OLEDContent;
import util.RessourceLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Demo diplay content: Auto refresh enabled, image output, loading image ressources
 * <br> <br>
 * Shows a flying bird animation
 */
public class FlyingBird extends OLEDContent
{
    int frame = 0;
    final BufferedImage[] BirdAsset = new BufferedImage[8];

    public FlyingBird() throws IOException
    {
        BirdAsset[0] = RessourceLoader.LoadImageRessource("frame-1.png");
        BirdAsset[1] = RessourceLoader.LoadImageRessource("frame-2.png");
        BirdAsset[2] = RessourceLoader.LoadImageRessource("frame-3.png");
        BirdAsset[3] = RessourceLoader.LoadImageRessource("frame-4.png");
        BirdAsset[4] = RessourceLoader.LoadImageRessource("frame-5.png");
        BirdAsset[5] = RessourceLoader.LoadImageRessource("frame-6.png");
        BirdAsset[6] = RessourceLoader.LoadImageRessource("frame-7.png");
        BirdAsset[7] = RessourceLoader.LoadImageRessource("frame-8.png");

        EnableAutoRefresh(20);
    }

    @Override
    protected void Paint(Graphics g)
    {
        g.drawImage(BirdAsset[frame], 0, 0, null);
        frame++;
        if(frame >= 8) frame = 0;
    }

}
