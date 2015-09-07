import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import trade.OpenTrade;
import trade.Report;
import trade.TradeProperties;
import misc.Utils;
import model.Spx;
import model.service.SpxService;


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
		
		bt.runBackTest(TradeProperties.SYMBOL, TradeProperties.CONTRACTS);
	}
	
	
	private void runBackTest(String symbol, int contracts) {
	
		Map<Date, Date> potentialTrades = Utils.getPotentialTrades(TradeProperties.OPEN_DTE);
		
		String callPut;
		
		for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
		    Date tradeDate = tradeDateSet.getKey();
		    Date expiration = tradeDateSet.getValue();
		    
		    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) 
	    			+ " expiration: " + Utils.asMMMddYYYY(expiration));
		    
		    OpenTrade.findIronCondorChains(tradeDate, expiration);
		}
			
//		CloseTrade.closeTrades(symbol);
		
		Report.showTradeResults();
	}



}
