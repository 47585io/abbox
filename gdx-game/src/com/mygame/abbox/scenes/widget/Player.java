package com.mygame.abbox.scenes.widget;

public class Player extends Person
{
	public Player(){
		super();
	}
	public Player(int left, int top, int right, int bottom){
		super(left, top, right, bottom);
	}
	
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return super.touchDown(screenX, screenY, pointer, button);
	}

	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return super.touchDragged(screenX, screenY, pointer);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return super.touchUp(screenX, screenY, pointer, button);
	}
}
