package org.softlang.client.interfaces;

import org.softlang.client.guiinfos.EmployeeInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EmployeeServiceAsync {

	void cut(int id, AsyncCallback<Double> callback);

	void getEmployee(int id, AsyncCallback<EmployeeInfo> callback);

}
