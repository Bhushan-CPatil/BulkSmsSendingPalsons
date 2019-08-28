package com.palsons.bulksmssendingpalsons.model;

import com.google.gson.annotations.SerializedName;

public class TimeDelayResponse{

	@SerializedName("datetime")
	private String datetime;

	@SerializedName("repeatAfter")
	private String repeatAfter;

	public void setDatetime(String datetime){
		this.datetime = datetime;
	}

	public String getDatetime(){
		return datetime;
	}

	public void setRepeatAfter(String repeatAfter){
		this.repeatAfter = repeatAfter;
	}

	public String getRepeatAfter(){
		return repeatAfter;
	}

	@Override
 	public String toString(){
		return 
			"TimeDelayResponse{" + 
			"datetime = '" + datetime + '\'' + 
			",repeatAfter = '" + repeatAfter + '\'' + 
			"}";
		}
}