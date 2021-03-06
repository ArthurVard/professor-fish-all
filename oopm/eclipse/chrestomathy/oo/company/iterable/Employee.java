// (C) 2009 Ralf Laemmel

package oo.company.iterable;

/**
 * An employee has attributes for name and salary.
 */
public class Employee extends Unit {

	private String name;
	private double salary;
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public double getSalary() { return salary; }
	public void setSalary(double salary) { this.salary = salary; }

}
