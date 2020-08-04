package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the aapl database table.
 * 
 */
@Entity
@NamedQuery(name="Aapl.findAll", query="SELECT s FROM Aapl s")
public class Aapl extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}