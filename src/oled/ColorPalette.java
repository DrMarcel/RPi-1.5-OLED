package oled;

import oled.OLED.*;

import java.awt.*;

public class ColorPalette extends OLEDContent
{
    public enum Mode {Bars, Smooth}
    private Mode mode;

    public ColorPalette(Mode m)
    {
        DisableAutoRefresh();
        SetMode(m);
    }

    public void SetMode(Mode m)
    {
        mode = m;
        Invalidate();
    }

    @Override
    protected void Paint(Graphics g)
    {
        switch (mode)
        {
            case Bars:
                for(int i=0; i<128; i++) {
                    g.setColor(new Color(i*2, 255-i*2, 0));
                    g.fillRect(i, 0, 1, 42);
                    g.setColor(new Color(0,i*2, 255-i*2));
                    g.fillRect(i, 42, 1, 42);
                    g.setColor(new Color(255-i*2,0,i*2));
                    g.fillRect(i, 84, 1, 44);
                }
                break;
            case Smooth:
                for(int y=0; y<128; y++) {
                    for(int x=0; x<128; x++) {
                        int d;

                        d=(int)(255-2*Math.sqrt(x*x+y*y));
                        if(d<0) d=0;
                        int r = d;

                        d=(int)(255-2*Math.sqrt((127-x)*(127-x)+y*y));
                        if(d<0) d=0;
                        int gr = d;

                        d=(int)(255-2*Math.sqrt(x*x+(127-y)*(127-y)));
                        if(d<0) d=0;
                        int b = d;

                        g.setColor(new Color(r, gr, b));
                        g.fillRect(x, y, 1, 1);
                    }
                }
                break;
        }
    }
}
