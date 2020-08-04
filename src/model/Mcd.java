package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Mcd database table.
 * 
 */
@Entity
@NamedQuery(name="Mcd.findAll", query="SELECT e FROM Mcd e")
public class Mcd extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}