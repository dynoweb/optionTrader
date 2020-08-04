package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Fas database table.
 * 
 */
@Entity
@NamedQuery(name="Fas.findAll", query="SELECT e FROM Fas e")
public class Fas extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}