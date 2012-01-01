package org.softlang.client.guiinfos;

import java.io.Serializable;

public class CompanyInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6539729645301595406L;
	
	private String name;
	private double total;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

}
