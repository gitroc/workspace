package com.example.testyibiao;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SpeedActivity extends Activity {

	TextView fileLength = null;
	TextView speed = null;
	TextView hasDown = null;
	TextView percent = null;
	String url = "http://www.51eoc.com:8080/itravel/36.7-1.7.8.apk";

	byte[] imageData = null;
	Button b ;
	NetWorkSpeedInfo netWorkSpeedInfo = null;
	private final int UPDATE_SPEED = 1;// 进行中
	private final int UPDATE_DNOE = 0;// 完成下载

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
		//speed = (TextView) findViewById(R.id.tvSpeed);
		//Button b = (Button) findViewById(R.id.btn_testSpeed);
		netWorkSpeedInfo = new NetWorkSpeedInfo();
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new Thread() {
					@Override
					public void run() {
						Log.i("开始", "**********开始  ReadFile*******");
						imageData = ReadFile.getFileFromUrl(url, netWorkSpeedInfo);
					}
				}.start();

				new Thread() {
					@Override
					public void run() {
						Log.i("开始", "**********开始  netWorkSpeedInfo1*******");
						while (netWorkSpeedInfo.hadFinishedBytes < netWorkSpeedInfo.totalBytes) {
							try {
								sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							handler.sendEmptyMessage(UPDATE_SPEED);
						}
						if (netWorkSpeedInfo.hadFinishedBytes == netWorkSpeedInfo.totalBytes) {
							handler.sendEmptyMessage(UPDATE_DNOE);
							netWorkSpeedInfo.hadFinishedBytes = 0;
						}

					}
				}.start();
			}
		});
	}

	private Handler handler = new Handler() {
		long tem = 0;
		long falg = 0;
		long numberTotal = 0;
		List<Long> list = new ArrayList<Long>();

		@Override
		public void handleMessage(Message msg) {
			int value = msg.what;
			switch (value) {
			case UPDATE_SPEED:
				tem = netWorkSpeedInfo.speed / 1024;
				list.add(tem);
				for (Long numberLong : list) {
					numberTotal += numberLong;
				}
				falg = numberTotal / list.size();
				numberTotal = 0;
				speed.setText("当前速度："+tem + "kb/s"+"  平均速度："+falg+"kb/s");
				break;

			case UPDATE_DNOE:
				//speed.setText("完成");
				list.clear();
				tem = 0;
				falg = 0;
				numberTotal = 0;
				break;
			default:
				break;
			}
		}
	};
}
