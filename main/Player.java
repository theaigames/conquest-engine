package main;

public class Player {
	
	private String name;
	private Robot bot;
	private int armiesPerTurn; 
	private int armiesLeft;    //variable armies that can be added, changes with superRegions fully owned and moves already placed.
	
	public Player(String name, Robot bot, int startingArmies)
	{
		this.name = name;
		this.bot = bot;
		this.armiesPerTurn = startingArmies; //start with 5 armies per turn
	}
	
	/**
	 * @param n Sets the number of armies this player has left to place
	 */
	public void setArmiesLeft(int n) {
		armiesLeft = n;
	}
	
	/**
	 * @return The String name of this Player
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return The Bot object of this Player
	 */
	public Robot getBot() {
		return bot;
	}
	
	/**
	 * @return The standard number of armies this Player gets each turn to place on the map
	 */
	public int getArmiesPerTurn() {
		return armiesPerTurn;
	}
	
	/**
	 * @return The number of armies this Player has left to place on the map
	 */
	public int getArmiesLeft() {
		return armiesLeft;
	}

}
