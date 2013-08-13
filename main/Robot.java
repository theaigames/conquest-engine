package main;

import java.util.ArrayList;

import move.Move;

public interface Robot {
	
	public void setup(long timeOut);
	
	public void writeMove(Move move);
	
	public String getPreferredStartingArmies(long timeOut, ArrayList<Region> pickableRegions);
	
	public String getPlaceArmiesMoves(long timeOut);
	
	public String getAttackTransferMoves(long timeOut);
	
	public void writeInfo(String info);

}
