package oled;

import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.io.IOException;
import java.util.Arrays;

public class OLED
{
    final byte WRITE_COMMAND = (byte)0x5C;

    SpiDevice SPI;

    GpioController GPIO;
    GpioPinDigitalOutput RST;
    GpioPinDigitalOutput DC;

    public OLED(GpioController gpioController, Pin pinRST, Pin pinDC, int speed)
    {
        System.out.println("[OLED]: Starting...");
        GPIO = gpioController;

        System.out.println("[OLED]: Initialize SPI");
        try {
            SPI = SpiFactory.getInstance(SpiChannel.CS0, 32*SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RST = GPIO.provisionDigitalOutputPin(pinRST, "OLED RST", PinState.LOW);
        RST.setShutdownOptions(true, PinState.HIGH);
        RST.high();
        DC = GPIO.provisionDigitalOutputPin(pinDC, "OLED DC", PinState.LOW);
        DC.setShutdownOptions(true, PinState.LOW);
        DC.low();

        Reset();

        Initialize();
    }

    private void Write(byte command)
    {
        Write(command,null);
    }
    private void Write(byte command, byte d1)
    {
        Write(command,new byte[] {d1});
    }
    private void Write(byte command, byte d1, byte d2)
    {
        Write(command,new byte[] {d1, d2});
    }
    private void Write(byte command, byte d1, byte d2, byte d3)
    {
        Write(command,new byte[] {d1, d2, d3});
    }
    private void Write(byte command, byte data[])
    {
        try
        {
            DC.low();
            SPI.write(new byte[] {command});

            if(data!=null)
            {
                DC.high();
                SPI.write(data);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void ClearScreen()
    {
        for(int i=0;i<128;i++)
        {
            byte[] data = new byte[3*128];
            WriteLine(data);
        }
    }

    public void WriteLine(byte[] data)
    {
        Write(WRITE_COMMAND, data);
    }
    public void WriteImage(byte[] data)
    {
        for(int i=0;i<128;i++)
            WriteLine(Arrays.copyOfRange(data,i*128*3,i*128*3+128*3));
    }

    public void Reset()
    {
        System.out.println("[OLED]: Reset");

        RST.low();
        try
        {
            Thread.sleep(500);
        } catch (InterruptedException e) { }
        RST.high();
        try
        {
            Thread.sleep(500);
        } catch (InterruptedException e) { }
    }

    public void Initialize()
    {
        System.out.println("[OLED]: Initialize");

        Write((byte)0xfd, (byte)0x12);	// command lock
        Write((byte)0xfd, (byte)0xB1);	// command lock

        Write((byte)0xae);	// display off
        Write((byte)0xa4); 	// Normal Display mode

        Write((byte)0x15, (byte)0, (byte)127);	//set column address
        Write((byte)0x75, (byte)0, (byte)127);	//set row address

        Write((byte)0xB3, (byte)0xF0);

        //Write((byte)0xCA, (byte)0x7F);

        Write((byte)0xa0, (byte)162);  //set re-map & data format

        Write((byte)0xa1, (byte)0x00);  //set display start line

        Write((byte)0xa2, (byte)0x00);  //set display offset

        //Write((byte)0xAB, (byte)0x01);

        //Write((byte)0xB4, (byte)0xA0, (byte)0xB5, (byte)0x55);
        //RGB
        Write((byte)0xC1,(byte)158,(byte)100,(byte)158); //Contrast

        //Write((byte)0xC7,(byte)0x0D); //Master contrast

        //Write((byte)0xB1,(byte)0x32);

        Write((byte)0xB2,(byte)0xA4,(byte)0x00,(byte)0x00);

        //Write((byte)0xBB,(byte)0x00);

        //Write((byte)0xB6,(byte)0x01);

        //Write((byte)0xBE,(byte)0x05);

        Write((byte)0xA6);


        Write((byte)0xaf);	 //display on

        //ClearScreen();
    }
}
