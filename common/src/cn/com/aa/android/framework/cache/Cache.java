package cn.com.aa.android.framework.cache;

import android.database.Cursor;

import java.io.File;
import java.text.ParseException;

/**
 * 缓存数据实体
 */
public class Cache {
    //bitmap内存缓存默认设置为4M
    public static int memoryCacheSize=1024*1024*2;
    private long id;
    private String key;     //缓存key
    private File file;      //本地缓存的文件
    private long size;      //文件大小，单位byte
    private int status;     //状态
    private long time;      //下载时间
    private long expire;    //缓存过期时间

    public void parse(Cursor cur) throws ParseException {
        this.setId(cur.getLong(cur.getColumnIndex("id")));
        if(cur.getString(cur.getColumnIndex("key")) != null) {
            this.setKey(cur.getString(cur.getColumnIndex("key")));
        }
        if(cur.getString(cur.getColumnIndex("file")) != null) {
            this.setFile(new File(cur.getString(cur.getColumnIndex("file"))));
        }
        if(cur.getString(cur.getColumnIndex("size")) != null) {
            this.setSize(cur.getLong(cur.getColumnIndex("size")));
        }
        this.setStatus(cur.getInt(cur.getColumnIndex("status")));
        this.setTime(cur.getLong(cur.getColumnIndex("time")));
        this.setExpire(cur.getLong(cur.getColumnIndex("expire")));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }
}
