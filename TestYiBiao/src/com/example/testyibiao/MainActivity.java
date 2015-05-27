package com.example.testyibiao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private String url = "http://pai.chexiang.com/download/app/chexiangpai.apk";
	
	private String upload = "http://10.32.141.12:8080/interface/taskfile.json";

	byte[] imageData = null;
	Button b;
	NetWorkSpeedInfo netWorkSpeedInfo = null;
	private final int UPDATE_SPEED = 1;// 进行中
	private final int UPDATE_DNOE = 0;// 完成下载
	private ImageView imageView;
	private long begin = 0;
	private Button startButton;
	private TextView connectionType, nowSpeed, avageSpeed;
	long tem = 0;
	long falg = 0;
	long numberTotal = 0;
	List<Long> list = new ArrayList<Long>();

	public String Ping(String str) {
		String resault = "";
		Process p;
		try {
			// ping -c 3 -w 100 中 ，-c 是指ping的次数 3是指ping 3次 ，-w 100
			// 以秒为单位指定超时间隔，是指超时时间为100秒
			p = Runtime.getRuntime().exec("ping -c 1 -w 5 " + str);
			int status = p.waitFor();
			InputStream input = p.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}

			Log.i("MainActivity", buffer.toString());
			getAvgOfTime(getPingResponseTime(buffer.toString()));
			if (status == 0) {
				resault = "success";
			} else {
				resault = "faild";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return resault;
	}

	/**
	 * 根据 PING命令应答获取所有应答时间
	 * 
	 * @param response
	 * @return
	 */
	private ArrayList<String> getPingResponseTime(String response) {

		// String reg = "^PING\\b" //# match ping
		// + "[^(]*\\(([^)]*)\\)" // # capture IP
		// + "\\s([^.]*)\\sbytes\\sof\\sdata." // # capture the bytes of data
		// + ".*?(\\d+)\\sbytes" // # capture bytes
		// + ".*?icmp_seq=(\\d+)" // # capture icmp_seq
		// + ".*?ttl=(\\d+)" // # capture ttl
		// + ".*?time=(.*?)\\sms" // # capture time
		// + ".*?(\\d+)\\spackets\\stransmitted" //
		// + ".*?(\\d+)\\sreceived" //
		// + ".*?(\\d+%)\\spacket\\sloss" //
		// + ".*?time\\s(\\d+ms)" //
		// + ".*?=\\s([^\\/]*)\\/([^\\/]*)\\/([^\\/]*)\\/(.*?)\\sms";

		String reg = ".*?time=(.*?)\\sms"; // # capture time

		Pattern re = Pattern.compile(reg, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = re.matcher(response);

		ArrayList<String> strArray = new ArrayList<String>();

		while (m.find()) {
			for (int groupIdx = 0; groupIdx < m.groupCount() + 1; groupIdx++) {
				if (groupIdx > 0) {
					strArray.add(m.group(groupIdx));
				}
			}
		}

		Log.i("MainActivity", strArray.toString());
		return strArray;
	}

	/**
	 * 算出PING多次应答时间的平均值
	 * 
	 * @param timeArray
	 * @return
	 */
	private float getAvgOfTime(ArrayList<String> timeArray) {
		float time = 0;
		for (int i = 0; i < timeArray.size(); i++) {
			time += Float.valueOf(timeArray.get(i));
		}

		float avg = time / timeArray.size();
		DecimalFormat decimalFormat = new DecimalFormat(".00");
		Log.i("getAvgOfTime", decimalFormat.format(avg));
		return avg;
	}

	private class NetPing extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String s = "";
			s = Ping("www.baidu.com");
			Log.i("ping", s);
			return s;
		}
	}
	
	private class DownLoad extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.equipment);
		imageView = (ImageView) findViewById(R.id.iv_needle);
		startButton = (Button) findViewById(R.id.start_button);
		connectionType = (TextView) findViewById(R.id.connection_type);
		nowSpeed = (TextView) findViewById(R.id.now_speed);
		avageSpeed = (TextView) findViewById(R.id.average_speed);
		// timer.schedule(task, 1000, 1000);
		netWorkSpeedInfo = new NetWorkSpeedInfo();

		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new NetPing().execute();
				list.clear();
				tem = 0;
				falg = 0;
				numberTotal = 0;

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
							handler.sendEmptyMessage(UPDATE_SPEED);
							netWorkSpeedInfo.hadFinishedBytes = 0;
						}

					}
				}.start();
			}
		});
	}

	protected void startAnimation(double d) {
		AnimationSet animationSet = new AnimationSet(true);
		/**
		 * 前两个参数定义旋转的起始和结束的度数，后两个参数定义圆心的位置
		 */
		// Random random = new Random();
		int end = getDuShu(d);

		Log.i("", "********************begin:" + begin + "***end:" + end);
		RotateAnimation rotateAnimation = new RotateAnimation(begin, end, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 1f);
		rotateAnimation.setDuration(1000);
		animationSet.addAnimation(rotateAnimation);
		imageView.startAnimation(animationSet);
		begin = end;
	}

	public int getDuShu(double number) {
		double a = 0;
		if (number >= 0 && number <= 512) {
			a = number / 128 * 15;
		} else if (number > 521 && number <= 1024) {
			a = number / 256 * 15 + 30;
		} else if (number > 1024 && number <= 10 * 1024) {
			a = number / 512 * 5 + 80;
		} else {
			a = 180;
		}
		return (int) a;
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
				Log.i("a", "tem****" + tem);
				for (Long numberLong : list) {
					numberTotal += numberLong;
				}
				falg = numberTotal / list.size();
				numberTotal = 0;
				nowSpeed.setText(tem + "kb/s");
				avageSpeed.setText(falg + "kb/s");
				startAnimation(Double.parseDouble(tem + ""));
				break;
			default:
				break;
			}
		}
	};

}
