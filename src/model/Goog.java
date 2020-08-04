package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Goog database table.
 * 
 */
@Entity
@NamedQuery(name="Goog.findAll", query="SELECT e FROM Goog e")
public class Goog extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}