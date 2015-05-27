package com.hexiaochun;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.hexiaochun.utils.Base64Coder;
import com.hexiaochun.utils.ZoomBitmap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener {

	// 服务器地址
	private static final String HOST = "http://10.35.4.107:8080/upServer";
//	private static final String HOST = "http://10.32.141.12:8080/interface/taskfile.json?MD5=3DA4FED1-4F54-4006-8BA2-B41E53D9FEF8";
	// 显示图片
	private ImageView image;
	// 两个but
	private Button take;
	private Button selete;
	// 记录文件名
	private String filename;
	// 上传的bitmap
	private Bitmap upbitmap;
	private Button up;
	
	//多线程通信
	private Handler myHandler;
	private ProgressDialog myDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		image = (ImageView) this.findViewById(R.id.imageView1);
		take = (Button) this.findViewById(R.id.take);
		selete = (Button) this.findViewById(R.id.selete);
		up=(Button)this.findViewById(R.id.up);
		take.setOnClickListener(this);
		selete.setOnClickListener(this);
		up.setOnClickListener(this);

		myHandler=new MyHandler();
	}

	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.take:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			filename = "xiaochun" + System.currentTimeMillis() + ".jpg";
			System.out.println(filename);
			// 下面这句指定调用相机拍照后的照片存储的路径
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
					Environment.getExternalStorageDirectory(), filename)));
			startActivityForResult(intent, 1);
			break;
		case R.id.selete:
			intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					"image/*");
			startActivityForResult(intent, 2);
			break;
		case R.id.up:
			myDialog = ProgressDialog.show(this, "Loading...", "Please wait...", true, false);
			new Thread(new Runnable() {
				public void run() {
					upload();
					myHandler.sendMessage(new Message());
				}
			}).start();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		switch (requestCode) {
		case 1:
			//解成bitmap,方便裁剪
			Bitmap bitmap=BitmapFactory.decodeFile(Environment.
					getExternalStorageDirectory().getPath()+"/"+filename);
			float wight=bitmap.getWidth();
			float height=bitmap.getHeight();
//			ZoomBitmap.zoomImage(bitmap, wight/8, height/8);
			image.setImageBitmap(ZoomBitmap.zoomImage(bitmap, wight/8, height/8));
			upbitmap=ZoomBitmap.zoomImage(bitmap, wight/8, height/8);
			break;
		case 2:
			if(data!=null){
				image.setImageURI(data.getData());
				System.out.println(getAbsoluteImagePath(data.getData()));
				upbitmap=BitmapFactory.decodeFile(getAbsoluteImagePath(data.getData()));
				//剪一下，防止测试的时候上传的文件太大
				upbitmap=ZoomBitmap.zoomImage(upbitmap, upbitmap.getWidth()/8, upbitmap.getHeight()/8);
			}
			break;
		default:
			break;
		}
	}

	// 取到绝对路径
	protected String getAbsoluteImagePath(Uri uri) {
		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, proj, // Which columns to return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	// 上传
	public void upload() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		upbitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
		byte[] b = stream.toByteArray();
		// 将图片流以字符串形式存储下来
		String file = new String(Base64Coder.encodeLines(b));
		HttpClient client = new DefaultHttpClient();
		// 设置上传参数
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("file", file));
		HttpPost post = new HttpPost(HOST);
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			post.addHeader("Accept",
					"text/javascript, text/html, application/xml, text/xml");
			post.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			post.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			post.addHeader("Connection", "Keep-Alive");
			post.addHeader("Cache-Control", "no-cache");
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			System.out.println(response.getStatusLine().getStatusCode());
			HttpEntity e = response.getEntity();			
			System.out.println(EntityUtils.toString(e));
			if (200 == response.getStatusLine().getStatusCode()) {
				System.out.println("上传完成");
			} else {
				System.out.println("上传失败");
			}
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			myDialog.dismiss();
		}
	}

}
