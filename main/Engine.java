package main;

import java.util.ArrayList;
import java.util.LinkedList;

import move.AttackTransferMove;
import move.Move;
import move.MoveQueue;
import move.MoveResult;
import move.PlaceArmiesMove;

public class Engine {
	
	private Player player1;
	private Player player2;
	private Map map;
	private Parser parser;
	private int roundNr;
	private LinkedList<MoveResult> fullPlayedGame;
	private LinkedList<MoveResult> player1PlayedGame;
	private LinkedList<MoveResult> player2PlayedGame;
	private MoveQueue moveQueue;

	public Engine(Map initMap, Player player1, Player player2)
	{
		this.map = initMap;
		this.player1 = player1;
		this.player2 = player2;
		roundNr = 1;
		moveQueue = new MoveQueue(player1, player2);
		
		parser = new Parser(player1, player2, map);
		
		fullPlayedGame = new LinkedList<MoveResult>();
		player1PlayedGame = new LinkedList<MoveResult>();
		player2PlayedGame = new LinkedList<MoveResult>();
	}
	
	public void playRound()
	{
		getMoves(player1.getBot().getPlaceArmiesMoves(2000));
		getMoves(player2.getBot().getPlaceArmiesMoves(2000));
		
		executePlaceArmies();
		
		getMoves(player1.getBot().getAttackTransferMoves(2000));
		getMoves(player2.getBot().getAttackTransferMoves(2000));
		
		moveQueue.orderMoves(roundNr, moveQueue.ORDER_RANDOM); //order random
		executeAttackTransfer();
		
		moveQueue.clear();
		recalculateStartingArmies();
		sendAllInfo();	
		fullPlayedGame.add(null); //indicates round end	
		player1PlayedGame.add(null);
		player2PlayedGame.add(null);
		roundNr++;	
	}
	
	public void distributeStartingRegions()
	{
		ArrayList<Region> pickableRegions = new ArrayList<Region>();
		int nrOfStartingRegions = 3;
		int regionsAdded = 0;
		
		//pick semi random regions to start with
		for(SuperRegion superRegion : map.getSuperRegions())
		{
			int nrOfRegions = superRegion.getSubRegions().size();
			while(regionsAdded < 2)
			{
				double rand = Math.random();
				int randomRegionId = (int) (rand*nrOfRegions);
				Region randomRegion = superRegion.getSubRegions().get(randomRegionId); //get one random subregion from superRegion
				if(!pickableRegions.contains(randomRegion))
				{
					pickableRegions.add(randomRegion);
					regionsAdded++;
				}
			}
			regionsAdded = 0;
		}
		
		//get the preferred starting regions from the players
		ArrayList<Region> p1Regions = parser.parsePreferredStartingRegions(player1.getBot().getPreferredStartingArmies(2000, pickableRegions), pickableRegions);
		ArrayList<Region> p2Regions = parser.parsePreferredStartingRegions(player2.getBot().getPreferredStartingArmies(2000, pickableRegions), pickableRegions);
		ArrayList<Region> givenP1Regions = new ArrayList<Region>();
		ArrayList<Region> givenP2Regions = new ArrayList<Region>();
		
		//if the bot did not correctly return his starting regions, get some random ones
		if(p1Regions == null)
			p1Regions = getRandomStartingRegions(pickableRegions);
		if(p2Regions == null)
			p2Regions = getRandomStartingRegions(pickableRegions);
		
		//distribute the starting regions
		int i1, i2, n;
		i1 = 0; i2 = 0;
		n = 0;
		while(n < nrOfStartingRegions)
		{
			Region p1Region = p1Regions.get(i1);
			Region p2Region = p2Regions.get(i2);
			
			if(givenP2Regions.contains(p1Region)) //preferred region for player1 is not given to player2 already
				i1++;
			else if(givenP1Regions.contains(p2Region)) //preferred region for player2 is not given to player1 already
				i2++;
			else if(p1Region != p2Region)
			{
				p1Region.setPlayerName(player1.getName());
				p2Region.setPlayerName(player2.getName());
				givenP1Regions.add(p1Region);
				givenP2Regions.add(p2Region);
				n++; i1++; i2++;
			}
			else //random player gets the region if same preference
			{
				double rand = Math.random();
				if(rand < 0.5)
					i1++;
				else
					i2++;
			}
		}
		
		fullPlayedGame.add(new MoveResult(null, map.getMapCopy()));
		player1PlayedGame.add(new MoveResult(null, map.getVisibleMapCopyForPlayer(player1)));
		player2PlayedGame.add(new MoveResult(null, map.getVisibleMapCopyForPlayer(player2)));
	}
	
	private ArrayList<Region> getRandomStartingRegions(ArrayList<Region> pickableRegions)
	{
		ArrayList<Region> startingRegions = new ArrayList<Region>();
		for(int i=0; i<6; i++)
		{
			double rand = Math.random();
			int randomIndex = (int) rand * pickableRegions.size();
			Region randomRegion = pickableRegions.get(randomIndex);
			startingRegions.add(randomRegion);
		}
		return startingRegions;
	}
	
	private void getMoves(String movesInput)
	{
		ArrayList<Move> moves = parser.parseMoves(movesInput);
		
		for(Move move : moves)
		{
			try //PlaceArmiesMove
			{
				PlaceArmiesMove plm = (PlaceArmiesMove) move;
				queuePlaceArmies(plm);
			}
			catch(Exception e) //AttackTransferMove
			{
				AttackTransferMove atm = (AttackTransferMove) move;
				queueAttackTransfer(atm);
			}
		}
	}

	private void queuePlaceArmies(PlaceArmiesMove plm)
	{
		if(plm == null) { System.out.println("Error on place_armies input."); return; }
		
		Region region = plm.getRegion();
		Player player = getPlayer(plm.getPlayerName());
		int armies = plm.getArmies();
		
		//check legality
		if(region.ownedByPlayer(player.getName()))
		{
			if(armies > player.getArmiesLeft()) //player wants to place more armies than he has left
				plm.setArmies(player.getArmiesLeft()); //place all armies he has left
			if(player.getArmiesLeft() <= 0)
				plm.setIllegalMove(" place-armies " + "no armies left to place");
			
			player.setArmiesLeft(player.getArmiesLeft() - plm.getArmies());
		}
		else
			plm.setIllegalMove(plm.getRegion().getId() + " place_armies " + "doesn't own ");

		moveQueue.addMove(plm);
	}
	
	private void queueAttackTransfer(AttackTransferMove atm)
	{
		if(atm == null){ System.out.println("Error on attack/transfer input."); return; }
		
		Region fromRegion = atm.getFromRegion();
		Region toRegion = atm.getToRegion();
		Player player = getPlayer(atm.getPlayerName());
		int armies = atm.getArmies();
		
		//check legality
		if(fromRegion.ownedByPlayer(player.getName()))
		{
			if(fromRegion.isNeighbor(toRegion))
			{
				if(armies < 1)
					atm.setIllegalMove(atm.getFromRegion().getId() + " attack/transfer " + "has less than 1 army");
			}
			else
				atm.setIllegalMove(atm.getToRegion().getId() + " attack/transfer " + "not a neighbor");
		}
		else
			atm.setIllegalMove(atm.getFromRegion().getId() + " attack/transfer " + "not owned");

		moveQueue.addMove(atm);
	}
	
	//Moves have already been checked if they are legal
	private void executePlaceArmies()
	{
		for(PlaceArmiesMove move : moveQueue.placeArmiesMoves)
		{
			if(move.getIllegalMove().equals("")) //the move is not illegal
				move.getRegion().setArmies(move.getRegion().getArmies() + move.getArmies());
			
			Map mapCopy = map.getMapCopy();
			fullPlayedGame.add(new MoveResult(move, mapCopy));
			if(map.visibleRegionsForPlayer(player1).contains(move.getRegion()))
				player1PlayedGame.add(new MoveResult(move, map.getVisibleMapCopyForPlayer(player1)));
			if(map.visibleRegionsForPlayer(player2).contains(move.getRegion()))
				player2PlayedGame.add(new MoveResult(move, map.getVisibleMapCopyForPlayer(player2)));
		}
	}

	private void executeAttackTransfer()
	{
		LinkedList<Region> visibleRegionsPlayer1Map = map.visibleRegionsForPlayer(player1);
		LinkedList<Region> visibleRegionsPlayer2Map = map.visibleRegionsForPlayer(player2);
		LinkedList<Region> visibleRegionsPlayer1OldMap = visibleRegionsPlayer1Map;
		LinkedList<Region> visibleRegionsPlayer2OldMap = visibleRegionsPlayer2Map;
		
		for(AttackTransferMove move : moveQueue.attackTransferMoves)
		{	
			if(move.getIllegalMove().equals("")) //the move is not illegal
			{
				Region fromRegion = move.getFromRegion();
				Region toRegion = move.getToRegion();
				Player player = getPlayer(move.getPlayerName());
				
				if(fromRegion.ownedByPlayer(player.getName())) //check if the fromRegion still belongs to this player
				{
					if(toRegion.ownedByPlayer(player.getName())) //transfer
					{
						if(fromRegion.getArmies() > 1)
						{
							if(fromRegion.getArmies() - 1 < move.getArmies()) //not enough armies on fromRegion?
								move.setArmies(fromRegion.getArmies() - 1); //move the maximal number.
							
							fromRegion.setArmies(fromRegion.getArmies() - move.getArmies());
							toRegion.setArmies(toRegion.getArmies() + move.getArmies());
						}
						else
							move.setIllegalMove(move.getFromRegion().getId() + " transfer " + "only 1 army on ");
					}
					else //attack
						doAttack(move);
				}
				else
					move.setIllegalMove(move.getFromRegion().getId() + " attack/transfer " + "was taken this round");
			}
			visibleRegionsPlayer1Map = map.visibleRegionsForPlayer(player1);
			visibleRegionsPlayer2Map = map.visibleRegionsForPlayer(player2);
			
			fullPlayedGame.add(new MoveResult(move, map.getMapCopy()));
			if(visibleRegionsPlayer1Map.contains(move.getFromRegion()) || visibleRegionsPlayer1Map.contains(move.getToRegion()) ||
					visibleRegionsPlayer1OldMap.contains(move.getToRegion()))
				player1PlayedGame.add(new MoveResult(move, map.getVisibleMapCopyForPlayer(player1)));
			if(visibleRegionsPlayer2Map.contains(move.getFromRegion()) || visibleRegionsPlayer2Map.contains(move.getToRegion()) ||
					visibleRegionsPlayer2OldMap.contains(move.getToRegion()))
				player2PlayedGame.add(new MoveResult(move, map.getVisibleMapCopyForPlayer(player2)));
			
			visibleRegionsPlayer1OldMap = visibleRegionsPlayer1Map;
			visibleRegionsPlayer2OldMap = visibleRegionsPlayer2Map;
		}
	}
	
	//see wiki.warlight.net/index.php/Combat_Basics
	private void doAttack(AttackTransferMove move)
	{
		Region fromRegion = move.getFromRegion();
		Region toRegion = move.getToRegion();
		int attackingArmies;
		int defendingArmies = toRegion.getArmies();
		
		int defendersDestroyed = 0;
		int attackersDestroyed = 0;
		
		if(fromRegion.getArmies() > 1)
		{
			if(fromRegion.getArmies()-1 >= move.getArmies()) //are there enough armies on fromRegion?
				attackingArmies = move.getArmies();
			else
				attackingArmies = fromRegion.getArmies()-1;
			
			for(int t=1; t<=attackingArmies; t++) //calculate how much defending armies are destroyed
			{
				double rand = Math.random();
				if(rand < 0.6) //60% chance to destroy one defending army
					defendersDestroyed++;
			}
			for(int t=1; t<=defendingArmies; t++) //calculate how much attacking armies are destroyed
			{
				double rand = Math.random();
				if(rand < 0.7) //70% chance to destroy one attacking army
					attackersDestroyed++;
			}
			
			if(attackersDestroyed >= attackingArmies)
			{
				if(defendersDestroyed >= defendingArmies)
					defendersDestroyed = defendingArmies - 1;
				
				attackersDestroyed = attackingArmies;
			}		
			
			//process result of attack
			if(defendersDestroyed >= defendingArmies) //attack success
			{
				fromRegion.setArmies(fromRegion.getArmies() - attackingArmies);
				toRegion.setPlayerName(move.getPlayerName());
				toRegion.setArmies(attackingArmies - attackersDestroyed);
			}
			else //attack fail
			{
				fromRegion.setArmies(fromRegion.getArmies() - attackersDestroyed);
				toRegion.setArmies(toRegion.getArmies() - defendersDestroyed);
			}
		}
		else
			move.setIllegalMove(move.getFromRegion().getId() + " attack " + "only 1 army on ");
	}
	
	public Player winningPlayer()
	{
		if(map.ownedRegionsByPlayer(player1).isEmpty())
			return player2;
		else if(map.ownedRegionsByPlayer(player2).isEmpty())
			return player1;
		else
			return null;
	}
	
	//calculate how many armies each player is able to place on the map for the next.
	public void recalculateStartingArmies()
	{
		player1.setArmiesLeft(player1.getArmiesPerTurn());
		player2.setArmiesLeft(player2.getArmiesPerTurn());
		
		for(SuperRegion superRegion : map.getSuperRegions())
		{
			Player player = getPlayer(superRegion.ownedByPlayer());
			if(player != null)
				player.setArmiesLeft(player.getArmiesLeft() + superRegion.getArmiesReward());
		}
	}
	
	public void sendAllInfo()
	{
		//System.out.println("Round: " + this.roundNr);
		sendStartingArmiesInfo(player1);
		sendStartingArmiesInfo(player2);
		sendUpdateMapInfo(player1);
		sendUpdateMapInfo(player2);
	}
		
	//inform the player about how much armies he can place at the start next round
	private void sendStartingArmiesInfo(Player player)
	{
		String updateStartingArmiesString = "settings starting_armies";
		
		updateStartingArmiesString = updateStartingArmiesString.concat(" " + player.getArmiesLeft());
		
		//System.out.println("sending to " + player.getName() + ": " + updateStartingArmiesString);
		player.getBot().writeInfo(updateStartingArmiesString);
	}
	
	//inform the player about how his visible map looks now
	private void sendUpdateMapInfo(Player player)
	{
		LinkedList<Region> visibleRegions = map.visibleRegionsForPlayer(player);
		String updateMapString = "update_map";
		for(Region region : visibleRegions)
		{
			int id = region.getId();
			String playerName = region.getPlayerName();
			int armies = region.getArmies();
			
			updateMapString = updateMapString.concat(" " + id + " " + playerName + " " + armies);
		}
		//System.out.println("sending to " + player.getName() + ": " + updateMapString);
		player.getBot().writeInfo(updateMapString);
	}
	
	private Player getPlayer(String playerName)
	{
		if(player1.getName().equals(playerName))
			return player1;
		else if(player2.getName().equals(playerName))
			return player2;
		else
			return null;
	}
	
	public LinkedList<MoveResult> getFullPlayedGame() {
		return fullPlayedGame;
	}
	
	public LinkedList<MoveResult> getPlayer1PlayedGame() {
		return player1PlayedGame;
	}
	
	public LinkedList<MoveResult> getPlayer2PlayedGame() {
		return player2PlayedGame;
	}
	
	public int getRoundNr() {
		return roundNr;
	}
}
