package org.softlang.client.interfaces;

import org.softlang.client.guiinfos.DepartmentInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("department")
public interface DepartmentService extends RemoteService {

	public double cut(int id);
	
	public DepartmentInfo getDepartment(int id);
	
	public DepartmentInfo saveDepartment(int id, String name, Integer parent, Integer manager);
	
}