package oled;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class OLEDDemo {

    public static void main(String[] args) throws InterruptedException
    {

        new OLEDDemo();
    }

    public OLEDDemo()
    {
        //DC - GPIO 24 pin 18
        //RST - GPIO 25 pin 22
        OLED oled = new OLED(GpioFactory.getInstance(), RaspiPin.GPIO_06, RaspiPin.GPIO_05, 1000000);

        System.out.println("[OLEDDemo]: Loading images");
        try {
            byte[] f1 = LoadImage("frame-1.png");
            byte[] f2 = LoadImage("frame-2.png");
            byte[] f3 = LoadImage("frame-3.png");
            byte[] f4 = LoadImage("frame-4.png");
            byte[] f5 = LoadImage("frame-5.png");
            byte[] f6 = LoadImage("frame-6.png");
            byte[] f7 = LoadImage("frame-7.png");
            byte[] f8 = LoadImage("frame-8.png");

            System.out.println("[OLEDDemo]: Finished");

            int f=0;
            while(true)
            {
                if(f==0) oled.WriteImage(f1);
                if(f==1) oled.WriteImage(f2);
                if(f==2) oled.WriteImage(f3);
                if(f==3) oled.WriteImage(f4);
                if(f==4) oled.WriteImage(f5);
                if(f==5) oled.WriteImage(f6);
                if(f==6) oled.WriteImage(f7);
                if(f==7) oled.WriteImage(f8);
                f++;
                if(f>=8) f=0;

                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[] LoadImage(String name) throws IOException
    {
        InputStream stream = this.getClass().getResourceAsStream("/"+name);
        BufferedImage frame = ImageIO.read(stream);
        byte[] image = new byte[128*128*3];
        for(int y=0;y<128;y++) {
            for (int x = 0; x < 128; x++) {

                Color c = new Color(frame.getRGB(x,y));
                byte r = (byte)((c.getBlue() >> 2) & 0xff);
                byte g = (byte)((c.getGreen() >> 2) & 0xff);
                byte b = (byte)((c.getRed() >> 2) & 0xff);
                image[y*128*3+x*3+0] = r;
                image[y*128*3+x*3+1] = g;
                image[y*128*3+x*3+2] = b;
            }
        }
        return image;
    }
}
