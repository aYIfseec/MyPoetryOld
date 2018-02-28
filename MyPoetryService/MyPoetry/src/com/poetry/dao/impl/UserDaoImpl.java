package com.poetry.dao.impl;

import java.util.ArrayList;
import java.util.List;

import com.poetry.dao.UserDao;
import com.poetry.entity.User;
import com.poetry.utils.DaoUtil;

public class UserDaoImpl implements UserDao{
	
	public User selectUserByPhoneNum(String num) {
		String sql = "select * from t_user where phoneNumber = ?";
		List<Object> list = new ArrayList<Object>(1);
		list.add(num);
		List<User> user = null;
		try {
			user = DaoUtil.operQuery(sql, list, User.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user.size() > 0 ? user.get(0) : null;
	}

	public int insert(User u) {
		String sql = "insert into t_user value(0,?,?,?)";
		List<Object> list = new ArrayList<Object>(1);
		list.add(u.getPhoneNum());
		list.add(u.getName());
		list.add(u.getPassword());
		return DaoUtil.operUpdate(sql, list);
	}
}
