package trade;

import misc.Utils;
import model.Spx;

/**
 * A straddle has two legs, one put and one call.
 * 
 * @author rcromer
 *
 */
public class VerticalSpread {

	private Spx longOptionClose = null;
	private Spx longOptionOpen = null;
	private Spx shortOptionClose = null;
	private Spx shortOptionOpen = null;
	
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
	public Spx getLongOptionClose() {
		return longOptionClose;
	}
	public void setLongOptionClose(Spx longOptionClose) {
		this.longOptionClose = longOptionClose;
	}
	public Spx getLongOptionOpen() {
		return longOptionOpen;
	}
	
	public void setLongOptionOpen(Spx longOptionOpen) {
		this.longOptionOpen = longOptionOpen;
	}
	
	public Spx getShortOptionClose() {
		return shortOptionClose;
	}
	public void setShortOptionClose(Spx shortOptionClose) {
		this.shortOptionClose = shortOptionClose;
	}
	public Spx getShortOptionOpen() {
		return shortOptionOpen;
	}
	
	public void setShortOptionOpen(Spx shortOptionOpen) {
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
