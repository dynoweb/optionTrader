package model.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import main.TradeProperties;
import misc.Utils;


public class ExpirationService {

	/**
	 * Returns a list of all the expiration dates for a particular set of optionPricing 
	 * @param symbol required to be in mixed case with the first char upper case for example Spx
	 * @return
	 */
	public List<Date> getExpirations() {		
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(opt.expiration) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt "
//				+ "where "
//				+ "opt.trade_date = :tradeDate " 
//				+ "and opt.expiration=:expiration "	
//				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by opt.expiration");
		
//		query.setParameter("tradeDate", tradeDate);
//		query.setParameter("expiration", expiration);
//		query.setParameter("call_put", callPut);

		List<Date> dates = query.getResultList();
		em.close();
		return dates;		
	}
	
	
	/**
	 * Trying to find the latest trade date for a given expiration
	 * 
	 * @param expiration
	 * @return
	 */
	public List<Date> getTradeDatesForExpiration(Date expiration) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(opt.trade_date) from " + TradeProperties.SYMBOL_FOR_QUERY + " opt "
				+ "where "
//				+ "opt.trade_date = :tradeDate " 
				+ "opt.expiration=:expiration "	
//				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by opt.trade_date");
		
//		query.setParameter("tradeDate", tradeDate);
		query.setParameter("expiration", expiration);
//		query.setParameter("call_put", callPut);

		List<Date> dates = query.getResultList();
		em.close();
		return dates;		
	}
	
	/**
	 * Returns a list of all the expiration dates for a particular set of optionPricing 
	 * @param symbol required to be in mixed case with the first char upper case for example SPX
	 * @return
	 */
	public List<Date> getPotentialMonthlyExpirations() {
		
		List<Date> monthlyExpirations = new ArrayList<Date>(); 
		
		Calendar cal = Calendar.getInstance();
		
		for (Date date : getExpirations()) {
			cal.setTime(date);
			if (cal.get(Calendar.DAY_OF_MONTH) > 14 && cal.get(Calendar.DAY_OF_MONTH) < 23) {
				if (cal.get(Calendar.DAY_OF_MONTH) == 22 && cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
					// The 22nd must be a Sat for Friday to be the third Friday of the month
				} else {
					monthlyExpirations.add(date);
				}
			}
		}
		return monthlyExpirations;
	}
	
	public static void main(String[] arg) {
		
		ExpirationService es = new ExpirationService();
//		List<Date> dates = es.getPotentialMonthlyExpirations();
//		for (Date date : dates) {
//			
//			System.out.println(Utils.asMMddYY(date));
//		}
		Calendar cal = Calendar.getInstance();
		cal.set(2008, 1, 16);
		
		List<Date> dates = es.getTradeDatesForExpiration(cal.getTime());
		if (dates.size() == 0) {
			System.out.println("No dates returned");
		}
		for (Date date : dates) {
			
			System.out.println(Utils.asMMddYY(date));
		}
	}

}
