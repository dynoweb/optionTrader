package model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the results database table.
 * 
 */
@Entity
@Table(name="results")
@NamedQuery(name="Result.findAll", query="SELECT r FROM Result r")
public class Result implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	@Column(name="avg_days_in_trade")
	private double avgDaysInTrade;
	
	@Column(name="close_dte")
	private int closeDte;

	@Column(name = "created", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(name="credit_per_trade")
	private double creditPerTrade;

	private int dte;

	@Column(name="max_dd")
	private double maxDd;

	@Column(name="net_profit")
	private double netProfit;

	@Column(name="profit_per_day")
	private double profitPerDay;

	@Column(name="profit_per_trade")
	private double profitPerTrade;

	@Column(name="profit_target")
	private double profitTarget;

	private double profitable;

	@Column(name="return_per_trade")
	private double returnPerTrade;

	@Column(name="short_delta")
	private double shortDelta;

	@Column(name="long_delta")
	private double longDelta;

	@Column(name="stop_loss")
	private double stopLoss;

	private String symbol;

	@Column(name="trade_type")
	private String tradeType;

	private int trades;

	@Column(name = "updated", insertable = false, updatable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	private double width;

	public Result() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getAvgDaysInTrade() {
		return this.avgDaysInTrade;
	}

	public void setAvgDaysInTrade(double avgDaysInTrade) {
		this.avgDaysInTrade = avgDaysInTrade;
	}

	public int getCloseDte() {
		return closeDte;
	}

	public void setCloseDte(int closeDte) {
		this.closeDte = closeDte;
	}

	public Date getCreated() {
		return this.created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public double getCreditPerTrade() {
		return this.creditPerTrade;
	}

	public void setCreditPerTrade(double creditPerTrade) {
		this.creditPerTrade = creditPerTrade;
	}

	public int getDte() {
		return this.dte;
	}

	public void setDte(int dte) {
		this.dte = dte;
	}

	public double getMaxDd() {
		return this.maxDd;
	}

	public void setMaxDd(double maxDd) {
		this.maxDd = maxDd;
	}

	public double getNetProfit() {
		return this.netProfit;
	}

	public void setNetProfit(double netProfit) {
		this.netProfit = netProfit;
	}

	public double getProfitPerDay() {
		return this.profitPerDay;
	}

	public void setProfitPerDay(double profitPerDay) {
		this.profitPerDay = profitPerDay;
	}

	public double getProfitPerTrade() {
		return this.profitPerTrade;
	}

	public void setProfitPerTrade(double profitPerTrade) {
		this.profitPerTrade = profitPerTrade;
	}

	public double getProfitTarget() {
		return this.profitTarget;
	}

	public void setProfitTarget(double profitTarget) {
		this.profitTarget = profitTarget;
	}

	public double getProfitable() {
		return this.profitable;
	}

	public void setProfitable(double profitable) {
		this.profitable = profitable;
	}

	public double getReturnPerTrade() {
		return this.returnPerTrade;
	}

	public void setReturnPerTrade(double returnPerTrade) {
		this.returnPerTrade = returnPerTrade;
	}

	public double getShortDelta() {
		return this.shortDelta;
	}

	public void setShortDelta(double shortDelta) {
		this.shortDelta = shortDelta;
	}

	public double getLongDelta() {
		return longDelta;
	}

	public void setLongDelta(double longDelta) {
		this.longDelta = longDelta;
	}

	public double getStopLoss() {
		return this.stopLoss;
	}

	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public int getTrades() {
		return this.trades;
	}

	public void setTrades(int trades) {
		this.trades = trades;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public Date getUpdated() {
		return this.updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public double getWidth() {
		return this.width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

}