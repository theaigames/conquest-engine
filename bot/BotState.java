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

package bot;

import java.util.ArrayList;

import main.Map;
import main.Region;
import main.SuperRegion;

public class BotState {
	
	private String myName = "";
	private String opponentName = "";
	
	private final Map fullMap = new Map(); //This map is known from the start, contains all the regions and how they are connected, doesn't change after initialization
	private Map visibleMap; //This map represents everything the player can see, updated at the end of each round.
	
	private ArrayList<Region> pickableStartingRegions; //2 randomly chosen regions from each superregion are given, which the bot can chose to start with
	
	private int startingArmies; //number of armies the player can place on map
	
	private int roundNumber;
	
	public BotState()
	{
		pickableStartingRegions = new ArrayList<Region>();
		roundNumber = 0;
	}
	
	public void updateSettings(String key, String value)
	{
		if(key.equals("your_bot")) //bot's own name
			myName = value;
		else if(key.equals("opponent_bot")) //opponent's name
			opponentName = value;
		else if(key.equals("starting_armies")) 
		{
			startingArmies = Integer.parseInt(value);
			roundNumber++; //next round
		}
	}
	
	//initial map is given to the bot with all the information except for player and armies info
	public void setupMap(String[] mapInput)
	{
		int i, regionId, superRegionId, reward;
		
		if(mapInput[1].equals("super_regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					superRegionId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					fullMap.add(new SuperRegion(superRegionId, reward));
				}
				catch(Exception e) {
					System.err.println("Unable to parse SuperRegions");
				}
			}
		}
		else if(mapInput[1].equals("regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					regionId = Integer.parseInt(mapInput[i]);
					i++;
					superRegionId = Integer.parseInt(mapInput[i]);
					SuperRegion superRegion = fullMap.getSuperRegion(superRegionId);
					fullMap.add(new Region(regionId, superRegion));
				}
				catch(Exception e) {
					System.err.println("Unable to parse Regions " + e.getMessage());
				}
			}
		}
		else if(mapInput[1].equals("neighbors"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					Region region = fullMap.getRegion(Integer.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for(int j=0; j<neighborIds.length; j++)
					{
						Region neighbor = fullMap.getRegion(Integer.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				}
				catch(Exception e) {
					System.err.println("Unable to parse Neighbors " + e.getMessage());
				}
			}
		}
	}
	
	//regions from wich a player is able to pick his preferred starting regions
	public void setPickableStartingRegions(String[] mapInput)
	{
		for(int i=2; i<mapInput.length; i++)
		{
			int regionId;
			try {
				regionId = Integer.parseInt(mapInput[i]);
				Region pickableRegion = fullMap.getRegion(regionId);
				pickableStartingRegions.add(pickableRegion);
			}
			catch(Exception e) {
				System.err.println("Unable to parse pickable regions " + e.getMessage());
			}
		}
	}
	
	//visible regions are given to the bot with player and armies info
	public void updateMap(String[] mapInput)
	{
		visibleMap = fullMap.getMapCopy();
		for(int i=1; i<mapInput.length; i++)
		{
			try {
				Region region = visibleMap.getRegion(Integer.parseInt(mapInput[i]));
				String playerName = mapInput[i+1];
				int armies = Integer.parseInt(mapInput[i+2]);
				
				region.setPlayerName(playerName);
				region.setArmies(armies);
				i += 2;
			}
			catch(Exception e) {
				System.err.println("Unable to parse Map Update " + e.getMessage());
			}
		}
		ArrayList<Region> unknownRegions = new ArrayList<Region>();
		
		//remove regions which are unknown.
		for(Region region : visibleMap.regions)
			if(region.getPlayerName().equals("unknown"))
				unknownRegions.add(region);
		for(Region unknownRegion : unknownRegions)
			visibleMap.getRegions().remove(unknownRegion);				
	}
	
	public String getMyPlayerName(){
		return myName;
	}
	
	public String getOpponentPlayerName(){
		return opponentName;
	}
	
	public int getStartingArmies(){
		return startingArmies;
	}
	
	public int getRoundNumber(){
		return roundNumber;
	}
	
	public Map getVisibleMap(){
		return visibleMap;
	}
	
	public Map getFullMap(){
		return fullMap;
	}
	
	public ArrayList<Region> getPickableStartingRegions(){
		return pickableStartingRegions;
	}

}
