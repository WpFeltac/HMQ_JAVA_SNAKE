package project_snake;

import java.util.ArrayList;
import java.util.List;

import project_snake.Snake.Direction;

public class TurnPoint 
{	
	public static List<TurnPoint> turnPointsList = new ArrayList<>();
	public static int count = 0;
	
	public List<Integer> passedOnArtIndexList = new ArrayList<>();
	
	public float x;
	public float y;
	
	public int id;
	
	public Direction newDir;
	
	public TurnPoint(float x, float y, Direction dir) 
	{
		this.x = x;
		this.y = y;
		this.newDir = dir;
		
		this.id = count;
		count++;
		
		turnPointsList.add(this);
		
	}
	
	public void AddPassedOnId(int id)
	{
		passedOnArtIndexList.add(id);
		//System.out.println("Articulation " + id + " passed TurnPoint " + this.id + " on " + x + ", " + y);
	}	
	
	public static void Reset()
	{
		turnPointsList.clear();
		count = 0;
	}
		
}
