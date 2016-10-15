package trade;

import misc.Utils;
import model.OptionPricing;

public class CalendarSpread {

	private OptionPricing longOptionClose = null;
	private OptionPricing longOptionOpen = null;
	private OptionPricing shortOptionClose = null;
	private OptionPricing shortOptionOpen = null;
	
	private double openCost = 0;
	private double closeCost = 0;
	
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
	public double getOpenCost() {
		return openCost;
	}
	public void setOpenCost(double openCost) {
		this.openCost = openCost;
	}
	public double getCloseCost() {
		return closeCost;
	}
	public void setCloseCost(double closeCost) {
		this.closeCost = closeCost;
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
			return "no open positions";
		}
	}

}
