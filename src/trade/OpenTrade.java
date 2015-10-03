package trade;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import main.TradeProperties;
import misc.Utils;
import model.OptionPricing;
import model.Trade;
import model.service.OptionPricingService;
import model.service.TradeService;

public class OpenTrade {
	

//	public void addTrade(Date tradeDate) {
//		
//		EntityManager em = emf.createEntityManager();
//
//		// Note: table name refers to the Entity Class and is case sensitive
//		//       field names are property names in the Entity Class
//		Query query = em.createQuery("select opt from Spx opt where " + 
//				"opt.trade_date = :tradeDate and opt.expiration = :expiration");
//		
//		query.setParameter("tradeDate", tradeDate);
//
//		Calendar td = Calendar.getInstance();
//		td.setTime(tradeDate);
//
//		// Month is 0 based
//		Calendar exp = Calendar.getInstance();
//		exp = td;
//		exp.add(Calendar.DATE, TradeProperties.OPEN_DTE);
//		
//		query.setParameter("expiration", exp.getTime());
//		
//		List<Spx> spxRecords = query.getResultList();
//		
//		if (spxRecords.size() == 0) {
//			exp.add(Calendar.DATE, 1);
//			query.setParameter("expiration", exp.getTime());
//			spxRecords = query.getResultList();
//			
//			if (spxRecords.size() == 0) {
//				exp.add(Calendar.DATE, 1);
//				query.setParameter("expiration", exp.getTime());
//				spxRecords = query.getResultList();
//			}
//		}
//		
//		for (Spx spx : spxRecords) {
//			//System.out.println(spx.getAdjusted_stock_close_price());
//			System.out.println(spx.toString());
//		}
//		em.close();
//		
//	}

//	public static void trade(String symbol, Calendar tradeDate) {
//		
//		if (isTradingDay(tradeDate)) {
//			
//		}
//	}
//
//	private static boolean isTradingDay(Calendar tradeDate) {
//		
//		if (tradeDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || tradeDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
//			return false;
//		
//		HolidayService hs = HolidayService.getInstance();
//		Map<Date, String> holidaysMap = hs.getHolidaysMap();
//		
//		if (holidaysMap.containsKey(tradeDate.getTime())) {
//			return false;
//		}
//		
//		return true;
//	}

	public static void findIronCondorChains(Date tradeDate, Date expiration, double openDelta, double spreadWidth) {
		
		String callPut;
		OptionPricingService ops = new OptionPricingService();
		callPut = "C";
		List<OptionPricing> callChain = ops.getOptionChain(tradeDate, expiration, callPut);
		
		if (callChain.isEmpty()) {
			
			Calendar expirationDateCal = Calendar.getInstance();
			expirationDateCal.clear();		    	
			expirationDateCal.setTime(expiration);
			expirationDateCal.add(Calendar.DATE, 1);
			expiration = expirationDateCal.getTime();
			
		    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) 
					+ " expiration: " + Utils.asMMMddYYYY(expiration));
			callChain = ops.getOptionChain(tradeDate, expiration, callPut);
		}
		
//		    for (Spx spx : spxCallChain) {
//				
//		    	System.out.println("runBackTest: tradeDate: "  + ProjectProperties.dateFormat.format(spx.getTrade_date()) 
//		    			+ " expiration: " + ProjectProperties.dateFormat.format(spx.getExpiration()) + " Delta: " + spx.getDelta());
//			}
		
		if (!callChain.isEmpty()) {
		    callPut = "P";
		    List<OptionPricing> putChain = ops.getOptionChain(tradeDate, expiration, callPut);
		    if (!putChain.isEmpty()) {
		    	try {
		    		openIronCondor(callChain, putChain, openDelta, spreadWidth);
		    	} catch (Exception ex) {
		    		ex.printStackTrace();
		    		System.err.println("Problem with put or call chain");
		    	}
		    }
		}
	}
	
	
	public static void openIronCondor(List<OptionPricing> callChain, List<OptionPricing> putChain, double openDelta, double spreadWidth) {
		
		VerticalSpread callSpread = openCallSpread(callChain, openDelta, spreadWidth);
		VerticalSpread putSpread = openPutSpread(putChain, openDelta, spreadWidth);
		
		if (callSpread == null || callSpread.getLongOptionOpen() == null) {
			System.out.println("null pointer problem");
		}
		
		IronCondor ironCondor = new IronCondor();
		ironCondor.setCallSpread(callSpread);
		ironCondor.setPutSpread(putSpread);
		
    	TradeService.openIronCondor(ironCondor);
	}
	
	private static VerticalSpread openPutSpread(List<OptionPricing> putChain, double openDelta, double spreadWidth) {
		
		// Build Pub Bull Debit Spread
		// find short put at delta
		OptionPricing shortPut = null;
		double smallestDiff = 1.0;
		for (OptionPricing put : putChain) {
			
			double diffFromDelta = Math.abs(openDelta + put.getDelta());
			if (diffFromDelta < smallestDiff) {
				shortPut = put;
				smallestDiff = diffFromDelta;
			}
		}
		if (shortPut == null) {
			System.err.println("Could not find a suitable put");
		}
		
		// get long put
		double putStrike = shortPut.getStrike() - spreadWidth;
		OptionPricing longPut = null;
		
		for (OptionPricing put : putChain) {
			if (put.getStrike() == putStrike) {
				longPut = put;
				break;
			}
		}
		if (longPut == null) {
			OptionPricing longPutCandidate = null;
			System.err.println("Could not pair up short put with a long put");
			double diff = 1000;
			for (OptionPricing put : putChain) {
				if (put.getStrike() < putStrike) {	// make sure we are building a debit spread
					if (diff > putStrike - put.getStrike()) {
						longPutCandidate = put;
						diff = putStrike - put.getStrike();
					}
				}				
			}
			longPut = longPutCandidate;		// use the next strike available
		}
		
		VerticalSpread putSpread = new VerticalSpread();
		putSpread.setLongOptionOpen(longPut);
		putSpread.setShortOptionOpen(shortPut);

		return putSpread;
	}

	private static VerticalSpread openCallSpread(List<OptionPricing> callChain, double openDelta, double spreadWidth) {
		
		// Build Call Bear Debit Spread
		
		// find short call at delta
		OptionPricing shortCall = null;
		double smallestDiff = 1.0;
		for (OptionPricing call : callChain) {
			
			double diffFromDelta = Math.abs(openDelta - call.getDelta());
			if (diffFromDelta < smallestDiff) {
				shortCall = call;
				smallestDiff = diffFromDelta;
			}
		}
		
		// get long call
		double longStrike = shortCall.getStrike() + spreadWidth;
		OptionPricing longCall = null;
		
		for (OptionPricing call : callChain) {
			if (call.getStrike() == longStrike) {
				longCall = call;
				break;
			}
		}

		if (longCall == null) {
			OptionPricing longCallCandidate = null;
			System.err.println("Could not pair up short call with a long call");
			double diff = 1000;
			for (OptionPricing call : callChain) {
				if (call.getStrike() > longStrike) {	// make sure we are building a debit spread
					if (diff > call.getStrike() - longStrike) {
						longCallCandidate = call;
						diff = call.getStrike() - longStrike;
					}
				}				
			}
			longCall = longCallCandidate;		// use the next strike available
		}
		

		// TODO remove this set of code
		// This may not fix the problem of missing strikes, but hopefully it will fix most of them.
//		if (longCall == null) {
//			System.err.println("Unable to set long call at " + longStrike);
//			int shortStrike = shortCall.getStrike() + 5; // trying the next level
//			for (OptionPricing call : callChain) {
//				if (call.getStrike() == shortStrike) {
//					shortCall = call;
//				}
//				if (call.getStrike() == shortStrike + TradeProperties.SPREAD_WIDTH) {
//					longCall = call;
//					break;
//				}
//			}			
//		}
		
		VerticalSpread callSpread = new VerticalSpread();
		callSpread.setLongOptionOpen(longCall);
		callSpread.setShortOptionOpen(shortCall);

		return callSpread;
	}


	/**
	 * Builds a covered call trade
	 * @param expiration
	 * @param daysUntilExpiration
	 * @param offset - Strikes from ATM, 0 represents closest to ATM, 
	 * 		+1 is next strike OTM and -1 is first strike ITM.
	 */
	public static void coveredCall(Date expiration, int daysUntilExpiration, int offset) {

		String callPut = "C";

		Date tradeDate = Utils.getTradeDate(expiration, daysUntilExpiration);
		
		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> callChain = ops.getOptionChain(tradeDate, expiration, callPut);
		

		if (callChain.size() > 0) {
			System.out.println("Trading a Covered Call on " + Utils.asMMddYY(tradeDate) + " Expires on " + expiration);

			double stockPrice = callChain.get(0).getAdjusted_stock_close_price();
			
			double price = Math.rint(stockPrice) + offset;
			
			for (OptionPricing option : callChain) {
				
				if (option.getStrike() == price) {
					TradeService.openCoveredCall(option);
				}
			}
		} else {
			System.err.println("No options returns from call chain - Trade date " + Utils.asMMddYY(tradeDate) + " Expires on " + expiration);
		}
		
	}


	public static CoveredStraddle initializeCoveredStraddle(Date expiration, int daysUntilExpiration) {

		double smallestDiff = 100.0;
		Date leapExpiration = null;

		CoveredStraddle coveredStraddle = null;
		
		OptionPricing shortCall = null;
		OptionPricing shortPut = null;
		OptionPricing longCall = null;
		OptionPricing longPut = null;
		
		Date tradeDate = Utils.getTradeDate(expiration, daysUntilExpiration);
		
		String callPut = "C";
		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> callChain = ops.getOptionChain(tradeDate, expiration, callPut);

		if (callChain.size() > 0) {
			System.out.println("Trading a Covered Straddle  on " + Utils.asMMddYY(tradeDate) + " Expires on " + Utils.asMMddYY(expiration));

			double stockPrice = callChain.get(0).getAdjusted_stock_close_price();
			double price = Math.rint(stockPrice);
			
			// find the short call
			for (OptionPricing option : callChain) {

				double diffFromPrice = Math.abs(price - option.getStrike());
				if (diffFromPrice < smallestDiff) {
					shortCall = option;
					smallestDiff = diffFromPrice;
				}
			}
			
			// get the short put
			callPut = "P";
			shortPut = OptionPricingService.getRecord(tradeDate, expiration, shortCall.getStrike(), callPut);
			coveredStraddle = new CoveredStraddle();
			coveredStraddle.setShortCall(shortCall);
			coveredStraddle.setShortPut(shortPut);
			
			
			// Search for long leap option
			List<Date> monthlys = Utils.getMonthlyExpirations();
			List<Date> expirationsAtTradeDate = ops.getExpirationsForTradeDate(tradeDate);

			// finding the leap expiration, the last one should be the leap
			for (Date expireDate : expirationsAtTradeDate) {
				if (monthlys.contains(expireDate))
					leapExpiration = expireDate;
			}
			
			// Getting the option record
			smallestDiff = 500.0;
			callPut = "C";
			List<OptionPricing> leapCallChain = ops.getOptionChain(tradeDate, leapExpiration, callPut);
			for (OptionPricing option : leapCallChain) {

				double diffFromPrice = Math.abs(price - option.getStrike());
				if (diffFromPrice < smallestDiff) {
					longCall = option;
					smallestDiff = diffFromPrice;
				}
			}
			
			callPut = "P";
			longPut = OptionPricingService.getRecord(tradeDate, leapExpiration, longCall.getStrike(), callPut);
			coveredStraddle.setLongCall(longCall);
			coveredStraddle.setLongPut(longPut);
			
			Trade trade = TradeService.openCoveredStraddle(coveredStraddle);
			coveredStraddle.setTrade(trade);
		} else {
			System.err.println("No options returns from call chain - Trade date " + Utils.asMMddYY(tradeDate) + " Expires on " + expiration);
		}
		
		return coveredStraddle;
	}

}
