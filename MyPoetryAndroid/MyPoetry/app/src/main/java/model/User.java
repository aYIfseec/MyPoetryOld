package model;

import java.io.Serializable;

public class User implements Serializable {
	private int id;
	private String phoneNumber;
	private String name;
	private String password;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPhoneNum() {
		return phoneNumber;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNumber = phoneNum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
