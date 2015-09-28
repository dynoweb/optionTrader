package model.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import model.Trade;
import model.TradeDetail;

public class TradeDetailService {

	public static List<TradeDetail> getTradeDetails(Date expiration) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select rec from TradeDetail rec where "
//				+ "rec.closingCost is null "
//				+ "and rec.trade_date = :tradeDate " 
				+ "rec.exp=:expiration "	
//				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by rec.id");
		
		//query.setParameter("tradeDate", tradeDate);
		query.setParameter("expiration", expiration);
		
		List<TradeDetail> tradeDetails = query.getResultList();
	
		em.close();
		
		return tradeDetails;
	}

	public static List<TradeDetail> getTradeDetails(Trade trade) {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select rec from TradeDetail rec where "
				+ "rec.trade=:trade "	
//				+ "rec.closingCost is null "
//				+ "and rec.trade_date = :tradeDate " 
//				+ "rec.exp=:expiration "	
//				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by rec.id");
		
		//query.setParameter("tradeDate", tradeDate);
		query.setParameter("trade", trade);
		
		List<TradeDetail> tradeDetails = query.getResultList();
	
		em.close();
		
		return tradeDetails;
	}

}

