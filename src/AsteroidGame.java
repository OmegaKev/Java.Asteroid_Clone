import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.Timer;

	//The Main Game Panel for Asteroids
	public class AsteroidGame extends JPanel implements KeyListener, ActionListener
	{
		private static final long serialVersionUID = 1L; // Needed to avoid eclipse warning
		private Graphics2D g2d;
		
		// Private Constants
		private static final int FPS = 1000/64;
		private static final int TITLE_SCREEN = 0;
		private static final int LEVEL_START = 1;
		private static final int GAME = 2;
		private static final int PAUSE = 3;
		
		// Base Boundary
		private static int x_boundary;
		private static int y_boundary; 
		
		private static Random rng = new Random();
		
		// Game Data
		private Player player;		// Holds Player data
		private Level game_level;	// Level data
		private int game_state;		// The Game State
		private Set<Integer> key_pressed = new HashSet<Integer>();
		protected Timer draw_timer;
		
		// Asteroid Variations
		private static final int [][] asteroid_ = {{0,20,15,35,30,13,5,-18,-30,-25,
				  									-48, -43, -20, -15, 8, 8, 20, 10, -13, -33}};
		
		public AsteroidGame(int sw, int sh)
		{
			this.setBackground(Color.black);
			x_boundary = sw;
			y_boundary = sh;
			game_level = new Level(); // Title Screen Asteroids
			player = new Player(g2d, this, 400, 0, 400.0f, 300.0f);
			addKeyListener(this);
			draw_timer = new Timer(FPS, this);
			draw_timer.start();
		}
		
		public static Random getRNG(){return rng;}
		
		public static int [][] getAsteroidType(){return asteroid_;}
		
		public Graphics2D getGraphicHandle(){return g2d;}
		
		public void changeGameState(int state){game_state = state;}
		
		private void printString(Graphics g2d_h, String s, int sc_width, int pos_x, int pos_y)
		{  
            int stringLen = (int) g2d_h.getFontMetrics().getStringBounds(s, g2d).getWidth();  
            int start = sc_width/2 - stringLen/2;  
            g2d_h.drawString(s, start + pos_x, pos_y);  
		}  
		
		public static float getOnLRBounds(float pos_x, float dx)
		{
			// LEFT:RIGHT BOUNDARY
			if(pos_x+dx < 0)return AsteroidGame.x_boundary;
			else if(pos_x+dx > AsteroidGame.x_boundary)return 0;
			else return pos_x;
		}
		
		public static float getOnTBBounds(float pos_y, float dy)
		{
			// LEFT:RIGHT BOUNDARY
			if(pos_y+dy < 0)return AsteroidGame.y_boundary;
			else if(pos_y+dy > AsteroidGame.y_boundary)return 0;
			else return pos_y;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			// On timer fire, get keys and perform necessary actions
			if(game_state == TITLE_SCREEN)
			{
				if(key_pressed.remove(KeyEvent.VK_ENTER))
				{
					game_level = new Level();
					player.reset(3, 400, 300);
					game_state = LEVEL_START;
				}else if(key_pressed.remove(KeyEvent.VK_S))
				{
					// SuperMode Activate
					game_level = new Level(true);
					player.reset(3, 400, 300);
					game_state = LEVEL_START;
				}
				game_level.doOActions();
				
			}else if(game_state == LEVEL_START)
			{
				game_state = GAME;
			}else if(game_state == GAME)
			{
				// Ship Controls -- Arrow Keys/WASD
				if(key_pressed.contains(KeyEvent.VK_UP) || key_pressed.contains(KeyEvent.VK_W))player.thrust();
				else if(key_pressed.contains(KeyEvent.VK_DOWN) || key_pressed.contains(KeyEvent.VK_S))player.brake();
				
				if(key_pressed.contains(KeyEvent.VK_LEFT) || key_pressed.contains(KeyEvent.VK_A))player.turn(-1);
				else if(key_pressed.contains(KeyEvent.VK_RIGHT) || key_pressed.contains(KeyEvent.VK_D))player.turn(1);
				
				if(key_pressed.contains(KeyEvent.VK_SPACE))player.fire(game_level);
				
				if(key_pressed.remove(KeyEvent.VK_ENTER))
				{
					game_state = PAUSE;
				}
				
				// Run Actions
				if(game_level.isGameOver())
				{
					game_level.gameover = false;
					game_state = TITLE_SCREEN;
				}
				game_level.doOActions();
				player.checkForCollision(game_level);
				player.doShipActions(game_level);
			
			}else if(game_state == PAUSE)
			{
				if(key_pressed.remove(KeyEvent.VK_ENTER))
				{
					game_state = GAME;
				}
			}
			
			if(e.getSource() == draw_timer)repaint();
		}
		
		public void StartNewGame()
		{
			game_state = LEVEL_START;
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			
			g2d = (Graphics2D)g;
			player.setGraphicHandle(g2d);
			
			// These states are for the drawing of objects
			// I do Game Logic in timer
			if(game_state == TITLE_SCREEN)
			{
				g2d.setColor(Color.white);
				printString(g2d, "Asteroids", x_boundary, 0, y_boundary/2);
				printString(g2d, "Press Enter for Normal Game", x_boundary, 0, y_boundary/2+100);
				printString(g2d, "Press S for Super Game!", x_boundary, 0, y_boundary/2+140);
				
			
				game_level.drawObjects(g2d);
				player.drawInfo(false);
			}else if(game_state == LEVEL_START)
			{
				// TODO Draw Screen
			}else if(game_state == GAME)
			{
				game_level.drawObjects(g2d);
				player.drawInfo(true);
				player.drawShip(player.x, player.y, player.angle);
			}else if(game_state == PAUSE)
			{
				g2d.setColor(Color.white);
				printString(g2d, "Paused", x_boundary, 0, y_boundary/4);
				game_level.drawObjects(g2d);
				player.drawInfo(true);
				player.drawShip(player.x, player.y, player.angle);
			}
		}
		
		// Holds Player and Ship information
		private class Player
		{
			// Info Variables
			private int score;
			private int lives = 3;
			
			private Graphics2D g2d_h;
			private Container parent_h;
			private Shape ship;						// Used for boundary checks
			private boolean dead;
			private int i_timer;					// Invincibility after respawn
			private static final int i_length = 10*FPS;
			private static final int max_respawn = 15*FPS;
			private int f_respawn = max_respawn;
			private static final int refire_rate = 1*FPS;
			private int refire = 0;
			private final int max_speed = 4;		// Max speed of the ship
			private final int turn_speed = 3;
			private final float accel = 0.1f;		// Acceleration of the ship
			private int info_pos_x = 0;				// X pos for drawing the info panel
			private int info_pos_y = 0;				// Y pos for drawing the info panel
			private float dx;						// X Directional velocity of ship
			private float dy;						// Y Directional velocity of ship
			private float x;						// X position
			private float y;						// Y position
			private double angle;					// Angle of ship
			
			Player(Graphics2D g, Container c, int i_x, int i_y, float p_x, float p_y)
			{
				g2d_h = g;
				parent_h = c;
				info_pos_x = i_x;
				info_pos_y = i_y;
				x = p_x;
				y = p_y;
			}
			
			public void setGraphicHandle(Graphics2D g){g2d_h = g;}
			
			public void setLives(int v){lives = v;}
			
			public void addScore(int points){score += points;}
			
			public boolean isInvincible(){return (i_timer > 0);}
			
			public void reset(int v, int p_x, int p_y)
			{
				score = 0;
				setLives(v);
				x = p_x;
				y = p_y;
				dead = false;
				angle = 0;
			}
			
			public void drawShip(float s_x, float s_y, double s_angle)
			{
				drawShip(s_x, s_y, s_angle, true);
			}
			
			public void drawShip(float s_x, float s_y, double s_angle, boolean solid)
			{
				if(dead && solid)return;
				
				// Create the ship
				GeneralPath ship_gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
				final float [][] sp = {{s_x,    s_x-12, s_x-4, s_x+4, s_x+12},
								 	   {s_y-16, s_y+16, s_y+8, s_y+8, s_y+16}};
				
				ship_gp.moveTo(sp[0][0], sp[1][0]);
				for(int i = 1; i < sp[0].length; i++)ship_gp.lineTo(sp[0][i], sp[1][i]);
				
				ship_gp.closePath();
				
				// Rotate the ship
				AffineTransform at = new AffineTransform();
				at.setToRotation(s_angle - (Math.PI/2), s_x, s_y);
				if(solid)ship = at.createTransformedShape(ship_gp);
				
				// Draw the ship
				g2d_h.setColor(Color.WHITE);
				if(!isInvincible() || !solid)g2d_h.draw(at.createTransformedShape(ship_gp));
				else
				{
					// Create a flicker effect
			       if((i_timer/10)%2 == 0)g2d_h.draw(at.createTransformedShape(ship_gp));
				}
				
			}
			
			public void drawInfo(boolean drawLives)
			{
				// Draw Score
				g2d_h.setColor(Color.white);
				g2d_h.drawString(""+score, this.info_pos_x*(1.0f/5.0f), this.info_pos_y+parent_h.getFont().getSize()+8);
				
				// Draw lives
				if(drawLives)
					for(int i = 0; i < lives; i++)
						drawShip((int)(this.info_pos_x*(1.0f/5.0f))+24*i, this.info_pos_y+(parent_h.getFont().getSize()+8)*2, Math.PI/2, false);
				
			}
			
			public void turn(int s_angle)
			{
				angle += s_angle*(Math.PI/(180/turn_speed));
			}
			
			public void thrust(){thrust(false);}
			
			public void thrust(boolean reverse)
			{
				if(dead)return;
				
				float mx_speed = max_speed;
				float r_spd = -1;
				
				// If reverse thrusters are applied
				if(reverse)
				{
					mx_speed = max_speed/4;
					r_spd = 1;
				}
				
				// Set the dx and dy variables
				final float dx_a = (float)(r_spd*accel*Math.cos(angle));
				final float dy_a = (float)(r_spd*accel*Math.sin(angle));
				final float mx_a = (float)(r_spd*mx_speed*Math.cos(angle));
				final float my_a = (float)(r_spd*mx_speed*Math.sin(angle));
				
				//System.out.println("dy: "+dy_a + ", dx: "+dx_a);
				
				if((mx_a > 0 && dx < mx_a) || (mx_a < 0 && dx > mx_a))dx += dx_a;
				else if(reverse && ((mx_a > 0 && dx > mx_a) || (mx_a < 0 && dx < mx_a)))dx -= dx_a;
				
				if((my_a > 0 && dy < my_a) || (my_a < 0 && dy > my_a))dy += dy_a;
				else if(reverse && ((my_a > 0 && dy > my_a) || (my_a < 0 && dy < my_a)))dy -= dy_a;
				
				//System.out.println("dy: "+dy + ", dx: "+dx);
			}
			
			public void brake()
			{
				thrust(true);
			}
			
			public void fire(Level gl)
			{
				if(refire == 0 && !dead)
				{
					gl.spawnBullet(player, x-(float)(16*Math.cos(angle)), y-(float)(16*Math.sin(angle)), angle);
					refire++;
				}
			}
			
			public void doShipActions(Level gl)
			{
				// Player death and respawn
				if(dead && !gl.gameover)
				{
					if(f_respawn > 0)f_respawn--;
					else
						{
							f_respawn = max_respawn;
							dead = false;
							i_timer = i_length;
						}
					
					return;
				}
				
				// Ship refire rate
				if(refire > 0 && refire < refire_rate)refire++;
				else if(refire >= refire_rate)refire = 0;
				
				if(i_timer > 0)i_timer--;
				move();
			}
			
			public void move()
			{
				x = AsteroidGame.getOnLRBounds(x, dx);
				y = AsteroidGame.getOnTBBounds(y, dy);
				
				x += dx;
				y += dy;
			}
			
			public void killPlayer(Level gl)
			{
				lives--;
				if(lives <= 0)
				{
					gl.f_timer = 0;
					gl.gameover = true;
				}
				
				gl.spawnPlayerDeath(x, y);
				dead = true;
				dx = 0;
				dy = 0;
			}
			
			
			public void checkForCollision(Level gl)
			{
				if(dead || isInvincible())return;
				
				// Asteroid Collision
				for(int i = 0; i < gl.getAsteroidVect().size(); i++)
				{
					//System.out.println(gl.getAsteroidVect().size());
					if(ship.intersects(gl.getAsteroidVect().elementAt(i).ast.getBounds2D()))
						killPlayer(gl);
				}
				
				// Enemy Alien Collision
				for(int i = 0; i < gl.ae_group.size(); i++)
				{
					if(ship.intersects(gl.ae_group.elementAt(i).hull.getBounds2D()))
						killPlayer(gl);
				}
				
				// Enemy Bullet Collision
				for(int i = 0; i < gl.b_group.size(); i++)
				{
					if(gl.b_group.elementAt(i).bp == this)continue;
						
					if(ship.intersects(gl.b_group.elementAt(i).b_shape.getBounds2D()))
						killPlayer(gl);
				}
			}
			
			
		}
		
		private class Level
		{
			private Vector<E_Asteroid> a_group = new Vector<E_Asteroid>();
			private Vector<Alien> ae_group = new Vector<Alien>();
			private Vector<Bullet> b_group = new Vector<Bullet>();
			private Vector<e_explosion> exp_group = new Vector<e_explosion>();
			private p_explosion pe;
			
			
			// Level information
			private boolean supermode = false;
			private int d_level = 0;
			private boolean finish = false;
			private boolean gameover = false;
			private static final int level_delay = 20*FPS;
			private int alien_spawn_timer;
			private int alien_spawn_time = 150*FPS;
			private int f_timer = 0;
			
			Level(){this(false);}
			
			Level(boolean sm)
			{
				supermode = sm;
				startNewLevel(0);
			}
			
			//public void setSuperMode(){supermode = true;}
			
			public void startNewLevel(int diff)
			{
				alien_spawn_timer = 0;
				d_level = diff;
				int num_ast = 4+(d_level*2);
				
				a_group.ensureCapacity(num_ast*5);
				
				for(int i = 0; i < num_ast; i++)
				{
					int [] pos = getSpawnLocationOnBorder();
				  
					a_group.add(new E_Asteroid(d_level, 3, 0, pos[0], pos[1], supermode));
				}
			}
			
			public boolean isLevelFinished()
			{
				return (a_group.size() <= 0 && ae_group.size() <= 0);
			}
			
			public int [] getSpawnLocationOnBorder()
			{
				// Border: North, South, East, West
				final int North = 0;
				final int South = 1;
				final int East = 2;
				final int West = 3;
				
				
				int border = AsteroidGame.getRNG().nextInt(4);
				// xpos = 0, ypos = 1
				int [] pos = {0,0};

				switch(border)
				{
					case North:
						pos[0] = AsteroidGame.getRNG().nextInt(AsteroidGame.x_boundary);
						pos[1] = 0;
						break;
					case South:
						pos[0] = AsteroidGame.getRNG().nextInt(AsteroidGame.x_boundary);
						pos[1] = AsteroidGame.y_boundary;
						break;
					case East:
						pos[0] = AsteroidGame.x_boundary;
						pos[1] = AsteroidGame.getRNG().nextInt(AsteroidGame.y_boundary);
						break;
					case West:
						pos[0] = 0;
						pos[1] = AsteroidGame.getRNG().nextInt(AsteroidGame.y_boundary);
						break;
				}
				
				return pos;
			}
			
			public void drawObjects(Graphics2D g2d)
			{
				// GameOver
				g2d.setColor(Color.WHITE);
				if(gameover)printString(g2d, "Game Over", AsteroidGame.x_boundary, 0, AsteroidGame.y_boundary/2);
				
				// Draw Player explosion if player is dead
				if(pe != null)pe.drawExplosion(g2d);
				
				// Draw explosions
				for(int i = 0; i < exp_group.size(); i++)
					exp_group.elementAt(i).drawExplosion(g2d);
					
				// Draw Asteroids
				for(int i = 0; i < a_group.size(); i++)
					a_group.elementAt(i).drawAsteroid(g2d);
				
				// Draw Aliens
				for(int i = 0; i < ae_group.size(); i++)
					ae_group.elementAt(i).drawAlien(g2d);
				
				// Draw Bullets
				for(int i = 0; i < b_group.size(); i++)
					b_group.elementAt(i).drawBullet(g2d);
				
			}
			
			public void doOActions()
			{
				// Used for logical actions, not drawing actions
				if(alien_spawn_timer >= alien_spawn_time)
				{
					int [] pos = getSpawnLocationOnBorder();
					spawnAlien(pos[0], pos[1]);
					alien_spawn_timer = 0;
				}else if(ae_group.size() < (int)(1+0.25f*d_level) && !isLevelFinished())
							alien_spawn_timer++;
				
				
				// Player explosion timer
				if(pe != null)
				{
					 pe.move();
					 pe.advanceTimer();
				}
				
				// Move enemy based explosion particles
				for(int i = 0; i < exp_group.size(); i++)
				{
					exp_group.elementAt(i).move();
					if(exp_group.elementAt(i).isDead())exp_group.remove(i);
				}
				
				// Move Aliens and check collision
				for(int i = 0; i < ae_group.size(); i++)
				{
					ae_group.elementAt(i).doActions();
					ae_group.elementAt(i).fire(this, player);
					if(ae_group.elementAt(i).checkCollision(b_group))
					{
						player.addScore(1000);
						spawnExplosion(ae_group.elementAt(i).x, ae_group.elementAt(i).y);
						ae_group.remove(i);
						
						if(isLevelFinished())finish = true;
					}
				}
				
				// Move Asteroids and check for bullet collision
				for(int i = 0; i < a_group.size(); i++)
				{
					a_group.elementAt(i).doAction(this, player);;
					if(a_group.elementAt(i).checkForCollision(b_group))
					{
						if(!supermode)player.addScore(a_group.elementAt(i).points);
						else player.addScore(a_group.elementAt(i).points*2);
						
						spawnAsteroids(a_group.elementAt(i).size-1, 2, 
								       a_group.elementAt(i).x, a_group.elementAt(i).y);
						spawnExplosion(a_group.elementAt(i).x, a_group.elementAt(i).y);
						a_group.remove(i);
						
						// Check if level is empty
						if(isLevelFinished())finish = true;
					}
				}
				
				// Move bullets
				for(int i = 0; i < b_group.size(); i++)
				{
					b_group.elementAt(i).move();
					if(b_group.elementAt(i).isDead())b_group.remove(i);
				}
				
				// Advance to next level if conditions are met
				checkLevelFin();
			}
			
			public void checkLevelFin()
			{
				if(finish == false || gameover == true)return;
				
				if(f_timer < level_delay)f_timer++;
				else
				{
					f_timer = 0;
					finish = false;
					d_level++;
					startNewLevel(d_level);
				}
			}
			
			public boolean isGameOver()
			{
				if(!gameover)return false;
				
				if(f_timer < level_delay)
				{
					f_timer++;
					return false;
				
				}else return true;
			
				
			}
		
			public void spawnPlayerDeath(float pos_x, float pos_y)
			{
				pe = new p_explosion(pos_x, pos_y);
			}
			
			public void spawnBullet(Player p, float pos_x, float pos_y, double angle)
			{
				b_group.add(new Bullet(p, pos_x, pos_y, angle));
			}
			
			public void spawnAsteroids(int sz, int n, float x, float y)
			{
				if(sz < 1)return;
				
				for(int i = 0; i < n; i++)a_group.add(new E_Asteroid(d_level, sz, 0, x, y, supermode));
			}
			
			public void spawnAlien(float pos_x, float pos_y)
			{
				ae_group.add(new Alien(pos_x, pos_y));
			}
			
			public void spawnExplosion(float pos_x, float pos_y)
			{
				exp_group.add(new e_explosion(pos_x, pos_y));
			}
			
			public Vector<E_Asteroid> getAsteroidVect(){return a_group;}
		}
		
		private class E_Asteroid
		{
			private int size;
			private int type;
			private boolean dead;
			private Shape ast;
			private int points;
			private Turret sup_t;
			private float x;
			private float y;
			private float dx;
			private float dy;
			
			/*E_Asteroid(int diff, int s, int t, float pos_x, float pos_y)
			{
				this(diff, s, t, pos_x, pos_y, false);
			}*/
			
			E_Asteroid(int diff, int s, int t, float pos_x, float pos_y, boolean sm)
			{
				// Pick an asteroid type
				size = s;
				type = t;
				
				// Set point amount
				points = (int)(Math.pow(2, (3-size))*25);
				
				// Randomize speed
				float dirx = ((AsteroidGame.getRNG().nextInt(2) == 0) ? 1 : -1);
				float diry = ((AsteroidGame.getRNG().nextInt(2) == 0) ? 1 : -1);
				float speed = diff*0.1f;
				
				dx = dirx*(speed+(AsteroidGame.getRNG().nextFloat()/size));
				dy = diry*(speed+(AsteroidGame.getRNG().nextFloat()/size));
				
				// Place Asteroid at position
				x = pos_x;
				y = pos_y;
				
				// Create the shape
				createShape();
				
				// Secret Turret
				if(sm)
				{
					if(AsteroidGame.getRNG().nextInt(4-size) == 0)
						sup_t = new Turret((float)this.ast.getBounds2D().getCenterX(), (float)this.ast.getBounds2D().getCenterY(), size);
				}
				
			}
			
			public void drawAsteroid(Graphics2D g2d_h)
			{
				if(dead)return;
				
				createShape();
				
				// Draw asteroid
				g2d_h.setColor(Color.WHITE);
				g2d_h.draw(ast);
				
				// Super Turret
				if(sup_t != null)sup_t.drawTurret(g2d_h);
			}
			
			public void move()
			{
				x = AsteroidGame.getOnLRBounds(x, dx);
				y = AsteroidGame.getOnTBBounds(y, dy);
				
				x += dx;
				y += dy;
			}
			
			public void doAction(Level gl, Player p)
			{
				move();
				if(sup_t != null)
				{
					sup_t.doAction(p);
					sup_t.move((float)this.ast.getBounds2D().getCenterX(), (float)this.ast.getBounds2D().getCenterY());
					sup_t.fire(gl, p);
				}
			}
			
			public void createShape()
			{
				// Create the asteroid
				GeneralPath ast_gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
				
				// Draw the asteroid at the correct size
				double sz = 0.10+(0.30*size);
				
				ast_gp.moveTo(AsteroidGame.getAsteroidType()[type][0]*sz+x, AsteroidGame.getAsteroidType()[type][10]*sz+y);
				for(int i = 1; i < AsteroidGame.getAsteroidType()[type].length/2; i++)
					ast_gp.lineTo(AsteroidGame.getAsteroidType()[type][i]*sz+x, AsteroidGame.getAsteroidType()[type][10+i]*sz+y);
				
				
				ast_gp.closePath();
				
				// Rotate asteroid
				AffineTransform at = new AffineTransform();
				//at.setToRotation(s_angle - (Math.PI/2), s_x, s_y);
				ast = at.createTransformedShape(ast_gp);
			}
			
			public boolean checkForCollision(Vector <Bullet> bg)
			{
				for(int i = 0; i < bg.size(); i++)
				{
					if(bg.elementAt(i).bp == null)continue;
					
					if(ast.intersects(bg.elementAt(i).b_shape.getBounds2D()))
					{
						bg.remove(i);	// Destroy Bullet
						return true;
					}
				}
				return false;
			}
			
		}
		
		private class p_explosion
		{
			private class debris
			{
				private Line2D line;
				private float dx;
				private float dy;
				private float x_length;
				private float y_length;
				private float x;
				private float y;
				
				debris(float pos_x, float pos_y)
				{
					x = pos_x;
					y = pos_y;
					x_length = pos_x + AsteroidGame.getRNG().nextInt(33)-16;
					y_length = pos_y + AsteroidGame.getRNG().nextInt(33)-16;
					dx = 1-AsteroidGame.getRNG().nextFloat()*2;
					dy = 1-AsteroidGame.getRNG().nextFloat()*2;
					line = new Line2D.Float(pos_x, pos_y, x_length, y_length);
					//System.out.println(pos_x+", "+pos_y+" :"+x_length+", "+y_length);
				}
				
				public void move()
				{
					x += dx;
					y += dy;
					x_length += dx;
					y_length += dy;
					line.setLine(x, y, x_length, y_length);
				}
			}
			
			int f_timer = 10*FPS;
			debris [] d = new debris[5];
			
			p_explosion(float pos_x, float pos_y)
			{
				for(int i = 0; i < d.length; i++)d[i] = new debris(pos_x, pos_y);
			}
			
			public void advanceTimer(){f_timer--;}
			
			public void drawExplosion(Graphics2D g2d_h)
			{
				if(f_timer > 0)
					for(int i = 0; i < d.length; i++)
					{
						g2d_h.setColor(Color.white);
						g2d_h.draw(d[i].line);
					}
			}
			
			public void move()
			{
				if(f_timer > 0)
					for(int i = 0; i < d.length; i++)d[i].move();
			}
		}
		
		public class e_explosion
		{
			private Shape [] exp_p = new Shape[16];
			private float [] x = new float[exp_p.length];
			private float [] y = new float[exp_p.length];
			private float [] dx = new float[exp_p.length];
			private float [] dy = new float[exp_p.length];
			//private static final int speed = 1;
			private static final int max_distance = 200;
			private float dist;
			
			e_explosion(float pos_x, float pos_y)
			{
				double angle = (360/exp_p.length)*(Math.PI/180);
				for(int i = 0; i < exp_p.length; i++)
				{
					x[i] = pos_x;
					y[i] = pos_y;
					dx[i] = (float)(-(AsteroidGame.getRNG().nextFloat()+1)*Math.cos(i*angle));
					dy[i] = (float)(-(AsteroidGame.getRNG().nextFloat()+1)*Math.sin(i*angle));;
					exp_p[i] = new Ellipse2D.Float(x[i], y[i], 1, 1);
				}
			}
			
			public void drawExplosion(Graphics2D g2d_h)
			{
				for(int i = 0; i < exp_p.length; i++)
				{
					g2d_h.setColor(Color.white);
					g2d_h.draw(exp_p[i]);
				}
			}
			
			public void move()
			{
				for(int i = 0; i < exp_p.length; i++)
				{
					x[i] += dx[i];
					y[i] += dy[i];
					((Ellipse2D.Float)exp_p[i]).setFrame(x[i], y[i], 1, 1);
					dist++;
				}
			}
			
			public boolean isDead(){return dist >= max_distance;}
		}
		
		private class Bullet
		{
			private Player bp;
			private static final int sz = 2;
			private static final int speed = 8;
			private Shape b_shape;
			private float x;
			private float y;
			private float dx;
			private float dy;
			private static final int max_distance = 600;
			private float dist;
			
			
			Bullet(Player p, float pos_x, float pos_y, double angle)
			{
				bp = p;
				x = pos_x;
				y = pos_y;
				dx = (float)(-speed*Math.cos(angle));
				dy = (float)(-speed*Math.sin(angle));
				b_shape = new Ellipse2D.Float(x, y, sz, sz);
			}
			
			public void drawBullet(Graphics2D g2d_h)
			{
				g2d_h.setColor(Color.white);
				g2d.draw(b_shape);
			}
			
			public void move()
			{
				x = AsteroidGame.getOnLRBounds(x, dx);
				y = AsteroidGame.getOnTBBounds(y, dy);
				
				x += dx;
				y += dy;
				((Ellipse2D.Float)b_shape).setFrame(x, y, sz, sz);
				dist += speed;
			}
			
			/* NOT USED AT THE MOMENT
			public boolean checkCollision(E_Asteroid asteroid)
			{
				if(b_shape.intersects(asteroid.ast.getBounds2D()))return true;
				
				return false;
			}*/
			//public Player getBulletOwner(){return player_bullet;}
			
			public boolean isDead(){return dist >= max_distance;}
		}
		
		private class Alien
		{
			private Shape hull;
			private static final float speed = 2;
			private static final int dir_choices = 8; // 8 Direction choices evenly spaced
			private static final int choice_wait = 15*FPS;
			private static final int refire_rate = 10*FPS;
			private int refire = refire_rate;
			private int choice_timer;
			private float x;
			private float y;
			private float dx;
			private float dy;
			
			Alien(float pos_x, float pos_y)
			{
				// Possible directions
				x = pos_x;
				y = pos_y;
				
				adjustDirection();
				createShape();
			}
			
			public void createShape()
			{
				GeneralPath hull_gp = new GeneralPath(GeneralPath.WIND_NON_ZERO, 5);
				
				
				final float [][] hp = {{-16, 16, -16, -8, 8},
						               {  0,  0,  0,  -4,-4}};
				
				hull_gp.moveTo(x+hp[0][0], y+hp[1][0]);
				
				for(int i = 0; i < 2; i++)
				{
					hull_gp.curveTo(x+hp[0][i],   y+hp[1][i]-4+(8*i), 
					        		x+hp[0][i+1], y+hp[1][i+1]-4+(8*i), 
					        		x+hp[0][i+1], y+hp[1][i+1]);
				}
				
				hull_gp.moveTo(x+hp[0][3], y+hp[1][3]);
				
				hull_gp.curveTo(x+hp[0][3], y+hp[1][3]-5, 
								x+hp[0][4], y+hp[1][4]-5, 
								x+hp[0][4], y+hp[1][4]);
				
				AffineTransform at = new AffineTransform();
				hull = at.createTransformedShape(hull_gp);
				
			}
			
			public void adjustDirection()
			{
				double angle = AsteroidGame.getRNG().nextInt(dir_choices)*(360/dir_choices)*(Math.PI/180);
				dx = (float)(-speed*Math.cos(angle));
				dy = (float)(-speed*Math.sin(angle));
			}
			
			public void doActions()
			{
				if(choice_timer > choice_wait)
				{
					adjustDirection();
					choice_timer = 0;
				}
				
				choice_timer++;
				refire--;
				move();
				
			}
			
			public void move()
			{
				x = AsteroidGame.getOnLRBounds(x, dx);
				y = AsteroidGame.getOnTBBounds(y, dy);
				
				x += dx;
				y += dy;
				
				// Recreate shape for alien
				createShape();
			}
			
			public void fire(Level gl, Player p)
			{
				if(refire == 0)
				{	
					gl.spawnBullet(null, x, y, Math.atan2(y-p.y, x-p.x));
					refire = refire_rate;
				}
			}
			
			public void drawAlien(Graphics2D g2d_h)
			{
				g2d_h.setColor(Color.white);
				g2d_h.draw(hull);
			}
			
			public boolean checkCollision(Vector<Bullet> bg)
			{
				for(int i = 0; i < bg.size(); i++)
				{
					if(bg.elementAt(i).bp == null)continue;
					
					if(hull.intersects(bg.elementAt(i).b_shape.getBounds2D()))
						return true;
				}
				
				return false;
			}
				
		}
		
		private class Turret
		{
			private static final int refire_rate = 15*FPS;
			private int refire = AsteroidGame.getRNG().nextInt(refire_rate/2)+(refire_rate/2);
			private float noz_len;
			private float x;
			private float y;
			private double angle;
			
			Turret(float pos_x, float pos_y, float sz)
			{
				x = pos_x;
				y = pos_y;
				noz_len = 40*(0.10f+(0.30f*sz));
			}
			
			public void drawTurret(Graphics2D g2d_h)
			{
				GeneralPath t_gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
				final float [][] tp = {{-1, -1,  1, 1},
								 	   { 0, -noz_len, -noz_len, 0}};
				
				t_gp.moveTo(x+tp[0][0], y+tp[1][0]);
				for(int i = 1; i < tp[0].length; i++)
					t_gp.lineTo(x+tp[0][i], y+tp[1][i]);
				
				t_gp.closePath();
				
				AffineTransform at = new AffineTransform();
				at.setToRotation(angle - (Math.PI/2), x, y);
				Shape ts = at.createTransformedShape(t_gp);
					
				g2d_h.setColor(Color.white);
				g2d_h.draw(ts);
			}
			
			public void move(float pos_x, float pos_y)
			{
				x = pos_x;
				y = pos_y;
			}
			
			
			public void fire(Level gl, Player p)
			{
				if(refire == 0)
				{	
					gl.spawnBullet(null, x-(float)((noz_len-4)*Math.cos(angle)), y-(float)((noz_len-4)*Math.sin(angle)), Math.atan2(y-p.y, x-p.x));
					refire = refire_rate;
				}
			}
			
			
			public void doAction(Player p)
			{
				angle = Math.atan2(y-p.y, x-p.x);
				refire--;
			}
		}
		
		// Key Listener events
		public void keyPressed(KeyEvent e)
		{
			key_pressed.add(e.getKeyCode());
		}
		
		public void keyReleased(KeyEvent e)
		{
			key_pressed.remove(e.getKeyCode());
		}
		
		public void keyTyped(KeyEvent e)
		{

		}
		
	}