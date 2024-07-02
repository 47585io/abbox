package com.mygame.abbox.scenes;
import com.badlogic.gdx.*;
import com.mygame.abbox.obstacle.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import java.util.*;
import com.mygame.abbox.scenes.widget.*;

public class Scenes extends InputAdapter
{
	public Scenes(int width, int height, int displayw, int displayh){
		mObstacleGroup = new ObstacleGroup(width, height, displayw, displayh);
		mShapeRenderer = new ShapeRenderer();
		mGameManger = new GameManger(this);
	}
	
	public void init(){
		mGameManger.init();
		mObstacleGroup.setCollisionCallback(mGameManger);
	}
	public ObstacleGroup getObstacleGroup(){
		return mObstacleGroup;
	}
	public ShapeRenderer getShapeRender(){
		return mShapeRenderer;
	}
	
	public void draw(){
		mShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		mObstacleGroup.draw(mShapeRenderer);
		mShapeRenderer.end();
	}
	public void update(){
		mObstacleGroup.update();
	}

	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		return mObstacleGroup.touchDown(screenX, screenY, pointer, button);
	}

	public boolean touchDragged(int screenX, int screenY, int pointer){
		return mObstacleGroup.touchDragged(screenX, screenY, pointer);
	}

	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		return mObstacleGroup.touchUp(screenX, screenY, pointer, button);
	}
	
	private ArrayList<Box> mBoxes;
	private ArrayList<Person> mPerson;
	
	private ShapeRenderer mShapeRenderer;
	private ObstacleGroup mObstacleGroup;
	private GameManger mGameManger;
}
