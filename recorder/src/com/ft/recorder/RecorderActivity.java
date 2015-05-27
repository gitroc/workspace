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
 * @author ������ 11/12/2013
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
	private boolean isSDCardExit; // �ж�SDCard�Ƿ����
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
	 * ��Ӱ�ť�¼�
	 */
	private void buttonListener() {
		// ��ʼ¼��
		btnStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initRecorder();
				startRecorder();
				text.setText("����¼������" + tempFile.getAbsolutePath());
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
				btnUpload.setEnabled(true);
			}
		});
		// ֹͣ¼��
		btnStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopRecorder();
				text.setText("ֹͣ¼������" + tempFile.getAbsolutePath());
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				btnUpload.setEnabled(true);
			}
		});
		// �ϴ�¼��
		btnUpload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				text.setText("�����ϴ�����" + tempFile.getAbsolutePath());
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				btnUpload.setEnabled(false);
				// if (upload(tempFile)) {
				// text.setText("�ϴ��ɹ�����");
				// } else {
				// text.setText("�ϴ�ʧ�ܡ���");
				// Toast.makeText(RecorderActivity.this, "�ϴ�ʧ��",
				// Toast.LENGTH_SHORT);
				// }
				FileUploadTask fileuploadtask = new FileUploadTask();
				fileuploadtask.execute();
			}
		});
		btnplay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				text.setText("���ڲ��š���" + tempFile.getAbsolutePath());
				// �򿪲��ŵĳ���
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

	// �ж�Ҫ�򿪵��ļ�����
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
	 * ׼��¼��
	 */
	private void initRecorder() {
		recorder = new MediaRecorder();
		/* ������ƵԴ */
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		/* ���������ʽ */
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		/* ������Ƶ������ */
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			/* ����һ����ʱ�ļ����������¼�� */
			tempFile = File.createTempFile("tempFile", ".mp3", SDPathDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/* ����¼���ļ� */
		recorder.setOutputFile(tempFile.getAbsolutePath());
	}

	/**
	 * ��ʼ¼��
	 */
	private void startRecorder() {
		try {
			if (!isSDCardExit) {
				Toast.makeText(this, "�����SD��", Toast.LENGTH_LONG).show();
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
	 * ֹͣ¼��
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
			recorder.release();// �ͷ���Դ
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
			dialog.setMessage("�����ϴ�...");
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
				connection.setConnectTimeout(6000);// ��������������ʱ����λ�����룩
				connection.setReadTimeout(6000);// ���ô�������ȡ���ݳ�ʱ����λ�����룩
				// �����Ƿ��httpUrlConnection���룬Ĭ���������true;
				connection.setDoInput(true);
				// �����Ƿ���httpUrlConnection�������Ϊ�����post���󣬲���Ҫ����
				// http�����ڣ������Ҫ��Ϊtrue, Ĭ���������false;
				connection.setDoOutput(true);
				// Post ������ʹ�û���
				connection.setUseCaches(false);
				// �趨����ķ���Ϊ"POST"��Ĭ����GET
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Charset", "UTF-8");
				// �趨���͵����������ǿ����л���java����
				// (����������,�ڴ������л�����ʱ,��WEB����Ĭ�ϵĲ�����������ʱ������java.io.EOFException)
				connection.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				// ���ӣ���������2����url.openConnection()���˵����ñ���Ҫ��connect֮ǰ��ɣ�
//				connection.connect();
				// �˴�getOutputStream�������Ľ���connect(������ͬ���������connect()������
				// �����ڿ����в�����������connect()Ҳ����)��
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
				/* ��Response��ʾ��Dialog */
				// Toast toast = Toast.makeText(UploadtestActivity.this, ""
				// + serverResponseMessage.toString().trim(),
				// Toast.LENGTH_LONG);
				// showDialog(serverResponseMessage.toString().trim());
				/* ȡ��Response���� */
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
					return "���������Ӵ���";
				else if (ex.getMessage().contains(tempFile.getAbsolutePath()))
					return "��Ƶ�ļ�����ʧ��";
			} catch (MalformedURLException ex) {
				ex.printStackTrace();
				System.out.println("MalformedURLException::" + ex.getMessage());
				return "URL����";
			} catch (IOException ex) {
				ex.printStackTrace();
				System.out.println("IOException::" + ex.getMessage());
				return "����������";
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
					Toast.makeText(RecorderActivity.this, "�ϴ��ɹ�", 1).show();
				} else {
					Toast.makeText(RecorderActivity.this, "�ϴ�ʧ�ܣ�" + res, 1)
							.show();
				}
				deleteFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}