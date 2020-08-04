package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the Amd database table.
 * 
 */
@Entity
@NamedQuery(name="Amd.findAll", query="SELECT e FROM Amd e")
public class Amd extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}