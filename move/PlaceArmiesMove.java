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
import main.Region;

/**
 * This Move is used in the first part of each round. It represents what Region is increased
 * with how many armies.
 */

public class PlaceArmiesMove extends Move {
	
	private Region region;
	private int armies;
	
	//geen misbruik maken van playerName aub, alleen je eigen botnaam invullen
	public PlaceArmiesMove(String playerName, Region region, int armies)
	{
		super.setPlayerName(playerName);
		this.region = region;
		this.armies = armies;
	}
	
	/**
	 * @param n Sets the number of armies this move will place on a Region
	 */
	public void setArmies(int n) {
		armies = n;
	}
	
	/**
	 * @return The Region this Move will be placing armies on
	 */
	public Region getRegion() {
		return region;
	}
	
	/**
	 * @return The number of armies this move will place
	 */
	public int getArmies() {
		return armies;
	}
	
	/**
	 * @return A string representation of this Move
	 */
	public String getString() {
		if(getIllegalMove().equals(""))
			return getPlayerName() + " place_armies " + region.getId() + " " + armies;
		else
			return getPlayerName() + " illegal_move " + getIllegalMove();
				
	}
	
}
