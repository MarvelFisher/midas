package com.cyanspring.cstw.model.admin;

import com.cyanspring.cstw.model.BasicModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/06/11
 *
 */
public final class ExchangeAccountModel extends BasicModel {

	private String id;

	private String name;

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
	
	public static class Builder {
		private String id;
		private String name;
		
		public Builder id(String val) {
			id = val; 	return this;
		}
		
		public Builder name(String val) {
			name = val;		return this;
		}
		
		public ExchangeAccountModel build() {
			return new ExchangeAccountModel(this);
		}
	}
	
	private ExchangeAccountModel(Builder builder) {
		id = builder.id;
		name = builder.name;
	}


}
