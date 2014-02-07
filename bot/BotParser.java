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

package bot;

import java.util.ArrayList;
import java.util.Scanner;

import main.Region;
import move.PlaceArmiesMove;
import move.AttackTransferMove;

public class BotParser {
	
	final Scanner scan;
	
	final Bot bot;
	
	BotState currentState;
	
	public BotParser(Bot bot)
	{
		this.scan = new Scanner(System.in);
		this.bot = bot;
		this.currentState = new BotState();
	}
	
	public void run()
	{
		while(scan.hasNextLine())
		{
			String line = scan.nextLine().trim();
			if(line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if(parts[0].equals("pick_starting_regions")) {
				//pick which regions you want to start with
				currentState.setPickableStartingRegions(parts);
				ArrayList<Region> preferredStartingRegions = bot.getPreferredStartingRegions(currentState, Long.valueOf(parts[1]));
				String output = "";
				for(Region region : preferredStartingRegions)
					output = output.concat(region.getId() + " ");
				
				System.out.println(output);
			} else if(parts.length == 3 && parts[0].equals("go")) {
				//we need to do a move
				String output = "";
				if(parts[1].equals("place_armies")) 
				{
					//place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = bot.getPlaceArmiesMoves(currentState, Long.valueOf(parts[2]));
					for(PlaceArmiesMove move : placeArmiesMoves)
						output = output.concat(move.getString() + ",");
				} 
				else if(parts[1].equals("attack/transfer")) 
				{
					//attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = bot.getAttackTransferMoves(currentState, Long.valueOf(parts[2]));
					for(AttackTransferMove move : attackTransferMoves)
						output = output.concat(move.getString() + ",");
				}
				if(output.length() > 0)
					System.out.println(output);
				else
					System.out.println("No moves");
			} else if(parts.length == 3 && parts[0].equals("settings")) {
				//update settings
				currentState.updateSettings(parts[1], parts[2]);
			} else if(parts[0].equals("setup_map")) {
				//initial full map is given
				currentState.setupMap(parts);
			} else if(parts[0].equals("update_map")) {
				//all visible regions are given
				currentState.updateMap(parts);
			} else {
				System.err.printf("Unable to parse line \"%s\"\n", line);
			}
		}
	}

}
