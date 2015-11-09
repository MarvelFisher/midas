package com.cyanspring.cstw.model.admin;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/20
 *
 */
public final class StockPoolModel {

	private String id;

	private String name;

	private SubAccountModel relativeAccount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SubAccountModel getRelativeAccount() {
		return relativeAccount;
	}

	public void setRelativeAccount(SubAccountModel relativeAccount) {
		this.relativeAccount = relativeAccount;
	}

}
