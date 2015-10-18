package trade.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import main.TradeProperties;
import misc.Utils;
import model.OptionPricing;
import model.TradeDetail;
import model.service.OptionPricingService;
import model.service.TradeService;
import trade.CoveredStraddle;

public class CoveredCallTradeManager {

	CoveredStraddle coveredStraddle;
	List<Date> expirations;
	int daysTillExpiration;
	double initialDelta;

	EntityManager em = null;
	
	public CoveredCallTradeManager(CoveredStraddle coveredStraddle, List<Date> expirations, int daysTillExpiration, double initialDelta) {
		
		this.coveredStraddle = coveredStraddle;
		this.expirations = expirations;
		this.daysTillExpiration = daysTillExpiration;
		this.initialDelta = initialDelta;
		
//		// A little house keeping - but causes a concurrent exception
//		for (Date expiration : expirations) {
//			if (expiration.before(this.coveredStraddle.getShortCall().getExpiration())) {
//				expirations.remove(expirations.indexOf(expiration));
//			}
//		}
	}
	
	public void manageTrade() {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		
		em = emf.createEntityManager();		
		em.getTransaction().begin();
		
		manageCoveredStraddle();
		
		em.getTransaction().commit();
		em.close();
		emf.close();
	}
	
	private void manageCoveredStraddle() {

		// first day of trade (a trade may last several years and consist of several different transaction histories)
		Calendar tradeDateCal = Utils.dateToCal(OptionPricingService.getFirstTradeDate());
		Calendar lastTradeDateCal = Utils.dateToCal(OptionPricingService.getLastTradeDate());
				
		for (Date expiration : expirations) {
			System.out.println("Managing trade for expiration period: " + Utils.asMMddYY(expiration));
			Calendar expirationCal = Utils.dateToCal(expiration);
			
			while (tradeDateCal.before(lastTradeDateCal) && tradeDateCal.before(expirationCal)) {
				tradeDateCal.add(Calendar.DATE, 1);
				
				if (Utils.isTradableDay(tradeDateCal.getTime())) {
					
					System.out.println("Checking trade day " + Utils.asMMddYY(tradeDateCal.getTime()));
					
					inversionRepair(expiration, tradeDateCal.getTime());				
					rollShortCall(expiration, tradeDateCal.getTime());
					rollShortPut(expiration, tradeDateCal.getTime());
				}
			}
		}
	}

	private void inversionRepair(Date expiration, Date tradeDate) {
		// TODO check if they are at the same expiration period
		
	}

	private void rollShortPut(Date expiration, Date tradeDate) {
		
		
	}
	
	private void rollShortCall(Date expiration, Date tradeDate) {
		
		OptionPricing shortCall = coveredStraddle.getShortCall();

		// If tradeDate after expiration
		if (tradeDate.after(expiration)) {
			return;
		}
		// Already rolled to the next expiration, no action required
		if (!shortCall.getExpiration().equals(expiration)) {
			return;
		}
		
		Calendar tradeDateCal = Calendar.getInstance();
		tradeDateCal.setTime(tradeDate);
		
		// Check the current status of the position
		OptionPricing option = OptionPricingService.getRecord(tradeDate, shortCall.getExpiration(), shortCall.getStrike(), "C");
		
		
		// Roll if delta is less than 0.10 or ITM and 2 days until expiration (Thursday EOD)
		// also if expiration is on a Mon or Tue (qrtly) need to move back the by date
		// calculate roll deadline
		Calendar rollByDate = Calendar.getInstance();
		rollByDate.setTime(shortCall.getExpiration());
		rollByDate.add(Calendar.DATE, -1);	// going back 2 days, but need to loop only 1
		do {
			rollByDate.add(Calendar.DATE, -1);
		} while (!Utils.isTradableDay(rollByDate.getTime()));
		
		//roll to the next period
		if (tradeDateCal.equals(rollByDate) || tradeDateCal.after(rollByDate)) {
			rollToNextPeriodTypeII(option, "TIME");
			return;
		}
			
		if (option.getMean_price() < 0.1) {
			rollToNextPeriodTypeII(option, "PRICE");
			return;
		}
	}

	/**
	 * This rolls the next call up to the first ITM strike or rolls down to the first OTM strike.
	 * Seems really hard to make a profit on IWM
	 * 
	 * @param option
	 * @param rollComment
	 */
	private void rollToNextPeriodTypeI(OptionPricing option, String rollComment) {
		
		// Getting next expiration to potentially roll to
		int currentExpIndex = expirations.indexOf(option.getExpiration());

		// Roll to next expiration considering DTE
		Date nextExpiration = null;
		do {
			currentExpIndex += 1;
			if (expirations.size() > currentExpIndex) {
				nextExpiration = expirations.get(currentExpIndex);
			} else {
				System.err.println("Unable to roll to next expiration");
				return;
			}
		} while (Utils.calculateDaysBetween(option.getTrade_date(), nextExpiration) <= this.daysTillExpiration);
			
		System.out.println("Rolling " + option);
		
		// Close current option
		TradeDetail closeShortCall = TradeService.initializeTradeDetail(option, TradeProperties.CONTRACTS, "CLOSING", "BUY", rollComment);
		
		closeShortCall.setTrade(this.coveredStraddle.getTrade());		
		em.persist(closeShortCall);

		// Rolling strategy - roll up under price or down above price
		// could consider a strategy that only rolls 1/2 or 1/3 up to price 
		// If rolling down, roll to next strike above price, if rolling up, roll to first strike less than price.
		double nextShortStrike = 0.0;		
		if (option.getStrike() < option.getAdjusted_stock_close_price()) {	
			// roll down and out or just out
			nextShortStrike = Math.floor(option.getAdjusted_stock_close_price());
		} else { 
			// roll up and out
			nextShortStrike = Math.ceil(option.getAdjusted_stock_close_price());
		}
		
		try {
			// Get next short Call
			option = OptionPricingService.getRecord(option.getTrade_date(), nextExpiration, nextShortStrike, option.getCall_put());
		} catch (NoResultException ex) {
			// try to get next expiration
			currentExpIndex = expirations.indexOf(nextExpiration);
			option = OptionPricingService.getRecord(option.getTrade_date(), expirations.get(currentExpIndex + 1), nextShortStrike, option.getCall_put());
		}
		this.coveredStraddle.setShortCall(option);
		
		TradeDetail openShortCall = TradeService.initializeTradeDetail(option, - TradeProperties.CONTRACTS, "OPENING", "SELL", "SELLING - stock price: " + option.getAdjusted_stock_close_price());
		openShortCall.setTrade(this.coveredStraddle.getTrade());
		em.persist(openShortCall);

	}

	/**
	 * This rolls the call to a initial delta value (for example 0.30)
	 * 
	 * @param option
	 * @param rollComment
	 */
	private void rollToNextPeriodTypeII(OptionPricing option, String rollComment) {
		
		// Getting next expiration to potentially roll to
		int currentExpIndex = expirations.indexOf(option.getExpiration());

		// Roll to next expiration considering DTE
		Date nextExpiration = null;
		do {
			currentExpIndex += 1;
			if (expirations.size() > currentExpIndex) {
				nextExpiration = expirations.get(currentExpIndex);
			} else {
				System.err.println("Unable to roll to next expiration");
				return;
			}
		} while (Utils.calculateDaysBetween(option.getTrade_date(), nextExpiration) <= this.daysTillExpiration);
			
		System.out.println("Rolling " + option);
		
		// Close current option
		TradeDetail closeShortCall = TradeService.initializeTradeDetail(option, TradeProperties.CONTRACTS, "CLOSING", "BUY", rollComment);
		
		closeShortCall.setTrade(this.coveredStraddle.getTrade());		
		em.persist(closeShortCall);

		// ------------------------------------------------------------------------------------------
		// Rolling strategy 
		//  if Delta is > .7 roll up to .7, if delta is < 0.7 and ITM no change, if OTM roll to 0.3 
		// ------------------------------------------------------------------------------------------
		double nextShortStrike = 0.0;	
		// Get next short Call - this option is the one if it's just rolled to the next expiration
		OptionPricing nextOption = OptionPricingService.getRecord(option.getTrade_date(), nextExpiration, option.getStrike(), option.getCall_put());
		
		double targetDelta = 0.0;	// no change
		
		// Are we deep ITM?
		if (nextOption.getDelta() > (1 - initialDelta)) {
			// target delta is ITM
			targetDelta = 1 - initialDelta;
		} else if (nextOption.getStrike() > nextOption.getAdjusted_stock_close_price()) {
			// if OTM roll to 0.3
			targetDelta = initialDelta;
		}
		
		// now to roll strike to new price
		if (targetDelta > 0.0) {
			OptionPricingService ops = new OptionPricingService();
			String callPut = "C";
			nextOption = ops.getOptionByDelta(option.getTrade_date(), nextExpiration, callPut , targetDelta);
		}
		
		this.coveredStraddle.setShortCall(nextOption);
		
		TradeDetail openShortCall = TradeService.initializeTradeDetail(nextOption, - TradeProperties.CONTRACTS, "OPENING", "SELL", "SELLING - stock price: " + option.getAdjusted_stock_close_price());
		openShortCall.setTrade(this.coveredStraddle.getTrade());
		em.persist(openShortCall);

	}

}
