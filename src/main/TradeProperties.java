package main;

public class TradeProperties {

	// ["COVERED_CALL", "COVERED_STRADDLE", "IRON_CONDOR" 
	// "SHORT_CALL_SPREAD", "SHORT_PUT_SPREAD", "SHORT_CALL", "SHORT_PUT"]
	public static final String TRADE_TYPE="IRON_CONDOR";  
	
//	public static final String SYMBOL = "EEM";
//	public static final String SYMBOL = "IWM";
//	public static final String SYMBOL = "RUT";
	public static final String SYMBOL = "SPX";
//	public static final String SYMBOL = "SPY";
//	public static final String SYMBOL = "TLT";
	
	// Name of the Entity Class
	public static final String SYMBOL_FOR_QUERY = "Spx";
	public static final int CONTRACTS = 1;

	// 68.27%, 95.45% and 99.73% of the values lie within one, two and three standard deviations of the mean
	// or 
	// Delta = 0.1587 - 1 SD
	// Delta = 0.0668 - 1.5 SD
	// Delta = 0.0228 - 2 SD
	//
	// Open trade properties
	public static double OPEN_DELTA = 0.1587;
	public static int OPEN_DTE = 7;
	public static double SPREAD_WIDTH = 50.0;
	
	// Close trade properties
	public static double PROFIT_TARGET = 0.0;  // 0.50 - 50% of max potential
	public static double MAX_LOSS = 0.0;  // 2.0 - 200%
	public static int CLOSE_DTE = 0;	// 8 is the default
	public static double CLOSE_DELTA_TARGET = 0.0;
}
