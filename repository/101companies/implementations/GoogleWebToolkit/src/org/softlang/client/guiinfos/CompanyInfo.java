package org.softlang.client.guiinfos;

import java.io.Serializable;
import java.util.Map;

public class CompanyInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6539729645301595406L;
	
	private String name;
	private Map<Integer, String> departments;
	private double total;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<Integer, String> getDepartments() {
		return departments;
	}
	
	public void setDepartments(Map<Integer, String> departments) {
		this.departments = departments;
	}
	
	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

}
