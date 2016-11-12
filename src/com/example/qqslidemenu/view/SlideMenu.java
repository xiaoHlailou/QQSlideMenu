package com.example.qqslidemenu.view;

import com.example.qqslidemenu.utils.ColorUtil;
import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.animation.IntEvaluator;
import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class SlideMenu extends FrameLayout {

	private View menuView;// 菜单的view
	private View mainView;// 主界面的view
	private ViewDragHelper viewDragHelper;
	private FloatEvaluator floatEvaluator;//float计算器
	private IntEvaluator intEvaluator;

	private int width;
	private float dragRange;// 拖拽范围

	// 定义状态常量
	enum DragState {
		Open, Close;
	}

	private DragState currentState = DragState.Close;// 当前SlideMenu状态为关闭

	public SlideMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SlideMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SlideMenu(Context context) {
		super(context);
		init();
	}

	private void init() {
		viewDragHelper = ViewDragHelper.create(this, callback);
		floatEvaluator = new FloatEvaluator();
		intEvaluator = new IntEvaluator();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// 简单的异常处理
		if (getChildCount() != 2) {
			throw new IllegalArgumentException(
					"SlideMenu only have 2 children!");
		}

		menuView = getChildAt(0);
		mainView = getChildAt(1);
	}

	/**
	 * 该方法在onMeasure完后执行,那么可以在该方法中初始化(获取)自己和View的宽高
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = getMeasuredWidth();
		dragRange = width * 0.6f;
	}

	public boolean onTouchEvent(android.view.MotionEvent event) {
		viewDragHelper.processTouchEvent(event);
		return true;
	};

	public boolean onInterceptTouchEvent(android.view.MotionEvent ev) {
		return viewDragHelper.shouldInterceptTouchEvent(ev);
	};

	ViewDragHelper.Callback callback = new Callback() {

		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == menuView || child == mainView;
		}

		public int getViewHorizontalDragRange(View child) {
			return (int) dragRange;
		}

		// 改变位置
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			// 限制范围
			if (child == mainView) {
				if (left < 0)
					left = 0; // 限制mianView左边
				if (left > dragRange)
					left = (int) dragRange; // 限制mianView右边
			} else if (child == menuView) {
				// left=left-dx;//不移动
				//需要固定住，不能左右移动menuView 在下面实现
			}

			return left;
		}

		// 伴随动画
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			if (changedView == menuView) {
				// 固定住menuView  
				menuView.layout(0, 0, menuView.getMeasuredWidth(),
						menuView.getMeasuredHeight());

				// 让mainView伴随动起来
				int newLeft = mainView.getLeft() + dx;
				if (newLeft < 0)
					newLeft = 0; // 限制mianView左边
				if (newLeft > dragRange)
					newLeft = (int) dragRange; // 限制mianView右边
				mainView.layout(newLeft, mainView.getTop() + dy,
						mainView.getRight() + dx, mainView.getBottom() + dy);
			}

			// 1.计算滑动的百分比
			float fraction = mainView.getLeft() / dragRange;
//			Log.i("wdf", "slide::fraction="+fraction);
			// 2.执行伴随动画
			executeAnim(fraction);
			// 3.更改状态，回调listener方法
			if (fraction <0.1f && currentState != DragState.Close) {
				// 更改状态为关闭，并回调关闭的方法
				currentState = DragState.Close;
				if (listener != null)
					listener.onClose();
			}else if (fraction>0.9f&&currentState!=DragState.Open) {
				// 更改状态为打开，并回调打开的方法
				currentState=DragState.Open;
				if (listener!=null) {
					listener.onOpen();	
				}
			}
			//将drag的fraction暴露给外界
			if (listener!=null) {
				listener.onDraging(fraction);
			}

		}

		//手抬起执行
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			if (mainView.getLeft() < dragRange / 2) { // computeScroll
				// 左半边 平滑滚动
				close();
			} else {
				// 右半边
				open();
			}
			
			//处理用户的稍微滑动
			if (xvel>200&&currentState!=DragState.Open) {
				open();
			}else if (xvel<-200&&currentState!=DragState.Close) {
				close();
			}
		}
	};
	
	/**
	 * 关闭菜单
	 */
	public void close() {
		viewDragHelper
				.smoothSlideViewTo(mainView, 0, mainView.getTop());
		ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
	}

	/**
	 * 打开菜单
	 */
	public void open() {
		viewDragHelper.smoothSlideViewTo(mainView, (int) dragRange,
				mainView.getTop());
		ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
	}

	public void computeScroll() {
		if (viewDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
		}
	}

	/**
	 * 执行伴随动画
	 * 
	 * @param fraction
	 */
	protected void executeAnim(float fraction) {
		// fraction:0-1
		// 缩小mainView
		// float scaleValue=0.8f+0.2f*(1-fraction);
		ViewHelper.setScaleX(mainView,
				floatEvaluator.evaluate(fraction, 1f, 0.8f));
		ViewHelper.setScaleY(mainView,
				floatEvaluator.evaluate(fraction, 1f, 0.8f));

		// 移动menuView
		ViewHelper.setTranslationX(menuView, intEvaluator.evaluate(fraction,
				-menuView.getMeasuredWidth() / 2, 0));

		// 放大menuView
		ViewHelper.setScaleX(menuView,
				floatEvaluator.evaluate(fraction, 0.5f, 1f));
		ViewHelper.setScaleY(menuView,
				floatEvaluator.evaluate(fraction, 0.5f, 1f));

		// 改变menuView的透明度
		ViewHelper.setAlpha(menuView,
				floatEvaluator.evaluate(fraction, 0.3f, 1f));

		// 给SlideMenu的背景添加黑色的遮罩效果
		getBackground().setColorFilter(
				(Integer) ColorUtil.evaluateColor(fraction, Color.BLACK,
						Color.TRANSPARENT), Mode.SRC_OVER);
	};
	
	/**
	 * 获取当前状态
	 * @return
	 */
	public DragState getCurrentState(){
		return currentState;
	}

	private OnDragStateChangeListener listener;

	public void setOnDragStateChangeListener(OnDragStateChangeListener listener) {
		this.listener = listener;
	}


	public interface OnDragStateChangeListener {
		/**
		 * 打开的回调
		 */
		void onOpen();

		/**
		 * 失败的回调
		 */
		void onClose();

		/**
		 * 拖拽中的回调
		 * 
		 * @param fraction
		 *            百分比
		 */
		void onDraging(float fraction);

	}

}
