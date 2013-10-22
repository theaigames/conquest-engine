package main;

import io.IORobot;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.SwingUtilities;

import move.AttackTransferMove;
import move.MoveResult;
import move.PlaceArmiesMove;

import org.bson.types.ObjectId;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

public class RunGame
{
	LinkedList<MoveResult> fullPlayedGame;
	LinkedList<MoveResult> player1PlayedGame;
	LinkedList<MoveResult> player2PlayedGame;
	int gameIndex = 1;

	String playerName1, playerName2;
	String bot1Id, bot2Id;

	DB db;
	
	public RunGame(String gameId, String bot1Id, String bot2Id, String bot1Dir, String bot2Dir) throws IOException
	{
		System.out.println("starting");
		
		Engine engine;
		Map initMap, map;
		Player player1, player2;
		IORobot bot1, bot2;
		int startingArmies;

		db = new MongoClient("localhost", 27017).getDB("test");

		// authentication
		// boolean auth = db.authenticate(<username>,<password>);
		
		//setup the bots
		bot1 = new IORobot("/opt/aigames/scripts/run_bot.sh aiplayer1 " + bot1Dir);
		bot2 = new IORobot("/opt/aigames/scripts/run_bot.sh aiplayer2 " + bot2Dir);
		playerName1 = "player1";
		playerName2 = "player2";
		startingArmies = 5;
		player1 = new Player(playerName1, bot1, startingArmies);
		player2 = new Player(playerName2, bot2, startingArmies);

		//setup the map
		initMap = makeInitMap();
		map = setupMap(initMap);
		
		//start the engine
		engine = new Engine(map, player1, player2);
		
		//send the bots the info they need to start
		bot1.writeInfo("settings your_bot " + player1.getName());
		bot1.writeInfo("settings opponent_bot " + player2.getName());
		bot2.writeInfo("settings your_bot " + player2.getName());
		bot2.writeInfo("settings opponent_bot " + player1.getName());
		sendSetupMapInfo(player1.getBot(), initMap);
		sendSetupMapInfo(player2.getBot(), initMap);
		engine.distributeStartingRegions(); //decide the player's starting regions
		engine.recalculateStartingArmies(); //calculate how much armies the players get at the start of the round (depending on owned SuperRegions)
		engine.sendAllInfo();
		
		//play the game
		while(engine.winningPlayer() == null && engine.getRoundNr() <= 50)
			engine.playRound();

		fullPlayedGame = engine.getFullPlayedGame();
		player1PlayedGame = engine.getPlayer1PlayedGame();
		player2PlayedGame = engine.getPlayer2PlayedGame();
		
		String outputFile = this.writeOutputFile(gameId, engine.winningPlayer());
		this.saveScore(gameId, engine.winningPlayer().getName(), engine.getRoundNr(), outputFile);


	}
	
	public static void main(String args[]) throws IOException
	{	
		final String bot1Dir, bot2Dir, bot1Id, bot2Id, gameId;
		gameId = args[0];
		bot1Id = args[1];
		bot2Id = args[2];
		bot1Dir = args[3];
		bot2Dir = args[4];


		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try { new RunGame(gameId, bot1Id, bot2Id, bot1Dir, bot2Dir); } 
				catch (IOException e) {}
			}
		});
	}

	//tijdelijk handmatig invoeren
	private Map makeInitMap()
	{
		Map map = new Map();
		SuperRegion northAmerica = new SuperRegion(1, 5);
		SuperRegion southAmerica = new SuperRegion(2, 2);
		SuperRegion europe = new SuperRegion(3, 5);
		SuperRegion afrika = new SuperRegion(4, 3);
		SuperRegion azia = new SuperRegion(5, 7);
		SuperRegion australia = new SuperRegion(6, 2);

		Region region1 = new Region(1, northAmerica);
		Region region2 = new Region(2, northAmerica);
		Region region3 = new Region(3, northAmerica);
		Region region4 = new Region(4, northAmerica);
		Region region5 = new Region(5, northAmerica);
		Region region6 = new Region(6, northAmerica);
		Region region7 = new Region(7, northAmerica);
		Region region8 = new Region(8, northAmerica);
		Region region9 = new Region(9, northAmerica);
		Region region10 = new Region(10, southAmerica);
		Region region11 = new Region(11, southAmerica);
		Region region12 = new Region(12, southAmerica);
		Region region13 = new Region(13, southAmerica);
		Region region14 = new Region(14, europe);
		Region region15 = new Region(15, europe);
		Region region16 = new Region(16, europe);
		Region region17 = new Region(17, europe);
		Region region18 = new Region(18, europe);
		Region region19 = new Region(19, europe);
		Region region20 = new Region(20, europe);
		Region region21 = new Region(21, afrika);
		Region region22 = new Region(22, afrika);
		Region region23 = new Region(23, afrika);
		Region region24 = new Region(24, afrika);
		Region region25 = new Region(25, afrika);
		Region region26 = new Region(26, afrika);
		Region region27 = new Region(27, azia);
		Region region28 = new Region(28, azia);
		Region region29 = new Region(29, azia);
		Region region30 = new Region(30, azia);
		Region region31 = new Region(31, azia);
		Region region32 = new Region(32, azia);
		Region region33 = new Region(33, azia);
		Region region34 = new Region(34, azia);
		Region region35 = new Region(35, azia);
		Region region36 = new Region(36, azia);
		Region region37 = new Region(37, azia);
		Region region38 = new Region(38, azia);
		Region region39 = new Region(39, australia);
		Region region40 = new Region(40, australia);
		Region region41 = new Region(41, australia);
		Region region42 = new Region(42, australia);
		
		region1.addNeighbor(region2);
		region1.addNeighbor(region4);
		region1.addNeighbor(region30);
		region2.addNeighbor(region4);
		region2.addNeighbor(region3);
		region2.addNeighbor(region5);
		region3.addNeighbor(region5);
		region3.addNeighbor(region6);
		region3.addNeighbor(region14);
		region4.addNeighbor(region5);
		region4.addNeighbor(region7);
		region5.addNeighbor(region6);
		region5.addNeighbor(region7);
		region5.addNeighbor(region8);
		region6.addNeighbor(region8);
		region7.addNeighbor(region8);
		region7.addNeighbor(region9);
		region8.addNeighbor(region9);
		region9.addNeighbor(region10);
		region10.addNeighbor(region11);
		region10.addNeighbor(region12);
		region11.addNeighbor(region12);
		region11.addNeighbor(region13);
		region12.addNeighbor(region13);
		region12.addNeighbor(region21);
		region14.addNeighbor(region15);
		region14.addNeighbor(region16);
		region15.addNeighbor(region16);
		region15.addNeighbor(region18);
		region15.addNeighbor(region19);
		region16.addNeighbor(region17);
		region17.addNeighbor(region19);
		region17.addNeighbor(region20);
		region17.addNeighbor(region27);
		region17.addNeighbor(region32);
		region17.addNeighbor(region36);
		region18.addNeighbor(region19);
		region18.addNeighbor(region20);
		region18.addNeighbor(region21);
		region19.addNeighbor(region20);
		region20.addNeighbor(region21);
		region20.addNeighbor(region22);
		region20.addNeighbor(region36);
		region21.addNeighbor(region22);
		region21.addNeighbor(region23);
		region21.addNeighbor(region24);
		region22.addNeighbor(region23);
		region22.addNeighbor(region36);
		region23.addNeighbor(region24);
		region23.addNeighbor(region25);
		region23.addNeighbor(region26);
		region23.addNeighbor(region36);
		region24.addNeighbor(region25);
		region25.addNeighbor(region26);
		region27.addNeighbor(region28);
		region27.addNeighbor(region32);
		region27.addNeighbor(region33);
		region28.addNeighbor(region29);
		region28.addNeighbor(region31);
		region28.addNeighbor(region33);
		region28.addNeighbor(region34);
		region29.addNeighbor(region30);
		region29.addNeighbor(region31);
		region30.addNeighbor(region31);
		region30.addNeighbor(region34);
		region30.addNeighbor(region35);
		region31.addNeighbor(region34);
		region32.addNeighbor(region33);
		region32.addNeighbor(region36);
		region32.addNeighbor(region37);
		region33.addNeighbor(region34);
		region33.addNeighbor(region37);
		region33.addNeighbor(region38);
		region34.addNeighbor(region35);
		region36.addNeighbor(region37);
		region37.addNeighbor(region38);
		region38.addNeighbor(region39);
		region39.addNeighbor(region40);
		region39.addNeighbor(region41);
		region40.addNeighbor(region41);
		region40.addNeighbor(region42);
		region41.addNeighbor(region42);

		map.add(region1); map.add(region2); map.add(region3);
		map.add(region4); map.add(region5); map.add(region6);
		map.add(region7); map.add(region8); map.add(region9);
		map.add(region10); map.add(region11); map.add(region12);
		map.add(region13); map.add(region14); map.add(region15);
		map.add(region16); map.add(region17); map.add(region18);
		map.add(region19); map.add(region20); map.add(region21);
		map.add(region22); map.add(region23); map.add(region24);
		map.add(region25); map.add(region26); map.add(region27);
		map.add(region28); map.add(region29); map.add(region30);
		map.add(region31); map.add(region32); map.add(region33);
		map.add(region34); map.add(region35); map.add(region36);
		map.add(region37); map.add(region38); map.add(region39);
		map.add(region40); map.add(region41); map.add(region42);
		map.add(northAmerica);
		map.add(southAmerica);
		map.add(europe);
		map.add(afrika);
		map.add(azia);
		map.add(australia);

		return map;
	}
	
	//Make every region neutral with 2 armies to start with
	private Map setupMap(Map initMap)
	{
		Map map = initMap;
		for(Region region : map.regions)
		{
			region.setPlayerName("neutral");
			region.setArmies(2);
		}
		return map;
	}
	
	private void sendSetupMapInfo(Robot bot, Map initMap)
	{
		String setupSuperRegionsString, setupRegionsString, setupNeighborsString;
		setupSuperRegionsString = getSuperRegionsString(initMap);
		setupRegionsString = getRegionsString(initMap);
		setupNeighborsString = getNeighborsString(initMap);
		
		bot.writeInfo(setupSuperRegionsString);
		System.out.println(setupSuperRegionsString);
		bot.writeInfo(setupRegionsString);
		System.out.println(setupRegionsString);
		bot.writeInfo(setupNeighborsString);
		System.out.println(setupNeighborsString);
	}
	
	private String getSuperRegionsString(Map map)
	{
		String superRegionsString = "setup_map super_regions";
		for(SuperRegion superRegion : map.superRegions)
		{
			int id = superRegion.getId();
			int reward = superRegion.getArmiesReward();
			superRegionsString = superRegionsString.concat(" " + id + " " + reward);
		}
		return superRegionsString;
	}
	
	private String getRegionsString(Map map)
	{
		String regionsString = "setup_map regions";
		for(Region region : map.regions)
		{
			int id = region.getId();
			int superRegionId = region.getSuperRegion().getId();
			regionsString = regionsString.concat(" " + id + " " + superRegionId);
		}
		return regionsString;
	}
	
	//beetje inefficiente methode, maar kan niet sneller wss
	private String getNeighborsString(Map map)
	{
		String neighborsString = "setup_map neighbors";
		ArrayList<Point> doneList = new ArrayList<Point>();
		for(Region region : map.regions)
		{
			int id = region.getId();
			String neighbors = "";
			for(Region neighbor : region.getNeighbors())
			{
				if(checkDoneList(doneList, id, neighbor.getId()))
				{
					neighbors = neighbors.concat("," + neighbor.getId());
					doneList.add(new Point(id,neighbor.getId()));
				}
			}
			if(neighbors.length() != 0)
			{
				neighbors = neighbors.replaceFirst(","," ");
				neighborsString = neighborsString.concat(" " + id + neighbors);
			}
		}
		return neighborsString;
	}
	
	private Boolean checkDoneList(ArrayList<Point> doneList, int regionId, int neighborId)
	{
		for(Point p : doneList)
			if((p.x == regionId && p.y == neighborId) || (p.x == neighborId && p.y == regionId))
				return false;
		return true;
	}
	
	private String writeOutputFile(String gameId, Player winner)
	{
		try {
			//temp
			String fileString = "/home/jim/development/the-ai-games-website/public/games/gameOutputFile" + gameId + ".txt";
			FileWriter fileStream = new FileWriter(fileString);
			BufferedWriter out = new BufferedWriter(fileStream);
			
			writePlayedGame(winner, out, "fullGame");
			writePlayedGame(winner, out, "player1");
			writePlayedGame(winner, out, "player2");
			
			out.close();
			return fileString;
		}
		catch(Exception e) {
			System.err.println("Error on creating output file: " + e.getMessage());
			return null;
		}
	}
	
	private void writePlayedGame(Player winner, BufferedWriter out, String gameView) throws IOException
	{
		LinkedList<MoveResult> playedGame;
		if(gameView.equals("player1"))
			playedGame = player1PlayedGame;
		else if(gameView.equals("player2"))
			playedGame = player2PlayedGame;
		else
			playedGame = fullPlayedGame;
			
		playedGame.removeLast();
		int roundNr = 2;
		out.write("map " + playedGame.getFirst().getMap().getMapString() + "\n");
		out.write("round 1" + "\n");
		for(MoveResult moveResult : playedGame)
		{
			if(moveResult != null)
			{
				if(moveResult.getMove() != null)
				{
					try {
						PlaceArmiesMove plMove = (PlaceArmiesMove) moveResult.getMove();
						out.write(plMove.getString() + "\n");
					}
					catch(Exception e) {
						AttackTransferMove atMove = (AttackTransferMove) moveResult.getMove();
						out.write(atMove.getString() + "\n");
					}
				out.write("map " + moveResult.getMap().getMapString() + "\n");
				}
			}
			else
			{
				out.write("round " + roundNr + "\n");
				roundNr++;
			}
		}
		
		if(winner != null)
			out.write(winner.getName() + " won\n");
		else
			out.write("Nobody won\n");
	}


	/*
	 * MongoDB connection functions
	 */

	// which dump is being used? this one or the one in the python iowrapper??
	public void saveDump() {
		// DBCollection coll = db.getCollection("games");

		// coll.insert(doc)
	}

	public void saveScore(String game_id, String winnerName, int score, String outputFile) {
		DBCollection coll = db.getCollection("games");

		DBObject queryDoc = new BasicDBObject()
			.append("_id", new ObjectId(game_id));

		ObjectId winnerId = new ObjectId();
		if(winnerName.equals(playerName1))
			winnerId = new ObjectId(bot1Id);
		else if (winnerName.equals(playerName2))
			winnerId = new ObjectId(bot2Id);

		DBObject updateDoc = new BasicDBObject()
			.append("$set", new BasicDBObject()
				.append("winner", winnerId)
				.append("score", score)
				.append("output", outputFile)
			);

		coll.findAndModify(queryDoc, updateDoc);

		System.out.print("Game done... winner: " + winnerName + ", score: " + score + ", file: " + outputFile);
	}
}
