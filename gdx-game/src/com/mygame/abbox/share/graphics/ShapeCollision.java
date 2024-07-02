package com.mygame.abbox.share.graphics;

public class ShapeCollision
{
	/* 检查两条线段是否相交 */
	public static boolean line_line(
        double x1, double y1, double x2, double y2,
        double x3, double y3, double x4, double y4)
    {
        double ax = x2 - x1;
        double ay = y2 - y1;
        double bx = x3 - x4;
        double by = y3 - y4;
        double dx = x1 - x3;
        double dy = y1 - y3;

        double denom = ay * bx - ax * by;
        if (denom == 0.0) {
            return false;
        }

        double r = (dx * by - dy * bx) / denom;
        double s = (ax * dy - ay * dx) / denom;

        return (r >= 0.0 && r <= 1.0 && s >= 0.0 && s <= 1.0);
    }
	
	/* 检查圆形是否和圆形碰撞 */
	public static boolean circle_circle(int cx1, int cy1, int radius1,
	                                    int cx2, int cy2, int radius2)
	{
		// 计算两个圆心之间的距离的平方(防止精度丢失)
		int dx = cx2 - cx1;
        int dy = cy2 - cy1;
        int distanceSquared = dx * dx + dy * dy;
		
		// 计算两个圆的半径之和的平方
        int radiusSumSquared = (radius1 + radius2) * (radius1 + radius2);
		
		// 如果圆心距离的平方小于等于半径之和的平方，则发生碰撞
        return distanceSquared <= radiusSumSquared;
	}
	
	/* 检查矩形是否和圆形碰撞 */
	public static boolean rect_circle(int left, int top, int right, int bottom, 
	                                  int cx, int cy, int radius)
	{
		// 计算在矩形上的与圆心距离最近的点
        int closestX = clamp(cx, left, right);
        int closestY = clamp(cy, top, bottom);

        // 计算圆心和最近点之间的距离(的平方)
        int distanceX = cx - closestX;
        int distanceY = cy - closestY;
        int distanceSquared = distanceX * distanceX + distanceY * distanceY;

        // 如果距离(的平方)小于半径(的平方)，则发生碰撞
        return distanceSquared < (radius * radius);
	}
	
	// 辅助方法：将值限制在指定范围内
    private static int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }
}
