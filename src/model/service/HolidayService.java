package model.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import model.Holiday;


public class HolidayService {

	private static HolidayService instance = null;

	private static List<Holiday> holidays = null;
	private static Map<Date, String> holidaysMap = null;
	
	private static EntityManagerFactory emf;
	
	/**
	 * Created as a singleton class
	 */
	protected HolidayService() {
		
		emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		getHolidays();
	}

	public static HolidayService getInstance() {
		
		if (instance == null) {
			instance = new HolidayService();
		}
		return instance;
	}
	
	
	public List<Holiday> getHolidays() {
		
		if (holidays == null) {
			
			EntityManager em = emf.createEntityManager();
	
			// Note: table name refers to the Entity Class and is case sensitive
			//       field names are property names in the Entity Class
			Query query = em.createQuery("select hol from Holiday hol order by hol.holiday ");
			
			holidays = query.getResultList();
			holidaysMap = new HashMap<Date, String>();
			
			for (Holiday holiday : holidays) {
				holidaysMap.put(holiday.getHoliday(), holiday.getName());
				System.out.println(holiday.toString());
			}
			em.close();
		}
		
		return holidays;
	}
	
	public Map<Date, String> getHolidaysMap() {
		
		if (holidays == null) {
			getHolidays();
		}
		
		return holidaysMap;
	}
	
	public boolean isHoliday(Date date) {

		if (holidaysMap.containsKey(date)) {
		//if (holidays.contains(date)) {
			return true;
		}
		return false;
	}
}
