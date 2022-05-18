package project_snake;

public class Player 
{
	public static Snake snake;
	
	//Proviennent uniquement des bonus
	public static int points = 0;
	
	public static int nbVies = 3;
	
	//Articulations + points
	public static int score;

	public static String GetScoreStr() 
	{
		return score + "";
	}

	public static void UpdateScore() 
	{
		Player.score = snake.currentArticulations + Player.points;
	}
	
	public static void AddPoints(int points)
	{
		Player.points += points;
	}
}

