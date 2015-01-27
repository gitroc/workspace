package com.roc.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by songjunpeng on 2015/1/27.
 */
public class PushActivity extends Activity {
    /** TAG to Log */
    public static final String TAG = PushActivity.class
            .getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
    }
}
