package com.poetry.service.impl;

import java.util.List;

import com.poetry.dao.RecordDao;
import com.poetry.dao.impl.RecordDaoImpl;
import com.poetry.entity.Record;
import com.poetry.page.Pagination;
import com.poetry.service.RecordService;

public class RecordServiceImpl implements RecordService {

	private RecordDao recordDao = new RecordDaoImpl();
	public int insert(Record record) {
		return recordDao.insert(record);
	}
	public List<Record> selectByPoetryId(String poetryId,Pagination<Record> pagination) {
		return recordDao.selectByPoetryId(poetryId, pagination);
	}
	public int getCount(String poetryId) {
		return recordDao.getCount(poetryId);
	}
	public int getCountByUser(String phoneNumber) {
		return recordDao.getCountByUser(phoneNumber);
	}
	public List<Record> selectByPhoneNumber(String phoneNumber,
			Pagination<Record> pagination) {
		return recordDao.selectByPhoneNumber(phoneNumber, pagination);
	}

}
