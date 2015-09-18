package model;

import java.io.Serializable;

import javax.persistence.*;

import misc.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * The persistent class for the spx database table.
 * 
 */
@Entity
@NamedQuery(name="Spx.findAll", query="SELECT s FROM Spx s")
public class Spx implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	@Lob
	@Column(name="`*`")
	private String _asterisk;

	@Column(name="`adjusted stock close price`")
	private double adjusted_stock_close_price;

	private double ask;

	private double bid;

	@Lob
	@Column(name="`call/put`")
	private String call_put;

	private double delta;

	@Lob
	private String exchange;

	@Temporal(TemporalType.DATE)
	private Date expiration;

	private double gamma;

	private double iv;

	@Column(name="`mean price`")
	private double mean_price;

	@Column(name="`open interest`")
	private int open_interest;

	@Lob
	@Column(name="`option symbol`")
	private String option_symbol;

	private double rho;

	@Column(name="`stock price for iv`")
	private double stock_price_for_iv;

	private int strike;

	@Lob
	private String style;

	@Lob
	private String symbol;

	private double theta;

	@Temporal(TemporalType.DATE)
	@Column(name="`trade date`")
	private Date trade_date;

	private double vega;

	private int volume;

	public Spx() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String get_asterisk() {
		return this._asterisk;
	}

	public void set_(String _asterisk) {
		this._asterisk = _asterisk;
	}

	public double getAdjusted_stock_close_price() {
		return this.adjusted_stock_close_price;
	}

	public void setAdjusted_stock_close_price(double adjusted_stock_close_price) {
		this.adjusted_stock_close_price = adjusted_stock_close_price;
	}

	public double getAsk() {
		return this.ask;
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public double getBid() {
		return this.bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public String getCall_put() {
		return this.call_put;
	}

	public void setCall_put(String call_put) {
		this.call_put = call_put;
	}

	public double getDelta() {
		return this.delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public String getExchange() {
		return this.exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public Date getExpiration() {
		return this.expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public double getGamma() {
		return this.gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public double getIv() {
		return this.iv;
	}

	public void setIv(double iv) {
		this.iv = iv;
	}

	public double getMean_price() {
		return this.mean_price;
	}

	public void setMean_price(double mean_price) {
		this.mean_price = mean_price;
	}

	public int getOpen_interest() {
		return this.open_interest;
	}

	public void setOpen_interest(int open_interest) {
		this.open_interest = open_interest;
	}

	public String getOption_symbol() {
		return this.option_symbol;
	}

	public void setOption_symbol(String option_symbol) {
		this.option_symbol = option_symbol;
	}

	public double getRho() {
		return this.rho;
	}

	public void setRho(double rho) {
		this.rho = rho;
	}

	public double getStock_price_for_iv() {
		return this.stock_price_for_iv;
	}

	public void setStock_price_for_iv(double stock_price_for_iv) {
		this.stock_price_for_iv = stock_price_for_iv;
	}

	public int getStrike() {
		return this.strike;
	}

	public void setStrike(int strike) {
		this.strike = strike;
	}

	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getTheta() {
		return this.theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	public Date getTrade_date() {
		return this.trade_date;
	}

	public void setTrade_date(Date trade_date) {
		this.trade_date = trade_date;
	}

	public double getVega() {
		return this.vega;
	}

	public void setVega(double vega) {
		this.vega = vega;
	}

	public int getVolume() {
		return this.volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public String toString() {
					
		return  "Spx: " + Utils.asMMddYY(trade_date) + " " + this.adjusted_stock_close_price + "  " + Utils.asMMddYY(expiration) + strike + " " + (call_put.equals("C") ? "CALL" : "PUT") + 
				" Delta: " + this.delta + " Bid: " + this.bid + " Ask:" + this.ask + " Mid: " + this.mean_price;
	}

}