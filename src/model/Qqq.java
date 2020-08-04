package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Qqq database table.
 * 
 */
@Entity
@NamedQuery(name="Qqq.findAll", query="SELECT e FROM Qqq e")
public class Qqq extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}