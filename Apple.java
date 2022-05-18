package project_snake;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Apple 
{
	public static Apple displayedApple;
	
	public int x;
	public int y;
	public Color color;
	
	public Apple(int x, int y, Color color)
	{
		this.x = x;
		this.y = y;
		this.color = color;
	}
	
	public void GetBonus()
	{
		if(color == Color.GREEN)
		{
			//AddArticulation
			Player.snake.AddArticulation();
			Player.UpdateScore();
		}
		
		if(color == Color.BLUE)
		{
			//+10pts
			Player.AddPoints(10);
			Player.UpdateScore();
		}
		
		if(color == Color.RED)
		{
			//+1 vies
			Player.nbVies += 1;
			System.out.println("Bonus obtained!");
		}
	}
	
	public static boolean IsXSpawnedOnObstacle(int x)
	{
		Obstacle obs = Obstacle.obstacleList.stream().filter(obstacle -> x == obstacle.x).findAny().orElse(null);
		
		if(obs != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static boolean IsYSpawnedOnObstacle(int y)
	{
		Obstacle obs = Obstacle.obstacleList.stream().filter(obstacle -> y == obstacle.y).findAny().orElse(null);
		
		if(obs != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
