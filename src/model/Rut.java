package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the rut database table.
 * 
 */
@Entity
@NamedQuery(name="Rut.findAll", query="SELECT t FROM Rut t")
public class Rut extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}