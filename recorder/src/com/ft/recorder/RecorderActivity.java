package com.ft.recorder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author 李晓磊 11/12/2013
 * 
 */
public class RecorderActivity extends Activity {

	private final String TAG = "RecorderActivity";

	private Button btnStart;
	private Button btnStop;
	private Button btnUpload;
	private Button btnplay;
	private TextView text;
	private MediaRecorder recorder;
	private boolean isSDCardExit; // 判断SDCard是否存在
	private File SDPathDir;
	private File tempFile;
	private String urlStr = "http://192.168.1.61:8080/recorder/aaa.txt";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		btnStart = (Button) findViewById(R.id.btnStart);
		btnStop = (Button) findViewById(R.id.btnStop);
		btnUpload = (Button) findViewById(R.id.btnUpload);
		btnplay = (Button) findViewById(R.id.btnPlay);
		text = (TextView) findViewById(R.id.text);

		btnStart.setEnabled(true);
		btnStop.setEnabled(false);
		btnUpload.setEnabled(false);

		isSDCardExit = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (isSDCardExit) {
			SDPathDir = Environment.getExternalStorageDirectory();
		}
		buttonListener();
	}

	/**
	 * 添加按钮事件
	 */
	private void buttonListener() {
		// 开始录音
		btnStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initRecorder();
				startRecorder();
				text.setText("正在录音……" + tempFile.getAbsolutePath());
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
				btnUpload.setEnabled(true);
			}
		});
		// 停止录音
		btnStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopRecorder();
				text.setText("停止录音……" + tempFile.getAbsolutePath());
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				btnUpload.setEnabled(true);
			}
		});
		// 上传录音
		btnUpload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				text.setText("正在上传……" + tempFile.getAbsolutePath());
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				btnUpload.setEnabled(false);
				// if (upload(tempFile)) {
				// text.setText("上传成功……");
				// } else {
				// text.setText("上传失败……");
				// Toast.makeText(RecorderActivity.this, "上传失败",
				// Toast.LENGTH_SHORT);
				// }
				FileUploadTask fileuploadtask = new FileUploadTask();
				fileuploadtask.execute();
			}
		});
		btnplay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				text.setText("正在播放……" + tempFile.getAbsolutePath());
				// 打开播放的程序
				openFile(tempFile);
			}
		});
	}

	private void openFile(File mediaFile) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		String type = getFileType(mediaFile);
		intent.setDataAndType(Uri.fromFile(mediaFile), type);
		startActivity(intent);
	}

	// 判断要打开的文件类型
	private String getFileType(File f) {
		String end = f.getName().substring(f.getName().lastIndexOf(".") + 1,
				f.getName().length());
		String type = "";
		if (end.equals("mp3") || end.equals("aac") || end.equals("amr")
				|| end.equals("mpeg") || end.equals("mp4") || end.equals("3gp")) {
			type = "audio";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg")) {
			type = "image";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}

	/**
	 * 准备录音
	 */
	private void initRecorder() {
		recorder = new MediaRecorder();
		/* 设置音频源 */
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		/* 设置输出格式 */
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		/* 设置音频编码器 */
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			/* 创建一个临时文件，用来存放录音 */
			tempFile = File.createTempFile("tempFile", ".mp3", SDPathDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/* 设置录音文件 */
		recorder.setOutputFile(tempFile.getAbsolutePath());
	}

	/**
	 * 开始录音
	 */
	private void startRecorder() {
		try {
			if (!isSDCardExit) {
				Toast.makeText(this, "请插入SD卡", Toast.LENGTH_LONG).show();
				return;
			}
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 停止录音
	 */
	private void stopRecorder() {
		if (recorder != null) {
			recorder.stop();     // stop recording
			recorder.reset();    // set state to idle
			recorder.release();  // release resources back to the system
			recorder = null;
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (recorder != null) {
			recorder.stop();
			recorder.release();// 释放资源
			recorder = null;
		}
		super.onStop();
	}

	private void deleteFile() {
		boolean del = tempFile.delete();
		System.out.println("deleteFile::" + del);
	}

	@SuppressLint("ShowToast")
	class FileUploadTask extends AsyncTask<Object, Integer, String> {

		private ProgressDialog dialog = null;
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;
		private FileInputStream fileInputStream = null;
		private InputStream is = null;
		// the server address to process uploaded file
		String urlServer = "http://192.168.0.36:8080/cnrvoice-file-upload/rest/file/upload/audio";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		long totalSize = tempFile.length(); // Get size of file, bytes

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(RecorderActivity.this);
			dialog.setMessage("正在上传...");
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.show();
		}

		@Override
		protected String doInBackground(Object... arg0) {

			long length = 0;
			int progress;
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 256 * 1024;// 256KB

			try {
				fileInputStream = new FileInputStream(new File(
						tempFile.getAbsolutePath()));

				URL url = new URL(urlServer);
				connection = (HttpURLConnection) url.openConnection();

				// Set size of every block for post
				connection.setChunkedStreamingMode(256 * 1024);// 256KB
				connection.setConnectTimeout(6000);// 设置连接主机超时（单位：毫秒）
				connection.setReadTimeout(6000);// 设置从主机读取数据超时（单位：毫秒）
				// 设置是否从httpUrlConnection读入，默认情况下是true;
				connection.setDoInput(true);
				// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
				// http正文内，因此需要设为true, 默认情况下是false;
				connection.setDoOutput(true);
				// Post 请求不能使用缓存
				connection.setUseCaches(false);
				// 设定请求的方法为"POST"，默认是GET
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Charset", "UTF-8");
				// 设定传送的内容类型是可序列化的java对象
				// (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
				connection.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				// 连接，从上述第2条中url.openConnection()至此的配置必须要在connect之前完成，
//				connection.connect();
				// 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，
				// 所以在开发中不调用上述的connect()也可以)。
				outputStream = new DataOutputStream(
						connection.getOutputStream());
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				outputStream
						.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
								+ tempFile + "\"" + lineEnd);
				outputStream.writeBytes(lineEnd);

				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				while (bytesRead > 0) {
					outputStream.write(buffer, 0, bufferSize);
					length += bufferSize;
					progress = (int) ((length * 100) / totalSize);
					publishProgress(progress);

					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens
						+ lineEnd);
				publishProgress(100);

				// Responses from the server (code and message)
				int serverResponseCode = connection.getResponseCode();
				String serverResponseMessage = connection.getResponseMessage();

				is = connection.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "utf-8");
				BufferedReader br = new BufferedReader(isr);
				String result = br.readLine();

				System.out.println("serverResponseCode::" + serverResponseCode);
				System.out.println("serverResponseMessage::"
						+ serverResponseMessage);
				System.out.println("result::" + result);
				return result;
				/* 将Response显示于Dialog */
				// Toast toast = Toast.makeText(UploadtestActivity.this, ""
				// + serverResponseMessage.toString().trim(),
				// Toast.LENGTH_LONG);
				// showDialog(serverResponseMessage.toString().trim());
				/* 取得Response内容 */
				// InputStream is = connection.getInputStream();
				// int ch;
				// StringBuffer sbf = new StringBuffer();
				// while ((ch = is.read()) != -1) {
				// sbf.append((char) ch);
				// }
				//
				// showDialog(sbf.toString().trim());
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
				System.out.println("FileNotFoundException::" + ex.getMessage());
				if (ex.getMessage().contains("http"))
					return "服务器连接错误";
				else if (ex.getMessage().contains(tempFile.getAbsolutePath()))
					return "音频文件查找失败";
			} catch (MalformedURLException ex) {
				ex.printStackTrace();
				System.out.println("MalformedURLException::" + ex.getMessage());
				return "URL错误";
			} catch (IOException ex) {
				ex.printStackTrace();
				System.out.println("IOException::" + ex.getMessage());
				return "数据流错误";
			} finally {
				try {
					if (fileInputStream != null)
						fileInputStream.close();
					if (fileInputStream != null) {
						outputStream.flush();
						outputStream.close();
					}
					if (is != null)
						is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			dialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String res) {
			try {
				dialog.dismiss();
				if (Boolean.parseBoolean(res)) {
					Toast.makeText(RecorderActivity.this, "上传成功", 1).show();
				} else {
					Toast.makeText(RecorderActivity.this, "上传失败，" + res, 1)
							.show();
				}
				deleteFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}