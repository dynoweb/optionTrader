package main;

public class TradeProperties {
	
	public enum TradeType {
		COVERED_CALL("Covered Call"), COVERED_STRADDLE("Covered Straddle"), IRON_CONDOR("Iron Condor"), 
		SHORT_CALL_SPREAD("Short Call Spread"), SHORT_PUT_SPREAD("Short Put Spread"), 
		SHORT_CALL("Short Call"), SHORT_PUT("Short Put"), CALENDAR("Calendar") ;
		
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

	public static TradeType tradeType = TradeType.SHORT_PUT_SPREAD;  
	
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
	public static double OPEN_DELTA = 0.25;
	public static int OPEN_DTE = 7;
	public static double SPREAD_WIDTH = 100.0;
	
	// Close trade properties
	public static double PROFIT_TARGET = 0.0;  // 0.50 - 50% of max potential
	public static double MAX_LOSS = 0;  // 2.0 - 200%
	public static int CLOSE_DTE = 0;	// 1 is the default
	public static double CLOSE_DELTA_TARGET = 0.0;
}
