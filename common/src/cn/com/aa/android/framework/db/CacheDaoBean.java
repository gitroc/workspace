package cn.com.aa.android.framework.db;

import com.databaseFreamwork.DBConstants;
import com.databaseFreamwork.orm.annotation.Column;
import com.databaseFreamwork.orm.annotation.Id;
import com.databaseFreamwork.orm.annotation.Table;
/**
 * 
* @Title: CacheDaoBean.java
*@Description:缓存json文件值对象
*@Copyright: Copyright (c) 2014
*@Company: 车享网
* @author houjie
* @date 2014年8月17日 上午3:10:57
 */
@Table(name = "app_cache")
public class CacheDaoBean {

	@Id
	@Column(name = "id")
	private int id;
	@Column(length=255,type = "TEXT", name = "key")
	private String key;
	@Column(length = 255, name = "file")
	private String file;
	@Column(type=DBConstants.NUMERIC,name = "size")
	private long size;
	@Column(type =DBConstants.INTEGER, name = "status")
	private int status;
	@Column(type = DBConstants.NUMERIC, name = "time")
	private long time;
	@Column(type = DBConstants.NUMERIC, name = "expire")
	private long expire;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
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
