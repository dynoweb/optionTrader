package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Slv database table.
 * 
 */
@Entity
@NamedQuery(name="Slv.findAll", query="SELECT e FROM Slv e")
public class Slv extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}