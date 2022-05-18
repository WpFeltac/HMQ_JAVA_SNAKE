package project_snake;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.LayoutFocusTraversalPolicy;

import java.util.Timer;

import project_snake.Articulation.ArticulationType;

public class Snake 
{	
	public Articulation[] articulations;
	
	public enum Direction
	{
		Up, 
		Down,
		Left,
		Right,
		None
	}
	
	public Direction dir = Direction.None;
	
	public Direction wantedDir = Direction.None;
	
	public float x;
	public float y;
	
	public float spawnX;
	public float spawnY; 
	public int maxArt;

	float addX = 0;
	float addY = 0;
	
	int currentArticulations;
	
	Level level;
	
	//Default constructor with articulations starting at 3 
	public Snake(int maxArticulations, float x, float y, Level level)
	{		
		this.x = x;
		this.y = y;
		
		//Retrieve spawn coordinates
		spawnX = x;
		spawnY = y;
		
		this.maxArt = maxArticulations;
		
		articulations = new Articulation[maxArt];
		
		for(int i = 0; i < 3; i++)
		{
			if(i != 0)
			{
				articulations[i] = new Articulation(i, x, y, ArticulationType.Body);
			}
			else 
			{
				articulations[i] = new Articulation(i, x, y, ArticulationType.Head);
			}
			
			x += 15;		
		}
		
		System.out.println(articulations[0].x + " " + articulations[1].x);
		
		currentArticulations = 3;
		
		this.level = level;
	}
	
	//Constructor for snake with more than 3 articulations
	public Snake(int baseArticulations, int maxArticulations, float x, float y, Level level)
	{		
		this.x = x;
		this.y = y;
		
		articulations = new Articulation[maxArticulations];
		
		for(int i = 0; i < baseArticulations; i++)
		{
			if(i != 0)
			{
				articulations[i] = new Articulation(i, x, y, ArticulationType.Body);
			}
			else 
			{
				articulations[i] = new Articulation(i, x, y, ArticulationType.Head);
			}
			
			x += 15;	
		}
		
		System.out.println(articulations[0].x + " " + articulations[1].x);
		
		currentArticulations = baseArticulations;
		
		this.level = level;
	}
	
	public void DrawSnake(Graphics g)
	{		
		for(int i = 0; i < currentArticulations; i++)
		{
			articulations[i].Draw(g);	
		}
				
	}	
	
	public void Move()
	{		
		//Pas possible de bouger si la touche espace n'a pas été pressée
		if(!isFirstMove)
		{
			//0.25f for decent speed
			switch(dir)
			{
				case Up: addX = 0; addY = -0.25f; break;
				case Down: addX = 0; addY = 0.25f; break;
				case Left: addX = -0.25f; addY = 0; break;
				case Right: addX = 0.25f; addY = 0; break;
				default: addX = 0; addY = 0; break;
			}		
			
			Articulation head = articulations[0];
			
			head.x += addX;
			head.y += addY;
			
			if(wantedDir != dir)
			{
				if(level.IsGridCoord(head.x, head.y))
				{
					dir = wantedDir;
					DirChanged();
				}
			}
			
			for(int i = 1; i < currentArticulations; i++)
			{
				articulations[i].Move();
			}
		}
				
	}
	
	boolean isFirstMove = true;
	
	public void DirChanged() 
	{
		if(isFirstMove)
		{
			for(int i = 1; i < currentArticulations; i++)
			{		
				wantedDir = dir;
				articulations[i].dir = dir;
				isFirstMove = false;
			}
		}
		
		Articulation head = articulations[0];
		
		TurnPoint turnPt = new TurnPoint(head.x, head.y, dir);
		//System.out.println("New TurnPoint created : " + turnPt.x + ", " + turnPt.y + " on direction " + turnPt.newDir + "\n");
		System.out.println("New TurnPoint created with id " + turnPt.id + "\n");
	}
	
	public Articulation GetLastArticulation()
	{
		return articulations[currentArticulations - 1];
	}
	
	public void AddArticulation()
	{		
		switch(dir)
		{
			case Up: 
				AddAfterHead(0, 15);
				break;
			case Down: 
				AddAfterHead(0, -15);
				break;
			case Left: 
				AddAfterHead(15, 0);			
				break;
			case Right: 
				AddAfterHead(-15, 0);
				break;
			default:
				break;
		}
		
		currentArticulations++;		
	}
	
	public void AddAfterHead(int xOffset, int yOffset)
	{
		//Décaler les autres articulations
		for (int i = 1; i < currentArticulations; i++) 
		{
			switch(articulations[i].dir)
			{
				case Up: 
					articulations[i].y += 15;
					break;
				case Down: 
					articulations[i].y -= 15;
					break;
				case Left: 
					articulations[i].x += 15;			
					break;
				case Right: 
					articulations[i].x -= 15;
					break;
				default:
					break;
			}
		}
		
		//Créer la nouvelle articulations entre la tête et le reste du corps
		Articulation head = articulations[0];
		
		Articulation newArt = new Articulation(GetLastArticulation().id + 1, head.x + xOffset, head.y + yOffset, ArticulationType.Body);
		newArt.dir = dir;
		
		TurnPoint latestTurnPoint = Collections.max(TurnPoint.turnPointsList, Comparator.comparing(tp -> tp.id));
		newArt.lastTurnPointIndex = latestTurnPoint.id + 1;
		
		articulations[currentArticulations] = newArt;
		
	}
	
	public void Respawn()
	{
		TurnPoint.Reset();
		
		x = spawnX;
		y = spawnY;
		
		//Keep first 3
		for(int i = 0; i < 3; i++)
		{			
			articulations[i].Respawn();
		}
		
		//Delete all others
		for(int i = 3; i < currentArticulations; i++)
		{			
			articulations[i] = null;
		}
		
		currentArticulations = 3;
		
		isFirstMove = true;
		
	}
}
