package me.maanuvazquez.adblocker;

import java.sql.*;

public class Database {

	public Connection connection;
	public String dbDirectory;

	public Database(String dbDirectory) {

		this.connection = null;

		try {
			Class.forName("org.sqlite.JDBC");
			this.dbDirectory = dbDirectory;
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

	}

	public void createTable() {

		Statement statement = null;

		try {
			openConnection();
			statement = this.connection.createStatement();
			statement.executeUpdate(
					"CREATE TABLE IF NOT EXISTS HOSTSLIST (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, website TEXT NOT NULL UNIQUE)");
			statement.executeUpdate(
					"CREATE TABLE IF NOT EXISTS WHITELIST (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, domain TEXT NOT NULL UNIQUE)");
			statement.executeUpdate(
					"CREATE TABLE IF NOT EXISTS MYHOSTS (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, domain TEXT NOT NULL UNIQUE)");
			statement.close();
			closeConnection();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public void insert(String table, String record) {

		Statement statement = null;

		try {
			openConnection();
			this.connection.setAutoCommit(false);
			statement = this.connection.createStatement();
			statement.executeUpdate("INSERT INTO " + table + " VALUES (NULL, '" + record + "')");
			this.connection.commit();
			statement.close();
			closeConnection();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public void delete(String table, int id) {

		Statement statement = null;

		try {
			openConnection();
			this.connection.setAutoCommit(false);
			statement = this.connection.createStatement();
			statement.executeUpdate("DELETE FROM " + table + " where id=" + id);
			this.connection.commit();
			statement.close();
			closeConnection();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public void update(String table, int id, String record) {

		Statement statement = null;

		try {
			openConnection();
			this.connection.setAutoCommit(false);
			statement = this.connection.createStatement();
			if (table.equals("HOSTSLIST")) {

				statement.executeUpdate("UPDATE " + table + " set website= '" + record + "' where ID=" + id + ";");
			} else {
				statement.executeUpdate("UPDATE " + table + " set domain= '" + record + "' where ID=" + id + ";");
			}
			this.connection.commit();
			statement.close();
			closeConnection();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public ResultSet getResultSet(String table) {

		Statement statement = null;
		ResultSet resultSet = null;

		try {
			openConnection();
			statement = this.connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM " + table);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return resultSet;

	}

	public int getCount(String table) {
		Statement statement = null;
		ResultSet resultSet = null;
		int count = 0;

		try {
			openConnection();
			statement = this.connection.createStatement();
			resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table + ";");
			count = resultSet.getInt("COUNT(*)");
			resultSet.close();
			closeConnection();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return count;
	}

	public void optimize() {
		Statement statement = null;

		try {
			openConnection();
			statement = this.connection.createStatement();
			statement.executeUpdate("VACUUM FULL");
			closeConnection();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public void openConnection() {

		this.connection = null;

		try {
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbDirectory);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public void closeConnection() {
		try {

			this.connection.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

}
