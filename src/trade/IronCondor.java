package trade;

import misc.ProjectProperties;

/**
 * An order contains 1 or 2 VerticalSpreads
 * An order could be an opening order or a closing order.  
 * Rolling a position will be two orders, a closing order and an opening order
 * 
 * @author rcromer
 *
 */
public class IronCondor {

	private VerticalSpread callSpread = null;
	private VerticalSpread putSpread = null;
	
	public VerticalSpread getCallSpread() {
		return callSpread;
	}
	public void setCallSpread(VerticalSpread callSpread) {
		this.callSpread = callSpread;
	}
	public VerticalSpread getPutSpread() {
		return putSpread;
	}
	public void setPutSpread(VerticalSpread putSpread) {
		this.putSpread = putSpread;
	}
	
	public String toString() {
		return callSpread + ", " + putSpread + " Net Credit: " + ProjectProperties.df.format((callSpread.getOpenCost() + putSpread.getOpenCost()) * -1);
	}
}
