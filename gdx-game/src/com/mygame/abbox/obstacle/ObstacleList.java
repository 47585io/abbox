package com.mygame.abbox.obstacle;
import java.util.*;
import com.mygame.abbox.share.graphics.*;

public class ObstacleList implements ObstacleContainer
{
	public ObstacleList(){
		mObstacles = new ArrayList<>();
	}
	
	/* 添加一个物体，但不包含重复的物体 */
	public void addObstacle(Obstacle obj)
	{
		if(!mObstacles.contains(obj)){
			mObstacles.add(obj);
		}
	}

	/* 如果有则移除指定的物体 */
	public void removeObstacle(Obstacle obj){
		mObstacles.remove(obj);
	}

	/* 获取与指定矩形区域重叠的所有物体 */
	public void getObstacles(int left, int top, int right, int bottom, Collection<Obstacle> ret)
	{
		int size = mObstacles.size();
		for(int i = 0; i < size; i++)
		{
			Obstacle obj = mObstacles.get(i);
			if(obj.getShape().getBounds().intersects(left, top, right, bottom)){
				ret.add(obj);
			}
		}
	}

	public int getObstacleCount(){
		return mObstacles.size();
	}

	public void clear(){
		mObstacles.clear();
	}

	public boolean contains(Obstacle obj){
		return mObstacles.contains(obj);
	}
	
	public ObstacleListIterator listIerator(){
		return new ObstacleListIterator();
	}
	
	private ArrayList<Obstacle> mObstacles;
	
	public class ObstacleListIterator
	{
		private int nowIndex;
		
		public ObstacleListIterator(){
			nowIndex = 0;
		}
		public void add(Obstacle o){
			addObstacle(o);
		}
		public void remove(Obstacle o)
		{
			int at = mObstacles.indexOf(o);
			if (at > -1){
				mObstacles.remove(at);
				if(at < nowIndex){
					nowIndex--;
				}
			}
		}
		public boolean hasNext(){
			return nowIndex < mObstacles.size();
		}
		public Obstacle next(){
			return mObstacles.get(nowIndex++);
		}
	}
}
