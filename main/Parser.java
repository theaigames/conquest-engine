package main;

import java.util.ArrayList;

import move.AttackTransferMove;
import move.Move;
import move.PlaceArmiesMove;

public class Parser {
	
	private Player player1;
	private Player player2;
	private Map map;
	
	public Parser(Player player1, Player player2, Map map)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.map = map;
	}
	
	public ArrayList<Move> parseMoves(String input)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		input = input.trim();
		if(input.length() <= 1)
			return moves;
		
		String[] split = input.split(",");
		
		for(int i=0; i<split.length; i++)
		{
			try {
				moves.add(parseMove(split[i]));
			}
			catch(Exception e) {
				System.out.println("Move input string incorrect.");
			}
		}
		return moves;
	}
	
	//misschien nog veranderen als Move weg gaat.
	//returns the correct Move. Null if input is incorrect.
	private Move parseMove(String input)
	{
		Player player = null;
		int armies = -1;
		
		String[] split = input.trim().split(" ");

		if(split[0].equals(player1.getName()))
			player = player1;
		else if(split[0].equals(player2.getName()))
			player = player2;
		else
			System.out.println("Wrong input on move: player does not exist");
		
		if(split[1].equals("place_armies"))		
		{
			Region region = null;

			region = parseRegion(split[2]);

			try { armies = Integer.parseInt(split[3]); }
			catch(Exception e) { System.out.println("Number of armies input incorrect.");}
		
			if(!(player == null || region == null || armies == -1))
				return new PlaceArmiesMove(player.getName(), region, armies);
			return null;
		}
		else if(split[1].equals("attack/transfer"))
		{
			Region fromRegion = null;
			Region toRegion = null;
			
			fromRegion = parseRegion(split[2]);
			toRegion = parseRegion(split[3]);
			
			try { armies = Integer.parseInt(split[4]); }
			catch(Exception e) { System.out.println("Number of armies input incorrect.");}

			if(!(player == null || fromRegion == null || toRegion == null || armies == -1))
				return new AttackTransferMove(player.getName(), fromRegion, toRegion, armies);
			return null;
		}
		else
		{
			System.out.println("Movetype incorrect.");
			return null;
		}
	}
	
	//parse the region given the id string.
	private Region parseRegion(String regionId)
	{
		int id = -1;
		Region region;
		
		try { id = Integer.parseInt(regionId); }
		catch(Exception e) { System.out.println("Region id input incorrect."); return null;}
		
		region = map.getRegion(id);
		
		return region;
	}
	
	public ArrayList<Region> parsePreferredStartingRegions(String input, ArrayList<Region> pickableRegions)
	{
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();
		int nrOfPreferredStartingRegions = 6;
		String[] split = input.split(" ");
		
		for(int i=0; i<nrOfPreferredStartingRegions; i++)
		{
			try {
				Region r = parseRegion(split[i]);
				
				if(pickableRegions.contains(r))
				{
					if(!preferredStartingRegions.contains(r))
						preferredStartingRegions.add(r);
					else
					{
						System.out.println("Error on preferred starting regions: Same region appears more than once");
						return null;
					}
				}
				else
				{
					System.out.println("Chosen region is not in the given pickable regions list");
					return null;
				}
			}
			catch(Exception e) { //player has not returned enough preferred regions
				System.out.println("Error on preferred starting regions: Player did not return enough preferred starting regions");
				return null;
			}
		}
		
		return preferredStartingRegions;
	}

}
