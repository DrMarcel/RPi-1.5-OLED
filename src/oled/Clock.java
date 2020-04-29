package oled;

import oled.OLED.*;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Clock extends OLEDContent{
    public enum Mode {Analog, Digital}
    private Mode mode;

    public Clock(Mode m)
    {
        DisableAutoRefresh();
        SetMode(m);
        EnableAutoRefresh(5);
    }

    public void SetMode(Mode m)
    {
        mode = m;
        Invalidate();
    }

    @Override
    protected void Paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.BLACK);
        g.fillRect(0,0,128,128);

        RenderingHints taa = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHints(taa);
        RenderingHints aa = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(aa);

        Date date = new Date();

        switch (mode)
        {
            case Digital:
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font( "SansSerif", Font.PLAIN, 26 ));
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                g2d.drawString(dateFormat.format(date), 4, 56);
                break;
            case Analog:
                g2d.setColor(Color.WHITE);
                double h, m, s;
                h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if(h>=12.0) h-=12.0;
                m = Calendar.getInstance().get(Calendar.MINUTE);
                s = Calendar.getInstance().get(Calendar.SECOND);
                h*=-2*Math.PI/12.0;
                m*=-2*Math.PI/60.0;
                s*=-2*Math.PI/60.0;

                g2d.drawLine(64,64, (int)(64-60*Math.sin(h)), (int)(64-40*Math.cos(h)));
                g2d.drawLine(64,64, (int)(64-60*Math.sin(m)), (int)(64-60*Math.cos(m)));
                g2d.drawLine(64,64, (int)(64-60*Math.sin(s)), (int)(64-55*Math.cos(s)));
                break;
        }
    }
}
