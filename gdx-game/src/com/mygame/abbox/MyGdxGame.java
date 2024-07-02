package com.mygame.abbox;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.viewport.*;
import com.mygame.abbox.scenes.*;

public class MyGdxGame implements ApplicationListener
{
	Scenes mScenes;

	@Override
	public void create()
	{
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		// 创建一个ExtendViewport，参数是视口的宽度和高度
		ExtendViewport viewport = new ExtendViewport(width, height);
		// 将视口设置在左上角
		viewport.setScreenBounds(0, height, width, -height);
		viewport.apply();
		
		mScenes = new Scenes(width* 2, height * 2, width, height);
		mScenes.init();
		mScenes.getShapeRender().getProjectionMatrix().setToOrtho2D(0, height, width, -height);
		Gdx.input.setInputProcessor(mScenes);
	}

	@Override
	public void render()
	{
		mScenes.update();
	    mScenes.draw();
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void resize(int width, int height)
	{
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}
}
