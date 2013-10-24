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
		
		try {
			input = input.trim();
			if(input.length() <= 1)
				return moves;
			
			String[] split = input.split(",");
			
			for(int i=0; i<split.length; i++)
			{
				Move move = parseMove(split[i]);
				if(move != null)
					moves.add(move);
			}
		}
		catch(Exception e) {
			System.err.println("Move input is null");
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
		{
			errorOut("Player does not exist or move format incorrect", input);
			return null;
		}
			
		
		if(split[1].equals("place_armies"))		
		{
			Region region = null;

			region = parseRegion(split[2], input);

			try { armies = Integer.parseInt(split[3]); }
			catch(Exception e) { errorOut("Number of armies input incorrect", input);}
		
			if(!(player == null || region == null || armies == -1))
				return new PlaceArmiesMove(player.getName(), region, armies);
			return null;
		}
		else if(split[1].equals("attack/transfer"))
		{
			Region fromRegion = null;
			Region toRegion = null;
			
			fromRegion = parseRegion(split[2], input);
			toRegion = parseRegion(split[3], input);
			
			try { armies = Integer.parseInt(split[4]); }
			catch(Exception e) { errorOut("Number of armies input incorrect", input);}

			if(!(player == null || fromRegion == null || toRegion == null || armies == -1))
				return new AttackTransferMove(player.getName(), fromRegion, toRegion, armies);
			return null;
		}

		errorOut("Bot's move format incorrect", input);
		return null;
	}
	
	//parse the region given the id string.
	private Region parseRegion(String regionId, String input)
	{
		int id = -1;
		Region region;
		
		try { id = Integer.parseInt(regionId); }
		catch(Exception e) { errorOut("Region id input incorrect", input); return null;}
		
		region = map.getRegion(id);
		
		return region;
	}
	
	public ArrayList<Region> parsePreferredStartingRegions(String input, ArrayList<Region> pickableRegions)
	{
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();

		try {
			int nrOfPreferredStartingRegions = 6;
			String[] split = input.split(" ");
			
			for(int i=0; i<nrOfPreferredStartingRegions; i++)
			{
				try {
					Region r = parseRegion(split[i], input);
					
					if(pickableRegions.contains(r))
					{
						if(!preferredStartingRegions.contains(r))
							preferredStartingRegions.add(r);
						else
						{
							errorOut("preferred starting regions: Same region appears more than once", input);
							return null;
						}
					}
					else
					{
						errorOut("preferred starting regions: Chosen region is not in the given pickable regions list", input);
						return null;
					}
				}
				catch(Exception e) { //player has not returned enough preferred regions
					errorOut("preferred starting regions: Player did not return enough preferred starting regions", input);
					return null;
				}
			}
			return preferredStartingRegions;
		}
		catch(Exception e) {
			System.err.println("Preferred starting regions input is null");
			return null;
		}
	}

	private void errorOut(String error, String input)
	{
		System.err.println("Parse error: " + error + " (" + input + ")");
	}

}
