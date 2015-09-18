package trade;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import main.TradeProperties;
import misc.Utils;
import model.OptionPricing;
import model.Trade;
import model.TradeDetail;
import model.service.OptionPricingService;
import model.service.TradeDetailService;
import model.service.TradeService;

public class CloseTrade {

	static EntityManagerFactory emf = null;
	static EntityManager em = null; 
	
	/**
	 * There are three ways that a trade will close
	 *  1 - Hit Profit target
	 *  2 - Hit Stop Loss
	 *  3 - Hit Exit at DTE Limit
	 */
	public static void closeTrades() {
		
		//String symbol = TradeProperties.SYMBOL;
		
		emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		em = emf.createEntityManager();

		// Check for profit target TradeProperties.PROFIT_TARGET
		em.getTransaction().begin();		
		closeProfitableOrLosingTrades();
		em.getTransaction().commit();
		
		
		// Check for stop loss TradeProperties.MAX_LOSS
		
		
		// Check for time exit, TradeProperties.CLOSE_DTE;
		em.getTransaction().begin();		
		closeTimeExit();
		em.getTransaction().commit();
		
		em.close();
		emf.close();
	}

	private static void closeProfitableOrLosingTrades() {

		Calendar cal = Calendar.getInstance();
		
		// Gets all open trades
		List<Trade> trades = TradeService.getOpenTrades();
		for (Trade trade : trades) {
			
			// Gets the trade details based on the expiration date
			List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade.getExp());

			// Calculate the number of days this was opened before expiration
			int maxDte = Utils.calculateDaysBetween(trade.getExecTime(), trade.getExp());
			//System.out.println("maxDte: " + maxDte + " trade exec: " + Utils.asMMddYY(trade.getExecTime()));
			
			for (int days = 1; days < maxDte - TradeProperties.CLOSE_DTE; days++) {				

				// Resetting here to make sure I find a valid match 
				OptionPricing longCall = null;
				OptionPricing shortCall = null;
				OptionPricing longPut = null;
				OptionPricing shortPut = null;
				
				cal.clear();
				cal.setTime(trade.getExecTime());				
				cal.add(Calendar.DATE, days);
				
				if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
					
					if (!Utils.isHoliday(cal)) {
					
						double strike = 0.0;
						Date exp = trade.getExp();
						
						//System.out.println("Checking profit target on " + Utils.asMMddYY(cal.getTime()));
						try {
							for (TradeDetail openTradeDetail : openTradeDetails) {
								strike = openTradeDetail.getStrike();
								
								if (openTradeDetail.getSide().equals("BUY") && openTradeDetail.getType().equals("CALL")) {
									longCall = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "C");
								}
								if (openTradeDetail.getSide().equals("SELL") && openTradeDetail.getType().equals("CALL")) {
									shortCall = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "C");
								}
								if (openTradeDetail.getSide().equals("BUY") && openTradeDetail.getType().equals("PUT")) {
									longPut = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "P");
								}
								if (openTradeDetail.getSide().equals("SELL") && openTradeDetail.getType().equals("PUT")) {
									shortPut = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "P");
								}
							}
						} catch (Exception e) {
							System.out.println("Checking profit target on " + Utils.asMMddYY(cal.getTime()) + " Expires on " + Utils.asMMddYY(exp) + " strike: " + strike);
							e.printStackTrace();
							//throw e;
						}
						
						if (longCall != null && shortCall != null && longPut != null && shortPut != null) {
							
							double closingCost = Utils.round(longCall.getMean_price() - shortCall.getMean_price() + longPut.getMean_price() - shortPut.getMean_price(), 2);
							
							// a PROFIT_TARGET of 0.50 means if we can close for 1/2 the initial credit to close the trade
							// Note: that openingCost is a negative number, so we want closing cost to be greater 
							//	(or more positive (closer to zero)) than the opening cost to be profitable
							if (TradeProperties.PROFIT_TARGET != 0.0) {
								if (closingCost > trade.getOpeningCost() * (1 - TradeProperties.PROFIT_TARGET)) {
									
									System.out.println("Profit Target Closing Cost: " + Utils.round(longCall.getMean_price(),2) + " - " + Utils.round(shortCall.getMean_price(),2) + " + " 
											 + Utils.round(longPut.getMean_price(),2) + " - " + Utils.round(shortPut.getMean_price(),2));
									
									trade.setClosingCost(closingCost);
									trade.setProfit(Utils.round(trade.getClosingCost() - trade.getOpeningCost(),2));
									trade.setClose_status((TradeProperties.PROFIT_TARGET * 100) + "% PROFIT TARGET");
									trade.setCloseDate(cal.getTime());
									
									em.merge(trade);
									break;								
								}
							}
							
							// MAX_LOSS of 2.0 is 200% of credit
							if (TradeProperties.MAX_LOSS != 0.0) {
								if (closingCost < trade.getOpeningCost() * TradeProperties.MAX_LOSS) {
									
									System.out.println("Max Loss Closing Cost: " + Utils.round(longCall.getMean_price(),2) + " - " + Utils.round(shortCall.getMean_price(),2) + " + " 
											 + Utils.round(longPut.getMean_price(),2) + " - " + Utils.round(shortPut.getMean_price(),2));
									
									trade.setClosingCost(closingCost);
									trade.setProfit(Utils.round(trade.getClosingCost() - trade.getOpeningCost(),2));
									trade.setClose_status((TradeProperties.MAX_LOSS * 100) +  "% MAX LOSS");
									trade.setCloseDate(cal.getTime());
									
									em.merge(trade);
									break;
								}
							}
						}
					}
				}
	
			}
		}
	}

	private static void closeTimeExit() {
		
		OptionPricing longCall = null;
		OptionPricing shortCall = null;
		OptionPricing longPut = null;
		OptionPricing shortPut = null;
		
		Calendar cal = Calendar.getInstance();
		
		// Gets all open trades
		List<Trade> trades = TradeService.getOpenTrades();
		for (Trade trade : trades) {
		
			cal.clear();
			cal.setTime(trade.getExp());
			
			// Gets the trade details based on the expiration date
			List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(cal.getTime());
			
			// set calendar to date to close trade
			cal.add(Calendar.DATE, - TradeProperties.CLOSE_DTE);
			double strike = 0.0;

			try {
				for (TradeDetail openTradeDetail : openTradeDetails) {
					strike = openTradeDetail.getStrike();
					
					if (openTradeDetail.getSide().equals("BUY") && openTradeDetail.getType().equals("CALL")) {
						longCall = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "C");
					}
					if (openTradeDetail.getSide().equals("SELL") && openTradeDetail.getType().equals("CALL")) {
						shortCall = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "C");
					}
					if (openTradeDetail.getSide().equals("BUY") && openTradeDetail.getType().equals("PUT")) {
						longPut = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "P");
					}
					if (openTradeDetail.getSide().equals("SELL") && openTradeDetail.getType().equals("PUT")) {
						shortPut = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "P");
					}
				}
			} catch (Exception e) {
				System.out.println("Checking time close on " + Utils.asMMddYY(cal.getTime()) + " Expires on " + Utils.asMMddYY(cal.getTime()) + " strike: " + strike);
				e.printStackTrace();
				//throw e;
			}
			
			if (longCall != null && shortCall != null && longPut != null && shortPut != null) {
				
				System.out.println("Time Closing Cost: " + Utils.round(longCall.getMean_price(),2) + " - " + Utils.round(shortCall.getMean_price(),2) + " + " 
													 + Utils.round(longPut.getMean_price(),2) + " - " + Utils.round(shortPut.getMean_price(),2));
				
				trade.setClosingCost(Utils.round(longCall.getMean_price() - shortCall.getMean_price() + longPut.getMean_price() - shortPut.getMean_price(), 2));
				trade.setProfit(Utils.round(trade.getClosingCost() - trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(cal.getTime());
				
				em.merge(trade);
			}
		}
	}

}
