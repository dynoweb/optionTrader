package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Tna database table.
 * 
 */
@Entity
@NamedQuery(name="Tna.findAll", query="SELECT e FROM Tna e")
public class Tna extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}