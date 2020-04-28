package util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class RessourceLoader {

    public static BufferedImage LoadImageRessource(String name) throws IOException
    {
        InputStream stream = RessourceLoader.class.getResourceAsStream("/"+name);
        return ImageIO.read(stream);
    }

}
