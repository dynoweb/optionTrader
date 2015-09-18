package trade;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import main.TradeProperties;
import misc.Utils;
import model.Spx;
import model.service.SpxService;
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
		SpxService spxService = new SpxService();
		callPut = "C";
		List<Spx> spxCallChain = spxService.getOptionChain(tradeDate, expiration, callPut);
		
		if (spxCallChain.isEmpty()) {
			
			Calendar expirationDateCal = Calendar.getInstance();
			expirationDateCal.clear();		    	
			expirationDateCal.setTime(expiration);
			expirationDateCal.add(Calendar.DATE, 1);
			expiration = expirationDateCal.getTime();
			
		    System.out.println("checking: tradeDate: "  + Utils.asMMMddYYYY(tradeDate) 
					+ " expiration: " + Utils.asMMMddYYYY(expiration));
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
		    openIronCondor(spxCallChain, spxPutChain);
		}
	}
	
	
	public static void openIronCondor(List<Spx> spxCallChain, List<Spx> spxPutChain) {
		
		VerticalSpread callSpread = openCallSpread(spxCallChain);
		VerticalSpread putSpread = openPutSpread(spxPutChain);
		
		if (callSpread == null || callSpread.getLongOptionOpen() == null) {
			System.out.println("null pointer problem");
		}
		
		IronCondor ironCondor = new IronCondor();
		ironCondor.setCallSpread(callSpread);
		ironCondor.setPutSpread(putSpread);
		
    	TradeService.openIronCondor(ironCondor);
	}
	
	private static VerticalSpread openPutSpread(List<Spx> spxPutChain) {
		
		// Build Pub Bull Debit Spread
		// find short put at delta
		Spx spxShortPut = null;
		double smallestDiff = 1.0;
		for (Spx spxPut : spxPutChain) {
			
			double diffFromDelta = Math.abs(TradeProperties.OPEN_DELTA + spxPut.getDelta());
			if (diffFromDelta < smallestDiff) {
				spxShortPut = spxPut;
				smallestDiff = diffFromDelta;
			}
		}
		
		// get long put
		int putStrike = spxShortPut.getStrike() - TradeProperties.SPREAD_WIDTH;
		Spx spxLongPut = null;
		
		for (Spx spxPut : spxPutChain) {
			if (spxPut.getStrike() == putStrike) {
				spxLongPut = spxPut;
				break;
			}
		}
		
		VerticalSpread putSpread = new VerticalSpread();
		putSpread.setLongOptionOpen(spxLongPut);
		putSpread.setShortOptionOpen(spxShortPut);

		return putSpread;
	}

	private static VerticalSpread openCallSpread(List<Spx> spxCallChain) {
		
		// Build Call Bear Debit Spread
		
		// find short call at delta
		Spx spxShortCall = null;
		double smallestDiff = 1.0;
		for (Spx spxCall : spxCallChain) {
			
			double diffFromDelta = Math.abs(TradeProperties.OPEN_DELTA - spxCall.getDelta());
			if (diffFromDelta < smallestDiff) {
				spxShortCall = spxCall;
				smallestDiff = diffFromDelta;
			}
		}
		
		// get long call
		int longStrike = spxShortCall.getStrike() + TradeProperties.SPREAD_WIDTH;
		Spx spxLongCall = null;
		
		for (Spx spxCall : spxCallChain) {
			if (spxCall.getStrike() == longStrike) {
				spxLongCall = spxCall;
				break;
			}
		}
		
		// This may not fix the problem of missing strikes, but hopefully it will fix most of them.
		if (spxLongCall == null) {
			System.err.println("Unable to set long call at " + longStrike);
			int shortStrike = spxShortCall.getStrike() + 5; // trying the next level
			for (Spx spxCall : spxCallChain) {
				if (spxCall.getStrike() == shortStrike) {
					spxShortCall = spxCall;
				}
				if (spxCall.getStrike() == shortStrike + TradeProperties.SPREAD_WIDTH) {
					spxLongCall = spxCall;
					break;
				}
			}			
		}
		
		VerticalSpread callSpread = new VerticalSpread();
		callSpread.setLongOptionOpen(spxLongCall);
		callSpread.setShortOptionOpen(spxShortCall);

		return callSpread;
	}

}
