package com.roc.example;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SquarePage extends Fragment{
	private String TAG = "[SquarePage]";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView()");
		return inflater.inflate(R.layout.fragment_square, null);
	}	
}