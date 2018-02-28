package com.poetry.servlet;


import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.poetry.entity.Record;
import com.poetry.page.Pagination;
import com.poetry.service.RecordService;
import com.poetry.service.impl.RecordServiceImpl;

@WebServlet("/getMyUploadRecord")
public class GetMyUploadRecord extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private RecordService recordService = new RecordServiceImpl();
	private Pagination<Record> pagination = new Pagination<Record>();
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
		pagination.setTotalItemsCount(recordService.getCountByUser(phoneNumber));
		List<Record> rList = recordService.selectByPhoneNumber(phoneNumber, pagination);
		Gson gson = new Gson();
		String json = gson.toJson(rList);
		json = "{\"data\":" + json + "}";
		response.getOutputStream().write(json.getBytes("UTF-8"));
	}
}

