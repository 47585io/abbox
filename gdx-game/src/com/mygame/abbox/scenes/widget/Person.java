package com.mygame.abbox.scenes.widget;
import com.mygame.abbox.obstacle.*;
import com.mygame.abbox.obstacle.shape.*;

public class Person extends Obstacle
{
	private Attributes mAttributes;

	public Person(){
		this(0, 0, 0, 0);
	}
	public Person(int left, int top, int right, int bottom){
		setShape(new RectShape(left, top, right, bottom));
		mAttributes = new Attributes();
	}

	public Attributes getAttributes(){
		return mAttributes;
	}
	public RectShape getShape(){
		return (RectShape)super.getShape();
	}

	public static class Attributes
	{
		public int healthy;
		public int attack;
		public int defense;

		public int attck(int damage){
			return damage + attack;
		}
		public void hurt(int damage){
			healthy -= damage - defense;
		}
	}
}
