package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Tqqq database table.
 * 
 */
@Entity
@NamedQuery(name="Tqqq.findAll", query="SELECT e FROM Tqqq e")
public class Tqqq extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}