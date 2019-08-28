package com.palsons.bulksmssendingpalsons.model;

import com.google.gson.annotations.SerializedName;

public class SMSListItem{

	@SerializedName("DIV")
	private String dIV;

	@SerializedName("sms_sent_from_sim")
	private String smsSentFromSim;

	@SerializedName("smsid")
	private String smsid;

	@SerializedName("sms_send_date")
	private String smsSendDate;

	@SerializedName("sms_flag")
	private String smsFlag;

	@SerializedName("added_by")
	private String addedBy;

	@SerializedName("deleteddate")
	private String deleteddate;

	@SerializedName("deleted_by")
	private String deletedBy;

	@SerializedName("mobileno")
	private String mobileno;

	@SerializedName("text_msg")
	private String textMsg;

	@SerializedName("adddate")
	private String adddate;

	@SerializedName("ecode")
	private String ecode;

	public void setDIV(String dIV){
		this.dIV = dIV;
	}

	public String getDIV(){
		return dIV;
	}

	public void setSmsSentFromSim(String smsSentFromSim){
		this.smsSentFromSim = smsSentFromSim;
	}

	public String getSmsSentFromSim(){
		return smsSentFromSim;
	}

	public void setSmsid(String smsid){
		this.smsid = smsid;
	}

	public String getSmsid(){
		return smsid;
	}

	public void setSmsSendDate(String smsSendDate){
		this.smsSendDate = smsSendDate;
	}

	public String getSmsSendDate(){
		return smsSendDate;
	}

	public void setSmsFlag(String smsFlag){
		this.smsFlag = smsFlag;
	}

	public String getSmsFlag(){
		return smsFlag;
	}

	public void setAddedBy(String addedBy){
		this.addedBy = addedBy;
	}

	public String getAddedBy(){
		return addedBy;
	}

	public void setDeleteddate(String deleteddate){
		this.deleteddate = deleteddate;
	}

	public String getDeleteddate(){
		return deleteddate;
	}

	public void setDeletedBy(String deletedBy){
		this.deletedBy = deletedBy;
	}

	public String getDeletedBy(){
		return deletedBy;
	}

	public void setMobileno(String mobileno){
		this.mobileno = mobileno;
	}

	public String getMobileno(){
		return mobileno;
	}

	public void setTextMsg(String textMsg){
		this.textMsg = textMsg;
	}

	public String getTextMsg(){
		return textMsg;
	}

	public void setAdddate(String adddate){
		this.adddate = adddate;
	}

	public String getAdddate(){
		return adddate;
	}

	public void setEcode(String ecode){
		this.ecode = ecode;
	}

	public String getEcode(){
		return ecode;
	}

	@Override
 	public String toString(){
		return 
			"SMSListItem{" + 
			"dIV = '" + dIV + '\'' + 
			",sms_sent_from_sim = '" + smsSentFromSim + '\'' + 
			",smsid = '" + smsid + '\'' + 
			",sms_send_date = '" + smsSendDate + '\'' + 
			",sms_flag = '" + smsFlag + '\'' + 
			",added_by = '" + addedBy + '\'' + 
			",deleteddate = '" + deleteddate + '\'' + 
			",deleted_by = '" + deletedBy + '\'' + 
			",mobileno = '" + mobileno + '\'' + 
			",text_msg = '" + textMsg + '\'' + 
			",adddate = '" + adddate + '\'' + 
			",ecode = '" + ecode + '\'' + 
			"}";
		}
}