// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package main;

import java.util.ArrayList;

import move.AttackTransferMove;
import move.Move;
import move.PlaceArmiesMove;

public class Parser {
	
	private Map map;
	
	public Parser(Map map)
	{
		this.map = map;
	}
	
	public ArrayList<Move> parseMoves(String input, Player player)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		try {
			input = input.trim();
			if(input.length() <= 1)
				return moves;
			
			String[] split = input.split(",");
			
			for(int i=0; i<split.length; i++)
			{
				Move move = parseMove(split[i], player);
				if(move != null)
					moves.add(move);
			}
		}
		catch(Exception e) {
			player.getBot().addToDump("Move input is null");
		}
		return moves;
	}

	//returns the correct Move. Null if input is incorrect.
	private Move parseMove(String input, Player player)
	{
		int armies = -1;
		
		String[] split = input.trim().split(" ");

		if(!split[0].equals(player.getName()))
		{
			errorOut("Incorrect player name or move format incorrect", input, player);
			return null;
		}	
		
		if(split[1].equals("place_armies"))		
		{
			Region region = null;

			region = parseRegion(split[2], input, player);

			try { armies = Integer.parseInt(split[3]); }
			catch(Exception e) { errorOut("Number of armies input incorrect", input, player);}
		
			if(!(region == null || armies == -1))
				return new PlaceArmiesMove(player.getName(), region, armies);
			return null;
		}
		else if(split[1].equals("attack/transfer"))
		{
			Region fromRegion = null;
			Region toRegion = null;
			
			fromRegion = parseRegion(split[2], input, player);
			toRegion = parseRegion(split[3], input, player);
			
			try { armies = Integer.parseInt(split[4]); }
			catch(Exception e) { errorOut("Number of armies input incorrect", input, player);}

			if(!(fromRegion == null || toRegion == null || armies == -1))
				return new AttackTransferMove(player.getName(), fromRegion, toRegion, armies);
			return null;
		}

		errorOut("Bot's move format incorrect", input, player);
		return null;
	}
	
	//parse the region given the id string.
	private Region parseRegion(String regionId, String input, Player player)
	{
		int id = -1;
		Region region;
		
		try { id = Integer.parseInt(regionId); }
		catch(Exception e) { errorOut("Region id input incorrect", input, player); return null;}
		
		region = map.getRegion(id);
		
		return region;
	}
	
	public ArrayList<Region> parsePreferredStartingRegions(String input, ArrayList<Region> pickableRegions, Player player)
	{
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();

		try {
			int nrOfPreferredStartingRegions = 6;
			String[] split = input.split(" ");
			
			for(int i=0; i<nrOfPreferredStartingRegions; i++)
			{
				try {
					Region r = parseRegion(split[i], input, player);
					
					if(pickableRegions.contains(r))
					{
						if(!preferredStartingRegions.contains(r))
							preferredStartingRegions.add(r);
						else
						{
							errorOut("preferred starting regions: Same region appears more than once", input, player);
							return null;
						}
					}
					else
					{
						errorOut("preferred starting regions: Chosen region is not in the given pickable regions list", input, player);
						return null;
					}
				}
				catch(Exception e) { //player has not returned enough preferred regions
					errorOut("preferred starting regions: Player did not return enough preferred starting regions", input, player);
					return null;
				}
			}
			return preferredStartingRegions;
		}
		catch(Exception e) {
			player.getBot().addToDump("Preferred starting regions input is null");
			return null;
		}
	}

	private void errorOut(String error, String input, Player player)
	{
		player.getBot().addToDump("Parse error: " + error + " (" + input + ")");
	}

}
