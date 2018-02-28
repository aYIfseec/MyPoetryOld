package com.poetry.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.poetry.dao.RecordDao;
import com.poetry.entity.Record;
import com.poetry.page.Pagination;
import com.poetry.utils.DaoUtil;

public class RecordDaoImpl implements RecordDao {

	public int insert(Record r) {
		String sql = "insert into t_upload_record(phoneNumber,poetryId,poetryTitle,recordPath, uploadTime) values(?, ?, ?, ?, ?)";
		List<Object> parameterList = new ArrayList<Object>();
		parameterList.add(r.getPhoneNumber());
		parameterList.add(r.getPoetryId());
		parameterList.add(r.getPoetryTitle());
		parameterList.add(r.getRecordPath());
		parameterList.add(new Date(System.currentTimeMillis()));
		return DaoUtil.operUpdate(sql, parameterList);
	}

	public List<Record> selectByPoetryId(String poetryId,Pagination<Record> pagination) {
		String sql = "select A.id, name, uploadTime, recordPath, playCount, praiseCount " +
				"from t_upload_record as A, t_user as B " +
				"where A.phoneNumber = B.phoneNumber and poetryId = ? " +
				"order by playCount ,uploadTime desc limit ?, ?";
		List<Object> list = new ArrayList<Object>();
		list.add(poetryId);
		list.add(pagination.getStart());
		list.add(pagination.getPageSize());
		List<Record> resList = new ArrayList<Record>();
		try {
			resList = DaoUtil.operQuery(sql, list, Record.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resList;
	}
	
	public List<Record> selectByPhoneNumber(String phoneNumber,Pagination<Record> pagination) {
		String sql = "select A.id, poetryTitle, uploadTime, recordPath, playCount, praiseCount " +
				"from t_upload_record as A left join t_user as B " +
				"on A.phoneNumber = B.phoneNumber where B.phoneNumber = ? " +
				"order by playCount limit ?, ?";
		List<Object> list = new ArrayList<Object>();
		list.add(phoneNumber);
		list.add(pagination.getStart());
		list.add(pagination.getPageSize());
		List<Record> resList = new ArrayList<Record>();
		try {
			resList = DaoUtil.operQuery(sql, list, Record.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resList;
	}

	public void doPraiseCount(int id) {
		String sql = "{CALL user_do_praise(?)}";
		Connection connection = DaoUtil.getConn();
		CallableStatement cstm = null;
		try {
			cstm = connection.prepareCall(sql);
			cstm.setInt(1, id);
			cstm.execute(); // 执行存储过程 
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				cstm.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}
	}

	public void doPlayCount(int id) {
		String sql = "{CALL user_do_play(?)}";
		Connection connection = DaoUtil.getConn();
		CallableStatement cstm = null;
		try {
			cstm = connection.prepareCall(sql);
			cstm.setInt(1, id);
			cstm.execute(); // 执行存储过程 
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				cstm.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}
	}

	public int getCount(String poetryId) {
		return DaoUtil.getCount("t_upload_record", "id", " where poetryId=\""+poetryId + "\"");
	}
	
	public int getCountByUser(String phone) {
		return DaoUtil.getCount("t_upload_record", "id", " where phoneNumber=\""+phone + "\"");
	}

	public int delete(int id) {
		String sql = "delete from t_upload_record where id = "+id;
		return DaoUtil.operUpdate(sql, null);
	}

}
