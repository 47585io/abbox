package com.mygame.abbox.obstacle;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.mygame.abbox.obstacle.shape.*;

public class Obstacle extends InputAdapter
{
	Shape mShape;
	ObstacleGroup mObstacleGroup;
	
	public ObstacleGroup getMyGroup(){
		return mObstacleGroup;
	}
	public Shape getShape(){
		return mShape;
	}
	protected void setShape(Shape shape){
		mShape = shape;
	}
	public void requestFocus(){
		if(mObstacleGroup != null){
			mObstacleGroup.mFocusObstacle = this;
		}
	}
	
	public void draw(ShapeRenderer render){}
	
	public void onCollision(Obstacle other){}
}
