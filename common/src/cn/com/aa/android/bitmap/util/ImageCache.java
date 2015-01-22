/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.com.aa.android.bitmap.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.aa.android.framework.cache.CacheManager;
import cn.com.aa.common.android.utils.Logs;

/**
 * This class handles disk and memory caching of bitmaps in conjunction with the
 * {@link ImageWorker} class and its subclasses. Use
 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} to get an
 * instance of this class, although usually a cache should be added directly to
 * an {@link ImageWorker} by calling
 * {@link ImageWorker#addImageCache(FragmentManager, ImageCacheParams)}.
 */
public class ImageCache {
	private static final String TAG = "ImageCache";

	// Default memory cache size in kilobytes
	private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

	// Default disk cache size in bytes
	private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 100; // 10MB

	// Compression settings when writing images to disk cache
	private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	private static final int DEFAULT_COMPRESS_QUALITY = 70;
	private static final int DISK_CACHE_INDEX = 0;

	// Constants to easily toggle various caches
	private static final boolean DEFAULT_MEM_CACHE_ENABLED = true; // 内存缓存开关
	private static final boolean DEFAULT_DISK_CACHE_ENABLED = true; // 磁盘缓存开关
	private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

	private static DiskLruCache mDiskLruCache;
	private LruCache<String, BitmapDrawable> mMemoryCache;
	private ImageCacheParams mCacheParams;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;

	private static File photoCacheDir;

	// private Set<SoftReference<Bitmap>> mReusableBitmaps;
	// 换成这个容器是为了解决多线程访问该容器并修改该容器造成的bug
	private ConcurrentHashMap<String, SoftReference<Bitmap>> mReusableBitmaps;

	/**
	 * Create a new ImageCache object using the specified parameters. This
	 * should not be called directly by other classes, instead use
	 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} to
	 * fetch an ImageCache instance.
	 * 
	 * @param cacheParams
	 *            The cache parameters to use to initialize the cache
	 */
	private ImageCache(ImageCacheParams cacheParams) {
		init(cacheParams);
	}

	/**
	 * Return an {@link ImageCache} instance. A {@link RetainFragment} is used
	 * to retain the ImageCache object across configuration changes such as a
	 * change in device orientation.
	 * 
	 * @param fragmentManager
	 *            The fragment manager to use when dealing with the retained
	 *            fragment.
	 * @param cacheParams
	 *            The cache parameters to use if the ImageCache needs
	 *            instantiation.
	 * @return An existing retained ImageCache object or a new one if one did
	 *         not exist
	 */
	public static ImageCache getInstance(FragmentManager fragmentManager, ImageCacheParams cacheParams) {

		// Search for, or create an instance of the non-UI RetainFragment
		final RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager);

		// See if we already have an ImageCache stored in RetainFragment
		ImageCache imageCache = (ImageCache) mRetainFragment.getObject();
		Logs.v(TAG, "get imagecache object...");
		// No existing ImageCache, create one and store it in RetainFragment
		if (imageCache == null) {
			Logs.v(TAG, "create imagecache...");
			imageCache = new ImageCache(cacheParams);
			mRetainFragment.setObject(imageCache);
		}

		return imageCache;
	}

	/**
	 * Initialize the cache, providing all parameters.
	 * 
	 * @param cacheParams
	 *            The cache parameters to initialize the cache
	 */
	private void init(ImageCacheParams cacheParams) {
		mCacheParams = cacheParams;

		// Set up memory cache
		if (mCacheParams.memoryCacheEnabled) {

			// If we're running on Honeycomb or newer, then
			if (Utils.hasHoneycomb()) {
				// mReusableBitmaps = new HashSet<SoftReference<Bitmap>>();
				// mReusableBitmaps = Collections.synchronizedSet(new
				// HashSet<SoftReference<Bitmap>>());
				mReusableBitmaps = new ConcurrentHashMap<String, SoftReference<Bitmap>>();
			}

			mMemoryCache = new LruCache<String, BitmapDrawable>(mCacheParams.memCacheSize) {

				/**
				 * Notify the removed entry that is no longer being cached
				 */
				@Override
				protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
					if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
						// The removed entry is a recycling drawable, so notify
						// it
						// that it has been removed from the memory cache
						((RecyclingBitmapDrawable) oldValue).setIsCached(false);
					} else {
						// The removed entry is a standard BitmapDrawable

						if (Utils.hasHoneycomb()) {
							// We're running on Honeycomb or later, so add the
							// bitmap
							// to a SoftRefrence set for possible use with
							// inBitmap later
							// mReusableBitmaps.add(new
							// SoftReference<Bitmap>(oldValue.getBitmap()));
							mReusableBitmaps.put(key, new SoftReference<Bitmap>(oldValue.getBitmap()));
						}
					}
				}

				/**
				 * Measure item size in kilobytes rather than units which is
				 * more practical for a bitmap cache
				 */
				@Override
				protected int sizeOf(String key, BitmapDrawable value) {
					final int bitmapSize = getBitmapSize(value) / 1024;
					return bitmapSize == 0 ? 1 : bitmapSize;
				}
			};
		}

		// By default the disk cache is not initialized here as it should be
		// initialized
		// on a separate thread due to disk access.
		if (cacheParams.initDiskCacheOnCreate) {
			// Set up disk cache
			Logs.v(TAG, "init disk cache ...");
			initDiskCache();
		}
	}

	/**
	 * Initializes the disk cache. Note that this includes disk access so this
	 * should not be executed on the main/UI thread. By default an ImageCache
	 * does not initialize the disk cache when it is created, instead you should
	 * call initDiskCache() to initialize it on a background thread.
	 */
	public void initDiskCache() {
		// Set up disk cache
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
				File diskCacheDir = mCacheParams.diskCacheDir;
				if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
					if (!diskCacheDir.exists()) {
						diskCacheDir.mkdirs();
					}
					if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
						try {
							mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
						} catch (final IOException e) {
							mCacheParams.diskCacheDir = null;
							Logs.e(TAG, "initDiskCache - " + e);
						}
					}
				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	/**
	 * Adds a bitmap to both memory and disk cache.
	 * 
	 * @param data
	 *            Unique identifier for the bitmap to store
	 * @param value
	 *            The bitmap drawable to store
	 */
	public void addBitmapToCache(String data, BitmapDrawable value) {
		if (data == null || value == null) {
			return;
		}

		boolean isPngFormat = data.endsWith(".png");

		// Add to memory cache
		if (mMemoryCache != null) {
			if (RecyclingBitmapDrawable.class.isInstance(value)) {
				// The removed entry is a recycling drawable, so notify it
				// that it has been added into the memory cache
				((RecyclingBitmapDrawable) value).setIsCached(true);
			}
			mMemoryCache.put(data, value);
		}

		synchronized (mDiskCacheLock) {
			// Add to disk cache
			if (mDiskLruCache != null) {
				Logs.v(TAG, "add to disk cache!");
				final String key = hashKeyForDisk(data);
				OutputStream out = null;
				try {
					DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot == null) {
						final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							Log.v(TAG, "isPngFormat :" + isPngFormat);
							if (!isPngFormat) {
								value.getBitmap().compress(mCacheParams.compressFormat, mCacheParams.compressQuality, out);
							}
							Logs.v(TAG, "cache disk is success!");
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (final IOException e) {
					Logs.e(TAG, "addBitmapToCache - " + e);
				} catch (Exception e) {
					Logs.e(TAG, "addBitmapToCache - " + e);
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Get from memory cache.
	 * 
	 * @param data
	 *            Unique identifier for which item to get
	 * @return The bitmap drawable if found in cache, null otherwise
	 */
	public BitmapDrawable getBitmapFromMemCache(String data) {
		BitmapDrawable memValue = null;
		if (mMemoryCache != null) {
			memValue = mMemoryCache.get(data);
		}

		return memValue;
	}

	/**
	 * Get from disk cache.
	 * 
	 * @param data
	 *            Unique identifier for which item to get
	 * @return The bitmap if found in cache, null otherwise
	 */
	public Bitmap getBitmapFromDiskCache(String data) {
		final String key = hashKeyForDisk(data);
		Bitmap bitmap = null;

		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			if (mDiskLruCache != null) {
				InputStream inputStream = null;
				try {
					final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot != null) {
						inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							FileDescriptor fd = ((FileInputStream) inputStream).getFD();

							// Decode bitmap, but we don't want to sample so
							// give
							// MAX_VALUE as the target dimensions
							bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(fd, Integer.MAX_VALUE, Integer.MAX_VALUE, this);
						}
					}
				} catch (final IOException e) {
					Log.e(TAG, "getBitmapFromDiskCache - " + e);
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
					}
				}
			}
			return bitmap;
		}
	}

	/**
	 * 根据url获取inputstream
	 * 
	 * @param data
	 * @return
	 */
	public static InputStream getInputStreamFromDiskCache(String data) {
		String key = ImageCache.hashKeyForDisk(data);
		InputStream inputStream = null;
		if (photoCacheDir != null) {
			try {
				if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
					// 如果磁盘缓存的大小有变化的时候
					// 就要在外面设置ImageCacheParams了这里的DEFAULT_DISK_CACHE_SIZE也要取外面设置的大小
					mDiskLruCache = DiskLruCache.open(photoCacheDir, 1, 1, DEFAULT_DISK_CACHE_SIZE);
				}
				final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
				if (snapshot != null) {
					inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return inputStream;
	}

	/**
	 * @param options
	 *            - BitmapFactory.Options with out* options populated
	 * @return Bitmap that case be used for inBitmap
	 */
	// private final Object mIteratorLock = new Object();
	protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
		Bitmap bitmap = null;
		if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
			// final Iterator<SoftReference<Bitmap>> iterator =
			// mReusableBitmaps.iterator();
			Bitmap item;
			SoftReference<Bitmap> reference = null;
			for (String key : mReusableBitmaps.keySet()) {
				reference = mReusableBitmaps.get(key);
				if (reference != null) {
					item = reference.get();
					if (null != item && item.isMutable()) {
						if (canUseForInBitmap(item, options)) {
							bitmap = item;
							mReusableBitmaps.remove(key);
						}
					} else {
						mReusableBitmaps.remove(key);
					}
				}
			}

			// while (iterator.hasNext()) {
			// reference = iterator.next();
			// item = reference.get();
			// if (null != item && item.isMutable()) {
			// // Check to see it the item can be used for inBitmap
			// if (canUseForInBitmap(item, options)) {
			// bitmap = item;
			// // Remove from reusable set so it can't be used again
			// iterator.remove();
			// if(mReusableBitmaps.contains(reference)){
			// mReusableBitmaps.remove(reference);
			// }
			// break;
			// }
			// } else {
			// // Remove from the set if the reference has been cleared.
			// iterator.remove();
			// if(mReusableBitmaps.contains(reference)){
			// mReusableBitmaps.remove(reference);
			// }
			// }
			// }
		}
		return bitmap;
	}

	/**
	 * Clears both the memory and disk cache associated with this ImageCache
	 * object. Note that this includes disk access so this should not be
	 * executed on the main/UI thread.
	 */
	public void clearCache() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
		}

		synchronized (mDiskCacheLock) {
			mDiskCacheStarting = true;
			if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
				try {
					mDiskLruCache.delete();
				} catch (IOException e) {
					Logs.e(TAG, "clearCache - " + e);
				}
				mDiskLruCache = null;
				initDiskCache();
			}
		}
	}

	/**
	 * Flushes the disk cache associated with this ImageCache object. Note that
	 * this includes disk access so this should not be executed on the main/UI
	 * thread.
	 */
	public void flush() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					mDiskLruCache.flush();
				} catch (IOException e) {
					Logs.e(TAG, "flush - " + e);
				}
			}
		}
	}

	/**
	 * Closes the disk cache associated with this ImageCache object. Note that
	 * this includes disk access so this should not be executed on the main/UI
	 * thread.
	 */
	public void close() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					if (!mDiskLruCache.isClosed()) {
						mDiskLruCache.close();
						mDiskLruCache = null;
					}
				} catch (IOException e) {
					Logs.e(TAG, "close - " + e);
				}
			}
		}
	}

	/**
	 * A holder class that contains cache parameters.
	 */
	public static class ImageCacheParams {
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public File diskCacheDir;
		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;
		public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
		public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
		public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

		/**
		 * Create a set of image cache parameters that can be provided to
		 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} or
		 * {@link ImageWorker#addImageCache(FragmentManager, ImageCacheParams)}.
		 * 
		 * @param context
		 *            A context to use.
		 * @param diskCacheDirectoryName
		 *            A unique subdirectory name that will be appended to the
		 *            application cache directory. Usually "cache" or "images"
		 *            is sufficient.
		 */
		public ImageCacheParams(Context context, String diskCacheDirectoryName) {
			diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
			Log.i("msg", "初始化图片缓存目录:"+diskCacheDir);
		}

		/**
		 * Sets the memory cache size based on a percentage of the max available
		 * VM memory. Eg. setting percent to 0.2 would set the memory cache to
		 * one fifth of the available memory. Throws
		 * {@link IllegalArgumentException} if percent is < 0.05 or > .8.
		 * memCacheSize is stored in kilobytes instead of bytes as this will
		 * eventually be passed to construct a LruCache which takes an int in
		 * its constructor.
		 * 
		 * This value should be chosen carefully based on a number of factors
		 * Refer to the corresponding Android Training class for more
		 * discussion: http://developer.android.com/training/displaying-bitmaps/
		 * 
		 * @param percent
		 *            Percent of available app memory to use to size memory
		 *            cache
		 */
		public void setMemCacheSizePercent(float percent) {
			if (percent < 0.05f || percent > 0.8f) {
				throw new IllegalArgumentException("setMemCacheSizePercent - percent must be " + "between 0.05 and 0.8 (inclusive)");
			}
			memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
		}
	}

	/**
	 * @param candidate
	 *            - Bitmap to check
	 * @param targetOptions
	 *            - Options that have the out* value populated
	 * @return true if <code>candidate</code> can be used for inBitmap re-use
	 *         with <code>targetOptions</code>
	 */
	private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
		int width = targetOptions.outWidth / targetOptions.inSampleSize;
		int height = targetOptions.outHeight / targetOptions.inSampleSize;

		return candidate.getWidth() == width && candidate.getHeight() == height;
	}

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            A unique directory name to append to the cache dir
	 * @return The cache dir
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() : context.getCacheDir().getPath();
		return photoCacheDir = new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * A hashing method that changes a string (like a URL) into a hash suitable
	 * for using as a disk filename.
	 */
	public static String hashKeyForDisk(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * Get the size in bytes of a bitmap in a BitmapDrawable.
	 * 
	 * @param value
	 * @return size in bytes
	 */
	@TargetApi(12)
	public static int getBitmapSize(BitmapDrawable value) {
		Bitmap bitmap = value.getBitmap();

		if (Utils.hasHoneycombMR1()) {
			return bitmap.getByteCount();
		}
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * Check if external storage is built-in or removable.
	 * 
	 * @return True if external storage is removable (like an SD card), false
	 *         otherwise.
	 */
	@TargetApi(9)
	public static boolean isExternalStorageRemovable() {
		if (Utils.hasGingerbread()) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	/**
	 * 这里和下载目录对应
	 * 
	 * @param context
	 *            The context to use
	 * @return The external cache dir
	 */
	// @TargetApi(8)
	public static File getExternalCacheDir(Context context) {
		if (CacheManager.cacheDirExternal == null) {
			//创建应用缓存文件夹
			CacheManager.cacheDirExternal = new File(Environment.getExternalStorageDirectory(), context.getPackageName() + "/cache/app");
			if (!CacheManager.cacheDirExternal.exists() || !CacheManager.cacheDirExternal.isDirectory()) {
				CacheManager.cacheDirExternal.mkdirs();
			}
		}
		return CacheManager.cacheDirExternal;
	}

	// @TargetApi(8)
	// public static File getExternalCacheDir(Context context) {
	// if (Utils.hasFroyo()) {
	// return context.getExternalCacheDir();
	// }
	//
	// // Before Froyo we need to construct the external cache dir ourselves
	// final String cacheDir = "/Android/data/" + context.getPackageName() +
	// "/cache/";
	// return new File(Environment.getExternalStorageDirectory().getPath() +
	// cacheDir);
	// }

	/**
	 * Check how much usable space is available at a given path.
	 * 
	 * @param path
	 *            The path to check
	 * @return The space available in bytes
	 */
	@TargetApi(9)
	public static long getUsableSpace(File path) {
		if (Utils.hasGingerbread()) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	/**
	 * Locate an existing instance of this Fragment or if not found, create and
	 * add it using FragmentManager.
	 * 
	 * @param fm
	 *            The FragmentManager manager to use.
	 * @return The existing instance of the Fragment or the new instance if just
	 *         created.
	 */
	private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
		// Check to see if we have retained the worker fragment.
		RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG);

		// If not retained (or first time running), we need to create and add
		// it.
		if (mRetainFragment == null) {
			mRetainFragment = new RetainFragment();
			fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
		}

		return mRetainFragment;
	}

	/**
	 * A simple non-UI Fragment that stores a single Object and is retained over
	 * configuration changes. It will be used to retain the ImageCache object.
	 */
	public static class RetainFragment extends Fragment {
		private Object mObject;

		/**
		 * Empty constructor as per the Fragment documentation
		 */
		public RetainFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Make sure this Fragment is retained over a configuration change
			// setRetainInstance(true);
		}

		/**
		 * Store a single object in this Fragment.
		 * 
		 * @param object
		 *            The object to store
		 */
		public void setObject(Object object) {
			mObject = object;
		}

		/**
		 * Get the stored object.
		 * 
		 * @return The stored object
		 */
		public Object getObject() {
			return mObject;
		}
	}

}