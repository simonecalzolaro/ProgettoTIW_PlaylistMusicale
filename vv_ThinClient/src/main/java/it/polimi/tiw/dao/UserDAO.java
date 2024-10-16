package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Method that search in the DB is the user exist or if the userName is already used by an other user
	 * @param userName is the userName I have to verify if it's already in the DB
	 * @return true if it exists, false otherwise
	 * @throws SQLException
	 */
	public boolean existsUser(String username) throws SQLException{
		boolean result = false;
		String query = "SELECT username FROM User WHERE username = ?";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1 , username);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) result = true;
		}catch(SQLException e) {
			throw new SQLException(e);
		}finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
	
	/**
	 * Method that verify if userName and Password, inserted during the login, are correct
	 * @param userName 
	 * @param password
	 * @return true is password and userName are right, false if userName doesn't exist or password is wrong
	 * @throws SQLException
	 */
	public User checkAuthentication(String username, String password) throws SQLException{
		String query ="SELECT * FROM User WHERE username = ? AND password = ?";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		
		try{			
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1 , username);
			pStatement.setString(2 , password);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) {
				
				User newUser = new User();
				newUser.setId(resultSet.getInt("id"));
				newUser.setPassword(resultSet.getString("password"));
				newUser.setUsername(resultSet.getString("username"));
				
				return newUser;
			}	
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return null;
	}

	/**
	 * Method that add a new user account into the data base
	 * @param userName
	 * @param password
	 * @return true if the account was added correctly, false otherwise
	 * @throws SQLException
	 */
	public boolean addUser(String username ,String password) throws SQLException{
		int code = 0;
		
		if(existsUser(username) == true)
			return false;
		
		String query = "INSERT into User (username,password) VALUES(?,?)";
		PreparedStatement pStatement = null;
		
		connection.setAutoCommit(false);
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1 , username);
			pStatement.setString(2 , password);
			
			code = pStatement.executeUpdate();//code is the number of updated row in the DB
			
			connection.commit();
		}catch(SQLException e) {
			connection.rollback();
			throw new SQLException(e);
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		connection.setAutoCommit(true);
		return (code > 0);		
	}
	
}
