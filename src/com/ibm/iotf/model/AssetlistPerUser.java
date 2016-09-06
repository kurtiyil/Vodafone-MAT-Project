package com.ibm.iotf.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AssetlistPerUser {

	private ArrayList <Asset> Data;
	private Result Result;
		




	public ArrayList<Asset> getData() {
		return Data;
	}





	public void setData(ArrayList<Asset> data) {
		Data = data;
	}





	public Result getResult() {
		return Result;
	}





	public void setResult(Result result) {
		Result = result;
	}





	private class Result{
		private String Error;
		private String ErrorCode;
		private String Status;
		public String getError() {
			return Error;
		}
		public void setError(String error) {
			Error = error;
		}
		public String getErrorCode() {
			return ErrorCode;
		}
		public void setErrorCode(String errorCode) {
			ErrorCode = errorCode;
		}
		public String getStatus() {
			return Status;
		}
		public void setStatus(String status) {
			Status = status;
		}

		
	}
	
}
