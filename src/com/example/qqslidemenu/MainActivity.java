package com.example.qqslidemenu;

import java.util.Random;

import com.example.qqslidemenu.view.MyLinearLayout;
import com.example.qqslidemenu.view.SlideMenu;
import com.example.qqslidemenu.view.SlideMenu.OnDragStateChangeListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ListView menu_listview, main_listview;
	private SlideMenu slideMenu;
	private ImageView iv_head;
	private MyLinearLayout my_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		
		initController();
		
		initData();

	}

	private void initData() {
		
		menu_listview.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Constant.sCheeseStrings) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textView = (TextView) super.getView(position,
						convertView, parent);
				textView.setTextColor(Color.WHITE);
				return textView;
			}
		});

		main_listview.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Constant.NAMES) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view =convertView==null? super.getView(position, convertView, parent):convertView;

				// 先缩小view
				ViewHelper.setScaleX(view, 0.5f);
				ViewHelper.setScaleY(view, 0.5f);
				
				//以属性动画放大   ViewHelper瞬时，适合伴随动画
				ViewPropertyAnimator.animate(view).scaleX(1).setDuration(350).start();
				ViewPropertyAnimator.animate(view).scaleY(1).setDuration(350).start();
				return view;
			}
		});
		
		my_layout.setSlideMenu(slideMenu);
	}

	private void initController() {
		
		
		slideMenu.setOnDragStateChangeListener(new OnDragStateChangeListener() {
			
			@Override
			public void onOpen() {
				//动画效果滚动到position
				menu_listview.smoothScrollToPosition(new Random().nextInt(menu_listview.getCount()));
			}
			
			@Override
			public void onDraging(float fraction) {
				ViewHelper.setAlpha(iv_head, 1-fraction);//设置图标透明度
			}
			
			@Override
			public void onClose() {
				//图标抖动
				ViewPropertyAnimator.animate(iv_head).translationXBy(15)
					.setInterpolator(new CycleInterpolator(4))
					.setDuration(300)
					.start();
			}
		});
	}

	private void initView() {
		setContentView(R.layout.activity_main);
		slideMenu=(SlideMenu) findViewById(R.id.slideMenu);
		iv_head = (ImageView) findViewById(R.id.iv_head);
		my_layout=(MyLinearLayout) findViewById(R.id.my_layout);
		menu_listview = (ListView) findViewById(R.id.menu_listview);
		main_listview = (ListView) findViewById(R.id.main_listview);
	}

}
