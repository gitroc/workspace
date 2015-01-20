package com.maintab;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomePage extends Fragment{
	private String TAG = "[HomePage]";
	
	private View view;
	private ViewPager m_vp;
	private FragmentOne fragmentOne;
	private FragmentTwo fragmentTwo;
	private FragmentThree fragmentThree;
	private FragmentFour fragmentFour;
	private FragmentFive fragmentFive;
	
	private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
	ArrayList<String> titleList = new ArrayList<String>();
	
	private PagerTabStrip pagerTabStrip;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach()");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate()");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView()");
		view = inflater.inflate(R.layout.fragment_home, null);
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "onStart()");
		initViewPager();
	}
	
	private void initViewPager() {
		Log.i(TAG, "initViewPager()");
		m_vp = (ViewPager)view.findViewById(R.id.viewpager);
		
		pagerTabStrip=(PagerTabStrip) view.findViewById(R.id.pagertab);
		//设置下划线的颜色
		pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.darkgoldenrod)); 
		//设置背景的颜色
		pagerTabStrip.setBackgroundColor(getResources().getColor(R.color.lightgoldenrodyellow));
		
		fragmentOne = new FragmentOne();
		fragmentTwo = new FragmentTwo();
		fragmentThree = new FragmentThree();
		fragmentFour = new FragmentFour();
		fragmentFive = new FragmentFive();
		
		fragmentList.add(fragmentOne);
		fragmentList.add(fragmentTwo);
		fragmentList.add(fragmentThree);
		fragmentList.add(fragmentFour);
		fragmentList.add(fragmentFive);
		
		String title_list[] = this.getResources().getStringArray(R.array.title_list);

		for(int i = 0; i < title_list.length; i++){
			titleList.add(title_list[i]);
		}
		
		m_vp.setAdapter(new MyViewPagerAdapter(this.getChildFragmentManager()));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause()");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume()");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		fragmentList.clear();
		
		titleList.clear();
		
		m_vp = null;
		Log.i(TAG, "onStop()");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy()");
	}
	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(TAG, "onDetach()");
	}
	
	public class MyViewPagerAdapter extends FragmentPagerAdapter{
		public MyViewPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
			//Log.i(TAG, "MyViewPagerAdapter()");
		}
		
		@Override
		public Fragment getItem(int arg0) {
			//Log.i(TAG, "getItem()");
			return fragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			//Log.i(TAG, "getCount()");
			return fragmentList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub
			//Log.i(TAG, "getPageTitle()");
			return titleList.get(position);
		}
	}
}
