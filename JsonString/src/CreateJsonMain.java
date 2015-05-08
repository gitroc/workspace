
public class CreateJsonMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Man man = new Man();
		
		man.setResult(1);
		man.setErrMsg("hello roc!");
		
		System.out.println(man.toString());
		
		
		Hall auction = new Hall();
		
		auction.setAuctionId(159);
        auction.setAddress("");
        auction.setAmount(10000);
        auction.setCity("沪");
        auction.setNow(SimpleTimeUtil.longToDate(System.currentTimeMillis() + ""));
        auction.setDesc("本田思域 1.8 自动 LXi 经典版");
        auction.setStartTime("2014-11-02 23:29:00");
        auction.setPic("http://img.pre.chexiang.com/img2/63267e91d661e550/20141102/34b7ebeaf8bd4f45bc9d8f39fee3a881.png");
        auction.setEndTime("2014-11-02 23:49:00");
        auction.setOfferMin(100);
        auction.setOffer(200);
        auction.setOfferMax(500);
        auction.setLevel("6S");
        auction.setPrice(100000);

	}

}
