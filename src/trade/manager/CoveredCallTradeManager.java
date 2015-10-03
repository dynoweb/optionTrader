package trade.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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

	EntityManager em = null;
	
	public CoveredCallTradeManager(CoveredStraddle coveredStraddle, List<Date> expirations) {
		
		this.coveredStraddle = coveredStraddle;
		this.expirations = expirations;
		
//		// A little house keeping - concurrent exception
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

		// first day of trade (a trade may last several year and consist of server different transaction history)
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
		
		// Already rolled to the next expiration, no action required
		if (!shortCall.getExpiration().equals(expiration) || tradeDate.after(expiration)) {
			return;
		}
		
		// Check the current status of the position
		OptionPricing option = OptionPricingService.getRecord(tradeDate, shortCall.getExpiration(), shortCall.getStrike(), "C");
		
		
		// Roll if delta is less than 0.10 or and ITM and 2 days until expiration (Thursday EOD)
		// calculate roll deadline
		Calendar rollByDate = Calendar.getInstance();
		rollByDate.setTime(shortCall.getExpiration());
		rollByDate.add(Calendar.DATE, -2);
		
		Calendar tradeDateCal = Calendar.getInstance();
		tradeDateCal.setTime(option.getTrade_date());
		
		//roll to the next month
		if (tradeDateCal.equals(rollByDate)) {
			rollToNextPeriod(option, "TIME");
			return;
		}
			
		if (option.getMean_price() < 0.1) {
			rollToNextPeriod(option, "PRICE");
			return;
		}
	}

	private void rollToNextPeriod(OptionPricing option, String rollComment) {
		
		// TODO need to consider rules for rolling up or down or letting it get assigned
		// Getting next expiration to potentially roll to
		int currentExpIndex = expirations.indexOf(option.getExpiration());
		
		Date nextExpiration = expirations.get(currentExpIndex + 1);
		
		System.out.println("Expecting to roll " + option);
		
		// Close current option
		TradeDetail closeShortCall = TradeService.initializeTradeDetail(option, TradeProperties.CONTRACTS, "CLOSING", "BUY", rollComment);
		
		closeShortCall.setTrade(this.coveredStraddle.getTrade());		
		em.persist(closeShortCall);

		double nextShortStrike = Math.min(option.getStrike(), Math.ceil(option.getAdjusted_stock_close_price()));
		
		// Get next short Call
		option = OptionPricingService.getRecord(option.getTrade_date(), nextExpiration, nextShortStrike, option.getCall_put());
		this.coveredStraddle.setShortCall(option);
		
		TradeDetail openShortCall = TradeService.initializeTradeDetail(option, - TradeProperties.CONTRACTS, "OPENING", "SELL", "SELLING - stock price: " + option.getAdjusted_stock_close_price());
		openShortCall.setTrade(this.coveredStraddle.getTrade());
		em.persist(openShortCall);

	}

}
