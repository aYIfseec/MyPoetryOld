package com.poetry.entity;

import java.sql.Date;

public class Record {
	private int id;
	private String phoneNumber;
	private String poetryId;
	private String poetryTitle;
	private String recordPath;
	private int praiseCount;
	private int playCount;
	private int state;
	private Date uploadTime;
	
	private String name;
	
	public String getPoetryTitle() {
		return poetryTitle;
	}
	public void setPoetryTitle(String poetryTitle) {
		this.poetryTitle = poetryTitle;
	}
	public Date getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getPoetryId() {
		return poetryId;
	}
	public void setPoetryId(String poetryId) {
		this.poetryId = poetryId;
	}
	public String getRecordPath() {
		return recordPath;
	}
	public void setRecordPath(String recordPath) {
		this.recordPath = recordPath;
	}
	public int getPraiseCount() {
		return praiseCount;
	}
	public void setPraiseCount(int like) {
		this.praiseCount = like;
	}
	public int getPlayCount() {
		return playCount;
	}
	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	
	
}
