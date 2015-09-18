package model.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import model.OptionsExpiration;


public class OptionsExpirationService {
	
	
	public List<OptionsExpiration> getExpirations(String symbol) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();

		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select distinct(oe) from OptionsExpiration oe where oe.symbol=:symbol order by oe.expiration");
		
		query.setParameter("symbol", symbol);
		
		List<OptionsExpiration> expirations = query.getResultList();

		for (OptionsExpiration expiration : expirations) {
			System.out.println(expiration.toString());
		}
		em.close();
		
		return expirations;
	}

}
