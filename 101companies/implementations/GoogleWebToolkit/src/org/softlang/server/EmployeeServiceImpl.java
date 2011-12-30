package org.softlang.server;

import org.softlang.client.guiinfos.EmployeeInfo;
import org.softlang.client.interfaces.EmployeeService;
import org.softlang.server.company.Employee;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EmployeeServiceImpl extends RemoteServiceServlet implements EmployeeService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6332540835039923850L;

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
		result.setSalary(employee.getSalary());
		
		return result;
	}



}
