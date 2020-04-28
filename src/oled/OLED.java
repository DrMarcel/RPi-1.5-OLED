package oled;

import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import jdk.jfr.Enabled;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class OLED
{
    private static final int WIDTH = 128, HEIGHT = 128;
    private static final int SPI_SPEED = 32000000;

    final byte WRITE_COMMAND = (byte)0x5C;

    private SpiDevice SPI;

    private GpioController GPIO;
    private GpioPinDigitalOutput RST;
    private GpioPinDigitalOutput DC;
;

    public OLED(GpioController gpioController, Pin pinRST, Pin pinDC, SpiChannel channel) throws OLEDException
    {
        System.out.println("[OLED]: Starting...");
        GPIO = gpioController;

        System.out.println("[OLED]: Initialize SPI");
        try {
            SPI = SpiFactory.getInstance(channel, SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        } catch (IOException e) {
            throw new OLEDException("Initialize SPI failed: " + e.getMessage());
        }

        System.out.println("[OLED]: Initialize GPIO pins");
        RST = GPIO.provisionDigitalOutputPin(pinRST, "OLED RST", PinState.HIGH);
        RST.setShutdownOptions(true, PinState.HIGH);
        DC = GPIO.provisionDigitalOutputPin(pinDC, "OLED DC", PinState.LOW);
        DC.setShutdownOptions(true, PinState.LOW);

        Reset();
        Initialize();
    }













    private BufferedImage displayBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private AtomicBoolean writeBusy = new AtomicBoolean(false);

    private Thread WriteDisplayBufferThread;
    private Runnable WriteDisplayBufferRunner = new Runnable()
    {
        @Override
        public void run() {
            //Data structure [B,G,R,B,G,R,...]
            int[] data = new int[WIDTH*HEIGHT*3];
            data = displayBuffer.getData().getPixels(0,0,WIDTH,HEIGHT,data);

            //Shift display buffer data to 6:6:6 format
            byte[] bdata = new byte[WIDTH*HEIGHT*3];
            for(int i=0; i<WIDTH*HEIGHT*3; i++)
                bdata[i] = (byte)(0xFF & (data[i]>>2));

            final int parLines = 4; // send 4 lines at once - maximum payload lenght restricted by the SPI driver
            for(int i=0; i<HEIGHT/parLines; i++) {
                try {
                    Write(WRITE_COMMAND, Arrays.copyOfRange(bdata,i*128*3*parLines, (i+1)*128*3*parLines));
                } catch (OLEDException e) {}
            }

            writeBusy.set(false);
        }
    };

    //Has to be called cyclic in main thread
    public void RepaintIfNeeded()
    {
        //Check if content to draw avaiable and no write operation active
        if(content!=null && !writeBusy.get())
        {
            //Calculate time since last repaint
            int dt = (int)(System.currentTimeMillis() - content.lastRepaintMilllis);
            //if autorefresh is set and refresh time exceeds request repaint
            if(content.autorefresh && dt > 1000/content.framerate) content.Invalidate();

            //Check if content needs repaint - may be triggered from the OLEDContent Object or by the autorefresh timer
            if(content.Invalidated())
            {
                content.lastRepaintMilllis = System.currentTimeMillis();
                content.Paint(displayBuffer.getGraphics());
                //Send new data to OLED
                writeBusy.set(true);
                WriteDisplayBufferThread = new Thread(WriteDisplayBufferRunner);
                WriteDisplayBufferThread.start();
                content.repaint.set(false);
            }
        }
    }








    private OLEDContent content;
    public void SetContent(OLEDContent c)
    {
        if(c!=null) c.Invalidate();
        content = c;
    }
    public void ClearScreen()
    {
        SetContent(null);
    }

    public static abstract class OLEDContent
    {

        private AtomicBoolean repaint = new AtomicBoolean(false);
        private boolean autorefresh = false;
        private int framerate = 20;
        private long lastRepaintMilllis = 0;

        protected void EnableAutoRefresh(int framerate)
        {
            autorefresh = true;
            this.framerate = framerate;
        }
        protected void DisableAutoRefresh()
        {
            autorefresh = false;
        }
        protected void Invalidate()
        {
            repaint.set(true);
        }
        protected boolean Invalidated()
        {
            return repaint.get();
        }
        protected abstract void Paint(Graphics g);
    }





















    private void Reset()
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
    private void Initialize() throws OLEDException
    {
        System.out.println("[OLED]: Initialize sequence");

        //Copied from Waveshare sample code
        Write((byte)0xfd, (byte)0x12);	// command lock
        Write((byte)0xfd, (byte)0xB1);	// command lock

        Write((byte)0xae);	// Sleep on
        Write((byte)0xa4); 	// All pixel off

        Write((byte)0x15, (byte)0, (byte)127);	//set column address 0-127
        Write((byte)0x75, (byte)0, (byte)127);	//set row address 0-127

        Write((byte)0xB3, (byte)0xF0); //Set diplay frequency (b7-b4) and Prescaler (b3-b0)

        Write((byte)0xa0, (byte)0xA6);  //set re-map (mirror screen), data format (6:6:6), Reverse Color order RGB->BGR

        Write((byte)0xa1, (byte)0x00);  //set display start line

        Write((byte)0xa2, (byte)0x00);  //set display offset

        Write((byte)0xC1,(byte)158,(byte)100,(byte)158); //Contrast values, order R,G,B

        Write((byte)0xC7,(byte)0x0F); //Master contrast, maximum 0x0F

        Write((byte)0xB2,(byte)0xA4,(byte)0x00,(byte)0x00); //Enable display enhancement (whatever that does... sounds good)

        Write((byte)0xA6); //Normal non-inverse display mode
        Write((byte)0xaf);	 //Sleep off

        ClearScreen();

        System.out.println("[OLED]: Ready");
    }
    private void Write(byte command) throws OLEDException
    {
        Write(command,null);
    }
    private void Write(byte command, byte d1) throws OLEDException
    {
        Write(command,new byte[] {d1});
    }
    private void Write(byte command, byte d1, byte d2) throws OLEDException
    {
        Write(command,new byte[] {d1, d2});
    }
    private void Write(byte command, byte d1, byte d2, byte d3) throws OLEDException
    {
        Write(command,new byte[] {d1, d2, d3});
    }
    private void Write(byte command, byte data[]) throws OLEDException
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
            throw new OLEDException("OLED IO Exception: "+e.getMessage());
        }
    }






    public class OLEDException extends Exception
    {
        public OLEDException(String message)
        {
            super(message);
        }
    }

}
