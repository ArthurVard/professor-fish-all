package org.softlang.server.company;

import java.util.ArrayList;
import java.util.List;

public class Department {
	
	private int id;
	private String name;
	private List<Department> departments;
	private List<Employee> employees;
	
	public Department(int id, String name) {
		this.id = id;
		this.name = name;
		this.departments = new ArrayList<Department>();
		this.employees = new ArrayList<Employee>();
	}
	
	public Department(int id, String name, List<Department> departments, List<Employee> employees) {
		this.id = id;
		this.name = name;
		this.departments = departments;
		this.employees = employees;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Department> getDepartments() {
		return departments;
	}
	
	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}
	
	public List<Employee> getEmployees() {
		return employees;
	}
	
	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}
	
	public int getId() {
		return id;
	}
	
	public double total() {
		double total = 0;
		
		for (Employee employee : employees) {
			total += employee.getSalary();
		}
		for (Department department : departments) {
			total += department.total();
		}
		
		return total;
	}
	
	public void cut() {
		for (Employee employee : employees) {
			employee.cut();
		}
		for (Department department : departments) {
			department.cut();
		}
	}
	
}
