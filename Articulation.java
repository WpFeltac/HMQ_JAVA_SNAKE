package project_snake;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import project_snake.Snake.Direction;

public class Articulation 
{
	public int id;
	
	public float x;
	public float y;
	
	public float spawnX;
	public float spawnY;
	
	public float changeX = 0;
	public float changeY = 0;
	
	Direction dir;
	
	float addX = 0;
	float addY = 0;
	
	public enum ArticulationType
	{
		Head,
		Body
	}
	
	ArticulationType type;
	
	int lastTurnPointIndex = 0;
	
	Articulation(int id, float x, float y, ArticulationType type)
	{
		this.id = id;
		
		this.x = x;
		this.y = y;
		
		//Retrieve spawn coordinates
		spawnX = x;
		spawnY = y;
		
		this.type = type;
		
		this.dir = Direction.None;
		
	}
	
	public void Respawn() 
	{
		this.x = spawnX;
		this.y = spawnY;
		
		lastTurnPointIndex = 0;
		
		dir = Direction.None;
	}
	
	public void Draw(Graphics g)
	{
		if(type == ArticulationType.Body)
		{
			DrawCircle(g, (int)x, (int)y, Color.ORANGE);
		}
		else 
		{
			DrawCircle(g, (int)x, (int)y, Color.MAGENTA);
		}
	}
	
	private void DrawCircle(Graphics g, int x, int y, Color color)
	{		
	    g.setColor(Color.BLACK);
	    g.drawOval(x, y, 15, 15);
	    g.setColor(color);
	    g.fillOval(x, y, 15, 15);
	}
	
	public void Move()
	{ 
		switch(dir)
		{
			case Up: addX = 0; addY = -0.25f; break;
			case Down: addX = 0; addY = 0.25f; break;
			case Left: addX = -0.25f; addY = 0; break;
			case Right: addX = 0.25f; addY = 0; break;
			default: addX = 0; addY = 0; break;
		}
		
		x += addX;
		y += addY;
		
		//Find next TurnPoint in order to know when to change direction
		TurnPoint turnPt = TurnPoint.turnPointsList.stream().filter(turnpoint -> lastTurnPointIndex == turnpoint.id).findAny().orElse(null);
		
		if(turnPt != null)
		{
			if(x == turnPt.x && y == turnPt.y)
			{
				this.dir = turnPt.newDir;
				turnPt.AddPassedOnId(id);
				lastTurnPointIndex ++;
			}
		}
	}
}
