package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the gld database table.
 * 
 */
@Entity
@NamedQuery(name="Gld.findAll", query="SELECT g FROM Gld g")
public class Gld extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;

}