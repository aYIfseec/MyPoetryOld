package com.poetry.dao;

import com.poetry.entity.User;

public interface UserDao {
	User selectUserByPhoneNum(String num);
	int insert(User u);
}
