
public class Hall {

	private int auctionId;// id

	private String pic;// ͼƬ��ַ

	private String desc;// ����

	private String level;// ����

	private String licence;// ����
	
	private String registDate;// ��������

	private float distince;// ���

	private String startTime;// ��ʼʱ��

	private String endTime;// ����ʱ��

	private String now;// ��ǰʱ��

	private float price;// ��ǰ�۸�

	private int isInterest;// �Ƿ��ע:0-δ��ע,1-�ѹ�ע,2-������,3 ������

	private int offerMin;// ����1

	private int offer;// ����2

	private int offerMax;// ����3
	private long residueTime;// ʣ��ʱ���Լ���

	private float myPrice;// �ҵĳ���

	private int status;// 0δ��ʼ,4��ʼ,0�ѽ���,

	private int type;// ��ʲô���͵� �ǽ��׳ɹ����� ��������

	private int isInnerAuction;// �Ƿ����ڳ��ĵ�
	/** ���ڳ��� */

	private String city;
	/** �ᳵ��ַ */

	private String address;
	/** ���� ���ᳵ **/

	private float carFund;
	/** ����� */

	private float serviceCost;
	/** ��֤�� **/

	private float certCost;
	
	/** ���շ� **/

	private float agentFee;
	/** ����� **/

	private float outFee;
	/** �ܷ��� **/
	
	private float allCost;
	
	/** ��� */

	private float amount;
	/** ���������Ǵ��ĸ���ڻ�õ����� ����������ڳ��� ���ھ��� */

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
