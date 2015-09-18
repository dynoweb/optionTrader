package trade;

import misc.Utils;
import model.OptionPricing;

/**
 * A straddle has two legs, one put and one call.
 * 
 * @author rcromer
 *
 */
public class VerticalSpread {

	private OptionPricing longOptionClose = null;
	private OptionPricing longOptionOpen = null;
	private OptionPricing shortOptionClose = null;
	private OptionPricing shortOptionOpen = null;
	
	private double openCost = 0;
	private double closeCost = 0;
	
	
	public double getOpenCost() {
		openCost = 0; 
		if (longOptionOpen != null && shortOptionOpen != null) {
			openCost = longOptionOpen.getMean_price() - shortOptionOpen.getMean_price();  
		}
		return openCost;
	}
	public double getCloseCost() {
		return closeCost;
	}
	private String spreadType = "CALL";
	
	public String getSpreadType() {
		return spreadType;
	}
	public void setSpreadType(String spreadType) {
		this.spreadType = spreadType;
	}
	public OptionPricing getLongOptionClose() {
		return longOptionClose;
	}
	public void setLongOptionClose(OptionPricing longOptionClose) {
		this.longOptionClose = longOptionClose;
	}
	public OptionPricing getLongOptionOpen() {
		return longOptionOpen;
	}
	
	public void setLongOptionOpen(OptionPricing longOptionOpen) {
		this.longOptionOpen = longOptionOpen;
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
		if (longOptionOpen != null && shortOptionOpen != null) {
			return Utils.asMMMddYYYY(longOptionOpen.getTrade_date()) + " "
			        + "LONG " + longOptionOpen.getSymbol() + " " + Utils.asMMMddYYYY(longOptionOpen.getExpiration()) + " " 
					+ longOptionOpen.getStrike() + " " + longOptionOpen.getCall_put() + " " + longOptionOpen.getMean_price() + " "
					+ "SHORT " + shortOptionOpen.getSymbol() + " " + Utils.asMMMddYYYY(shortOptionOpen.getExpiration()) + " " 
					+ shortOptionOpen.getStrike() + " " + shortOptionOpen.getCall_put() + " " + shortOptionOpen.getMean_price() + " "
					+ "Net Price:" + Utils.df.format(getOpenCost());
		} else {
			return "no vertical open positions";
		}
	}
}
