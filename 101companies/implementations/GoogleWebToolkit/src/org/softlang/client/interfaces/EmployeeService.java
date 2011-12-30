package org.softlang.client.interfaces;

import org.softlang.client.guiinfos.EmployeeInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("employee")
public interface EmployeeService extends RemoteService {

	public double cut(int id);
	
	public EmployeeInfo getEmployee(int id);
	
}
