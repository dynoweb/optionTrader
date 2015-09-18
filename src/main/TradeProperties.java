package main;

public class TradeProperties {

	public static final String SYMBOL = "SPX";
	public static final int CONTRACTS = 1;

	// 68.27%, 95.45% and 99.73% of the values lie within one, two and three standard deviations of the mean
	// or 
	// Delta = 0.1587 - 1 SD
	// Delta = 0.0668 - 1.5 SD
	// Delta = 0.0228 - 2 SD
	//
	// Open trade properties
	public static double OPEN_DELTA = 0.10;
	public static int OPEN_DTE = 45;
	public static int SPREAD_WIDTH = 20;
	
	// Close trade properties
	public static double PROFIT_TARGET = 0.75;  // 0.50 - 50% of max potential
	public static double MAX_LOSS = 0;  // 2.0 - 200%
	public static int CLOSE_DTE = 8;	// 8 is the default
}
