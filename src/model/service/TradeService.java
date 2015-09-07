package model.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import trade.IronCondor;
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

	public static void openIronCondor(IronCondor ironCondor, int contracts) {
		
		Trade trade = new Trade();
		TradeDetail shortCall = new TradeDetail(); 
		
		VerticalSpread callSpread = ironCondor.getCallSpread();
		VerticalSpread putSpread = ironCondor.getPutSpread();
		
//		TradeDetail shortCall = initializeTradeDetail(callSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
//		TradeDetail longCall = initializeTradeDetail(callSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
//		TradeDetail shortPut = initializeTradeDetail(putSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
//		TradeDetail longPut = initializeTradeDetail(putSpread.getLongOptionOpen(), contracts, "OPENING", "BUY");
		
		trade.setExecTime(new Date()); //shortCall.getExecTime());
		trade.setExp(new Date()); // shortCall.getExp());
		trade.setTradeType("IRON CONDOR");
		
		double openingCost = 0.00;
//		         Utils.round(shortCall.getPrice() * shortCall.getQty() + longCall.getPrice() * longCall.getQty() +
//				 shortPut.getPrice() * shortPut.getQty() + longPut.getPrice() * longPut.getQty(), 2);
		trade.setOpeningCost(openingCost);

//		List<TradeDetail> tradeDetails = new ArrayList<TradeDetail>();
		
		
		//trade.setTradeDetails(tradeDetails);
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAOptionsTrader");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();		
		em.persist(trade);		
		em.getTransaction().commit();

//		em.refresh(trade);
//		
		em.getTransaction().begin();
//		shortCall = initializeTradeDetail(callSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL");
		initializeTradeDetail(callSpread.getShortOptionOpen(), -contracts, "OPENING", "SELL", shortCall);
		shortCall.setTrade(trade);
		em.persist(shortCall);
		em.getTransaction().commit();
		
//		trade.setTradeDetails(tradeDetails);
		
//		trade.addTradeDetail(shortCall);
//		trade.addTradeDetail(longCall);
//		trade.addTradeDetail(shortPut);
//		trade.addTradeDetail(longPut);
		
//		trade.setTradeDetails(tradeDetails);
		
//		em.persist(trade);
		
	}
	
	private static void initializeTradeDetail(Spx spx, int contracts, String posEffect, String side, TradeDetail tradeDetail) {
		
//		TradeDetail tradeDetail = new TradeDetail();
		
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
//		return tradeDetail;
	}
	
}
