package com.mygame.abbox.share.graphics;

import android.graphics.Rect;

public final class RectUtils
{
	public static Rect getUnionRect(Rect r1, Rect r2)
	{
		Rect r3 = new Rect(r1);
		r3.union(r2);
		return r3;
	}
	public static Rect getUnionRect(Rect[] rects, int count)
	{
		Rect ret = new Rect();
		for(int i = 0; i < count; ++i){
			ret.union(rects[i]);
		}
		return ret;
	}
	public static int getArea(Rect r){
		return (r.right - r.left) * (r.bottom - r.top);
	}
	public static int intersectArea(Rect r1, Rect r2)
	{
		Rect r3 = new Rect();
		boolean intersect = r3.setIntersect(r1, r2);
		if(intersect){
			return getArea(r3);
		}
		return 0;
	}
}

