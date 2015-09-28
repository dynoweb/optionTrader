package main;
import java.util.Date;
import java.util.List;
import java.util.Map;

import trade.CloseTrade;
import trade.OpenTrade;
import trade.Report;
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
		
		//bt.ironCondorBackTest();
		bt.coveredCallBackTest();
	}
	
	
	private void ironCondorBackTest() {
	
		double[] spreadWidths = { 1.0, 2.0, 3.0, 5.0, 10 };
		//double[] deltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.3}; 
		double[] deltas = {0.0228, 0.0668, 0.1, 0.1587, 0.2, 0.3}; 
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

		boolean useWeekly = true;
		int[] offsets = { -3, -2, -1, 0, 1, 2, 3, 4, 5 };
		//int[] offsets = { 0 };
		//int[] dtes = { 8, 15, 22, 29, 36, 43 };
		int[] dtes = { 15 };
		
		for (int offset : offsets) {
			for (int dte : dtes) {
				
				DbService.resetDataBase();
				
				List<Date> expirations = null;
				
				if (!useWeekly) {
					expirations = Utils.getMonthlyExpirations();
				} else {
					expirations = Utils.getExpirations();
				}
				
				for (Date expiration : expirations) {
					OpenTrade.coveredCall(expiration, dte, offset);
				}
				
				CloseTrade.closeTrades();
				
				Report.buildCoveredCallReport(offset, dte);
			}
		}
		

		System.out.println("Finished!");
	}




}
