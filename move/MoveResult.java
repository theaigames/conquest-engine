package move;

import main.Map;

public class MoveResult {
	
	private final Move move;
	private final Map map;
	
	public MoveResult(Move move, Map resultingMap)
	{
		this.move = move;
		this.map = resultingMap;
	}
	
	public Move getMove()
	{
		return this.move;
	}
	
	public Map getMap()
	{
		return this.map;
	}

}
