package model.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import trade.IronCondor;
import trade.TradeProperties;
import trade.VerticalSpread;
import misc.Utils;
import model.Spx;
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
		
		TradeDetail shortCall = initializeTradeDetail(callSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
		TradeDetail longCall = initializeTradeDetail(callSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
		TradeDetail shortPut = initializeTradeDetail(putSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
		TradeDetail longPut = initializeTradeDetail(putSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
		
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
	
	private static TradeDetail initializeTradeDetail(Spx spx, int contracts, String posEffect, String side) {
		
		TradeDetail tradeDetail = new TradeDetail();
		
		tradeDetail.setExecTime(spx.getTrade_date());
		tradeDetail.setExp(spx.getExpiration());
		tradeDetail.setPosEffect(posEffect);
		tradeDetail.setPrice(spx.getMean_price());
		tradeDetail.setQty(contracts);
		tradeDetail.setSide(side);
		tradeDetail.setStrike((double) spx.getStrike());
		tradeDetail.setSymbol(spx.getSymbol());
		tradeDetail.setType(spx.getCall_put().equals("C") ? "CALL" : "PUT");
		
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
	
		em.close();
		
		return trades;
	}
	
}
