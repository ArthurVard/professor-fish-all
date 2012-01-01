package org.softlang.server;

import org.softlang.client.guiinfos.EmployeeInfo;
import org.softlang.client.interfaces.EmployeeService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EmployeeServiceImpl extends RemoteServiceServlet implements EmployeeService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2006611102432366389L;

	@Override
	public double cut(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EmployeeInfo getEmployee(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String saveEmployee(int id, String name, String address,
			double salary, Integer parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
