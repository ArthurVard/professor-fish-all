package company.mapping;
// Generated 06.10.2011 18:06:22 by Hibernate Tools 3.2.1.GA


import java.util.HashSet;
import java.util.Set;

/**
 * Department generated by hbm2java
 */
public class Department  implements java.io.Serializable {


    private Integer id;
    private Company company;
    private Department department;
    private String name;
    private Set<Employee> employees = new HashSet<Employee>(0);
    private Set<Department> departments = new HashSet<Department>(0);

    public Department() {
    }

	
    public Department(Company company, String name) {
        this.company = company;
        this.name = name;
    }
    public Department(Company company, Department department, String name, Set<Employee> employees, Set<Department> departments) {
       this.company = company;
       this.department = department;
       this.name = name;
       this.employees = employees;
       this.departments = departments;
    }
   
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    public Company getCompany() {
        return this.company;
    }
    
    public void setCompany(Company company) {
        this.company = company;
    }
    public Department getDepartment() {
        return this.department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
    }
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public Set<Employee> getEmployees() {
        return this.employees;
    }
    
    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
    public Set<Department> getDepartments() {
        return this.departments;
    }
    
    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

    public double total() {
        double total = 0d;
        for (Department dep : this.departments) {
            total += dep.total();
        }
        for (Employee employee : this.employees) {
            total += employee.getSalary();
        }
        return total;
    }

    


}


