package com.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接
 * 
 * @author
 */
public class ConnectDB {
	/**
	 * 获取数据库连接
	 * 
	 * @return Connection对象
	 */
	public static Connection getConnection() {
		Connection conn = null;
		try {
			// 加载驱动
			Class.forName("com.mysql.jdbc.Driver");
			// 数据库连接url
			String url = "jdbc:mysql://localhost:3306/tb_user";
			// 获取数据库连接
			conn = DriverManager.getConnection(url, "root", "lxk390140676");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 关闭数据库连接
	 * 
	 * @param conn
	 *            Connection对象
	 */
	public static void closeConnection(Connection conn) {
		// 判断conn是否为空
		if (conn != null) {
			try {
				conn.close(); // 关闭数据库连接
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
