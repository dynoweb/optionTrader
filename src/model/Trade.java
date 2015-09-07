package model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the trade database table.
 * 
 */
@Entity
@NamedQuery(name="Trade.findAll", query="SELECT t FROM Trade t")
public class Trade implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	@Column(name="closing_cost")
	private double closingCost;

	@Temporal(TemporalType.DATE)
	@Column(name="exec_time")
	private Date execTime;

	@Temporal(TemporalType.DATE)
	private Date exp;

	@Temporal(TemporalType.DATE)
	private Date exp2;

	@Column(name="opening_cost")
	private double openingCost;

	private double profit;

	@Column(name="trade_type")
	private String tradeType;

	//bi-directional many-to-one association to TradeDetail
	@OneToMany(mappedBy="trade")
	private List<TradeDetail> tradeDetails;

	public Trade() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getClosingCost() {
		return this.closingCost;
	}

	public void setClosingCost(double closingCost) {
		this.closingCost = closingCost;
	}

	public Date getExecTime() {
		return this.execTime;
	}

	public void setExecTime(Date execTime) {
		this.execTime = execTime;
	}

	public Date getExp() {
		return this.exp;
	}

	public void setExp(Date exp) {
		this.exp = exp;
	}

	public Date getExp2() {
		return this.exp2;
	}

	public void setExp2(Date exp2) {
		this.exp2 = exp2;
	}

	public double getOpeningCost() {
		return this.openingCost;
	}

	public void setOpeningCost(double openingCost) {
		this.openingCost = openingCost;
	}

	public double getProfit() {
		return this.profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public String getTradeType() {
		return this.tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public List<TradeDetail> getTradeDetails() {
		return this.tradeDetails;
	}

	public void setTradeDetails(List<TradeDetail> tradeDetails) {
		this.tradeDetails = tradeDetails;
	}

	public TradeDetail addTradeDetail(TradeDetail tradeDetail) {
		getTradeDetails().add(tradeDetail);
		tradeDetail.setTrade(this);

		return tradeDetail;
	}

	public TradeDetail removeTradeDetail(TradeDetail tradeDetail) {
		getTradeDetails().remove(tradeDetail);
		tradeDetail.setTrade(null);

		return tradeDetail;
	}

}