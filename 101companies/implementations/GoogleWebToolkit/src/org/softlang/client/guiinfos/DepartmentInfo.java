package org.softlang.client.guiinfos;

import java.io.Serializable;
import java.util.Map;

public class DepartmentInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1431472991504297822L;
	
	private String name;
	private String manager;
	private Integer managerId;
	private Map<Integer, String> departments;
	private Map<Integer, String> employees;
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
	
	public Map<Integer, String> getEmployees() {
		return employees;
	}
	
	public void setEmployees(Map<Integer, String> employees) {
		this.employees = employees;
	}
	
	public double getTotal() {
		return total;
	}
	
	public void setTotal(double total) {
		this.total = total;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public Integer getManagerId() {
		return managerId;
	}

	public void setManagerId(Integer managerId) {
		this.managerId = managerId;
	}
}
