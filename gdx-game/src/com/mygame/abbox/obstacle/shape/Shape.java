package com.mygame.abbox.obstacle.shape;
import android.graphics.Rect;

public interface Shape
{
	public void offset(int dx, int dy)
	
	public boolean contains(int x, int y)
	
	public Rect getBounds()
}
