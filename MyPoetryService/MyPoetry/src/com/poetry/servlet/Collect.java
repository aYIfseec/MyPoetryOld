package com.poetry.servlet;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.poetry.dao.CollectDao;
import com.poetry.dao.RecordDao;
import com.poetry.dao.impl.CollectDaoImpl;
import com.poetry.dao.impl.RecordDaoImpl;

@WebServlet("/collect")
public class Collect extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private CollectDao collectDao = new CollectDaoImpl();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String intent = request.getParameter("do");
		String message = "";
		if ("collect".equals(intent)) {
			String poetryId = request.getParameter("poetryId");
			String phone = request.getParameter("phoneNumber");
			String poetryTitle = request.getParameter("poetryTitle");
			poetryTitle = URLDecoder.decode(poetryTitle, "UTF-8");
			
			if (collectDao.isCollected(poetryId, phone)) {
				message = "收藏夹中已存在此诗";
			} else {
				if (collectDao.Collect(phone, poetryId, poetryTitle)>0) {
					message = "收藏成功";
				} else {
					message = "收藏失败";
				}
			}
		} else if("cancelCollect".equals(intent)) {
			String collectId = request.getParameter("collectId");
			int id = 0;
			try{
				id = Integer.parseInt(collectId);
				if (collectDao.collectDao(id)>0) {
					message = "已取消收藏";
				} else {
					message = "取消收藏失败";
				}
			} catch(Exception e) {
				e.printStackTrace();
				message = "取消收藏失败,参数错误";
			}
			
		} else {
			message = "参数错误";
		}
		response.getOutputStream().write(message.getBytes("UTF-8"));
	}
}
