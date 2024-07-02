package com.mygame.abbox.obstacle.shape;
import com.mygame.abbox.share.graphics.*;
import android.graphics.*;

public class ShapeCollisionChecker
{
	public static boolean rect_rect(Shape s1, Shape s2){
		return Rect.intersects(s1.getBounds(), s2.getBounds());
	}
	
	public static boolean circle_circle(CircleShape c1, CircleShape c2){
		return ShapeCollision.circle_circle(c1.getCircleX(), c1.getCircleY(), c1.getCircleRadius(),
		                                    c2.getCircleX(), c2.getCircleY(), c2.getCircleRadius());
	}

	public static boolean rect_circle(RectShape rect, CircleShape circle){
		return ShapeCollision.rect_circle(rect.getLeft(), rect.getTop(), rect.getRight(), rect.getBottom(),
		                                  circle.getCircleX(), circle.getCircleY(), circle.getCircleRadius());
	}
}

