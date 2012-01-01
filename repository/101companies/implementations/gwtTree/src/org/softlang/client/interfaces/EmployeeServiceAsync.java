package org.softlang.client.interfaces;

import org.softlang.client.guiinfos.EmployeeInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EmployeeServiceAsync {

	void cut(int id, AsyncCallback<Double> callback);

	void saveEmployee(int id, String name, String address, double salary,
			Integer parent, AsyncCallback<String> callback);

	void getEmployee(int id, AsyncCallback<EmployeeInfo> callback);

}
