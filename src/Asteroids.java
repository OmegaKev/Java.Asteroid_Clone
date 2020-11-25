import java.awt.Color;
import javax.swing.JFrame;

public class Asteroids
{
	public static void main(String [] Args)
	{
		// Create JPanel
		JFrame frame = new JFrame("J Asteroids");
						
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
		// Create our the main game panel and add it to the frame
		AsteroidGame ag = new AsteroidGame(800, 600);
		ag.setFocusable(true);
		ag.requestFocusInWindow();
		
		frame.add(ag);
		frame.setBackground(Color.BLACK);
		frame.setSize(800,600);
		frame.setVisible(true);
	}
}

	