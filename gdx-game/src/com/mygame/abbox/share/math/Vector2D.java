package com.mygame.abbox.share.math;

public class Vector2D 
{
    public double x;
    public double y;

    // 构造函数
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
	
	public void set(double x, double y){
		this.x = x;
        this.y = y;
	}
	
	public void set(Vector2D other){
		this.x = other.x;
        this.y = other.y;
	}
	
	// 向量长度（模）
    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    // 返回标准化（单位长度）的向量
    public Vector2D normalize() 
	{
        double len = length();
        if (len != 0) {
            return new Vector2D(x / len, y / len);
        }
        return new Vector2D(0, 0); // 零向量
    }
	
	// 获得该向量的反方向向量
    public Vector2D negate() {
        return new Vector2D(-x, -y);
    }

	// 计算向量的法向量
    public Vector2D perpendicular() {
        return new Vector2D(-y, x);
    }

	// 向量加法
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }
	
	// 向量减法
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    // 向量数乘
    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    // 计算两个向量的点积
    public double dotProduct(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    // 计算两个向量的叉积
    public double crossProduct(Vector2D other) {
        return this.x * other.y - this.y * other.x;
    }
	
    // 检查两个向量是否相等
    public boolean equals(Vector2D other) {
        return this.x == other.x && this.y == other.y;
    }

    // 返回该向量的字符串表示
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
