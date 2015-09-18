package trade;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import main.TradeProperties;
import misc.Utils;
import model.OptionPricing;
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

	public static void findIronCondorChains(Date tradeDate, Date expiration) {
		
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
		    openIronCondor(callChain, putChain);
		}
	}
	
	
	public static void openIronCondor(List<OptionPricing> callChain, List<OptionPricing> putChain) {
		
		VerticalSpread callSpread = openCallSpread(callChain);
		VerticalSpread putSpread = openPutSpread(putChain);
		
		if (callSpread == null || callSpread.getLongOptionOpen() == null) {
			System.out.println("null pointer problem");
		}
		
		IronCondor ironCondor = new IronCondor();
		ironCondor.setCallSpread(callSpread);
		ironCondor.setPutSpread(putSpread);
		
    	TradeService.openIronCondor(ironCondor);
	}
	
	private static VerticalSpread openPutSpread(List<OptionPricing> putChain) {
		
		// Build Pub Bull Debit Spread
		// find short put at delta
		OptionPricing shortPut = null;
		double smallestDiff = 1.0;
		for (OptionPricing put : putChain) {
			
			double diffFromDelta = Math.abs(TradeProperties.OPEN_DELTA + put.getDelta());
			if (diffFromDelta < smallestDiff) {
				shortPut = put;
				smallestDiff = diffFromDelta;
			}
		}
		
		// get long put
		int putStrike = shortPut.getStrike() - TradeProperties.SPREAD_WIDTH;
		OptionPricing longPut = null;
		
		for (OptionPricing put : putChain) {
			if (put.getStrike() == putStrike) {
				longPut = put;
				break;
			}
		}
		
		VerticalSpread putSpread = new VerticalSpread();
		putSpread.setLongOptionOpen(longPut);
		putSpread.setShortOptionOpen(shortPut);

		return putSpread;
	}

	private static VerticalSpread openCallSpread(List<OptionPricing> callChain) {
		
		// Build Call Bear Debit Spread
		
		// find short call at delta
		OptionPricing shortCall = null;
		double smallestDiff = 1.0;
		for (OptionPricing call : callChain) {
			
			double diffFromDelta = Math.abs(TradeProperties.OPEN_DELTA - call.getDelta());
			if (diffFromDelta < smallestDiff) {
				shortCall = call;
				smallestDiff = diffFromDelta;
			}
		}
		
		// get long call
		int longStrike = shortCall.getStrike() + TradeProperties.SPREAD_WIDTH;
		OptionPricing longCall = null;
		
		for (OptionPricing call : callChain) {
			if (call.getStrike() == longStrike) {
				longCall = call;
				break;
			}
		}
		
		// This may not fix the problem of missing strikes, but hopefully it will fix most of them.
		if (longCall == null) {
			System.err.println("Unable to set long call at " + longStrike);
			int shortStrike = shortCall.getStrike() + 5; // trying the next level
			for (OptionPricing call : callChain) {
				if (call.getStrike() == shortStrike) {
					shortCall = call;
				}
				if (call.getStrike() == shortStrike + TradeProperties.SPREAD_WIDTH) {
					longCall = call;
					break;
				}
			}			
		}
		
		VerticalSpread callSpread = new VerticalSpread();
		callSpread.setLongOptionOpen(longCall);
		callSpread.setShortOptionOpen(shortCall);

		return callSpread;
	}

}
