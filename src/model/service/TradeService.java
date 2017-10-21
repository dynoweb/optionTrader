package model.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import trade.Butterfly;
import trade.CalendarSpread;
import trade.CoveredStraddle;
import trade.IronCondor;
import trade.VerticalSpread;
import main.TradeProperties;
import misc.Utils;
import model.OptionPricing;
import model.Trade;
import model.TradeDetail;

public class TradeService {

//	public void addTrade(Trade trade) {
//	
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
//		EntityManager em = emf.createEntityManager();
//		
//		em.getTransaction().begin();		
//		em.persist(trade);
//		em.getTransaction().commit();
//	}

	public static List<Trade> getOpenTrades() {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select rec from Trade rec "
				+ "where rec.close_status = :closeStatus "
//				+ "opt.trade_date = :tradeDate " 
//				+ "and opt.expiration=:expiration "	
//				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by rec.exp");
		
		query.setParameter("closeStatus", "OPEN");

		@SuppressWarnings("unchecked")
		List<Trade> trades = query.getResultList();
//		Trade trade = trades.get(0);
//		List<TradeDetail> tradeDetails = trade.getTradeDetails();
		em.close();
		
		return trades;
	}
	
	public static List<Trade> getTrades() {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
	
		// Note: table name refers to the Entity Class and is case sensitive
		//       field names are property names in the Entity Class
		Query query = em.createQuery("select rec from Trade rec "
//				+ "where rec.close_status = :closeStatus "
//				+ "opt.trade_date = :tradeDate " 
//				+ "and opt.expiration=:expiration "	
//				+ "and opt.call_put = :call_put "
//				+ "and opt.delta > 0.04 "
//				+ "and opt.delta < 0.96 "
				+ "order by rec.exp");
		
		//query.setParameter("closeStatus", "OPEN");

		@SuppressWarnings("unchecked")
		List<Trade> trades = query.getResultList();
	
		em.close();
		
		return trades;
	}
	
	
	public static TradeDetail initializeTradeDetail(OptionPricing optionPricing, int contracts, String posEffect, String side) {
		
		return initializeTradeDetail(optionPricing, contracts, posEffect, side, "Current close price," + optionPricing.getAdjusted_stock_close_price() + ",Delta," + optionPricing.getDelta());
	}

	/**
	 * Populates a TradeDetail view object to eventually be saved into the TradeDetail table.
	 *  
	 * @param optionPricing
	 * @param contracts
	 * @param posEffect
	 * @param side
	 * @param comment
	 * @return
	 */
	public static TradeDetail initializeTradeDetail(OptionPricing optionPricing, int contracts, String posEffect, String side, String comment) {
		
		TradeDetail tradeDetail = new TradeDetail();
		
		double price = optionPricing.getMean_price();
		if (side.equals("BUY") && price > 0) {
			price *= -1;
		}
		
		tradeDetail.setExecTime(optionPricing.getTrade_date());
		tradeDetail.setExp(optionPricing.getExpiration());
		tradeDetail.setPosEffect(posEffect);
		tradeDetail.setPrice(Utils.round(price, 2));
		tradeDetail.setQty(contracts);
		tradeDetail.setSide(side);
		tradeDetail.setStrike((double) optionPricing.getStrike());
		tradeDetail.setSymbol(optionPricing.getSymbol());
		tradeDetail.setType(optionPricing.getCall_put().equals("C") ? "CALL" : "PUT");
		tradeDetail.setStockPrice(optionPricing.getAdjusted_stock_close_price());
		tradeDetail.setDelta(optionPricing.getDelta());
		//tradeDetail.setComment(comment);
		
//		Trade trade = new Trade();
//		trade.addTradeDetail(tradeDetail);
//		
//		TradeService tradeService = new TradeService();
//		tradeService.addTrade(trade);		
//
		return tradeDetail;
	}

	public static void openCoveredCall(OptionPricing option) {
		
		Trade trade = new Trade();
		
		trade.setExecTime(option.getTrade_date());
		trade.setExp(option.getExpiration());
		trade.setTradeType("COVERED CALL");		
		double fees = TradeProperties.CONTRACTS * 1 * TradeProperties.COST_PER_CONTRACT_FEE + TradeProperties.COST_PER_STOCK_TRADE_FEE;
		trade.setOpeningCost((Math.round(option.getMean_price()*100) - Math.round(option.getAdjusted_stock_close_price()*100)) * TradeProperties.CONTRACTS + fees);		
		trade.setClose_status("OPEN");
		
		TradeDetail shortCall = initializeTradeDetail(option, -TradeProperties.CONTRACTS, "OPENING", "SELL");
		
		TradeDetail longStock = new TradeDetail();;

		longStock.setExecTime(option.getTrade_date());
		//longStock.setExp(option.getExpiration());
		// buying back an open positions
		longStock.setPrice(-Utils.round(option.getAdjusted_stock_close_price(),2));
		longStock.setPosEffect("OPENING");
		longStock.setQty(TradeProperties.CONTRACTS * 100);
		longStock.setSide("BUY");
		//longStock.setStrike(option.getStrike());
		longStock.setSymbol(TradeProperties.SYMBOL);
		longStock.setType("STOCK");
		
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();		

		em.persist(trade);

		shortCall.setTrade(trade);
		em.persist(shortCall);
		
		longStock.setTrade(trade);
		em.persist(longStock);
		
		em.getTransaction().commit();
		em.close();
		emf.close();
	}

	public static Trade openCoveredStraddle(CoveredStraddle coveredStraddle) {
		
		Trade trade = new Trade();
		
		trade.setExecTime(coveredStraddle.getShortCall().getTrade_date());
		trade.setExp(coveredStraddle.getShortCall().getExpiration());
		trade.setTradeType("COVERED STRADDLE");
		double openingCost = coveredStraddle.getLongCall().getMean_price() + coveredStraddle.getLongPut().getMean_price() 
				- coveredStraddle.getShortCall().getMean_price() - coveredStraddle.getShortPut().getMean_price();
		trade.setOpeningCost(Math.rint(openingCost) * 100 * TradeProperties.CONTRACTS);		
		trade.setClose_status("OPEN");
		
		TradeDetail shortCall = initializeTradeDetail(coveredStraddle.getShortCall(), -TradeProperties.CONTRACTS, "OPENING", "SELL");
		TradeDetail shortPut = initializeTradeDetail(coveredStraddle.getShortPut(), -TradeProperties.CONTRACTS, "OPENING", "SELL");
		TradeDetail longCall = initializeTradeDetail(coveredStraddle.getLongCall(), TradeProperties.CONTRACTS, "OPENING", "BUY");
		TradeDetail longPut = initializeTradeDetail(coveredStraddle.getLongPut(), TradeProperties.CONTRACTS, "OPENING", "BUY");
				
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();		

		em.persist(trade);

		shortCall.setTrade(trade);
		em.persist(shortCall);
		
		shortPut.setTrade(trade);
		em.persist(shortPut);
		
		longCall.setTrade(trade);
		em.persist(longCall);
		
		longPut.setTrade(trade);
		em.persist(longPut);
		
		em.getTransaction().commit();
		em.close();
		emf.close();
		
		return trade;
	}

	public static void openIronCondor(IronCondor ironCondor) { 
			
		Trade trade = new Trade();
		int contracts = TradeProperties.CONTRACTS;
		
		VerticalSpread callSpread = ironCondor.getCallSpread();
		VerticalSpread putSpread = ironCondor.getPutSpread();
		
		TradeDetail shortCall = null;
		TradeDetail longCall = null;
		TradeDetail shortPut = null;
		TradeDetail longPut = null;
    	try {
			shortCall = initializeTradeDetail(callSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
			longCall = initializeTradeDetail(callSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
			shortPut = initializeTradeDetail(putSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
    		longPut = initializeTradeDetail(putSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
    	} catch (Exception ex) {
    		//ex.printStackTrace();
    		//throw ex;
    	}
		
		trade.setExecTime(shortCall.getExecTime());
		trade.setExp(shortCall.getExp());
		trade.setTradeType("IRON CONDOR");
		trade.setClose_status("OPEN");

		double fees = contracts * 4 * TradeProperties.COST_PER_CONTRACT_FEE;
		double openingCost = Utils.round(shortPut.getPrice() * 100 + longPut.getPrice() * 100, 2) +
							 Utils.round(shortCall.getPrice() * 100 + longCall.getPrice() * 100, 2) +
							 fees;
				 
		trade.setOpeningCost(openingCost);

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();		
		em.persist(trade);
		
		shortCall.setTrade(trade);
		em.persist(shortCall);
		
		longCall.setTrade(trade);
		em.persist(longCall);
		
		shortPut.setTrade(trade);
		em.persist(shortPut);
		
		longPut.setTrade(trade);
		em.persist(longPut);
		
		em.getTransaction().commit();
		em.close();
		emf.close();
	}
	
	/**
	 * Records a trade record and a single option position in the tradeDetails table.
	 * 
	 * @param shortOption call or put
	 */
	public static void recordShort(OptionPricing shortOption) {
		
		Trade trade = new Trade();
		//int contracts = TradeProperties.CONTRACTS;
		
		TradeDetail shortTrade = null;
		
		//OptionPricing shortCall = null;
    	try {
    		shortTrade = initializeTradeDetail(shortOption, -TradeProperties.CONTRACTS, "OPENING", "SELL");
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw ex;
    	}
		
		trade.setExecTime(shortTrade.getExecTime());
		trade.setExp(shortTrade.getExp());
		trade.setTradeType("SHORT " + (shortOption.getCall_put().equals("C") ? "CALL" : "PUT"));
		trade.setClose_status("OPEN");
		
		double fees = TradeProperties.CONTRACTS * 1 * TradeProperties.COST_PER_CONTRACT_FEE;
		double openingCost = Utils.round(shortTrade.getPrice() * 100, 2);
		trade.setOpeningCost(openingCost);

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();		
		em.persist(trade);
		
		shortTrade.setTrade(trade);
		em.persist(shortTrade);
		
		em.getTransaction().commit();
		em.close();
		emf.close();

	}

	/**
	 * Used to capture the opening Trade and TradeDetails EO's and saves the data.
	 * 
	 * @param shortCall
	 */
	public static void recordShortCall(OptionPricing shortCall) {
		
		Trade trade = new Trade();
		//int contracts = TradeProperties.CONTRACTS;
		
		TradeDetail shortCallTrade = null;
		
		//OptionPricing shortCall = null;
    	try {
    		shortCallTrade = initializeTradeDetail(shortCall, -TradeProperties.CONTRACTS, "OPENING", "SELL");
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw ex;
    	}
		
		trade.setExecTime(shortCallTrade.getExecTime());
		trade.setExp(shortCallTrade.getExp());
		trade.setTradeType("SHORT CALL");
		trade.setClose_status("OPEN");
		
		double fees = TradeProperties.CONTRACTS * 1 * TradeProperties.COST_PER_CONTRACT_FEE;
		double openingCost = Utils.round(shortCallTrade.getPrice() * 100, 2) + fees;
		trade.setOpeningCost(openingCost);

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();		
		em.persist(trade);
		
		shortCallTrade.setTrade(trade);
		em.persist(shortCallTrade);
		
		em.getTransaction().commit();
		em.close();
		emf.close();

	}

	public static void recordShortOptSpread(VerticalSpread optSpread) {

		Trade trade = new Trade();
		int contracts = TradeProperties.CONTRACTS;
		
		TradeDetail shortOpt = null;
		TradeDetail longOpt = null;
    	try {
			shortOpt = initializeTradeDetail(optSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
    		longOpt = initializeTradeDetail(optSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw ex;
    	}
		
		trade.setExecTime(shortOpt.getExecTime());
		trade.setExp(shortOpt.getExp());
		trade.setTradeType("SHORT " + shortOpt.getType() + " SPREAD");
		trade.setClose_status("OPEN");
		
		double fees = contracts * 2 * TradeProperties.COST_PER_CONTRACT_FEE;
		double openingCost = Utils.round(shortOpt.getPrice() * 100 + 
										  longOpt.getPrice() * 100 + fees, 2);
		trade.setOpeningCost(openingCost);

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();		
		em.persist(trade);
		
		shortOpt.setTrade(trade);
		em.persist(shortOpt);
		
		longOpt.setTrade(trade);
		em.persist(longOpt);
		
		em.getTransaction().commit();
		em.close();
		emf.close();
	}

	public static void recordCalendarSpread(CalendarSpread calendarSpread) {
		
		Trade trade = new Trade();
		int contracts = TradeProperties.CONTRACTS;
		
		TradeDetail shortOpt = null;
		TradeDetail longOpt = null;
    	try {
			shortOpt = initializeTradeDetail(calendarSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
    		longOpt = initializeTradeDetail(calendarSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw ex;
    	}
		
		trade.setExecTime(shortOpt.getExecTime());
		trade.setExp(shortOpt.getExp());
		trade.setTradeType("CALENDAR");
		trade.setClose_status("OPEN");
		
		double fees = contracts * 2 * TradeProperties.COST_PER_CONTRACT_FEE;
		double openingCost = Utils.round(shortOpt.getPrice() * 100 + 
										  longOpt.getPrice() * 100 + fees, 2);
		trade.setOpeningCost(openingCost);

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();		
		em.persist(trade);
		
		shortOpt.setTrade(trade);
		em.persist(shortOpt);
		
		longOpt.setTrade(trade);
		em.persist(longOpt);
		
		em.getTransaction().commit();
		em.close();
		emf.close();
	}

	public static void recordButterfly(Butterfly bf) {

		Trade trade = new Trade();
		int contracts = TradeProperties.CONTRACTS;

		TradeDetail lowerOption = null;
		TradeDetail middleOption = null;
		TradeDetail upperOption = null;
		
    	try {
    		lowerOption = initializeTradeDetail(bf.getLowerOptionOpen(), contracts, "OPENING", "BUY");
    		middleOption = initializeTradeDetail(bf.getMiddleOptionOpen(), -contracts * 2, "OPENING", "SELL");
    		upperOption = initializeTradeDetail(bf.getUpperOptionOpen(), contracts, "OPENING", "BUY");
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw ex;
    	}
		
		trade.setExecTime(lowerOption.getExecTime());
		trade.setExp(lowerOption.getExp());
		trade.setTradeType("BUTTERFLY");
		trade.setClose_status("OPEN");
		
		double fees = contracts * 4 * TradeProperties.COST_PER_CONTRACT_FEE;
		
		// Open long open has a neg price since it's money out of pocket, open short is pos since it's credit
		double openingCost = Utils.round(lowerOption.getPrice() * lowerOption.getQty() * 100 -
				middleOption.getPrice() * middleOption.getQty() * 100 + 
				upperOption.getPrice() * upperOption.getQty() * 100 + fees, 2);
		
		trade.setOpeningCost(openingCost);

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();		
		em.persist(trade);
		
		lowerOption.setTrade(trade);
		em.persist(lowerOption);
		
		middleOption.setTrade(trade);
		em.persist(middleOption);
		
		upperOption.setTrade(trade);
		em.persist(upperOption);
		
		em.getTransaction().commit();
		em.close();
		emf.close();
	}

//	private static void recordShortPutSpread(VerticalSpread putSpread) {
//		
//		Trade trade = new Trade();
//		int contracts = TradeProperties.CONTRACTS;
//		
//		TradeDetail shortPut = null;
//		TradeDetail longPut = null;
//    	try {
//			shortPut = initializeTradeDetail(putSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
//    		longPut = initializeTradeDetail(putSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
//    	} catch (Exception ex) {
//    		ex.printStackTrace();
//    		throw ex;
//    	}
//		
//		trade.setExecTime(shortPut.getExecTime());
//		trade.setExp(shortPut.getExp());
//		trade.setTradeType("SHORT PUT SPREAD");
//		trade.setClose_status("OPEN");
//		
//		double openingCost = Utils.round(shortPut.getPrice() * 100 + 
//										  longPut.getPrice() * 100, 2);
//		trade.setOpeningCost(openingCost);
//
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
//		EntityManager em = emf.createEntityManager();
//		
//		em.getTransaction().begin();		
//		em.persist(trade);
//		
//		shortPut.setTrade(trade);
//		em.persist(shortPut);
//		
//		longPut.setTrade(trade);
//		em.persist(longPut);
//		
//		em.getTransaction().commit();
//		em.close();
//		emf.close();
//	}
}
