package model.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import model.Result;

public class ResultService {

	
	public static Result getRecord(int dte, double shortDelta, double width, double profitTarget, double stopLoss) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select res from Result res where "
				+ "res.dte = :dte " 
				+ "and res.shortDelta = :shortDelta "	
				+ "and res.width = :width "
				+ "and res.profitTarget = :profitTarget "
				+ "and res.stopLoss = :stopLoss "
				);
		
		query.setParameter("dte", dte);
		query.setParameter("shortDelta", shortDelta);
		query.setParameter("width", width);
		query.setParameter("profitTarget", profitTarget);
		query.setParameter("stopLoss", stopLoss);
		
		//query.setHint("odb.read-only", "true");

		Result resultRecord = null;
		try {
			resultRecord = (Result) query.getSingleResult();
		} catch (NoResultException ignore) {
		} catch  (Exception ex) {
			System.err.println(ex.getMessage());
			System.err.println(" dte: " + dte + " shortDelta: " + shortDelta + " width: " + width + " profitTarget: " + profitTarget + " stopLoss: " + stopLoss);
			ex.printStackTrace();
			throw ex;
		}
		em.close();
		
		return resultRecord;
	}

	public static void main(String[] args) {
		
		EntityManagerFactory emf = null;
		EntityManager em = null; 
		
		emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		em = emf.createEntityManager();
		
		Result result = ResultService.getRecord(7, 0.1, 50.0, 0.0, 0.0);
		
		if (result != null) {
			// test edit
			System.out.println("Width: " + result.getWidth());
			
			em.getTransaction().begin();
			result.setMaxDd(400.25);
			em.merge(result);
			em.getTransaction().commit();
		} else {
			// test create
			em.getTransaction().begin();

			// id = 0 after new
			result = new Result();
			
			result.setDte(7);
			result.setShortDelta(0.1);
			result.setWidth(50.0);
			
			em.persist(result);
			em.getTransaction().commit();
		}
		
		em.close();
		emf.close();

	}

}
