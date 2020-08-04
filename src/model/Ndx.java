package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Ndx database table.
 * 
 */
@Entity
@NamedQuery(name="Ndx.findAll", query="SELECT e FROM Ndx e")
public class Ndx extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}