package model.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import main.TradeProperties;
import misc.Utils;
import model.OptionPricing;


public class OptionPricingService {

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
				+ "order by opt.delta");
		
		query.setParameter("tradeDate", tradeDate);
		query.setParameter("expiration", expiration);
		query.setParameter("call_put", callPut);

		List<OptionPricing> optionChain = query.getResultList();
	
		em.close();
		
		return optionChain;
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
			throw ex;
		}
		em.close();
		
		return optionPriceRecord;
	}
	
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

	public List<Date> getExpirationsForTradeDate(Date tradeDate) {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(opt.expiration) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt where "
				+ "opt.trade_date = :tradeDate ");
		query.setParameter("tradeDate", tradeDate);
		
		query.setHint("odb.read-only", "true");

		List<Date> expirations = query.getResultList();
		em.close();
		
		return expirations;
	}

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

		List<OptionPricing> options = query.getResultList();
		
		em.close();
		
		return options;
	}


}
