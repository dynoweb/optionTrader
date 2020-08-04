package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Amzn database table.
 * 
 */
@Entity
@NamedQuery(name="Amzn.findAll", query="SELECT e FROM Amzn e")
public class Amzn extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}