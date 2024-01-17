package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.beans.Song;

public class SongDAO {
	private Connection connection;
	
	public SongDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	
	/**
	 * Method that create the album and the song doing the commit if everything went okay, rollBack otherwise
	 * @param userId is the id of the user who is updating the DB
	 * @param songTitle is the title of the song
	 * @param genre is the genre of the song
	 * @param albumTitle is the title of the album
	 * @param singer is the singer
	 * @param publicationYear is the publication year of the album
	 * @param imgName is the name of the stored image
	 * @param songName is the name of the stored song
	 * @return true if the method created correctly both album and song , false otherwise
	 * @throws SQLException
	 */
	public boolean createSongAndAlbum(int userId , String songTitle , String genre , String albumTitle , String singer , int publicationYear , String imgName , String songName) 
			throws SQLException{
		
		boolean result = false;
		try {
			connection.setAutoCommit(false);
			
			int albumId = createAlbum(albumTitle , singer , publicationYear , imgName);
			result = createSong_private(userId , songTitle , genre , albumId , songName);
			
			connection.commit();
		}catch(SQLException e){
			connection.rollback();
			throw e;
		}finally {
			connection.setAutoCommit(true);
		}
		return result;
	}
	
	/**
	 * Method that check if the same album is already in the dataBase and then, eventually, create a new album
	 * @param albumTitle is the name of the album
	 * @param artist is the singer
	 * @param publicationYear is the publication year of the album
	 * @param filename is the name of the stored image
	 * @return the id of the album created or already present
	 * @throws SQLException
	 */
	private int createAlbum(String albumTitle , String artist , int publicationYear , String filename) throws SQLException{
		
		int albumId = 0;
		//If the album is already in the data base I reuse it
		if(albumId != 0)
			return albumId;
		
		String query = "INSERT INTO Album (title , imgPath , artist , releaseYear) VALUES (? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		String queryID="";
		PreparedStatement pStatementID = null;
		ResultSet resultSet = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, albumTitle);
			pStatement.setString(2, filename);
			pStatement.setString(3, artist);
			pStatement.setInt(4, publicationYear);
			
			int code = pStatement.executeUpdate();
			
			
			if(code > 0)
				{
				queryID ="SELECT * FROM Album WHERE title=? AND imgPath=? AND artist=? AND releaseYear=?";
				
				pStatementID = connection.prepareStatement(queryID);
				pStatementID.setString(1, albumTitle);
				pStatementID.setString(2, filename);
				pStatementID.setString(3, artist);
				pStatementID.setInt(4, publicationYear);
				
				resultSet = pStatementID.executeQuery();
				
				while(resultSet.next()) {
					albumId = resultSet.getInt("id");
				}
				
				return albumId;
				}
			
			
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return albumId;
	}
	
	
	/*
	 * get the album id if exists, -1 if not exists
	 */
	
	public int getAlbum(String title, String artist) throws SQLException{
		String query = "SELECT * FROM Album WHERE title = ? AND artist = ?";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		int result = -1;
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1,title);
			pStatement.setString(2, artist);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				result = resultSet.getInt("id");
			}
			
		}catch(SQLException e) {
			throw e;
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
	 * Method that create the song in the dataBase
	 * @param userId is the user id
	 * @param songTitle is the title of the song
	 * @param genre is the genre
	 * @param albumId is the id of the album where this song is
	 * @param filename is the name of the stored song
	 * @return true if the update of the DB went good , false otherwise
	 * @throws SQLException
	 */
	private boolean createSong_private(int userId , String songTitle , String genre , int albumId , String filename) throws SQLException{
		String query = "INSERT INTO Song (creatorID , genre , mp3Path , title , albumID) VALUES (? , ? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		int code = 0;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setString(2, genre);
			pStatement.setString(3, filename);
			pStatement.setString(4, songTitle);
			pStatement.setInt(5, albumId);
			
			code = pStatement.executeUpdate();
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return (code > 0); 
	}
	
	
	
	public boolean createSong(int userId , String songTitle , String genre , int albumId , String filename) throws SQLException{
		String query = "INSERT INTO Song (creatorID , genre , mp3Path , title , albumID) VALUES (? , ? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		int code = 0;
		
		try {
			connection.setAutoCommit(false);
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setString(2, genre);
			pStatement.setString(3, filename);
			pStatement.setString(4, songTitle);
			pStatement.setInt(5, albumId);
			
			code = pStatement.executeUpdate();
			
			connection.commit();
		}catch(SQLException e) {
			connection.rollback();
			throw e;
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
	
	/**
	 * Method that takes every song in a playList
	 * @param playlistId is the id of the playList the user wants the songs
	 * @return an array list filled for each song in the playList with id,title and image path
	 * @throws SQLException
	 */
	public ArrayList<Song> getSongTitleAndImg(int playlistId) throws SQLException{
		String query = "SELECT * FROM PlaylistSongBinder JOIN Song ON PlaylistSongBinder.SongID = song.id JOIN Album ON song.albumID = album.id "
				+ "WHERE PlaylistSongBinder.PlaylistID = ? ORDER BY Album.releaseYear DESC";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		ArrayList<Song> songs = new ArrayList<Song>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, playlistId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				Song song = new Song();
				
				//Read the image from the data base
				song.setId(resultSet.getInt("song.id"));
				song.setTitle(resultSet.getString("song.title"));
				song.setMp3Path(resultSet.getString("song.mp3Path"));
				song.setImgPath(resultSet.getString("album.imgPath"));//Set the name of the image file
				songs.add(song);
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
		return songs;
	}
	
	/**
	 * Method that take all the songs added by the user but not in the playList specified by playlistId
	 * @param playlistId is the id of the playList  
	 * @param userId is the id of the user
	 * @return an array list of SongDetails containing all the songs not in the playList
	 * @throws SQLException
	 */
	public ArrayList<Song> getSongsNotInPlaylist(int playlistId , int userId) throws SQLException{
		String query = "SELECT * FROM Song WHERE creatorId = ? AND id NOT IN ("
				+ "SELECT SongID FROM PlaylistSongBinder WHERE PlaylistID = ?)";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		ArrayList<Song> songs = new ArrayList<Song>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setInt(2, playlistId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				Song song = new Song();
				song.setId(resultSet.getInt("id"));
				song.setTitle(resultSet.getString("title"));
				songs.add(song);
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
		return songs;
	}
	
	/**
	 * Method that verify if a song belongs to a specific user
	 * @param sId is the song id
	 * @param userId is the user id
	 * @return true if the song belongs, false otherwise
	 * @throws SQLException
	 */
	public boolean findSongByUser(int sId , int userId) throws SQLException{
		String query = "SELECT * FROM Song WHERE id = ? AND creatorID = ?";
		boolean result = false;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, sId);
			pStatement.setInt(2, userId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next())
				result = true;
			
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
	 * Method that take from the data base details of a specific song
	 * @param songId is the song id the user wants the details
	 * @return a SongDetails object
	 * @throws SQLException
	 */
	public Song getSongDetails(int songId) throws SQLException{
		String query = "SELECT * FROM Song JOIN Album on song.albumID = album.id WHERE song.id = ?";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		Song song = new Song();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, songId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) {
				song.setTitle(resultSet.getString("song.title"));
				song.setAlbumTitle(resultSet.getString("album.title"));
				song.setArtist(resultSet.getString("album.artist"));
				song.setGenre(resultSet.getString("song.genre"));
			    song.setReleaseYear(resultSet.getInt("album.releaseYear"));
				song.setMp3Path(resultSet.getString("song.mp3Path"));
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
		return song;
	}

	/**
	 * Method that verify if an image (the image name) belongs to a specific user
	 * @param imageName is the name of the song file
	 * @param userId is the user id
	 * @return true if the song belongs, false otherwise
	 * @throws SQLException
	 */
	public boolean findSongByUser(String imageName , int userId) throws SQLException{
		String query = "SELECT * FROM song JOIN album WHERE album.Image = ? AND song.IdUser = ?";
		boolean result = false;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, imageName);
			pStatement.setInt(2, userId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next())
				result = true;
			
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
	 * Method that verify if a song (the song name) belongs to the user
	 * @param songName is the name of the stored file
	 * @param userId is the id of the user
	 * @return true if there is the song , false otherwise
	 * @throws SQLException
	 */
	public boolean findSongByUserId(String songName , int userId) throws SQLException{
		String query = "SELECT * FROM song WHERE MusicFile = ? AND IdUser = ?";
		boolean result = false;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, songName);
			pStatement.setInt(2, userId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next())
				result = true;
			
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
	



/*
 * Return the song for the user having id =userId
 */


public ArrayList<Song> getSongsByUser(int userID) throws SQLException{ 
	
	String query = "SELECT * FROM Song WHERE creatorID = ?";
	ResultSet resultSet = null;
	PreparedStatement pStatement = null;
	ArrayList<Song> songs = new ArrayList<Song>();
	
	try {
		
		pStatement = connection.prepareStatement(query);
		pStatement.setInt(1, userID);
		
		resultSet = pStatement.executeQuery();
		
		//debug
		int cont = 0;
		
		while(resultSet.next()) {
			Song song = new Song();
			
			
			
			song.setAlbumId(resultSet.getInt("albumID"));
			song.setCreatorId(resultSet.getInt("creatorID"));
			song.setGenre(resultSet.getString("genre"));
			song.setId(resultSet.getInt("id"));
			song.setTitle(resultSet.getString("title"));
			song.setMp3Path(resultSet.getString("mp3Path"));
			songs.add(song);
			
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
	
	return songs;
}

}

