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
	 * @param symbol required to be in mixed case with the first char upper case for example SPX
	 * @return
	 */
	public List<Date> getPotentialExpirations() {		
		
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
	 * Returns a list of all the expiration dates for a particular set of optionPricing 
	 * @param symbol required to be in mixed case with the first char upper case for example SPX
	 * @return
	 */
	public List<Date> getPotentialMonthlyExpirations() {
		
		List<Date> monthlyExpirations = new ArrayList<Date>(); 
		
		Calendar cal = Calendar.getInstance();
		
		for (Date date : getPotentialExpirations()) {
			cal.setTime(date);
			if (cal.get(Calendar.DAY_OF_MONTH) > 14 && cal.get(Calendar.DAY_OF_MONTH) < 22) {
				monthlyExpirations.add(date);
			}
		}
		return monthlyExpirations;
	}
	
	public static void main(String[] arg) {
		
		ExpirationService es = new ExpirationService();
		List<Date> dates = es.getPotentialMonthlyExpirations();
		for (Date date : dates) {
			
			System.out.println(Utils.asMMddYY(date));
		}
	}
}
