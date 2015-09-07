package model;

import java.io.Serializable;

import javax.persistence.*;

import misc.Utils;

import java.util.Date;


/**
 * The persistent class for the holiday database table.
 * 
 */
@Entity
@NamedQuery(name="Holiday.findAll", query="SELECT h FROM Holiday h")
public class Holiday implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Temporal(TemporalType.DATE)
	private Date holiday;

	private String name;

	public Holiday() {
	}

	public Date getHoliday() {
		return this.holiday;
	}

	public void setHoliday(Date holiday) {
		this.holiday = holiday;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		
		return "Holiday: " + Utils.asMMMddYYYY(holiday) + " " + name;
	}

}