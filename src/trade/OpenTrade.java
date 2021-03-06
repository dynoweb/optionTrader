package trade;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

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

	/**
	 * Builds a covered call trade
	 * @param expiration
	 * @param daysUntilExpiration
	 * @param delta - used for short strike selection
	 */
	public static void coveredCall(Date expiration, int daysUntilExpiration, double delta) {

		String callPut = "C";

		Date tradeDate = Utils.getTradeDate(expiration, daysUntilExpiration);

		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> callChain = ops.getOptionChain(tradeDate, expiration, callPut);
		
		if (callChain.size() > 0) {
			
			OptionPricing callOption = findOptionAtDelta(callChain, delta);
					
			double price = callOption.getAdjusted_stock_close_price();
			System.out.println("Trading a Covered Call on " + Utils.asMMddYY(tradeDate) + " Expires on " + Utils.asMMddYY(expiration) + " Current price: " + price);

			TradeService.openCoveredCall(callOption);
		}
	}
	
	
	public static void findIronCondorChains(Date tradeDate, Date expiration, double shortDelta, double longDelta, double spreadWidth) {
		
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
		    		openIronCondor(callChain, putChain, shortDelta, longDelta, spreadWidth);
		    	} catch (Exception ex) {
//		    		ex.printStackTrace();
//		    		System.err.println("Problem with put or call chain");
		    	}
		    }
		}
	}
	
	/**
	 * Returns the option nearest the specified delta
	 * 
	 * @param optionChain
	 * @param targetDelta
	 * @return OptionPricing
	 */
	private static OptionPricing findOptionAtDelta(List<OptionPricing> optionChain, double targetDelta) {
		
		// find option at delta
		OptionPricing option = null;
		double smallestDiff = 1.0;
		for (OptionPricing opt : optionChain) {
			
			double diffFromDelta = Math.abs(targetDelta - Math.abs(opt.getDelta()));
			if (diffFromDelta < smallestDiff) {
				option = opt;
				smallestDiff = diffFromDelta;
			}
		}
		return option;
	}


//	private static OptionPricing openShortCall(List<OptionPricing> callChain, double openDelta) {
//		
//		// Build short call
//		OptionPricing shortCall = findOptionAtDelta(callChain, openDelta);
//		if (shortCall == null) {
//			System.err.println("Could not find a suitable call");
//		}
//				
//		return shortCall;
//	}


	/**
	 * Finds the short call on that trade date with the given expiration at the specified delta
	 * 
	 * @param tradeDate
	 * @param expiration
	 * @param delta
	 */
//	public static void findShortCall(Date tradeDate, Date expiration, double delta) {
//
//		String callPut = "C";
//		OptionPricingService ops = new OptionPricingService();
//		List<OptionPricing> callChain = ops.getOptionChain(tradeDate, expiration, callPut);
//		
//	    if (!callChain.isEmpty()) {
//	    	try {
//	    		OptionPricing shortCall = openShortCall(callChain, delta);
//	    		
//	    		   // validate contract   			// short option is priced                  // credit is created
//	    		if (shortCall != null && shortCall.getBid() != 0 && shortCall.getAsk() != 0 && shortCall.getMean_price() > 0) {
//	    			
//	    			if (shortCall.getMean_price() != 0.0 && shortCall.getDelta() != 0 ) {
//	    				
//	    				// write the opening Trade and TradeDetails to the database
//	    				TradeService.recordShortCall(shortCall);
//	    			}
//	    		}
//	    	} catch (Exception ex) {
//	    		ex.printStackTrace();
//	    		System.err.println("Problem with call chain");
//	    	}
//	    } else {
//	    	System.err.println("Call chain is empty for tradeDate: " + tradeDate + " and expiration: " + expiration);
//	    }
//	}


	private static OptionPricing getOptionAtStrike(Date tradeDate, Date expiration, Double strike, String callPut) {

		OptionPricing option = null;
		
		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> optChain = ops.getOptionChain(tradeDate, expiration, callPut);
		
	    if (!optChain.isEmpty()) {
			for (OptionPricing optionPrice : optChain) {
				if (optionPrice.getStrike() == strike) {
					option = optionPrice;
					break;
				}
			}
	    } else {
	    	System.err.println("Option chain is empty for tradeDate: " + tradeDate + " and expiration: " + expiration);
	    }
	    return option;
	}


	/**
	 * 
	 * @param tradeDate
	 * @param expiration
	 * @param priceOffset	0 for ATM, positive offset of price above ATM, and negative for below ATM 
	 * @param callPut "P" or "C"
	 * @return
	 */
	private static OptionPricing getOptionAtmPlusOffset(Date tradeDate, Date expiration, Double priceOffset, String callPut) {

		OptionPricing option = null;
		
		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> optChain = ops.getOptionChain(tradeDate, expiration, callPut);
		
	    if (!optChain.isEmpty()) {
			for (OptionPricing optionItem : optChain) {
				if (optionItem.getStrike() > (optionItem.getAdjusted_stock_close_price() + priceOffset)) {
					option = optionItem;
					break;
				}
			}
			// write the opening Trade and TradeDetails to the database
			//TradeService.recordShort(shortOpt);
	    } else {
	    	System.err.println("Option chain is empty for tradeDate: " + tradeDate + " and expiration: " + expiration);
	    }
	    return option;
	}

	public static OptionPricing findShort(Date tradeDate, Date expiration, double delta, String callPut) {

		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> optChain = ops.getOptionChain(tradeDate, expiration, callPut);
		OptionPricing shortOpt = null;
		
	    if (!optChain.isEmpty()) {
	    	try {
	    		shortOpt = findOptionAtDelta(optChain, delta);
	    		
	    		   // validate contract
	    		if (shortOpt.getBid() == 0 || shortOpt.getAsk() == 0 
	    				|| shortOpt.getMean_price() == 0 || shortOpt.getDelta() == 0) {
	    			shortOpt = null;
	    		}
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    		System.err.println("Problem with option chain");
	    	}
	    } else {
	    	System.err.println("Option chain is empty for tradeDate: " + tradeDate + " and expiration: " + expiration);
	    }
    	return shortOpt;
	}

	public static void findShortOptionSpread(Date tradeDate, Date expiration, double shortDelta, double longDelta, double spreadWidth, String callPut) {

		//String callPut = "C";
		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> optChain = ops.getOptionChain(tradeDate, expiration, callPut);
		
	    if (!optChain.isEmpty()) {
	    	try {
	    		VerticalSpread optSpread = null;
	    		if (callPut.equals("C")) {
	    			optSpread = openCallSpread(optChain, shortDelta, longDelta, spreadWidth);
	    		} else {
	    			optSpread = openPutSpread(optChain, shortDelta, longDelta, spreadWidth);
	    		}
	    		// Validating contract pricing
	    		// found long and short contract
	    		if (optSpread.getShortOptionOpen() != null && optSpread.getLongOptionOpen() != null
	    				// long option is priced
	    				&& optSpread.getLongOptionOpen().getBid() != 0 && optSpread.getLongOptionOpen().getAsk() != 0
	    				// short option is priced
	    				&& optSpread.getShortOptionOpen().getBid() != 0 && optSpread.getShortOptionOpen().getAsk() != 0
	    				// credit is created
	    				&& optSpread.getOpenCost() < 0) { 
	    			if (optSpread.getOpenCost() != 0.0 &&
	    					optSpread.getShortOptionOpen().getDelta() != 0 && optSpread.getLongOptionOpen().getDelta() != 0) {
	    				TradeService.recordShortOptSpread(optSpread);
	    			}

	    		}
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    		System.err.println("Problem with option chain");
	    	}
	    } else {
	    	System.err.println("Option chain is empty for tradeDate: " + tradeDate + " and expiration: " + expiration);
	    }
	}


	private static void findShortPutSpread(Date tradeDate, Date expiration, double shortDelta, double longDelta, double spreadWidth) {

		String callPut = "P";
		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> putChain = ops.getOptionChain(tradeDate, expiration, callPut);
		
	    if (!putChain.isEmpty()) {
	    	try {
	    		VerticalSpread putSpread = openPutSpread(putChain, shortDelta, longDelta, spreadWidth);
	    		// Validating contract pricing
	    		// found long and short contract
	    		if (putSpread.getShortOptionOpen() != null && putSpread.getLongOptionOpen() != null
	    				// long option is priced
	    				&& putSpread.getLongOptionOpen().getBid() != 0 && putSpread.getLongOptionOpen().getAsk() != 0
	    				// short option is priced
	    				&& putSpread.getShortOptionOpen().getBid() != 0 && putSpread.getShortOptionOpen().getAsk() != 0
	    				// credit is created
	    				&& putSpread.getOpenCost() < 0) { 
	    			if (putSpread.getOpenCost() != 0.0 &&
	    					putSpread.getShortOptionOpen().getDelta() != 0 && putSpread.getLongOptionOpen().getDelta() != 0) {
	    				TradeService.recordShortOptSpread(putSpread);
	    			}

	    		}
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    		System.err.println("Problem with option chain");
	    	}
	    } else {
	    	System.err.println("Option chain is empty for tradeDate: " + tradeDate + " and expiration: " + expiration);
	    }
	}


	public static CoveredStraddle initializeCoveredStraddle(Date expiration, int daysUntilExpiration, double initialDelta) {

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
				double diffFromDelta = Math.abs(initialDelta - option.getDelta());
				if (initialDelta == 0.50) {
					if (diffFromPrice < smallestDiff) {
						shortCall = option;
						smallestDiff = diffFromPrice;
					}
				} else {
					if (diffFromDelta < smallestDiff) {
						shortCall = option;
						smallestDiff = diffFromDelta;
					}
				}
				
			}
			
			// TODO This made some assumptions about the put side - need to generalize
			
			// get the short put
			callPut = "P";
			shortPut = OptionPricingService.getRecord(tradeDate, expiration, shortCall.getStrike(), callPut);
			coveredStraddle = new CoveredStraddle();
			coveredStraddle.setShortCall(shortCall);
			coveredStraddle.setShortPut(shortPut);
			
			
			// Search for long leap option
			List<Date> monthlys = Utils.getMonthlyExpirations();
			List<Date> expirationsAtTradeDate = OptionPricingService.getExpirationsForTradeDate(tradeDate);

			// finding the leap expiration, the last one should be the leap
			for (Date expireDate : expirationsAtTradeDate) {
				if (monthlys.contains(expireDate))
					leapExpiration = expireDate;
			}
						
			// Getting the option record		
			callPut = "C";
			if (initialDelta == 0.50) {
				longCall = ops.getOptionByStrike(tradeDate, leapExpiration, callPut, price);
			} else {
				longCall = ops.getOptionByDelta(tradeDate, leapExpiration, callPut, initialDelta);
			}
			
//			smallestDiff = 500.0;
//			callPut = "C";
//			List<OptionPricing> leapCallChain = ops.getOptionChain(tradeDate, leapExpiration, callPut);
//			for (OptionPricing option : leapCallChain) {
//
//				double diffFromPrice = Math.abs(price - option.getStrike());
//				double diffFromDelta = Math.abs(initialDelta - option.getDelta());
//				if (initialDelta == 0.50) {
//					if (diffFromPrice < smallestDiff) {
//						longCall = option;
//						smallestDiff = diffFromPrice;
//				}
//				} else {
//					if (diffFromDelta < smallestDiff) {
//						longCall = option;
//						smallestDiff = diffFromDelta;
//					}
//				}
//			}
			
			callPut = "P";
			longPut = OptionPricingService.getRecord(tradeDate, leapExpiration, longCall.getStrike(), callPut);
			coveredStraddle.setLongCall(longCall);
			coveredStraddle.setLongPut(longPut);
			
			Trade trade = TradeService.openCoveredStraddle(coveredStraddle);
			coveredStraddle.setTrade(trade);
		} else {
			System.err.println("No options returned from call chain - Trade date " + Utils.asMMddYY(tradeDate) + " Expires on " + expiration);
		}
		
		return coveredStraddle;
	}

	/**
	 * Finds trade details for a calendar trade.  This will be different from other OpenTrades since 
	 * it will be opening trades daily to get test instances.
	 * @param nearDte
	 * @param farDte
	 * @param delta
	 * @param putCall "P" or "C"
	 */
	public static void openCalendar(int nearDte, int farDte, double delta, String putCall) {

		boolean useWeekly = true;		

		List<Date> expirationList = null;
		
		if (useWeekly) {
			expirationList = Utils.getExpirations();
		} else {
			expirationList = Utils.getMonthlyExpirations();
		}

		Date[] expirations = expirationList.toArray(new Date[expirationList.size()]);
		
		List<Date> tradeDays = OptionPricingService.getTradeDays();

		for (Date tradeDate : tradeDays) {
			
			Date longExpiration = null;
			Date shortExpiration = null;

			// find short date
			// from JDK to Joda
			DateTime jOpenDate = new DateTime(tradeDate);
			DateTime jNearExpDate = new DateTime(jOpenDate.plusDays(nearDte)); 
			DateTime jFarExpDate = new DateTime(jOpenDate.plusDays(farDte));
			
			// TODO - TRADE FILTER - remove me to clear filter, this is here to make it run faster
//			if (jOpenDate.getYear() != 2015) { // || jOpenDate.getMonthOfYear() == 18) { // || jOpenDate.getDayOfMonth() != 22) {
//				continue;
//			}
//			if (jOpenDate.getYear() < 2018) {
//				continue;	// skip these years				
//			}
			
			// find short expiration
//			for (int i = 0; i < expirations.length; i++) {
//				DateTime jExp = new DateTime(expirations[i]);
//				if (jExp.isAfter(jNearExpDate) && jExp.isBefore(jFarExpDate)) {
//					shortExpiration = jExp.toDate();
//					// TODO do this better than this, it could still be assigning the wrong date to the out leg, should also consider handling different intervals
//					if (jExp.plusDays(20).isAfter(new DateTime(expirations[i+1]))) {						
//						// hopefully this is long enough
//						longExpiration = expirations[i+2];
//					} else {
//						longExpiration = expirations[i+1];
//					}
//					break;
//				}
//			}

			// find short expiration
			Arrays.sort(expirations);	// binarySearch assumes array is sorted
			int position = Arrays.binarySearch(expirations, jNearExpDate.toDate());
			// Note another option to using binarySearch would be using a list
			//   List<Date> list = Arrays.asList(expirations);
		    //   list.contains(item);
			if (position >= 0) {
				shortExpiration = jNearExpDate.toDate();
				// find long expiration
				position = Arrays.binarySearch(expirations, jFarExpDate.toDate());
				if (position >= 0) {
					longExpiration = jFarExpDate.toDate();
				} else {
					// is there an expiration one more day later? 
					position = Arrays.binarySearch(expirations, jFarExpDate.plusDays(1).toDate());
					if (position >= 0) {
						longExpiration = expirations[position];
					}
				}
			}

						
			// if it found trade-able dates, get strikes
			if (shortExpiration != null && longExpiration != null) {				
				OptionPricing shortOption = OpenTrade.findShort(tradeDate, shortExpiration, delta, putCall);
				
				if (shortOption != null) {
					System.out.println("Opening Calendar on:" + Utils.asMMddYY(tradeDate) + " strike:" + shortOption.getStrike() 
							+ "  nearExpiry:" + Utils.asMMddYY(shortExpiration) + " farExpiry:" + Utils.asMMddYY(longExpiration));
					
					OptionPricing longOption = OpenTrade.getOptionAtStrike(tradeDate, longExpiration, shortOption.getStrike(), putCall);
			    				
					if (longOption != null) {
						CalendarSpread calendarSpread = new CalendarSpread();
						calendarSpread.setShortOptionOpen(shortOption);
						calendarSpread.setLongOptionOpen(longOption);
						
				    	TradeService.recordCalendarSpread(calendarSpread);
					}
				}
			}
		}
		
		
	}

	private static VerticalSpread openCallSpread(List<OptionPricing> callChain, double shortDelta, double longDelta, double spreadWidth) {
		
		// find short call at delta
		OptionPricing shortCall = findOptionAtDelta(callChain, shortDelta);
		
		// get long call
		OptionPricing longCall = null;
		double longStrike = 0;
		
		if (spreadWidth > 0) {
			longStrike = shortCall.getStrike() + spreadWidth;
			for (OptionPricing call : callChain) {
				if (call.getStrike() == longStrike) {
					longCall = call;
					break;
				}
			}
		} else if (longDelta != 0) {
			longCall = findOptionAtDelta(callChain, longDelta);
		}

		VerticalSpread callSpread = new VerticalSpread();
		callSpread.setLongOptionOpen(longCall);
		callSpread.setShortOptionOpen(shortCall);

		return callSpread;
	}


	public static void openIronCondor(List<OptionPricing> callChain, List<OptionPricing> putChain, double shortDelta, double longDelta, double spreadWidth) {
		
		VerticalSpread callSpread = openCallSpread(callChain, shortDelta, longDelta, spreadWidth);
		VerticalSpread putSpread = openPutSpread(putChain, shortDelta, longDelta, spreadWidth);
		
		if (callSpread == null || callSpread.getLongOptionOpen() == null) {
			System.out.println("null pointer problem");
		}
		
		IronCondor ironCondor = new IronCondor();
		ironCondor.setCallSpread(callSpread);
		ironCondor.setPutSpread(putSpread);
		
    	TradeService.openIronCondor(ironCondor);
	}


	private static VerticalSpread openPutSpread(List<OptionPricing> putChain, double shortDelta, double longDelta, double spreadWidth) {
		
		// set to true if creating a spread of 1 buy 1 isn't available, then find the next available option
//		boolean enableWidthExtention = false;
		
		// Build Put Bull Credit Spread
		OptionPricing shortPut = findOptionAtDelta(putChain, shortDelta);
		if (shortPut == null) {
			System.err.println("Could not find a suitable put");
		}
		
		// get long put
		OptionPricing longPut = null;
		double longStrike = 0;

		if (spreadWidth > 0) {
			longStrike = shortPut.getStrike() - spreadWidth;
			for (OptionPricing put : putChain) {
				if (put.getStrike() == longStrike) {
					longPut = put;
					break;
				}
			}
		} else if (longDelta != 0) {
			longPut = findOptionAtDelta(putChain, longDelta);
		}
				
		// Add a non-standard spread width long put
//		if (enableWidthExtention && longPut == null) {
//			OptionPricing longPutCandidate = null;
//			System.err.println("Could not pair up short put with a long put");
//			double diff = 1000;
//			for (OptionPricing put : putChain) {
//				if (put.getStrike() < putStrike) {	// make sure we are building a debit spread
//					if (diff > putStrike - put.getStrike()) {
//						longPutCandidate = put;
//						diff = putStrike - put.getStrike();
//					}
//				}				
//			}
//			longPut = longPutCandidate;		// use the next strike available
//		}
		
		VerticalSpread putSpread = new VerticalSpread();
		putSpread.setLongOptionOpen(longPut);
		putSpread.setShortOptionOpen(shortPut);

		return putSpread;
	}


	public static void open_20_40_60_Butterfly(Date expiration, int dte) {

		// find short date
		// from JDK to Joda
		DateTime jExpDate = new DateTime(expiration);
		DateTime jTradeDate = new DateTime(jExpDate.minusDays(dte)); 
		
		String callPut = "P";
		OptionPricingService ops = new OptionPricingService();
		List<OptionPricing> putChain = ops.getOptionChain(jTradeDate.toDate(), expiration, callPut);
		
	    if (!putChain.isEmpty()) {
	    	try {
	    		// find lower put at delta
	    		OptionPricing lowerPut = findOptionAtDelta(putChain, TradeProperties.LOWER_DELTA);
	    		OptionPricing middlePut = findOptionAtDelta(putChain, TradeProperties.MID_DELTA);
	    		OptionPricing upperPut = findOptionAtDelta(putChain, TradeProperties.UPPER_DELTA);
	    		
	    		Butterfly bf = new Butterfly();
	    		bf.setLowerOptionOpen(lowerPut);
	    		bf.setMiddleOptionOpen(middlePut);
	    		bf.setUpperOptionOpen(upperPut);
	    		
	    		TradeService.recordButterfly(bf);
	    		
	    	} catch (Exception ex) {
//	    		ex.printStackTrace();
//	    		System.err.println("Problem with put or call chain");
	    	}
	    }

	    
		
	}




}
