package main;

public class TradeProperties {
	
	public enum TradeType {
		COVERED_CALL("Covered Call"), COVERED_STRADDLE("Covered Straddle"), IRON_CONDOR("Iron Condor"), 
		SHORT_CALL_SPREAD("Short Call Spread"), SHORT_PUT_SPREAD("Short Put Spread"), 
		SHORT_CALL("Short Call"), SHORT_PUT("Short Put"), CALENDAR("Calendar"), ;
		
		private String tradeName;
		
		private TradeType(String tradeName) {
			this.setTradeName(tradeName);
		}

		public String getTradeName() {
			return tradeName;
		}

		public void setTradeName(String tradeName) {
			this.tradeName = tradeName;
		}
	}

//	public static final int COVERED_CALL = 1;
//	public static final int COVERED_STRADDLE = 2;
//	public static final int IRON_CONDOR = 3;
//	public static final int SHORT_CALL_SPREAD = 4;
//	public static final int SHORT_PUT_SPREAD = 5;
//	public static final int SHORT_CALL = 6;
//	public static final int SHORT_PUT = 7;
	
	// ["COVERED_CALL", "COVERED_STRADDLE", "IRON_CONDOR" 
	// "SHORT_CALL_SPREAD", "SHORT_PUT_SPREAD", "SHORT_CALL", "SHORT_PUT"]
	public static TradeType tradeType = TradeType.SHORT_CALL;  
	
//	public static final String SYMBOL = "EEM";
//	public static final String SYMBOL = "GLD";
//	public static final String SYMBOL = "IWM";
//	public static final String SYMBOL = "RUT";
	public static final String SYMBOL = "SPX";
//	public static final String SYMBOL = "SPY";
//	public static final String SYMBOL = "TLT";
	
	// Name of the Entity Class
	public static final String SYMBOL_FOR_QUERY = "Spx";
	public static final int CONTRACTS = 1;

	// cost to open or close 1 contract. Being a cost it should be negative
	public static final double COST_PER_CONTRACT_FEE = -1.50;
	public static final double COST_PER_STOCK_TRADE_FEE = -9.99;

	// 68.27%, 95.45% and 99.73% of the values lie within one, two and three standard deviations of the mean
	// or 
	// Delta = 0.1587 - 1 SD
	// Delta = 0.0668 - 1.5 SD
	// Delta = 0.0228 - 2 SD
	//
	// Open trade properties
	public static double OPEN_DELTA = 0.1587;
	public static int OPEN_DTE = 45;
	public static double SPREAD_WIDTH = 5.0;
	
	// Close trade properties
	public static double PROFIT_TARGET = 0.0;  // 0.50 - 50% of max potential
	public static double MAX_LOSS = 0.0;  // 2.0 - 200%
	public static int CLOSE_DTE = 0;	// 8 is the default
	public static double CLOSE_DELTA_TARGET = 0.0;
}
