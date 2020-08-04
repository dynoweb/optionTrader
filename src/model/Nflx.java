package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Nflx database table.
 * 
 */
@Entity
@NamedQuery(name="Nflx.findAll", query="SELECT e FROM Nflx e")
public class Nflx extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}