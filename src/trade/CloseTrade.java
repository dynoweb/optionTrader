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
//import model.service.ExpirationService;
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
				//closeShortCall(trade, profitTarget);
				closeShort(trade, profitTarget);
			}
			if (trade.getTradeType().equals("SHORT PUT")) {
				closeShort(trade, profitTarget);
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
	public static void closeTrades(String tradeType, double spreadWidth) {
		
		emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		em = emf.createEntityManager();

		// Check for profit target TradeProperties.PROFIT_TARGET
		em.getTransaction().begin();		
		closeProfitableOrLosingTrades();
		em.getTransaction().commit();
		
		
		// Check for stop loss TradeProperties.MAX_LOSS
		
		
		// Check for time exit, TradeProperties.CLOSE_DTE;
		em.getTransaction().begin();		
		closeTimeExit(spreadWidth);
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
//	private static void closeShortCall(Trade trade, double profitTarget) {
//
//		TradeDetail openingShortCallLeg = null;
//		
//		// Get the legs of the trade
//		List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
//		for (TradeDetail tradeDetail : openTradeDetails) {
//			if (tradeDetail.getType().equals("CALL"))
//				openingShortCallLeg = tradeDetail;
//		}
//		
//		// from JDK to Joda
//		DateTime jOpenDate = new DateTime(openingShortCallLeg.getExecTime());
//		DateTime jExpDate = new DateTime(trade.getExp()); 
//		DateTime jCloseByDate = new DateTime(trade.getExp());
//		jCloseByDate = jCloseByDate.plusDays(- TradeProperties.CLOSE_DTE);
//		
//		List<OptionPricing> callPriceList = OptionPricingService
//				.getPriceHistory(jOpenDate.plusDays(1).toDate(), jCloseByDate.toDate(), jExpDate.toDate(), 
//						openingShortCallLeg.getStrike(), "C");
//
//		for (OptionPricing call: callPriceList) {
//		
//			// look for profit target exit
//			if (profitTarget != 0.0) {  
//
//				// short call closing close
//				double shortCallClosingCost = (Utils.round(- call.getMean_price() * 100, 2));
//				// since all legs (trade) closing cost
//				double closingCost = shortCallClosingCost;
//				
//				if (Math.abs(closingCost) < trade.getOpeningCost() * (1 - profitTarget)) {
//					
//					System.out.println("Profit Target Closing Cost: " + closingCost);  
//					trade.setClosingCost(closingCost);
//					trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(), 2));
//					trade.setClose_status((profitTarget * 100) + "% PROFIT TARGET");
//					trade.setCloseDate(call.getTrade_date());
//					
//					em.merge(trade);									
//					
//					// Save the closing price of the short call in the trade details table
//					TradeDetail closeShortCallTradeDetail = new TradeDetail();
//					
//					closeShortCallTradeDetail.setExecTime(call.getTrade_date());
//					closeShortCallTradeDetail.setExp(call.getExpiration());
//					closeShortCallTradeDetail.setPosEffect("CLOSING");
//					closeShortCallTradeDetail.setPrice(Utils.round(-call.getMean_price(), 2));
//					closeShortCallTradeDetail.setQty(TradeProperties.CONTRACTS);
//					closeShortCallTradeDetail.setSide("BUY");
//					closeShortCallTradeDetail.setStrike(call.getStrike());
//					closeShortCallTradeDetail.setSymbol(call.getSymbol());
//					closeShortCallTradeDetail.setTrade(trade);
//					closeShortCallTradeDetail.setType(call.getCall_put().equals("C") ? "CALL" : "PUT");
//					
//					em.persist(closeShortCallTradeDetail);				
//					
//					break;
//				}
//			}
//			
//			// Current trade date under consideration
//			DateTime jTradeDate = new DateTime(call.getTrade_date());
//			// Last trade in the trading days list
//			DateTime jLastTradeDate = new DateTime(callPriceList.get(callPriceList.size()-1).getTrade_date());
//			
//			if (jTradeDate.equals(jLastTradeDate)) {
//				
//				// perform a time close
//				trade.setClosingCost(Utils.round(- call.getMean_price() * 100, 2));
//				System.out.println("Time Closing Cost: " + trade.getClosingCost());
//				
//				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
//				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
//				trade.setCloseDate(call.getTrade_date());
//				
//				em.merge(trade);
//				
//				// Save the closing price of the short call in the trade details table
//				TradeDetail closeShortCallTradeDetail = new TradeDetail();
//				
//				String comment = null;
//				if (trade.getClosingCost() > 0) {
//					comment = "Stock close price: " + call.getAdjusted_stock_close_price() + " delta: " + call.getDelta();
//				}
//				
//				closeShortCallTradeDetail.setComment(comment);
//				closeShortCallTradeDetail.setExecTime(call.getTrade_date());
//				closeShortCallTradeDetail.setExp(call.getExpiration());
//				closeShortCallTradeDetail.setPosEffect("CLOSING");
//				closeShortCallTradeDetail.setPrice(Utils.round(-call.getMean_price(), 2));
//				closeShortCallTradeDetail.setQty(TradeProperties.CONTRACTS);
//				closeShortCallTradeDetail.setSide("BUY");
//				closeShortCallTradeDetail.setStrike(call.getStrike());
//				closeShortCallTradeDetail.setSymbol(call.getSymbol());
//				closeShortCallTradeDetail.setTrade(trade);
//				closeShortCallTradeDetail.setType(call.getCall_put().equals("C") ? "CALL" : "PUT");
//				
//				em.persist(closeShortCallTradeDetail);					
//			}
//		}
//	}
	
	private static void closeShort(Trade trade, double profitTarget) {

		TradeDetail openingShortLeg = null;
		
		// Get the legs of the trade
		List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
		for (TradeDetail tradeDetail : openTradeDetails) {
			//if (tradeDetail.getType().equals("CALL"))
				openingShortLeg = tradeDetail;
		}
		
		// from JDK to Joda
		DateTime jOpenDate = new DateTime(openingShortLeg.getExecTime());
		DateTime jExpDate = new DateTime(trade.getExp()); 
		DateTime jCloseByDate = new DateTime(trade.getExp());
		jCloseByDate = jCloseByDate.plusDays(- TradeProperties.CLOSE_DTE);
		
//		if (jExpDate.getYear() == 2011) {
//			System.out.println("Setting breakpoint");
//		}
		
		List<OptionPricing> optPriceList = OptionPricingService
				.getPriceHistory(jOpenDate.plusDays(1).toDate(), jCloseByDate.toDate(), jExpDate.toDate(), 
						openingShortLeg.getStrike(), openingShortLeg.getType().substring(0, 1));

		for (OptionPricing shortOpt: optPriceList) {
		
			// look for profit target exit
			if (profitTarget != 0.0) {  

				// short option closing close
				double shortClosingCost = (Utils.round(- shortOpt.getMean_price() * 100, 2));
				// since all legs (trade) closing cost
				double closingCost = shortClosingCost;
				
				if (Math.abs(closingCost) < trade.getOpeningCost() * (1 - profitTarget)) {
					
					System.out.println("Profit Target Closing Cost: " + closingCost);  
					trade.setClosingCost(closingCost);
					trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(), 2));
					trade.setClose_status((profitTarget * 100) + "% PROFIT TARGET");
					trade.setCloseDate(shortOpt.getTrade_date());
					
					em.merge(trade);									
					
					// Save the closing price of the short call in the trade details table
					recordShortClose(trade, shortOpt);
					
					break;
				}
			}
			
			// MAX_LOSS of 2.0 is 200% of credit
			if (TradeProperties.MAX_LOSS != 0.0) {

				// short option closing close
				double closingCost = (Utils.round(- shortOpt.getMean_price() * 100, 2));

				if (Math.abs(closingCost) > trade.getOpeningCost() * (1 + TradeProperties.MAX_LOSS)) {
					
					System.out.println("Closing - Stop Loss - on " + Utils.asMMddYY(shortOpt.getTrade_date()) + " "  + closingCost);
					
					trade.setClosingCost(closingCost);
					trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
					trade.setClose_status((TradeProperties.MAX_LOSS * 100) +  "% MAX LOSS");
					trade.setCloseDate(shortOpt.getTrade_date());
					
					em.merge(trade);
					
					// Save the closing price of the short call in the trade details table
					recordShortClose(trade, shortOpt);
					
					break;					
				}
			}

			
			// Current trade date under consideration
			DateTime jTradeDate = new DateTime(shortOpt.getTrade_date());
			// Last trade in the trading days list
			DateTime jLastTradeDate = new DateTime(optPriceList.get(optPriceList.size()-1).getTrade_date());
			
			if (jTradeDate.equals(jLastTradeDate)) {
				
				// perform a time close
				trade.setClosingCost(Utils.round(- shortOpt.getMean_price() * 100, 2));
				System.out.println("Time Closing Cost: " + trade.getClosingCost());
				
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(shortOpt.getTrade_date());
				
				em.merge(trade);
				
				// Save the closing price of the short call in the trade details table
				recordShortClose(trade, shortOpt);
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
							
							recordShortClose(trade, shortCall);
							
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
	private static void closeTimeExit(double spreadWidth) {
		
		if (TradeProperties.TRADE_TYPE.equals("SHORT_CALL") || TradeProperties.TRADE_TYPE.equals("SHORT_PUT")) {
			return;
		}
		
		//--------------------------------------------
		// TODO May Want To Put This Type Of Logic Here
//		TradeDetail openingShortLeg = null;
//		
//		// Get the legs of the trade
//		List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
//		for (TradeDetail tradeDetail : openTradeDetails) {
//			//if (tradeDetail.getType().equals("CALL"))
//				openingShortLeg = tradeDetail;
//		}
//		
//		DateTime jOpenDate = new DateTime(openingShortLeg.getExecTime());
//		DateTime jExpDate = new DateTime(trade.getExp()); 
//		DateTime jCloseByDate = new DateTime(trade.getExp());
//		jCloseByDate = jCloseByDate.plusDays(- TradeProperties.CLOSE_DTE);
//		
//		List<OptionPricing> optPriceList = OptionPricingService
//				.getPriceHistory(jOpenDate.plusDays(1).toDate(), jCloseByDate.toDate(), jExpDate.toDate(), 
//						openingShortLeg.getStrike(), openingShortLeg.getType().substring(0, 1));
		//--------------------------------------------
		
		OptionPricing longCall = null;
		OptionPricing shortCall = null;
		OptionPricing longPut = null;
		OptionPricing shortPut = null;
		TradeDetail longStock = null; 
		
		Calendar lastOptionTradedCal = Calendar.getInstance();
		//Date lastTradeDate = OptionPricingService.getLastTradeDate();
		
		// Gets all open trades
		List<Trade> trades = TradeService.getOpenTrades();
		System.out.println("Looking at time close for " + trades.size() + " trades");
		
		for (Trade trade : trades) {
		
			Date lastContractTradeDate = OptionPricingService.getLastTradeDateForOption(trade.getExp());
			lastOptionTradedCal.clear();
			lastOptionTradedCal.setTime(lastContractTradeDate);

			// Gets the trade details based on the expiration date
			//List<TradeDetail> openTradeDetails = trade.getTradeDetails();
			List<TradeDetail> openTradeDetails = TradeDetailService.getTradeDetails(trade);
			

			
// Assuming all trades close at last trade date			
//			// set calendar to date to close trade
//			cal.add(Calendar.DATE, - TradeProperties.CLOSE_DTE);
//			
//			ExpirationService es = new ExpirationService();
//			List<Date> dates = es.getTradeDatesForExpiration(trade.getExp());
//			while (!dates.contains(cal.getTime())) {
//				cal.add(Calendar.DAY_OF_MONTH, -1);
//			}
			
//			while (Utils.isHoliday(cal) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
//				cal.add(Calendar.DAY_OF_MONTH, -1);
//			}			

			double strike = 0.0;

			// if the normal close time is after the price records, close the trade on the last record date.
//			if (cal.getTime().after(lastTradeDate)) {
//				cal.setTime(lastTradeDate);
//			}			

			try {
				for (TradeDetail openTradeDetail : openTradeDetails) {

					strike = openTradeDetail.getStrike();
					System.out.println("Checking time close on " + Utils.asMMddYY(lastOptionTradedCal.getTime()) + " Expires on " + Utils.asMMddYY(lastOptionTradedCal.getTime()) + " strike: " + strike + " type: " + openTradeDetail.getType());

					if (openTradeDetail.getSide().equals("BUY") && openTradeDetail.getType().equals("CALL")) {
						longCall = OptionPricingService.getRecord(lastOptionTradedCal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "C");
					}
					if (openTradeDetail.getSide().equals("SELL") && openTradeDetail.getType().equals("CALL")) {
						shortCall = OptionPricingService.getRecord(lastOptionTradedCal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "C");
					}
					if (openTradeDetail.getSide().equals("BUY") && openTradeDetail.getType().equals("PUT")) {
						longPut = OptionPricingService.getRecord(lastOptionTradedCal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "P");
					}
					if (openTradeDetail.getSide().equals("SELL") && openTradeDetail.getType().equals("PUT")) {
						shortPut = OptionPricingService.getRecord(lastOptionTradedCal.getTime(), trade.getExp(), openTradeDetail.getStrike(), "P");
					}
					if (openTradeDetail.getType().equals("STOCK")) {
						longStock = openTradeDetail; 
					}
				}
			} catch (Exception e) {
				System.out.println("Exception - Checking time close on " + Utils.asMMddYY(lastOptionTradedCal.getTime()) + " Expires on " + Utils.asMMddYY(lastOptionTradedCal.getTime()) + " strike: " + strike);
				e.printStackTrace();
				//throw e;
			}
			
			
			// Close the Short Call
			if (longCall == null && shortCall != null && longPut == null && shortPut == null) {
				
				double fees = TradeProperties.CONTRACTS * 1 * TradeProperties.COST_PER_CONTRACT_FEE;
				trade.setClosingCost(Utils.round(- shortCall.getMean_price() * 100 + fees, 2));
				System.out.println("Time Closing Cost: " + trade.getClosingCost());
				
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(lastOptionTradedCal.getTime());
				
				em.merge(trade);
				
				// Save the closing price of the short call in the trade details table
				recordShortClose(trade, shortCall);
			}

			
			// Close the Iron Condor
			if (longCall != null && shortCall != null && longPut != null && shortPut != null) {
				
//				System.out.println("Time Closing Cost: " + (Utils.round(longPut.getMean_price() * 100.0 - shortPut.getMean_price() * 100.0, 2) +
//						 Utils.round(longCall.getMean_price() * 100.0 - shortCall.getMean_price() * 100.0, 2)) + " for stock price: " + shortPut.getAdjusted_stock_close_price());
//				
//				double closingCost = (longPut.getMean_price() - shortPut.getMean_price() +
//									 longCall.getMean_price() - shortCall.getMean_price()) * 100.0;
				
				// ===========================================================================
				// validate closingCost seems reasonable - cost will be < 0 since it's a debit
				// ===========================================================================
				double stockPrice = shortPut.getAdjusted_stock_close_price();
				
				double itmPutCost = stockPrice < shortPut.getStrike() ? stockPrice - shortPut.getStrike() : 0.0;
				if (itmPutCost != 0.0) {
					double psWidth = longPut.getStrike() - shortPut.getStrike();
					itmPutCost = Math.max(itmPutCost, psWidth);	// buy back cost
				}
				
				double itmCallCost = shortCall.getStrike() < stockPrice ? shortCall.getStrike() - stockPrice : 0.0;
				if (itmCallCost != 0) {
					double csWidth = shortCall.getStrike() - longCall.getStrike();
					itmCallCost = Math.max(itmCallCost, csWidth);
				}
				
				// assuming closing the entire tested spread
				double fees = TradeProperties.CONTRACTS * 2 * TradeProperties.COST_PER_CONTRACT_FEE;
				double closingCost = Utils.round(Math.min(itmPutCost, itmCallCost) * 100 + fees, 2);
				
				System.out.println("Time Closing Cost: " + closingCost + " for stock price: " + shortPut.getAdjusted_stock_close_price());
				
				// ===========================================================================
				// end validation
				// ===========================================================================
				
				// Cash settled should only have to pay the spreadwidth to close, but left 2% extra in there to close.
				//trade.setClosingCost(Utils.round(Math.max(-spreadWidth * 102.0 , closingCost), 2));  
				
				trade.setClosingCost(closingCost);
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(lastOptionTradedCal.getTime());
				
				em.merge(trade);
				
				recordShortClose(trade, shortPut);
				recordLongClose(trade, longPut);
				recordShortClose(trade, shortCall);
				recordLongClose(trade, longCall);
			}
			
			// TODO need better condition
			// Close the Short Put Spread
			if (longCall == null && shortCall == null && longPut != null && shortPut != null) {
				
				double stockPrice = shortPut.getAdjusted_stock_close_price();
				
				// calculate closing cost if ITM
				double itmPutCost = stockPrice < shortPut.getStrike() ? stockPrice - shortPut.getStrike() : 0.0;
				if (itmPutCost != 0.0) {
					double psWidth = longPut.getStrike() - shortPut.getStrike();
					itmPutCost = Math.max(itmPutCost, psWidth);	// buy back cost
				}
				
				double cc = 0.0;
				if (itmPutCost != 0.0) {
					double fees = TradeProperties.CONTRACTS * 2 * TradeProperties.COST_PER_CONTRACT_FEE;
					cc = Utils.round(itmPutCost * 100.0 + fees, 2);
				}

				System.out.println("Time Closing Cost: " + cc);
								
				// should not be able to close for a credit, it should always cost something or be zero, never a credit
				trade.setClosingCost(Math.min(0.0, cc));
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(lastOptionTradedCal.getTime());
				
				em.merge(trade);
				
				// Save the closing price of the short put in the trade details table
				recordShortClose(trade, shortPut);
				
				// Save the closing price of the short put in the trade details table
				recordLongClose(trade, longPut);
			}
			
			// Close the Short Call Spread
			if (longCall != null && shortCall != null && longPut == null && shortPut == null) {
				
				System.out.println("Time Closing Cost: " +  
													 + Utils.round(longCall.getMean_price(),2) + " - " + Utils.round(shortCall.getMean_price(),2));
				
				double stockPrice = shortCall.getAdjusted_stock_close_price();
				
				// calculate closing cost if ITM
				double itmCallCost = stockPrice > shortCall.getStrike() ? shortCall.getStrike() - stockPrice : 0.0;
				if (itmCallCost != 0.0) {
					double psWidth = shortCall.getStrike() - longCall.getStrike(); // width expressed as a debt (negative number)
					itmCallCost = Math.max(itmCallCost, psWidth);	// buy back cost
				}
				
				double cc = 0.0;
				if (itmCallCost != 0.0) {
					double fees = TradeProperties.CONTRACTS * 2 * TradeProperties.COST_PER_CONTRACT_FEE;
					cc = Utils.round(itmCallCost * 100.0 + fees, 2);
				}
				
				// should not be able to close for a credit, it should always cost something or be zero, never a credit
				trade.setClosingCost(Math.min(0.0, cc));
				trade.setProfit(Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(lastOptionTradedCal.getTime());
				
				em.merge(trade);
				
				recordShortClose(trade, shortCall);
				recordLongClose(trade, longCall);
			}
			
			// Close the Covered Call
			if (longStock != null && shortCall != null) {
				double stockClose = Utils.round(shortCall.getAdjusted_stock_close_price(), 2);

				System.out.println("Time Closing Cost: " + stockClose + " - "  
						 + shortCall.getMean_price());

				double fees = TradeProperties.CONTRACTS * TradeProperties.COST_PER_CONTRACT_FEE + TradeProperties.COST_PER_STOCK_TRADE_FEE;
				double closingCost = Math.min(shortCall.getStrike(), (stockClose - shortCall.getMean_price())) * 100.00 + fees;
				trade.setClosingCost(Utils.round(closingCost, 2));
				trade.setProfit(trade.getClosingCost() + trade.getOpeningCost()); //Utils.round(trade.getClosingCost() + trade.getOpeningCost(),2));
				trade.setClose_status(TradeProperties.CLOSE_DTE + " DTE TIME CLOSE");
				trade.setCloseDate(lastOptionTradedCal.getTime());				
				
				em.merge(trade);
				
				// Save the closing price of the short call in the trade details table
				recordShortClose(trade, shortCall);

				// Save closing price of stock
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

	private static void recordShortClose(Trade trade, OptionPricing shortOption) {
		
		String type = shortOption.getCall_put().equals("C") ? "CALL" : "PUT";
		
		TradeDetail tradeDetail = new TradeDetail();
		
		tradeDetail.setExecTime(shortOption.getTrade_date());
		tradeDetail.setExp(shortOption.getExpiration());
		tradeDetail.setPosEffect("CLOSING");
		tradeDetail.setPrice(-shortOption.getMean_price());
		tradeDetail.setQty(TradeProperties.CONTRACTS);
		tradeDetail.setSide("BUY");
		tradeDetail.setStrike(shortOption.getStrike());
		tradeDetail.setSymbol(shortOption.getSymbol());
		tradeDetail.setTrade(trade);
		tradeDetail.setType(type);
		tradeDetail.setStockPrice(shortOption.getAdjusted_stock_close_price());
		String comment = null;
		if (type.equals("CALL") && tradeDetail.getStrike() < tradeDetail.getStockPrice()) {
			comment = "ITM by $" + Utils.round((tradeDetail.getStockPrice() - tradeDetail.getStrike()), 2);
		} else if (type.equals("PUT") && tradeDetail.getStrike() > tradeDetail.getStockPrice()) {
			comment = "ITM by $" + Utils.round((tradeDetail.getStrike() - tradeDetail.getStockPrice()), 2);
		}
		tradeDetail.setComment(comment);
		
		em.persist(tradeDetail);
	}

	private static void recordLongClose(Trade trade, OptionPricing longOption) {
		TradeDetail tradeDetail = new TradeDetail();
		
		tradeDetail.setExecTime(longOption.getTrade_date());
		tradeDetail.setExp(longOption.getExpiration());
		tradeDetail.setPosEffect("CLOSING");
		tradeDetail.setPrice(longOption.getMean_price());
		tradeDetail.setQty(-TradeProperties.CONTRACTS);
		tradeDetail.setSide("SELL");
		tradeDetail.setStrike(longOption.getStrike());
		tradeDetail.setSymbol(longOption.getSymbol());
		tradeDetail.setTrade(trade);
		tradeDetail.setType(longOption.getCall_put().equals("C") ? "CALL" : "PUT");
		tradeDetail.setStockPrice(longOption.getAdjusted_stock_close_price());
		//tradeDetail.setComment("price," + longPut.getAdjusted_stock_close_price());
		
		em.persist(tradeDetail);
	}

}
