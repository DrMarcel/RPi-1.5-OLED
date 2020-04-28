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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OLEDDemo() throws OLED.OLEDException, InterruptedException, IOException
    {
        OLED oled = new OLED(GpioFactory.getInstance(), RaspiPin.GPIO_06, RaspiPin.GPIO_05, SpiChannel.CS0);
        FlyingBird flyingBird = new FlyingBird();

        oled.SetContent(flyingBird);

        for(int i=0; i<10000; i++)
        {
            oled.RepaintIfNeeded();
            Thread.sleep(1);
        }
    }
}
