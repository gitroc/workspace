package cn.com.aa.android.framework.http.client;


public class CacheParams {
    
    private int storeType;          //存储类型（内部/外部）
    private int expireTime;         //过期时间
    private boolean isRefresh;      //是否刷新
	/**
     * 构造一个缓存对象
     * @param storeType  存储类型
     * @param expireTime 过期时间
     * @param isRefresh  是否刷新
     */
    public CacheParams(int storeType,int expireTime,boolean isRefresh){
        this.storeType=storeType;
        this.expireTime=expireTime;
        this.isRefresh=isRefresh;
    }
    
    public CacheParams(int storeType,boolean isRefresh){
        this.storeType=storeType;
        this.isRefresh=isRefresh;
    }

    public void setRefresh(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    public int getStoreType() {
        return storeType;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public boolean isRefresh() {
        return isRefresh;
    }
}

