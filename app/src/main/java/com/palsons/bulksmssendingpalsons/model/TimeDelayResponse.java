package com.palsons.bulksmssendingpalsons.model;

import com.google.gson.annotations.SerializedName;

public class TimeDelayResponse{

	@SerializedName("datetime")
	private String datetime;

	@SerializedName("password")
	private String password;

	@SerializedName("repeatAfter")
	private String repeatAfter;

	@SerializedName("id")
	private String id;

	public void setDatetime(String datetime){
		this.datetime = datetime;
	}

	public String getDatetime(){
		return datetime;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return password;
	}

	public void setRepeatAfter(String repeatAfter){
		this.repeatAfter = repeatAfter;
	}

	public String getRepeatAfter(){
		return repeatAfter;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}


	@Override
 	public String toString(){
		return 
			"TimeDelayResponse{" + 
			"datetime = '" + datetime + '\'' + 
			",password = '" + password + '\'' + 
			",repeatAfter = '" + repeatAfter + '\'' + 
			",id = '" + id + '\'' + 
			"}";
		}
}