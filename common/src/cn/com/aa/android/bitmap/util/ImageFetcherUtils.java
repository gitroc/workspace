package cn.com.aa.android.bitmap.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ProgressBar;
import cn.com.aa.android.bitmap.util.ImageCache.ImageCacheParams;
import cn.com.aa.common.android.utils.FileUtils;

/**
 * 异步下载图片工具类针对 listview、gridview、gallery等多图片下载
 * 
 * 
 */
public class ImageFetcherUtils {
	/**
	 * 缓存图片文件夹名称
	 */
	public static final String DiskCacheDirName = "images";

	/**
	 * 实例化ImageFetcher并初始化参数
	 * 
	 * @param context
	 * @param fragmentManager
	 *            碎片管理
	 * @param buildParams
	 * @return
	 */
	public static ImageFetcher instanceImageFecher(Context context, FragmentManager fragmentManager, BuildParams buildParams) {
		Params params = null;
		ImageFetcher mImageFetcher = null;
		if (buildParams != null) {
			params = buildParams.params;
		} else {
			params = new Params();
		}
		ImageCacheParams cacheParams = new ImageCacheParams(context, DiskCacheDirName);
		cacheParams.setMemCacheSizePercent(0.2f); // Set memory cache to 20% of
													// app memory
		if (params.imgSize != null && params.imgSize.length == 2) {
			Log.i("width", "==="  +params.imgSize[0]+"   ===="+params.imgSize[1]);
			mImageFetcher = new ImageFetcher(context, params.imgSize[0], params.imgSize[1]);
		} else {
			mImageFetcher = new ImageFetcher(context);
		}
		mImageFetcher.setImageFadeIn(params.isFadeOut);
		mImageFetcher.addImageCache(fragmentManager, cacheParams);
		return mImageFetcher;
	}

	/**
	 * 将图片保存到指定文件
	 * 
	 * @param url
	 * @param targetFile
	 */
	public static void saveImageToDisk(final String url, final File targetFile, final SaveImageListener saveImageListener) {
		new Thread() {
			public void run() {
				Message message = handler.obtainMessage();
				InputStream is = ImageCache.getInputStreamFromDiskCache(url);
				if (is != null) {
					try {
						FileUtils.writeFile(targetFile, is);
						message.what = 0;
						message.obj = saveImageListener;
						handler.sendMessage(message);
					} catch (IOException e) {
						message.what = SaveImageListener.SAVE_EXCEPTION;
						message.obj = saveImageListener;
						handler.sendMessage(message);
						e.printStackTrace();
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					message.what = SaveImageListener.DATA_ISNULL;
					message.obj = saveImageListener;
					handler.sendMessage(message);
				}
			}
		}.start();
	}

	private static Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			SaveImageListener saveImageListener = (SaveImageListener) msg.obj;
			int what = msg.what;
			if (what == 0) {
				if (saveImageListener != null) {
					saveImageListener.onSuccess();
				}
			} else if (what == SaveImageListener.SAVE_EXCEPTION) {
				if (saveImageListener != null) {
					saveImageListener.onFailure(what);
				}
			} else if (what == SaveImageListener.DATA_ISNULL) {
				if (saveImageListener != null) {
					saveImageListener.onFailure(what);
				}
			}
		}
	};

	public static abstract class SaveImageListener {
		public static final int SAVE_EXCEPTION = -1;
		public static final int DATA_ISNULL = -2;

		public abstract void onSuccess();

		public void onFailure(int failType) {
		}
	}

	public static void onPause(ImageFetcher mImageFetcher) {
		if (mImageFetcher != null) {
			mImageFetcher.setPauseWork(false);
			mImageFetcher.setExitTasksEarly(true);
			mImageFetcher.flushCache();
		}
	}

	public static void onResume(ImageFetcher mImageFetcher) {
		if (mImageFetcher != null) {
			mImageFetcher.setExitTasksEarly(false);
		}
	}

	public static void onDestroy(ImageFetcher mImageFetcher) {
		if (mImageFetcher != null) {
			mImageFetcher.closeCache();
		}
	}

	public static class BuildParams {
		Params params;

		public BuildParams() {
			params = new Params();
		}

		public BuildParams setFadeOut(boolean isFadeOut) {
			params.isFadeOut = isFadeOut;
			return this;
		}

		public BuildParams setProgressBar(ProgressBar progressBar) {
			params.progressBar = progressBar;
			return this;
		}

		public BuildParams setImgSize(final int width, final int height) {
			params.imgSize = new int[] { width, height };
			return this;
		}

		public Params getParams() {
			return params;
		}

		@Override
		public String toString() {
			return params.getImgSize()[0]+"  "+params.getImgSize()[1];
		}
	}

	public static class Params {
		private boolean isFadeOut = true; // 是否淡出
		private ProgressBar progressBar;
		private int[] imgSize; // 图片尺寸

		public int[] getImgSize() {
			return imgSize;
		}

		public void setImgSize(int[] imgSize) {
			this.imgSize = imgSize;
		}
	}
}
