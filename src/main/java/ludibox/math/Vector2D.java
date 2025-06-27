package ludibox.math;

public class Vector2D {
    public double x, y;

    /* コンストラクタ */
    public Vector2D() {
        this(0, 0);
    }
    public Vector2D(double x, double y) {
        this.x = x; this.y = y;
    }

    // 加算
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    // 減算
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    // スカラー倍
    public Vector2D scale(double s) {
        return new Vector2D(this.x * s, this.y * s);
    }

    // スカラー除算
    public Vector2D divide(double s) {
        return new Vector2D(this.x / s, this.y / s);
    }

    // 長さ（大きさ）
    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    // 正規化（長さ1にする）
    public Vector2D normalize() {
        double len = length();
        if (len == 0) return new Vector2D(0, 0);
        return new Vector2D(x / len, y / len);
    }

    // 内積
    public double dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    // 外積（2Dではスカラー）
    public double cross(Vector2D other) {
        return this.x * other.y - this.y * other.x;
    }

    // クローン
    public Vector2D copy() {
        return new Vector2D(x, y);
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", x, y);
    }
}
