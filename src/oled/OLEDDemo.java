package oled;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.spi.SpiChannel;

import java.io.IOException;

import oled.OLED.*;


//RST - GPIO 25 pin 22    DC - GPIO 24 pin 18

/**
 * Waveshare 1.5" RGB OLED demo code. See GitHub page for more info.
 * <br> <br>
 * Contact: oled@mail.perske.eu
 * GitHub: https://github.com/DrMarcel/RPi-1.5-OLED
 * Licence: CC0
 * @author DrMarcel
 */
public class OLEDDemo
{
    public static void main(String[] args)
    {
        try
        {
            new OLEDDemo();
        }
        catch(OLEDException | IOException e)
        {
            e.printStackTrace();
        }
    }

    OLED oled;

    /**
     * Create new OLED demo and stay in infinite loop.
     * @throws OLEDException OLEDException
     * @throws IOException IOException
     */
    public OLEDDemo() throws OLEDException, IOException
    {
        //Initialize OLED
        oled = new OLED(GpioFactory.getInstance(), RaspiPin.GPIO_06, RaspiPin.GPIO_05, SpiChannel.CS0);

        //Initialize display content
        Clock        clock        = new Clock(Clock.Mode.Digital);
        ColorPalette colorPalette = new ColorPalette(ColorPalette.Mode.Bars);
        FlyingBird   flyingBird   = new FlyingBird();

        //noinspection InfiniteLoopStatement
        while(true)
        {
            colorPalette.SetMode(ColorPalette.Mode.Bars);
            oled.SetContent(colorPalette);
            Sleep(3000);

            clock.SetMode(Clock.Mode.Digital);
            oled.SetContent(clock);
            Sleep(10000);

            colorPalette.SetMode(ColorPalette.Mode.Smooth);
            oled.SetContent(colorPalette);
            Sleep(3000);

            clock.SetMode(Clock.Mode.Analog);
            oled.SetContent(clock);
            Sleep(10000);

            oled.SetContent(flyingBird);
            Sleep(5000);
        }
    }

    /**
     * Sleep while checking for display updates.
     * @param millis Sleep time in milliseconds
     */
    private void Sleep(int millis)
    {
        for(int i = 0; i < millis; i++)
        {
            oled.RepaintIfNeeded();
            //noinspection CatchMayIgnoreException
            try
            {
                Thread.sleep(1);
            }
            catch(InterruptedException e)
            {
            }
        }
    }
}
