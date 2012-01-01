package org.softlang.server;

import org.softlang.client.guiinfos.DepartmentInfo;
import org.softlang.client.interfaces.DepartmentService;
import org.softlang.server.company.Department;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DepartmentServiceImpl extends RemoteServiceServlet implements DepartmentService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 864523380679574211L;

	@Override
	public double cut(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DepartmentInfo getDepartment(int id) {
		DepartmentInfo result = new DepartmentInfo();
		
		Department department = CompanyApp.getInstance().getDepartments().get(id);
		
		result.setName(department.getName());
		result.setTotal(department.total());
		
		return result;
	}

	@Override
	public String saveDepartment(int id, String name, Integer parent,
			Integer manager) {
		// TODO Auto-generated method stub
		return null;
	}

}
