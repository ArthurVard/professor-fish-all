package org.softlang.server;

import java.util.HashMap;
import java.util.Map;

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
		Department department = CompanyApp.getInstance().getDepartments().get(id);
		
		department.cut();
		
		return department.total();
	}

	@Override
	public DepartmentInfo getDepartment(int id) {
		DepartmentInfo result = new DepartmentInfo();
		
		Department department = CompanyApp.getInstance().getDepartments().get(id);
		
		result.setName(department.getName());
		result.setTotal(department.total());
		
		Map<Integer, String> otherDeps = new HashMap<Integer, String>();
		for (Integer key : CompanyApp.getInstance().getDepartments().keySet()) {
			if (key != department.getId()) {
				otherDeps.put(key, CompanyApp.getInstance().getDepartments().get(key).getName());
			}
		}
		result.setOtherDepartments(otherDeps);
		if (department.getParent() instanceof Department) {
			result.setParentDepartment(department.getParent().getId());
		} else {
			result.setParentDepartment(null);
		}
		
		Map<Integer, String> emps = new HashMap<Integer, String>();
		for (Integer key : CompanyApp.getInstance().getEmployees().keySet()) {
			emps.put(key, CompanyApp.getInstance().getEmployees().get(key).getName());
		}
		result.setAllEmployees(emps);
		
		if (department.getManager() != null) {
			result.setManager(department.getManager().getId());
		} else {
			result.setManager(null);
		}
		
		
		return result;
	}

	@Override
	public DepartmentInfo saveDepartment(int id, String name, Integer parent,
			Integer manager) {
		Department department = CompanyApp.getInstance().getDepartments().get(id);
		
		department.setName(name);
		if (parent != null) {
			department.setParent(CompanyApp.getInstance().getDepartments().get(parent));
		} else {
			department.setParent(CompanyApp.getInstance().getCompanies().get(1));
		}
		
		if (manager != null) {
			department.setManager(CompanyApp.getInstance().getEmployees().get(manager));
		} else {
			department.setManager(null);
		}
		
		return getDepartment(id);
	}

}
