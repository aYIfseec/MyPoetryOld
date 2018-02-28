package com.poetry.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.poetry.dao.RecordDao;
import com.poetry.dao.impl.RecordDaoImpl;
@WebServlet("/updateRecord")
public class UpdateRecord extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private RecordDao recordDao = new RecordDaoImpl();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String intent = request.getParameter("do");
		String recordId = request.getParameter("recordId");
		int id = 0;
		try{
			id = Integer.parseInt(recordId);
			if ("doPraise".equals(intent)) {
				recordDao.doPraiseCount(id);
			} else if("doPlay".equals(intent)) {
				recordDao.doPlayCount(id);
			} else if("doDelete".equals(intent)) {
				if (recordDao.delete(id) > 0) {
					response.getOutputStream().write("已删除".getBytes("UTF-8"));
				} else {
					response.getOutputStream().write("操作失败".getBytes("UTF-8"));
				}
			}
		} catch (Exception e) {
			response.getOutputStream().write("参数错误".getBytes("UTF-8"));
		}
		//response.getOutputStream().write("".getBytes("UTF-8"));
	}
}
