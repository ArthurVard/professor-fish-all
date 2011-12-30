package org.softlang.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.softlang.client.guiinfos.DepartmentInfo;
import org.softlang.client.interfaces.DepartmentService;
import org.softlang.server.company.Department;
import org.softlang.server.company.Employee;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DepartmentServiceImpl extends RemoteServiceServlet implements DepartmentService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1927250711954216570L;

	@Override
	public double cut(int id) {
		Department department = CompanyApp.getInstance().getDepartments().get(id);
		
		department.cut();
		
		return department.total();
	}

	@Override
	public DepartmentInfo getDepartment(int id) {
		DepartmentInfo result = new DepartmentInfo();
		
		Department department = CompanyApp.getInstance().getDepartments().get(id);
		
		result.setName(department.getName());
		Map<Integer, String> departmentsMap = new HashMap<Integer, String>();
		Map<Integer, String> employeesMap = new HashMap<Integer, String>();
		List<Department> departments = department.getDepartments();
		List<Employee> employees = department.getEmployees();
		for (Department dep : departments) {
			departmentsMap.put(dep.getId(), dep.getName());
		}
		for (Employee emp : employees) {
			if (!emp.isManager()) {
				employeesMap.put(emp.getId(), emp.getName());
			} else {
				result.setManager(emp.getName());
				result.setManagerId(emp.getId());
			}
			
		}
		result.setDepartments(departmentsMap);
		result.setEmployees(employeesMap);
		result.setTotal(department.total());
		
		return result;
	}

	@Override
	public String saveDepartment(int id, String name) {
		Department department = CompanyApp.getInstance().getDepartments().get(id);
		
		department.setName(name);
		
		return CompanyApp.getInstance().getDepartments().get(id).getName();
	}



}
