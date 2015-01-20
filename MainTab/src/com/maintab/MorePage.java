package com.maintab;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MorePage extends Fragment{
	private String TAG = "[MorePage]";
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {	
		Log.i(TAG, "onCreateView()");
		return inflater.inflate(R.layout.fragment_more, null);		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "onStart()");
		
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
}