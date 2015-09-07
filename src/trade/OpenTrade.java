package trade;

import java.util.List;

import model.Spx;
import model.Trade;
import model.service.TradeService;

public class OpenTrade {
	
	private static int qty = 1;

	public OpenTrade() {
		
	}
	
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

	public static void openIronCondor(List<Spx> spxCallChain, List<Spx> spxPutChain, int contracts) {
		
		VerticalSpread callSpread = openCallSpread(spxCallChain);
		VerticalSpread putSpread = openPutSpread(spxPutChain);
		
		IronCondor ironCondor = new IronCondor();
		ironCondor.setCallSpread(callSpread);
		ironCondor.setPutSpread(putSpread);
		
    	TradeService.openIronCondor(ironCondor, contracts);
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
		putSpread.setLongOptionOpen(spxLongPut, qty);
		putSpread.setShortOptionOpen(spxShortPut, qty );

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
		
		VerticalSpread callSpread = new VerticalSpread();
		callSpread.setLongOptionOpen(spxLongCall, qty);
		callSpread.setShortOptionOpen(spxShortCall, qty);

		return callSpread;
	}

}
