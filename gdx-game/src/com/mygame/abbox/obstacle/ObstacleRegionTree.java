package com.mygame.abbox.obstacle;

import java.util.*;
import android.graphics.Rect;
import com.mygame.abbox.obstacle.Obstacle;
import com.mygame.abbox.share.graphics.RectUtils;


/* 区域树的简单实现，用于存储矩形，以及快速获取与指定查找范围重叠的所有矩形 */
public class ObstacleRegionTree implements ObstacleContainer
{
	/* 构建一颗空区域树 */
	public ObstacleRegionTree(){
		mObstacleCount = 0;
		mTree = new RTree();
	}

	/* 清空区域树的物体 */
	public void clear()
	{
		mObstacleCount = 0;
		if(mRectOfObstacle != null){
			mRectOfObstacle.clear();
		}
		mTree = new RTree();
	}

	/* 为指定的物体在区域树中分配一块矩形区域 */
	public void addObstacle(Obstacle obj)
	{
		if(mRectOfObstacle == null){
			mRectOfObstacle = new IdentityHashMap<>();
		}
		Rect self = obj.getShape().getBounds();
		Rect rect = mRectOfObstacle.get(obj);
		if(rect == null){
			//如果没有这个矩形物体就添加一个
			mObstacleCount++;
			rect = new Rect(self);
			mRectOfObstacle.put(obj, rect);
		}else{
			//否则重新设置物体的矩形范围
			mTree.delete(rect, obj);
			rect.set(self);
		}
		mTree.insert(rect, obj);
	}

	/* 从区域树中移除指定的物体 */
	public void removeObstacle(Obstacle obj)
	{
		if(mRectOfObstacle == null){
			return;
		} 
		Rect rect = mRectOfObstacle.remove(obj);
		if(rect != null){
			//如果存在此矩形物体则移除它
			mTree.delete(rect, obj);
			mObstacleCount--;
		}
	}

	/* 获取区域树中的物体，这些物体与指定的矩形查找范围重叠 */
	public void getObstacles(int left, int top, int right, int bottom, Collection<Obstacle> ret)
	{
		if(mObstacleCount > 0){
			//如果有，则收集树中指定范围内的物体
			mTree.getObjects(left, top, right, bottom, ret);
		}
	}
	
	/* 获取物体在区域树中的矩形区域，如果存在此物体则返回true */
	public boolean getObstacleBounds(Obstacle obj, Rect bounds)
	{
		if(mRectOfObstacle != null)
		{
			Rect self = mRectOfObstacle.get(obj);
			if(self != null){
				//如果存在此矩形物体则获取它的矩形区域
				bounds.set(self);
				return true;
			}
		}
		return false;
	}
	
	/* 获取区域树中物体的个数 */
	public int getObstacleCount(){
		return mObstacleCount;
	}

	/* 物体是否在区域树中 */
	public boolean contains(Obstacle obj){
		return mRectOfObstacle.get(obj) != null;
	}
	
	/* 获取区域树的大小，这个矩形大小刚好可以包围区域树中的所有物体 */
	public void containerBounds(Rect bounds){
		mTree.treeBounds(bounds);
	}
	
	private RTree mTree;  //区域树
	private int mObstacleCount; //物体个数
	private IdentityHashMap<Obstacle, Rect> mRectOfObstacle; //每个物体在树中的矩形区域

	
	/* 区域树中的节点分为内部节点(RTDirNode)，以及叶子节点(RTDataNode)，每一个节点都存储了一组的矩形条目
	 * 只有叶子节点中存储的是真正的物体以及它们的矩形区域，内部节点存储的是它的子节点和包围子节点的外包矩形
	 * 每一层的内部节点都将下一层节点的矩形分组，这样在查找时可以层层快速舍弃不在查找范围内的那些组
	 * 如果查找范围真的命中了某些矩形，那么查找时需要走到最后一层的叶子来获取它，为了更快速地获取
	 * 我们在插入时会维护树的平衡，将其均匀地分配给每个节点，使树的层次尽可能地少。在删除时，也会压缩树的层数
	 */
	private static class RTree
	{
		private RTNode mRoot; //根节点
		private Rect mBounds; //根节点的外包矩形
		public static final int NODE_CAPACITY = 4;     //每个节点的最大条目数
		public static final int MIN_NODE_CAPACITY = 2; //每个节点的最小条目数

		public RTree(){
			mRoot = new RTDataNode(this, null);
			mBounds = new Rect();
		}

		/* 为树设置一个根节点 */
		public void setRoot(RTNode root){
			mRoot = root;
		}
		/* 获取树的外包矩形 */
		public void treeBounds(Rect bounds){
			bounds.set(mBounds);
		}

		/* 为指定的物体在树中分配一块矩形区域 
		 *
		 *  1、首先找到与它最接近的节点(组)，并且层层这样寻找下去，直至叶子节点
		 *
		 *  2、将它插入叶子节点中，如果此时该节点矩形数超出了NODE_CAPACITY
		 *     那么将这个节点分裂为两个节点，并加入父节点中
		 *     如果父节点的矩形数也超出了NODE_CAPACIT，同样也需要这样做
		 *
		 *  3、分裂的同时重新计算被修改节点的外包矩形
		 *     将这种变化层层向上传递，重新计算插入路径上的每个节点的外包矩形
		 *
		 */
		public void insert(Rect rect, Object obj)
		{
			RTDataNode leaf = mRoot.chooseLeaf(rect);
			leaf.insert(rect, obj);
			mBounds = mRoot.getNodeRect();
		}

		/* 在树中移除指定的矩形，并移除矩形对应的物体 
		 * 
		 *  1、首先找到包含它的节点，并且层层这样寻找下去，直至叶子节点
		 *
		 *  2、从叶子节点中移除这个矩形，如果叶子节点中的矩形数太少
		 *     需要删除这个叶子节点，并记录节点
		 *     如果父节点的矩形数也太少，同样也需要这样做
		 *
		 *  3、层层向上删除并重新计算被修改节点的外包矩形，压缩树的高度
		 *     最后将删除节点中剩余的矩形重新插入到树中
		 */
		public void delete(Rect rect, Object obj)
		{
			RTDataNode leaf = mRoot.findLeaf(rect, obj);
			if(leaf != null){
				leaf.delete(rect, obj);
				mBounds = mRoot.getNodeRect();
			}
		}

		/* 从给定的结点开始遍历之下所有的叶子结点 */
		public void foreachLeaves(RTNode node, Collection<RTDataNode> nodes)
		{
			if(node.level == 0){
				nodes.add((RTDataNode)node);
				return;
			}
			RTDirNode dir = (RTDirNode) node;
			for(int i = 0; i < dir.usedSpace; ++i){
				foreachLeaves(dir.children[i], nodes);
			}
		}

		/* 获取所有与指定范围重叠的物体 */
		public void getObjects(int left, int top, int right, int bottom, Collection objs)
		{
			if(mBounds.intersects(left, top, right, bottom)){
				getObjects(left, top, right, bottom, mRoot, objs);
			}
		}
		/* 从给定的节点开始获取所有与指定范围重叠的物体 */
		private void getObjects(int left, int top, int right, int bottom, RTNode node, Collection objs)
		{
			if(node.level == 0){
				//叶子节点则直接遍历它的条目，并获取与范围重叠的条目
				RTDataNode leaf = (RTDataNode) node;
				for(int i = 0; i < leaf.usedSpace; ++i){
					if(leaf.bounds[i].intersects(left, top, right, bottom)){
						objs.add(leaf.dates[i]);
					}
				}
				return;
			}
			//内部节点将遍历它的所有子节点
			RTDirNode dir = (RTDirNode) node;
			for(int i = 0; i < dir.usedSpace; ++i){
				if(dir.bounds[i].intersects(left, top, right, bottom)){
					//传递给函数的节点应先判断是否与查找范围重叠，因为它只判断自己的孩子而不是自己
					getObjects(left, top, right, bottom, dir.children[i], objs);
				}
			}
		}
	}
	
	/* 树的节点，此类申明了一些基础的内容 */
	private static abstract class RTNode
	{
		protected RTree rTree;     //节点所在的树
		protected RTNode parent;   //父节点(根节点的父节点为null)
		protected int level;       //节点所在的层(叶子节点在第0层)
		protected int insertIndex; //记录刚刚插入时，选择的子节点在数组中的下标
		protected int deleteIndex; //记录刚刚删除时，找到的子节点在数组中的下标
		protected int usedSpace;   //结点已用的空间(数组的元素个数)
		protected Rect[] bounds;   //存储子节点的外包矩形(对于叶子节点即是实际对象)

		public RTNode(RTree rtee, RTNode parent, int level){
			this.rTree = rtee;
			this.parent = parent;
			this.level = level;
			usedSpace = 0;
			bounds = new Rect[RTree.NODE_CAPACITY + 1];
		}

		/* 协议1: 子类的任意数组，其大小必须为NODE_CAPACIT + 1
		 * 协议2: 子类对于数组的操作，应与此处一致
		 * 协议3: 子类在所有数组操作后，应手动修改usedSpace的值
		 */

		/* 在数组末尾加入一个矩形条目，obj为保留参数，供子类使用 */
		protected void addEntry(Rect rect, Object obj){
			bounds[usedSpace] = rect;
		}
		/* 将末尾与i位置的条目置换，将末尾置为null */
		protected void removeEntry(int i){
			bounds[i] = bounds[usedSpace - 1];
			bounds[usedSpace - 1] = null;
		}

		/* 叶子节点中刚刚删除了一个条目，如果叶子节点中的剩余条目数太少
		 * 需要删除这个叶子节点，同时收集该结点，之后将收集的节点中剩余的条目重插入到其他结点中
		 * 如果有必要，要逐级向上进行这种删除，调整向上传递的路径上的所有外包矩形，使其尽可能小，直到根节点
		 */
		protected void condenseTree(List<RTDataNode> list)
		{
			if (parent == null){
				//如果当前节点是根节点，且不是叶子，并且只有一个孩子了
				if(level != 0 && usedSpace == 1){
					//那么将它的孩子设为根节点，并释放原根节点
					RTDirNode root = (RTDirNode) this;
					RTNode child = root.children[0];
					root.removeEntry(0);
					rTree.setRoot(child);
				}
			}
			else{
				//当前节点不是根节点，处理自己后继续向上传递
				RTNode parent = this.parent;
				int min = RTree.MIN_NODE_CAPACITY; 
				if (usedSpace < min){
					//如果当前节点的条目小于MIN_NODE_CAPACITY，则将它从父节点中移除
					//并且将当前节点记录在list中，等待之后将条目重新插入到树中
					parent.removeEntry(parent.deleteIndex);
					if(level == 0){
						//叶子节点就直接加入
						list.add((RTDataNode)this);
					}else{
						//内部节点需要遍历它的叶子节点
						rTree.foreachLeaves(this, list);
					}
				}
				else{
					//否则，重新测量当前节点的外包矩形，并设置到父节点中
					parent.bounds[parent.deleteIndex] = getNodeRect();
				}
				parent.condenseTree(list);
			}
		}

		/* 将这些矩形分为两组，尽可能地将离得近的那一堆矩形归为一组，另一堆归为另一组 
		 * count必须 >= 2
		 * 返回两组矩形在rects中的下标
		 */
		protected static int[][] quadraticSplit(Rect[] rects, int count)
		{
			int[] mask = new int[count]; //标记访问的条目
			for(int i = 0; i < count; ++i){
				mask[i] = i;
			}

			int rem = count; //剩余的未分配的矩形个数
			int[] group1 = new int[count]; //记录分配至第一个组的条目在rects的索引
			int[] group2 = new int[count]; //记录分配至第二个组的条目在rects的索引
			int i1 = 0, i2 = 0; //记录每个组的条目个数

			//选择两个离得最远的矩形，让它们各成一组
			int[] seed = pickSeeds(rects, count);
			group1[i1++] = seed[0];
			group2[i2++] = seed[1];
			rem -= 2;
			mask[group1[0]] = -1;
			mask[group2[0]] = -1;

			Rect mbr1 = new Rect(rects[group1[0]]); //第一个组的外包矩形
			Rect mbr2 = new Rect(rects[group2[0]]); //第二个组的外包矩形
			//将剩余矩形一个个分配到两个组中
			while(rem > 0)
			{
				int diff = Integer.MIN_VALUE;
				int area1 = RectUtils.getArea(mbr1);
				int area2 = RectUtils.getArea(mbr2);
				int areaDiff1 = 0, areaDiff2 = 0;
				int sel = -1;
				//遍历剩余的所有矩形，找出下一个进行分配的条目
				for(int i = 0; i < count; ++i)
				{
					if(mask[i] != -1)
					{
						Rect a = RectUtils.getUnionRect(mbr1, rects[i]);
						int diff1 = RectUtils.getArea(a) - area1;

						Rect b = RectUtils.getUnionRect(mbr2, rects[i]);
						int diff2 = RectUtils.getArea(b) - area2;

						//若分别给两个组加入此矩形，为了包含它，发现一个组面积增加的较少，另一个组增加较多
						//两个组面积的增量的差越大，说明此矩形应该靠近一个组而远离另一个组
						//我们选择出一个使得两个组面积的增量的差最大的矩形，并把这个矩形分配给增量小(靠近)的那个组
						//尽可能地将离得近的那一堆矩形归为一组，另一堆归为另一组(见后面的代码)		

						int diffDiv = Math.abs(diff1 - diff2);
						if (diffDiv > diff){
							areaDiff1 = diff1;
							areaDiff2 = diff2;
							diff = diffDiv;
							sel = i;
						}
					}
				}

				int k1 = i1;
				//先比较面积增量
				if (areaDiff1 < areaDiff2){
					group1[i1++] = sel;
				}else if (areaDiff1 > areaDiff2){
					group2[i2++] = sel;
				}//再比较自身面积
				else if (area1 < area2){
					group1[i1++] = sel;
				}else if (area1 > area2){
					group2[i2++] = sel;
				}//最后比较条目个数
				else if (i1 < i2){
					group1[i1++] = sel;
				}else if (i1 > i2){
					group2[i2++] = sel;
				}
				else{
					group1[i1++] = sel;
				}

				//判断矩形被加入哪个组，并扩展那个组的矩形
				if(k1 != i1){
					mbr1.union(rects[sel]);
				}else{
					mbr2.union(rects[sel]);
				}
				mask[sel] = -1;
				rem--;
			}

			int[] ret1 = Arrays.copyOf(group1, i1);
			int[] ret2 = Arrays.copyOf(group2, i2);
			return new int[][]{ret1, ret2};
		}

		/* 对每一对条目E1和E2，计算包围它们的Rect
		 * 并计算减去E1与E2后Rect剩余的空间，记录剩余空间最大的那一对条目
		 * 返回两个条目如果放在一起会有最多的剩余空间的条目索引，这表示它们离得最远
		 */
		private static int[] pickSeeds(Rect[] rects, int count)
		{
			int inefficiency = Integer.MIN_VALUE;
			int i1 = 0, i2 = 0;
			for(int i = 0; i < count; ++i){
				int iArea = RectUtils.getArea(rects[i]);
				for(int j = i + 1; j < count; ++j)
				{
					Rect unionRect = RectUtils.getUnionRect(rects[i], rects[j]);
					int diff = RectUtils.getArea(unionRect) - iArea - RectUtils.getArea(rects[j]);
					if(diff > inefficiency){
						inefficiency = diff;
						i1 = i;
						i2 = j;
					}
				}
			}
			return new int[]{i1, i2};
		}

		/* 返回包含结点中所有条目的最小Rect */
		public Rect getNodeRect(){
			return RectUtils.getUnionRect(bounds, usedSpace);
		}

		/* 插入一个矩形时，为此矩形选择一个合适的叶子节点 */
		protected abstract RTDataNode chooseLeaf(Rect rect);

		/* 删除一个矩形时，寻找它处于的叶子节点，如果有两个物体的矩形相同，则寻找指定物体所在的叶子节点 */
		protected abstract RTDataNode findLeaf(Rect rect, Object tag);
	}
	
	/* 内部节点 */
	private static class RTDirNode extends RTNode
	{
		protected RTNode[] children; //子节点数组，每一个外包矩形对应的子节点

		public RTDirNode(RTree rtee, RTNode parent, int level){
			super(rtee, parent, level);
			children = new RTNode[RTree.NODE_CAPACITY + 1];
		}
		/* 对于RTDirNode，它添加一个矩形条目，且条目对应一个子节点 */
		protected void addEntry(Rect rect, Object obj){
			super.addEntry(rect, obj);
			RTNode node = (RTNode) obj;
			node.parent = this;
			children[usedSpace++] = node;
		}
		/* 对于RTDirNode，它移除一个矩形条目，且移除条目对应的子节点 */
		protected void removeEntry(int i){
			super.removeEntry(i);
			children[i].parent = null;
			children[i] = children[usedSpace - 1];
			children[--usedSpace] = null;
		}

		/* 为即将插入的矩形逐层选择子节点，直至叶子节点 */
		protected RTDataNode chooseLeaf(Rect rect)
		{
			int index;
			if(level == 1){
				//如果孩子是叶子节点，获得最小重叠面积的叶子节点的索引
				//因为叶子节点的面积增量其实不是很重要，本身就是最后一层了
				//另一方面，这可以让叶子节点中的矩形不会特别拥挤，方便之后分裂两个节点(不会重叠太多)
				index = findLeastOverlap(rect);
			}else{
				//对于内部节点，获得面积增量最小的子节点的索引
				//这表示矩形离该组更近
				index = findLeastEnlargement(rect);
			}
			insertIndex = index; //记录插入路径
			return children[index].chooseLeaf(rect);
		}

		/* 返回与矩形重叠面积最小的孩子的索引
		 * 如果重叠面积相等则选择加入此Rect后面积增量更小的
		 * 如果面积增量还相等则选择自身面积更小的
		 */
		private int findLeastOverlap(Rect rect)
		{
			int overlap = Integer.MAX_VALUE;
			int sel = -1;
			//遍历每个孩子的所有矩形条目，记录与rect重叠面积最小的孩子
			for (int i = 0; i < usedSpace; i++) 
			{
				RTNode child = children[i];
				int ol = 0;
				//将传入矩形与孩子中各个矩形重叠的面积累加到ol中，得到重叠的总面积
				for(int j = 0; j < child.usedSpace; ++j){
					ol += RectUtils.intersectArea(rect, child.bounds[j]);
				}

				if(ol < overlap){
					//记录重叠面积最小的孩子
					overlap = ol;
					sel = i;
				}
				else if(ol == overlap){
					//如果重叠面积相等则选择(i和sel两者中)加入此Rect后面积增量更小的
					Rect a = RectUtils.getUnionRect(bounds[i], rect);
					int diff1 = RectUtils.getArea(a) - RectUtils.getArea(bounds[i]);

					Rect b = RectUtils.getUnionRect(bounds[sel], rect);
					int diff2 = RectUtils.getArea(b) - RectUtils.getArea(bounds[sel]);

					if(diff1 == diff2){
						//如果面积增量还相等则选择自身面积更小的
						sel = RectUtils.getArea(bounds[i]) < RectUtils.getArea(bounds[sel]) ? i : sel;
					}else{
						sel = diff1 < diff2 ? i : sel;
					}
				}
			}
			return sel;
		}

		/* 返回加入此Rect后面积增量更小的孩子的下标
		 * 如果面积增量相等则选择自身面积更小的
		 */
		private int findLeastEnlargement(Rect rect)
		{
			int diff = Integer.MAX_VALUE;
			int sel = -1;
			for (int i = 0; i < usedSpace; ++i)
			{
				Rect union = RectUtils.getUnionRect(bounds[i], rect);
				int enlargement = RectUtils.getArea(union) - RectUtils.getArea(bounds[i]);
				if (enlargement < diff){
					diff = enlargement;
					sel = i;
				}
				else if(enlargement == diff){
					sel = RectUtils.getArea(bounds[i]) < RectUtils.getArea(bounds[sel]) ? i : sel;
				}
			}
			return sel;
		}

		/* 分裂的节点要插入到内部节点中 */
		protected void insert(RTNode node)
		{
			addEntry(node.getNodeRect(), node);
			if(usedSpace <= RTree.NODE_CAPACITY){
				//如果当前节点的容量足够，则不用分裂节点，直接调整树
				if(parent != null){
					((RTDirNode)parent).adjustTree(this, null);
				}
			}
			else{//节点容量不足，分裂节点，并将两个节点添加到父节点中
				RTDirNode[] nodes = spiltIndex();
				RTDirNode n1 = nodes[0];
				RTDirNode n2 = nodes[1];
				if(parent == null){
					//当前节点是根节点，就创建一个新的根节点，并将它们添加到新根节点中
					//此时整颗树的层数加1，根节点是最高的那一层
					RTDirNode root = new RTDirNode(rTree, null, level + 1);
					root.addEntry(n1.getNodeRect(), n1);
					root.addEntry(n2.getNodeRect(), n2);
					rTree.setRoot(root);
				}
				else{
					((RTDirNode)parent).adjustTree(n1, n2);
				}
			}
		}

		/* 插入新的Rect后从插入的节点开始向上调整RTree，直到根节点
		 * @param node1 引起需要调整的孩子结点
		 * @param node2 分裂的结点，若未分裂则为null
		 */
		public void adjustTree(RTNode node1, RTNode node2)
		{
			//先要找到指向原来旧的结点（即未分裂前）的条目的索引
			//先用node1覆盖原来的结点，并重新计算它的外包矩形
			children[insertIndex] = node1;
			bounds[insertIndex] = node1.getNodeRect();

			if(node2 != null){
				//插入新的结点，待会在insert中再调用adjustTree
				insert(node2);
			}
			else if(parent != null){
				//否则，如果还没到达根节点，就直接调整
				((RTDirNode)parent).adjustTree(this, null);
			}
		}

		/* 将当前节点的内容分裂为两个 */
		private RTDirNode[] spiltIndex()
		{
			//分裂的节点与当前节点在同一层，且即将把它们加入我的父节点中
			RTDirNode node1 = new RTDirNode(rTree, parent, level);
			RTDirNode node2 = new RTDirNode(rTree, parent, level);
			int[][] group = quadraticSplit(bounds, usedSpace);
			int[] group1 = group[0];
			int[] group2 = group[1];

			//将矩形条目归为两组后，分别分配给两个节点
			for(int i = 0; i < group1.length; ++i){
				int index = group1[i];
				node1.addEntry(bounds[index], children[index]);
			}
			for(int i = 0; i < group2.length; ++i){
				int index = group2[i];
				node2.addEntry(bounds[index], children[index]);
			}
			return new RTDirNode[]{node1, node2};
		}

		/* 寻找即将删除的矩形处于树中的叶子节点 */
		protected RTDataNode findLeaf(Rect rect, Object tag)
		{
			for (int i = 0; i < usedSpace; i++) {
				if (bounds[i].contains(rect)) {
					//如果该子节点包含此矩形，那么递归搜索它的条目
					deleteIndex = i;//记录搜索路径
					RTDataNode leaf = children[i].findLeaf(rect, tag);
					//找到了就直接返回叶子节点，没找到就尝试下个子节点
					if (leaf != null)
						return leaf;
				}
			}
			return null;
		}
	}
	
	/* 叶子节点 */
	private static class RTDataNode extends RTNode
	{
		protected Object[] dates; //每一个矩形对应的物体

		public RTDataNode(RTree rtee, RTNode parent){
			super(rtee, parent, 0);
			dates = new Object[RTree.NODE_CAPACITY + 1];
		}
		/* 对于RTDataNode，它添加一个矩形条目，且条目对应一个物体 */
		protected void addEntry(Rect rect, Object data){
			super.addEntry(rect, data);
			dates[usedSpace++] = data;
		}
		/* 对于RTDataNode，它移除一个矩形条目，且移除条目对应的物体 */
		protected void removeEntry(int i){
			super.removeEntry(i);
			dates[i] = dates[usedSpace - 1];
			dates[--usedSpace] = null;
		}

		/* 插入指定矩形且指定的物体 */
		public void insert(Rect rect, Object data)
		{
			addEntry(rect, data); //先添加这个条目
			if(usedSpace <= RTree.NODE_CAPACITY){
				//如果当前节点的容量足够，则不用分裂节点，直接调整树
				if(parent != null){
					((RTDirNode)parent).adjustTree(this, null);
				}
			}
			else{//节点容量不足，分裂节点，并将两个节点添加到父节点中
				RTDataNode[] nodes = splitLeaf();
				RTDataNode n1 = nodes[0];
				RTDataNode n2 = nodes[1];
				if(parent == null){
					//当前节点是根节点，就创建一个新的根节点，并将它们添加到新根节点中
					RTDirNode root = new RTDirNode(rTree, null, level + 1);
					root.addEntry(n1.getNodeRect(), n1);
					root.addEntry(n2.getNodeRect(), n2);
					rTree.setRoot(root);
				}
				else{
					((RTDirNode)parent).adjustTree(n1, n2);
				}
			}
		}

		/* 删除指定矩形且指定的物体 */
		public void delete(Rect rect, Object data)
		{
			removeEntry(deleteIndex); //刚刚已经找到了，直接删除即可
			List<RTDataNode> deleteEntriesList = new ArrayList<>();
			condenseTree(deleteEntriesList); 

			//重新插入删除结点中剩余的条目
			int size = deleteEntriesList.size();
			for(int i = 0; i < size; ++i)
			{
				RTDataNode leaf = deleteEntriesList.get(i);
				for(int k = 0; k < leaf.usedSpace; ++k){
					rTree.insert(leaf.bounds[k], leaf.dates[k]);
				}
			}
		}

		/* 将当前节点的内容分裂为两个 */
		private RTDataNode[] splitLeaf()
		{
			RTDataNode n1 = new RTDataNode(rTree, parent);
			RTDataNode n2 = new RTDataNode(rTree, parent);
			int[][] group = quadraticSplit(bounds, usedSpace);
			int[] group1 = group[0];
			int[] group2 = group[1];

			for(int i = 0; i < group1.length; ++i){
				int index = group1[i];
				n1.addEntry(bounds[index], dates[index]);
			}
			for(int i = 0; i < group2.length; ++i){
				int index = group2[i];
				n2.addEntry(bounds[index], dates[index]);
			}
			return new RTDataNode[]{n1, n2};
		}

		protected RTDataNode chooseLeaf(Rect rect){
			//已经选择到叶子了
			insertIndex = usedSpace;
			return this;
		}
		protected RTDataNode findLeaf(Rect rect, Object tag)
		{
			for(int i = 0; i < usedSpace; ++i){
				if(dates[i] == tag){
					//叶子节点中，只需要比较两个物体是否相同
					//如果找到了则返回叶子本身，否则返回null
					deleteIndex = i;
					return this;
				}
			}
			return null;
		}
	}
}
