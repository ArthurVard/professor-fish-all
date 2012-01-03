package org.softlang.server;

import java.util.HashMap;
import java.util.Map;

import org.softlang.client.guiinfos.EmployeeInfo;
import org.softlang.client.interfaces.EmployeeService;
import org.softlang.server.company.Employee;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EmployeeServiceImpl extends RemoteServiceServlet implements EmployeeService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2006611102432366389L;

	@Override
	public double cut(int id) {
		Employee employee = CompanyApp.getInstance().getEmployees().get(id);
				
		employee.cut();
		
		return employee.getSalary();
	}

	@Override
	public EmployeeInfo getEmployee(int id) {
		EmployeeInfo result = new EmployeeInfo();
		
		Employee employee = CompanyApp.getInstance().getEmployees().get(id);
		
		result.setName(employee.getName());
		result.setAddress(employee.getAddress());
		result.setTotal(employee.getSalary());
		
		Map<Integer, String> allDeps = new HashMap<Integer, String>();
		for (Integer key : CompanyApp.getInstance().getDepartments().keySet()) {
			allDeps.put(key, CompanyApp.getInstance().getDepartments().get(key).getName());
		}
		result.setAllDepartments(allDeps);
		result.setParent(employee.getParent().getId());
		
		return result;
	}

	@Override
	public EmployeeInfo saveEmployee(int id, String name, String address,
			double salary, Integer parent) {
		Employee employee = CompanyApp.getInstance().getEmployees().get(id);
		employee.setName(name);
		employee.setAddress(address);
		employee.setSalary(salary);
		
		if (employee.getParent().getId() != parent) {
			employee.setParent(CompanyApp.getInstance().getDepartments().get(parent));
			employee.setManager(false);
		}

		return getEmployee(id);
	}

}
