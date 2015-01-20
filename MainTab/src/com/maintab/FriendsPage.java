package com.maintab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FriendsPage extends Fragment{
	private String TAG = "[FriendsPage]";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {	
		Log.i(TAG, "onCreateView()");
		return inflater.inflate(R.layout.fragment_friends, null);		
	}	
}