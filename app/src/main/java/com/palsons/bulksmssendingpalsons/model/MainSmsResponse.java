package com.palsons.bulksmssendingpalsons.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class MainSmsResponse{

	@SerializedName("sim1count")
	private String sim1count;

	@SerializedName("SMSList")
	private List<SMSListItem> sMSList;

	@SerializedName("curdate")
	private String curdate;

	@SerializedName("sim2count")
	private String sim2count;

	public void setSim1count(String sim1count){
		this.sim1count = sim1count;
	}

	public String getSim1count(){
		return sim1count;
	}

	public void setSMSList(List<SMSListItem> sMSList){
		this.sMSList = sMSList;
	}

	public List<SMSListItem> getSMSList(){
		return sMSList;
	}

	public void setCurdate(String curdate){
		this.curdate = curdate;
	}

	public String getCurdate(){
		return curdate;
	}

	public void setSim2count(String sim2count){
		this.sim2count = sim2count;
	}

	public String getSim2count(){
		return sim2count;
	}

	@Override
 	public String toString(){
		return 
			"MainSmsResponse{" + 
			"sim1count = '" + sim1count + '\'' + 
			",sMSList = '" + sMSList + '\'' + 
			",curdate = '" + curdate + '\'' + 
			",sim2count = '" + sim2count + '\'' + 
			"}";
		}
}