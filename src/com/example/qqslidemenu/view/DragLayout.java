package com.example.qqslidemenu.view;

import com.example.qqslidemenu.R;
import com.example.qqslidemenu.utils.ColorUtil;
import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class DragLayout extends FrameLayout {

	private View redView;
	private View blueView;

	private ViewDragHelper viewDragHelper;
	private Scroller scroller;

	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DragLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DragLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		viewDragHelper = ViewDragHelper.create(this, 1.0f, mCallback);
		scroller = new Scroller(getContext());
	}

	/**
	 * 当DragLayout的xml布局结束标签被读取完成会执行该方法，此时会知道自己有几个子View 一般用来初始化子View的引用
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		redView = getChildAt(0);
		blueView = getChildAt(1);
	}

	// FrameLayout 自身有onMeasure
	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	//
	// // 要测量自己的子View
	// // int size=(int) getResources().getDimension(R.dimen.width);
	// // 测量规则
	// // int measureSpec = MeasureSpec.makeMeasureSpec(
	// // redView.getLayoutParams().width, MeasureSpec.EXACTLY);
	// // redView.measure(measureSpec, measureSpec);
	// // blueView.measure(measureSpec, measureSpec);
	//
	// measureChild(redView, widthMeasureSpec, heightMeasureSpec);
	// measureChild(blueView, widthMeasureSpec, heightMeasureSpec);
	// }

	// 重写FrameLayout 的onLayout方法
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int left = 0 + getPaddingLeft();
		int top = 0 + getPaddingTop();

		int centerleft = 0 + getPaddingLeft() + getMeasuredWidth() / 2
				- redView.getMeasuredWidth() / 2;// 水平居中

		redView.layout(left, top, left + redView.getMeasuredWidth(), top
				+ redView.getMeasuredHeight());
		blueView.layout(left, redView.getBottom(),
				left + blueView.getMeasuredWidth(), redView.getBottom()
						+ blueView.getMeasuredHeight());
	}

	@Override
	// 是否拦截触摸事件
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 让ViewDragHelper帮我们判断是否应该拦截
		boolean result = viewDragHelper.shouldInterceptTouchEvent(ev);
		return result;
	}

	public boolean onTouchEvent(android.view.MotionEvent event) {
		// 将触摸事件交给ViewDragHelper来解析处理。
		viewDragHelper.processTouchEvent(event);
		return true;
	};

	ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
		/**
		 * 用于判断是否捕获当前child的触摸事件 View   child:当前触摸子View return:true:捕获并解析 false:不处理
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == blueView || child == redView;
		}

		/**
		 * 当view被开始捕获和解析的回调 View capturedChild:当前被捕获的子View
		 */
		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			super.onViewCaptured(capturedChild, activePointerId);
			Log.i("tag", "onViewCaptured");
		}

		/**
		 * 获取child水平方向的拖拽范围，但是目前不能限制边界 返回的值目前用在手指抬起的时候View缓慢移动的动画时间的计算上面。
		 * 最好不要返回0
		 */
		@Override
		public int getViewHorizontalDragRange(View child) {
			// return super.getViewHorizontalDragRange(child);
			return getMeasuredWidth() - child.getMeasuredWidth();
		}

		public int getViewVerticalDragRange(View child) {
			return getMeasuredHeight() - child.getMeasuredHeight();
		};

		/**
		 * 控制child在水平方向上的移动 left:表示ViewDragHelper认为你想让当前child的left改变的值。
		 * left=child.getLeft()+dx dx:本次child水平方向移动的距离
		 */
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			// Log.i("tag", "left:"+left+"   dx:"+dx);

			if (left < 0) {
				// 限制左边界
				left = 0;
			} else if (left > getMeasuredWidth() - child.getMeasuredWidth()) {
				// 限制右边界
				left = getMeasuredWidth() - child.getMeasuredWidth();
			}
			return left;
			// return left-dx;//不移动
		}

		/**
		 * 控制child在垂直方向上的移动
		 */
		public int clampViewPositionVertical(View child, int top, int dy) {
			if (top < 0) {
				top = 0;
			} else if (top > getMeasuredHeight() - child.getMeasuredHeight()) {
				top = getMeasuredHeight() - child.getMeasuredHeight();
			}

			return top;
		};

		/**
		 * 当child的位置改变的时候执行, 一般用来做其他子View的伴随移动 View changedView: 位置改变的child 
		 * 
		 * int left, int top: child改变之后的left和top    int dx, int dy: 本次水平、垂直移动的距离
		 */
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);// 空方法

			if (changedView == blueView) {
				// blueView移动的时候需要让redView跟随移动
				redView.layout(redView.getLeft() + dx, redView.getRight() + dy,
						redView.getRight() + dx, redView.getBottom() + dy);
			} else if (changedView == redView) {
				// redView移动的时候需要让blueView跟随移动
				blueView.layout(blueView.getLeft() + dx,
						blueView.getTop() + dy, blueView.getRight() + dx,
						blueView.getBottom() + dy);
			}

			// 1.计算view移动的百分比
			float fraction = changedView.getLeft() * 1f
					/ (getMeasuredWidth() - changedView.getMeasuredWidth());
			// 2.执行一系列伴随动画
			executeAnim(fraction);
		}

		/**
		 * 手指抬起时执行该方法 View releasedChild:当前抬起的view float xvel:x方向上的速度 正:向右 float
		 * yvel:y方向上的速度 正:向下
		 */
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);

			int centerLeft = getMeasuredWidth() / 2
					- releasedChild.getMeasuredWidth() / 2;
			if (releasedChild.getLeft() < centerLeft) {
				// 在左半边,应该向左缓慢移动 computeScroll()中实现滚动

				// 传统方法
				// scroller.startScroll(startX, startY, dx, dy, duration);
				// invalidate();

				// ViewDragHelper 内部已经封装了Scroller
				viewDragHelper.smoothSlideViewTo(releasedChild, 0,
						releasedChild.getTop());

			} else {
				// 在右半边，应该向右缓慢移动
				viewDragHelper.smoothSlideViewTo(releasedChild,
						getMeasuredWidth() - releasedChild.getMeasuredWidth(),
						releasedChild.getTop());
			}
			ViewCompat.postInvalidateOnAnimation(DragLayout.this);// 刷新整个布局
		}
	};

	//必须实现
	public void computeScroll() {
		// 传统方法
		// if (scroller.computeScrollOffset()) {//移动还没结束
		// scrollTo(scroller.getCurrX(), scroller.getCurrY());
		// invalidate();
		// }
		if (viewDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(DragLayout.this);// 方法内部已经移动了
																	// 不需要像上面一样
		}
	};

	/**
	 * 执行伴随动画
	 * 
	 * @param fraction
	 */
	private void executeAnim(float fraction) {
		// fraction:0 ~ 1
		// 放大
		// ViewHelper.setScaleX(redView, 1+0.5f*fraction);
		// ViewHelper.setScaleY(redView, 1+0.5f*fraction);

		// 翻转
		// ViewHelper.setRotation(redView, 720*fraction);//平面转
		ViewHelper.setRotationX(redView, 720 * fraction);// x轴转
		// ViewHelper.setRotationY(redView, 720*fraction);//y轴转

		// 平移
		// ViewHelper.setTranslationX(redView, 80*fraction);

		// 透明 0为透明 1为不透明
		// ViewHelper.setAlpha(redView, 1-fraction);

		// 设置过渡颜色的渐变
		redView.setBackgroundColor((Integer) ColorUtil.evaluateColor(fraction,
				Color.RED, Color.GREEN));
	}

}
