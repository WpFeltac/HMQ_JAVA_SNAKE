package project_snake;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.plaf.synth.Region;

import project_snake.Snake.Direction;

public class Level extends JPanel 
{	
	private boolean STOP = false;
	
	//window measures
	private int winH;
	private int winW;
	
	//Taille en cases du terrain sans compter les limites
	int terrainWidth;
	
	//Taille en pixel d'un côté d'une case
	int cellSize;
	
	//Taille max = taille du terrain + 2 pour les bordures le tout fois 20px
	int maxLength;	
	
	int startX;
	int startY;
	
	int maxValueX;
	int maxValueY;
	
	public Snake snake;
	
	boolean moving = false;
	
	//Randomizing apples
	int appleCount;
	
	boolean hasRedApple;
	int redApplePlace;
	
	int blueAppleCount;
	int[] blueApplePlaces;
	int blueAppleIndex;
	String blueApplePlacesStr = "";
	
	//UI Management
	private boolean mustRestart = false;
	private boolean gameOver = false;
	private boolean isDebug;
	
	//Drawing and level init logic
	public Level(int winH, int winW, int terrainWidth, int snakeBase)
	{		
		this.winH = winH;
		this.winW = winW;
		this.terrainWidth = terrainWidth;
		
		//Apple management
		this.appleCount = 0;
		this.hasRedApple = false;
		
		this.blueAppleIndex = 0;
		
		cellSize = 15;	
		
		SetupTerrain();
		
		//Terrain width is also the required amount of articulations needed to win and go to level 2
		//TODO : random and limits-depending spawning
		snake = new Snake(snakeBase, terrainWidth * 2, 290, 225, this);
		
		SetInput();
		SetRandomObstacles();
		
		System.out.println("--- Debug ---");
		System.out.println("Start X = " + startX);
		System.out.println("Start Y = " + startY);
		System.out.println("-------------");
		
		Player.snake = snake;
		
		//Apple randomization		
		RandomizeApples();
		SetNewApple();
	}
	
	public void SetupTerrain()
	{
		//Max length in px of the terrain
		maxLength = ((terrainWidth + 2) * cellSize);
		
		//Start coordinates of the terrain including limits
		startX = winW / 2 - maxLength / 2;
		startY = winH / 2 - maxLength / 2;
		
		//Max coordinates x and y can take
		maxValueX = startX + maxLength;
		maxValueY = startY + maxLength;
	}

	
	//Game loop (logic) and drawing
	protected void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		DrawUI(g);
		DrawTerrain(g);
		
		snake.DrawSnake(g);
		
		//DrawApples
		DrawApple(g, Apple.displayedApple.x, Apple.displayedApple.y, Apple.displayedApple.color);
		
		Update();
		
		//Recalls paintComponent
		repaint();
	}	
	
	private void DrawTerrain(Graphics g)
	{					
		//Drawing limits
		for(int x = startX; x < maxValueX; x += cellSize)
		{
			for(int y = startY; y < maxValueY; y += cellSize)
			{
				if(x == startX || x == maxValueX - cellSize || y == startY || y == maxValueY - cellSize)
				{
					DrawObstacle(g, x, y);					
				}
			}
		}
		
		//Random obstacles
		DrawRandomObstacles(g);
	}
	
	private void DrawObstacle(Graphics g, int x, int y)
	{
		//Drawing obstacle
		g.setColor(Color.GRAY);
		g.fillRect(x, y, cellSize, cellSize);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, cellSize, cellSize);
	}
	
	private void Update()
	{
		SetupTerrain();
		
		if(moving)
		{
			snake.Move();			
			CheckCollisions();
		}
		
		//Switch Level
		if(Player.snake.currentArticulations >= terrainWidth * 2)
		{
			Obstacle.obstacleList.clear();
			terrainWidth += 10;		
			SetRandomObstacles();
			
			appleCount = 0;
			RandomizeApples();
		}
		
		//If lost
		if(Player.nbVies <= 0)
		{
			moving = false;
			mustRestart = true;
			gameOver = true;
		}
	}

	@SuppressWarnings("serial")
	private void SetInput()
	{
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		Action goUp = new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(snake.dir != Direction.Down)
				{
					snake.wantedDir = Direction.Up;
				}
			}
		};
		
		Action goDown = new AbstractAction() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{			
				if(snake.dir != Direction.Up)
				{
					snake.wantedDir = Direction.Down;
				}
			}
		};
		
		Action goLeft = new AbstractAction() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				// TODO Auto-generated method stub
				if(snake.dir != Direction.Right)
				{
					snake.wantedDir = Direction.Left;
				}
			}
		};
		
		Action goRight = new AbstractAction() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				// TODO Auto-generated method stub
				if(snake.dir != Direction.Left)
				{
					snake.wantedDir = Direction.Right;
				}
			}
		};
		
		Action start = new AbstractAction() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				//Start new game
				if(gameOver && !moving && mustRestart)
				{
					terrainWidth = 20;
					Player.points = 0;
					Player.nbVies = 3;
					appleCount = 0;
					
					snake.Respawn();
					moving = true;
					snake.dir = Direction.Left;
					snake.DirChanged();
					
					mustRestart = false;
					gameOver =  false;
					
					RandomizeApples(); 
				}
				//Respawn
				else if(!moving && mustRestart)
				{
					snake.Respawn();
					moving = true;
					snake.dir = Direction.Left;
					snake.DirChanged();
					
					appleCount = 0;
					mustRestart = false;
				}
				//Start for the 1st time
				else if(!moving && !mustRestart)
				{
					moving = true;
					snake.dir = Direction.Left;
					snake.DirChanged();
				}
			}
		};
		
		Action debug = new AbstractAction() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				isDebug = !isDebug;
			}
		};
		
		//Arrows
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0) , "up");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0) , "down");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0) , "left");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0) , "right");
		
		//ZQSD
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0) , "up");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0) , "down");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0) , "left");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0) , "right");
		
		//Start
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0) , "space");
		
		//Debug mode on d
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0) , "dKey");
		
		getActionMap().put("up", goUp);
		getActionMap().put("down", goDown);
		getActionMap().put("left", goLeft);
		getActionMap().put("right", goRight);
		getActionMap().put("space", start);
		getActionMap().put("dKey", debug);
	}
	
	//Check if coordinate is a grid cell
	public boolean IsGridCoord(float x, float y)
	{
		if((x - startX) % 15 == 0 && (y - startY) % 15 == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}	
	
	public void ReduceLife()
	{
		Player.nbVies--;
        moving = false;
        mustRestart = true;
	}
	
	public void CheckCollisions() 
	{
		Articulation head = snake.articulations[0];
		
	    //Body Collisions
	    switch(snake.dir)
	    {
	    	case Up: CheckBodyCollision(head, 0, 15); break;
	    	case Down: CheckBodyCollision(head, 0, -15); break;
	    	case Left: CheckBodyCollision(head, 15, 0); break;
	    	case Right: CheckBodyCollision(head, -15, 0); break;
		default:
			break;
	    }
	    	    		
	    //Border Collisions
	    //Check if head touches left border
	    if(head.x < startX + cellSize) 
	    {
	        ReduceLife();
	    }
	    
	    //Check if head touches right border
	    if(head.x > maxValueX - 2 * cellSize) 
	    {
	    	ReduceLife();
	    }
	    
	    //Check if head touches top border
	    if(head.y < startY + cellSize) 
	    {
	    	ReduceLife();
	    }
	    
	    //Check if head touches bottom border
	    if(head.y > maxValueY - 2 * cellSize) 
	    {
	    	ReduceLife();
	    }
	    
	    //Obstacle collision
	    switch(snake.dir)
	    {
	    	case Up: CheckObstacleCollision(head, 0, 15); break;
	    	case Down: CheckObstacleCollision(head, 0, -15); break;
	    	case Left: CheckObstacleCollision(head, 15, 0); break;
	    	case Right: CheckObstacleCollision(head, -15, 0); break;
		default:
			break;
	    }
	    
	    //Apple collision
	    switch(snake.dir)
	    {
	    	case Up: CheckAppleCollision(head, 0, 15); break;
	    	case Down: CheckAppleCollision(head, 0, -15); break;
	    	case Left: CheckAppleCollision(head, 15, 0); break;
	    	case Right: CheckAppleCollision(head, -15, 0); break;
		default:
			break;
	    }
	}
	
	public void CheckObstacleCollision(Articulation head, float xOffset, float yOffset)
	{
		for(int i = 0; i < Obstacle.obstacleList.size(); i++) 
	    {
	        if(head.x == Obstacle.obstacleList.get(i).x + xOffset && head.y == Obstacle.obstacleList.get(i).y + yOffset) 
	        {
	        	System.out.println("Collision with obstacle " + i);
	        	
	        	ReduceLife();
	        }
	    }
	}
	
	public void CheckBodyCollision(Articulation head, float xOffset, float yOffset)
	{
		for(int i = 3; i < snake.currentArticulations; i++) 
	    {
	        if(head.x == snake.articulations[i].x + xOffset && head.y == snake.articulations[i].y + yOffset) 
	        {
	        	System.out.println("Collision with articulation " + i);

	        	ReduceLife();
	        }
	    }
	}
	
	public void CheckAppleCollision(Articulation head, float xOffset, float yOffset)
	{
		if(head.x == Apple.displayedApple.x + xOffset && head.y == Apple.displayedApple.y + yOffset) 
        {
        	System.out.println("Ate the apple");
        	Apple.displayedApple.GetBonus();
        	SetNewApple();
        }
	}
	
	//Defines random coordinates for obstacles
	public void SetRandomObstacles()
	{
		int maxObstacles;
		
		switch(terrainWidth)
		{
			default: maxObstacles = 0; break;
			case 20 : maxObstacles = 0; break;
			case 30: maxObstacles = 3; break;
			case 40: maxObstacles = 5; break;
		}
		
		Random random = new Random();
		
		for(int i = 0; i < maxObstacles; i++)
		{
			//Set rX coordinate to a grid cell coordinate
		    int rX = random.nextInt(startX + 2 * cellSize, maxValueX - cellSize);
		    
		    while((rX - startX) % 15 != 0)
		    {
		    	rX = random.nextInt(startX + 2 * cellSize, maxValueX - cellSize);
		    }		    
		    	
		    //Set rY coordinate to a grid cell coordinate
		    int rY = random.nextInt(startY + 2 * cellSize, maxValueY - cellSize);
		    
		    while((rY - startY) % 15 != 0)
		    {
		    	rY = random.nextInt(startX + 2 * cellSize, maxValueX - cellSize);
		    }
		    
		    System.out.println("Création d'un obstacle en " + rX + ", " + rY);
		    
		    Obstacle obs = new Obstacle(rX, rY);
		}
		
	}
	
	//Dessin de chaque obstacle dans la liste
	public void DrawRandomObstacles(Graphics g)
	{
		for (Obstacle obs : Obstacle.obstacleList) 
		{
			DrawObstacle(g, obs.x, obs.y);
		}
	}
	
	//Defines random coordinates for apples
	private void SetNewApple()
	{
		Random random = new Random();
		
		//Set rX coordinate to a grid cell coordinate
	    int rX = random.nextInt(startX + 2 * cellSize, maxValueX - cellSize);
	    
	    while((rX - startX) % 15 != 0 && !Apple.IsXSpawnedOnObstacle(rX))
	    {
	    	rX = random.nextInt(startX + 2 * cellSize, maxValueX - cellSize);
	    }		    
	    	
	    //Set rY coordinate to a grid cell coordinate
	    int rY = random.nextInt(startY + 2 * cellSize, maxValueY - cellSize);
	    
	    while((rY - startY) % 15 != 0 && !Apple.IsYSpawnedOnObstacle(rY))
	    {
	    	rY = random.nextInt(startX + 2 * cellSize, maxValueX - cellSize);
	    }
	    
	    //System.out.println("Création d'une pomme en " + rX + ", " + rY);
	    
	    Apple apple = null;
	    
	    if(hasRedApple && appleCount == redApplePlace)
	    {
	    	apple = new Apple(rX, rY, Color.RED);
	    }
	    else if(blueApplePlacesArrayContains(appleCount)) 
	    {
	    	if(appleCount == blueApplePlaces[blueAppleIndex])
	    	{
	    		apple = new Apple(rX, rY, Color.BLUE);
	    		blueAppleIndex++;
	    	}
	    }
	    else
	    {
	    	apple = new Apple(rX, rY, Color.GREEN);
	    }   	    
    
	    Apple.displayedApple = apple;
	    appleCount++;
	}
	
	private void DrawApple(Graphics g, int x, int y, Color color)
	{
		//Drawing apple
		g.setColor(color);
		g.fillOval(x, y, cellSize, cellSize);
		g.setColor(Color.BLACK);
		g.drawOval(x, y, cellSize, cellSize);
		g.drawLine(x + 7, y + 3, x + 7 , y - 3);
	}
	
	private void DrawUI(Graphics g)
	{
		Player.UpdateScore();		
		
		g.drawString("Articulations : " + snake.currentArticulations + " | Points : " + Player.points  + "  | Score : " + Player.GetScoreStr(), 10, 15);
		
		String lvl;
		
		switch(terrainWidth)
		{
			default: lvl = "0"; break;
			case 20 : lvl = "1"; break;
			case 30: lvl = "2"; break;
			case 40: lvl = "3"; break;
		}
		
		PrintText(g, "LEVEL " + lvl, 2);
		
		for(int i = 0, hX = 10; i < Player.nbVies; i++, hX += 15)
		{
			DrawHeart(g, hX, 20, 10, 10);
		}
		
		if(gameOver)
		{
			PrintText(g, "Game Over! Press SPACE to restart the game", 4);
		}		
		else if(mustRestart)
		{
			PrintText(g, "Press SPACE to RESTART", 4);
		}
		
		if(isDebug)
		{
			Font defFont = g.getFont();
			Color defColor = g.getColor();
			
			g.setColor(Color.ORANGE);
			g.setFont(new Font("Consolas", Font.BOLD, 12));
			g.drawString("Apple Spawned : " + appleCount, 10, 45);
			g.drawString("Has Red Apple : " + hasRedApple + (hasRedApple ? " (" + (redApplePlace + 1) + ")" : ""), 10, 60);
			g.drawString("Blue Apples Count : " + blueAppleCount, 10, 75);			
			
			g.drawString("Blue Apple Places : " + blueApplePlacesStr, 10, 90);
			
			g.setFont(defFont);
			g.setColor(defColor);
		}
	}
	
	public void PrintText(Graphics g, String text, int linesFromBottom)
	{
		Font defFont = g.getFont();
		Color defColor = g.getColor();
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("Consolas", Font.PLAIN, 20));
		g.drawString(text, (winW / 2 - (text.length() * 11) / 2), maxValueY + linesFromBottom * cellSize);
		
		g.setFont(defFont);
		g.setColor(defColor);
	}
	
	private void DrawHeart(Graphics g, int x, int y, int width, int height) 
	{
	    int[] triangleX = 
	    	{
	            x - 2*width/18,
	            x + width + 2*width/18,
	            (x - 2*width/18 + x + width + 2*width/18)/2
	        };
	    
	    int[] triangleY = 
	    	{ 
	            y + height - 2*height/3, 
	            y + height - 2*height/3, 
	            y + height 
            };
	    
	    //Top 1
	    g.setColor(Color.BLACK);
	    
	    g.drawOval(x - width/12,
	            y, 
	            width/2 + width/6, 
	            height/2);
	    
	    g.setColor(Color.RED);
	    
	    g.fillOval(
	            x - width/12,
	            y, 
	            width/2 + width/6, 
	            height/2);
	    
	    //Top 2
	    g.setColor(Color.BLACK);
	    
	    g.drawOval(
	    		x + width/2 - width/12,
	            y,
	            width/2 + width/6,
	            height/2);
	    
	    g.setColor(Color.RED);
	    
	    g.fillOval(
	            x + width/2 - width/12,
	            y,
	            width/2 + width/6,
	            height/2);
	    
	    //Bottom    
	    g.setColor(Color.BLACK);
	    g.drawPolygon(triangleX, triangleY, triangleX.length);
	    
	    g.setColor(Color.RED);
	    g.fillPolygon(triangleX, triangleY, triangleX.length);
	}
	
	public void RandomizeApples()
	{
		Random random = new Random();
		
		//Red Apple - 0 to 1 per level
	    int rand = random.nextInt(0, 2);
	    
	    if(rand == 1)
	    {
	    	hasRedApple = true;
	    }
	    else
	    {
	    	hasRedApple = false;
	    }
	    
	    //Max apple count is terrainWidth * 2
	    redApplePlace = random.nextInt(0, terrainWidth * 2);
	    
	    //Blue Apple
	    //0 to 3 for lvl 1
	    //1 to 5 for lvl 2
	    //3 to 10 for lvl 3
	  	switch(terrainWidth)
	  	{
	  		//Lvl 1
	  		case 20:
	  			blueAppleCount = random.nextInt(0, 4);
	  			SetBlueApples(blueAppleCount);
	  			break;
	  		case 30:
	  			blueAppleCount = random.nextInt(1, 6);
	  			SetBlueApples(blueAppleCount);
	  			break;
	  		case 40:
	  			blueAppleCount = random.nextInt(3, 11);
	  			SetBlueApples(blueAppleCount);
	  			break;
	  	}	  	
		
		for(int i = 0; i < blueAppleCount; i++)
		{
			blueApplePlacesStr += (blueApplePlaces[i] + 1) + " | ";
		}
		
	}
	
	private void SetBlueApples(int count)
	{
		blueApplePlaces = new int[count];
		
		Random random = new Random();
		
		for(int i = 0; i < count; i++)
		{
			int possiblePlace = random.nextInt(0, terrainWidth * 2);
			
			while(blueApplePlacesArrayContains(possiblePlace) || blueApplePlaces[i] == redApplePlace)
			{
				possiblePlace = random.nextInt(0, terrainWidth * 2);
			}	
			
			blueApplePlaces[i] = possiblePlace;
		}
	}
	
	private boolean blueApplePlacesArrayContains(int match)
	{
		for(int i = 0; i < blueApplePlaces.length; i++)
		{
			if(blueApplePlaces[i] == match)
			{
				return true;
			}
			else 
			{
				return false;
			}
		}
		
		return false;
	}
	
}
