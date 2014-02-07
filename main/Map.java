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

import java.util.LinkedList;

public class Map {
	
	public LinkedList<Region> regions;
	public LinkedList<SuperRegion> superRegions;
	
	public Map()
	{
		this.regions = new LinkedList<Region>();
		this.superRegions = new LinkedList<SuperRegion>();
	}
	
	public Map(LinkedList<Region> regions, LinkedList<SuperRegion> superRegions)
	{
		this.regions = regions;
		this.superRegions = superRegions;
	}

	/**
	 * add a Region to the map
	 * @param region : Region to be added
	 */
	public void add(Region region)
	{
		for(Region r : regions)
			if(r.getId() == region.getId())
			{
				System.err.println("Region cannot be added: id already exists.");
				return;
			}
		regions.add(region);
	}
	
	/**
	 * add a SuperRegion to the map
	 * @param superRegion : SuperRegion to be added
	 */
	public void add(SuperRegion superRegion)
	{
		for(SuperRegion s : superRegions)
			if(s.getId() == superRegion.getId())
			{
				System.err.println("SuperRegion cannot be added: id already exists.");
				return;
			}
		superRegions.add(superRegion);
	}
	
	/**
	 * @return : a new Map object exactly the same as this one
	 */
	public Map getMapCopy() {
		Map newMap = new Map();
		for(SuperRegion sr : superRegions) //copy superRegions
		{
			SuperRegion newSuperRegion = new SuperRegion(sr.getId(), sr.getArmiesReward());
			newMap.add(newSuperRegion);
		}
		for(Region r : regions) //copy regions
		{
			Region newRegion = new Region(r.getId(), newMap.getSuperRegion(r.getSuperRegion().getId()), r.getPlayerName(), r.getArmies());
			newMap.add(newRegion);
		}
		for(Region r : regions) //add neighbors to copied regions
		{
			Region newRegion = newMap.getRegion(r.getId());
			for(Region neighbor : r.getNeighbors())
				newRegion.addNeighbor(newMap.getRegion(neighbor.getId()));
		}
		return newMap;
	}
	
	/**
	 * @return : the list of all Regions in this map
	 */
	public LinkedList<Region> getRegions() {
		return regions;
	}
	
	/**
	 * @return : the list of all SuperRegions in this map
	 */
	public LinkedList<SuperRegion> getSuperRegions() {
		return superRegions;
	}
	
	/**
	 * @param id : a Region id number
	 * @return : the matching Region object
	 */
	public Region getRegion(int id)
	{
		for(Region region : regions)
			if(region.getId() == id)
				return region;
		System.err.println("Could not find region with id " + id);
		return null;
	}
	
	/**
	 * @param id : a SuperRegion id number
	 * @return : the matching SuperRegion object
	 */
	public SuperRegion getSuperRegion(int id)
	{
		for(SuperRegion superRegion : superRegions)
			if(superRegion.getId() == id)
				return superRegion;
		System.err.println("Could not find superRegion with id " + id);
		return null;
	}
	
	public String getMapString()
	{
		String mapString = "";
		for(Region region : regions)
		{
			mapString = mapString.concat(region.getId() + ";" + region.getPlayerName() + ";" + region.getArmies() + " ");
		}
		return mapString;
	}
	
	//return all regions owned by given player
	public LinkedList<Region> ownedRegionsByPlayer(Player player)
	{
		LinkedList<Region> ownedRegions = new LinkedList<Region>();
		
		for(Region region : this.getRegions())
			if(region.getPlayerName().equals(player.getName()))
				ownedRegions.add(region);

		return ownedRegions;
	}
	
	//fog of war
	//return all regions visible to given player
	public LinkedList<Region> visibleRegionsForPlayer(Player player)
	{
		LinkedList<Region> visibleRegions = new LinkedList<Region>();
		LinkedList<Region> ownedRegions = ownedRegionsByPlayer(player);
		
		visibleRegions.addAll(ownedRegions);
		
		for(Region region : ownedRegions)	
			for(Region neighbor : region.getNeighbors())
				if(!visibleRegions.contains(neighbor))
					visibleRegions.add(neighbor);

		return visibleRegions;
	}
	
	public Map getVisibleMapCopyForPlayer(Player player) {
		Map visibleMap = getMapCopy();
		LinkedList<Region> visibleRegions = visibleRegionsForPlayer(player);
		
		for(Region region : regions)
		{
			if(!visibleRegions.contains(region)){
				Region unknownRegion = visibleMap.getRegion(region.getId());
				unknownRegion.setPlayerName("unknown");
				unknownRegion.setArmies(0);
			}
		}
		
		return visibleMap;		
	}
	
}
