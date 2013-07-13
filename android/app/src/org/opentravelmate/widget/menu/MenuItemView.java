package org.opentravelmate.widget.menu;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;

/**
 * Customized ImageButton for building a menu item.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class MenuItemView extends ImageButton {
	
	private int marginRight;
	private int marginTop;
	private int marginBottom;

	public MenuItemView(Context context) {
		super(context);
		setMargins();
	}

	public MenuItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setMargins();
	}

	public MenuItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setMargins();
	}
	
	private void setMargins() {
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		marginRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);
		marginTop = marginRight;
		marginBottom = marginRight;
	}

	public int getMarginRight() {
		return marginRight;
	}

	public int getMarginTop() {
		return marginTop;
	}

	public int getMarginBottom() {
		return marginBottom;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		View parentView = (View) this.getParent();
		int size = parentView.getHeight() - marginTop - marginBottom;
		if (size < 0) {
			size = 0;
		}
		setMeasuredDimension(size, size);
	}
}
