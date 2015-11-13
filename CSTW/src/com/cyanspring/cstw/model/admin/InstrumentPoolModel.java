package com.cyanspring.cstw.model.admin;

import com.cyanspring.cstw.model.BasicModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/20
 *
 */
public final class InstrumentPoolModel extends BasicModel {

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
	
	public static class Builder {
		private String id;
		private String name;
		private SubAccountModel relativeAccount;
		
		public Builder id(String val) {
			id = val;		return this;
		}
		
		public Builder name(String val) {
			name = val;		return this;
		}
		
		public Builder relativeAccount(SubAccountModel val) {
			relativeAccount = val;		return this;
		}
		
		public InstrumentPoolModel build() {
			return new InstrumentPoolModel(this);
		}
	}
	
	private InstrumentPoolModel(Builder builder) {
		id = builder.id;
		name = builder.name;
		relativeAccount = builder.relativeAccount;
	}

}