package org.softlang.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.softlang.client.guiinfos.CompanyInfo;
import org.softlang.client.interfaces.CompanyService;
import org.softlang.server.company.Company;
import org.softlang.server.company.Department;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CompanyServiceImpl extends RemoteServiceServlet implements CompanyService {

	/**
	 * automatically generated serial-version UID
	 */
	private static final long serialVersionUID = -4414941964797293399L;

	@Override
	public double cut(int id) {
		Company company = CompanyApp.getInstance().getCompanies().get(id);
		
		company.cut();
		
		return company.total();
	}

	@Override
	public CompanyInfo getCompany(int id) {
		CompanyInfo result = new CompanyInfo();
		
		Company company = CompanyApp.getInstance().getCompanies().get(id);
		
		result.setName(company.getName());
		Map<Integer, String> departmentsMap = new HashMap<Integer, String>();
		List<Department> departments = company.getDepartments();
		for (Department department : departments) {
			departmentsMap.put(department.getId(), department.getName());
		}
		result.setDepartments(departmentsMap);
		result.setTotal(company.total());
		
		return result;
	}

	@Override
	public String saveCompany(int id, String name) {
		Company company = CompanyApp.getInstance().getCompanies().get(id);
		
		company.setName(name);
		
		return CompanyApp.getInstance().getCompanies().get(id).getName();
	}


	
}
