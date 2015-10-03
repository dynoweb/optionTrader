package trade;

import model.OptionPricing;
import model.Trade;

/**
 * A covered straddle is a poor mans covered call and a poor mans covered put. Sells near options
 * while covering those with long leaps.
 * 
 * @author rcromer
 *
 */
public class CoveredStraddle {

	private OptionPricing longCall = null;
	private OptionPricing longPut = null;
	private OptionPricing shortCall = null;
	private OptionPricing shortPut = null;
	
	Trade trade = null;
	
	public Trade getTrade() {
		return trade;
	}
	public void setTrade(Trade trade) {
		this.trade = trade;
	}
	public OptionPricing getLongCall() {
		return longCall;
	}
	public void setLongCall(OptionPricing longCall) {
		this.longCall = longCall;
	}
	public OptionPricing getLongPut() {
		return longPut;
	}
	public void setLongPut(OptionPricing longPut) {
		this.longPut = longPut;
	}
	public OptionPricing getShortCall() {
		return shortCall;
	}
	public void setShortCall(OptionPricing shortCall) {
		this.shortCall = shortCall;
	}
	public OptionPricing getShortPut() {
		return shortPut;
	}
	public void setShortPut(OptionPricing shortPut) {
		this.shortPut = shortPut;
	}

	
}
