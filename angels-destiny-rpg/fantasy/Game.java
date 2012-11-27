package fantasy;
/*
Copyright (C) 2012 Johan Ceuppens

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
import java.awt.*;
import javax.swing.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.util.*;
import java.util.Random;

public class Game extends JPanel implements ActionListener {
    String prefix = "./pics/";
    Dimension d;
    Font smallfont = new Font("Helvetica", Font.BOLD, 14);
    Font font2 = new Font("Serif", Font.PLAIN, 12);
    private int level = 1;
    double mapy = 0;
    double mapx = 0;
    double activationPixel = 0;
    int levelnumber = 1;
    private int SCREENWIDTH = 320;
    private int SCREENHEIGHT = 200;
    private int spritecount = 3;
    short screendata;
    Timer timer;

    String displaylanguage = "english";

    private Random rng = new Random();

    private LinkedList buildings = new LinkedList();
    private LinkedList nonplayercharacters = new LinkedList();
 
    private int numberofmonsters = 2; /////Random().nextInt(3)+1;
    private LinkedList monsters = new LinkedList();
    private MonsterDatabase monsterdatabase = new MonsterDatabaseLevel1();

    Player player = new Player(100,100);
    int playerindex = -1;//NOTE!
    int numberofplayercharacters = 1;//start the game with 1 player character
    Map map = new Map(0,0,640,640, new ImageIcon(prefix+"map-1024x1024-1.png").getImage(), 0, 0);

    FantasyBattleWidget battlewidget = new FantasyBattleWidget(0,200-64);
    int handcursordrawoffset = 20;//draw hand -20 pixels to the left
    int handcursormonsteroffset = 48;//hand jump offset between monsters on battle screen
    FantasyHandCursorWidget handcursorwidget = new FantasyHandCursorWidget(96,96);
    FantasyTalkWidget talkwidget = new FantasyTalkWidget(0,0);
    String currenttalktext = "";
    int currenttalktextindex = -1;
    int currenttalktextmax = 0;
    FantasyAskWidget askwidget = new FantasyAskWidget(0,0);
    AskWordDatabase askworddatabase = new AskWordDatabase();

    boolean askmode = false;

    boolean talk = false;
    boolean collidedwithnonplayercharacter = false;
    boolean gameover = false;
    boolean showintro = false;
    LinkedList introstrings = new LinkedList();
	//battle screen is on 
    boolean battle = false;//NOTE! false to start game
	//a battle phase is being displayed
    boolean battlegoingon = false;
    boolean chooseattackmode = false;
    boolean choosetalkmode = false;
    boolean attack = false;
 
    private int battlegridmonstertoattackx = 0;
    private int battlegridmonstertoattacky = 0;
    private String battlegridmonstertoattackname = "";
    BattleGrid battlegrid = new BattleGrid(6);

    public Game() {

	/// intro string show setup
	introstrings.add("The land of Aricea was at peace");
	introstrings.add("For a long time...");
	introstrings.add("Celestia's denizens watched the lands");
	introstrings.add("But another power rose on another plane");
	introstrings.add("A mighty demon king arose...");
	introstrings.add("The battle between good and evil started..");
	introstrings.add("and Aricea was invaded..");
	introstrings.add("But there is still a light in the dark..");
	introstrings.add("a knighthood does not come easy...");

        addKeyListener(new TAdapter());
        setFocusable(true);
	
        d = new Dimension(800, 600);
        setBackground(Color.black);
        setDoubleBuffered(true);
        timer = new Timer(40, this);
        timer.start();

	buildings.add(new Building(0,0,100,100,new ImageIcon(prefix+"wallrock-100x100-1.png").getImage())); //FIXME
	nonplayercharacters.add(new ElfGreen(200,0,displaylanguage)); //FIXME
	//monsters.add(new Slime(48,96)); //FIXME


	//delete, for starting battle mode
				int randomnumber2 = rng.nextInt(4);
				numberofmonsters = randomnumber2 + 1;
				
				int number;
				for (number = 0; number < numberofmonsters; number++) {
					int randomnumber3 = rng.nextInt(monsterdatabase.size());
					if (level == 1) 
						addMonsterLevel1(randomnumber3, number);
				}
    }

    public void addNotify() {
        super.addNotify();
        GameInit();
    }

    public void addMonsterLevel1(int index, int numberofmonster) {
	String monstername = monsterdatabase.getMonsterName(index);
	int hp = monsterdatabase.getMonsterHitpoints(index);
	//NOTE! different moduli
	if (monstername == "slime") {
		monsters.add(new Slime(48+(numberofmonster%3)*48,48+(numberofmonster%2)*48));

		//the attackx and y get set to the last monster generated for 
		//e.g. automatic battle play

		battlegridmonstertoattackx = 1 + numberofmonster % 3;
		battlegridmonstertoattacky = 1 + numberofmonster % 2;

		//sets slime monster on x and y

		battlegrid.set(battlegridmonstertoattackx-1, battlegridmonstertoattacky-1, "slime", hp);//NOTE! -1 

		//sets hand cursor to this slime monster

		battlewidget.sethandx(battlegridmonstertoattackx*48-handcursordrawoffset);
		battlewidget.sethandy(battlegridmonstertoattacky*48);
	} else {
		monsters.add(new Slime(48+(numberofmonster%4)*48,48+(numberofmonster%2)*48));
		battlegridmonstertoattackx = 1 + numberofmonster % 4;
		battlegridmonstertoattacky = 1 + numberofmonster % 2;
		battlegrid.set(battlegridmonstertoattackx-1, battlegridmonstertoattacky-1, "slime", hp);
		battlewidget.sethandx(battlegridmonstertoattackx*48-handcursordrawoffset);
		battlewidget.sethandy(battlegridmonstertoattacky*48);
	}

    }
    
    public void DrawMap(Graphics2D g2d) {
        g2d.drawImage(map.getImage(), map.getx(), map.gety(), this);
    }

    public void DrawBuildingsOnMap(Graphics2D g2d)
    {
	int i;
	for ( i = 0; i < buildings.size(); i++) {

		Object o = buildings.get(i);
		Building b = (Building)o; 
        	g2d.drawImage((Image)b.getImage(), b.getx()+map.getx(), b.gety()+map.gety(), this);
		

	}

     }


/*
 * drawing talk
 */

    public void DrawAskLearnedWords(Graphics g2d) {
      
	g2d.setColor(Color.white);
    	Font fontfoo = new Font("Serif", Font.PLAIN, 17);
        g2d.setFont(fontfoo);


	int i;
	for (i = 0; i < askworddatabase.learnedwordsize(); i++) {

		g2d.drawString(askworddatabase.getLearnedWord(i), 20, (i+1)*21);
	}
    }

    public void DrawAskBackgroundWidget(Graphics g2d) {
	g2d.drawImage(askwidget.getBackgroundImage(), 0, 0, this);//FIXME fixed size
    }

    public void DrawTalkBackgroundWidget(Graphics g2d) {
	g2d.drawImage(talkwidget.getBackgroundImage(), 0, 0, this);//FIXME fixed size
    }

    public void DrawTalkListWidget(Graphics g2d) {
	g2d.drawImage(talkwidget.getListImage(), 48, 200-64, this);//FIXME fixed size
    }

    public void DrawTalkWidget(Graphics g2d) {
	g2d.drawImage(talkwidget.getImage(), 48, 200-64, this);
    }

    public void DrawTalkWidgetHandCursor(Graphics g2d) {
	g2d.drawImage(talkwidget.getHandImage(), talkwidget.gethandx(), talkwidget.gethandy()+210, this);//FIXME +210 fixed size talkwidget offsets
    }


/*
 * drawing battles 
 */ 

    public void DrawBattleMonsterHandCursor(Graphics g2d) {
	g2d.drawImage(handcursorwidget.getImage(), handcursorwidget.getx()-handcursordrawoffset, handcursorwidget.gety(), this);
    }

    public void DrawBattleWidget(Graphics g2d) {
	g2d.drawImage(battlewidget.getImage(), battlewidget.getx(), battlewidget.gety(), this);
    }

    public void DrawBattleHitpointsWidget(Graphics g2d) {
	g2d.drawImage(battlewidget.getHitpointsImage(), battlewidget.getx()+200, battlewidget.gety()+10, this);
	int i;
        for (i = 0; i < numberofplayercharacters; i++) {
      		g2d.setColor(Color.white);
    		g2d.drawString("" + player.getPlayerName(i) + "  " + player.getPlayerHitpoints(i) + "/" + player.getPlayerMaxHitpoints(i), battlewidget.getx()+210, battlewidget.gety()+30);
	}
    }

    public void DrawBattleWidgetHandCursor(Graphics g2d) {
	g2d.drawImage(battlewidget.getHandImage(), battlewidget.gethandx(), battlewidget.gethandy(), this);
    }

    public void DrawBattleWidgetList(Graphics g2d) {
	g2d.drawImage(battlewidget.getListImage(), battlewidget.getx(), battlewidget.gety(), this);
    }

    public void DrawBattleStage(Graphics g2d) {
	//draw battle stage in combination with map environment
	g2d.drawImage(new ImageIcon(prefix+"battlestage-1.png").getImage(), 0, 0, this);
    }

    public void DrawBattlePlayer(Graphics g2d, int dx, int dy) {
	//draw battle stage in combination with map environment
	g2d.drawImage(player.getLeftImage(2), 250+dx, 96+dy, this);
    }

    public void DrawBattleMonsters(Graphics2D g2d)
    {
	int i;
	for ( i = 0; i < monsters.size(); i++) {

		Object o = monsters.get(i);
		Monster m = (Monster)o; 
        	g2d.drawImage((Image)m.getImage(), m.getx(), m.gety(), this);
		

	}

     }
/*
 * drawing map game entities
 */

    public void DrawPlayer(Graphics2D g2d) {
       	g2d.drawImage(player.getImage(), player.getx(), player.gety(), this);
    }

    public void DrawNonPlayerCharacters(Graphics2D g2d)
    {
	int i;
	for ( i = 0; i < nonplayercharacters.size(); i++) {

		Object o = nonplayercharacters.get(i);
		NonPlayerCharacter npc = (NonPlayerCharacter)o; 
        	g2d.drawImage((Image)npc.getImage(), npc.getx()+map.getx(), npc.gety()+map.gety(), this);
		

	}

     }

/*
 * Collision detection code
 */

    public boolean collision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
	if (x1 > x2 && y1 > y2 && x1 < x2 + w2 && y1 < y2 + h2)//FIXME
		return true;
	else
		return false;
    }

    public boolean CollideBuildings()
    {
	int i;
	boolean collide = false;

	for ( i = 0; i < buildings.size(); i++) {

		Object o = buildings.get(i);
		Building b = (Building)o; 
		
		collide = collision(player.getx(), player.gety(), 32,32, b.getx()+map.getx(), b.gety()+map.gety(), b.getw(), b.geth()); //FIXME fixed width&height of player
		if (collide) 
			return collide;
	}

	return collide;
     }

    public boolean CollideNonPlayerCharacters()
    {
	int i;
	boolean collide = false;

	for ( i = 0; i < nonplayercharacters.size(); i++) {

		Object o = nonplayercharacters.get(i);
		NonPlayerCharacter b = (NonPlayerCharacter)o; 
		
		collide = collision(player.getx(), player.gety(), 32,32, b.getx()+map.getx(), b.gety()+map.gety(), b.getw(), b.geth()); //FIXME fixed width&height of player

		currenttalktext = b.talkto();
		currenttalktextmax = b.talktomaxindex();

		if (collide) 
			return collide;
	}

	return collide;
     }

    public void GameInit() {
        LevelInit();
    }


    public void LevelInit() {
//        int i;
//        for (i = 0; i < nrofblocks * nrofblocks; i++)
//            screendata[i] = leveldata[i];
//
    }


    public void GetImages()
    {
    /**********  
      explosion1 = new ImageIcon(prefix+"explosion48x35-1.png").getImage();//prefix+"ghost.png")).getImage();
      tile1 = new ImageIcon(prefix+"tile60x60-1.png").getImage();//prefix+"ghost.png")).getImage();
      tile2 = new ImageIcon(prefix+"tile60x60-2.png").getImage();//prefix+"ghost.png")).getImage();
      bg1 = new ImageIcon(prefix+"bg800x1200-1.png").getImage();//prefix+"ghost.png")).getImage();
      //bg2 = new ImageIcon(prefix+"bg1200x800-2.png").getImage();//prefix+"ghost.png")).getImage();
      bg2 = new ImageIcon(prefix+"bg4000x600-3.png").getImage();//prefix+"ghost.png")).getImage();
      bg3 = new ImageIcon(prefix+"bg4000x600-4.png").getImage();//prefix+"ghost.png")).getImage();
      //player = new ImageIcon(Board.class.getResource("/Users/roguelord/java/2d/shooter/pics/ghost.png")).getImage();//prefix+"ghost.png")).getImage();
****************/
    }

	
    public void paint(Graphics g)
    {
      Graphics2D g2d = (Graphics2D) g;
	//FIXME use some return and else statements for speed
	if (gameover) {
      		Image gameoverimage = new ImageIcon(prefix+"gameoverscreen.png").getImage();
    		Font fontfoo = new Font("Serif", Font.PLAIN, 20);
        	g2d.setFont(fontfoo);
      		g2d.setColor(Color.white);
		
        	g2d.fillRect(0, 0, SCREENWIDTH, SCREENHEIGHT);
        	g2d.drawImage(gameoverimage, 0,0, this);
		String str = "The demon king prevails!";
       		g2d.drawString(str, 10,200);
		repaint();
		return;
	}

	if (showintro) {
		float DELTA = -0.01f;
		float alpha = 1f;

		setOpaque(true);
		setBackground(Color.black);
        	g2d.setFont(font2);
		int i = 0;

		for (i = 0; i < introstrings.size(); i++) {
			alpha = 1f;
			for ( ; alpha >= 0; alpha += DELTA) {
			
        			g2d.fillRect(0, 0, SCREENWIDTH, SCREENHEIGHT);
        			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        			g2d.setPaint(Color.white);

				Object o = introstrings.get(i);
				String introstr = (String)o;
        			g2d.drawString(introstr, 10,100);
				repaint();
			}
			/**try {
				Thread.currentThread().sleep(700);//FIXME battle timer wait!
			}
			catch(InterruptedException ie){}
			**/
		}
	}

      g2d.setColor(Color.black);
      g2d.fillRect(0, 0, d.width, d.height);

	//map screen
      if (!battle) {
	DrawMap(g2d);	
	DrawBuildingsOnMap(g2d);	
	DrawNonPlayerCharacters(g2d);	
	DrawPlayer(g2d);
	if (talk && collidedwithnonplayercharacter) {

		if (!askmode) {
			DrawTalkBackgroundWidget(g2d);
			if (choosetalkmode) {

				DrawTalkWidget(g2d);
				DrawTalkListWidget(g2d);
			
				DrawTalkWidgetHandCursor(g2d);	

			}
	
		}

		if (askmode) {

			DrawAskBackgroundWidget(g2d);
			DrawAskLearnedWords(g2d);

		}

		if (!choosetalkmode && currenttalktextmax-1 == currenttalktextindex) {	
			DrawTalkWidget(g2d);
			DrawTalkListWidget(g2d);
			//currenttalktextindex = -1;

			choosetalkmode = true;
		}

		if (!askmode) {
      			g2d.setColor(Color.white);
    			Font fontfoo = new Font("Serif", Font.PLAIN, 17);
        		g2d.setFont(fontfoo);

			g2d.drawString(currenttalktext,20, 20);
		}
	}
      } else if (battle) {//battle screen
	DrawBattleStage(g2d);
	DrawBattleMonsters(g2d);
	DrawBattlePlayer(g2d,0,0);
	DrawBattleWidget(g2d);
	DrawBattleHitpointsWidget(g2d);
	if (!battlegoingon) {
		if (chooseattackmode) {
			battlewidget.sethandx(battlewidget.getx());//redundant
			battlewidget.sethandy(battlewidget.gety());
			DrawBattleWidgetList(g2d);
			DrawBattleWidgetHandCursor(g2d);
		} else if (!chooseattackmode) {
			DrawBattleMonsterHandCursor(g2d);
		}	
	} else if (battlegoingon) {
            int monsterindex;
	    for (monsterindex = 0; monsterindex < battlegrid.getsizex()*battlegrid.getsizey(); monsterindex++) {
		int randomnumber = rng.nextInt(60);
		if (randomnumber == 0) {	//monster attacks first
			int randomnumber2 = rng.nextInt(numberofmonsters);
			
			int gridxx, gridyy;

			for (gridyy = 0; gridyy < battlegrid.getsizey(); gridyy++) {	
				for (gridxx = 0; gridxx < battlegrid.getsizex(); gridxx++) {

					if (randomnumber2 == gridxx + gridyy*battlegrid.getsizex()) {

						String monstername = battlegrid.get(gridxx,gridyy);
						if (monstername != "none") {
						
							String str = DoMonsterAttack(gridxx, gridyy);
						        g2d.setColor(Color.white);
        						g2d.setFont(smallfont);
        						g2d.drawString(str, player.getx(), player.gety());
							try {
								Thread.currentThread().sleep(1000);//FIXME battle timer wait!
							}
							catch(InterruptedException ie){}
							
						}

					}

							

					}
				}
			}
		}

		//walk through all charcters to let them attack

        	int i;
        	for (i = 0; i < numberofplayercharacters; i++) {
			//leveraging player towards left before he/she fights
			int j;
			for (j = 0; j < 48; j+=2) {
      				g2d.setColor(Color.white);
      				g2d.fillRect(0, 0, d.width, d.height);

				DrawBattlePlayer(g2d,-j,0);
				try {
					Thread.currentThread().sleep(20);//FIXME battle timer wait!
				} catch(InterruptedException ie){}
				
      				Toolkit.getDefaultToolkit().sync();
      				g.dispose();

			}

			String str = DoPlayerAttack(i);

			g2d.setColor(Color.white);
        		g2d.setFont(smallfont);
        		g2d.drawString(str, battlegridmonstertoattackx, battlegridmonstertoattacky);
			try {
				Thread.currentThread().sleep(1000);//FIXME battle timer wait!
			}
			catch(InterruptedException ie){}
			//leveraging player towards left before he/she fights
			for (j = 0; j < 48; j+=2) {
      				g2d.setColor(Color.white);
      				g2d.fillRect(0, 0, d.width, d.height);

				DrawBattlePlayer(g2d,j,0);
				try {
					Thread.currentThread().sleep(20);//FIXME battle timer wait!
				} catch(InterruptedException ie){}
				
      				Toolkit.getDefaultToolkit().sync();
      				g.dispose();
			}
		}

		//let monsters attack
		int gridxx, gridyy;
		for (gridyy = 0; gridyy < battlegrid.getsizey(); gridyy++) {	
			for (gridxx = 0; gridxx < battlegrid.getsizex(); gridxx++) {
			
				int randomnumber4 = rng.nextInt(3);//FIXME use hitchance
				if (randomnumber4 != 0 && randomnumber4 != 1)
					DoMonsterAttack(gridxx, gridyy);
			}
		}

		battlegoingon = false;
		chooseattackmode = false;	
		}		
	}
      
      Toolkit.getDefaultToolkit().sync();
      g.dispose();

    }

    /*
     */

    public String DoPlayerAttack(int index)
    {
	String monstername = battlegrid.get(battlegridmonstertoattackx-1,battlegridmonstertoattacky-1);
	if (monstername == "none") {//no monster selected
		/********int i;
		int randomnumber = rng.nextInt(numberofmonsters);
	
		//skip a few monsters so the attacked monster gets randomized	
		for (i = 0; i < (randomnumber * battlegrid.getsize()); i++)//FIXME fixed size 6
			;

		String s = battlegrid.get(i % battlegrid.getsizex(), i % battlegrid.getsizey());
			if (s == "none")//never reached
				return "none";

		*******/
		int j;
		//for (j = 0; j < monsters.size(); j++) {
			Object o = monsters.get(0);
			Monster mo = (Monster)o;
		
		//	battlegridmonstertoattackx = 1 + mo.getx() / 48;
		//	battlegridmonstertoattacky = 1 + mo.gety() / 48;
		//}
	}
		
	int chancetohit = player.getPlayerHitchance(index);

	int randomnumber = rng.nextInt(chancetohit);
	if (randomnumber == 0)//player fails to hit
		return "Miss!";

	int str = player.getPlayerStrength(index);
	int randomnumber2 = rng.nextInt(str) + 1;
	int die = battlegrid.hit(battlegridmonstertoattackx-1, battlegridmonstertoattacky-1, randomnumber2);
	if (die <= 0) {

		battlegrid.set(battlegridmonstertoattackx-1, battlegridmonstertoattacky-1, "none", 0);//FIXME "0"

		int j;
		for (j = 0; j < monsters.size(); j++) {
			Object o = monsters.get(j);
			//instanceof
			Monster m = (Monster)o;

			System.out.println("m.x= " + m.getx()/48 + " m.y= " + m.gety()/48 + " x=" + battlegridmonstertoattackx +" y=" + battlegridmonstertoattacky);

			if (battlegridmonstertoattackx == m.getx() / 48 && battlegridmonstertoattacky == m.gety() / 48) {
				monsters.remove(j);
				numberofmonsters -= 1;

				if (monsters.size() <= 0) {

					battlegoingon = false;
					chooseattackmode = false;
					attack = false;
					battle = false;
					return "0";
				}
				/**********
				int k;
				for (k = 0; k < monsters.size(); k++) {

					Object o3 = monsters.get(k);
					Monster mo = (Monster)o3;

					battlegridmonstertoattackx = 1 + mo.getx() / 48;	
					battlegridmonstertoattacky = 1 + mo.gety() / 48;	
				
				}
				**********/
				break;
			}
		}
	}
	Object o = monsters.get(0);
	Monster mo = (Monster)o;
		
	battlegridmonstertoattackx = 1 + mo.getx() / 48;
	battlegridmonstertoattacky = 1 + mo.gety() / 48;

	String returnstring = "" + randomnumber2;
	return returnstring;
	
    }	

    public int GetMonsterIndex(String monstername)
    {
	int i;
	for (i = 0; i < monsterdatabase.size(); i++) {
		if (monsterdatabase.getMonsterName(i) == monstername)
			return i;
	}
	return 0;//NOTE! should never be reached
    }

    public String DoMonsterAttack(int xx, int yy) 
    {

	String monstername = battlegrid.get(xx,yy);
	int index = GetMonsterIndex(monstername);
	int chancetohit = monsterdatabase.getMonsterHitchance(index);

	int randomnumber = rng.nextInt(chancetohit);
	if (randomnumber == 0)//monster fails to hit
		return "Miss!";

	int str = monsterdatabase.getMonsterStrength(index);
	int randomnumber2 = rng.nextInt(str) + 1;

	//hit a random player character
	int randomnumber3 = rng.nextInt(numberofplayercharacters);

	player.hit(randomnumber3, randomnumber2);

	int i;
	int hp = 0;
	for (i = 0; i < numberofplayercharacters; i++) {
		hp += player.getPlayerHitpoints(i);
	}

	if (hp <= 0) {
		gameover = true;
		return "0";//FIXME
	}
	String returnstring = "" + randomnumber2;
	return returnstring;
    }

    class TAdapter extends KeyAdapter {
        public void keyReleased(KeyEvent e) {
        	player.settonotmoving();
	} 
      
        public void keyPressed(KeyEvent e) {

          int key = e.getKeyCode();

	if (showintro) {
   		if (key == KeyEvent.VK_ESCAPE) {
			showintro = false;
		}
	}

	if (gameover) {
   		if (key == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
	}


	//do not move if collided
	   if (CollideBuildings()) {
		if (player.getdirection() == "left")
			map.moveleft();
		if (player.getdirection() == "right")
			map.moveright();
		if (player.getdirection() == "up")
			map.moveup();
		if (player.getdirection() == "down")
			map.movedown();
		return;
	   }
	   if (CollideNonPlayerCharacters()) {

		if (player.getdirection() == "left")
			map.moveleft();
		if (player.getdirection() == "right")
			map.moveright();
		if (player.getdirection() == "up")
			map.moveup();
		if (player.getdirection() == "down")
			map.movedown();

		collidedwithnonplayercharacter = true;

		//FIXMEif (!choosetalkmode) {
			return;//FIXME leave this, collidedwithnonplayercharacter gets set in (key == ...) further on
		//}
	   } else {//NOTE!
		if (!choosetalkmode) {//FIXME
			talk = false;
		}
	   }
	   if (!battle) {//map screen

	   	if (key == KeyEvent.VK_LEFT) {

			if (!choosetalkmode) {
				player.settomoving("left");
				map.moveright();
			}
	   	}
	   	if (key == KeyEvent.VK_RIGHT) {
			if (!choosetalkmode) {
				player.settomoving("right");
				map.moveleft();
			}
	   	}
	   	if (key == KeyEvent.VK_UP) {

			if (choosetalkmode) {
				talkwidget.movehandup();
			} else {		
				player.settomoving("up");
				map.movedown();
			}
	   	}
	   	if (key == KeyEvent.VK_DOWN) {
			if (choosetalkmode) {
				talkwidget.movehanddown();
			} else {		
				player.settomoving("down");
				map.moveup();
			}
	   	}
		if (key == KeyEvent.VK_LEFT || 
			key == KeyEvent.VK_RIGHT ||
			key == KeyEvent.VK_UP ||
			key == KeyEvent.VK_DOWN) {

			if (!choosetalkmode) {

				collidedwithnonplayercharacter = false;
				//talk = false;
				//currenttalktext = "";
			}

      			int randomnumber = rng.nextInt(3200);
      			if (randomnumber == 0) {
				battle = true;

				int randomnumber2 = rng.nextInt(4);
				numberofmonsters = randomnumber2 + 1;
				
				int number;
				for (number = 0; number < numberofmonsters; number++) {
					int randomnumber3 = rng.nextInt(monsterdatabase.size());
					if (level == 1) 
						addMonsterLevel1(randomnumber3, number);
				}
				return;//NOTE!
			}
		}	
	   	if (key == KeyEvent.VK_X) {

			if (choosetalkmode) {

				choosetalkmode = false;
				askmode = true;	//FIXME ask learn item

			} else if (!choosetalkmode) {

				if (talk && collidedwithnonplayercharacter) {//FIXMENOTE
					talk = false;
				} else if (collidedwithnonplayercharacter) {
					currenttalktextindex++;
					talk = true;
				}
			}
	   	}
	   	if (key == KeyEvent.VK_Z) {//go back to history of talkmodes

			if (choosetalkmode) {
				choosetalkmode = false;
				currenttalktextindex = -1;
			}

			if (askmode) {
				askmode = false;
			}

		}
	   } else if (battle) {
	   	if (key == KeyEvent.VK_LEFT) {
			if (!chooseattackmode) {
				handcursorwidget.setx(handcursorwidget.getx()-48);//FIXME - monster height
	   		}
		}
	   	if (key == KeyEvent.VK_RIGHT) {
			if (!chooseattackmode) {
				handcursorwidget.setx(handcursorwidget.getx()+48);//FIXME - monster height
	   		}
		}
	   	if (key == KeyEvent.VK_UP) {
			if (!chooseattackmode) {
				handcursorwidget.sety(handcursorwidget.gety()-48);//FIXME - monster height
				battlegridmonstertoattackx = handcursorwidget.getx() % 48 + 1;
				battlegridmonstertoattacky = handcursorwidget.gety() % 48 + 1;
				
	   		} else {
				battlewidget.movehandup();
			}
		}
	   	if (key == KeyEvent.VK_DOWN) {
			if (!chooseattackmode) {
				handcursorwidget.sety(handcursorwidget.gety()+48);//FIXME - monster height
				battlegridmonstertoattackx = handcursorwidget.getx() % 48 + 1;
				battlegridmonstertoattacky = handcursorwidget.gety() % 48 + 1;
	   		} else {
				battlewidget.movehanddown();
			}
	   	}
	   	if (key == KeyEvent.VK_X) {

			if (!chooseattackmode) {
				chooseattackmode = true;
				
			} else if (chooseattackmode) {
				
				switch(battlewidget.getindex()) {
					case 0://Attack
						attack = true;
						battlegoingon = true;
					default://Attack
						attack = true;
						battlegoingon = true;	
				}

			}
			//battlegoingon = true;
			//battlegrid.get(handcursorwidget.getx() % handcursormonsteroffset, handcursorwidget.gety() % handcursormonsteroffset);

			
		}
		
		//flee battle
	   	if (key == KeyEvent.VK_ESCAPE) {
			battle = false;
			chooseattackmode = false;
			battlegoingon = false;
			attack = false;
	   	}
	   }
	}
	}

    public void actionPerformed(ActionEvent e) {
        repaint();  
    }

}
