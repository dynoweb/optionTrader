package main;

public class TradeProperties {
	
	public enum TradeType {
		COVERED_CALL("Covered Call"), COVERED_STRADDLE("Covered Straddle"), IRON_CONDOR("Iron Condor"), 
		SHORT_CALL_SPREAD("Short Call Spread"), SHORT_PUT_SPREAD("Short Put Spread"), 
		SHORT_CALL("Short Call"), SHORT_PUT("Short Put"), CALENDAR("Calendar"), BWBF_20_40_60("20-40-60"),
		DOUBLE_DIAGONAL("Double Diagonal");
		
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

	public static TradeType tradeType = TradeType.SHORT_PUT;  
	
	// Name of the Entity Class
//	public static final String SYMBOL_FOR_QUERY = "Aapl";
//	public static final String SYMBOL_FOR_QUERY = "Amd";
//	public static final String SYMBOL_FOR_QUERY = "Amzn";
//	public static final String SYMBOL_FOR_QUERY = "Ba";
//	public static final String SYMBOL_FOR_QUERY = "C";
//	public static final String SYMBOL_FOR_QUERY = "Cost";
//	public static final String SYMBOL_FOR_QUERY = "Dia";
//	public static final String SYMBOL_FOR_QUERY = "Eem";
//	public static final String SYMBOL_FOR_QUERY = "Fas";
//	public static final String SYMBOL_FOR_QUERY = "Fb";
//	public static final String SYMBOL_FOR_QUERY = "Gld";
//	public static final String SYMBOL_FOR_QUERY = "Goog";
//	public static final String SYMBOL_FOR_QUERY = "Iwm";
//	public static final String SYMBOL_FOR_QUERY = "Jpm";
//	public static final String SYMBOL_FOR_QUERY = "Mcd";
//	public static final String SYMBOL_FOR_QUERY = "Msft";
//	public static final String SYMBOL_FOR_QUERY = "Ndx";
//	public static final String SYMBOL_FOR_QUERY = "Nflx";
//	public static final String SYMBOL_FOR_QUERY = "Qqq";
//	public static final String SYMBOL_FOR_QUERY = "Rut";
/////	public static final String SYMBOL_FOR_QUERY = "Rvx";
//	public static final String SYMBOL_FOR_QUERY = "Slv";
//	public static final String SYMBOL_FOR_QUERY = "Spx";
//	public static final String SYMBOL_FOR_QUERY = "Spy";
//	public static final String SYMBOL_FOR_QUERY = "Tlt";
//	public static final String SYMBOL_FOR_QUERY = "Tna";
	public static final String SYMBOL_FOR_QUERY = "Tqqq";
	
	public static final String SYMBOL = SYMBOL_FOR_QUERY.toUpperCase();
	public static final int CONTRACTS = 1;

	// cost to open or close 1 contract. Being a cost it should be negative
	public static final double COST_PER_CONTRACT_FEE = -1.00;
	public static final double COST_PER_STOCK_TRADE_FEE = 0.0;

	// 68.27%, 95.45% and 99.73% of the values lie within one, two and three standard deviations of the mean
	// or 
	// Delta = 0.1587 - 1 SD
	// Delta = 0.0668 - 1.5 SD
	// Delta = 0.0228 - 2 SD
	//
	// Open trade properties
	public static double SHORT_DELTA = 0.2;
	public static double LONG_DELTA = 0.0;
	public static int OPEN_DTE = 7;
	public static double SPREAD_WIDTH = 10;
	
	// Close trade properties
	public static double PROFIT_TARGET = 0.0;  // 0.50 - 50% of max potential
	public static double MAX_LOSS = 0;  // .30 30%, 2.0 - 200%
	public static double CLOSE_DELTA_TARGET = 0.0;
	public static int CLOSE_DTE = 0;	// 0 is the default, 21 days seems to work well with 45 DTE opens
	public static double UPPER_DELTA = 0.60;	// uses absolute values
	public static double MID_DELTA = 0.40;
	public static double LOWER_DELTA = 0.20;
}
