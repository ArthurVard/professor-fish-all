package org.softlang.client.interfaces;

import org.softlang.client.guiinfos.CompanyInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CompanyServiceAsync {

	void getCompany(int id, AsyncCallback<CompanyInfo> callback);

	void cut(int id, AsyncCallback<Double> callback);

	void saveCompany(int id, String name, AsyncCallback<String> callback);

}
