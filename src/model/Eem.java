package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the eem database table.
 * 
 */
@Entity
@NamedQuery(name="Eem.findAll", query="SELECT e FROM Eem e")
public class Eem extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}