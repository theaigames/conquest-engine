package move;
import main.Region;

/**
 * This Move is used in the second part of each round. It represents the attack or transfer of armies from
 * fromRegion to toRegion. If toRegion is owned by the player himself, it's a transfer. If toRegion is
 * owned by the opponent, this Move is an attack. 
 */

public class AttackTransferMove extends Move {
	
	private Region fromRegion;
	private Region toRegion;
	private int armies;
	
	//geen misbruik maken van playerName aub, alleen je eigen botnaam invullen
	public AttackTransferMove(String playerName, Region fromRegion, Region toRegion, int armies)
	{
		super.setPlayerName(playerName);
		this.fromRegion = fromRegion;
		this.toRegion = toRegion;
		this.armies = armies;
	}
	
	/**
	 * @param n Sets the number of armies of this Move
	 */
	public void setArmies(int n) {
		armies = n;
	}
	
	/**
	 * @return The Region this Move is attacking or transferring from
	 */
	public Region getFromRegion() {
		return fromRegion;
	}
	
	/**
	 * @return The Region this Move is attacking or transferring to
	 */
	public Region getToRegion() {
		return toRegion;
	}
	
	/**
	 * @return The number of armies this Move is attacking or transferring with
	 */
	public int getArmies() {
		return armies;
	}
	
	/**
	 * @return A string representation of this Move
	 */
	public String getString() {
		if(getIllegalMove().equals(""))
			return getPlayerName() + " attack_transfer " + fromRegion.getId() + " " + toRegion.getId() + " " + armies;
		else
			return getPlayerName() + " illegal_move " + getIllegalMove();
	}

}
