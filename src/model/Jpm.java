package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Jpm database table.
 * 
 */
@Entity
@NamedQuery(name="Jpm.findAll", query="SELECT e FROM Jpm e")
public class Jpm extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}