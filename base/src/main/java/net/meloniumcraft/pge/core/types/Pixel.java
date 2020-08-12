package net.meloniumcraft.pge.core.types;

import java.util.Objects;

public final class Pixel {
    public byte r;
    public byte g;
    public byte b;
    public byte a;
    
    public static final Pixel           GREY    = new Pixel((byte)192, (byte)192, (byte)192);
    public static final Pixel      DARK_GREY    = new Pixel((byte)128, (byte)128, (byte)128);
    public static final Pixel VERY_DARK_GREY    = new Pixel((byte) 64, (byte) 64, (byte) 64);
    public static final Pixel           RED     = new Pixel((byte)255, (byte)  0, (byte)  0);
    public static final Pixel      DARK_RED     = new Pixel((byte)128, (byte)  0, (byte)  0);
    public static final Pixel VERY_DARK_RED     = new Pixel((byte) 64, (byte)  0, (byte)  0);
    public static final Pixel           YELLOW  = new Pixel((byte)255, (byte)255, (byte)  0);
    public static final Pixel      DARK_YELLOW  = new Pixel((byte)128, (byte)128, (byte)  0);
    public static final Pixel VERY_DARK_YELLOW  = new Pixel((byte) 64, (byte) 64, (byte)  0);
    public static final Pixel           GREEN   = new Pixel((byte)  0, (byte)255, (byte)  0);
    public static final Pixel      DARK_GREEN   = new Pixel((byte)  0, (byte)128, (byte)  0);
    public static final Pixel VERY_DARK_GREEN   = new Pixel((byte)  0, (byte) 64, (byte)  0);
    public static final Pixel           CYAN    = new Pixel((byte)  0, (byte)255, (byte)255);
    public static final Pixel      DARK_CYAN    = new Pixel((byte)  0, (byte)128, (byte)128);
    public static final Pixel VERY_DARK_CYAN    = new Pixel((byte)  0, (byte) 64, (byte) 64);
    public static final Pixel           BLUE    = new Pixel((byte)  0, (byte)  0, (byte)255);
    public static final Pixel      DARK_BLUE    = new Pixel((byte)  0, (byte)  0, (byte)128);
    public static final Pixel VERY_DARK_BLUE    = new Pixel((byte)  0, (byte)  0, (byte) 64);
    public static final Pixel           MAGENTA = new Pixel((byte)255, (byte)  0, (byte)255);
    public static final Pixel      DARK_MAGENTA = new Pixel((byte)128, (byte)  0, (byte)128);
    public static final Pixel VERY_DARK_MAGENTA = new Pixel((byte) 64, (byte)  0, (byte) 64);
    public static final Pixel           WHITE   = new Pixel((byte)255, (byte)255, (byte)255);
    public static final Pixel           BLACK   = new Pixel((byte)  0, (byte)  0, (byte)  0);
    public static final Pixel           BLANK   = new Pixel((byte)  0, (byte)  0, (byte)  0, (byte)  0);
    
    public Pixel()                                      { this((byte)0, (byte)0, (byte)0); }
    public Pixel(float r,  float g,  float b)           { this((byte)(r * 255), (byte)(g * 255), (byte)(b * 255)); }
    public Pixel(float r,  float g,  float b,  float a) { this((byte)(r * 255), (byte)(g * 255), (byte)(b * 255), (byte)(a * 255)); }
    public Pixel(byte r, byte g, byte b)                { this(r, g, b, (byte)255); }
    public Pixel(byte r, byte g, byte b, byte a)        {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pixel pixel = (Pixel) o;
        return r == pixel.r &&
                g == pixel.g &&
                b == pixel.b &&
                a == pixel.a;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }
}
