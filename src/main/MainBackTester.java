package main;
import java.util.Date;
import java.util.List;
import java.util.Map;

import trade.CloseTrade;
import trade.CoveredStraddle;
import trade.OpenTrade;
import trade.Report;
import trade.manager.CoveredCallTradeManager;
import misc.Utils;
import model.service.DbService;


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
		
		switch (TradeProperties.TRADE_TYPE) {
		case "COVERED_CALL":
			bt.coveredCallBackTest();
			break;
		case "COVERED_STRADDLE":
			bt.coveredStraddleBackTest();
			break;
		case "IRON_CONDOR":
			bt.ironCondorBackTest();
			break;
		case "SHORT_PUT_SPREAD":
			bt.shortPutSpreadTest();
			break;
		default:
			System.err.println("Unknown trade type: " + TradeProperties.TRADE_TYPE);
			break;
		}
	}
	
	
	private void ironCondorBackTest() {
	
		//double[] spreadWidths = { TradeProperties.SPREAD_WIDTH }; 
		double[] spreadWidths = { 5 }; //, 10, 25, 50 };
		//double[] spreadWidths = { 1.0, 2.0, 3.0, 5.0, 10 };
		//double[] deltas = {TradeProperties.OPEN_DELTA};
		double[] deltas = {0.0228}; //, 0.0668, 0.1, 0.1587, 0.2, 0.3, 0.35}; 
		for (double delta : deltas) {
		
			for (double spreadWidth : spreadWidths) {
				
				DbService.resetDataBase();
				
				Map<Date, Date> potentialTrades = Utils.getPotentialTrades(TradeProperties.OPEN_DTE);		
				
				for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
				    Date tradeDate = tradeDateSet.getKey();
				    Date expiration = tradeDateSet.getValue();
				    
				    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) + " expiration: " + Utils.asMMMddYYYY(expiration));
				    OpenTrade.findIronCondorChains(tradeDate, expiration, delta, spreadWidth);
				}
					
				CloseTrade.closeTrades();
				
				Report.buildIronCondorReport(delta, spreadWidth);
			}
		}
		
		System.out.println("Finished!");
	}


	private void coveredCallBackTest() {

		boolean useWeekly = false;

		// A value of 0 will use dynamic deltas - NOT IMPLEMENTED YET
		//double[] deltas = {TradeProperties.OPEN_DELTA};
		double[] deltas = {0.3};
		//double[] deltas = {0.6, 0.5, 0.4, 0.3, 0.2, 0.1};
		
		int[] dtes = {TradeProperties.OPEN_DTE};
		//int[] dtes = { 8, 15, 22, 29, 36, 43 };
		//int[] dtes = { 28, 45, 60 };
		
		for (double delta : deltas) {
			for (int dte : dtes) {
				
				DbService.resetDataBase();
				
				List<Date> expirations = null;
				
				if (useWeekly) {
					expirations = Utils.getExpirations();
				} else {
					expirations = Utils.getMonthlyExpirations();
				}
				
				for (Date expiration : expirations) {
					OpenTrade.coveredCall(expiration, dte, delta);
				}
				System.out.println("================= All trades opened - checking closing trade =================");
				
				CloseTrade.closeTrades();
				
				Report.buildCoveredCallReport(delta, dte);
			}
		}
		

		System.out.println("Finished!");
	}

	private void coveredStraddleBackTest() {

		boolean useWeekly = false;
		//int[] dtes = { 8, 15, 22, 29, 36, 43 };
		int[] dtes = { 15 };
		Date leapExpiration = null;
		double initialDelta = TradeProperties.OPEN_DELTA;
		
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
			
			CloseTrade.closeTrades();
			
			Report.buildCoveredStraddleReport(dte);
		}
		

		System.out.println("Finished!");
	}


	private void shortPutSpreadTest() {
		
		//double[] spreadWidths = { TradeProperties.SPREAD_WIDTH }; 
		double[] spreadWidths = { 5, 10, 25 };
		//double[] spreadWidths = { 1.0, 2.0, 3.0, 5.0, 10 };
		//double[] deltas = {TradeProperties.OPEN_DELTA};
		//double[] deltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.3}; 
		//double[] deltas = {0.0228, 0.0668};
		double[] deltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.3};
		//double[] deltas = {0.25};
		//int[] openDte = {TradeProperties.OPEN_DTE};
		int[] openDte = {7, 14, 28, 45};
		
		for (int dte :  openDte) {
			
			for (double delta : deltas) {
			
				for (double spreadWidth : spreadWidths) {
					
					DbService.resetDataBase();
					
					Map<Date, Date> potentialTrades = Utils.getPotentialWeeklyTrades(dte);		
					
					for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
					    Date tradeDate = tradeDateSet.getKey();
					    Date expiration = tradeDateSet.getValue();
					    
					    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) + " expiration: " + Utils.asMMMddYYYY(expiration));
					    OpenTrade.findShortPutSpread(tradeDate, expiration, delta, spreadWidth);
					}
						
					CloseTrade.closeTrades();
					
					Report.shortPutSpreadReport(delta, spreadWidth, dte);
				}
			}
		}
		
		System.out.println("Finished!");
	}





}
