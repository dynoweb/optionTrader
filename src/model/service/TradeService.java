package model.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

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
    		ex.printStackTrace();
    		throw ex;
    	}
		
		trade.setExecTime(shortCall.getExecTime());
		trade.setExp(shortCall.getExp());
		trade.setTradeType("IRON CONDOR");
		trade.setClose_status("OPEN");
		
		double openingCost = Utils.round(shortCall.getPrice() * shortCall.getQty() + longCall.getPrice() * longCall.getQty() +
				 shortPut.getPrice() * shortPut.getQty() + longPut.getPrice() * longPut.getQty(), 2);
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
	
	private static TradeDetail initializeTradeDetail(OptionPricing optionPricing, int contracts, String posEffect, String side) {
		
		TradeDetail tradeDetail = new TradeDetail();
		
		tradeDetail.setExecTime(optionPricing.getTrade_date());
		tradeDetail.setExp(optionPricing.getExpiration());
		tradeDetail.setPosEffect(posEffect);
		tradeDetail.setPrice(optionPricing.getMean_price());
		tradeDetail.setQty(contracts);
		tradeDetail.setSide(side);
		tradeDetail.setStrike((double) optionPricing.getStrike());
		tradeDetail.setSymbol(optionPricing.getSymbol());
		tradeDetail.setType(optionPricing.getCall_put().equals("C") ? "CALL" : "PUT");
		
//		Trade trade = new Trade();
//		trade.addTradeDetail(tradeDetail);
//		
//		TradeService tradeService = new TradeService();
//		tradeService.addTrade(trade);		
//
		return tradeDetail;
	}

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

		List<Trade> trades = query.getResultList();
	
		em.close();
		
		return trades;
	}

	public static void openCoveredCall(OptionPricing option) {
		
		Trade trade = new Trade();
		
		trade.setExecTime(option.getTrade_date());
		trade.setExp(option.getExpiration());
		trade.setTradeType("COVERED CALL");		
		trade.setOpeningCost((Math.round(option.getMean_price()*100) - Math.round(option.getAdjusted_stock_close_price()*100)) * TradeProperties.CONTRACTS);		
		trade.setClose_status("OPEN");
		
		TradeDetail shortCall = initializeTradeDetail(option, -TradeProperties.CONTRACTS, "OPENING", "SELL");
		
		TradeDetail longStock = new TradeDetail();;

		longStock.setExecTime(option.getTrade_date());
		//longStock.setExp(option.getExpiration());
		longStock.setPrice(Utils.round(option.getAdjusted_stock_close_price(),2));
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
	
}
