package com.ibm.iotf.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Config {
	  private String _id; 
	  private String _rev;
	  private String type;
	  private Mat mat;
	  private Wiotp wiotp;
	  private String crontab;
	  private int testmode;
	  private String startDate;
	  
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public Mat getMat() {
		return mat;
	}
	public void setMat(Mat mat) {
		this.mat = mat;
	}
	public Wiotp getWiotp() {
		return wiotp;
	}
	public void setWiotp(Wiotp wiotp) {
		this.wiotp = wiotp;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String get_rev() {
		return _rev;
	}
	public void set_rev(String _rev) {
		this._rev = _rev;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCrontab() {
		return crontab;
	}
	public void setCrontab(String crontab) {
		this.crontab = crontab;
	}
	public int getTestmode() {
		return testmode;
	}
	public void setTestmode(int testmode) {
		this.testmode = testmode;
	}
	  
	  
}
