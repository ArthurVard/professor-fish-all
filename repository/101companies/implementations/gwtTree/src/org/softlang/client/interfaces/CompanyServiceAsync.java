package org.softlang.client.interfaces;

import org.softlang.client.guiinfos.CompanyInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CompanyServiceAsync {

	void cut(int id, AsyncCallback<Double> callback);

	void getCompany(int id, AsyncCallback<CompanyInfo> callback);

	void saveCompany(int id, String name, AsyncCallback<String> callback);

}
