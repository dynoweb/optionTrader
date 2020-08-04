package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Ba database table.
 * 
 */
@Entity
@NamedQuery(name="Ba.findAll", query="SELECT e FROM Ba e")
public class Ba extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}