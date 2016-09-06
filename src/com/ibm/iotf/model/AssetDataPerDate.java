package com.ibm.iotf.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AssetDataPerDate {
	private ArrayList <AssetData> Data;

	public ArrayList<AssetData> getData() {
		return Data;
	}

	public void setData(ArrayList<AssetData> data) {
		Data = data;
	}
	

}
