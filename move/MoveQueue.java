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

package move;
import java.util.ArrayList;

import main.Player;


public class MoveQueue {
	
	public ArrayList<PlaceArmiesMove> placeArmiesMoves;
	public ArrayList<AttackTransferMove> attackTransferMovesP1;
	public ArrayList<AttackTransferMove> attackTransferMovesP2;
	private Player player1, player2;
	// public final int ORDER_RANDOM = 1;
	// public final int ORDER_CYCLIC = 2;
	
	public MoveQueue(Player player1, Player player2)
	{
		this.placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		this.attackTransferMovesP1 = new ArrayList<AttackTransferMove>();
		this.attackTransferMovesP2 = new ArrayList<AttackTransferMove>();
		this.player1 = player1;
		this.player2 = player2;
	}
	
	public void addMove(Move move)
	{
		try { //add PlaceArmiesMove
			PlaceArmiesMove plm = (PlaceArmiesMove) move;
			placeArmiesMoves.add(plm);
		}
		catch(Exception e) { //add AttackTransferMove
			AttackTransferMove atm = (AttackTransferMove) move;
			if(player1.getName().equals(move.getPlayerName()))
				attackTransferMovesP1.add(atm);
			else if(player2.getName().equals(move.getPlayerName()))
				attackTransferMovesP2.add(atm);
		}
	}

	public void clear()
	{
		placeArmiesMoves.clear();
		attackTransferMovesP1.clear();
		attackTransferMovesP2.clear();
	}

	public boolean hasNextAttackTransferMove()
	{
		if(attackTransferMovesP1.isEmpty() && attackTransferMovesP2.isEmpty())
			return false;
		return true;
	}

	//the player's moves are still in the same order, but here is determined which player moves first.
	//player to move first each move is chosen random.
	//makes sure that if a player has an illegal move, it is the next legal move is selected.
	public AttackTransferMove getNextAttackTransferMove(int moveNr, String previousMovePlayer, Boolean previousWasIllegal)
	{
		if(!hasNextAttackTransferMove()) //shouldnt ever happen
		{
			System.err.println("No more AttackTransferMoves left in MoveQueue");
			return null;
		}

		if(!previousWasIllegal)
		{
			if(moveNr % 2 == 1 || previousMovePlayer.equals("")) //first move of the two
			{
				double rand = Math.random();
				return getMove(rand < 0.5);
			}
			else //it's the other player's turn
			{
				return getMove(previousMovePlayer.equals(player2.getName()));
			}
		}
		else //return another move by the same player
		{
			return getMove(previousMovePlayer.equals(player1.getName()));
		}
	}

	private AttackTransferMove getMove(Boolean conditionForPlayer1)
	{
		AttackTransferMove move;
		if(!attackTransferMovesP1.isEmpty() && (conditionForPlayer1 || attackTransferMovesP2.isEmpty())) //get player1's move
		{
			move = attackTransferMovesP1.get(0);
			attackTransferMovesP1.remove(0);
			return move;
		}
		else
		{
			move = attackTransferMovesP2.get(0);
			attackTransferMovesP2.remove(0);
			return move;
		}
	}
	
	//the player's moves are still in the same order, but here is determined which player moves first.
	//if orderingType is ORDER_RANDOM, player to move first each move is chosen random.
	//if orderingType is ORDER_CYCLIC, every round an other player moves first on every move.
	
	//not used anymore
	// public void orderMoves(int roundNr, int orderingType)
	// {
	// 	if(!attackTransferMoves.isEmpty())
	// 	{
	// 		ArrayList<AttackTransferMove> orderedMoves = new ArrayList<AttackTransferMove>();
	// 		int p = nrOfMovesP1;
	// 		int i = 0;
			
	// 		while(true)
	// 		{
	// 			if(i >= p) //when player2 has more moves than player1
	// 			{
	// 				for(int j=i+p; j<attackTransferMoves.size(); j++)
	// 					orderedMoves.add(attackTransferMoves.get(j)); //add remaining moves to queue
	// 				break;
	// 			}
	// 			if(i+p >= p+nrOfMovesP2) //when player1 has more moves than player2
	// 			{
	// 				for(int j=i; j<p; j++)
	// 					orderedMoves.add(attackTransferMoves.get(j)); //add remaining moves to queue
	// 				break;
	// 			}
				
	// 			double rand = Math.random();
	// 			if((orderingType == ORDER_RANDOM && rand < 0.5) || (orderingType == ORDER_CYCLIC && roundNr%2 == 1))
	// 			{
	// 				orderedMoves.add(attackTransferMoves.get(i)); 	//player1's move
	// 				orderedMoves.add(attackTransferMoves.get(i+p));	//player2's move
	// 			}
	// 			else
	// 			{
	// 				orderedMoves.add(attackTransferMoves.get(i+p)); //player2's move
	// 				orderedMoves.add(attackTransferMoves.get(i));	//player1's move
	// 			}

	// 			i++;
	// 		}
			
	// 		attackTransferMoves = orderedMoves;
	// 	}
	// }


}
