package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the spx database table.
 * 
 */
@Entity
@NamedQuery(name="Spx.findAll", query="SELECT s FROM Spx s")
public class Spx extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}