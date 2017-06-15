package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.user.User;

/**
 * 用户数据库操作类
 * 
 * @author
 */
public class UserDao {
	/**
	 * 添加用户
	 * 
	 * @param user
	 *            用户对象
	 */
	public void saveUser(User user) {
		// 获取数据库连接Connection对象
		Connection conn = ConnectDB.getConnection();
		// 插入用户注册信息的SQL语句
		String sql = "insert into tb_user(username,password,sex,tel,photo,email) values(?,?,?,?,?,?)";
		try {
			// 获取PreparedStatement对象
			PreparedStatement ps = conn.prepareStatement(sql);
			// 对SQL语句的占位符参数进行动态赋值
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPassword());
			ps.setString(3, user.getSex());
			ps.setString(4, user.getTel());
			ps.setString(5, user.getPhoto());
			ps.setString(6, user.getEmail());
			// 执行更新操作
			ps.executeUpdate();
			// 释放此 PreparedStatement 对象的数据库和 JDBC 资源
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭数据库连接
			ConnectDB.closeConnection(conn);
		}
	}

	/**
	 * 用户登录
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return 用户对象
	 */
	public User login(String username, String password) {
		User user = null;
		// 获取数据库连接Connection对象
		Connection conn = ConnectDB.getConnection();
		// 根据用户名及密码查询用户信息
		String sql = "select * from tb_user where username = ? and password = ?";
		try {
			// 获取PreparedStatement对象
			PreparedStatement ps = conn.prepareStatement(sql);
			// 对SQL语句的占位符参数进行动态赋值
			ps.setString(1, username);
			ps.setString(2, password);
			// 执行查询获取结果集
			ResultSet rs = ps.executeQuery();
			// 判断结果集是否有效
			if (rs.next()) {
				// 实例化一个用户对象
				user = new User();
				// 对用户对象属性赋值
				user.setId(rs.getInt("id"));
				user.setUsername(rs.getString("username"));
				user.setPassword(rs.getString("password"));
				user.setSex(rs.getString("sex"));
				user.setTel(rs.getString("tel"));
				user.setPhoto(rs.getString("photo"));
				user.setEmail(rs.getString("email"));
			}
			// 释放此 ResultSet 对象的数据库和 JDBC 资源
			rs.close();
			// 释放此 PreparedStatement 对象的数据库和 JDBC 资源
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭数据库连接
			ConnectDB.closeConnection(conn);
		}
		return user;
	}

	/**
	 * 判断用户名在数据库中是否存在
	 * 
	 * @param username
	 *            用户名
	 * @return 布尔值
	 */
	public boolean userIsExist(String username) {
		// 获取数据库连接Connection对象
		Connection conn = ConnectDB.getConnection();
		// 根据指定用户名查询用户信息
		String sql = "select * from tb_user where username = ?";
		try {
			// 获取PreparedStatement对象
			PreparedStatement ps = conn.prepareStatement(sql);
			// 对用户对象属性赋值
			ps.setString(1, username);
			// 执行查询获取结果集
			ResultSet rs = ps.executeQuery();
			// 判断结果集是否有效
			if (!rs.next()) {
				// 如果无效则证明此用户名可用
				return true;
			}
			// 释放此 ResultSet 对象的数据库和 JDBC 资源
			rs.close();
			// 释放此 PreparedStatement 对象的数据库和 JDBC 资源
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// 关闭数据库连接
			ConnectDB.closeConnection(conn);
		}
		return false;
	}
}
