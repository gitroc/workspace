package cn.com.aa.common.android.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

public class BitmapUtils {
	public static Bitmap getScaleBitmap(Context context, int resId, int dstWidth, int dstHeight) {
		Bitmap origialBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
		Bitmap scaleBitmap = Bitmap.createScaledBitmap(origialBitmap, dstWidth, dstHeight, true);
		if ((origialBitmap != null) && (!origialBitmap.isRecycled())) {
			origialBitmap.recycle();
		}
		return scaleBitmap;
	}

	public static Bitmap getScaleBitmap(Bitmap origialBitmap, int dstWidth, int dstHeight) {
		Bitmap scaleBitmap = Bitmap.createScaledBitmap(origialBitmap, dstWidth, dstHeight, true);
		if ((origialBitmap != null) && (!origialBitmap.isRecycled())) {
			origialBitmap.recycle();
		}
		return scaleBitmap;
	}

	public static Bitmap rotate(Bitmap bitmap, int angle) {
		Matrix m = new Matrix();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		m.setRotate(angle);
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
	}

	public static void writeToFile(Bitmap bitmap, String filePath, int quality) {
		File f = new File(filePath);
		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Bitmap compressWithWidth(String filePath, int width) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

		int bmpWidth = options.outWidth;
		int bmpHeight = options.outHeight;

		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(filePath, options);

		float scaleWidth = bmpWidth;
		float scaleHeight = bmpHeight;

		if (bmpWidth > width) {
			scaleWidth = width / scaleWidth;
			scaleHeight = scaleWidth;
		} else {
			scaleWidth = 1.0F;
			scaleHeight = 1.0F;
		}

		Matrix matrix = new Matrix();

		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);

		if (bitmap != resizeBitmap) {
			bitmap.recycle();
		}
		return resizeBitmap;
	}

	public static Bitmap compress(String filePath, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

		int bmpWidth = options.outWidth;
		int bmpHeight = options.outHeight;

		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(filePath, options);

		float scaleWidth = bmpWidth;
		float scaleHeight = bmpHeight;

		if (bmpWidth > width)
			scaleWidth = width / scaleWidth;
		else {
			scaleWidth = 1.0F;
		}

		if (bmpHeight > height)
			scaleHeight = height / scaleHeight;
		else {
			scaleHeight = 1.0F;
		}

		Matrix matrix = new Matrix();

		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);

		if (bitmap != resizeBitmap) {
			bitmap.recycle();
		}
		return resizeBitmap;
	}

	public static Bitmap compressWithWidth(Bitmap bitmap, int width) {
		int bmpWidth = bitmap.getWidth();
		int bmpHeight = bitmap.getHeight();

		float scaleWidth = bmpWidth;
		float scaleHeight = bmpHeight;

		if (bmpWidth > width) {
			scaleWidth = width / scaleWidth;
			scaleHeight = scaleWidth;
		} else {
			scaleWidth = 1.0F;
			scaleHeight = 1.0F;
		}

		Matrix matrix = new Matrix();

		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);

		if (bitmap != resizeBitmap) {
			bitmap.recycle();
		}
		return resizeBitmap;
	}

	public static Bitmap compressWithHeight(File file, int height) {
		return null;
	}

	public static Bitmap compressWithHeight(String filePath, int height) {
		return null;
	}

	public static Bitmap compressWithHeight(Bitmap bitmap, int height) {
		return null;
	}

	public static byte[] bitmap2Bytes(Bitmap bm, Bitmap.CompressFormat format) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(format, 100, baos);
		return baos.toByteArray();
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		int color = -12434878;
		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(-12434878);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public static Bitmap getVideoThumbnail(ContentResolver cr, Uri uri) {
		return getVideoThumbnail(cr, uri, 3);
	}

	public static Bitmap getVideoThumbnail(ContentResolver cr, Uri uri, int kind) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Cursor cursor = cr.query(uri, new String[] { "_id" }, null, null, null);

		if ((cursor == null) || (cursor.getCount() == 0)) {
			return null;
		}
		cursor.moveToFirst();
		String videoId = cursor.getString(cursor.getColumnIndex("_id"));

		if (videoId == null) {
			return null;
		}
		cursor.close();
		long videoIdLong = Long.parseLong(videoId);
		bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, videoIdLong, kind, options);

		return bitmap;
	}

	public static Bitmap getMediaThumbnail(ContentResolver cr, Uri uri) {
		return getMediaThumbnail(cr, uri, 3);
	}

	public static Bitmap getMediaThumbnail(ContentResolver cr, Uri uri, int kind) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Cursor cursor = cr.query(uri, new String[] { "_id" }, null, null, null);

		if ((cursor == null) || (cursor.getCount() == 0)) {
			return null;
		}
		cursor.moveToFirst();
		String mediaId = cursor.getString(cursor.getColumnIndex("_id"));

		if (mediaId == null) {
			return null;
		}
		cursor.close();
		long videoIdLong = Long.parseLong(mediaId);
		bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, videoIdLong, kind, options);

		return bitmap;
	}

	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false;

		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight)
			be = beWidth;
		else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;

		bitmap = BitmapFactory.decodeFile(imagePath, options);

		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, 2);
		return bitmap;
	}

	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;

		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		System.out.println("w" + bitmap.getWidth());
		System.out.println("h" + bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, 2);
		return bitmap;
	}

	public static void recycleBitmap(Bitmap bitmap) {
		if ((bitmap != null) && (!bitmap.isRecycled()))
			bitmap.recycle();
	}
}