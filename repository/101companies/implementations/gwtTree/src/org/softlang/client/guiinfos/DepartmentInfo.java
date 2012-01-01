package org.softlang.client.guiinfos;

import java.io.Serializable;
import java.util.Map;

public class DepartmentInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3643721159314417030L;
	
	private String name;
	private double total;
	private Integer parent;
	private Map<Integer, String> departments;
	private Integer manager;
	private Map<Integer, String> employees;
	
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
	
	public Integer getManager() {
		return manager;
	}
	
	public void setManager(Integer manager) {
		this.manager = manager;
	}
	
	public Map<Integer, String> getEmployees() {
		return employees;
	}
	
	public void setEmployees(Map<Integer, String> employees) {
		this.employees = employees;
	}
	
	
}
