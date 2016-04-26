package trade;

import misc.Utils;
import model.OptionPricing;

public class ShortCallTrade {
	
	
	private OptionPricing shortOptionClose = null;
	private OptionPricing shortOptionOpen = null;
	
	private double openCost = 0;
	private double closeCost = 0;	
	
	public double getOpenCost() {
		openCost = 0; 
		if (shortOptionOpen != null) {
			openCost = shortOptionOpen.getMean_price();  
		}
		return openCost;
	}
	public double getCloseCost() {
		return closeCost;
	}
	
	public OptionPricing getShortOptionClose() {
		return shortOptionClose;
	}
	public void setShortOptionClose(OptionPricing shortOptionClose) {
		this.shortOptionClose = shortOptionClose;
	}
	public OptionPricing getShortOptionOpen() {
		return shortOptionOpen;
	}
	
	public void setShortOptionOpen(OptionPricing shortOptionOpen) {
		this.shortOptionOpen = shortOptionOpen;
	}
	
	public String toString() {
		if (shortOptionOpen != null) {
			return  "SHORT " + shortOptionOpen.getSymbol() + " " + Utils.asMMMddYYYY(shortOptionOpen.getExpiration()) + " " 
					+ shortOptionOpen.getStrike() + " " + shortOptionOpen.getCall_put() + " " + shortOptionOpen.getMean_price() + " "
					+ "Net Price:" + Utils.df.format(getOpenCost());
		} else {
			return "no open positions";
		}
	}


}
