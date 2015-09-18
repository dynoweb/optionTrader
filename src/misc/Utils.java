package misc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Holiday;
import model.service.ExpirationService;
import model.service.HolidayService;
import model.service.OptionsExpirationService;

public class Utils {

	// ProjectProperties.dateFormat.format(longOptionOpen.getExpiration())
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY ");
	public static SimpleDateFormat shortDateFormat = new SimpleDateFormat("MM-dd-YY ");
	public static SimpleDateFormat abriviatedDateFormat = new SimpleDateFormat("MM YY ");
	
	// ProjectProperties.df.format(54.680054);
	public static DecimalFormat df = new DecimalFormat("#.00");

	public static Date addDays(Date date, int days) {
		
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(Calendar.DATE, days); //minus number would decrement the days
	    return cal.getTime();
	}

	public static String asMMMddYYYY(Date date) {
		return dateFormat.format(date);
	}
	
	public static String asMMddYY(Date date) {
		return shortDateFormat.format(date);
	}
	
	public static String asMMYY(Date date) {
		return abriviatedDateFormat.format(date);
	}
	
	/** 
	 * @param year
	 * @param month	[1..12]
	 * @return
	 */
	public static Date getFirstTradingDayOfTheMonth(int year, int month) {
		
		Calendar firstTradingDayOfMonth = Calendar.getInstance();
		firstTradingDayOfMonth.clear();
		
		firstTradingDayOfMonth.set(Calendar.YEAR, year);
		firstTradingDayOfMonth.set(Calendar.MONTH, month-1);
		firstTradingDayOfMonth.set(Calendar.DATE, 1);

//		firstTradingDayOfMonth.set(Calendar.HOUR, 0);
//		firstTradingDayOfMonth.set(Calendar.MINUTE, 0);
//		firstTradingDayOfMonth.set(Calendar.MILLISECOND, 0);
		
		Calendar holidayCal = Calendar.getInstance(); 
		
		HolidayService hs = HolidayService.getInstance();
		List<Holiday> holidays = hs.getHolidays();
		for (Holiday holiday : holidays) {
			
			holidayCal.setTime(holiday.getHoliday());
			if (holidayCal.get(Calendar.DAY_OF_YEAR) == firstTradingDayOfMonth.get(Calendar.DAY_OF_YEAR)) {
				if (firstTradingDayOfMonth.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
					firstTradingDayOfMonth.add(Calendar.DATE, 3);
				} else {
					firstTradingDayOfMonth.add(Calendar.DATE, 1);	// assuming holiday was on a Monday
				}
				break;
			}
		}
		
		return firstTradingDayOfMonth.getTime();
	}

	/**
	 * Determines the third Friday of the month
	 * 
	 * Set calendar to the first day of the month.
	 * Retrieve its day of the week
	 * Calculate the date of the first Friday of the month.
	 * Add 14 days using calendar.add() method. You will get the third Friday
	 * 
	 * @param year yyyy
	 * @param month [1..12]
	 * @return
	 */
	public static Date getMonthlyExpiration(int year, int month) {
	
		// set to the first day of the month
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(year, month-1, 1);	
		
//		cal.set(Calendar.HOUR, 0);
//		cal.set(Calendar.MINUTE, 0);
//		cal.set(Calendar.SECOND, 0);
		
		// determine the third Friday of the month
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int daysUntilFriday = Calendar.FRIDAY - dayOfWeek;
		if (dayOfWeek ==  Calendar.SATURDAY) daysUntilFriday = 6;		// Calendar.SATURDAY == 7
		cal.add(Calendar.DATE, daysUntilFriday + 14); 		// the third Friday of the month
		
		return cal.getTime();
	}

//	Jan 16 2015  
//	Feb 20 2015  
//	Mar 20 2015  
//	Apr 17 2015  
//	May 15 2015  
//	Jun 19 2015  
//	Jul 17 2015  
//	Aug 14 2015  
//	Sep 18 2015  
//	Oct 16 2015  
//	Nov 20 2015  
//	Dec 18 2015


	public static List<Date> getMonthlyExpirations() {
		
		ExpirationService es = new ExpirationService(); 		
		return es.getPotentialMonthlyExpirations();
	}
	
	/**
	 * Determines the trades in order to have the expiration as close to DTE
	 * 
	 * @param dte number of days until expiration
	 * @return
	 * 		Map - key: Date tradeOpenDate, value: Date expirationDate
	 */
	public static Map<Date, Date> getPotentialTrades(int dte) {				
		
		Map<Date, Date> tradeDates = new LinkedHashMap<Date, Date>();
		List<Date> expirations = getMonthlyExpirations();
		
		for (Date expiration : expirations) {
			Date openTradeDate = getTradeDate(expiration, dte);
			tradeDates.put(openTradeDate, expiration);
		}
		return tradeDates;
	}

	/**
	 * Determines the trade date to open a trade in order to have the expiration as close to DTE
	 * @param expiration (Friday expiration)
	 * @param dte number of days until expiration
	 * @return
	 * 		
	 */
	public static Date getTradeDate(Date expiration, int dte) {				
		
		HolidayService hs = HolidayService.getInstance();
		Map<Date, String> holidaysMap = hs.getHolidaysMap();
		
		Calendar tradeDayCal = Calendar.getInstance();
		tradeDayCal.clear();
		tradeDayCal.setTime(expiration);
		tradeDayCal.add(Calendar.DATE, -dte);			
			
		if (tradeDayCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			tradeDayCal.add(Calendar.DATE, -1); 	// Go back to Friday
			if (holidaysMap.containsKey(tradeDayCal.getTime())) {
				tradeDayCal.add(Calendar.DATE, -1); 	// Go back to Thurs
			}
		} else if (tradeDayCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			tradeDayCal.add(Calendar.DATE, 1);		// Go to Monday
			if (holidaysMap.containsKey(tradeDayCal.getTime())) {
				tradeDayCal.add(Calendar.DATE, 1);		// Go to Tues
			}
		}

		System.out.println("getNextTradeDates: " + asMMMddYYYY(tradeDayCal.getTime()) + " " + asMMMddYYYY(expiration));
		
		return tradeDayCal.getTime();
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.DOWN);
	    return bd.doubleValue();
	}
	
	/**
	 * Calculated the number of days between to calendars.  
	 * 
	 * Note: Assumes that startDate is either in the same year as endDate or
	 * the prior year.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * 	number of days
	 */
	public static int calculateDaysBetween(Calendar startDate, Calendar endDate) {

		int days = 0;
		if (startDate.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)) {
			days = endDate.get(Calendar.DAY_OF_YEAR) - startDate.get(Calendar.DAY_OF_YEAR);
		} else {
			Calendar endOfYear = new GregorianCalendar();
			endOfYear.set(Calendar.YEAR, 11, 31);
			days = endOfYear.get(Calendar.DAY_OF_YEAR) - startDate.get(Calendar.DAY_OF_YEAR);
			days += endDate.get(Calendar.DAY_OF_YEAR);
		}
		
		return days;
	}

	public static void main(String[] args) {
		
		Calendar startDate = new GregorianCalendar();
		Calendar endDate = new GregorianCalendar();
		
		// Note: Months are 0 based, days are 1 based.
		startDate.set(2015, 1, 1);		
		endDate.set(2015,2,1);
		
		System.out.println("Days: " + Utils.calculateDaysBetween(startDate, endDate));
	}

	public static int calculateDaysBetween(Date start, Date end) {

		Calendar startDate = new GregorianCalendar();
		Calendar endDate = new GregorianCalendar();
		
		startDate.setTime(start);
		endDate.setTime(end);
		
		return calculateDaysBetween(startDate, endDate);
	}

	public static boolean isHoliday(Calendar calendar) {
		
		HolidayService holidayService = HolidayService.getInstance(); 
		Map<Date, String> holidayMap = holidayService.getHolidaysMap();
		if (holidayMap.containsKey(calendar.getTime())) {
			return true;
		}
		return false;
	}
}
