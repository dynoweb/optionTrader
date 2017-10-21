package trade;

import misc.Utils;
import model.OptionPricing;

/**
 * An order contains a single butterfly
 * An order could be an opening order or a closing order.  
 * 
 * @author rcromer
 *
 */
public class Butterfly {
	
	private OptionPricing lowerOptionClose = null;
	private OptionPricing lowerOptionOpen = null;
	private OptionPricing middleOptionClose = null;
	private OptionPricing middleOptionOpen = null;
	private OptionPricing upperOptionClose = null;
	private OptionPricing upperOptionOpen = null;
	
	private double openCost = 0;
	private double closeCost = 0;
	
	
	public OptionPricing getLowerOptionClose() {
		return lowerOptionClose;
	}


	public void setLowerOptionClose(OptionPricing lowerOptionClose) {
		this.lowerOptionClose = lowerOptionClose;
	}


	public OptionPricing getLowerOptionOpen() {
		return lowerOptionOpen;
	}


	public void setLowerOptionOpen(OptionPricing lowerOptionOpen) {
		this.lowerOptionOpen = lowerOptionOpen;
	}


	public OptionPricing getMiddleOptionClose() {
		return middleOptionClose;
	}


	public void setMiddleOptionClose(OptionPricing middleOptionClose) {
		this.middleOptionClose = middleOptionClose;
	}


	public OptionPricing getMiddleOptionOpen() {
		return middleOptionOpen;
	}


	public void setMiddleOptionOpen(OptionPricing middleOptionOpen) {
		this.middleOptionOpen = middleOptionOpen;
	}


	public OptionPricing getUpperOptionClose() {
		return upperOptionClose;
	}


	public void setUpperOptionClose(OptionPricing upperOptionClose) {
		this.upperOptionClose = upperOptionClose;
	}


	public OptionPricing getUpperOptionOpen() {
		return upperOptionOpen;
	}


	public void setUpperOptionOpen(OptionPricing upperOptionOpen) {
		this.upperOptionOpen = upperOptionOpen;
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
		if (lowerOptionOpen != null && middleOptionOpen != null && upperOptionOpen != null) {
			return Utils.asMMMddYYYY(middleOptionOpen.getTrade_date()) + " "
			        + middleOptionOpen.getSymbol() + " " + Utils.asMMMddYYYY(middleOptionOpen.getExpiration()) + " " 
					+ lowerOptionOpen.getStrike() + " " + middleOptionOpen.getStrike() + " " + upperOptionOpen.getStrike() + " " 
					+ "Open Price:" + Utils.df.format(getOpenCost());
		} else {
			return "no vertical open positions";
		}		
	}

}
