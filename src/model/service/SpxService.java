package model.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import model.Spx;

public class SpxService {

	public List<Date> getExpirationDates(Date tradeDate) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(opt.expiration) from Spx opt where opt.trade_date = :tradeDate order by opt.expiration");
		query.setParameter("tradeDate", tradeDate);

		List<Date> expirations = query.getResultList();
	
		em.close();
		
		return expirations;
	}

	public List<Spx> getOptionChain(Date tradeDate, Date expiration, String callPut) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select opt from Spx opt where "
				+ "opt.trade_date = :tradeDate " 
				+ "and opt.expiration=:expiration "	
				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by opt.delta");
		
		query.setParameter("tradeDate", tradeDate);
		query.setParameter("expiration", expiration);
		query.setParameter("call_put", callPut);

		List<Spx> spxOptionChain = query.getResultList();
	
		em.close();
		
		return spxOptionChain;
	}

}
