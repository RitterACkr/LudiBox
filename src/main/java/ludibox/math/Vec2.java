package ludibox.math;

public class Vec2 {
    public double x, y;

    public Vec2() {
        this.x = 0; this.y = 0;
    }
    public Vec2(double x, double y) {
        this.x = x; this.y = y;
    }
    public Vec2(Vec2 other) {
        this.x = other.x; this.y = other.y;
    }

    public void translate(double dx, double dy) {
        this.x += dx; this.y += dy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vec2 vec2 = (Vec2) o;
        return x == vec2.x && y == vec2.y;
    }
}
