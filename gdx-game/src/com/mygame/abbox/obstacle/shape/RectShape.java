package com.mygame.abbox.obstacle.shape;
import android.graphics.*;

public class RectShape implements Shape
{
	private Rect mBounds;
	
	public RectShape(){
		mBounds = new Rect();
	}
	public RectShape(int left, int top, int right, int bottom){
		mBounds = new Rect();
		mBounds.set(left, top, right, bottom);
	}
	
	public void setRect(int left, int top, int right, int bottom){
		mBounds.set(left, top, right, bottom);
	}
	public int getLeft(){
		return mBounds.left;
	}
	public int getTop(){
		return mBounds.top;
	}
	public int getRight(){
		return mBounds.right;
	}
	public int getBottom(){
		return mBounds.bottom;
	}
	
	public void offset(int dx, int dy){
		mBounds.offset(dx, dy);
	}
	public boolean contains(int x, int y){
		return mBounds.contains(x, y);
	}
	public Rect getBounds(){
		return new Rect(mBounds);
	}
}
