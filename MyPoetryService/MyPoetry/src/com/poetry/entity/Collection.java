package com.poetry.entity;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Collection {

	private int id;
	private String phoneNumber;
	private String poetryId;
	private String poetryTitle;
	private Date collectTime;
	
	public String getPoetryTitle() {
		return poetryTitle;
	}
	public void setPoetryTitle(String poetryTitle) {
		this.poetryTitle = poetryTitle;
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
	public Date getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(Date collectTime) {
		this.collectTime = collectTime;
	}
}
