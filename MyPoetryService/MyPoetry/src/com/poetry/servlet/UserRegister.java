package com.poetry.servlet;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.poetry.entity.User;
import com.poetry.service.UserService;
import com.poetry.service.impl.UserServiceImpl;


@WebServlet("/userRegister")
public class UserRegister extends HttpServlet {
	private UserService userService = new UserServiceImpl();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("name");
		name = URLDecoder.decode(name, "UTF-8");
		String phoneNum = request.getParameter("phoneNum");
		String password = request.getParameter("password");
		String stringer = "";
		User u = userService.selectUser(phoneNum);//查该用户
		if(u != null) {
			stringer = "reregister";
			response.getOutputStream().write(stringer.getBytes("UTF-8"));
		} else {
			User user = new User();
			user.setName(name);
			user.setPassword(password);
			user.setPhoneNum(phoneNum);
			stringer = userService.insert(user);
			response.getOutputStream().write(stringer.getBytes("UTF-8"));
		}
	}
}