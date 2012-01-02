package org.softlang.client.guiinfos;

import java.io.Serializable;
import java.util.Map;

public class EmployeeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7339458164866881122L;
	
	private String name;
	private String address;
	private double total;
	private Integer parent;
	private Map<Integer, String> departments;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public double getTotal() {
		return total;
	}
	
	public void setTotal(double total) {
		this.total = total;
	}
	
	public Integer getParent() {
		return parent;
	}
	
	public void setParent(Integer parent) {
		this.parent = parent;
	}
	
	public Map<Integer, String> getDepartments() {
		return departments;
	}
	
	public void setDepartments(Map<Integer, String> departments) {
		this.departments = departments;
	}
	
	
}