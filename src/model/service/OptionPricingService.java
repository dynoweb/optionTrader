package model.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import main.TradeProperties;
import model.OptionPricing;
import model.Spx;


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
					+ "and opt.strike = :strike "
					+ "order by opt.delta");
			
			query.setParameter("tradeDate", tradeDate);
			query.setParameter("expiration", expiration);
			query.setParameter("call_put", callPut);
			query.setParameter("strike", strike);

			OptionPricing optionPriceRecord = (OptionPricing) query.getSingleResult();	
			em.close();
			
			return optionPriceRecord;
		}

}
