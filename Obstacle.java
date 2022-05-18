package project_snake;

import java.util.ArrayList;
import java.util.List;

public class Obstacle 
{
	public static List<Obstacle> obstacleList = new ArrayList<>();
	public static int count = 0;
	
	public int x;
	public int y;
	
	public int id;
	
	public Obstacle(int x, int y)
	{
		this.x = x;
		this.y = y;
		
		this.id = count;
		count++;
		
		obstacleList.add(this);
	}
}
