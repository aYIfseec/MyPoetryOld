package com.poetry.dao;

import java.util.List;

import com.poetry.entity.Collection;
import com.poetry.entity.Record;
import com.poetry.page.Pagination;

public interface CollectDao {

	int Collect(String phoneNumber, String poetryId, String poetryTitle);

	int collectDao(int id);

	int getCount(String phoneNumber);

	List<Collection> selectByUser(String phoneNumber, Pagination<Collection> pagination);

	boolean isCollected(String poetryId, String phone);

}
