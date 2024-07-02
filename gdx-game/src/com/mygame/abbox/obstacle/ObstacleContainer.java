package com.mygame.abbox.obstacle;
import java.util.Collection;

public interface ObstacleContainer
{
	public void addObstacle(Obstacle obj);

	public void removeObstacle(Obstacle obj);

	public void getObstacles(int left, int top, int right, int bottom, Collection<Obstacle> ret);

	public int getObstacleCount();

	public boolean contains(Obstacle obj);
	
	public void clear();
}

