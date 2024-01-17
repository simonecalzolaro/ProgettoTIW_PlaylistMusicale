package it.polimi.tiw.beans;

public class Album {
	private int id;
	private String title;
	private String artist;
	private int releaseYear;
	private String releasePath;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public int getReleaseYear() {
		return releaseYear;
	}
	public void setReleaseYear(int releaseYear) {
		this.releaseYear = releaseYear;
	}
	public String getReleasePath() {
		return releasePath;
	}
	public void setReleasePath(String releasePath) {
		this.releasePath = releasePath;
	}
	

}
