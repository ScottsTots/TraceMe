package helperClasses;

import java.util.ArrayList;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * An extension of ParseObject that makes
 * it more convenient to access information
 * about a given GameSession between two players
 * 
 * 
 * A Game Session contains
 *  -player1 user
 *  -player2 user
 *  -player1 paths to draw
 *  -player2 paths to draw
 *  -
 *  
 *  -image data for the most recent turn
 *  -Total number of images drawn for this game session
 * 
 */

@ParseClassName("GameSession")
public class ParseGame extends ParseObject {

	public ParseGame() {
		// A default constructor is required.
	}
	
	public ParseUser getAuthor() {
		return getParseUser("author");
	}

	public void setAuthor(ParseUser user) {
		put("author", user);
	}

	public String getPlayerOne() {
		return getString("playerOne");
	}

	public void setPlayerOne(String username) {
		put("playerOne", username);
	}

	public String getPlayerTwo() {
		return getString("playerTwo");
	}

	public void setPlayerTwo(String username) {
		put("playerTwo", username);
	}

	// Game status is either open or closed
	public void setGameStatus(String status) {
		put("gameStatus", status);
	}

	public String getGameStatus() {
		return getString("gameStatus");
	}
	
	// Keep track of which player has to move.
	public void setPlayerTurn(String user) {
		put("playerTurn", user);
	}

	public String getPlayerTurn() {
		return getString("playerTurn");
	}
	
	
	

	public void setTotalNumberOfFrames(int num) {
		put("totalNumberOfFrames", num);
	}

	public int getTotalNumberOfFrames() {
		return getInt("totalNumberOfFrames");
	}

	public String getRating() {
		return getString("rating");
	}

	public void setRating(String rating) {
		put("rating", rating);
	}

	public int getFramesPerTurn() {
		return getInt("framesPerTurn");
	}

	public void setFramesPerTurn(int num) {
		put("framesPerTurn", num);
	}

	public int getNumTurns() {
		return getInt("numTurns");
	}

	public void setNumTurns(int num) {
		put("numTurns", num);
	}
	
	
	public int getTotalImagesSaved() {
		return getInt("numTotalImages");
	}

	public void setTotalImagesSaved(int num) {
		put("numTotalImages", num);
	}
	
	// Indicates whether the game has ended or not.
	public String getGameEnd()
	{
	    return getString("gameEnd");
	}
	
	public void setGameEnd(String end)
	{
	    put("gameEnd", end);
	}

	// gets image files for the last turn. This does not include all the image files for
	// the whole animation. Downloads from [indexStart, indexEnd)
	public ArrayList<ParseFile> getTurnImageFiles(int indexStart, int indexEnd) {
		ArrayList<ParseFile> results = new ArrayList<ParseFile>();
		for (int i = indexStart; i < indexEnd; i++) {
			results.add(getParseFile("photo" + Integer.toString(i)));
		}
		return results;
	}

	// put images in the cloud start naming from the given index.
	// If index is 3, first image uploaded will be named "photo3"
	public void setTurnImageFiles(int index, ArrayList<ParseFile> fileList) {
		for (int i = 0; i < fileList.size(); i++)
			put("photo" + Integer.toString(i + index), fileList.get(i));
	}

	// gets however many image files you want starting from [photo0, photonumber)
	public ArrayList<ParseFile> getImageFiles(int number) {
		ArrayList<ParseFile> results = new ArrayList<ParseFile>();
		for (int i = 0; i < number; i++) {
			results.add(getParseFile("photo" + Integer.toString(i)));
		}
		return results;
	}

	// put images in the cloud. Start naming from 0. photo0, photo1, etc...
	public void setImageFiles(ArrayList<ParseFile> fileList) {
		for (int i = 0; i < fileList.size(); i++)
			put("photo" + Integer.toString(i), fileList.get(i));
	}

	public static ParseQuery<ParseGame> getQuery() {
		return ParseQuery.getQuery(ParseGame.class);
	}

}
