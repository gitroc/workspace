package com.maintab;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentOne extends Fragment{
	private String TAG = "[FragmentOne]";
	private View mMainView;
	private TextView tv;
	private Button btn;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG, "FragmentOne-->onCreate()");
		
		initService();
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mMainView = inflater.inflate(R.layout.fragment_one, (ViewGroup)getActivity().findViewById(R.id.viewpager), false);
		
		tv = (TextView)mMainView.findViewById(R.id.tv1);
		btn = (Button)mMainView.findViewById(R.id.btn1);

        final String strJni = JniString.getJniStringFromNative();
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tv.setText(strJni);
			}
		});
		
	}
	
	private void initService() {
		Log.i(TAG, "initService()");
		Intent intent = new Intent(getActivity(), CoreService.class);
		getActivity().startService(intent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "FragmentOne-->onCreateView()");
		
		ViewGroup p = (ViewGroup) mMainView.getParent(); 
        if (p != null) { 
            p.removeAllViewsInLayout(); 
            Log.i(TAG, "FragmentOne-->�Ƴ��Ѵ��ڵ�View");
        } 
		
		return mMainView;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "FragmentOne-->onDestroy()");
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG, "FragmentOne-->onPause()");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "FragmentOne-->onResume()");
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(TAG, "FragmentOne-->onStart()");
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i(TAG, "FragmentOne-->onStop()");
	}
}
