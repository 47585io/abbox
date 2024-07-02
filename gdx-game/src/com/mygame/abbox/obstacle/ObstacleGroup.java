package com.mygame.abbox.obstacle;

import java.util.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.*;
import com.mygame.abbox.obstacle.shape.*;
import com.mygame.abbox.obstacle.ObstacleList.ObstacleListIterator;
import com.badlogic.gdx.*;
import android.graphics.*;
import com.mygame.abbox.share.input.*;

public class ObstacleGroup extends InputAdapter implements ObstacleContainer
{
	/* 创建指定大小的物体组 */
	public ObstacleGroup(int width, int height, int displayw, int displayh)
	{
		mContentWidth = width;
		mContentHeight = height;
		mDisplayWidth = displayw;
		mDisplayHeight = displayh;
		mScrollX = mScrollY = 0;
		
		mStaticObstacles = new ObstacleRegionTree();
		mDynamicObstacles = new ObstacleList();
	}

	/* 添加一个物体到组中 */
	public void addObstacle(Obstacle obj)
	{
		//动态物体添加到动态物体容器中，静态物体添加到静态物体容器中
		if(obj instanceof DynamicObject){
			if(onForeachDynamicObstacles){
				//在遍历时，使用iterator修改容器，同步下标
				modifyIterator.add(obj);
			}else{
				mDynamicObstacles.addObstacle(obj);
			}
		}
		else{
			mStaticObstacles.addObstacle(obj);
		}
		obj.mObstacleGroup = this; //并建立与该组的绑定
	}

	/* 从组中移除一个物体 */
	public void removeObstacle(Obstacle obj)
	{
		//动态物体从动态物体容器中移除，静态物体从静态物体容器中移除
		if(obj instanceof DynamicObject){
			if(onForeachDynamicObstacles){
				//在遍历时，使用iterator修改容器，同步下标
				modifyIterator.remove(obj);
			}else{
				mDynamicObstacles.removeObstacle(obj);
			}	
		}
		else{
			mStaticObstacles.removeObstacle(obj);
		}
		obj.mObstacleGroup = null; //并移除与该组的绑定
	}

	/* 获取与指定矩形区域重叠的所有物体，获取到指定的集合中 */
	public void getObstacles(int left, int top, int right, int bottom, Collection<Obstacle> ret){
		mStaticObstacles.getObstacles(left, top, right, bottom, ret);
		mDynamicObstacles.getObstacles(left, top, right, bottom, ret);
	}
	/* 获取与指定矩形区域重叠的所有物体，返回这些物体的列表 */
	public List<Obstacle> getObstacles(int left, int top, int right, int bottom)
	{
		if(getObstacleCount() == 0){
			return Collections.emptyList();
		}
		ArrayList<Obstacle> ret = new ArrayList<>();
		getObstacles(left, top, right, bottom, ret);
		return ret;
	}

	/* 获取组中的物体个数 */
	public int getObstacleCount(){
		return mStaticObstacles.getObstacleCount() + mDynamicObstacles.getObstacleCount();
	}
	/* 组中是否包含该物体 */
	public boolean contains(Obstacle obj){
		return mStaticObstacles.contains(obj) || mDynamicObstacles.contains(obj);
	}

	/* 清空组中的物体 */
	public void clear()
	{
		//获取所有物体并一个个移除，在removeObstacle中处理各种情况
		List<Obstacle> objs = getObstacles(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		for(Obstacle obj : objs){
			removeObstacle(obj);
		}
	}

	/* 设置物体碰撞回调，并启用碰撞检测 */
	public void setCollisionCallback(CollisionCallback callback){
		mCollisionCallback = callback;
	}
	
	public int getWidth(){
		return mContentWidth;
	}
	public int getHeight(){
		return mContentHeight;
	}
	public int getScrollX(){
		return mScrollX;
	}
	public int getScrollY(){
		return mScrollY;
	}
	public int getDisplayWidth(){
		return mDisplayWidth;
	}
	public int getDisplayHeight(){
		return mDisplayHeight;
	}

	/* 滚动场景的画布 */
	public void scrollTo(int sx, int sy){
		mScrollX = calcScroll(sx, mContentWidth, mDisplayWidth);
		mScrollY = calcScroll(sy, mContentHeight, mDisplayHeight);
	}
	public void scrollBy(int x, int y){
		scrollTo(mScrollX + x, mScrollY + y);
	}
	//计算滚动值，不允许滑出范围外
	private int calcScroll(int scroll, int content, int display){
		//如果显示宽度大于内容宽度，则只能滚动到0
		//如果滚动到的位置小于内容左边(0)，则只能滚动到内容左边(0)
		//如果要滚动到的位置加上显示宽度后，超出了内容的右边，则只能滚动到(content - display)
		return (display >= content || scroll <= 0) ? 0 :
			(scroll + display > content ? content - display : scroll);
	}

	/* 绘制可视范围内的所有物体 */
	public void draw(ShapeRenderer render)
	{
		//绘制前先设置平移矩阵，在之后绘制时将所有物体偏移到屏幕区域
		Matrix4 matrix = render.getTransformMatrix();
		matrix.translate(-mScrollX, -mScrollY, 0);
		render.setTransformMatrix(matrix);
		ArrayList<Obstacle> objs = obtain();
		//获取可视范围内的所有物体，一个个绘制在它们的位置上
		getObstacles(mScrollX, mScrollY, mScrollX + mDisplayWidth, mScrollY + mDisplayHeight, objs);
		int size = objs.size();
		for(int i = 0; i < size; i++){
			objs.get(i).draw(render);
		}
		recyle(objs);
		//绘制后将矩阵还原
		matrix.translate(mScrollX, mScrollY, 0);
		render.setTransformMatrix(matrix);
	}

	/* 更新场景内物体的位置，并检查物体的碰撞 */
	public void update()
	{
		onForeachDynamicObstacles = true; 
		//使用迭代器安全地遍历物体，在回调中允许修改容器
		modifyIterator = mDynamicObstacles.listIerator();
		while(modifyIterator.hasNext()){
			((DynamicObject)modifyIterator.next()).update();
		}
		if(mCollisionCallback != null){
			checkCollision();
		}
		onForeachDynamicObstacles = false;
		
		//跟踪拥有焦点的物体
		Rect bounds = mFocusObstacle.getShape().getBounds();
		int x = bounds.centerX();
		int y = bounds.centerY();
		scrollTo(x - mDisplayWidth / 2, y - mDisplayHeight / 2);
	}

	/* 检查物体的碰撞 */
	private void checkCollision()
	{
		ArrayList<Obstacle> tmpList = obtain();
		//遍历每个动态物体，检查它与场景内的物体碰撞
		modifyIterator = mDynamicObstacles.listIerator();
		while(modifyIterator.hasNext())
		{
			Obstacle obj = modifyIterator.next();
			Rect rect = obj.getShape().getBounds();
			//获取与该物体矩形区域重叠的所有物体
			getObstacles(rect.left, rect.top, rect.right, rect.bottom, tmpList);
			int size = tmpList.size();
			//将每对碰撞的物体传给mCollisionCallback操作，但不包含自己与自己的碰撞
			for(int j = 0; j < size; ++j){
				Obstacle other = tmpList.get(j);
				if(obj != other && hasCollision(obj, other)){
					mCollisionCallback.onCollision(obj, other);
				}
			}
			tmpList.clear();
		}
		recyle(tmpList);
	}
	
	/* 再次检查真实形状是否发生碰撞 */
	private boolean hasCollision(Obstacle o1, Obstacle o2)
	{
		Shape s1 = o1.getShape();
		Shape s2 = o2.getShape();
		if(s1 instanceof CircleShape && s2 instanceof CircleShape){
			return ShapeCollisionChecker.circle_circle((CircleShape)s1, (CircleShape)s2);
		}
		if(s1 instanceof CircleShape && s2 instanceof RectShape){
			return ShapeCollisionChecker.rect_circle((RectShape)s2, (CircleShape)s1);
		}
		if(s1 instanceof RectShape && s2 instanceof CircleShape){
			return ShapeCollisionChecker.rect_circle((RectShape)s1, (CircleShape)s2);
		}
		return true;
	}
	
	/* 获取临时物体容器 */
	private static ArrayList<Obstacle> obtain()
	{
		synchronized(sCachedBuffer)
		{
			for(int i = sCachedBuffer.length - 1; i >= 0; --i){
				ArrayList<Obstacle> buffer = sCachedBuffer[i];
				if(buffer != null){
					sCachedBuffer[i] = null;
					return buffer;
				}
			}
		}
		return new ArrayList<Obstacle>();
	}
	/* 回收临时物体容器 */
	private static void recyle(ArrayList<Obstacle> buffer)
	{
		buffer.clear();
		synchronized(sCachedBuffer)
		{
			for(int i = sCachedBuffer.length - 1; i >= 0; --i){
				if(sCachedBuffer[i] == null){
					sCachedBuffer[i] = buffer;
					break;
				}
			}
		}
	}

	private int mContentWidth, mContentHeight;    //内容大小
	private int mDisplayWidth, mDisplayHeight;    //显示区域
	private int mScrollX, mScrollY;               //内容滚到的位置

	private ObstacleRegionTree mStaticObstacles;  //位置静态不变的物体用树存储，提升获取效率
	private ObstacleList mDynamicObstacles;       //位置动态改变的物体用列表存储，不需频繁调整

	private boolean onForeachDynamicObstacles;    //是否正在遍历DynamicObstacles物体容器
	private ObstacleListIterator modifyIterator;  //在遍历DynamicObstacles时修改要用iterator，在同步数据的同时安全遍历
	
	private CollisionCallback mCollisionCallback; //碰撞检测回调器
	private static final ArrayList<Obstacle>[] sCachedBuffer = new ArrayList[6]; //存储临时物体容器
	
	private TouchTarget mTouchTarget;       //记录所有被触摸的物体
	private InputRecording mInputRecording; //记录触摸的下标或者键
	Obstacle mFocusObstacle;                //要跟踪的物体，键分配给的物体
	
	/* 处理触摸事件 */
	public boolean touchDown(int x, int y, int id, int b)
	{
		ArrayList<Obstacle> tmpList = obtain();
		//获取包含该点的所有物体，一个个遍历
		getObstacles(x, y, x, y, tmpList);
		int count = tmpList.size();
		for(int i = 0; i < count; ++i)
		{
			Obstacle obj = tmpList.get(i);
			if(obj.getShape().contains(x, y) && obj.touchDown(x, y, id, b)){
				//如果物体的真实形状包含此点则将事件传给它，如果它消耗了事件，则记录它并返回true
				mTouchTarget = TouchTarget.addPointer(mTouchTarget, obj, id);
				recyle(tmpList);
				return true;
			}
		}
		recyle(tmpList);
		//没有物体被触摸或消耗事件，则自己消耗事件
		if(mInputRecording == null){
			mInputRecording = new InputRecording();
		}
		if(mFocusObstacle != null){
			//如果正在跟踪焦点物体，则不滚动
			return false;
		}
		mInputRecording.touchDown(x, y, id);
		return true;
	}
	
	public boolean touchUp(int x, int y, int id, int p4)
	{
		//从记录的被触摸物体中寻找一个消耗该手指事件的物体
		InputProcessor input = TouchTarget.findInputById(mTouchTarget, id);
		if(input != null){
			//如果找到了则传递事件，不管消不消耗，在touchUp时必须移除该物体的该手指
			boolean consume = input.touchUp(x, y, id, p4);
			TouchTarget.removePointer(mTouchTarget, id);
			return consume;
		}
		//没有物体消耗事件，则看自己能不能消耗事件
		if(id == mInputRecording.pointer){
			//如果在touchDown时记录了手指，则继续消耗该手指的事件滚动自己
			mInputRecording.setNowPos(x, y);
			int dx = x - mInputRecording.lastX;
			int dy = y - mInputRecording.lastY;
			scrollBy(-dx, -dy);
			mInputRecording.touchUp();
			return true;
		}
		return false; //自己和物体都不消耗该手指的事件
	}
	
	public boolean touchDragged(int x, int y, int id)
	{
		//从记录的被触摸物体中寻找一个消耗该手指事件的物体
		InputProcessor input = TouchTarget.findInputById(mTouchTarget, id);
		if(input != null){
			//如果找到了则传递事件，继续消耗事件则返回true，不继续消耗事件则移除该物体的该手指
			if(input.touchDragged(x, y, id)){
				return true;
			}
			TouchTarget.removePointer(mTouchTarget, id);
			return false;  //不消耗该手指的事件了
		}
		//没有物体消耗事件，则看自己能不能消耗事件
		if(id == mInputRecording.pointer){
			//如果在touchDown时记录了手指，则继续消耗该手指的事件滚动自己
			mInputRecording.setNowPos(x, y);
			int dx = x - mInputRecording.lastX;
			int dy = y - mInputRecording.lastY;
			scrollBy(-dx, -dy);
			mInputRecording.setLastPos(x, y);
			return true;
		}
		return false; //自己和物体都不消耗该手指的事件
	}

	/* 处理键事件 */
	public boolean keyDown(int p1)
	{
		if(mFocusObstacle != null){
			return mFocusObstacle.keyDown(p1);
		}
		return false;
	}
	public boolean keyUp(int p1)
	{
		if(mFocusObstacle != null){
			return mFocusObstacle.keyUp(p1);
		}
		return false;
	}
	public boolean keyTyped(char p1)
	{
		if(mFocusObstacle != null){
			return mFocusObstacle.keyTyped(p1);
		}
		return false;
	}

	/* 鼠标和滚轮，手机上不会触发 */
	public boolean mouseMoved(int p1, int p2){
		return false;
	}
	public boolean scrolled(int p1){
		return false;
	}
	
	public static interface CollisionCallback
	{
		public void onCollision(Obstacle o1, Obstacle o2)
	}
	
	private static final class TouchTarget
	{
		private InputProcessor input;  //被触摸的Obstacle
		private int pointerIdBits;     //目标捕获的所有指针的 ID 的组合位掩码，用不同的位表示一个指针id
		private TouchTarget next;      //链表的下一个target

		public TouchTarget(InputProcessor input, int id, TouchTarget next)
		{
			this.input = input;
			this.pointerIdBits = id;
			this.next = next;
		}
		
		//为指定的物体添加一个手指，如果没有该物体则将它添加到链表中
		public static TouchTarget addPointer(final TouchTarget touchTarget, InputProcessor input, int id)
		{
			TouchTarget target = touchTarget;
			for(;target != null; target = target.next){
				if(target.input == input){
					target.pointerIdBits |= 1 << id;
					return touchTarget;
				}
			}
			return new TouchTarget(input, id, touchTarget);
		}
		
		//从链表中寻找与该手指绑定的物体，移除物体与该手指的绑定
		public static TouchTarget removePointer(final TouchTarget touchTarget, int id)
		{
			TouchTarget target = touchTarget;
			if(((target.pointerIdBits >> id) & 1) == 1)
			{
				target.pointerIdBits &= ~(1 << id);
				if(target.pointerIdBits == 0){
					target = target.next;
				}
				return target;
			}
			for(;target.next != null; target = target.next)
			{
				if(((target.next.pointerIdBits >> id) & 1) == 1){
					target.next.pointerIdBits &= ~(1 << id);
					if(target.next.pointerIdBits == 0){
						target.next = target.next.next;
					}
					break;
				}
			}
			return touchTarget;
		}
		
		//寻找与指定手指绑定的物体
		public static InputProcessor findInputById(TouchTarget target, int id)
		{
			for(;target != null; target = target.next){
				if(((target.pointerIdBits >> id) & 1) == 1){
					return target.input;
				}
			}
			return null;
		}
	}
}
