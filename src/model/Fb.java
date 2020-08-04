package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Fb database table.
 * 
 */
@Entity
@NamedQuery(name="Fb.findAll", query="SELECT e FROM Fb e")
public class Fb extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}