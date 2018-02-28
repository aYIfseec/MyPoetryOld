package com.poetry.service;

import java.util.List;

import com.poetry.entity.Record;
import com.poetry.page.Pagination;


public interface RecordService {

	int insert(Record record);

	List<Record> selectByPoetryId(String poetryId, Pagination<Record> pagination);

	int getCount(String poetryId);

	int getCountByUser(String phoneNumber);

	List<Record> selectByPhoneNumber(String phoneNumber,
			Pagination<Record> pagination);
}
