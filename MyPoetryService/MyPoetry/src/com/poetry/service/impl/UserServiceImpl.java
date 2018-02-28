package com.poetry.service.impl;

import com.poetry.entity.User;
import com.poetry.service.UserService;
import com.poetry.dao.UserDao;
import com.poetry.dao.impl.UserDaoImpl;

public class UserServiceImpl implements UserService {

	private UserDao userDao = new UserDaoImpl();
	
	public User selectUser(String phoneNum) {
		return userDao.selectUserByPhoneNum(phoneNum);
	}

	public String insert(User u) {
		int result = userDao.insert(u);
		return result > 0 ? "success":"fail";
	}

}
