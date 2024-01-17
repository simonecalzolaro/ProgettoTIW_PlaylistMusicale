package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.beans.Playlist;

public class PlaylistDAO {
	private Connection connection;
	
	public PlaylistDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Method that create a list of playList of the user
	 * @param userId is the id of the user
	 * @return an ArrayList of playList created by the user
	 * @throws SQLException 
	 */
	public ArrayList<Playlist> findUserPlaylists(int userId) throws SQLException{
		String query = "SELECT * FROM Playlist WHERE creatorID = ? ORDER BY creationDate DESC";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		ArrayList<Playlist> playlists = new ArrayList<Playlist>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1 , userId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				Playlist playlist = new Playlist();
				playlist.setTitle(resultSet.getString("title"));
				playlist.setId(resultSet.getInt("id"));
				playlist.setCreationDate(resultSet.getDate("creationDate"));
				playlist.setCreatorID(resultSet.getInt("creatorID"));
				playlists.add(playlist);
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
		return playlists;
	}
	
	/**
	 * Method that verify if a title is already in the data base 
	 * @param title is the title to search
	 * @return true if the title is already present, false otherwise
	 * @throws SQLException
	 */
	public boolean findPlaylistByTitle(String title , int userId) throws SQLException{
		String query = "SELECT * FROM Playlist WHERE title = ? AND creatorID = ?";
		boolean result = false;
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, title);
			pStatement.setInt(2, userId);
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) result = true;
			
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
		return result;
	}
	
	/**
	 * Method that create a new playList with an unique title for the user
	 * @param title 
	 * @param creationDate
	 * @return true if the playList was created correctly, false otherwise
	 * @throws SQLException
	 */
//	
	public boolean createPlaylist(String title , Date creationDate , int userId, ArrayList<Integer> songs) throws SQLException{
		
		String query = "INSERT INTO Playlist (creatorID , title , creationDate) VALUES (? , ? , ?)";
		int code = 0;
		PreparedStatement pStatement = null;
	
		
		if(findPlaylistByTitle(title , userId) == true)
			
			return false;
		
		
		connection.setAutoCommit(false);
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setString(2 , title);
			pStatement.setDate(3 , creationDate);
			code = pStatement.executeUpdate();
			
		
			int pId = getPlaylistId(title, userId);
			
			for(Integer s: songs) {
				addSongBefore(pId, s);
			}
			

			
			connection.commit();
		}catch(SQLException e) {
			connection.rollback();
			throw new SQLException();
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		
		System.out.println("Step2");

		connection.setAutoCommit(true);

		return (code > 0);
	}
	
	
	/**
	 * Method that add a song to a playList in the "contains" schema  
	 * @param pId is the playList id
	 * @param sId is the song id
	 * @return true if the update went well, false otherwise
	 * @throws SQLException
	 */
	public boolean addSongBefore(int pId , int sId) throws SQLException{
		String query = "INSERT INTO PlaylistSongBinder (SongId , PlaylistId) VALUES (? , ?)";
		int code = 0;
		PreparedStatement pStatement = null;
		
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, sId);
			pStatement.setInt(2, pId);
			
			code = pStatement.executeUpdate();
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}

		return (code > 0);
	}
	
	
	/*
	 * return the playlist id
	 */
	
	public int getPlaylistId(String title, int userId) throws SQLException {
		
		String query = "SELECT * FROM Playlist WHERE title=? AND creatorID = ?";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		int result=-1;
		
		try {
			
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1,title);
			pStatement.setInt(2, userId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) {
				result = resultSet.getInt("id");
			}
			
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		
		return result;
		
		
		
	}
	
	/**
	 * Method that find if a playList and a user are connected
	 * @param playlistId is the id of the playList
	 * @param userId is the id of the user
	 * @return true if the user created this playList, false otherwise
	 * @throws SQLException
	 */
	public boolean findPlayListById(int playlistId , int userId) throws SQLException{
		String query = "SELECT * FROM Playlist WHERE id = ? AND creatorId = ?";
		boolean result = false;
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1 , playlistId);
			pStatement.setInt(2 , userId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) {
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally{
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
	 * Method that find the title of a playList by its id
	 * @param playlistId is the unique id of the playList
	 * @return a String containing the title
	 * @throws SQLException
	 */
	public String findPlayListTitleById(int playlistId) throws SQLException{
		String query = "SELECT * FROM Playlist WHERE id = ?";
		String result = "";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1 , playlistId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) {
				result = resultSet.getString("Title");
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally{
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
	 * Method that add a song to a playList in the "contains" schema  
	 * @param pId is the playList id
	 * @param sId is the song id
	 * @return true if the update went well, false otherwise
	 * @throws SQLException
	 */
	public boolean addSong(int pId , int sId) throws SQLException{
		String query = "INSERT INTO PlaylistSongBinder (SongId , PlaylistId) VALUES (? , ?)";
		int code = 0;
		PreparedStatement pStatement = null;
		
		connection.setAutoCommit(false);
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, sId);
			pStatement.setInt(2, pId);
			
			code = pStatement.executeUpdate();
			connection.commit();
		}catch(SQLException e) {
			connection.rollback();
			throw new SQLException();
		}finally {
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
		connection.setAutoCommit(true);

		return (code > 0);
	}
	
	/**
	 * Method that checks if a song is already in the playList
	 * @param pId is the playList id 
	 * @param sId is the song id
	 * @return true if the song is already present , false otherwise
	 * @throws SQLException
	 */
	public boolean findSongInPlaylist(int pId , int sId) throws SQLException {
		String query = "SELECT * FROM PlaylistSongBinder WHERE SongId = ? AND PlaylistId = ?";
		boolean result = false;
		
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, sId);
			pStatement.setInt(2, pId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next())
				result = true;
		}catch(SQLException e) {
			throw new SQLException();
		}finally{
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
	
}
