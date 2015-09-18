package trade;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import main.TradeProperties;
import misc.Utils;
import model.Trade;
import model.service.TradeService;

public class Report {

	final static Charset ENCODING = StandardCharsets.UTF_8;
	static String outFileName = "C:\\_eProj\\optionsdb\\testResults\\";
	
	public static void showTradeResults() {
		
		double netProfit = 0.0;
		double grossCredit = 0.0;
		int numberOfTrades = 0;
		
		List<String> lines = new ArrayList<String>();

		lines.add("<!DOCTYPE html>");
		lines.add("<HTML><head>");
		lines.add("<link rel=\"stylesheet\" href=\"trade.css\"/>");
		lines.add("</head>");
		
		lines.add("<BODY><CENTER>");
		lines.add("<H1>Iron Condor Backtest</H1>");
		lines.add("<H2>Test Details</H2>");
		lines.add("<H3>"+ TradeProperties.SYMBOL + ", Short Strikes Delta: " + TradeProperties.OPEN_DELTA + ", Spread Width: " + TradeProperties.SPREAD_WIDTH + "</H3>");
		lines.add("<H3>Contracts: "+ TradeProperties.CONTRACTS + ", Days To Expiration: " + TradeProperties.OPEN_DTE + "</H3>");
		lines.add("<H3>Profit Target: " + (TradeProperties.PROFIT_TARGET * 100) + "%, Max Loss: " + (TradeProperties.MAX_LOSS * 100) + "%, Close at: " + TradeProperties.CLOSE_DTE + " DTE</H3>");		
		lines.add("</CENTER><CENTER><TABLE>");
		lines.add("<thead>\n<TR><TH>Open Date</TH><TH>Close Date</TH><TH>Expiration</TH><TH>Close Reason</TH><TH>Open Credit</TH><TH>Close Cost</TH><TH>Profit</TH></TR></thead><tbody>");
		
		List<Trade> trades = TradeService.getTrades();
		for (Trade trade : trades) {

			lines.add("  <TR>"
					+ "<TD>"+Utils.asMMddYY(trade.getExecTime())+"</TD><TD>"+Utils.asMMddYY(trade.getCloseDate())+"</TD>"
					+ "<TD>"+Utils.asMMddYY(trade.getExp())+"</TD><TD>"+trade.getClose_status()+"</TD>"
					+ "<TD>"+(-trade.getOpeningCost())+"</TD><TD>"+trade.getClosingCost()+"</TD><TD>"+trade.getProfit()+"</TD>"
					+ "</TR>");
			
			numberOfTrades++;
			netProfit += trade.getProfit();
			grossCredit += trade.getOpeningCost();
		}
		
		lines.add("<tbody></TABLE>");
		lines.add("<h3>Trades: " + numberOfTrades + "</h3>");
		lines.add("<h3>Avg Credit Per Trade: " + Utils.round(grossCredit/numberOfTrades * -100, 2) + "</h3>");
		lines.add("<h3>Avg Profit Per Trade: " + Utils.round(netProfit/numberOfTrades * 100, 2) + "</h3>");
		lines.add("<h3>Net Profit: " + Utils.round(netProfit * 100, 2) + "</h3></CENTER>");
		lines.add("</BODY></HTML>");
		
		outFileName = outFileName + "IC_" + TradeProperties.SYMBOL + "_D_" + TradeProperties.OPEN_DELTA
				+ "_DTE_" + TradeProperties.OPEN_DTE + "_SW_" + TradeProperties.SPREAD_WIDTH 
				+ "_PT_" + (TradeProperties.PROFIT_TARGET * 100) + "_SL_" + (TradeProperties.MAX_LOSS * 100)
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

}
