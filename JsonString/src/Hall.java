
public class Hall {

	private int auctionId;// id

	private String pic;// 图片地址

	private String desc;// 描述

	private String level;// 评级

	private String licence;// 车牌
	
	private String registDate;// 上牌日期

	private float distince;// 里程

	private String startTime;// 开始时间

	private String endTime;// 结束时间

	private String now;// 当前时间

	private float price;// 当前价格

	private int isInterest;// 是否关注:0-未关注,1-已关注,2-出过价,3 被超过

	private int offerMin;// 出价1

	private int offer;// 出价2

	private int offerMax;// 出价3
	private long residueTime;// 剩余时间自己算

	private float myPrice;// 我的出价

	private int status;// 0未开始,4开始,0已结束,

	private int type;// 是什么类型的 是交易成功还是 其他类型

	private int isInnerAuction;// 是否是内场拍的
	/** 所在城市 */

	private String city;
	/** 提车地址 */

	private String address;
	/** 需打款 可提车 **/

	private float carFund;
	/** 服务费 */

	private float serviceCost;
	/** 办证费 **/

	private float certCost;
	
	/** 代收费 **/

	private float agentFee;
	/** 出库费 **/

	private float outFee;
	/** 总费用 **/
	
	private float allCost;
	
	/** 金额 */

	private float amount;
	/** 用来区分是从哪个入口获得的数据 比如大厅的内场拍 正在竞拍 */

	private int requstStatus;
	public int getAuctionId() {
		return auctionId;
	}
	public void setAuctionId(int auctionId) {
		this.auctionId = auctionId;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getLicence() {
		return licence;
	}
	public void setLicence(String licence) {
		this.licence = licence;
	}
	public String getRegistDate() {
		return registDate;
	}
	public void setRegistDate(String registDate) {
		this.registDate = registDate;
	}
	public float getDistince() {
		return distince;
	}
	public void setDistince(float distince) {
		this.distince = distince;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getNow() {
		return now;
	}
	public void setNow(String now) {
		this.now = now;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public int getIsInterest() {
		return isInterest;
	}
	public void setIsInterest(int isInterest) {
		this.isInterest = isInterest;
	}
	public int getOfferMin() {
		return offerMin;
	}
	public void setOfferMin(int offerMin) {
		this.offerMin = offerMin;
	}
	public int getOffer() {
		return offer;
	}
	public void setOffer(int offer) {
		this.offer = offer;
	}
	public int getOfferMax() {
		return offerMax;
	}
	public void setOfferMax(int offerMax) {
		this.offerMax = offerMax;
	}
	public long getResidueTime() {
		return residueTime;
	}
	public void setResidueTime(long residueTime) {
		this.residueTime = residueTime;
	}
	public float getMyPrice() {
		return myPrice;
	}
	public void setMyPrice(float myPrice) {
		this.myPrice = myPrice;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getIsInnerAuction() {
		return isInnerAuction;
	}
	public void setIsInnerAuction(int isInnerAuction) {
		this.isInnerAuction = isInnerAuction;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public float getCarFund() {
		return carFund;
	}
	public void setCarFund(float carFund) {
		this.carFund = carFund;
	}
	public float getServiceCost() {
		return serviceCost;
	}
	public void setServiceCost(float serviceCost) {
		this.serviceCost = serviceCost;
	}
	public float getCertCost() {
		return certCost;
	}
	public void setCertCost(float certCost) {
		this.certCost = certCost;
	}
	public float getAgentFee() {
		return agentFee;
	}
	public void setAgentFee(float agentFee) {
		this.agentFee = agentFee;
	}
	public float getOutFee() {
		return outFee;
	}
	public void setOutFee(float outFee) {
		this.outFee = outFee;
	}
	public float getAllCost() {
		return allCost;
	}
	public void setAllCost(float allCost) {
		this.allCost = allCost;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public int getRequstStatus() {
		return requstStatus;
	}
	public void setRequstStatus(int requstStatus) {
		this.requstStatus = requstStatus;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
}
