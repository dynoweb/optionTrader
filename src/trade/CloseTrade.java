package trade;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.joda.time.DateTime;

import main.TradeProperties;
import misc.Utils;
import model.OptionPricing;
import model.Trade;
import model.TradeDetail;
import model.service.ExpirationService;
import model.service.OptionPricingService;
import model.service.TradeDetailService;
import model.service.TradeService;

public class CloseTrade {

	static EntityManagerFactory emf = null;
	static EntityManager em = null; 
	
	public static void closeTrades(double profitTarget) {
		
		emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		em = emf.createEntityManager();
		em.getTransaction().begin();		

		// Gets all open trades
		List<Trade> trades = TradeService.getOpenTrades();
		for (Trade trade : trades) {
		
			if (trade.getTradeType().equals("SHORT CALL")) {
				closeShortCall(trade, profitTarget);
			}
		}
		
		em.getTransaction().commit();
		em.close();
		emf.close();
	}

	/**
	 * There are three ways that a trade will close
	 *  1 - Hit Profit target
	 *  2 - Hit Stop Loss
	 *  3 - Hit Exit at DTE Limit
	 */
	public static void closeTrades() {
		
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

		if (TradeProperties.PROFIT_TARGET == 0.0 && TradeProperties.MAX_LOSS == 0.0 && TradeProperties.CLOSE_DELTA_TARGET == 0.0) {
			return;
		}

		// Gets all open trades
		List<Trade> trades = TradeService.getOpenTrades();
		for (Trade trade : trades) {
			
			// Calculate the number of days this was opened before expiration
			int maxDte = Utils.calculateDaysBetween(trade.getExecTime(), trade.getExp());
			
			//System.out.println("maxDte: " + maxDte + " trade exec: " + Utils.asMMddYY(trade.getExecTime()));
			
			if (trade.getTradeType().equals("IRON CONDOR")) {	
				// Gets the trade details based on the expiration date
				//List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade.getExp());
				closeProfitableOrLosingIronCondor(trade, maxDte);
			}
			
			if (trade.getTradeType().equals("COVERED CALL")) {
				closeProfitableOrLosingCoveredCall(trade, maxDte);
			}
			
			if (trade.getTradeType().equals("SHORT PUT SPREAD")) {
				closeProfitableOrLosingShortPutSpread(trade, maxDte);
			}
			
		}
	}

	/**
	 *  The code in this method was copied from the Covered Call trade and hasn't yet been specialized for the Short Call.
	 * @param trade
	 * @param profitTarget 
	 */
	private static void closeShortCall(Trade trade, double profitTarget) {

		TradeDetail openingShortCallLeg = null;
		
		// Get the legs of the trade
		List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
		for (TradeDetail tradeDetail : openTradeDetails) {
			if (tradeDetail.getType().equals("CALL"))
				openingShortCallLeg = tradeDetail;
		}
		
		// from JDK to Joda
		DateTime jOpenDate = new DateTime(openingShortCallLeg.getExecTime());
		DateTime jExpDate = new DateTime(trade.getExp()); 
		DateTime jCloseByDate = new DateTime(trade.getExp());
		jCloseByDate = jCloseByDate.plusDays(- TradeProperties.CLOSE_DTE);
		
		List<OptionPricing> callPriceList = OptionPricingService
				.getPriceHistory(jOpenDate.plusDays(1).toDate(), jCloseByDate.toDate(), jExpDate.toDate(), 
						openingShortCallLeg.getStrike(), "C");

		for (OptionPricing call: callPriceList) {
		
			// look for profit target exit
			if (profitTarget != 0.0) {  

				// short call closing close
				double shortCallClosingCost = (Utils.round(- call.getMean_price() * 100, 2));
				// since all legs (trade) closing cost
				double closingCost = shortCallClosingCost;
				
				if (Math.abs(closingCost) < trade.getOpeningCost() * (1 - profitTarget)) {
					
					System.out.println("Profit Target Closing Cost: " + closingCost);  
					trade.setClosingCost(closingCost);
					trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(), 2));
					trade.setClose_status((profitTarget * 100) + "% PROFIT TARGET");
					trade.setCloseDate(call.getTrade_date());
					
					em.merge(trade);									
					
					// Save the closing price of the short call in the trade details table
					TradeDetail closeShortCallTradeDetail = new TradeDetail();
					
					closeShortCallTradeDetail.setExecTime(call.getTrade_date());
					closeShortCallTradeDetail.setExp(call.getExpiration());
					closeShortCallTradeDetail.setPosEffect("CLOSING");
					closeShortCallTradeDetail.setPrice(Utils.round(-call.getMean_price(), 2));
					closeShortCallTradeDetail.setQty(TradeProperties.CONTRACTS);
					closeShortCallTradeDetail.setSide("BUY");
					closeShortCallTradeDetail.setStrike(call.getStrike());
					closeShortCallTradeDetail.setSymbol(call.getSymbol());
					closeShortCallTradeDetail.setTrade(trade);
					closeShortCallTradeDetail.setType(call.getCall_put().equals("C") ? "CALL" : "PUT");
					
					em.persist(closeShortCallTradeDetail);				
					
					break;
				}
			}
			
			// Current trade date under consideration
			DateTime jTradeDate = new DateTime(call.getTrade_date());
			// Last trade in the trading days list
			DateTime jLastTradeDate = new DateTime(callPriceList.get(callPriceList.size()-1).getTrade_date());
			
			if (jTradeDate.equals(jLastTradeDate)) {
				
				// perform a time close
				trade.setClosingCost(Utils.round(- call.getMean_price() * 100, 2));
				System.out.println("Time Closing Cost: " + trade.getClosingCost());
				
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(call.getTrade_date());
				
				em.merge(trade);
				
				// Save the closing price of the short call in the trade details table
				TradeDetail closeShortCallTradeDetail = new TradeDetail();
				
				String comment = null;
				if (trade.getClosingCost() > 0) {
					comment = "Stock close price: " + call.getAdjusted_stock_close_price() + " delta: " + call.getDelta();
				}
				
				closeShortCallTradeDetail.setComment(comment);
				closeShortCallTradeDetail.setExecTime(call.getTrade_date());
				closeShortCallTradeDetail.setExp(call.getExpiration());
				closeShortCallTradeDetail.setPosEffect("CLOSING");
				closeShortCallTradeDetail.setPrice(Utils.round(-call.getMean_price(), 2));
				closeShortCallTradeDetail.setQty(TradeProperties.CONTRACTS);
				closeShortCallTradeDetail.setSide("BUY");
				closeShortCallTradeDetail.setStrike(call.getStrike());
				closeShortCallTradeDetail.setSymbol(call.getSymbol());
				closeShortCallTradeDetail.setTrade(trade);
				closeShortCallTradeDetail.setType(call.getCall_put().equals("C") ? "CALL" : "PUT");
				
				em.persist(closeShortCallTradeDetail);					
			}
		}
	}

	private static void closeProfitableOrLosingShortPutSpread(Trade trade, int maxDte) {
		
		Calendar cal = Calendar.getInstance();
		Date lastTradeDate = OptionPricingService.getLastTradeDate();
		//List<TradeDetail> openTradeDetails = trade.getTradeDetails();
		List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
		
		for (int days = 1; days < maxDte - TradeProperties.CLOSE_DTE; days++) {				

			// Resetting here to make sure I find a valid match 
			OptionPricing longPut = null;
			OptionPricing shortPut = null;
			
			cal.clear();
			cal.setTime(trade.getExecTime());				
			cal.add(Calendar.DATE, days);
			
			if (cal.getTime().after(lastTradeDate)) {
				break;
			}
			
			if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				
				if (!Utils.isHoliday(cal)) {
				
					double strike = 0.0;
					Date exp = trade.getExp();
					
					//System.out.println("Checking profit target on " + Utils.asMMddYY(cal.getTime()));
					try {
						for (TradeDetail openTradeDetail : openTradeDetails) {
							strike = openTradeDetail.getStrike();
							
							if (openTradeDetail.getSide().equals("BUY") && openTradeDetail.getType().equals("PUT")) {
								longPut = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "P");
							}
							if (openTradeDetail.getSide().equals("SELL") && openTradeDetail.getType().equals("PUT")) {
								shortPut = OptionPricingService.getRecord(cal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "P");
							}
						}
					} catch (Exception e) {
						System.out.println("Checking profit target on " + Utils.asMMddYY(cal.getTime()) + " Expires on " + Utils.asMMddYY(exp) 
								+ " strike: " + strike + " lastTradeDate: " + Utils.asMMddYY(lastTradeDate));
						e.printStackTrace();
						//throw e;
					}
					
					if (longPut != null && shortPut != null) {
						
						double closingCost = Utils.round(longPut.getMean_price() * 100 - shortPut.getMean_price() * 100, 2);
						
						// a PROFIT_TARGET of 0.50 means if we can close for 1/2 the initial credit to close the trade
						// Note: that openingCost is a negative number, so we want closing cost to be greater 
						//	(or more positive (closer to zero)) than the opening cost to be profitable
						if (TradeProperties.PROFIT_TARGET != 0.0) {
							if (Math.abs(closingCost) < trade.getOpeningCost() * (1 - TradeProperties.PROFIT_TARGET)) {
								
								System.out.println("Profit Target Closing Cost: "  
										 + Utils.round(longPut.getMean_price(),2) + " - " + Utils.round(shortPut.getMean_price(),2));
								
								trade.setClosingCost(closingCost);
								trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
								trade.setClose_status((TradeProperties.PROFIT_TARGET * 100) + "% PROFIT TARGET");
								trade.setCloseDate(cal.getTime());
								
								em.merge(trade);
								break;								
							}
						}
						
						// MAX_LOSS of 2.0 is 200% of credit
						if (TradeProperties.MAX_LOSS != 0.0) {
							if (Math.abs(closingCost) > trade.getOpeningCost() * (1 + TradeProperties.MAX_LOSS)) {
								
								System.out.println("Closing - Stop Loss - on " + Utils.asMMddYY(cal.getTime()) + " " 
										 + Utils.round(longPut.getMean_price(),2) + " - " + Utils.round(shortPut.getMean_price(),2));
								
								trade.setClosingCost(closingCost);
								trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
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


	private static void closeProfitableOrLosingCoveredCall(Trade trade, int maxDte) {

		Date lastTradeDate = OptionPricingService.getLastTradeDate();

		TradeDetail coveredCall = null;
		TradeDetail coveredStock = null;

		List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
		
		for (TradeDetail tradeDetail : openTradeDetails) {
			if (tradeDetail.getType().equals("CALL"))
				coveredCall = tradeDetail;
			if (tradeDetail.getType().equals("STOCK"))
				coveredStock = tradeDetail;		
		}
		
		// Date being tested
		Calendar cal = Calendar.getInstance();

		// TODO I could iterate through the days this option was traded
		for (int days = 1; days < maxDte - TradeProperties.CLOSE_DTE; days++) {				
		
			cal.clear();
			cal.setTime(trade.getExecTime());				
			cal.add(Calendar.DATE, days);			
			
			if (cal.getTime().after(lastTradeDate)) {
				break;
			}
			
			if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				
				if (!Utils.isHoliday(cal)) {
					
					OptionPricing shortCall = null;
					try {
						shortCall = OptionPricingService.getRecord(cal.getTime(), trade.getExp(),  coveredCall.getStrike(), "C");
						
						if (TradeProperties.CLOSE_DELTA_TARGET != 0 && shortCall.getDelta() > TradeProperties.CLOSE_DELTA_TARGET) {
							
							System.out.println("Close Delta Target " + Utils.asMMddYY(trade.getExecTime()) + " - " + Utils.round(shortCall.getMean_price(),2));
							
							double stockClose = Utils.round(shortCall.getAdjusted_stock_close_price(), 2);
							double closingCost = Math.min(shortCall.getStrike(), (stockClose - shortCall.getMean_price())) * 100.00;
							trade.setClosingCost(closingCost);
							trade.setProfit(trade.getClosingCost() + trade.getOpeningCost());
							trade.setClose_status("DELTA PROFIT TARGET");
							trade.setCloseDate(cal.getTime());
							
							em.merge(trade);
							
							// Save the closing price of the short put in the trade details table
							TradeDetail closeShortCallTradeDetail = new TradeDetail();
							
							closeShortCallTradeDetail.setExecTime(shortCall.getTrade_date());
							closeShortCallTradeDetail.setExp(shortCall.getExpiration());
							closeShortCallTradeDetail.setPosEffect("CLOSING");
							closeShortCallTradeDetail.setPrice(shortCall.getMean_price());
							closeShortCallTradeDetail.setQty(TradeProperties.CONTRACTS);
							closeShortCallTradeDetail.setSide("BUY");
							closeShortCallTradeDetail.setStrike(shortCall.getStrike());
							closeShortCallTradeDetail.setSymbol(shortCall.getSymbol());
							closeShortCallTradeDetail.setTrade(trade);
							closeShortCallTradeDetail.setType(shortCall.getCall_put().equals("C") ? "CALL" : "PUT");
							
							em.persist(closeShortCallTradeDetail);
							
							TradeDetail longStockTradeDetail = new TradeDetail();
							
							longStockTradeDetail.setExecTime(shortCall.getTrade_date());
							longStockTradeDetail.setPosEffect("CLOSING");
							longStockTradeDetail.setPrice(Utils.round(shortCall.getAdjusted_stock_close_price(),2));
							longStockTradeDetail.setQty(TradeProperties.CONTRACTS * 100);
							longStockTradeDetail.setSide("SELL");
							longStockTradeDetail.setSymbol(shortCall.getSymbol());
							longStockTradeDetail.setTrade(trade);
							longStockTradeDetail.setType("STOCK");
							
							em.persist(longStockTradeDetail);
							
							break;
						}
						
					} catch (Exception ignore) {
						ignore.printStackTrace();					
					}
				}
			}
			
		}
	}

	private static void closeProfitableOrLosingIronCondor(Trade trade, int maxDte) {
		
		Calendar cal = Calendar.getInstance();
		Date lastTradeDate = OptionPricingService.getLastTradeDate();
		//List<TradeDetail> openTradeDetails = trade.getTradeDetails();
		List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
		
		for (int days = 1; days < maxDte - TradeProperties.CLOSE_DTE; days++) {				

			// Resetting here to make sure I find a valid match 
			OptionPricing longCall = null;
			OptionPricing shortCall = null;
			OptionPricing longPut = null;
			OptionPricing shortPut = null;
			
			cal.clear();
			cal.setTime(trade.getExecTime());				
			cal.add(Calendar.DATE, days);
			
			if (cal.getTime().after(lastTradeDate)) {
				break;
			}
			
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
						System.out.println("Checking profit target on " + Utils.asMMddYY(cal.getTime()) + " Expires on " + Utils.asMMddYY(exp) 
								+ " strike: " + strike + " lastTradeDate: " + Utils.asMMddYY(lastTradeDate));
						e.printStackTrace();
						//throw e;
					}
					
					// Close the Iron Condor
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

	/**
	 * Iterates through the list of open trades, 
	 */
	private static void closeTimeExit() {
		
		if (TradeProperties.TRADE_TYPE.equals("SHORT_CALL")) {
			return;
		}
		
		OptionPricing longCall = null;
		OptionPricing shortCall = null;
		OptionPricing longPut = null;
		OptionPricing shortPut = null;
		TradeDetail longStock = null; 
		
		Calendar cal = Calendar.getInstance();
		Date lastTradeDate = OptionPricingService.getLastTradeDate();
		
		// Gets all open trades
		List<Trade> trades = TradeService.getOpenTrades();
		System.out.println("Looking at time close for " + trades.size() + " trades");
		
		for (Trade trade : trades) {
		
			cal.clear();
			cal.setTime(trade.getExp());

			// Gets the trade details based on the expiration date
			//List<TradeDetail> openTradeDetails = trade.getTradeDetails();
			List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
			
			// set calendar to date to close trade
			cal.add(Calendar.DATE, - TradeProperties.CLOSE_DTE);
			
			ExpirationService es = new ExpirationService();
			List<Date> dates = es.getTradeDatesForExpiration(trade.getExp());
			while (!dates.contains(cal.getTime())) {
				cal.add(Calendar.DAY_OF_MONTH, -1);
			}
			
//			while (Utils.isHoliday(cal) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
//				cal.add(Calendar.DAY_OF_MONTH, -1);
//			}			

			double strike = 0.0;

			// if the normal close time is after the price records, close the trade on the last record date.
			if (cal.getTime().after(lastTradeDate)) {
				cal.setTime(lastTradeDate);
			}			

			try {
				for (TradeDetail openTradeDetail : openTradeDetails) {
					strike = openTradeDetail.getStrike();
					
					System.out.println("Checking time close on " + Utils.asMMddYY(cal.getTime()) + " Expires on " + Utils.asMMddYY(cal.getTime()) + " strike: " + strike + " type: " + openTradeDetail.getType());

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
					if (openTradeDetail.getType().equals("STOCK")) {
						longStock = openTradeDetail; 
					}
				}
			} catch (Exception e) {
				System.out.println("Exception - Checking time close on " + Utils.asMMddYY(cal.getTime()) + " Expires on " + Utils.asMMddYY(cal.getTime()) + " strike: " + strike);
				e.printStackTrace();
				//throw e;
			}
			
			// Close the Short Call
			if (longCall == null && shortCall != null && longPut == null && shortPut == null) {
				
				trade.setClosingCost(Utils.round(- shortCall.getMean_price() * 100, 2));
				System.out.println("Time Closing Cost: " + trade.getClosingCost());
				
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(cal.getTime());
				
				em.merge(trade);
				
				// Save the closing price of the short call in the trade details table
				TradeDetail closeShortCallTradeDetail = new TradeDetail();
				
				closeShortCallTradeDetail.setExecTime(shortCall.getTrade_date());
				closeShortCallTradeDetail.setExp(shortCall.getExpiration());
				closeShortCallTradeDetail.setPosEffect("CLOSING");
				closeShortCallTradeDetail.setPrice(Utils.round(-shortCall.getMean_price(), 2));
				closeShortCallTradeDetail.setQty(TradeProperties.CONTRACTS);
				closeShortCallTradeDetail.setSide("BUY");
				closeShortCallTradeDetail.setStrike(shortCall.getStrike());
				closeShortCallTradeDetail.setSymbol(shortCall.getSymbol());
				closeShortCallTradeDetail.setTrade(trade);
				closeShortCallTradeDetail.setType(shortCall.getCall_put().equals("C") ? "CALL" : "PUT");
				
				em.persist(closeShortCallTradeDetail);				
			}
			
			// Close the Iron Condor
			if (longCall != null && shortCall != null && longPut != null && shortPut != null) {
				
				System.out.println("Time Closing Cost: " + (Utils.round(longPut.getMean_price() * 100 - shortPut.getMean_price() * 100, 2) +
						 Utils.round(longCall.getMean_price() * 100 - shortCall.getMean_price() * 100, 2)));
				
				trade.setClosingCost(Utils.round(longPut.getMean_price() * 100 - shortPut.getMean_price() * 100, 2) +
									 Utils.round(longCall.getMean_price() * 100 - shortCall.getMean_price() * 100, 2));
				
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(cal.getTime());
				
				em.merge(trade);
			}
			
			// TODO need better condition
			// Close the Short Put Spread
			if (longCall == null && shortCall == null && longPut != null && shortPut != null) {
				
				System.out.println("Time Closing Cost: " +  
													 + Utils.round(longPut.getMean_price(),2) + " - " + Utils.round(shortPut.getMean_price(),2));
				
				double cc = Utils.round(longPut.getMean_price() * 100 - shortPut.getMean_price() * 100, 2);
				// should not be able to close for a credit, it should always cost something or be zero, never a credit
				trade.setClosingCost(Math.min(0.0, cc));
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(cal.getTime());
				
				em.merge(trade);
				
				// Save the closing price of the short put in the trade details table
				TradeDetail closeShortPutTradeDetail = new TradeDetail();
				
				closeShortPutTradeDetail.setExecTime(shortPut.getTrade_date());
				closeShortPutTradeDetail.setExp(shortPut.getExpiration());
				closeShortPutTradeDetail.setPosEffect("CLOSING");
				closeShortPutTradeDetail.setPrice(-shortPut.getMean_price());
				closeShortPutTradeDetail.setQty(TradeProperties.CONTRACTS);
				closeShortPutTradeDetail.setSide("BUY");
				closeShortPutTradeDetail.setStrike(shortPut.getStrike());
				closeShortPutTradeDetail.setSymbol(shortPut.getSymbol());
				closeShortPutTradeDetail.setTrade(trade);
				closeShortPutTradeDetail.setType(shortPut.getCall_put().equals("C") ? "CALL" : "PUT");
				
				em.persist(closeShortPutTradeDetail);
				
				// Save the closing price of the short put in the trade details table
				TradeDetail closeLongPutTradeDetail = new TradeDetail();
				
				closeLongPutTradeDetail.setExecTime(longPut.getTrade_date());
				closeLongPutTradeDetail.setExp(longPut.getExpiration());
				closeLongPutTradeDetail.setPosEffect("CLOSING");
				closeLongPutTradeDetail.setPrice(longPut.getMean_price());
				closeLongPutTradeDetail.setQty(-TradeProperties.CONTRACTS);
				closeLongPutTradeDetail.setSide("SELL");
				closeLongPutTradeDetail.setStrike(longPut.getStrike());
				closeLongPutTradeDetail.setSymbol(longPut.getSymbol());
				closeLongPutTradeDetail.setTrade(trade);
				closeLongPutTradeDetail.setType(longPut.getCall_put().equals("C") ? "CALL" : "PUT");
				
				em.persist(closeLongPutTradeDetail);
				
			}
			
			// Close the Covered Call
			if (longStock != null && shortCall != null) {
				double stockClose = Utils.round(shortCall.getAdjusted_stock_close_price(), 2);

				System.out.println("Time Closing Cost: " + stockClose + " - "  
						 + shortCall.getMean_price());

				double closingCost = Math.min(shortCall.getStrike(), (stockClose - shortCall.getMean_price())) * 100.00;
				trade.setClosingCost(Utils.round(closingCost, 0));
				trade.setProfit(trade.getClosingCost() + trade.getOpeningCost()); //Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(cal.getTime());				
				
				em.merge(trade);
				
				// Save the closing price of the short call in the trade details table
				TradeDetail closeShortCallTradeDetail = new TradeDetail();
				
				closeShortCallTradeDetail.setExecTime(shortCall.getTrade_date());
				closeShortCallTradeDetail.setExp(shortCall.getExpiration());
				closeShortCallTradeDetail.setPosEffect("CLOSING");
				closeShortCallTradeDetail.setPrice(Utils.round(-shortCall.getMean_price(), 2));
				closeShortCallTradeDetail.setQty(TradeProperties.CONTRACTS);
				closeShortCallTradeDetail.setSide("BUY");
				closeShortCallTradeDetail.setStrike(shortCall.getStrike());
				closeShortCallTradeDetail.setSymbol(shortCall.getSymbol());
				closeShortCallTradeDetail.setTrade(trade);
				closeShortCallTradeDetail.setType(shortCall.getCall_put().equals("C") ? "CALL" : "PUT");
				
				em.persist(closeShortCallTradeDetail);

				TradeDetail longStockTradeDetail = new TradeDetail();
				
				longStockTradeDetail.setExecTime(shortCall.getTrade_date());
				//longStockTradeDetail.setExp(shortCall.getExpiration());
				longStockTradeDetail.setPosEffect("CLOSING");
				longStockTradeDetail.setPrice(Utils.round(shortCall.getAdjusted_stock_close_price(),2));
				longStockTradeDetail.setQty(TradeProperties.CONTRACTS * 100);
				longStockTradeDetail.setSide("SELL");
				//longStockTradeDetail.setStrike(shortCall.getStrike());
				longStockTradeDetail.setSymbol(shortCall.getSymbol());
				longStockTradeDetail.setTrade(trade);
				longStockTradeDetail.setType("STOCK");
				
				em.persist(longStockTradeDetail);

			}
		}
	}

}
