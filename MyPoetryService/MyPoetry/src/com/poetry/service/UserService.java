package com.poetry.service;

import com.poetry.entity.User;
public interface UserService {

	User selectUser(String phoneNum);
	
	String insert(User u);
}
