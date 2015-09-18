package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the tlt database table.
 * 
 */
@Entity
@NamedQuery(name="Tlt.findAll", query="SELECT t FROM Tlt t")
public class Tlt extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}