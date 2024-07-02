package com.mygame.abbox.scenes.widget;

import android.graphics.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.mygame.abbox.obstacle.*;
import com.mygame.abbox.obstacle.shape.*;

public class Box extends Obstacle
{
	public Box(){
		this(0, 0, 0, 0);
	}
	public Box(int left, int top, int right, int bottom){
		setShape(new RectShape(left, top, right, bottom));
	}
	public RectShape getShape(){
		return (RectShape)super.getShape();
	}

	public void draw(ShapeRenderer render){
		Rect rect = getShape().getBounds();
		render.rect(rect.left, rect.top, rect.width(), rect.height());
	}
}

