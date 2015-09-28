package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the spy database table.
 * 
 */
@Entity
@NamedQuery(name="Spy.findAll", query="SELECT s FROM Spy s")
public class Spy extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}