package com.cyanspring.cstw.model.admin;

/**
 * @author Junfeng
 * @create 16 Nov 2015
 */
public class AssignedModel {
	
	private String userId;
	
	private String roleType;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}
	
	public static class Builder {
		private String userId;
		private String roleType;
		
		public Builder userId(String val) {
			userId = val;		return this;
		}
		
		public Builder roleType(String val) {
			roleType = val;		return this;
		}
		
		public AssignedModel build() {
			return new AssignedModel(this);
		}
	}
	
	private AssignedModel(Builder builder) {
		userId = builder.userId;
		roleType = builder.roleType;
	}
}
