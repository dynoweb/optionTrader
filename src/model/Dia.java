package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Dia database table.
 * 
 */
@Entity
@NamedQuery(name="Dia.findAll", query="SELECT e FROM Dia e")
public class Dia extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}