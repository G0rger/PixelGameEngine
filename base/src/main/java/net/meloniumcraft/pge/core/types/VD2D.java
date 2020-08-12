package net.meloniumcraft.pge.core.types;

import java.util.Objects;

public final class VD2D {
    public double x = 0;
    public double y = 0;
    public VD2D() {}
    public VD2D(double x, double y) {this.x = x; this.y = y;}
    public VD2D(VD2D v) {this.x = v.x; this.y = v.y;}
    
    public VD2D add        (VI2D other) { return new VD2D(this.x + other.x, this.y + other.y); }
    public VD2D subtract   (VI2D other) { return new VD2D(this.x - other.x, this.y - other.y); }
    public VD2D multiply   (int other)  { return new VD2D(this.x * other,   this.y * other);   }
    public VD2D multiplyRev(int other)  { return new VD2D(this.x * other,   this.y * other);   }
    public VD2D multiply   (VI2D other) { return new VD2D(this.x * other.x, this.y * other.y); }
    public VD2D divide     (int other)  { return new VD2D(this.x / other,   this.y / other);   }
    public VD2D divide     (VI2D other) { return new VD2D(this.x / other.x, this.y / other.y); }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VD2D vi2D = (VD2D) o;
        return x == vi2D.x &&
                y == vi2D.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
