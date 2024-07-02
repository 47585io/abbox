package com.mygame.abbox.obstacle.shape;
import android.graphics.*;

public class CircleShape implements Shape
{
	public int circleX, circleY, circleRadius;
	
	public CircleShape(){}
	
	public CircleShape(int cx, int cy, int radius){
		setCircle(cx, cy, radius);
	}
	
	public void setCircle(int cx, int cy, int radius){
		circleX = cx;
		circleY = cy;
		circleRadius = radius;
	}
	public int getCircleX(){
		return circleX;
	}
	public int getCircleY(){
		return circleY;
	}
	public int getCircleRadius(){
		return circleRadius;
	}
	
	public void offset(int dx, int dy){
		circleX += dx;
		circleY += dy;
	}
	public boolean contains(int x, int y)
	{
		int dx = x - circleX;
		int dy = y - circleY;
		int distanceSquared = dx * dx + dy * dy;
		int radiusSquared = circleRadius * circleRadius;
		return distanceSquared <= radiusSquared;
	}
	public Rect getBounds(){
		return new Rect(circleX - circleRadius, circleY - circleRadius, circleX + circleRadius, circleY + circleRadius);
	}
}

