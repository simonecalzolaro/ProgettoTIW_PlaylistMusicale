package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.playlist.beans.SongDetails;

public class SongDAO {
	private Connection connection;
	
	public SongDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Method that verify if an album is already in the data base schema "album"
	 * @param albumTitle is the title of the album
	 * @param singer is the singer
	 * @param publicationYear is the publication year of the album
	 * @param filename is the name of the image file saved in local
	 * @return the id of the album if that already exists, 0 otherwise
	 * @throws SQLException
	 */
	private int findAlbumId(String albumTitle , String singer , int publicationYear , String filename )throws SQLException {
		String query = "SELECT id FROM Album WHERE title = ? AND imgPath = ? AND artist = ? AND releaseYear = ?";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		int result = 0;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, albumTitle);
			pStatement.setString(2, filename);
			pStatement.setString(3, singer);
			pStatement.setInt(4, publicationYear);
			
			resultSet = pStatement.executeQuery();
			if(resultSet.next())
				result = resultSet.getInt("id");
		}catch(SQLException e) {
			throw e;
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
	
	/**
	 * Method that check if the same album is already in the dataBase and then, eventually, create a new album
	 * @param albumTitle is the name of the album
	 * @param singer is the singer
	 * @param publicationYear is the publication year of the album
	 * @param filename is the name of the stored image
	 * @return the id of the album created or already present
	 * @throws SQLException
	 */
	private int createAlbum(String albumTitle , String singer , int publicationYear , String filename) throws SQLException{
		
		int albumId = 0;
		albumId = findAlbumId(albumTitle , singer , publicationYear , filename);
		//If the album is already in the data base I reuse it
		if(albumId != 0)
			return albumId;
		
		String query = "INSERT INTO Album (title , imgPath , artist , releaseYear) VALUES (? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, albumTitle);
			pStatement.setString(2, filename);
			pStatement.setString(3, singer);
			pStatement.setInt(4, publicationYear);
			
			int code = pStatement.executeUpdate();
			
			if(code > 0)
				return (findAlbumId(albumTitle , singer , publicationYear , filename));
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
	public boolean createSong(int userId , String songTitle , String genre , int albumId , String filename) throws SQLException{
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
	
	/**
	 * Method that take every song in a playList
	 * @param playlistId is the id of the playList the user wants the songs
	 * @return an array list filled for each song in the playList with id,title and image path
	 * @throws SQLException
	 */
	public ArrayList<SongDetails> getSongTitleAndImg(int playlistId) throws SQLException{
		String query = "SELECT * FROM PlaylistSongBinder JOIN Song ON PlaylistSongBinder.SongID = Song.id JOIN Album ON Song.albumID = Album.Id "
				+ "WHERE PlaylistSongBinder.PlaylistID = ? ORDER BY album.releaseYear DESC";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		ArrayList<SongDetails> songs = new ArrayList<SongDetails>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, playlistId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				SongDetails song = new SongDetails();
				
				//Read the image from the data base
				song.setId(resultSet.getInt("song.Id"));
				song.setSongTitle(resultSet.getString("song.title"));
				song.setImgFile(resultSet.getString("album.imgPath"));//Set the name of the image file
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
	public ArrayList<SongDetails> getSongsNotInPlaylist(int playlistId , int userId) throws SQLException{
		String query = "SELECT * FROM Song WHERE creatorID = ? AND id NOT IN ("
				+ "SELECT SongID FROM PlaylistSongBinder WHERE PlaylistID = ?)";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		ArrayList<SongDetails> songs = new ArrayList<SongDetails>();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, userId);
			pStatement.setInt(2, playlistId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				SongDetails song = new SongDetails();
				song.setId(resultSet.getInt("id"));
				song.setSongTitle(resultSet.getString("title"));
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
		String query = "SELECT * FROM Song WHERE Id = ? AND creatorID = ?";
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
	public SongDetails getSongDetails(int songId) throws SQLException{
		String query = "SELECT * FROM Song JOIN Album on song.albumID = Album.Id WHERE Song.id = ?";
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		SongDetails song = new SongDetails();
		
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, songId);
			
			resultSet = pStatement.executeQuery();
			
			if(resultSet.next()) {//theoretically always true -> I've just controlled there is a song
				song.setSongTitle(resultSet.getString("song.title"));
				song.setAlbumTitle(resultSet.getString("album.title"));
				song.setSinger(resultSet.getString("album.artist"));
				song.setKindOf(resultSet.getString("song.genre"));
				song.setPublicationYear(resultSet.getInt("album.releaseYear"));
				song.setFile(resultSet.getString("song.mp3Path"));
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
	 * Method that verify if a song (the image name) belongs to a specific user
	 * @param imageName is the name of the song file
	 * @param userId is the user id
	 * @return true if the song belongs, false otherwise
	 * @throws SQLException
	 */
	public boolean findImageByUser(String imageName , int userId) throws SQLException{
		String query = "SELECT * FROM Song JOIN Album WHERE album.imgPath = ? AND song.creatorID = ?";
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
		String query = "SELECT * FROM Song WHERE mp3Path = ? AND creatorID = ?";
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
	
	
	public ArrayList<SongDetails> getUserSongs(int userId) throws SQLException{
		ArrayList<SongDetails> songs = new ArrayList<>();
		String query = "SELECT * FROM Song WHERE creatorID = ?";
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		int result = -1;
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1,userId);
			
			resultSet = pStatement.executeQuery();
			
			while(resultSet.next()) {
				SongDetails song = new SongDetails();
				song.setSongTitle(resultSet.getString("title"));
				song.setFile(resultSet.getString("mp3Path"));
				song.setId(resultSet.getInt("id"));
				song.setKindOf(resultSet.getString("genre"));
				
				songs.add(song);	
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
		return songs;
	}
	
	
}










