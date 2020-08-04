package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Rvx database table.
 * 
 */
@Entity
@NamedQuery(name="Rvx.findAll", query="SELECT e FROM Rvx e")
public class Rvx extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}