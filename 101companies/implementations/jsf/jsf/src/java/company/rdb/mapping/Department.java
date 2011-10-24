package company.rdb.mapping;
// Generated 06.10.2011 18:06:22 by Hibernate Tools 3.2.1.GA


import company.dao.exception.CompanyException;
import company.dao.factory.DAOFactory;
import company.dao.factory.FactoryManager;
import company.dao.interfaces.DepartmentDAO;
import company.dao.interfaces.EmployeeDAO;
import company.dao.interfaces.entities.DepartmentInterface;
import company.dao.interfaces.entities.EmployeeInterface;
import company.dao.rdb.RdbDepartmentDAO;
import company.dao.rdb.RdbEmployeeDAO;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Department generated by hbm2java
 */
public class Department implements java.io.Serializable, DepartmentInterface {

    private Integer id;
    private Company company;
    private Department department;
    private String name;
    private Set<EmployeeInterface> employees = new HashSet<EmployeeInterface>(0);
    private Set<DepartmentInterface> departments = new HashSet<DepartmentInterface>(0);

    public Department() {
    }

	
    public Department(Company company, String name) {
        this.company = company;
        this.name = name;
    }
    public Department(Company company, Department department, String name, Set<EmployeeInterface> employees, Set<DepartmentInterface> departments) {
       this.company = company;
       this.department = department;
       this.name = name;
       this.employees = employees;
       this.departments = departments;
    }
   
    @Override
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
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public Set<EmployeeInterface> getEmployees() {
        return this.employees;
    }
    
    public void setEmployees(Set<EmployeeInterface> employees) {
        this.employees = employees;
    }
    
    @Override
    public Set<DepartmentInterface> getDepartments() {
        return this.departments;
    }
    
    public void setDepartments(Set<DepartmentInterface> departments) {
        this.departments = departments;
    }

    @Override
    public double total() throws CompanyException {
        double total = 0d;
        
        DAOFactory daoFactory = FactoryManager.getInstance().getDaoFactory();
        
        DepartmentDAO depDAO = daoFactory.getDepartmentDAO();
        List<DepartmentInterface> deps = ((RdbDepartmentDAO)depDAO).loadDepartmentsForDepartment(this.id);
        
        for (DepartmentInterface dep : deps) {
            total += dep.total();
        }
        
        EmployeeDAO emplDAO = daoFactory.getEmployeeDAO();
        List<EmployeeInterface> empls = ((RdbEmployeeDAO)emplDAO).loadEmployeesForDepartment(this.id);

        for (EmployeeInterface employee : empls) {
            total += employee.total();
        }
        return total;
    }

    @Override
    public void cut() throws CompanyException {
        DAOFactory daoFactory = FactoryManager.getInstance().getDaoFactory();
        
        DepartmentDAO depDAO = daoFactory.getDepartmentDAO();
        List<DepartmentInterface> deps = ((RdbDepartmentDAO)depDAO).loadDepartmentsForDepartment(this.id);
        
        for (DepartmentInterface dep : deps) {
            dep.cut();
        }
        
        EmployeeDAO emplDAO = daoFactory.getEmployeeDAO();
        List<EmployeeInterface> empls = ((RdbEmployeeDAO)emplDAO).loadEmployeesForDepartment(this.id);
        
        for (EmployeeInterface employee : empls) {
            employee.cut();
        }
    }


    


}


