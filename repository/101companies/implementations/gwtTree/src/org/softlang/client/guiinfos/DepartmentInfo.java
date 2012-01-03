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
	private Integer parentDepartment;
	private Map<Integer, String> otherDepartments;
	private Integer manager;
	private Map<Integer, String> allEmployees;
	
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
	
	public Integer getParentDepartment() {
		return parentDepartment;
	}
	
	public void setParentDepartment(Integer parent) {
		this.parentDepartment = parent;
	}
	
	public Map<Integer, String> getOtherDepartments() {
		return otherDepartments;
	}
	
	public void setOtherDepartments(Map<Integer, String> departments) {
		this.otherDepartments = departments;
	}
	
	public Integer getManager() {
		return manager;
	}
	
	public void setManager(Integer manager) {
		this.manager = manager;
	}
	
	public Map<Integer, String> getAllEmployees() {
		return allEmployees;
	}
	
	public void setAllEmployees(Map<Integer, String> employees) {
		this.allEmployees = employees;
	}
	
	
}
