package com.poetry.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.poetry.dao.CollectDao;
import com.poetry.dao.impl.CollectDaoImpl;
import com.poetry.entity.Collection;
import com.poetry.entity.Record;
import com.poetry.page.Pagination;

@WebServlet("/getCollectionList")
public class GetCollectionList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private CollectDao collectDao = new CollectDaoImpl();
	private Pagination<Collection> pagination = new Pagination<Collection>();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pageNum = request.getParameter("page");
		String phoneNumber = request.getParameter("phoneNumber");
		if (pageNum != null) {
			pagination.setPageNum(Integer.parseInt(pageNum));
		}
		pagination.setTotalItemsCount(collectDao.getCount(phoneNumber));
		List<Collection> rList = collectDao.selectByUser(phoneNumber, pagination);
		Gson gson = new Gson();
		String json = gson.toJson(rList);
		json = "{\"data\":" + json + "}";
		response.getOutputStream().write(json.getBytes("UTF-8"));
	}
}