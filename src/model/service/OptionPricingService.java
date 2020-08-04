package model.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import main.TradeProperties;
import misc.Utils;
import model.OptionPricing;


public class OptionPricingService {

	public static Date getFirstTradeDate() {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select min(opt.trade_date) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt");
		
		query.setHint("odb.read-only", "true");

		Date firstTradeDate = (Date) query.getSingleResult();	
		em.close();
		
		return firstTradeDate;
	}

	/**
	 * Returns the last date this symbol was traded on.
	 * 
	 * @return last trade date
	 */
	public static Date getLastTradeDate() {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select max(opt.trade_date) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt");
		
		query.setHint("odb.read-only", "true");

		Date lastTradeDate = (Date) query.getSingleResult();	
		em.close();
		
		return lastTradeDate;
	}
	
	/**
	 * Returns the last date this symbol was traded on for a specified option.
	 * 
	 * @return last trade date
	 */
	public static Date getLastTradeDateForOption(Date expiration) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select max(opt.trade_date) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.expiration=:expiration ");

		query.setParameter("expiration", expiration);
		
		query.setHint("odb.read-only", "true");

		Date lastTradeDate = (Date) query.getSingleResult();	
		em.close();
		
		return lastTradeDate;
	}
	
	/**
	 * Get option pricing records for a range of trade dates for a particular expiration at a strike and callPutType.
	 * 
	 * @param startTradeDate
	 * @param endTradeDate
	 * @param expiration
	 * @param strike
	 * @param callPut
	 * @return
	 */
	public static List<OptionPricing> getPriceHistory(Date startTradeDate, Date endTradeDate, Date expiration, double strike, String callPut) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select opt from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.trade_date >= :startTradeDate " 
				+ "and opt.trade_date <= :endTradeDate " 
				+ "and opt.expiration=:expiration "	
				+ "and opt.call_put = :call_put "
				+ "and opt.strike = :strike "
				+ " order by opt.trade_date");
		
		query.setParameter("startTradeDate", startTradeDate);
		query.setParameter("endTradeDate", endTradeDate);
		query.setParameter("expiration", expiration);
		query.setParameter("call_put", callPut);
		query.setParameter("strike", strike);
		
		query.setHint("odb.read-only", "true");

		@SuppressWarnings("unchecked")
		List<OptionPricing> options = query.getResultList();
		
		em.close();
		
		return options;
	}

	public static double getPriceOnDate(Date tradeDate) throws NoResultException {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		double closePrice = 0;
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select opt.adjusted_stock_close_price from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.trade_date = :tradeDate ");
		query.setParameter("tradeDate", tradeDate);
		
		query.setHint("odb.read-only", "true");

		query.setMaxResults(1);
		
		try {
			closePrice = (double) query.getSingleResult();
		} catch (NoResultException ex) {
			System.err.println("Error getting price on " + tradeDate);
			ex.printStackTrace();
//			throw ex;
		} finally {
			em.close();
		}
		
		return closePrice;
	}

	public static OptionPricing getRecord(Date tradeDate, Date expiration, double strike, String callPut) {
			
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select opt from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.trade_date = :tradeDate " 
				+ "and opt.expiration=:expiration "	
				+ "and opt.call_put = :call_put "
				+ "and opt.strike = :strike ");
		
		query.setParameter("tradeDate", tradeDate);
		query.setParameter("expiration", expiration);
		query.setParameter("call_put", callPut);
		query.setParameter("strike", strike);
		
		query.setHint("odb.read-only", "true");

		OptionPricing optionPriceRecord = null;
		try {
			optionPriceRecord = (OptionPricing) query.getSingleResult();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			System.err.println(" tradeDate: " + Utils.asMMddYY(tradeDate)+ " expiration: " + Utils.asMMddYY(expiration) + " strike: " + strike + " Call/Put: " + callPut);
			ex.printStackTrace();
			throw ex;
		}
		em.close();
		
		return optionPriceRecord;
	}


	public static List<Date> getExpirationsForTradeDate(Date tradeDate) {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(opt.expiration) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.trade_date = :tradeDate ");
		query.setParameter("tradeDate", tradeDate);
		
		query.setHint("odb.read-only", "true");

		@SuppressWarnings("unchecked")
		List<Date> expirations = query.getResultList();
		em.close();
		
		return expirations;
	}

	public OptionPricing getOptionByDelta(Date tradeDate, Date expiration, String callPut, double delta) {
		
		OptionPricing targetOption = null;
		double smallestDiff = 500.0;
		
		List<OptionPricing> optionChain = getOptionChain(tradeDate, expiration, callPut);
		for (OptionPricing option : optionChain) {

			double diffFromTarget = Math.abs(delta - option.getDelta());
			if (diffFromTarget < smallestDiff) {
				targetOption = option;
				smallestDiff = diffFromTarget;
			}
		}		
		return targetOption;
	}

	public OptionPricing getOptionByStrike(Date tradeDate, Date expiration, String callPut, double strike) {
		
		OptionPricing targetOption = null;
		double smallestDiff = 500.0;
		
		List<OptionPricing> optionChain = getOptionChain(tradeDate, expiration, callPut);
		for (OptionPricing option : optionChain) {

			double diffFromTarget = Math.abs(strike - option.getStrike());
			if (diffFromTarget < smallestDiff) {
				targetOption = option;
				smallestDiff = diffFromTarget;
			}
		}		
		if (targetOption == null) {
			System.err.println("Not able to find a matching option");
		}
		return targetOption;
	}
	

	public List<OptionPricing> getOptionChain(Date tradeDate, Date expiration, String callPut) {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select opt from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.trade_date = :tradeDate " 
				+ "and opt.expiration=:expiration "	
				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by opt.strike");
		
		query.setParameter("tradeDate", tradeDate);
		query.setParameter("expiration", expiration);
		query.setParameter("call_put", callPut);

		@SuppressWarnings("unchecked")
		List<OptionPricing> optionChain = query.getResultList();
	
		em.close();
		
		return optionChain;
	}

	/**
	 * Get option chain for a trade start date until expiration at strike and callPutType.
	 * 
	 * @param startingTradeDate
	 * @param expiration
	 * @param strike
	 * @param callPut
	 * @return
	 */
	public List<OptionPricing> getTradeDays(Date startingTradeDate, Date expiration, double strike, String callPut) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select opt from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.trade_date >= :tradeDate " 
				+ "and opt.expiration=:expiration "	
				+ "and opt.call_put = :call_put "
				+ "and opt.strike = :strike "
				+ " order by opt.trade_date");
		
		query.setParameter("tradeDate", startingTradeDate);
		query.setParameter("expiration", expiration);
		query.setParameter("call_put", callPut);
		query.setParameter("strike", strike);
		
		query.setHint("odb.read-only", "true");

		@SuppressWarnings("unchecked")
		List<OptionPricing> options = query.getResultList();
		
		em.close();
		
		return options;
	}

	/**
	 * Gets the option trade-able date range from startDate to expiration
	 *  
	 * Note: not current used or tested 
	 *  
	 * @param startDate start of trade date
	 * @param expiration expiration of option chain
	 * @return
	 * 
	 * @see model.service.ExpirationService.getTradeDatesForExpiration(Date expiration)
	 */
	private static List<Date> getTradeDays(Date startDate, Date expiration) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(opt.trade_date) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.trade_date >= :tradeDate " 
				+ "and opt.expiration=:expiration "	
				+ " order by opt.trade_date");
		
		query.setParameter("tradeDate", startDate);
		query.setParameter("expiration", expiration);

		query.setHint("odb.read-only", "true");

		@SuppressWarnings("unchecked")
		List<Date> tradeDates = query.getResultList();
		
		em.close();
		
		return tradeDates;
	}


	/**
	 * Gets all the dates traded for the current symbol
	 * 
	 * @return List of Date 
	 */
	public static List<Date> getTradeDays() {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(opt.trade_date) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt "
				+ " order by opt.trade_date");
		
		query.setHint("odb.read-only", "true");

		@SuppressWarnings("unchecked")
		List<Date> tradeDates = query.getResultList();
		
		em.close();
		
		return tradeDates;
	}


}
