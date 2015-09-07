package model;

import java.io.Serializable;

import javax.persistence.*;

import misc.ProjectProperties;

import java.util.Date;


/**
 * The persistent class for the options_expirations database table.
 * 
 */
@Entity
@Table(name="options_expirations")
@NamedQuery(name="OptionsExpiration.findAll", query="SELECT o FROM OptionsExpiration o")
public class OptionsExpiration implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Temporal(TemporalType.DATE)
	private Date expiration;

	private String symbol;

	public OptionsExpiration() {
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getExpiration() {
		return this.expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public String toString() {
		
		return "OptionsExpiration: " + symbol + " " + ProjectProperties.dateFormat.format(expiration);
	}

}