package move;
import java.util.ArrayList;

import main.Player;


public class MoveQueue {
	
	public ArrayList<PlaceArmiesMove> placeArmiesMoves;
	public ArrayList<AttackTransferMove> attackTransferMoves;
	private Player player1, player2;
	private int nrOfMovesP1, nrOfMovesP2;
	public final int ORDER_RANDOM = 1;
	public final int ORDER_CYCLIC = 2;
	
	public MoveQueue(Player player1, Player player2)
	{
		this.placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		this.attackTransferMoves = new ArrayList<AttackTransferMove>();
		this.player1 = player1;
		this.player2 = player2;
		this.nrOfMovesP1 = 0;
		this.nrOfMovesP2 = 0;
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
				nrOfMovesP1++;
			else if(player2.getName().equals(move.getPlayerName()))
				nrOfMovesP2++;
			
			attackTransferMoves.add(atm);
		}
	}
	/*
	public void addPlaceArmiesMove(PlaceArmiesMove move)
	{
		placeArmiesMoves.add(move);
	}
	
	public void addAttackTransferMove(AttackTransferMove move)
	{
		if(player1.name.equals(move.playerName))
			nrOfMovesP1++;
		else if(player2.name.equals(move.playerName))
			nrOfMovesP2++;
		
		attackTransferMoves.add(move);
	}
	*/
	public void clear()
	{
		placeArmiesMoves.clear();
		attackTransferMoves.clear();
		nrOfMovesP1 = 0;
		nrOfMovesP2 = 0;
	}
	
	//the player's moves are still in the same order, but here is determined which player moves first.
	//if orderingType is ORDER_RANDOM, player to move first each move is chosen random.
	//if orderingType is ORDER_CYCLIC, every round an other player moves first on every move.
	public void orderMoves(int roundNr, int orderingType)
	{
		ArrayList<AttackTransferMove> orderedMoves = new ArrayList<AttackTransferMove>();
		int p = nrOfMovesP1;
		int i = 0;
		
		while(true)
		{
			if(i >= p) //when player2 has more moves than player1
			{
				for(int j=i+p; j<attackTransferMoves.size(); j++)
					orderedMoves.add(attackTransferMoves.get(j)); //add remaining moves to queue
				break;
			}
			if(i+p >= p+nrOfMovesP2) //when player1 has more moves than player2
			{
				for(int j=i; j<p; j++)
					orderedMoves.add(attackTransferMoves.get(j)); //add remaining moves to queue
				break;
			}
			
			double rand = Math.random();
			if((orderingType == ORDER_RANDOM && rand < 0.5) || (orderingType == ORDER_CYCLIC && roundNr%2 == 1))
			{
				orderedMoves.add(attackTransferMoves.get(i)); 	//player1's move
				orderedMoves.add(attackTransferMoves.get(i+p));	//player2's move
			}
			else
			{
				orderedMoves.add(attackTransferMoves.get(i+p)); //player2's move
				orderedMoves.add(attackTransferMoves.get(i));	//player1's move
			}

			i++;
		}
		
		attackTransferMoves = orderedMoves;
	}


}
