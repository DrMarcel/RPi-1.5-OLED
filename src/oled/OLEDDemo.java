package oled;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.spi.SpiChannel;

import java.awt.*;
import java.io.IOException;


//RST - GPIO 25 pin 22    DC - GPIO 24 pin 18
public class OLEDDemo {

    public static void main(String[] args)
    {
        try {
            new OLEDDemo();
        } catch (OLED.OLEDException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    OLED oled;

    public OLEDDemo() throws OLED.OLEDException, IOException
    {
        oled = new OLED(GpioFactory.getInstance(), RaspiPin.GPIO_06, RaspiPin.GPIO_05, SpiChannel.CS0);

        Clock clock = new Clock(Clock.Mode.Digital);
        ColorPalette colorPalette = new ColorPalette(ColorPalette.Mode.Bars);
        FlyingBird flyingBird = new FlyingBird();


        while(true) {
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

    private void Sleep(int millis)
    {
        for(int i=0; i<millis; i++)
        {
            oled.RepaintIfNeeded();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) { }
        }
    }
}
