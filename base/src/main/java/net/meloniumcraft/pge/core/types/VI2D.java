package net.meloniumcraft.pge.core.types;

import java.util.Objects;

public final class VI2D {
    public int x = 0;
    public int y = 0;
    public VI2D() {}
    public VI2D(int x, int y) {this.x = x; this.y = y;}
    public VI2D(VI2D v) {this.x = v.x; this.y = v.y;}
    
    public VI2D add        (VI2D other) { return new VI2D(this.x + other.x, this.y + other.y); }
    public VI2D subtract   (VI2D other) { return new VI2D(this.x - other.x, this.y - other.y); }
    public VI2D multiply   (int other)  { return new VI2D(this.x * other,   this.y * other);   }
    public VI2D multiplyRev(int other)  { return new VI2D(this.x * other,   this.y * other);   }
    public VI2D multiply   (VI2D other) { return new VI2D(this.x * other.x, this.y * other.y); }
    public VI2D divide     (int other)  { return new VI2D(this.x / other,   this.y / other);   }
    public VI2D divide     (VI2D other) { return new VI2D(this.x / other.x, this.y / other.y); }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VI2D vi2D = (VI2D) o;
        return x == vi2D.x &&
                y == vi2D.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
