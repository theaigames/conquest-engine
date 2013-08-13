package bot;

import java.util.ArrayList;
import java.util.LinkedList;

import main.Region;
import main.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarterJim implements Bot 
{
	@Override
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut)
	{
		int m = 6;
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();
		for(int i=0; i<m; i++)
		{
			double rand = Math.random();
			int r = (int) (rand*state.getPickableStartingRegions().size());
			int regionId = state.getPickableStartingRegions().get(r).getId();
			Region region = state.getFullMap().getRegion(regionId);

			if(!preferredStartingRegions.contains(region))
				preferredStartingRegions.add(region);
			else
				i--;
		}
		
		return preferredStartingRegions;
	}

	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on all regions
	 * untill he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{
		
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armiesLeft = state.getStartingArmies();
		int armies = (int) (armiesLeft / 2.5);
		
		ArrayList<Region> myRegionsList = getMyRegions(state);
		
		//order SuperRegions by importance
		ArrayList<SuperRegion> orderedSuperRegions = new ArrayList<SuperRegion>();
		for(int i=0; i<6; i++)
			orderedSuperRegions.add(null);
		for(SuperRegion superRegion : state.getVisibleMap().getSuperRegions())
		{	
			switch(superRegion.getId()) {
				case 1: //north america
					orderedSuperRegions.add(2, superRegion);
					orderedSuperRegions.remove(3);
					break;
				case 2: //south america
					orderedSuperRegions.add(0, superRegion);
					orderedSuperRegions.remove(1);
					break;
				case 3: //europe
					orderedSuperRegions.add(4, superRegion);
					orderedSuperRegions.remove(5);
					break;
				case 4: //afrika
					orderedSuperRegions.add(3, superRegion);
					orderedSuperRegions.remove(4);
					break;
				case 5: //azia
					orderedSuperRegions.add(5, superRegion);
					orderedSuperRegions.remove(6);
					break;
				case 6: //australia
					orderedSuperRegions.add(1, superRegion);
					orderedSuperRegions.remove(2);
					break;	
			}
		}
		
		//if the threat of an enemy taking over an owned Super Region is really big: put all armies on defending region
		for(SuperRegion superRegion : orderedSuperRegions)
		{
			String ownedByPlayer = superRegion.ownedByPlayer();
			if(ownedByPlayer != null && ownedByPlayer.equals(state.getMyPlayerName()))
			{
				for(Region subRegion : superRegion.getSubRegions())
				{
					if(isBorderRegion(subRegion) && this.hasEnemyNeighbor(subRegion, state.getMyPlayerName()))
					{
						for(Region neighbor : subRegion.getNeighbors())
						{
							if(subRegion.getArmies()*1.2 < neighbor.getArmies())
							{
								placeArmiesMoves.add(new PlaceArmiesMove(myName, subRegion, armiesLeft));
								subRegion.setArmies(subRegion.getArmies() + armies);
								return placeArmiesMoves;
							}
						}
					}
				}
			}
		}
			
		//if region is in takable superRegion and has an enemy neighbor and has the most armies: put all armies on this region
		for(SuperRegion superRegion : orderedSuperRegions)
		{
			LinkedList<Region> goodRegions = new LinkedList<Region>();
			for(Region subRegion : superRegion.getSubRegions())
			{
				if(subRegion.getPlayerName().equals(state.getMyPlayerName()) && 
						isInTakableSuperRegion(subRegion, superRegion, state) && 
							hasEnemyNeighborInSuperRegion(subRegion, superRegion, state.getMyPlayerName()))
					goodRegions.add(subRegion);
			}
			for(Region region : goodRegions)
			{
				if(this.hasMostArmiesInRegions(region, goodRegions, state))
				{
					placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesLeft));
					region.setArmies(region.getArmies() + armies);
					return placeArmiesMoves;
				}
			}
		}
		
		for(Region region : myRegionsList)
			if(placeArmies(region, state) && armiesLeft > 0)
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
				armiesLeft -= armies;
				region.setArmies(region.getArmies() + armies);
			}
		
		//add to random region with an enemy neighbor
		while(armiesLeft > 0 && !myRegionsList.isEmpty())
		{	
			double rand = Math.random();	
			int rIndex = (int) (myRegionsList.size() * rand);
			Region region = myRegionsList.get(rIndex);
		
			if(hasEnemyNeighbor(region, state.getMyPlayerName()))
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
				armiesLeft -= armies;
				region.setArmies(region.getArmies() + armies);
			}
			
			myRegionsList.remove(rIndex);
		}
		/*
		synchronized(this){
			try{
			this.wait(500);
			}
			catch(Exception e){}
		}
		*/
		return placeArmiesMoves;
	}

	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		String myName = state.getMyPlayerName();
		
		
		ArrayList<Region> myRegionsList = getMyRegions(state);
		ArrayList<Region> attackedRegions = new ArrayList<Region>();
		
		//transfers
		ArrayList<ArrayList<Region>> transferFromRegions = getTransferFromRegions(myRegionsList, state);
		int nrOfArmies = countMyArmies(myRegionsList);
		
		for(int i=0; i<3; i++)
		{
			for(Region fromRegion : transferFromRegions.get(i))
			{
				int armies;
				
				if(i == 0)
					armies = (int) (fromRegion.getArmies() - (nrOfArmies * 0.15));
				else if(i == 1)
					armies = (int) (fromRegion.getArmies() - (nrOfArmies * 0.10));
				else
					armies = fromRegion.getArmies() - 1;
					
				if(armies >= 1)
				{
					boolean toRegionHasEnemy = false;
					
					for(Region toRegion : fromRegion.getNeighbors())
					{
						ArrayList<Region> enemyToRegionNeighbors = getEnemyNeighbors(toRegion, state);
						if(!enemyToRegionNeighbors.isEmpty())
						{
							toRegionHasEnemy = true;
							attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
							break;
						}
					}
					if(!toRegionHasEnemy) //transfer to random neighboring region
					{
						double rand = Math.random();
						int index = (int) (fromRegion.getNeighbors().size() * rand);
						Region toRegion = fromRegion.getNeighbors().get(index);
						
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
					}
				}
			}
		}
		
		//set the transfers to the map for the attacks
		for(AttackTransferMove transfer : attackTransferMoves)
		{
			Region fromRegion = transfer.getFromRegion();
			Region toRegion = transfer.getToRegion();
			int armies = transfer.getArmies();
			
			fromRegion.setArmies(fromRegion.getArmies() - armies);
			toRegion.setArmies(toRegion.getArmies() + armies);
		}
		
		//attacks
		ArrayList<Region> attackingRegions = orderFromRegionsAttack(myRegionsList, state);
		
		for(Region fromRegion : attackingRegions)
		{
			ArrayList<Region> toRegions = orderToRegionsAttack(fromRegion, state);
			for(int i=toRegions.size()-1; i>=0; i--) //reversed order
			{
				Region toRegion = toRegions.get(i);
				int armies = attackRegionWithHowManyArmies(fromRegion, toRegion);
				if(armies > 0 && !attackedRegions.contains(toRegion))
				{
					attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
					attackedRegions.add(toRegion);
					fromRegion.setArmies(fromRegion.getArmies() - armies);
				}
			}
		}

		return attackTransferMoves;
	}
	
	private ArrayList<Region> orderFromRegionsAttack(ArrayList<Region> myRegions, BotState state)
	{
		ArrayList<ArrayList<Region>> regionTable = new ArrayList<ArrayList<Region>>();
		for(int i=0; i<=10; i++)
			regionTable.add(new ArrayList<Region>());
		
		for(Region region : myRegions)
		{
			int value = 0;
			
			boolean isBorderRegion = isBorderRegion(region);
			boolean hasEnemyNeighbor = hasEnemyNeighbor(region, state.getMyPlayerName());
			boolean isInTakableSuperRegion = isInTakableSuperRegion(region, region.getSuperRegion(), state);
			boolean hasMostArmiesInSuperRegion = hasMostArmiesInRegions(region, region.getSuperRegion().getSubRegions(), state);
			boolean isInGoodSuperRegion = isInGoodSuperRegion(region);
			int nrOfOwnedNeighbors = countNeighbors(region, state.getMyPlayerName());
			int nrOfNeutralNeighbors = countNeighbors(region, "neutral");
			int nrOfOpponentNeighbors = countNeighbors(region, state.getOpponentPlayerName());
			int mostArmiesTop3 = hasMostArmies(myRegions, region);
			
			if(!hasEnemyNeighbor)
				continue;
			
			if(isInGoodSuperRegion)
				value += 1;
			
			if(isInTakableSuperRegion && hasMostArmiesInSuperRegion)
			{
				value += 9;
				regionTable.get(value).add(region);
				continue;
			}
			
			if(isInTakableSuperRegion)
				value += 1;
			
			value += mostArmiesTop3;
			
			if(nrOfOwnedNeighbors >= nrOfNeutralNeighbors+nrOfOpponentNeighbors)
				value += 1;
			
			if(nrOfOpponentNeighbors == 0)
				value += 1;
			
			if(isBorderRegion)
				value += 1;
			
			regionTable.get(value).add(region);
		}
		
		return toSortedArrayList(regionTable);
	}
	
	//returns in reversed order
	private ArrayList<Region> orderToRegionsAttack(Region fromRegion, BotState state)
	{
		ArrayList<Region> neighbors = new ArrayList<Region>(fromRegion.getNeighbors());
		ArrayList<Region> toRegions1 = new ArrayList<Region>();
		ArrayList<Region> toRegions2 = new ArrayList<Region>();
		
		//if(isInTakableSuperRegion(fromRegion, fromRegion.getSuperRegion(), state))
		//{
			for(Region toRegion : neighbors)
			{
				if(fromRegion.getSuperRegion() == toRegion.getSuperRegion())
					toRegions1.add(toRegion);
				else
					toRegions2.add(toRegion);
			}
			toRegions1 = orderRegionsByArmies(toRegions1);
			toRegions2 = orderRegionsByArmies(toRegions2);
			
			toRegions2.addAll(toRegions1);
			
			return toRegions2;
		//}
		
		//neighbors = orderRegionsByArmies(neighbors);
		
		//return neighbors;
	}
	
	private ArrayList<ArrayList<Region>> getTransferFromRegions(ArrayList<Region> myRegions, BotState state)
	{
		ArrayList<ArrayList<Region>> transferFromRegions = new ArrayList<ArrayList<Region>>();
		for(int i=0; i<3; i++)
			transferFromRegions.add(new ArrayList<Region>());
		
		ArrayList<SuperRegion> ownedSuperRegions = getOwnedSuperRegions(state);
		
		for(Region region : myRegions)
		{
			boolean onlyOwnedNeighbors = true;
			for(Region neighbor : region.getNeighbors())
				if(!neighbor.getPlayerName().equals(state.getMyPlayerName()))
				{
					onlyOwnedNeighbors = false;
					break;
				}
			
			if(onlyOwnedNeighbors)
			{
				boolean isBorderRegion = isBorderRegion(region);
				if(ownedSuperRegions.contains(region) && isBorderRegion)
					transferFromRegions.get(0).add(region);
				if(isBorderRegion)
					transferFromRegions.get(1).add(region);
				else
					transferFromRegions.get(2).add(region);
			}
		}
		
		return transferFromRegions;
	}
	
	private ArrayList<Region> toSortedArrayList(ArrayList<ArrayList<Region>> regionTable)
	{
		ArrayList<Region> sortedFromRegions = new ArrayList<Region>();
		
		for(int i=10; i>=0; i--)
			if(!regionTable.get(i).isEmpty())
			{
				ArrayList<Region> orderedRegions = orderRegionsByArmies(regionTable.get(i));
				sortedFromRegions.addAll(orderedRegions);
			}
		
		return sortedFromRegions;
	}
	
	//return a list of regions, starting with the region with the most armies and ending with the least.
	private ArrayList<Region> orderRegionsByArmies(ArrayList<Region> regions)
	{
		ArrayList<Region> orderedRegions = new ArrayList<Region>();
		
		while(!regions.isEmpty())
		{
			Region mostArmies = regions.get(0);
			
			for(Region region : regions)
				if(region.getArmies() > mostArmies.getArmies())
					mostArmies = region;
			
			orderedRegions.add(mostArmies);
			regions.remove(mostArmies);
		}
		
		return orderedRegions;
	}
	
	//is the given region on the border of a SuperRegion?
	private boolean isBorderRegion(Region region)
	{	
		for(Region neighbor : region.getNeighbors())
			if(neighbor.getSuperRegion() != region.getSuperRegion())
				return true;
		return false;
	}
	
	//count the number of neighbors with the given name (neutral/opponentname)
	private int countNeighbors(Region region, String enemyName)
	{
		int nrOfEnemyNeighbors = 0;
		
		for(Region neighbor : region.getNeighbors())
			if(neighbor.getPlayerName().equals(enemyName))
				nrOfEnemyNeighbors++;
		
		return nrOfEnemyNeighbors;
	}
	
	//does the region have a neighbor which is not the player's region
	private boolean hasEnemyNeighbor(Region region, String myName)
	{
		for(Region neighbor : region.getNeighbors())
			if(!neighbor.getPlayerName().equals(myName))
				return true;
		return false;
	}
	
	//does the region have a neighbor which is not the player's region, is on the same SuperRegion
	private boolean hasEnemyNeighborInSuperRegion(Region region, SuperRegion superRegion, String myName)
	{
		for(Region neighbor : region.getNeighbors())
			if(!neighbor.getPlayerName().equals(myName) && 
					neighbor.getSuperRegion() == region.getSuperRegion())
				return true;
		return false;
	}
	
	private ArrayList<Region> getEnemyNeighbors(Region region, BotState state)
	{
		ArrayList<Region> enemyNeighbors = new ArrayList<Region>();
		
		for(Region neighbor : region.getNeighbors())
			if(neighbor.getPlayerName().equals(state.getOpponentPlayerName()))
				enemyNeighbors.add(neighbor);
		
		return enemyNeighbors;
	}
	
	//checks if the region is in the top 3 of most armies and returns that value
	private int hasMostArmies(ArrayList<Region> myRegions, Region region)
	{
		Region r1, r2, r3;
		r1 = region;
		r2 = null;
		r3 = null;
		
		for(Region r : myRegions)
			if(r.getArmies() > r1.getArmies())
			{
				r3 = r2;
				r2 = r1;
				r1 = r;
			}
		
		if(region == r1)
			return 3;
		if(region == r2)
			return 2;
		if(region == r3)
			return 1;
		
		return 0;
	}
	
	private boolean isSuperRegionFullyVisible(SuperRegion superRegion, BotState state)
	{
		boolean visible = true;
		for(Region subRegion : superRegion.getSubRegions())
			if(!state.getVisibleMap().getRegions().contains(subRegion))
			{
				visible = false;
				break;
			}
		return visible;
	}
	
	private int countMyArmies(ArrayList<Region> myRegions)
	{
		int myArmies = 0;
		
		for(Region region : myRegions)
			myArmies += region.getArmies();
		
		return myArmies;
	}
	
	//counts the number of armies in a superRegion for the given name (if superRegion is fully visible)
	private int countArmiesInSuperRegion(SuperRegion superRegion, String name, BotState state)
	{
		int nrOfArmiesInSuperRegion = 0;
		
		for(Region region : superRegion.getSubRegions())
			if(region.getPlayerName().equals(name))
				nrOfArmiesInSuperRegion += region.getArmies();
		
		return nrOfArmiesInSuperRegion;
	}
	
	private boolean isInTakableSuperRegion(Region region, SuperRegion superRegion, BotState state)
	{
		if(isSuperRegionFullyVisible(superRegion, state))
		{
			if(getNrOfMyRegionsInSuperRegion(superRegion, state)*2 >= superRegion.getSubRegions().size()
					&& superRegion.ownedByPlayer() == null) 												//I have most of the regions in given superRegion					
			{
				int myArmies = countArmiesInSuperRegion(superRegion, state.getMyPlayerName(), state);
				int opponentArmies = countArmiesInSuperRegion(superRegion, state.getOpponentPlayerName(), state);
				
				if(myArmies > opponentArmies) //I have more armies on the SuperRegion than my opponent
					return true;
				
				boolean opponentIsOnThisSuperRegion = false;
				for(Region subRegion : superRegion.getSubRegions())
				{
					if(subRegion.getPlayerName().equals(state.getOpponentPlayerName()))
					{
						opponentIsOnThisSuperRegion = true;
						break;
					}
				}
				
				if(!opponentIsOnThisSuperRegion)
					return true;
			}
		}
		
		return false;
	}
	
	//returns the number of owned regions in given SuperRegion (SuperRegion must be fully visible)
	private int getNrOfMyRegionsInSuperRegion(SuperRegion superRegion, BotState state)
	{
		int nrOfRegions = 0;
		
		for(Region region : superRegion.getSubRegions())
			if(region.getPlayerName().equals(state.getMyPlayerName()))
				nrOfRegions++;
		
		return nrOfRegions;
	}
	
	private boolean hasMostArmiesInRegions(Region region, LinkedList<Region> regions, BotState state)
	{
		int mostArmies = 0;
		
		for(Region subRegion : regions)
			if(subRegion.getArmies() > mostArmies)
				mostArmies = subRegion.getArmies();
		
		if(region.getArmies() >= mostArmies)
			return true;
		
		return false;
	}
	
	private boolean isInGoodSuperRegion(Region region)
	{
		if(!(region.getSuperRegion().getId() == 5 || region.getSuperRegion().getId() == 3))
			return true;
		return false;
	}
	
	private ArrayList<SuperRegion> getOwnedSuperRegions(BotState state)
	{
		ArrayList<SuperRegion> mySuperRegions = new ArrayList<SuperRegion>();
		
		for(SuperRegion superRegion : state.getVisibleMap().getSuperRegions())
		{
			String playerName = superRegion.ownedByPlayer();
			if(playerName != null && playerName.equals(state.getMyPlayerName()))
				mySuperRegions.add(superRegion);
		}
		
		return mySuperRegions;
	}
	
	private ArrayList<Region> getMyRegions(BotState state) {
		ArrayList<Region> myRegionsList = new ArrayList<Region>();
		
		for(Region region : state.getVisibleMap().getRegions())
			if(region.ownedByPlayer(state.getMyPlayerName()))
				myRegionsList.add(region);
		
		return myRegionsList;
	}
	
	private boolean isEnemyRegion(Region region, Region neighbor)
	{
		if(!region.getPlayerName().equals(neighbor.getPlayerName()))
			return true;
		return false;
	}
	
	private boolean placeArmies(Region region, BotState state)
	{
		for(Region neighbor : region.getNeighbors())
			if(region.getSuperRegion() != neighbor.getSuperRegion() && 
				neighbor.getPlayerName().equals(state.getOpponentPlayerName()))
				return true;
		return false;
	}
	
	//decides whether to attack a region or not
	private int attackRegionWithHowManyArmies(Region fromRegion, Region toRegion)
	{
		if(fromRegion.getArmies() > 5 && isEnemyRegion(fromRegion, toRegion))
		{
			boolean fullAttack = true;
			for(Region neighbor : fromRegion.getNeighbors())
			{
				if(neighbor != toRegion && isEnemyRegion(fromRegion, neighbor))
				{
					fullAttack = false;
					break;
				}
			}
			if(fullAttack)
				return (fromRegion.getArmies() - 1);
			
			if(toRegion.getArmies() * 6 <= fromRegion.getArmies())
				return fromRegion.getArmies() / 2;
			
			if(toRegion.getArmies() * 3 <= fromRegion.getArmies())
				return (int) (toRegion.getArmies() * 2.6); 
		}
		return 0;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarterJim());
		parser.run();
	}

}
