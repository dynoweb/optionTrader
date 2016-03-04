package model.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import main.TradeProperties;


public class MarketOpenService {

	/**
	 * Returns a list of all the dates the instrument is traded  
	 * @param symbol required to be in mixed case with the first char upper case for example Spy
	 * @return
	 */
	public List<Date> getTradeDates() {		
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(opt.trade_date) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt "
//				+ "where "
//				+ "opt.trade_date = :tradeDate " 
//				+ "and opt.expiration=:expiration "	
//				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by opt.trade_date");
		
//		query.setParameter("tradeDate", tradeDate);
//		query.setParameter("expiration", expiration);
//		query.setParameter("call_put", callPut);

		List<Date> dates = query.getResultList();
		em.close();
		return dates;		
	}
	
	

}
