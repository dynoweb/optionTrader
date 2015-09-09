package main;

public class TradeProperties {

	public static final String SYMBOL = "SPX";
	public static final int CONTRACTS = 1;
	
	// Open trade properties
	public static double OPEN_DELTA = 0.10;
	public static int OPEN_DTE = 45;
	public static int SPREAD_WIDTH = 20;
	
	// Close trade properties
	public static double MAX_LOSS = 2.0;  // 2.0 - 200%
	public static int CLOSE_DTE = 8;
	public static double PROFIT_TARGET = 0.5;  // 0.50 - 50% of max potential
}
