package com.tessoft.domain;

public class UserSetting {

	private String userID;
	private String settingName;
	private String settingValue;
	private String createdDate;
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getSettingName() {
		return settingName;
	}
	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}
	public String getSettingValue() {
		return settingValue;
	}
	public void setSettingValue(String settingValue) {
		this.settingValue = settingValue;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	
}
