import java.util.Date;
import java.util.Map;

import trade.CloseTrade;
import trade.OpenTrade;
import trade.Report;
import trade.TradeProperties;
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
		
		bt.runBackTest();
	}
	
	
	private void runBackTest() {
	
		DbService.resetDataBase();
		
		Map<Date, Date> potentialTrades = Utils.getPotentialTrades(TradeProperties.OPEN_DTE);		
		
		for (Map.Entry<Date, Date> tradeDateSet : potentialTrades.entrySet()) {
		    Date tradeDate = tradeDateSet.getKey();
		    Date expiration = tradeDateSet.getValue();
		    
		    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) + " expiration: " + Utils.asMMMddYYYY(expiration));
		    OpenTrade.findIronCondorChains(tradeDate, expiration);
		}
			
		CloseTrade.closeTrades();
		
		Report.showTradeResults();
	}




}
