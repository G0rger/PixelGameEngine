package net.meloniumcraft.pge.core.types;

import java.util.Objects;

public final class VF2D {
    public float x = 0;
    public float y = 0;
    public VF2D() {}
    public VF2D(float x, float y) {this.x = x; this.y = y;}
    public VF2D(VF2D v) {this.x = v.x; this.y = v.y;}
    
    public float mag() { return (float) Math.sqrt(x * x + y * y); }
    
    public VF2D add        (VF2D other)  { return new VF2D(this.x + other.x, this.y + other.y); }
    public VF2D subtract   (VF2D other)  { return new VF2D(this.x - other.x, this.y - other.y); }
    public VF2D multiply   (float other) { return new VF2D(this.x * other,   this.y * other);   }
    public VF2D multiplyRev(float other) { return new VF2D(this.x * other,   this.y * other);   }
    public VF2D multiply   (VF2D other)  { return new VF2D(this.x * other.x, this.y * other.y); }
    public VF2D divide     (float other) { return new VF2D(this.x / other,   this.y / other);   }
    public VF2D divide     (VF2D other)  { return new VF2D(this.x / other.x, this.y / other.y); }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VF2D vi2D = (VF2D) o;
        return x == vi2D.x &&
                y == vi2D.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
