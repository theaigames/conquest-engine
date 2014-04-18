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
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

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
	private LinkedList<Move> opponentMovesPlayer1;
	private LinkedList<Move> opponentMovesPlayer2;
	private MoveQueue moveQueue;

	public Engine(Map initMap, Player player1, Player player2)
	{
		this.map = initMap;
		this.player1 = player1;
		this.player2 = player2;
		roundNr = 1;
		moveQueue = new MoveQueue(player1, player2);
		
		parser = new Parser(map);
		
		fullPlayedGame = new LinkedList<MoveResult>();
		player1PlayedGame = new LinkedList<MoveResult>();
		player2PlayedGame = new LinkedList<MoveResult>();
		opponentMovesPlayer1 = new LinkedList<Move>();
		opponentMovesPlayer2 = new LinkedList<Move>();
	}
	
	public void playRound()
	{
		getMoves(player1.getBot().getPlaceArmiesMoves(2000), player1);
		getMoves(player2.getBot().getPlaceArmiesMoves(2000), player2);
		
		executePlaceArmies();
		
		getMoves(player1.getBot().getAttackTransferMoves(2000), player1);
		getMoves(player2.getBot().getAttackTransferMoves(2000), player2);
		
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
		ArrayList<Region> p1Regions = parser.parsePreferredStartingRegions(player1.getBot().getPreferredStartingArmies(2000, pickableRegions), pickableRegions, player1);
		ArrayList<Region> p2Regions = parser.parsePreferredStartingRegions(player2.getBot().getPreferredStartingArmies(2000, pickableRegions), pickableRegions, player2);
		ArrayList<Region> givenP1Regions = new ArrayList<Region>();
		ArrayList<Region> givenP2Regions = new ArrayList<Region>();
		
		//if the bot did not correctly return his starting regions, get some random ones
		if(p1Regions == null) {
			p1Regions = new ArrayList<Region>();
		}
		if(p2Regions == null) {
			p2Regions = new ArrayList<Region>();
		}

		p1Regions.addAll(getRandomStartingRegions(pickableRegions));
		p2Regions.addAll(getRandomStartingRegions(pickableRegions));
		
		//distribute the starting regions
		int i1, i2, n;
		i1 = 0; i2 = 0;
		n = 0;

		while(n < nrOfStartingRegions) {
			Region p1Region = p1Regions.get(i1);
			Region p2Region = p2Regions.get(i2);
			
			if(givenP2Regions.contains(p1Region)) {//preferred region for player1 is not given to player2 already
				i1++;
			} else if(givenP1Regions.contains(p2Region)) { //preferred region for player2 is not given to player1 already
				i2++;
			} else if(p1Region != p2Region) {
				p1Region.setPlayerName(player1.getName());
				p2Region.setPlayerName(player2.getName());
				givenP1Regions.add(p1Region);
				givenP2Regions.add(p2Region);
				n++; i1++; i2++;
			} else { //random player gets the region if same preference
				double rand = Math.random();
				if(rand < 0.5) {
					i1++;
				} else {
					i2++;
				}
			}
		}
		
		fullPlayedGame.add(new MoveResult(null, map.getMapCopy()));
		player1PlayedGame.add(new MoveResult(null, map.getVisibleMapCopyForPlayer(player1)));
		player2PlayedGame.add(new MoveResult(null, map.getVisibleMapCopyForPlayer(player2)));
	}
	
	private List<Region> getRandomStartingRegions(ArrayList<Region> pickableRegions)
	{
		List<Region> startingRegions = new ArrayList<Region>(pickableRegions);
		Collections.shuffle(startingRegions);

		startingRegions = startingRegions.subList(0,6);
		return startingRegions;
	}
	
	private void getMoves(String movesInput, Player player)
	{
		ArrayList<Move> moves = parser.parseMoves(movesInput, player);
		
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
		//should not ever happen
		if(plm == null) { System.err.println("Error on place_armies input."); return; }
		
		Region region = plm.getRegion();
		Player player = getPlayer(plm.getPlayerName());
		int armies = plm.getArmies();
		
		//check legality
		if(region.ownedByPlayer(player.getName()))
		{
			if(armies < 1)
			{
				plm.setIllegalMove(" place-armies " + "cannot place less than 1 army");
			}
			else
			{
				if(armies > player.getArmiesLeft()) //player wants to place more armies than he has left
					plm.setArmies(player.getArmiesLeft()); //place all armies he has left
				if(player.getArmiesLeft() <= 0)
					plm.setIllegalMove(" place-armies " + "no armies left to place");
				
				player.setArmiesLeft(player.getArmiesLeft() - plm.getArmies());
			}
		}
		else
			plm.setIllegalMove(plm.getRegion().getId() + " place-armies " + " not owned");

		moveQueue.addMove(plm);
	}
	
	private void queueAttackTransfer(AttackTransferMove atm)
	{
		//should not ever happen
		if(atm == null){ System.err.println("Error on attack/transfer input."); return; }
		
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
					atm.setIllegalMove(" attack/transfer " + "cannot use less than 1 army");
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
			{
				player1PlayedGame.add(new MoveResult(move, map.getVisibleMapCopyForPlayer(player1))); //for the game file
				if(move.getPlayerName().equals(player2.getName()))
					opponentMovesPlayer1.add(move); //for the opponent_moves output
			}
			if(map.visibleRegionsForPlayer(player2).contains(move.getRegion()))
			{
				player2PlayedGame.add(new MoveResult(move, map.getVisibleMapCopyForPlayer(player2))); //for the game file
				if(move.getPlayerName().equals(player1.getName()))
					opponentMovesPlayer2.add(move); //for the opponent_moves output
			}
		}
	}

	private void executeAttackTransfer()
	{
		LinkedList<Region> visibleRegionsPlayer1Map = map.visibleRegionsForPlayer(player1);
		LinkedList<Region> visibleRegionsPlayer2Map = map.visibleRegionsForPlayer(player2);
		LinkedList<Region> visibleRegionsPlayer1OldMap = visibleRegionsPlayer1Map;
		LinkedList<Region> visibleRegionsPlayer2OldMap = visibleRegionsPlayer2Map;
		ArrayList<ArrayList<Integer>> usedRegions = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i<=42; i++) {
			usedRegions.add(new ArrayList<Integer>());
		}
		Map oldMap = map.getMapCopy();

		int moveNr = 1;
		Boolean previousMoveWasIllegal = false;
		String previousMovePlayer = "";
		while(moveQueue.hasNextAttackTransferMove())
		{	
			AttackTransferMove move = moveQueue.getNextAttackTransferMove(moveNr, previousMovePlayer, previousMoveWasIllegal);

			if(move.getIllegalMove().equals("")) //the move is not illegal
			{
				Region fromRegion = move.getFromRegion();
				Region oldFromRegion = oldMap.getRegion(move.getFromRegion().getId());
				Region toRegion = move.getToRegion();
				Player player = getPlayer(move.getPlayerName());
				
				if(fromRegion.ownedByPlayer(player.getName())) //check if the fromRegion still belongs to this player
				{
					if(!usedRegions.get(fromRegion.getId()).contains(toRegion.getId())) //between two regions there can only be attacked/transfered once
					{
						if(oldFromRegion.getArmies() > 1) //there are still armies that can be used
						{
							if(oldFromRegion.getArmies() < fromRegion.getArmies() && oldFromRegion.getArmies() - 1 < move.getArmies()) //not enough armies on fromRegion at the start of the round?
								move.setArmies(oldFromRegion.getArmies() - 1); //move the maximal number.
							else if(oldFromRegion.getArmies() >= fromRegion.getArmies() && fromRegion.getArmies() - 1 < move.getArmies()) //not enough armies on fromRegion currently?
								move.setArmies(fromRegion.getArmies() - 1); //move the maximal number.

							oldFromRegion.setArmies(oldFromRegion.getArmies() - move.getArmies()); //update oldFromRegion so new armies cannot be used yet

							if(toRegion.ownedByPlayer(player.getName())) //transfer
							{
								if(fromRegion.getArmies() > 1)
								{
									fromRegion.setArmies(fromRegion.getArmies() - move.getArmies());
									toRegion.setArmies(toRegion.getArmies() + move.getArmies());
									usedRegions.get(fromRegion.getId()).add(toRegion.getId());
								}
								else
									move.setIllegalMove(move.getFromRegion().getId() + " transfer " + "only has 1 army");
							}
							else //attack
							{
								doAttack(move);
								usedRegions.get(fromRegion.getId()).add(toRegion.getId());
							}
						}
						else
							move.setIllegalMove(move.getFromRegion().getId() + " attack/transfer " + "has used all available armies");
					}
					else
						move.setIllegalMove(move.getFromRegion().getId() + " attack/transfer " + "has already attacked/transfered to this region");
				}
				else
					move.setIllegalMove(move.getFromRegion().getId() + " attack/transfer " + "was taken this round");
			}

			visibleRegionsPlayer1Map = map.visibleRegionsForPlayer(player1);
			visibleRegionsPlayer2Map = map.visibleRegionsForPlayer(player2);
			
			fullPlayedGame.add(new MoveResult(move, map.getMapCopy()));
			if(visibleRegionsPlayer1Map.contains(move.getFromRegion()) || visibleRegionsPlayer1Map.contains(move.getToRegion()) ||
					visibleRegionsPlayer1OldMap.contains(move.getToRegion()))
			{
				player1PlayedGame.add(new MoveResult(move, map.getVisibleMapCopyForPlayer(player1))); //for the game file
				if(move.getPlayerName().equals(player2.getName()))
					opponentMovesPlayer1.add(move); //for the opponent_moves output
			}
			if(visibleRegionsPlayer2Map.contains(move.getFromRegion()) || visibleRegionsPlayer2Map.contains(move.getToRegion()) ||
					visibleRegionsPlayer2OldMap.contains(move.getToRegion()))
			{
				player2PlayedGame.add(new MoveResult(move, map.getVisibleMapCopyForPlayer(player2))); //for the game file
				if(move.getPlayerName().equals(player1.getName()))
					opponentMovesPlayer2.add(move); //for the opponent_moves output
			}
			
			visibleRegionsPlayer1OldMap = visibleRegionsPlayer1Map;
			visibleRegionsPlayer2OldMap = visibleRegionsPlayer2Map;

			//set some stuff to know what next move to get
			if(move.getIllegalMove().equals("")) {
				previousMoveWasIllegal = false;
				moveNr++;
			}
			else {
				previousMoveWasIllegal = true;
			}
			previousMovePlayer = move.getPlayerName();
			
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
			move.setIllegalMove(move.getFromRegion().getId() + " attack " + "only has 1 army");
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
	
	//calculate how many armies each player is able to place on the map for the next round
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
		sendStartingArmiesInfo(player1);
		sendStartingArmiesInfo(player2);
		sendUpdateMapInfo(player1);
		sendUpdateMapInfo(player2);
		sendOpponentMovesInfo(player1);
		opponentMovesPlayer1.clear();
		sendOpponentMovesInfo(player2);
		opponentMovesPlayer2.clear();
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
		player.getBot().writeInfo(updateMapString);
	}

	private void sendOpponentMovesInfo(Player player)
	{
		String opponentMovesString = "opponent_moves ";
		LinkedList<Move> opponentMoves = new LinkedList<Move>();

		if(player == player1)
			opponentMoves = opponentMovesPlayer1;
		else if(player == player2)
			opponentMoves = opponentMovesPlayer2;

		for(Move move : opponentMoves)
		{
			if(move.getIllegalMove().equals(""))
			{
				try {
					PlaceArmiesMove plm = (PlaceArmiesMove) move;
					opponentMovesString = opponentMovesString.concat(plm.getString() + " ");
				}
				catch(Exception e) {
					AttackTransferMove atm = (AttackTransferMove) move;
					opponentMovesString = opponentMovesString.concat(atm.getString() + " ");					
				}
			}
		}
		
		opponentMovesString = opponentMovesString.substring(0, opponentMovesString.length()-1);

		player.getBot().writeInfo(opponentMovesString);
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
