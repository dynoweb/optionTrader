package main;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import main.TradeProperties.TradeType;
import trade.Butterfly;
import trade.CloseTrade;
import trade.CoveredStraddle;
import trade.OpenTrade;
import trade.Report;
import trade.manager.CoveredCallTradeManager;
import misc.Utils;
import model.OptionPricing;
import model.service.DbService;
import model.service.TradeService;


public class MainBackTester {

	public static void main(String[] args) {
		
		MainBackTester bt = new MainBackTester();
		
		
//		SpxService ss = new SpxService();
//		List<Date> expirations = ss.getExpirationDates(openTradeDate);
//		
//		for (Date optionsExpiration : expirations) {
//			
//			long diff = optionsExpiration.getTime() - openTradeDate.getTime();
//		    System.out.println ("openTradeDate: "  + openTradeDate + " optionsExpiration: " + optionsExpiration + " Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
//		}
		
		DateTime start = new DateTime();
		
		switch (TradeProperties.tradeType) {
		case CALENDAR:
			bt.calendarBackTest();
			break;
		case COVERED_CALL:
			bt.coveredCallBackTest();
			break;
		case COVERED_STRADDLE:
			bt.coveredStraddleBackTest();
			break;
		case IRON_CONDOR:
			bt.ironCondorBackTest();
			break;
		case SHORT_CALL:
			bt.shortCallBackTest();
			break;
		case SHORT_CALL_SPREAD:
			bt.shortCallSpreadTest();
			break;
		case SHORT_PUT:
			bt.shortPutBackTest();
			break;
		case SHORT_PUT_SPREAD:
			bt.shortPutSpreadTest();
			break;
		case BWBF_20_40_60:
			bt.bwbf_20_40_60_Test();
			break;
		default:
			System.err.println("Unknown trade type: " + TradeProperties.tradeType);
			break;
		}
		
		System.out.println("Processing time: " + new Duration(start, new DateTime()));
	}
	
	/**
	 * This trade opens a Broken Wing Butterfly x dte, with the legs at 20, 40 & 60 delta.  It closes
	 * the trade when the short strike delta goes out of range.
	 */
	private void bwbf_20_40_60_Test() {

		boolean useWeekly = true;
		//int[] dtes = { 16,23,30,37,44 };	// trade results not real promising for these periods
		//int[] dtes = { 51,58,65,72,79 };
		int[] dtes = { 70 };
		//int[] dtes = {TradeProperties.OPEN_DTE};

		//double[] lowerDeltas = { TradeProperties.LOWER_DELTA };
		//double[] upperDeltas = { TradeProperties.UPPER_DELTA };
		//double[] lowerDeltas = { 0.05, 0.10, 0.15, 0.20, 0.25, 0.30 };
		//double[] upperDeltas = { 0.50, 0.55, 0.60, 0.65, 0.70, 0.75 };
		double[] lowerDeltas = { 0.15 };
//		double[] upperDeltas = { 0.55, 0.60, 0.70 };
		double[] upperDeltas = { 0.65 };
		
		for (double lowerDelta : lowerDeltas) {
			for (double upperDelta : upperDeltas) {
				for (int dte : dtes) {
					
					DbService.resetDataBase();
					
					List<Date> expirations = null;
					
					if (useWeekly) {
						expirations = Utils.getExpirations();
					} else {
						expirations = Utils.getMonthlyExpirations();				
					}
						
					//Butterfly brokenWingButterfly = null;
					for (Date expiration : expirations) {
						OpenTrade.open_20_40_60_Butterfly(expiration, dte);
					}
					CloseTrade.closeTrades(lowerDelta, upperDelta);
					
					Report.build_20_40_60_ButterflyReport(dte, lowerDelta, upperDelta);			
				}
			}
		}
		System.out.println("Finished!");
	}

	/**
	 * Test the calendar trade
	 */
	private void calendarBackTest() {
		
		double[] shortDeltas = {TradeProperties.SHORT_DELTA};
		//double[] deltas = {0.45, 0.4, 0.35, 0.3, 0.25, 0.20, 0.15, 0.1};
		//double[] shortDeltas = {0.10};
		
		// Profit target is in percentage
		double[] profitTargets = {TradeProperties.PROFIT_TARGET};
		//double[] profitTargets = {0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5};
		//double[] profitTargets = {0.1, 0.15, 0.2, 0.25, 0.3};
		//double[] profitTargets = {0.35, 0.4, 0.45, 0.5};
		
		int[] closeDtes = {TradeProperties.CLOSE_DTE};
		
		// maxLoss is in percentage
		double[] maxLosses = {TradeProperties.MAX_LOSS};
		//double[] maxLosses = { 0.50 };
		//double[] maxLosses = {0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5};
		
		int nearDte = 9;
		int farDte = 16;
		String putCall = "C";
		
		for (double shortDelta : shortDeltas) {
			
			for (double maxLoss : maxLosses) {
				
				for (double profitTarget : profitTargets) {
					
					DbService.resetDataBase();
					
					OpenTrade.openCalendar(nearDte, farDte, shortDelta, putCall);
					CloseTrade.closeTrades(profitTarget, closeDtes[0], maxLoss);
					
					Report.buildCalendarReport(profitTarget, maxLoss, nearDte, farDte, closeDtes[0], shortDelta, putCall);
				}
			}
		}
	}

	private void coveredCallBackTest() {

		boolean useWeekly = false;

		// A value of 0 will use dynamic deltas - NOT IMPLEMENTED YET
		double[] deltas = {TradeProperties.SHORT_DELTA};
		//double[] deltas = {0.3};
		//double[] deltas = {0.6, 0.5, 0.4, 0.3, 0.2, 0.1};
		
		int[] dtes = {TradeProperties.OPEN_DTE};
		//int[] dtes = { 8, 15, 22, 29, 36, 43 };
		//int[] dtes = { 28, 45, 60 };
		
		for (double shortDelta : deltas) {
			for (int dte : dtes) {
				
				DbService.resetDataBase();
				
				List<Date> expirations = null;
				
				if (useWeekly) {
					expirations = Utils.getExpirations();
				} else {
					expirations = Utils.getMonthlyExpirations();
				}
				
				for (Date expiration : expirations) {
					OpenTrade.coveredCall(expiration, dte, shortDelta);
				}
				System.out.println("================= All trades opened - checking closing trade =================");
				
				CloseTrade.closeTrades(TradeProperties.tradeType, 0);
				
				Report.buildCoveredCallReport(shortDelta, dte);
			}
		}
		

		System.out.println("Finished!");
	}


	private void coveredStraddleBackTest() {

		boolean useWeekly = false;
		//int[] dtes = { 8, 15, 22, 29, 36, 43 };
		int[] dtes = { 15 };
		Date leapExpiration = null;
		double initialDelta = TradeProperties.SHORT_DELTA;
		
		for (int dte : dtes) {
			
			DbService.resetDataBase();
			
			List<Date> expirations = null;
			CoveredStraddle coveredStraddle = null;
			
			expirations = Utils.getMonthlyExpirations();
			if (expirations.size() > 0) {
				leapExpiration = expirations.get(expirations.size() -1);
			}
			if (useWeekly) {
				expirations = Utils.getExpirations();
			} 
				
			
			
			for (Date expiration : expirations) {
				coveredStraddle = OpenTrade.initializeCoveredStraddle(expiration, dte, initialDelta);
				if (coveredStraddle != null) {
					break;
				}
			}
			CoveredCallTradeManager tm = new CoveredCallTradeManager(coveredStraddle, expirations, dte, initialDelta);
			tm.manageTrade();
			
			CloseTrade.closeTrades(TradeProperties.tradeType, 0);
			
			Report.buildCoveredStraddleReport(dte);
		}
		

		System.out.println("Finished!");
	}

	private void ironCondorBackTest() {
	
		boolean useWeekly = true;
		
		double[] spreadWidths = { TradeProperties.SPREAD_WIDTH }; 
		//double[] spreadWidths = { 5, 10, 25, 50, 100 };
		//double[] spreadWidths = { 1.0, 2.0, 3.0, 5.0, 10.0 };
		//double[] spreadWidths = { 3.0, 5.0 };
		//double[] spreadWidths = { 10, 20, 50 };				// good for RUT
		
		double[] shortDeltas = {TradeProperties.SHORT_DELTA};
		//double[] shortDeltas = {0.1, 0.1587, 0.2, 0.3, 0.35};
		//double[] shortDeltas = {0.1, 0.1587, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45};
		//double[] shortDeltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.25};
		//double[] shortDeltas = {0.4, 0.45};
		//double[] shortDeltas = {0.1587, 0.2, 0.3};
		
		double[] longDeltas = {TradeProperties.LONG_DELTA};
		//double[] longDeltas = {0.1, 0.05};
		
		int[] openDte = {TradeProperties.OPEN_DTE};
		//int[] openDte = {120, 60, 45};
		//int[] openDte = {7,14,28,45,60,120};
		//int[] openDte = {28,45,60,120};
		
		for (int dte :  openDte) {
			
			for (double shortDelta : shortDeltas) {
				
				for (double longDelta : longDeltas) {
					
					for (double spreadWidth : spreadWidths) {
						
						if (spreadWidth != 0 && longDelta != 0) {
							System.err.println("Choose either longDelta or spreadWith");
							return;
						}
						
						DbService.resetDataBase();
						
						Map<Date, Date> potentialTrades = null;
						if (useWeekly) {
							potentialTrades = Utils.getPotentialWeeklyTrades(dte);
						} else {
							potentialTrades = Utils.getPotentialTrades(dte);
						}
						
						for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
						    Date tradeDate = tradeDateSet.getKey();
						    Date expiration = tradeDateSet.getValue();
						    
						    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) + " expiration: " + Utils.asMMMddYYYY(expiration));
						    OpenTrade.findIronCondorChains(tradeDate, expiration, shortDelta, longDelta, spreadWidth);
						}
						
						CloseTrade.closeTrades(TradeProperties.tradeType, spreadWidth);
						Report.buildIronCondorReport(shortDelta, longDelta, spreadWidth, dte, TradeProperties.PROFIT_TARGET, TradeProperties.MAX_LOSS);
					}
				}
			}
		}
		System.out.println("Finished!");
	}

	private void shortCallBackTest() {

		//double[] deltas = {TradeProperties.OPEN_DELTA};
		//double[] deltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.3}; 
		double[] deltas = {0.1, 0.1587, 0.2, 0.3}; 
		//double[] deltas = {0.0228, 0.0668};
		//double[] deltas = {0.25};
		
		double longDelta = 0.0;
		
		//int[] openDte = {TradeProperties.OPEN_DTE};
		int[] openDte = {7, 14, 28, 45};
		
		//double[] profitTargets = {TradeProperties.PROFIT_TARGET};
		//double[] profitTargets = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9}; 
		//double[] profitTargets = {0.3, 0.4, 0.5, 0.6, 0.7};
		//double[] profitTargets = {0.1, 0.3, 0.5, 0.7, 0.9};
		double[] profitTargets = {0.0, 0.5};		
		
		for (int dte :  openDte) {
			
			for (double delta : deltas) {
				
				for (double profitTarget : profitTargets) {
			
					DbService.resetDataBase();
					
					Map<Date, Date> potentialTrades = Utils.getPotentialWeeklyTrades(dte);		
					
					for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
					    Date tradeDate = tradeDateSet.getKey();
					    Date expiration = tradeDateSet.getValue();
					    
					    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) + " expiration: " + Utils.asMMMddYYYY(expiration));
					    OptionPricing shortOpt = OpenTrade.findShort(tradeDate, expiration, delta, "C");
					    if (shortOpt != null) {
					    	// write the opening Trade and TradeDetails to the database
					    	TradeService.recordShort(shortOpt);
					    }
					}
						
					CloseTrade.closeTrades(profitTarget, TradeProperties.CLOSE_DTE);
					
					Report.shortOptionReport(delta, longDelta, dte, profitTarget, TradeProperties.MAX_LOSS);
				}
			}
		}
		
		System.out.println("Finished!");
	}

	private void shortCallSpreadTest() {
		
		//double[] spreadWidths = { TradeProperties.SPREAD_WIDTH }; 
		//double[] spreadWidths = { 5, 10, 25, 50 };	    // good for SPX
		double[] spreadWidths = { 1.0, 2.0, 3.0, 5.0, 10.0 };	// good for SPY
		//double[] spreadWidths = { 1.0, 2.0, 3.0 };
	//	double[] spreadWidths = { 10, 20, 50 };				// good for RUT
		
		//double[] deltas = {TradeProperties.OPEN_DELTA};
		//double[] deltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.3};
		double[] deltas = {0.1, 0.1587, 0.2, 0.3};
		//double[] deltas = {0.0228, 0.0668};
		//double[] deltas = {0.25};
		
		double longDelta = 0.0;

		//int[] openDte = {TradeProperties.OPEN_DTE};
		int[] openDte = {7, 14, 21, 28, 45};
		//int[] openDte = {7, 14};
		//int[] openDte = {28, 45};

		
		for (int dte :  openDte) {
			
			for (double shortDelta : deltas) {
			
				for (double spreadWidth : spreadWidths) {
					
					DbService.resetDataBase();
					
					Map<Date, Date> potentialTrades = Utils.getPotentialWeeklyTrades(dte);		
					
					for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
					    Date tradeDate = tradeDateSet.getKey();
					    Date expiration = tradeDateSet.getValue();
					    
					    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) + " expiration: " + Utils.asMMMddYYYY(expiration));
					    OpenTrade.findShortOptionSpread(tradeDate, expiration, shortDelta, longDelta, spreadWidth, "C");
					}
					CloseTrade.closeTrades(TradeProperties.tradeType, 0);
					Report.shortSpreadReport(shortDelta, longDelta, spreadWidth, dte, TradeProperties.PROFIT_TARGET, TradeProperties.MAX_LOSS);
				}
			}
		}
		
		System.out.println("Finished!");
	}

	private void shortPutBackTest() {

		double[] shortDeltas = {TradeProperties.SHORT_DELTA};
		//double[] shortDeltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.3}; 
		//double[] shortDeltas = {0.1, 0.1587, 0.2, 0.3}; 
		//double[] shortDeltas = {0.1, 0.1587, 0.2};
		//double[] shortDeltas = {0.0228, 0.0668};
		//double[] shortDeltas = {0.1587};
		
		double longDelta = 0.0;
		
		int[] openDte = {TradeProperties.OPEN_DTE};
		//int[] openDte = {7, 14, 28, 45};
		//int[] openDte = {8, 9};
		
		double[] profitTargets = {TradeProperties.PROFIT_TARGET};
		//double[] profitTargets = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9}; 
		//double[] profitTargets = {0.3, 0.4, 0.5, 0.6, 0.7};
		//double[] profitTargets = {0.1, 0.3, 0.5, 0.7, 0.9};
		//double[] profitTargets = {0.0, 0.5};
		
		for (int dte :  openDte) {
			
			for (double shortDelta : shortDeltas) {
				
				for (double profitTarget : profitTargets) {
			
					DbService.resetDataBase();
					
					Map<Date, Date> potentialTrades = Utils.getPotentialWeeklyTrades(dte);		
					
					for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
					    Date tradeDate = tradeDateSet.getKey();
					    Date expiration = tradeDateSet.getValue();
					    
					    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) + " expiration: " + Utils.asMMMddYYYY(expiration));
					    OptionPricing shortOpt = OpenTrade.findShort(tradeDate, expiration, shortDelta, "P");
					    if (shortOpt != null) {
					    	// write the opening Trade and TradeDetails to the database
					    	TradeService.recordShort(shortOpt);
					    }
					}
						
					CloseTrade.closeTrades(profitTarget, TradeProperties.CLOSE_DTE);
					
					Report.shortOptionReport(shortDelta, longDelta, dte, profitTarget, TradeProperties.MAX_LOSS);
				}
			}
		}
		
		System.out.println("Finished!");
	}

	private void shortPutSpreadTest() {
		
		double[] spreadWidths = { TradeProperties.SPREAD_WIDTH }; 
		//double[] spreadWidths = { 10, 25, 50, 100 };	// good for SPX
		//double[] spreadWidths = { 1.0, 2.0, 3.0, 5.0, 10.0 };	// good for SPY
		//double[] spreadWidths = { 1.0, 2.0, 3.0, 5.0 };
		//double[] spreadWidths = { 10, 20, 50 };				// good for RUT
		//double[] spreadWidths = { 100 };
		
		double[] shortDeltas = {TradeProperties.SHORT_DELTA};
		//double[] deltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45};
		//double[] deltas = {0.1, 0.1587, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45};
		//double[] deltas = {0.0668, 0.1, 0.1587, 0.2, 0.25, 0.3};
		//double[] deltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.25, 0.3};
		//double[] deltas = {0.2, 0.25, 0.35, 0.4, 0.45};
		//double[] deltas = {0.1, 0.1587, 0.2, 0.25, 0.3};
		//double[] deltas = {0.1, 0.1587, 0.2, 0.3};
		//double[] deltas = {0.0228, 0.0668};
		//double[] deltas = {0.25};

		double longDelta = 0;

		int[] openDte = {TradeProperties.OPEN_DTE};
		//int[] openDte = {7, 14, 28, 45};
		//int[] openDte = {7, 14, 21, 28, 45};
		//int[] openDte = {28, 45};
		
		
		for (int dte :  openDte) {
			
			for (double shortDelta : shortDeltas) {
			
				for (double spreadWidth : spreadWidths) {
					
					DbService.resetDataBase();
					
					Map<Date, Date> potentialTrades = Utils.getPotentialWeeklyTrades(dte);		
					
					for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
					    Date tradeDate = tradeDateSet.getKey();
					    Date expiration = tradeDateSet.getValue();
					    
					    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) + " expiration: " + Utils.asMMMddYYYY(expiration));
					    OpenTrade.findShortOptionSpread(tradeDate, expiration, shortDelta, longDelta, spreadWidth, "P");
					}
					// TODO - may need to calculate close date if CLOSE_DTE is != 0 so dates after are not checked for profit target
					CloseTrade.closeTrades(TradeProperties.tradeType, spreadWidth);
					
					Report.shortSpreadReport(shortDelta, longDelta, spreadWidth, dte, TradeProperties.PROFIT_TARGET, TradeProperties.MAX_LOSS);
				}
			}
		}
		
		System.out.println("Finished!");
	}






}
