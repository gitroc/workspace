package com.maintab;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * @author roc
 *	�����������Զ���TabHost
 */
public class MainTab extends FragmentActivity{
	
	private static String TAG = "[MainTab]";
		
	//����FragmentTabHost����
	private FragmentTabHost mTabHost;
	
	//����һ������
	private LayoutInflater layoutInflater;
		
	//�������������Fragment����
	private Class<?> fragmentArray[] = {HomePage.class,
										MessagePage.class,
										FriendsPage.class,
										SquarePage.class,
										MorePage.class};
	
	//������������Ű�ťͼƬ
	private int mImageViewArray[] = {R.drawable.tab_home_btn,
									 R.drawable.tab_message_btn,
									 R.drawable.tab_selfinfo_btn,
									 R.drawable.tab_square_btn,
									 R.drawable.tab_more_btn};
	//Tabѡ�������
	private String mTextviewArray[];
	
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_tab_layout);
        
        initView();
    }
	 
	/**
	 * ��ʼ�����
	 */
	private void initView(){
		Log.i(TAG, "initView()");
		//ʵ�������ֶ���
		layoutInflater = LayoutInflater.from(this);
				
		//ʵ����TabHost���󣬵õ�TabHost
		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);	
		mTextviewArray = getResources().getStringArray(R.array.text_view);
		
		for(int i = 0; i < fragmentArray.length; i++) {
			//Log.i(TAG, "i = " + i);
			//Ϊÿһ��Tab��ť����ͼ�ꡢ���ֺ�����
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
			//��Tab��ť��ӽ�Tabѡ���
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			//����Tab��ť�ı���
			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
		}
	}
	
	/**
	 * ��Tab��ť����ͼ�������
	 */
	private View getTabItemView(int index){
		//Log.i(TAG, "index = " + index);
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);
	
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		
		TextView textView = (TextView) view.findViewById(R.id.textview);		
		textView.setText(mTextviewArray[index]);
	
		return view;
	}
}
