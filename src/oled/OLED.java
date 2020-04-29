package oled;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "SameParameterValue", "CanBeFinal"})
public class OLED
{
    private static final int WIDTH = 128, HEIGHT = 128;
    private static final int SPI_SPEED = 32000000;

    final byte WRITE_COMMAND = (byte) 0x5C;

    private SpiDevice SPI;

    private GpioController       GPIO;
    private GpioPinDigitalOutput RST;
    private GpioPinDigitalOutput DC;


    /**
     * Create new OLED Object
     *
     * @param gpioController Initialized GPIO controller
     * @param pinRST         Reset Pin
     * @param pinDC          DC Pin
     * @param channel        SPI channel
     * @throws OLEDException OLED Exception
     */
    public OLED(GpioController gpioController, Pin pinRST, Pin pinDC, SpiChannel channel) throws OLEDException
    {
        System.out.println("[OLED]: Starting...");
        GPIO = gpioController;

        System.out.println("[OLED]: Initialize SPI");
        try
        {
            SPI = SpiFactory.getInstance(channel, SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        }
        catch(IOException e)
        {
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
    private AtomicBoolean writeBusy     = new AtomicBoolean(false);

    private Thread   WriteDisplayBufferThread;
    private Runnable WriteDisplayBufferRunner = () ->
    {
        //Data structure [B,G,R,B,G,R,...]
        int[] data = new int[WIDTH * HEIGHT * 3];
        data = displayBuffer.getData().getPixels(0, 0, WIDTH, HEIGHT, data);

        //Shift display buffer data to 6:6:6 format
        byte[] bdata = new byte[WIDTH * HEIGHT * 3];
        for(int i = 0; i < WIDTH * HEIGHT * 3; i++)
            bdata[i] = (byte) (0xFF & (data[i] >> 2));

        final int parLines = 4; // send 4 lines at once - maximum payload length restricted by the SPI driver
        for(int i = 0; i < HEIGHT / parLines; i++)
        {
            //noinspection CatchMayIgnoreException
            try
            {
                Write(WRITE_COMMAND, Arrays.copyOfRange(bdata, i * 128 * 3 * parLines, (i + 1) * 128 * 3 * parLines));
            }
            catch(OLEDException e)
            {
            }
        }

        writeBusy.set(false);
    };

    /**
     * Check if current display content is invalidated and trigger Paint(...)-method.
     * Has to be called cyclic in the main Thread.
     */
    public void RepaintIfNeeded()
    {
        //Check if content to draw available and no write operation active
        if(content != null && !writeBusy.get())
        {
            //Calculate time since last repaint
            int dt = (int) (System.currentTimeMillis() - content.lastRepaintMillis);
            //if autorefresh is set and refresh time exceeds request repaint
            if(content.autorefresh && dt > 1000 / content.framerate) content.Invalidate();

            //Check if content needs repaint - may be triggered from the OLEDContent Object or by the autorefresh timer
            if(content.Invalidated())
            {
                content.lastRepaintMillis = System.currentTimeMillis();
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

    /**
     * Set display content
     *
     * @param c Display content to draw or null
     */
    public void SetContent(OLEDContent c)
    {
        if(c != null) c.Invalidate();
        content = c;
    }

    /**
     * Remove current display content
     */
    public void ClearScreen() throws OLEDException
    {
        SetContent(null);

        //Empty data
        final int parLines = 4; // send 4 lines at once - maximum payload length restricted by the SPI driver
        byte[]    lclear   = new byte[WIDTH * HEIGHT * 3 / parLines];
        for(int i = 0; i < HEIGHT / parLines; i++)
        {
            Write(WRITE_COMMAND, lclear);
        }
    }

    /**
     * Abstract display content class. To draw on the display extend this class and set the display content
     * with oled.SetContent(...). The Paint(...)-method will be called every time the display needs a redraw. This
     * can be triggered manually by calling the Invalidate()-method within the OLEDContent object or automatically
     * if auto refresh is enabled. To enable the auto refresh call EnableAutoRefresh(...).
     * <br> <br>
     * Important: The oled.RepaintIfNeeded()-method has to be called cyclic in the main Thread.
     */
    public static abstract class OLEDContent
    {
        private AtomicBoolean repaint           = new AtomicBoolean(false);
        private boolean       autorefresh       = false;
        private int           framerate         = 20;
        private long          lastRepaintMillis = 0;

        /**
         * Enable auto refresh to call Paint(...) automatically with the given framerate
         *
         * @param framerate Framerate
         */
        protected void EnableAutoRefresh(int framerate)
        {
            autorefresh    = true;
            this.framerate = framerate;
        }

        /**
         * Disable auto refresh. Paint needs to be triggered by calling the Invalidate()-method
         */
        protected void DisableAutoRefresh()
        {
            autorefresh = false;
        }

        /**
         * Trigger repaint
         */
        protected void Invalidate()
        {
            repaint.set(true);
        }

        /**
         * Check if the display content needs repaint
         *
         * @return true if invalidated
         */
        protected boolean Invalidated()
        {
            return repaint.get();
        }

        /**
         * Overwrite this method to draw on the display
         *
         * @param g Graphics Object of the display
         */
        protected abstract void Paint(Graphics g);
    }


    /**
     * Display hardware reset
     */
    private void Reset()
    {
        System.out.println("[OLED]: Reset");

        RST.low();
        //noinspection CatchMayIgnoreException
        try
        {
            Thread.sleep(500);
        }
        catch(InterruptedException e)
        {
        }
        RST.high();
        //noinspection CatchMayIgnoreException
        try
        {
            Thread.sleep(500);
        }
        catch(InterruptedException e)
        {
        }
    }

    /**
     * Initialize display parameters
     *
     * @throws OLEDException OLED Exception
     */
    private void Initialize() throws OLEDException
    {
        System.out.println("[OLED]: Initialize...");

        //Copied from Waveshare sample code
        Write((byte) 0xfd, (byte) 0x12);    // command lock
        Write((byte) 0xfd, (byte) 0xB1);    // command lock

        Write((byte) 0xae);    // Sleep on
        Write((byte) 0xa4);    // All pixel off

        Write((byte) 0x15, (byte) 0, (byte) 127);    //set column address 0-127
        Write((byte) 0x75, (byte) 0, (byte) 127);    //set row address 0-127

        Write((byte) 0xB3, (byte) 0xF0); //Set display frequency (b7-b4) and Prescaler (b3-b0)

        Write((byte) 0xa0, (byte) 0xA6);  //set re-map (mirror screen), data format (6:6:6), Reverse Color order RGB->BGR

        Write((byte) 0xa1, (byte) 0x00);  //set display start line

        Write((byte) 0xa2, (byte) 0x00);  //set display offset

        Write((byte) 0xC1, (byte) 158, (byte) 100, (byte) 158); //Contrast values, order R,G,B

        Write((byte) 0xC7, (byte) 0x0F); //Master contrast, maximum 0x0F

        Write((byte) 0xB2, (byte) 0xA4, (byte) 0x00, (byte) 0x00); //Enable display enhancement (whatever that does... sounds good)

        Write((byte) 0xA6); //Normal non-inverse display mode
        Write((byte) 0xaf);     //Sleep off

        ClearScreen();

        System.out.println("[OLED]: Ready");
    }

    /**
     * Send command
     *
     * @param command Command to send
     * @throws OLEDException OLED Exception
     */
    private void Write(byte command) throws OLEDException
    {
        Write(command, null);
    }

    /**
     * Send command with parameter
     *
     * @param command Command to send
     * @param d1      Command parameter
     * @throws OLEDException OLED Exception
     */
    private void Write(byte command, byte d1) throws OLEDException
    {
        Write(command, new byte[]{d1});
    }

    /**
     * Send command with multiple parameter
     *
     * @param command Command to send
     * @param d1      Command parameter
     * @param d2      Command parameter
     * @throws OLEDException OLED Exception
     */
    private void Write(byte command, byte d1, byte d2) throws OLEDException
    {
        Write(command, new byte[]{d1, d2});
    }

    /**
     * Send command with multiple parameter
     *
     * @param command Command to send
     * @param d1      Command parameter
     * @param d2      Command parameter
     * @param d3      Command parameter
     * @throws OLEDException OLED Exception
     */
    private void Write(byte command, byte d1, byte d2, byte d3) throws OLEDException
    {
        Write(command, new byte[]{d1, d2, d3});
    }

    /**
     * Send command with data array
     *
     * @param command Command to send
     * @param data    Data to send
     * @throws OLEDException OLED Exception
     */
    private void Write(byte command, byte[] data) throws OLEDException
    {
        try
        {
            DC.low();
            SPI.write(command);

            if(data != null)
            {
                DC.high();
                SPI.write(data);
            }
        }
        catch(IOException e)
        {
            throw new OLEDException("OLED IO Exception: " + e.getMessage());
        }
    }


    /**
     * Exception on OLED errors
     */
    public static class OLEDException extends Exception
    {
        public OLEDException(String message)
        {
            super(message);
        }
    }

}
