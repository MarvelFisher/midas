package com.cyanspring.common.info;

public class GroupInfo implements Comparable<GroupInfo>{
	private String groupID;
	private String groupName;
	private int symbolCount;
	public GroupInfo()
	{
		setGroupID(null);
		setGroupName(null);
		setSymbolCount(0);
	}
	public GroupInfo(String groupID, String groupName, int symbolCount)
	{
		setGroupID(groupID);
		setGroupName(groupName);
		setSymbolCount(symbolCount);
	}
	
	public String getGroupID() {
		return groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public int getSymbolCount() {
		return symbolCount;
	}
	public void setSymbolCount(int symbolCount) {
		this.symbolCount = symbolCount;
	}

	@Override
	public int compareTo(GroupInfo o) 
	{
		return groupID.compareTo(o.groupID);
	}
}
