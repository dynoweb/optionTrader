import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import trade.OpenTrade;
import trade.Report;
import trade.TradeProperties;
import misc.ProjectProperties;
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
		
		String symbol = "Spx";
		int contracts = 1;
		bt.runBackTest("symbol", contracts);
	}
	
	
	private void runBackTest(String symbol, int contracts) {
	
		Map<Date, Date> potentialTrades = Utils.getPotentialTrades(TradeProperties.OPEN_DTE);
		
		String callPut;
		
		for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
		    Date tradeDate = tradeDateSet.getKey();
		    Date expiration = tradeDateSet.getValue();
		    
		    System.out.println("checking: tradeDate: "  + ProjectProperties.dateFormat.format(tradeDate) 
	    			+ " expiration: " + ProjectProperties.dateFormat.format(expiration));
		    SpxService spxService = new SpxService();
		    callPut = "C";
		    List<Spx> spxCallChain = spxService.getOptionChain(tradeDate, expiration, callPut);
		    
		    if (spxCallChain.isEmpty()) {
		    	
		    	Calendar expirationDateCal = Calendar.getInstance();
		    	expirationDateCal.clear();		    	
		    	expirationDateCal.setTime(expiration);
		    	expirationDateCal.add(Calendar.DATE, 1);
		    	expiration = expirationDateCal.getTime();
		    	
			    System.out.println("checking: tradeDate: "  + ProjectProperties.dateFormat.format(tradeDate) 
		    			+ " expiration: " + ProjectProperties.dateFormat.format(expiration));
		    	spxCallChain = spxService.getOptionChain(tradeDate, expiration, callPut);
		    }
		    
//		    for (Spx spx : spxCallChain) {
//				
//		    	System.out.println("runBackTest: tradeDate: "  + ProjectProperties.dateFormat.format(spx.getTrade_date()) 
//		    			+ " expiration: " + ProjectProperties.dateFormat.format(spx.getExpiration()) + " Delta: " + spx.getDelta());
//			}
		    
		    if (!spxCallChain.isEmpty()) {
			    callPut = "P";
			    List<Spx> spxPutChain = spxService.getOptionChain(tradeDate, expiration, callPut);
			    OpenTrade.openIronCondor(spxCallChain, spxPutChain, contracts);
		    }
		}
			
//		CloseTrade.closeTrades(symbol);
		
		Report.showTradeResults();
	}
	

}
