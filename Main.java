package project_snake;

import javax.swing.JFrame;

public class Main 
{
	private static JFrame _window;
	
	//Main game logic
	public static void main(String[] args) 
	{
		System.out.println("Hello World");
		
		_window = InitWindow();
		
		InitLevel(20, 3);
		
		_window.setVisible(true);
		
	}
	
	public static JFrame InitWindow()
	{		
		JFrame myWindow = new JFrame();
		
		myWindow.setTitle("HMQ Snake");
		myWindow.setSize(700, 750);
		
		//Centrer la fenêtre
		myWindow.setLocationRelativeTo(null);
		myWindow.setResizable(false);
		myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		return myWindow;
	}
	
	//Width in cases of 15px
	public static void InitLevel(int terrainWidth, int snakeBase)
	{		
		//Init Level
		Level lvl = new Level(_window.getHeight(), _window.getWidth(), terrainWidth, snakeBase);
				
		_window.add(lvl);
	}
	
	public static void NewLevel(int terrainWidth, int snakeBase) 
	{
		_window.removeAll();
		
		Level lvl = new Level(_window.getHeight(), _window.getWidth(), terrainWidth, snakeBase);
		
		_window.add(lvl);
	}

}
