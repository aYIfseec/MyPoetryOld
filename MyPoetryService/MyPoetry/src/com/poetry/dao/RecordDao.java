package com.poetry.dao;

import java.util.List;

import com.poetry.entity.Record;
import com.poetry.page.Pagination;

public interface RecordDao {
	int insert(Record r);

	void doPraiseCount(int id);

	void doPlayCount(int id);

	List<Record> selectByPoetryId(String poetryId, Pagination<Record> pagination);

	int getCount(String poetryId);

	int getCountByUser(String phoneNumber);

	List<Record> selectByPhoneNumber(String phoneNumber,
			Pagination<Record> pagination);

	int delete(int id);
}
