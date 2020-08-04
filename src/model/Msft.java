package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Msft database table.
 * 
 */
@Entity
@NamedQuery(name="Msft.findAll", query="SELECT e FROM Msft e")
public class Msft extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}