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

public class Move {
	
	private String playerName; //name of the player that did this move
	private String illegalMove = ""; //gets the value of the error message if move is illegal, else remains empty
	
	/**
	 * @param playerName Sets the name of the Player that this Move belongs to
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	/**
	 * @param illegalMove Sets the error message of this move. Only set this if the Move is illegal.
	 */
	public void setIllegalMove(String illegalMove) {
		this.illegalMove = illegalMove;
	}
	
	/**
	 * @return The Player's name that this Move belongs to
	 */
	public String getPlayerName() {
		return playerName;
	}
	
	/**
	 * @return The error message of this Move
	 */
	public String getIllegalMove() {
		return illegalMove;
	}

}
