package com.poetry.dao.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.poetry.dao.CollectDao;
import com.poetry.entity.Collection;
import com.poetry.entity.Record;
import com.poetry.page.Pagination;
import com.poetry.utils.DaoUtil;

public class CollectDaoImpl implements CollectDao {

	public int Collect(String phoneNumber, String poetryId, String poetryTitle) {
		String sql = "insert into t_user_collection (phoneNumber, poetryId, poetryTitle, collectTime) value(?, ?, ?, ?)";
		List<Object> parameterList = new ArrayList<Object>();
		parameterList.add(phoneNumber);
		parameterList.add(poetryId);
		parameterList.add(poetryTitle);
		parameterList.add(new Date(System.currentTimeMillis()));
		return DaoUtil.operUpdate(sql, parameterList);
	}

	public int collectDao(int id) {
		String sql = "delete from t_user_collection where id=?";
		List<Object> parameterList = new ArrayList<Object>();
		parameterList.add(id);
		return DaoUtil.operUpdate(sql, parameterList);
	}

	public int getCount(String phoneNumber) {
		return DaoUtil.getCount("t_user_collection", "id", " where phoneNumber=\""+phoneNumber + "\"");
	}

	public List<Collection> selectByUser(String phoneNumber,
			Pagination<Collection> pagination) {
		String sql = "select * from t_user_collection " +
				"where phoneNumber = ? order by collectTime desc limit ?, ?";
		List<Object> list = new ArrayList<Object>();
		list.add(phoneNumber);
		list.add(pagination.getStart());
		list.add(pagination.getPageSize());
		List<Collection> resList = new ArrayList<Collection>();
		try {
			resList = DaoUtil.operQuery(sql, list, Collection.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resList;
	}

	public boolean isCollected(String poetryId, String phone) {
		String sql = "select id from t_user_collection " +
				"where phoneNumber = ? and poetryId = ?";
		List<Object> list = new ArrayList<Object>();
		list.add(phone);
		list.add(poetryId);
		List<Collection> resList = new ArrayList<Collection>();
		try {
			resList = DaoUtil.operQuery(sql, list, Collection.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resList.size() > 0 ? true : false;
	}

}
