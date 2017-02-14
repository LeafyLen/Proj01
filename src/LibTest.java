import CustomUtils.Time;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.opengl.ImageIOImageData;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.*;

//import static javax.swing.plaf.metal.MetalBumps.createBuffer;

/**
 *
 *
 * The LibTest class is the main class. 
 * 
 * Within this class sprites are drawn, deleted, and updated.
 * Basically contains everything the game has to offer. 
 * @author Andrey Grebenik (Kuro)
 * @version 1.0
 * @since 2016-12-3
 * */

public class LibTest {
	private static boolean debug =false;
	private static boolean isFullScreen=false;
	//screen width and height controlling variables.
	private static final int W = 800;
	private static final int H = 640;

	private static boolean limit_U = false;
	private static boolean limit_D = false;
	private static boolean limit_L = false;
	private static boolean limit_R = false;
	
	//currently variables to save player position to be removed later.
	private static int x = 200;
	private static int y = 100;

	GLtile[][] grid_T = new GLtile[H/32][W/32];

	public static GLplayer PLAYER;

	//A stopwatch which holds the time since the game started
	private static final Time gameTime=new Time();

	private static int currentUnit = 0;

	private static String curUnit = "";

	private static long lastUpdateTime=0;

	private static ArrayList<GLimage> images = new ArrayList<>();

	private static ArrayList<GLtext> text = new ArrayList<>();

	private static String curLevel = "0-0";

	private static GLtile[][] grid = new GLtile[640/32][800/32];

	private static ArrayList<GLtile> tiles = new ArrayList<>();

	private static ArrayList<String> names = new ArrayList<>();

	private static ArrayList<String> coll = new ArrayList<>();


	/**
	 * start() initializes the GL and then renders, updates, and takes input.
	 * Usually this is where you would put anything you would want to happen every tick.
	 * @throws IOException if invalid path is specified
	 */
	public static void start() throws IOException , CustomUtils.AudioControllerException {
		initGL(W,H);
		init();
		//createImage("whiteBack.png",0,0);
		while (true) {
			long curTime=gameTime.getTime();
			double dt=(curTime-lastUpdateTime)/1000.0;
			lastUpdateTime=curTime;
			glClear(GL_COLOR_BUFFER_BIT);
			RENDER(dt);
			UPDATE(dt);
			INPUT(dt);
			Display.update();
			Display.sync(100);
			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}

		}
	}
 
	/**
	 * Initializes the game and creates the screen on which everything is done.
	 * @param width the height of the screen.
	 * @param height the width of the screen.
	 */
	private static void initGL(int width, int height)
	{
		try 
		{
			Display.setDisplayMode(new DisplayMode(width,height));
			//Display.setFullscreen(true);
			//Display.setResizable(true);
			//Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
			Display.setTitle("Energy Crisis");
			Display.setIcon(new ByteBuffer[] {
					new ImageIOImageData().imageToByteBuffer(ImageIO.read(new File("src/Assets/ico16.png")), false, false, null),
					new ImageIOImageData().imageToByteBuffer(ImageIO.read(new File("src/Assets/ico32.png")), false, false, null)
			});
			Display.setVSyncEnabled(true);
			Display.create();

		} 
		catch (Exception e)
		{
			e.printStackTrace();
			//System.exit(0);
		} 
		glEnable(GL_TEXTURE_2D);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glViewport(0,0,width,height);
		glMatrixMode(GL_MODELVIEW);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
	}

	/**
	 * Ransoms and integer between @param min and @param max
	 * @param min minimum value to random.
	 * @param max maximum value to random.
	 * @return returns a random between @param min and @param max.
	 */
	public static int rRn(int min, int max)
	{
		return (int)(Math.random()*max)+min;
	}
	
	/**
	 * Creates an GLimage at the position [ @param x, @param y ] with the image @param path.
	 * @param path the path of the image for the GLimage.
	 * @param x the x coordinate at which to create the GLimage.
	 * @param y the y coordinate at which to create the GLimage.
	 * @throws IOException if an invalid file path is specified.
	 */
	public static void createImage(String path, int x, int y) throws IOException
	{
		GLimage tex = new GLimage(path,x,y);
		images.add(tex);
	}

	/**
	 * Creates an GLimage at the position [ @param x, @param y ] with the image @param path adding the @param tag to it
	 * @param path the path of the image for the GLimage.
	 * @param x the x coordinate at which to create the GLimage.
	 * @param y the y coordinate at which to create the GLimage.
	 * @param tag the tag to add to the GLimage
	 * @throws IOException if an invalid file path is specified.
	 */
	public static void createImage(String path, int x, int y, String tag) throws IOException
	{
		GLimage tex = new GLimage(path,x,y,tag);
		images.add(tex);
	}

	/**
	 * Creates text with a size of @param size containing the string (s).
	 * @param s the text to be  displayed
	 * @param x the x position of the first letter.
	 * @param y the y position of the first letter.
	 * @param size changes the size of text, minimum is 0
	 * @throws IOException if an invalid path is specified
	 */
	public static void createText(String s, int x, int y, int size) throws IOException
	{
		GLtext tex = new GLtext(s,x,y,size);
		text.add(tex);
	}

	public static void createPlayer(int x, int y, double health, double speed, double rate) throws IOException
	{
		PLAYER = new GLplayer(x,y,health,speed,rate);
	}

	public static void createTile(String img, int x, int y, char use, char type) throws IOException
	{
		GLtile tex = new GLtile("tl-"+img,x,y,use,type);
		tex.tag = img;
		tiles.add(tex);
	}


	/**
	 * This is used to load images into the game before the first update loop.
	 * Things like backgrounds.
	 * @throws FileNotFoundException if a file not found exception occurs during GLimage creation.
	 */
	public static void init() throws IOException, CustomUtils.AudioControllerException {
		try {

			//createText("hello world",0,0,10);
			createPlayer(200,200,100,250,10);
			loadMap("lvl2");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static GLtile findTile(String tag)
	{
		for(GLtile a :tiles)
		{
			if(("tl-"+a.tag).equals(tag))
			{
				return a;
			}
		}
		return null;
	}
	public static GLtile findTile(char tag)
	{
		for(GLtile a :tiles)
		{
			if((""+a.sm).equals(""+tag))
			{
				return a;
			}
		}
		return tiles.get(0);
	}

	public static boolean chkCol()
	{
		boolean k = false;
		int q = 0;
		int w = 0;
		int si = 28;
		int ct = (32-si)/2;

		for(GLtile[] a : grid)
		{
			for (GLtile l : a)
			{
				int x = l.x;
				int y = l.y;
				int px = (int)PLAYER.x+ct;
				int py = (int)PLAYER.y+ct;
				if((l.tp+"").equals("#") && (x<px+si && x+si>px) && (y<py+si && y+si>py))
				{
					String ip = "true:"+l.x+":"+l.y;
					if(coll.contains(ip))
					{
						coll.remove(ip);
					}
					coll.add(ip);

					k = true;

				}
			}
		}
		return k;
	}


	public static void loadMap(String name) throws IOException
	{
		int q = 0;
		int w = 0;
		for(GLtile[] a : grid)
		{
			for(GLtile ignored : a)
			{
				//Tools.p(q+":"+w);
				grid[q][w] = new GLtile("tl-default.png",w*32+50,q*32+50,(char)128,(char)128);
				w++;
			}
			q++;
			w = 0;
		}
		String current = new java.io.File( "." ).getCanonicalPath();
		File folder = new File(current+"/src/Assets/Art/Tiles");
		File[] listOfFiles = folder.listFiles();
		assert listOfFiles!=null;
		for(java.io.File listOfFile : listOfFiles) {
			if(listOfFile.isFile()&&listOfFile.getName().startsWith("tl-")) {
				names.add(listOfFile.getName());
			}
		}
		for(String a:names)
		{
			createTile(a.substring(3),-100,-100,(char)(tiles.size()+33),'*');
		}
		String path = "src\\Assets\\Scenes\\";
		Scanner in = new Scanner(new FileReader(path+name+".map"));
		Scanner in2 = new Scanner(new FileReader(path+name+".col"));
		q = 0;
		w = 0;
		for(GLtile[] a : grid)
		{
			String s = in.nextLine();
			String s2 = in2.nextLine();
			Tools.p("aaaaa >><<>>"+s);
			char[] sp = s.toCharArray();
			char[] sp2 = s2.toCharArray();
			for(GLtile b : a)
			{
				GLtile t = findTile(sp[w]);
				//System.out.println(t);
				b = new GLtile("tl-"+t.tag,w*32,q*32,sp[w],sp2[w]);
				grid[q][w] = b;
				w++;
			}
			w=0;
			q++;
		}

	}
 
	/**
	 * Where you do things to GLimages to make the game function. 
	 * Yes I know it's vague but that is what it is. 
	 * @throws IOException if a file not found exception occurs during GLimage creation.
	 */
	public static void UPDATE(double dt) throws IOException {
		PLAYER.curWeapon.paused =debug;
		
	}
	
	/**
	 * Renders all GLimages in images[] and runs their updates their information.
	 */
	public static void RENDER(double dt) throws IOException , CustomUtils.AudioControllerException
	{


		for(GLimage image : images) {
			image.render();
		}
		for(GLtile[] a : grid)
		{
			for(GLtile l : a)
			{
				l.render();
			}
		}
		for(GLtext aText : text) {
			aText.render();
		}

		PLAYER.render();

	}
 
	/**
	 * You casual main(String [] args) function
	 * Starts the game ...
	 * @param args ... they are args ...
	 * @throws IOException if you suck at specifying file paths...
	 */
	public static void main(String[] args) throws IOException , CustomUtils.AudioControllerException
	{
		gameTime.start();
		start();
	}



	/**
	 * Controls user input.
	 * Mouse and Keyboard.
	 */
	private static void INPUT(double dt) throws IOException , CustomUtils.AudioControllerException
	{

		if (Mouse.isButtonDown(0))
		{

		}
		else if (Mouse.isButtonDown(1)) {

		}

		if(Keyboard.isKeyDown(Keyboard.KEY_1))
		{
			debug = true;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_2))
		{
			debug = false;
		}

		String q = "";

		boolean W = Keyboard.isKeyDown(Keyboard.KEY_W);
		boolean S = Keyboard.isKeyDown(Keyboard.KEY_S);
		boolean A = Keyboard.isKeyDown(Keyboard.KEY_A);
		boolean D = Keyboard.isKeyDown(Keyboard.KEY_D);

		if (W) {
			q = "up";
			PLAYER.facing = q;
		}

		if (S) {
			q = "down";
			PLAYER.facing = q;
		}

		if (A) {
			q = "left";
			PLAYER.facing = q;
		}

		if (D) {
			q = "right";
			PLAYER.facing = q;
		}

		if (W && A)
		{
			q = "upleft";
		}

		if (W && D)
		{
			q = "upright";
		}

		if(S && A)
		{
			q = "downleft";
		}

		if(S && D)
		{
			q = "downright";
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_UP) ||
				Keyboard.isKeyDown(Keyboard.KEY_DOWN) ||
				Keyboard.isKeyDown(Keyboard.KEY_LEFT) ||
				Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
		{
			PLAYER.shooting = true;
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				PLAYER.facing = "up";
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				PLAYER.facing = "down";
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				PLAYER.facing = "left";
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
				PLAYER.facing = "right";
			}
		}
		else
		{
			PLAYER.shooting = false;
		}
		int m = 0;
		boolean s = chkCol();
		if(s)
		{
			for(String aColl : coll) {
				getDir(aColl);
			}
		}
		if (q.equals("up")&&!limit_U) {
			PLAYER.y -= (PLAYER.speed+m)*dt;
		}

		if (q.equals("down")&&!limit_D) {
			PLAYER.y += (PLAYER.speed+m)*dt;
		}

		if (q.equals("left")&&!limit_L) {
			PLAYER.x -= (PLAYER.speed+m)*dt;
		}

		if (q.equals("right")&&!limit_R) {
			PLAYER.x += (PLAYER.speed+m)*dt;
		}

		if (q.equals("upleft")) {
			if(!limit_U)
			{
				PLAYER.y -= (PLAYER.speed+m)*dt;
			}
			if(!limit_L)
			{

				PLAYER.x -= (PLAYER.speed+m)*dt;
			}
		}

		if (q.equals("upright")) {
			if(!limit_U)
			{
				PLAYER.y -= (PLAYER.speed+m)*dt;
			}
			if(!limit_R)
			{
				PLAYER.x += (PLAYER.speed+m)*dt;
			}
		}

		if (q.equals("downleft")) {
			if(!limit_D)
			{
				PLAYER.y += (PLAYER.speed+m)*dt;
			}
			if(!limit_L)
			{

				PLAYER.x -= (PLAYER.speed+m)*dt;
			}
		}

		if (q.equals("downright")) {
			if(!limit_D)
			{
				PLAYER.y += (PLAYER.speed+m)*dt;
			}
			if(!limit_R)
			{
				PLAYER.x += (PLAYER.speed+m)*dt;
			}
		}

		if(!s)
		{
			limit_R = false;
			limit_L = false;
			limit_D = false;
			limit_U = false;
		}
		coll.clear();
	}

	public static int getDir(String spec)
	{
		String[] a = spec.split(":");
		int ox = Integer.parseInt(a[1])+16;
		int oy = Integer.parseInt(a[2])+16;
		int mx = (int)PLAYER.x+16;
		int my = (int)PLAYER.y+16;
		if(ox > mx && (oy>my-16 && oy<my+16))
		{
			limit_R = true;
			return 0;
		}
		if(ox <= mx && (oy>my-16 && oy<my+16))
		{
			limit_L = true;
			return 0;
		}
		if(oy > my && (ox>mx-16 && ox<mx+16))
		{
			limit_D = true;
			return 0;
		}
		if(oy <= my && (ox>mx-16 && ox<mx+16))
		{
			limit_U = true;
			return 0;
		}
		return 0;
	}

	//levels start with 0 and so do areas
	//filename is %area%-%level% example 1-5;
	public static void LOAD(int area, int level)
	{

	}
}
