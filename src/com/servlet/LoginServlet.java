package com.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dao.UserDao;
import com.user.User;

/**
 * 用户登录Servlet类
 * 
 * @author
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = -3009431503363456775L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 获取用户名
		String username = request.getParameter("username");
		// 获取密码
		String password = request.getParameter("password");
		// 实例化UserDao对象
		UserDao userDao = new UserDao();
		// 根据用户密码查询用户
		User user = userDao.login(username, password);
		// 判断user是否为空
		if (user != null) {
			// 将用户对象放入session中
			request.getSession().setAttribute("user", user);
			// 转发到result.jsp页面
			request.getRequestDispatcher("welcome.jsp").forward(request,
					response);
		} else {
			// 登录失败
			request.setAttribute("info", "错误：用户名或密码错误！");
			PrintWriter pw = response.getWriter();
			pw.write("<script language='javascript'>alert('用户名或密码错误！');window.history.back(-1); </script>");
		}
	}
}
