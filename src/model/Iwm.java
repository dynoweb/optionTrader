package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the iwm database table.
 * 
 */
@Entity
@NamedQuery(name="Iwm.findAll", query="SELECT i FROM Iwm i")
public class Iwm extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}