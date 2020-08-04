package model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the trade_detail database table.
 * 
 */
@Entity
@Table(name="trade_detail")
@NamedQuery(name="TradeDetail.findAll", query="SELECT t FROM TradeDetail t")
public class TradeDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Temporal(TemporalType.DATE)
	@Column(name="exec_time")
	private Date execTime;

	@Temporal(TemporalType.DATE)
	private Date exp;

	@Column(name="pos_effect")
	private String posEffect;

	private double price;

	@Column(name="stock_price")
	private double stockPrice;

	private double delta;

	private int qty;

	private String side;

	private double strike;

	private String symbol;

	// "CALL" or "PUT"
	private String type;
	
	private String comment;

	//bi-directional many-to-one association to Trade
	@ManyToOne
	@JoinColumn(name="trade_id")
	private Trade trade;

	public TradeDetail() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getPosEffect() {
		return this.posEffect;
	}

	public void setPosEffect(String posEffect) {
		this.posEffect = posEffect;
	}

	public double getPrice() {
		return this.price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getStockPrice() {
		return stockPrice;
	}

	public void setStockPrice(double stockPrice) {
		this.stockPrice = stockPrice;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public int getQty() {
		return this.qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getSide() {
		return this.side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public double getStrike() {
		return this.strike;
	}

	public void setStrike(double strike) {
		this.strike = strike;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * CALL OR PUT.
	 * @return String of either "CALL" or "PUT".
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * 
	 * @return Returns "P" or "C"
	 */
	public String getPutCallType() {
		return this.type.equalsIgnoreCase("PUT") ? "P" : "C";
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Trade getTrade() {
		return this.trade;
	}

	public void setTrade(Trade trade) {
		this.trade = trade;
	}

}