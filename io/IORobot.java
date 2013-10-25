package io;

import java.io.IOException;
import java.util.ArrayList;

import main.Region;
import main.Robot;
import move.Move;

public class IORobot implements Robot
{
	IOHandler handler;
	String dump;
	
	public IORobot(String command) throws IOException
	{
		handler = new IOHandler(command);
		dump = "";
	}
	
	@Override
	public void setup(long timeOut)
	{
		//test
		//System.out.println(handler.readLine(timeOut) + " read1");
	}
	
	@Override
	public void writeMove(Move move) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getPreferredStartingArmies(long timeOut, ArrayList<Region> pickableRegions)
	{
		String output = "pick_starting_regions " + timeOut;
		for(Region region : pickableRegions)
			output = output.concat(" " + region.getId());
		
		handler.writeLine(output);
		String line = handler.readLine(timeOut);
		dump += output + '/n';
		dump += line + '/n';
		return line;
	}
	
	@Override
	public String getPlaceArmiesMoves(long timeOut)
	{
		return getMoves("place_armies", timeOut);
	}
	
	@Override
	public String getAttackTransferMoves(long timeOut)
	{
		return getMoves("attack/transfer", timeOut);
	}
	
	private String getMoves(String moveType, long timeOut)
	{
		handler.writeLine("go " + moveType + " " + timeOut);
		dump += "go " + moveType + " " + timeOut + '/n';
		
		String line = "";
		long timeStart = System.currentTimeMillis();
		while(line.length() < 1)
		{
			long timeNow = System.currentTimeMillis();
			long timeElapsed = timeNow - timeStart;
			line = handler.readLine(timeOut); //timeOut werkt niet in inStream??? daarom timeout hier.
			dump += line + '/n';
			if(timeElapsed >= timeOut)
				break;
		}
		if(line.equals("No moves")) //moet algemener
			return "";

		// System.out.println("read: " + line);
		return line;
	}
	
	@Override
	public void writeInfo(String info){
		handler.writeLine(info);
		dump += info + '/n';
		//System.out.println(info);
		//System.out.println("readInfo: " + handler.readLine(300));
		/*String[] test = info.split(" ");
		if(test[0].equals("update_map"))
		{
			System.out.println(handler.getStderr());
			handler.err.buffer.delete(0, handler.err.buffer.length());
		}*/
	}
	
	public void finish() {
		handler.stop();
	}
	
	public String getStdin() {
		return handler.getStdin();
	}
	
	public String getStdout() {
		return handler.getStdout();
	}
	
	public String getStderr() {
		return handler.getStderr();
	}

	public String getDump() {
		return dump;
	}

}
