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

import move.Move;

public interface Robot {
	
	public void setup(long timeOut);
	
	public void writeMove(Move move);
	
	public String getPreferredStartingArmies(long timeOut, ArrayList<Region> pickableRegions);
	
	public String getPlaceArmiesMoves(long timeOut);
	
	public String getAttackTransferMoves(long timeOut);
	
	public void writeInfo(String info);

	public void addToDump(String dumpy);

}
