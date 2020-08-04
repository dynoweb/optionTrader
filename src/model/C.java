package model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the C database table.
 * 
 */
@Entity
@NamedQuery(name="C.findAll", query="SELECT e FROM C e")
public class C extends OptionPricing implements Serializable {
	private static final long serialVersionUID = 1L;


}