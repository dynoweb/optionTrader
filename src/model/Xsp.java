package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the Xsp database table.
 * 
 */
@Entity
@NamedQuery(name="Xsp.findAll", query="SELECT s FROM Xsp s")
public class Xsp extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}