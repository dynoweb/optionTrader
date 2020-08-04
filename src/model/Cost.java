package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Cost database table.
 * 
 */
@Entity
@NamedQuery(name="Cost.findAll", query="SELECT e FROM Cost e")
public class Cost extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}