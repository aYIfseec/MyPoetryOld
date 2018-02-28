package com.poetry.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.poetry.entity.User;
import com.poetry.service.UserService;
import com.poetry.service.impl.UserServiceImpl;

@WebServlet("/userLogin")
public class UserLogin extends HttpServlet {
	private UserService userService = new UserServiceImpl();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String phoneNum = request.getParameter("phoneNum");
		//获得登录时输入的用户名
		String password = request.getParameter("password");
		User user = userService.selectUser(phoneNum);//查该用户
		
		String stringer = "";
		System.out.println(phoneNum+"  "+password);
		//用户验证
		if(user != null && user.getPassword().equals(password)) {
			Gson gson = new GsonBuilder().create();
			stringer = gson.toJson(user);
		}
		response.getOutputStream().write(stringer.getBytes("UTF-8"));
	}
}
