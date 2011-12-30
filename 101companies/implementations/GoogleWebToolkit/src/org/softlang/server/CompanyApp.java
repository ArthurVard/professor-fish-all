package org.softlang.server;

import java.util.HashMap;
import java.util.Map;

import org.softlang.server.company.Company;
import org.softlang.server.company.Department;
import org.softlang.server.company.Employee;

public class CompanyApp {
	
	private static CompanyApp singleton;
	
	private Map<Integer, Company> companies;
	private Map<Integer, Department> departments;
	private Map<Integer, Employee> employees;
	
	private CompanyApp() {
		Employee craig = new Employee(1, "Craig", "Redmond", 123456, true);
		Employee ray = new Employee(2, "Ray", "Redmond", 234567, true);
		Employee klaus = new Employee(3, "Klaus", "Boston", 23456, true);
		Employee karl = new Employee(4, "Karl", "Riga", 2345, true);
		Employee erik = new Employee(5, "Erik", "Utrecht", 12345, false);
		Employee ralf = new Employee(6, "Ralf", "Koblenz", 1234, false);
		Employee joe = new Employee(7, "Joe", "Wifi City", 2344, false);
		
		employees = new HashMap<Integer, Employee>();
		employees.put(craig.getId(), craig);
		employees.put(ray.getId(), ray);
		employees.put(klaus.getId(), klaus);
		employees.put(karl.getId(), karl);
		employees.put(erik.getId(), erik);
		employees.put(ralf.getId(), ralf);
		employees.put(joe.getId(), joe);
		
		Department research = new Department(1, "Research");
		Department development = new Department(2, "Development");
		Department dev1 = new Department(3, "Dev 1");
		Department dev11 = new Department(4, "Dev 1.1");
		
		research.getEmployees().add(craig);
		research.getEmployees().add(erik);
		research.getEmployees().add(ralf);
		development.getEmployees().add(ray);
		development.getDepartments().add(dev1);
		dev1.getEmployees().add(klaus);
		dev1.getDepartments().add(dev11);
		dev11.getEmployees().add(karl);
		dev11.getEmployees().add(joe);
		
		departments = new HashMap<Integer, Department>();
		departments.put(research.getId(), research);
		departments.put(development.getId(), development);
		departments.put(dev1.getId(), dev1);
		departments.put(dev11.getId(), dev11);		
		
		Company company = new Company(1, "Meganalysis");
		
		company.getDepartments().add(research);
		company.getDepartments().add(development);
		
		companies = new HashMap<Integer, Company>();
		companies.put(company.getId(), company);
	}
	
	public static CompanyApp getInstance() {
		if (singleton == null) {
			singleton = new CompanyApp();
		}
		return singleton;
	}

	public Map<Integer, Company> getCompanies() {
		return companies;
	}

	public void setCompanies(Map<Integer, Company> companies) {
		this.companies = companies;
	}

	public Map<Integer, Department> getDepartments() {
		return departments;
	}

	public void setDepartments(Map<Integer, Department> departments) {
		this.departments = departments;
	}

	public Map<Integer, Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(Map<Integer, Employee> employees) {
		this.employees = employees;
	}
}
