package bot;

import java.util.ArrayList;

import main.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public interface Bot {
	
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut);
	
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut);
	
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut);

}
