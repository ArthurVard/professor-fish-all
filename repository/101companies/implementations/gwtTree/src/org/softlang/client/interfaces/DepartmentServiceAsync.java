package org.softlang.client.interfaces;

import org.softlang.client.guiinfos.DepartmentInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DepartmentServiceAsync {

	void cut(int id, AsyncCallback<Double> callback);

	void getDepartment(int id, AsyncCallback<DepartmentInfo> callback);

	void saveDepartment(int id, String name, Integer parent, Integer manager,
			AsyncCallback<DepartmentInfo> callback);
}
