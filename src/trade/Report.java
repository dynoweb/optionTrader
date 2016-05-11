package trade;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;

import main.TradeProperties;
import misc.Utils;
import model.Trade;
import model.service.TradeService;

/**
 * 
 * 
 * Profit Factor
 * 
 * This statistic returns a ratio that can be used as a performance measure for your strategy. It gives you an idea 
 * of how much more money your strategy earns then it loses. A higher ratio can be considered characteristic of a 
 * high performing strategy. A ratio less than one indicates your strategy loses more money than it earns.
 * 
 * 		Gross Profit / Gross Loss
 * 
 * Sharpe Ratio
 * 
 * This statistic returns a ratio that measures the risk premium per unit of risk of your strategy. It can help you make 
 * decisions based on the excess risk of your strategies. You may have a high-return strategy, but the high returns may 
 * come at a cost of excess risk. The Sharpe ratio will help you determine if it is an appropriate increase in risk for 
 * the higher return or not. Generally, a ratio of 1 or greater is good, 2 or greater is very good, and 3 and up is great.
 * 
 * 		(Profit per Month – risk free Rate of Return) / standard deviation of monthly profits
 * 
 * Note:
 * 
 *  · NinjaTrader presets "risk free Rate of Return" to a value of zero
 *  · In the event that there is only 1 month of trade history or less, there is insufficient data to calculate the 
 *    monthly standard deviation of profits in which event, the Sharpe Ratio is set to a value of 1
 *      
 * 
 * @author rcromer
 *
 */
public class Report {

	final static Charset ENCODING = StandardCharsets.UTF_8;
	static String reportFolder = "C:\\_eProj\\optionsdb\\testResults\\";
	
	private static void buildHeader(List<String> lines) {
		
		lines.add("<!DOCTYPE html>");
		lines.add("<HTML><head>");
		lines.add("<link rel=\"stylesheet\" href=\"trade.css\"/>");
		lines.add("</head>");
		lines.add("<BODY><CENTER>");
		
	}

	public static void buildCoveredCallReport(double delta, int dte) {
		
		List<String> lines = new ArrayList<String>();

		buildHeader(lines);

		double netProfit = 0.0;
		double grossCost = 0.0;
		double maxProfit = 0.0;
		double maxDD = 0.0;
		double drawDown = 0.0;
		int numberOfTrades = 0;

		lines.add("<H3>"+ TradeProperties.SYMBOL + ", Covered Calls: "+ TradeProperties.CONTRACTS + ", Short Strike Delta: " + delta + "</H3>");
		lines.add("<H3>Days To Expiration: " + dte + "</H3>");
		lines.add("<H3>Profit Target: " + (TradeProperties.PROFIT_TARGET * 100) + "%, Max Loss: " + (TradeProperties.MAX_LOSS * 100) + "%, Close at: " + TradeProperties.CLOSE_DTE + " DTE</H3>");		
		lines.add("</CENTER>");

		lines.add("<CENTER><TABLE>");
		lines.add("<thead>\n<TR><TH>Open Date</TH><TH>Close Date</TH><TH>Expiration</TH><TH>Close Reason</TH><TH>Open</TH><TH>Close</TH><TH>Profit</TH><TH>Net Profit</TH><TH>Draw Down</TH></TR></thead><tbody>");

		List<Trade> trades = TradeService.getTrades();
		for (Trade trade : trades) {

			numberOfTrades++;
			netProfit += trade.getProfit();
			grossCost += trade.getOpeningCost();
			maxProfit = Math.max(maxProfit, netProfit);
			drawDown = netProfit-maxProfit;
			maxDD = Math.min(maxDD, drawDown);
			
			lines.add("  <TR>"
					+ "<TD>"+Utils.asMMddYY(trade.getExecTime())+"</TD><TD>"+Utils.asMMddYY(trade.getCloseDate())+"</TD>"
					+ "<TD>"+Utils.asMMddYY(trade.getExp())+"</TD><TD>"+trade.getClose_status()+"</TD>"
					+ "<TD>"+(-trade.getOpeningCost())+"</TD><TD>"+trade.getClosingCost()+"</TD><TD>"+trade.getProfit()+"</TD>"
					+ "<TD>"+Utils.round(netProfit, 2)+"</TD><TD>"+Utils.round(drawDown,2)+"</TD>"
					+ "</TR>");			
		}
		
		lines.add("<tbody></TABLE>");
		lines.add("<h3>Trades: " + numberOfTrades + "</h3>");
		if (numberOfTrades > 0) {
			lines.add("<h3>Avg Profit Per Trade: $" + Utils.round(netProfit/numberOfTrades,2) + "</h3>");
			lines.add("<h3>Avg Return Per Trade: " + Utils.round(100 * netProfit/-grossCost, 2) + "%</h3>");
			lines.add("<h3>        Max Drawdown: $" + Utils.round(maxDD, 2) + "</h3>");
		}
		lines.add("<h3>Net Profit: $" + Utils.round(netProfit,2) + "</h3></CENTER>");
		lines.add("</BODY></HTML>");
		
		String outFileName = reportFolder + "CC_" + TradeProperties.SYMBOL + "_DLT_" + delta
				+ "_DTE_" + dte  
				+ "_PT_" + (TradeProperties.PROFIT_TARGET * 100) + "_SL_" + (TradeProperties.MAX_LOSS * 100)
				+ "_CDTE_" + TradeProperties.CLOSE_DTE + ".html";
		try {
			writeTextFile(outFileName, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void buildIronCondorReport(double delta, double spreadWidth) {
		
		List<String> lines = new ArrayList<String>();

		buildHeader(lines);

		double grossCredit = 0.0;	
		double netProfit = 0.0;
		double grossRisk = 0.0;
		double maxProfit = Double.MIN_VALUE;
		double maxDD = 0.0;
		double drawDown = 0.0;
		int numberOfTrades = 0;
		int profitableTrades = 0;

		lines.add("<H3>"+ TradeProperties.SYMBOL + "</H3>");
		lines.add("<H3> Short Strikes Delta: " + delta + ", Days To Expiration: " + TradeProperties.OPEN_DTE + ", Spread Width: " + spreadWidth + "</H3>");
		lines.add("<H3>Profit Target: " + (TradeProperties.PROFIT_TARGET * 100) + "%, Max Loss: " + (TradeProperties.MAX_LOSS * 100) + "%, Close at: " + TradeProperties.CLOSE_DTE + " DTE</H3>");		
		lines.add("</CENTER>");

		lines.add("<CENTER><TABLE>");
		lines.add("<thead>\n<TR><TH>Open Date</TH><TH>Close Date</TH><TH>Expiration</TH><TH>Close Reason</TH><TH>Open Credit</TH><TH>Close Cost</TH><TH>Profit</TH><TH>Net Profit</TH><TH>Draw Down</TH></TR></thead><tbody>");

		List<Trade> trades = TradeService.getTrades();
		for (Trade trade : trades) {

			numberOfTrades++;			
			netProfit += trade.getProfit();
			maxProfit = Math.max(maxProfit, netProfit);
			drawDown = netProfit-maxProfit;
			maxDD = Math.min(maxDD, drawDown);
			profitableTrades = trade.getProfit() > 0 ? profitableTrades + 1 : profitableTrades;
			
			grossRisk += (spreadWidth * 100 - trade.getOpeningCost());
			grossCredit += trade.getOpeningCost();
			
			lines.add("  <TR>"
					+ "<TD>"+Utils.asMMddYY(trade.getExecTime())+"</TD><TD>"+Utils.asMMddYY(trade.getCloseDate())+"</TD>"
					+ "<TD>"+Utils.asMMddYY(trade.getExp())+"</TD><TD>"+trade.getClose_status()+"</TD>"
					+ "<TD>"+(trade.getOpeningCost())+"</TD><TD>"+trade.getClosingCost()+"</TD><TD>"+trade.getProfit()+"</TD>"
					+ "<TD>"+Utils.round(netProfit, 2)+"</TD><TD>"+Utils.round(drawDown,2)+"</TD>"
					+ "</TR>");
			
		}

		lines.add("<tbody></TABLE>");
		lines.add("<br/><h3>Trades: " + numberOfTrades + "</h3>");
		if (numberOfTrades > 0) {
			lines.add("<h3>Avg Credit Per Trade: " + Utils.round(grossCredit/numberOfTrades, 2) + "</h3>");
			lines.add("<h3>Avg Profit Per Trade: $" + Utils.round(netProfit/numberOfTrades,2) + "</h3>");
			double avgReturnPerTrade = Utils.round(100 * netProfit/grossRisk, 2);
			lines.add("<h3>Avg Return Per Trade: " + avgReturnPerTrade + "%</h3>");
			lines.add("<h3>Profitable Trades: " + Utils.round(100 * profitableTrades/numberOfTrades, 2) + "%</h3>");
			lines.add("<h3>        Max Drawdown: $" + Utils.round(maxDD, 2) + "</h3>");
		}
		lines.add("<h3>Net Profit: $" + Utils.round(netProfit,2) + "</h3></CENTER>");
		lines.add("</BODY></HTML>");
		
		String outFileName = reportFolder + "IC_" + TradeProperties.SYMBOL 
				+ "_D_" + delta
				+ "_DTE_" + TradeProperties.OPEN_DTE 
				+ "_SW_" + spreadWidth 
				+ "_PT_" + (TradeProperties.PROFIT_TARGET * 100) 
				+ "_SL_" + (TradeProperties.MAX_LOSS * 100)
				+ "_CDTE_" + TradeProperties.CLOSE_DTE + ".html";
		try {
			writeTextFile(outFileName, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void writeTextFile(String fileName, List<String> lines) throws IOException {
		
		Path path = Paths.get(fileName);
		try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)){
			for (String line : lines){
				writer.write(line);
				writer.newLine();
			}
			writer.close();
		}
	}

	public static void shortPutSpreadReport(double delta, double spreadWidth, int dte) {
		
		List<String> lines = new ArrayList<String>();

		buildHeader(lines);

		double netProfit = 0.0;
		double grossRisk = 0.0;
		double grossCredit = 0.0;
		double maxProfit = Double.MIN_VALUE;;
		double maxDD = 0.0;
		double drawDown = 0.0;
		int numberOfTrades = 0;
		int profitableTrades = 0;
		int daysInTrade = 0;

		lines.add("<H3>"+ TradeProperties.SYMBOL + "</H3>");
		lines.add("<H3>" + " Short Delta: " + delta + " Days To Expiration: " + dte + " Short Put Spreads, width: " + spreadWidth + "</H3>");
		lines.add("<H3>Profit Target: " + (TradeProperties.PROFIT_TARGET * 100) + "%, Max Loss: " + (TradeProperties.MAX_LOSS * 100) + "%, Close at: " + TradeProperties.CLOSE_DTE + " DTE</H3>");		
		lines.add("</CENTER>");

		lines.add("<CENTER><TABLE>");
		lines.add("<thead>\n<TR><TH>Open Date</TH><TH>Close Date</TH><TH>Expiration</TH><TH>Close Reason</TH><TH>Open</TH><TH>Close</TH><TH>Profit</TH><TH>Net Profit</TH><TH>Draw Down</TH></TR></thead><tbody>");

		List<Trade> trades = TradeService.getTrades();
		for (Trade trade : trades) {

			numberOfTrades++;
			netProfit += trade.getProfit();
			maxProfit = Math.max(maxProfit, netProfit);
			drawDown = netProfit-maxProfit;
			maxDD = Math.min(maxDD, drawDown);
			grossCredit += trade.getOpeningCost();
			profitableTrades = trade.getProfit() > 0 ? profitableTrades + 1 : profitableTrades; 
			grossRisk += (spreadWidth * 100 - trade.getOpeningCost());
			
			DateTime jOpenDate = new DateTime(trade.getExecTime());
			DateTime jCloseDate = new DateTime(trade.getCloseDate());
			Days days = Days.daysBetween(jOpenDate, jCloseDate);
			daysInTrade += days.getDays();
			
			lines.add("  <TR>"
					+ "<TD>"+Utils.asMMddYY(trade.getExecTime())+"</TD><TD>"+Utils.asMMddYY(trade.getCloseDate())+"</TD>"
					+ "<TD>"+Utils.asMMddYY(trade.getExp())+"</TD><TD>"+trade.getClose_status()+"</TD>"
					+ "<TD>"+(trade.getOpeningCost())+"</TD><TD>"+trade.getClosingCost()+"</TD><TD>"+trade.getProfit()+"</TD>"
					+ "<TD>"+Utils.round(netProfit, 2)+"</TD><TD>"+Utils.round(drawDown,2)+"</TD>"
					+ "</TR>");			
		}
		
		lines.add("<tbody></TABLE>");
		lines.add("<br/><h3>Trades: " + numberOfTrades + "</h3>");
		if (numberOfTrades > 0) {
			lines.add("<h3>Avg Profit Per Trade: $" + Utils.round(netProfit/numberOfTrades,2) + "</h3>");
			double avgCreditPerTrade = Utils.round(grossCredit/numberOfTrades, 2);
			lines.add("<h3>Avg Credit Per Trade: $" + avgCreditPerTrade + "</h3>");
			double profitPerDay = Utils.round(netProfit/daysInTrade, 2);
			double avgReturnPerTrade = Utils.round(100 * netProfit/grossRisk, 2);
			lines.add("<h3>Avg Return Per Trade: " + avgReturnPerTrade + "%</h3>");
			lines.add("<h3>Profit Per Day: $" + profitPerDay + "</h3>");
			lines.add("<h3>Average Days In Trade: " + Utils.round(daysInTrade/numberOfTrades, 1) + "</h3>");
			lines.add("<h3>Profitable Trades: " + Utils.round(100 * profitableTrades/numberOfTrades, 2) + "%</h3>");
			lines.add("<h3>        Max Drawdown: $" + Utils.round(maxDD, 2) + "</h3>");
		}

		lines.add("<h3>Net Profit: $" + Utils.round(netProfit,2) + "</h3></CENTER>");
		lines.add("</BODY></HTML>");
		
		String outFileName = reportFolder + "SPS_" + TradeProperties.SYMBOL 
				+ "_D_" + delta 
				+ "_DTE_" + dte 
				+ "_SW" + spreadWidth
				+ "_PT_" + (TradeProperties.PROFIT_TARGET * 100) 
				+ "_SL_" + (TradeProperties.MAX_LOSS * 100)
				+ "_CDTE_" + TradeProperties.CLOSE_DTE + ".html";
		try {
			writeTextFile(outFileName, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void shortOptionReport(double delta, int dte, double profitTarget) {

		List<String> lines = new ArrayList<String>();

		buildHeader(lines);

		double netProfit = 0.0;
		double grossRisk = 0.0;
		double grossCredit = 0.0;
		double maxProfit = Double.MIN_VALUE;;
		double maxDD = 0.0;
		double drawDown = 0.0;
		int numberOfTrades = 0;
		int profitableTrades = 0;
		int daysInTrade = 0;

		String tradeType = TradeProperties.TRADE_TYPE.equals("SHORT_CALL") ? "Short Call" : "Short Put";
		lines.add("<H3>" + tradeType + " - "+ TradeProperties.SYMBOL + "</H3>");
		lines.add("<H3>" + " Short Delta: " + delta + " Days To Expiration: " + dte + "</H3>");
		lines.add("<H3>Profit Target: " + (profitTarget * 100) + "%, Max Loss: " + (TradeProperties.MAX_LOSS * 100) + "%, Close at: " + TradeProperties.CLOSE_DTE + " DTE</H3>");		
		lines.add("</CENTER>");

		lines.add("<CENTER><TABLE>");
		lines.add("<thead>\n<TR><TH>Open Date</TH><TH>Close Date</TH><TH>Expiration</TH><TH>Close Reason</TH><TH>Open</TH><TH>Close</TH><TH>Profit</TH><TH>Net Profit</TH><TH>Draw Down</TH></TR></thead><tbody>");

		List<Trade> trades = TradeService.getTrades();
		for (Trade trade : trades) {

			//System.out.println("Id: " + trade.getId());
			numberOfTrades++;
			netProfit += trade.getProfit();
			maxProfit = Math.max(maxProfit, netProfit);
			drawDown = netProfit-maxProfit;
			maxDD = Math.min(maxDD, drawDown);
			grossCredit += trade.getOpeningCost();
			profitableTrades = trade.getProfit() > 0 ? profitableTrades + 1 : profitableTrades; 
			
			DateTime jOpenDate = new DateTime(trade.getExecTime());
			DateTime jCloseDate = new DateTime(trade.getCloseDate());
			Days days = Days.daysBetween(jOpenDate, jCloseDate);
			daysInTrade += days.getDays();
			
			//grossRisk += (spreadWidth * 100 - trade.getOpeningCost());
			
			lines.add("  <TR>"
					+ "<TD>"+Utils.asMMddYY(trade.getExecTime())+"</TD><TD>"+Utils.asMMddYY(trade.getCloseDate())+"</TD>"
					+ "<TD>"+Utils.asMMddYY(trade.getExp())+"</TD><TD>"+trade.getClose_status()+"</TD>"
					+ "<TD>"+(trade.getOpeningCost())+"</TD><TD>"+trade.getClosingCost()+"</TD><TD>"+trade.getProfit()+"</TD>"
					+ "<TD>"+Utils.round(netProfit, 2)+"</TD><TD>"+Utils.round(drawDown,2)+"</TD>"
					+ "</TR>");			
		}
		
		lines.add("<tbody></TABLE>");
		lines.add("<br/><h3>Trades: " + numberOfTrades + "</h3>");
		if (numberOfTrades > 0) {
			lines.add("<h3>Avg Profit Per Trade: $" + Utils.round(netProfit/numberOfTrades,2) + "</h3>");
			double avgCreditPerTrade = Utils.round(grossCredit/numberOfTrades, 2);
			lines.add("<h3>Avg Credit Per Trade: $" + avgCreditPerTrade + "</h3>");
			double profitPerDay = Utils.round(netProfit/daysInTrade, 2);
			lines.add("<h3>Profit Per Day: $" + profitPerDay + "</h3>");
			lines.add("<h3>Average Days In Trade: " + Utils.round(daysInTrade/numberOfTrades, 1) + "</h3>");
			lines.add("<h3>Profitable Trades: " + Utils.round(100 * profitableTrades/numberOfTrades, 2) + "%</h3>");
			lines.add("<h3>        Max Drawdown: $" + Utils.round(maxDD, 2) + "</h3>");
		}
		lines.add("<h3>Net Profit: $" + Utils.round(netProfit,2) + "</h3></CENTER>");
		lines.add("</BODY></HTML>");
		
		String fileNamePrefix = TradeProperties.TRADE_TYPE.equals("SHORT_CALL") ? "SC_" : "SP_";
		String outFileName = reportFolder + fileNamePrefix + TradeProperties.SYMBOL 
				+ "_D_" + delta 
				+ "_DTE_" + dte 
				//+ "_SW" + spreadWidth
				+ "_PT_" + (profitTarget * 100) 
				+ "_SL_" + (TradeProperties.MAX_LOSS * 100)
				+ "_CDTE_" + TradeProperties.CLOSE_DTE + ".html";
		try {
			writeTextFile(outFileName, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void buildCoveredStraddleReport(int dte) {
		// TODO Auto-generated method stub
		
	}

}
