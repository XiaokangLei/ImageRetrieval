package com.user;

public class PicturesData {
	private int id; // 标志
	private String name; // 图片名字
	private String address; // 图片位置
	private String histogramdata; // 直方图数据
	private String shapedata;
	private String graindata;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getHistogramdata() {
		return histogramdata;
	}

	public void setHistogramdata(String histogramdata) {
		this.histogramdata = histogramdata;
	}

	public String getShapedata() {
		return shapedata;
	}

	public void setShapedata(String shapedata) {
		this.shapedata = shapedata;
	}

	public String getGraindata() {
		return graindata;
	}

	public void setGraindata(String graindata) {
		this.graindata = graindata;
	}
}
