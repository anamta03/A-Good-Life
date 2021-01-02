//Anamta Masoodi
//June 14th, 2019
//ICS4U
//MyGdxGame.java
package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import java.lang.Object;
import java.awt.Point;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import java.util.*;

/*INDEX
 * 		MAIN CLASS
 * 		PLAYER CLASS
 * 		ITEM CLASS
 * 		INVENTORY CLASS
 * 		PLANT CLASS
 */

//-----------------MAIN CLASS-----------------
public class MyGdxGame extends ApplicationAdapter {
	//This class draws the actual game, and goes through all the background methods. I used it to initialize all the variables
	//and to determine what screen the player should be at, depending on the user's choices.
	String screen; //used to draw the screen the user should be at
	
	Player player; //the player the user plays with
	SpriteBatch batch; //draws everything
	
	Texture[][] walkingPics = new Texture[4][3]; //each array represents a direction, and contains the frames for that direction
	Texture[][] wateringPics = new Texture[4][5]; //pics for different directions of player watering
	Texture[][] tillingPics = new Texture[4][6]; //pics for diff directions of player tilling the ground
	Texture[][] cuttingPics = new Texture[4][6]; //pics for diff directions of player cutting weeds
	
	LinkedList<Item> items = new LinkedList<Item>(); //items in the inventory
	LinkedList<Item> shopSeeds = new LinkedList<Item>(); //items in the shop (type seeds)
	String[] menuOptions = {"play","help"}; //options for the main menu, which are just the play and help buttons
	Boolean nextOption = true; //used to allow a break between the instructions and controls screens, so that the screen doesn't change from button being pressed once
	
	int shopIndex; //index of the selection of the shop
	int menuIndex; //index of the selection of the main menu
	
	int days = 1; //current days (used to end the game, so when it hits 31st night, then the game ends
	int numShipped = 0; //number of items shipped (used when game is over)
	int numPlanted = 0; //number of seeds planted, including ones that have been destroyed by weeds (used when game is over)
	
	Boolean honeyAvailable = true; //used to  check if honey has been taken from the tree that day already
	
	ArrayList<Plant> plants = new ArrayList<Plant>(); //plants currently planted, can be any size so is arraylist
	ArrayList<Integer> shippingBox = new ArrayList<Integer>(); //the money from each item put in is added to this list
	Rectangle tillableLand = new Rectangle(560, 432, 464, 244); //the land that the player can plant in
	ArrayList<Rectangle> onForestGround = new ArrayList<Rectangle>(); //any items that are on the forest floor
	Point[] possForestPoints = {new Point(350,620), new Point(413,430),new Point(361,405),new Point(548,800),new Point(537,650),new Point(610,560),new Point(633,725),new Point(714,597),new Point(745,685),new Point(762,771),new Point(812,540),new Point(863,629),new Point(895,702),new Point(933,819),new Point(966,652)};
	//possForestPoints are points where items can be generated in the forest (basically where there aren't any trees or water)
	
	Map<String, Integer> plantStages = new HashMap<String, Integer>(); //used to record how many stages each plant needs to grow

	Texture date; //the date bar, top right
	Texture eBar; //the energy bar, bottom right
	Texture mBar; //the money bar, top right
	Texture berryPic; //img for the berries that are in the forest
	
	//backgrounds for the shop, menu, instructions, controls, and end screens
	Texture shop;
	Texture menu;
	Texture instructions;
	Texture controls;
	Texture end;
	
	OrthographicCamera camera = new OrthographicCamera(); //used to follow the player around
	TiledMap farmMap; //the map used for the farm screen (contains objects and tiles)
	TiledMap forestMap; //the map used for the forest screen
	TiledMapRenderer tiledMapRenderer; //renders the maps for the farm and forest (tiledMap)

	TiledMapTileLayer baseLayer; //the baseLayer of the farm map, which is the ground
	TiledMapTileSet tileSet; //the tileset used for the ground of the farm map
	
	ShapeRenderer shapeRenderer; //renders the shapes
	BitmapFont font; //used to write text in game
	
	@Override
	public void create () { //creates all the variables that have been initialized, and is only called once
		screen = "menu"; // the screen starts off at the main menu
		Gdx.graphics.setWindowedMode(800, 500); //size is 800x500
		camera.setToOrtho(false,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		
		font = new BitmapFont();
		shapeRenderer = new ShapeRenderer();
		
		//loads all the textures and TiledMaps
		berryPic = new Texture("items/berry.png");
		date = new Texture("date bar.png");
		eBar = new Texture("energy bar.png");
		mBar = new Texture("money bar.png");
		
		shop = new Texture("maps/seedShop.png");
		menu = new Texture("maps/menu.png");
		instructions = new Texture("maps/instructions.png");
		controls = new Texture("maps/controls.png");
		end = new Texture("maps/end.png");
		
		farmMap = new TmxMapLoader().load("maps/farmmap.tmx");
		forestMap = new TmxMapLoader().load("maps/forestMap.tmx");
		
		batch = new SpriteBatch();
		
		for(int i = 0; i<4; i++) {
			//i becomes 0,1,2,3
			walkingPics[i] = makeMove((i)*3,(i+1)*3); //(0,3),(3,6),(6,9),(9,12)
			//for each directions, it adds those frames to the 2D array
		}
		for(int i = 0; i<4; i++) {
			wateringPics[i] = makeAction((i)*5,(i+1)*5, "watering can jack");
			// 5 frames for each direction
		}
		for(int i = 0; i<4; i++) {
			tillingPics[i] = makeAction((i)*6,(i+1)*6, "hoe jack");
			//6 frames for each direction
		}
		for(int i = 0; i<4; i++) {
			cuttingPics[i] = makeAction((i)*6,(i+1)*6, "sickle jack");
			//6 frames for each direction
		}
		
		//adds the name of the plants and their number of stages to the Map
		plantStages.put("potato",8);
		plantStages.put("parsnip",7);
		plantStages.put("cauliflower",9);
		plantStages.put("pumpkin",8);
		plantStages.put("strawberry",9);
		plantStages.put("tomato",11);
		
		generateBerries(); //spawns berries in the forest

		
		//adds these tools to items list, which will be used for the inventory
		items.add(new Item(new Texture("tools/watering can.png"), "watering can", "tool",0,0));
		items.add(new Item(new Texture("tools/hoe.png"), "hoe", "tool", 0, 0));
		items.add(new Item(new Texture("tools/sickle.png"), "sickle", "tool", 0, 0));
		
		//adds these items (seeds) to the shop seeds list
		shopSeeds.add(new Item(new Texture("plant/potatoSeed.png"), "potato seeds", "seed", 40, 80));
		shopSeeds.add(new Item(new Texture("plant/tomatoSeed.png"), "tomato seeds", "seed", 45, 90));
		shopSeeds.add(new Item(new Texture("plant/parsnipSeed.png"), "parsnip seeds", "seed", 55, 110));
		shopSeeds.add(new Item(new Texture("plant/cauliflowerSeed.png"), "cauliflower seeds", "seed", 70, 140));
		shopSeeds.add(new Item(new Texture("plant/pumpkinSeed.png"), "pumpkin seeds", "seed", 90, 180));
		shopSeeds.add(new Item(new Texture("plant/strawberrySeed.png"), "strawberry seeds", "seed", 125, 250));
		
		tileSet = farmMap.getTileSets().getTileSet("groundd"); //this is the base tileset
		baseLayer = (TiledMapTileLayer)farmMap.getLayers().get("ground"); //this is the baseLayer
		generateWeeds(20); //spawns 20 weeds on the farm land
		
		player = new Player(405, 780, walkingPics,wateringPics,tillingPics, cuttingPics, items); //create the player
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		font.getData().setScale(2f); //sets the size of the text
		int shopIndex = 0; //sets them at 0 every time the screen is opened again
		int menuIndex = 0;
		
		if(screen == "menu") {
			drawMenu();
		}
		if(screen == "farm") {
			drawFarm();
		}
		if(screen == "forest") {
			drawForest();
		}
		if(screen == "shop") {
			drawShop();
		}
		if(screen == "instructions") {
			drawInstructions();
		}
		if(screen == "controls") {
			drawControls();
		}
		if(screen == "end") {
			drawEnd();
		}
	}
	
	@Override
	public void dispose () {
		shapeRenderer.dispose();
		font.dispose();
	}
	
	public Texture[] makeMove(int start, int end) {
		//(0,3),(3,6),(6,9),(9,12)
		Texture img;
		Texture[] move = new Texture[3];
		for(int i = start; i<end; i++) { //0,1,2
			img = new Texture("pic split/walking/walking" + (i+1) +".png");//loads each frame
			move[i-start] = img;//adds each frame to the array
		}
		return move; //array is returned and becomes part of the 2D array
	}
	public Texture[] makeAction(int start, int end, String name) {
		//same process as makeMove but with specific name for imgs to find
		//for tilling: (0,5),(5,10),(10,15),(15,20)
		Texture img;
		Texture[] move = new Texture[end];
		for(int i = start; i<end; i++) { //5,6,7,8,9
			img = new Texture("pic split/actions/"+name+ (i+1) +".png");
			move[i-start] = img; //0, 1, 2, 3, 4
		}
		return move;
	}
	
	public Texture[] makePlantStages(int end, String name) {
		//same process as makeMove, but isn't used for 2D arrays since plant stages only have one direction
		Texture img;
		Texture[] stages = new Texture[end];
		for(int i = 1; i<end; i++) {
			img = new Texture("plant/" + name +"Stages"+ i + ".png");
			stages[i-1] = img;
		}
		return stages;
	}
	
	public String getDate() {
		String[] week = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"}; //array of the days of the week
		
		return week[(days-1)%7]; //returns the current day, which is the remainder after dividing the days-1 by 7
	}
	
public void endDay() {
		if(days<31) { //if the day is bigger than 31, then the game is over
			for(int m : shippingBox) { //goes through the ints in the shippingBox list and adds them to the player's money
				if(player.money < 99999) { //99999 is the limit for money
					player.money = player.money + m;
				}
			}
			
			generateWeeds(10); //10 new weeds grow every night
			
			for(int i = 0; i<plants.size(); i++){ //goes through each of the plants that are planted right now
				Plant p = plants.get(i);
				if(tileSet.getTile(38) == baseLayer.getCell((int)p.sprite.getX()/16, (int)p.sprite.getY()/16).getTile()) {
					p.changeStage(); 
					//if the tile that it's on is equal to the watered tile from the tileset, then the plant grows
				}
				if(baseLayer.getCell((int)(p.sprite.getX()-4)/16, (int)(p.sprite.getY()-4)/16).getTile() == tileSet.getTile(39)) {
					plants.remove(i);
					i--;
					//if the tile that it's on is equal to the weed tile from the tileset, then the plant is destroyed
				}
			}
			
			for(int i = 33; i < 66; i++) {
				for(int j = 24; j < 43; j++) {
					//goes through every tile that is in the farm land
					if(tileSet.getTile(38) == baseLayer.getCell(i, j).getTile()) {
						//if the tile is the watered tile, then it is set to the normal, tilled tile
						Cell tillCell = new Cell();
						tillCell.setTile(tileSet.getTile(37));
						baseLayer.setCell(i, j, tillCell);
					}
				}
			}
			shippingBox.clear(); //clears the list of all the money that was in it
			honeyAvailable = true; //player can now pick up honey from the tree again
			generateBerries(); //spawns new berries in the forest
			player.sleep();
			screenFade(); //the screen fades, turning black for a moment
			days++;
		}
		
		else {//if the days are equal to 31, then the game ends
			screen = "end";
		}
	}

	public void screenFade() {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(0,0,0,0);
		for(int i = 0; i<30; i++) {
			shapeRenderer.rect(0,0,800,500); //draws a black rectangle for the time it takes to iterate 30 times
		}
		shapeRenderer.end();
	}
	
	public void generateWeeds(int num) {
		Cell weedCell = new Cell();
		weedCell.setTile(tileSet.getTile(39)); //there's a weed tile from the tileSet
		for(int i = 0; i < num; i++) {
			int x = (new Random().nextInt((int)tillableLand.width +32) + (int)tillableLand.x - 16)/16; 
			int y = (new Random().nextInt((int)tillableLand.height + 56) + (int)tillableLand.y - 48)/16;
			//gets a random x and y value from the tillable land, and divides it by 16 to get the tile-equivalent location
			if(baseLayer.getCell(x, y).getTile() != tileSet.getTile(39)) {
				//as long as there's not already a weed in that location, it changes the cell to that of thw weed
				baseLayer.setCell(x, y, weedCell);
			}
		}
	}
	
	public void planting() {
		Item crop; //initializes crop, seed, and name of the plant
		Item seed;
		String pName;
		Boolean canPlant = true;
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
			//if the player presses x
			if(player.inventory.getItem().desc == "seed" && player.inventory.getItem().numLeft > 0) {
				//if the type of item is a seed and there are some left in it (since every seed type has 9 uses)
				int x = -1;
				int y = -1;
				
				if(player.direction == 0) { //if the player is facing down, then the tile will be below them 
					x = (int)(player.getX()) / 16;
					y = (int)(player.getY() - 16) / 16;
				}
				if(player.direction == 1) { //if the player is facing up, then the tile will be above them 
					x = (int)(player.getX() / 16);
					y = (int)(player.getY() + 16) / 16;
				}
				if(player.direction == 2) { //if the player is facing left, then the tile will be to the left of them 
					x = (int)(player.getX() - 16) / 16;
					y = (int)(player.getY()) / 16;
				}
				if(player.direction == 3) { //if the player is facing right, then the tile will be to the right of them 
					x = (int)(player.getX() + 32)/ 16;
					y = (int)(player.getY()) / 16;
				}
				
				if((tileSet.getTile(37) == baseLayer.getCell(x, y).getTile()) || (tileSet.getTile(38) == baseLayer.getCell(x, y).getTile())){
					//if the tile that the player is trying to place the seed on it tilled or tilled/watered, then they can plant
					for(int i = 0; i <plants.size(); i++) { //goes through the list of plants, to check if there's already a plant in that position
						Plant p = plants.get(i);
						if((16*x+4 == p.sprite.getX() && 16*y+4 == p.sprite.getY()) ) {
							canPlant = false; //if there's already a plant there, then the player can't plant, so canPlant becomes false
						}
					}
					
					if(canPlant == true) { //if the player CAN plant
						seed = player.inventory.getItem(); //the seed becomes the item the currently holding, which should be of seed type
						seed.numLeft = seed.numLeft - 1; //the number of seeds left in the pack goes down
						pName = seed.name.substring(0, seed.name.length() - 6); //the name of the plant is the seed name minus "seeds"
						//Ex. seeds for pumpkin are called "pumpkin seeds", so we take away 6 chars to get the actual name
						crop = new Item(new Texture("plant/"+pName+".png"), pName, "crop", (int)8*pName.length(), 0); //crop is an item, and the revenue is equal to 8 times the length of its name
						//this works since the longer names are also the most expensive
						plants.add(new Plant(makePlantStages(plantStages.get(pName), pName),crop, 16*x+4, 16*y+4));
						//the list of plants has a new plant, with the texture array needed, and the location is the x and y value multiplied
						// by 16, add 4, so that the texture can be drawn onto the screen of the player, not the tiled map
						numPlanted++;
					}
				}
				if(player.inventory.getItem().numLeft == 0) {player.inventory.delete(); player.inventory.index = player.inventory.index - 1;}
				//if there aren't any seeds left in the pack, then it removes that item from the inventory
			}
		}
	}
	public void harvesting() {
		if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
			//if the user presses x once
			for(int i = 0; i<plants.size(); i++) {
				Plant p = plants.get(i); //goes through the plants 1 by 1
				if(p.currStage + 2 == p.stages.length) { //if the current stage of the plant is the last stage in its texture array
					if(player.getX() < p.sprite.getX() + 16 && player.getX() + player.rectangle.width > p.sprite.getX() && player.getY() < p.sprite.getY() + 16 && player.getY() + player.rectangle.height > p.sprite.getY()){
						//if the rectangles of the player and the plant are intersecting
						//adds the crop of the plant to inventory, and removes it from the list of plants
						player.inventory.add(p.crop);
						plants.remove(i);
						i--;
					}
				}
			}
		}
	}
	
	public void switchShopItem() {
		//switching the user's selection in the shop
		if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
			//user presses up, then the index goes down unless it's at the lowest already, in which case it'll go to the highest one which is 5
			if(shopIndex == 0) {
				shopIndex = 5;
			}
			else {
				shopIndex--;
		
			}
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			//user presses down, index goes up unless it's at the highest, in which case it'll go to 0
			if(shopIndex == 5) {
				shopIndex = 0;
			}
			else {
				shopIndex++;
			}
		}
	}
	
	public void switchMenuOption() {
		if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
			//if the user presses up, then the index changes
			//if it's already at the lowest, then the index goes to the highest one
			if(menuIndex == 0) {
				menuIndex = 1;
			}
			else {menuIndex--;}
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			if(menuIndex == 1) {
				menuIndex = 0;
			}
			else {menuIndex ++;}
		}
	}
		
	public void objectCollision(TiledMap map) {
		String property = player.getProperty(map).toString(); //gets the property of the object that the player is currently colliding with
			
		if(property.hashCode() == "exit".hashCode()) {
			//if it is equal to exit and the player is in the farm, then the new screen will be forest
			if(screen == "farm") {
				screen = "forest";
				player.setPos((float)480, (float)825); //sets the player's new position right at the entrance to the forest
			}
			else if(screen == "forest") {
				//if in forest, screen becomes farm
				screen = "farm";
				player.setPos((float)400, (float)335); //sets the position at the entrance to the farm
			}
		}
			
		if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {
			//if the player presses X, which is for interacting with their surroundings
			if(property.hashCode() == "water".hashCode() && player.inventory.getItem().name == "watering can") {
				//if the object is water and the player is currently holding the watering can, then the watering can becomes full
				player.inventory.getItem().full = 26;
			}
			if(property.hashCode() == "shipping box".hashCode()) {
				//if the object is the shipping box
				int revenue = player.inventory.getItem().revenue;
				//the revenue is equal to the revenue of the item the player is holding
				if(revenue != 0) {
					//if it's not equal to 0, adds the revenue to the shippingBox list, and deletes the item from the inventory
					numShipped++;
					shippingBox.add(revenue);
					player.inventory.delete();
					player.inventory.index = player.inventory.index - 1;
				}
			}
			if(property.hashCode() == "honey tree".hashCode() && honeyAvailable == true){
				//if the object is the honey tree and honey is currently available
				Item honey = new Item (new Texture("items/honey.png"), "honey", "", 20, 0);
				player.inventory.add(honey);
				honeyAvailable = false;
				//adds the honey to the player's inventory and honey isn't available anymore, for that day
			}
			if(property.hashCode() == "house".hashCode()) {
				//if the object is the house, then the player sleeps and the day ends
				endDay();
			}
			if(property.hashCode() == "shop".hashCode()) {
				//if the object is the shop, then the screen becomes that of the shop
				screen = "shop";
			}
		}
	}
	
	public Item buyItem(Item seed) {
		if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
			if (player.money == seed.price || player.money > seed.price) {
				//if user presses X, and the price of seed is lower than or equal to player's money
				return new Item(seed.img, seed.name, seed.desc, seed.revenue, seed.price);
			}
		}
		//if those conditions aren't true, then it returns null
		return null;
	}
	
	public void generateBerries() {
		onForestGround.clear(); //clears the list of all previous objects
		for(int i = 0; i<3; i++) {
			int r = new Random().nextInt(14); //gets a random number from 0-14, which are the indices of the possForestPoints
			onForestGround.add(new Rectangle((int)possForestPoints[r].getX(),(int)possForestPoints[r].getY(), 15, 16));
			//adds a rectangle with the x and y value of the point chosen, and the height and width of the berry
		} 
	}
	
	public void forestBerryCollision() {
		for(Rectangle berryRect : onForestGround) { //for every rectangle in the list of items on the forest ground
			if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {
				if(berryRect != null) { //as long as the object isn't null
					if(player.getX() < berryRect.x + berryRect.width && player.getX() + player.rectangle.width > berryRect.x && player.getY() < berryRect.y + berryRect.height && player.getY() + player.rectangle.height > berryRect.y) {
						//if the rectangles of the player and berry are intersecting then adds the berry to the inveotyr, and sets it as null from the list
						player.inventory.add(new Item(berryPic, "berry", "", 10, 0));
						onForestGround.set(onForestGround.indexOf(berryRect), null);
					}
				}
			}
		}
	}
	
	public void drawFarm() {
		
		tiledMapRenderer = new OrthogonalTiledMapRenderer(farmMap);
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		batch.setProjectionMatrix(camera.combined);
		
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(0,1,0,0); //green
		shapeRenderer.rect(762, 5, 30, player.getEnergy()); //draws the energy left of the player
		shapeRenderer.end();
		
		if(player.actionFrame == 0) { //as long as the player isn't going through an action at the moment, then they can move
			player.move(farmMap);
		}
		
		camera.position.set(player.getX(), player.getY(), 0); //the new pos of the camera is that of the player
		camera.update();
		
		player.action(farmMap, baseLayer, tileSet, tillableLand); //checks if the player is performing an action (watering, tilling)
		objectCollision(farmMap);//checks for collision in the farmMap
		planting(); // checks for planting in the farm
		harvesting(); // checks for harvesting in the farm
		
		batch.begin();
		for(Plant p : plants) {
			p.sprite.draw(batch); // draws every plant in the list of plants
		}
		
		player.sprite.draw(batch);
		
		//since the player is at the centre of the screen, everything must be draw according to its position
		batch.draw(eBar, player.getX()+352, player.getY()-250);
		batch.draw(date, player.getX()+185, player.getY()+200);
		batch.draw(mBar, player.getX()+240, player.getY()+150);
		batch.draw(player.inventory.img, player.getX() - 180, player.getY() - 250);
		
		for(int i = 0; i<player.inventory.used; i++) {
			//goes through each of the items in the inventory and draws them in their specified places, according to index
			batch.draw(player.inventory.getItem(i).sprite, (player.getX()-157)+35*i,player.getY() - 235);
		}
		
		font.draw(batch, getDate()+" "+days, player.getX()+200, player.getY()+235);
		font.draw(batch, ""+player.money, player.getX()+320, player.getY()+185);
		font.getData().setScale(1f); //changes the scale of the text, making it smaller
		for(Item i : player.inventory.items) {
			if(i.desc == "seed") {
				//if an item in the inventory is of seed type, then it draws the number of seeds left in the bottom right corner of its specified square
				font.draw(batch,""+i.numLeft, player.getX()-142+(player.inventory.getItemIndex(i)*35), player.getY() - 230);
			}
		}
		
		batch.end();
		
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0,0,0,0); //black
		shapeRenderer.rect(231+36*(player.inventory.index),6, 35, 35);
		shapeRenderer.rect(232+36*(player.inventory.index),7, 35, 33); //draw multiple rectangles to make it thicker
		shapeRenderer.end();
		
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(0,0,1,0); //blue
		shapeRenderer.rect(236+36*(player.inventory.getItemIndex("watering can")), 11, player.inventory.getItem("watering can").full, 4);
		//draws the amount of water left in the watering can, for the player's reference
		shapeRenderer.end();
	}
	
	public void drawForest() {
		//draws the same basic aspects of the game as drawFarm()
		tiledMapRenderer = new OrthogonalTiledMapRenderer(forestMap);
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		batch.setProjectionMatrix(camera.combined);
		
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(0,1,0,0);
		shapeRenderer.rect(762, 5, 30, player.getEnergy());
		shapeRenderer.end();
		player.move(forestMap); //moves the player in the forest map
		camera.position.set(player.getX(), player.getY(), 0);
		camera.update();
		
		objectCollision(forestMap); //checks for object collision in the forest, which should just be the exit
		forestBerryCollision(); //checks for collision with the berries on the floor
		
		batch.begin();
		for(Rectangle bRect : onForestGround) {
			if(bRect != null) {
				//draws each of the berries, as long as it hasn't been picked up yet
				batch.draw(new Texture("items/berry.png"), bRect.x, bRect.y);
			}
		}
		player.sprite.draw(batch);

		batch.draw(eBar, player.getX()+352, player.getY()-250);
		batch.draw(date, player.getX()+185, player.getY()+200);
		batch.draw(mBar, player.getX()+240, player.getY()+150);
		batch.draw(player.inventory.img, player.getX() - 180, player.getY() - 250);
		
		for(int i = 0; i<player.inventory.used; i++) {
			batch.draw(player.inventory.getItem(i).sprite, (player.getX()-157)+35*i,player.getY() - 235);
		}

		font.draw(batch, getDate()+" "+days, player.getX()+200, player.getY()+235);
		font.draw(batch, ""+player.money, player.getX()+320, player.getY()+185);
		
		batch.end();
		
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0,0,0,0);
		shapeRenderer.rect(231+36*(player.inventory.index),6, 35, 35);
		shapeRenderer.rect(233+36*(player.inventory.index),7, 34, 33);
		shapeRenderer.end();
		
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(0,0,1,0);
		shapeRenderer.rect(236+36*(player.inventory.getItemIndex("watering can")), 11, player.inventory.getItem("watering can").full, 4);
		shapeRenderer.end();
		
	}
	
	public void drawShop() {
		//draws the shop, inventory, seeds, and money of the player
		batch.begin();
		batch.draw(shop, player.getX()-400, player.getY()-250);
		
		for(int i = 0; i<player.inventory.used; i++) {
			batch.draw(player.inventory.getItem(i).sprite, (player.getX()-83)+45*i,player.getY() - 210);
		}
		
		for(int i = 0; i<shopSeeds.size(); i++) {
			batch.draw(shopSeeds.get(i).sprite, player.getX()-95, (player.getY()+182)-60*i ); //drawing seed
			font.draw(batch, shopSeeds.get(i).name, player.getX()-40, (player.getY()+206)-60*i); // writing name
			font.draw(batch, ""+shopSeeds.get(i).price, player.getX()+300, (player.getY()+205)-60*i); // writing price
		}
		
		font.getData().setScale(3f); //changes the scale to draw the money so that it fits better in its box
		font.draw(batch, ""+player.money, player.getX()-280, player.getY()-175);
		batch.end();
		
		switchShopItem(); //checks for switching the shop selection
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
			//if the user presses enter
			Item seed = shopSeeds.get(shopIndex);
			if (player.money == seed.price || player.money > seed.price) {
				//if the player has enough money, then adds the seed item to their inventory and subtracts the money from player's money
				player.inventory.add(new Item(seed.img, seed.name, seed.desc,seed.revenue, seed.price));
				player.money = player.money - seed.price;
			}
		}
		
		shapeRenderer.begin(ShapeType.Line); //draws the selection for the shop, so 3 rectangles so that it's thicker
		shapeRenderer.setColor(0,0,0,0);
		shapeRenderer.rect(280, 415-61*shopIndex, 500, 58);
		shapeRenderer.rect(281, 416-61*shopIndex, 498, 56);
		shapeRenderer.rect(282, 417-61*shopIndex, 496, 54);
		shapeRenderer.end();
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			screen = "farm"; //if they press escape, then player is returned to the farm
		}
	}
	
	public void drawMenu() {
		//draws the main menu and the player's selection
		batch.begin();
		batch.draw(menu, 0,0);
		batch.end();
		
		switchMenuOption();
		
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0,0,0,0);
		shapeRenderer.rect(442, 100-75*menuIndex, 190, 58);
		shapeRenderer.rect(443, 101-75*menuIndex, 188, 56);
		shapeRenderer.rect(444, 102-75*menuIndex, 186, 54);
		shapeRenderer.end();
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && nextOption == true) {
			//since user must press enter to move onto the next two screens, i added nextOption so that they could be given a pause before the screen moves from intstructions to controls
			if(menuOptions[menuIndex] == "play") {
				//if they click play, then they immediately go to the farm screen
				nextOption = false;
				screen = "farm";
			}
			if(menuOptions[menuIndex] == "help") {
				//if they click help, then they go to the instructions page
				nextOption = false;
				screen = "instructions";
			}
		}
		else {nextOption = true;} //if they havent pressed enter, then they can click the nectOption the next loop
	}
	
	public void drawInstructions() {
		batch.begin();
		batch.draw(instructions, 0,0);
		batch.end();
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && nextOption == true) {
			//if they press enter, then it moves onto the next page, which is controls
			nextOption = false;
			screen = "controls";
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && nextOption == true) {
			//if they press escape, then it returns them to the main menu
			nextOption = false;
			screen = "menu";
		}
		else if(!Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
			//if they haven't pressed enter, then they will be able to select an option at the next loop
			nextOption = true;
		}
	}
	
	public void drawControls() {
		batch.begin();
		batch.draw(controls, 0,0);
		batch.end();
		
		if((Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) && nextOption == true) {
			//if they press esc or enter, then they're returned to the main menu
			nextOption = false;
			screen = "menu";
		}
		else {nextOption = true;} //if they haven't pressed either, then they will be able to at the next loop
	}
	
	public void drawEnd() {
		batch.begin();
		batch.draw(end, player.getX()-400, player.getY()-250);
		
		font.getData().setScale(2f); //changes the scale of the text again
		//these are used to just add more variety to the ending, instead of just showing how much money the user made
		font.draw(batch, ""+player.money, player.getX()+200, player.getY()+170);
		font.draw(batch, ""+numPlanted, player.getX()+200, player.getY()+95);
		font.draw(batch, ""+numShipped, player.getX()+200, player.getY()+15);
		batch.end();
	}
}

//-----------------PLAYER CLASS-----------------
class Player{
	//Used to represent the user. This class keeps track of everything that has to do with the player, so its movement, inventory, energy, and other properties.
	Sprite sprite; //the current frame of the player
	Rectangle rectangle; //the rectangle that the player is occupying at the moment
	
	int direction; //direction that player is facing
	
	//setting variables for the directions, so that my code is easier to understand
	public static final int DOWN = 0;
	public static final int UP = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	
	double frame = 0; //the frame of the player (based on direction and movement)
	double actionFrame = 0; //the frame of the player (based on direction and action)
	String currentAction = ""; //the action that the player is currently doing
	
	Texture[][] walkingPics = new Texture[4][3]; //the frames for walking
	Texture[][] wateringPics = new Texture[4][5]; //frames for watering action
	Texture[][] tillingPics = new Texture[4][6]; //frames for tiling action
	Texture[][] cuttingPics = new Texture[4][6]; //frames for cutting action

	//player can move in all these directions right now, if they're unable then it'll be set to false
	boolean moveD = true; 
	boolean moveU = true;
	boolean moveR = true;
	boolean moveL = true;
	
	int energy; //amount of energy they have left
	int money; //current money
	Inventory inventory; //the player's inventory, contains all their items
	
	public Player(int x, int y, Texture[][] walkingPics,Texture[][] wateringPics,Texture[][] tillingPics, Texture[][] cuttingPics, LinkedList<Item> items) {
		this.walkingPics = walkingPics;
		this.wateringPics = wateringPics;
		this.tillingPics = tillingPics;
		this.cuttingPics = cuttingPics;
		sprite = new Sprite(walkingPics[0][0]); //set the sprite to be facing down at first
		sprite.setPosition(x,y);
		energy = 170; //energy starts off at 170 every day
		direction = 0; //facing down
		inventory = new Inventory(items); //their item already contains their tools at first
		money = 100; //start off with $100 so that they can buy some seeds
		rectangle = new Rectangle((int)sprite.getX(), (int)sprite.getY(),19,10);
	}
	
	public void setPos(float x, float y) {
		sprite.setPosition(x, y);
	}
	
	//these methods are just used to get the parameters of the player
	public float getX() {return sprite.getX();}
	public float getY() {return sprite.getY();}
	public int getWidth() {return 19;}
	public int getHeight() {return 29;}
	public int getEnergy() {return energy;}
	public void resetEnergy() {energy = 170;}//method used to reset the energy, so thatI don't always have to remember the number

	public int getMoney() {return money;}
	
	public void move(TiledMap map) {
		//changes the direction and moves the player according to direction, changes the frame
		int newDir = -1; //used to check if player has changed direction or if they stayed the same
		int velocity = 3; //how much the player moves by
		
		rectangle = new Rectangle(getX(), getY(), 19, 29);
	    moveD = true;
	    moveU = true;
	    moveR = true;
	    moveL = true;
		blocking(map);//checks if player is being blocked by anything, which will set one of the direction booleans to false
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && moveD == true) { 
			newDir = DOWN;
			frameChange(newDir);
			sprite.translateY(-velocity);
		}
		  
		else if(Gdx.input.isKeyPressed(Input.Keys.UP) && moveU == true) { 
			newDir = UP;
			frameChange(newDir);
			sprite.translateY(velocity); 
		}
		  
		else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && moveR == true) { 
			newDir = RIGHT;
			frameChange(newDir);
			sprite.translateX(velocity);
		}
		  
		else if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && moveL == true) { 
			newDir = LEFT;
			frameChange(newDir);
			sprite.translateX(-velocity);
		}
		  
		else {
			frameChange(newDir);//changes the frame based off the newDir not changing, which means that the player did not move
		}
	}
		 
	
	public void frameChange(int newDir) {
		//changes the frame of the sprite based off of movement
		if(newDir != -1) { //if there is a new direction, then the direction changes
			direction = newDir; 
			if(direction == newDir) { //if they're equal (which they should be), changes the frame of the sprite
				frame = frame + 0.2;
				if(frame > 3) { //when the frame goes over the number of frames there are, it resets at 0
					frame = 0;
				}
			}
		}

		else {
			frame = 0; //if there is no movement, then the frame stays at 0
		}
		
		sprite.setTexture(walkingPics[direction][(int)frame]);//changes the texture of player according to direction and frame
	}
	
	public void actionFrameChange(Texture[][] pics,int end) {
		//used to change the frames for each action
		if(actionFrame < end*3) { //makes it take longer to go through each of the frames
			actionFrame = actionFrame + 1;
			sprite.setTexture(pics[direction][(int)(actionFrame)/3]);
		}
		else {actionFrame = 0; currentAction = "";} //when the action is done, then sets the current action to nothing
	}
	

	public void blocking(TiledMap map) {
		//the bottom, top, right, and left rectangles of the sprite
	    Rectangle bRect = new Rectangle(getX(), getY()-4, 19, 4);
	    Rectangle tRect = new Rectangle(getX(), getY()+10, 19, 4);
	    Rectangle rRect = new Rectangle(getX()+19, getY(), 4, 10);
	    Rectangle lRect = new Rectangle(getX()-2, getY(), 2, 10);
		MapObjects objects = map.getLayers().get("collision").getObjects(); //gets all the objects from the collision object layer of the map
		for (RectangleMapObject rectangleObject : objects.getByType(RectangleMapObject.class)) {
			//goes through each rectangle object
		    Rectangle rectCollide = rectangleObject.getRectangle();//gets the rectangle of that object
		    if(Intersector.overlaps(rectCollide, bRect)) {moveD = false;} //if sprite intersects the bottom rectangle, then player can't move down
		    if(Intersector.overlaps(rectCollide, tRect)) {moveU = false;} //if sprite intersects the top rectangle, then player can't move up
		    if(Intersector.overlaps(rectCollide, rRect)) {moveR = false;} //same
		    if(Intersector.overlaps(rectCollide, lRect)) {moveL = false;} 
		}
	}
	
	public void action(TiledMap map,TiledMapTileLayer baseLayer, TiledMapTileSet tileSet, Rectangle tillableLand ) {
		//checks for player's action, and which tool they're using, as well as how the map should change
		Cell norCell = new Cell();  //the normal cell, contains the tile for the farmland
		norCell.setTile(tileSet.getTile(14));
		Cell tillCell = new Cell(); //the tilled cell, contains the tile for tilled farmland
		tillCell.setTile(tileSet.getTile(37));
		Cell waterCell = new Cell(); //the watered cell, contains the tile for watered+tilled farmland
		waterCell.setTile(tileSet.getTile(38));
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.Z) && energy>10) {
			//if the player presses z and has enough energy, then the frame starts from 0
			frame = 0;
			if(inventory.getItem().name == "watering can" && inventory.getItem().full!=0) {
				//if they're using the watering can
				int x = -1;
				int y = -1;
				if(direction == 0) { //if they're facing down, gets the tile below them
					x = (int)(getX()+5) / 16;
					y = (int)(getY() - 16) / 16;
				}
				if(direction == 1) {//if they're facing up, gets the tile above them
					x = (int)(getX() / 16);
					y = (int)(getY() + 16) / 16;
				}
				if(direction == 2) {//if they're facing left, gets the tile left of them
					x = (int)(getX() - 15) / 16;
					y = (int)(getY()) / 16;
				}
				if(direction == 3) {//if they're facing right, gets the tile right of them
					x = (int)(getX() + 25)/ 16;
					y = (int)(getY()) / 16;
				}
				if(baseLayer.getCell(x, y).getTile() == tileSet.getTile(37) && x != -1 && y != -1) {
					//if the tile that we got is tilled, then it can be watered, so we change that tile's cell to the watered cell
					inventory.getItem().full = inventory.getItem().full-2;
					baseLayer.setCell(x, y, waterCell);
					
				}
				currentAction = "watering"; //since they watered, their current action is watering
			}
			
			if(inventory.getItem().name == "hoe") {
				if(getX() < tillableLand.x + tillableLand.width && getX() + rectangle.width > tillableLand.x && getY() < tillableLand.y + tillableLand.height && getY() + rectangle.height > tillableLand.y) {
					//if they're trying to till the land, then they must be within the farmland rectangle
					int x = -1;
					int y = -1;
					//next part is same as watering, but with the tilled tile and "tilling" action instead
					if(direction == 0) {
						x = (int)(getX()+5) / 16;
						y = (int)(getY() - 16) / 16;
					}
					if(direction == 1) {
						x = (int)(getX() / 16);
						y = (int)(getY() + 16) / 16;
					}
					if(direction == 2) {
						x = (int)(getX() - 15) / 16;
						y = (int)(getY()) / 16;
					}
					if(direction == 3) {
						x = (int)(getX() + 25)/ 16;
						y = (int)(getY()) / 16;
					}
					if(x != -1 && y!= -1 && baseLayer.getCell(x, y).getTile() != tileSet.getTile(39)) {
						baseLayer.setCell(x, y, tillCell);
					}
				}
				currentAction = "tilling";
			}
			
			if(inventory.getItem().name == "sickle") {
				//if player is using the sickle
				int x = -1;
				int y = -1;
				if(direction == 0) {
					x = (int)(getX()+5) / 16;
					y = (int)(getY() - 16) / 16;
				}
				if(direction == 1) {
					x = (int)(getX() / 16);
					y = (int)(getY() + 16) / 16;
				}
				if(direction == 2) {
					x = (int)(getX() - 15) / 16;
					y = (int)(getY()) / 16;
				}
				if(direction == 3) {
					x = (int)(getX() + 25)/ 16;
					y = (int)(getY()) / 16;
				}
				if(baseLayer.getCell(x, y).getTile() == tileSet.getTile(39) && x != -1 && y != -1) {
					//if the tile we got is a weed tile, then sets it to a normal farmland instead
					baseLayer.setCell(x, y, norCell);
				}
				currentAction = "cutting";
			}
			energy = energy - 5; //subtracts 5 from the energy every time the player does an action
		}
		
		if(currentAction == "watering") {
			actionFrameChange(wateringPics, 4);
			//if player wants to water, then goes through the frames of the watering pics
		}
		if(currentAction == "tilling") {
			actionFrameChange(tillingPics, 5);
			//if player wants to till, then goes through the frames of the tilling pics
		}
		if(currentAction == "cutting") {
			actionFrameChange(cuttingPics, 5);
			//if player wants to cut weeds, then goes through the frames of the cutting pics
		}
		
		inventory.switchItem(); //i counted cycling through the inventory as an action, since it is being checked for constantly
	}
	
	public void sleep() {
		energy = 170; //every time the player sleeps, energy is set back to 170
	}
	
	public Object getProperty(TiledMap map) {
		//returns the property of the object that the player is currently colliding with
		MapObjects objects = map.getLayers().get("doors").getObjects(); //named it as "doors" and didn't change it, but is basically any object that the player can interact with
		for (RectangleMapObject rectangleObject : objects.getByType(RectangleMapObject.class)) {	
		    Rectangle rectCollide = rectangleObject.getRectangle(); //gets the rectangle of the object
		    
		    
		    Rectangle[] playerRects = new Rectangle[4]; // contains the 4 sides of the player, used to check collision
		    playerRects[0] = new Rectangle(getX(), getY()-4, 19, 4);
		    playerRects[1] = new Rectangle(getX(), getY()+10, 19, 4);
		    playerRects[2] = new Rectangle(getX()+19, getY(), 4, 10);
		    playerRects[3] = new Rectangle(getX()-4, getY(), 4, 10);
		    
		    for(Rectangle pRect : playerRects) {
		    	if(Intersector.overlaps(rectCollide, pRect)) {
		    		// if any side of the player is intersecting the rectangle, then returns the property of that object
		    		Object property = rectangleObject.getProperties().get("type");
		    		return property;
		    	}
		    }
		}
		return (Object)"none";//returns none if there is no intersection
	}
}

//-----------------ITEM CLASS-----------------
class Item{
	//This class is used to represent any items that the player has in their inventory or that are in the shop.
	// It contains aspects such as its price, the revenue for when the item is shipped, it's name, description, and image.
	int full; //only used for watering can, checking for how much water is left in it
	int price; //price of item, only really used for the seeds
	int numLeft; //how many more times the item can be used (used for seeds)
	Texture img; //the texture of the item, using for inventory
	Sprite sprite; //the sprite for the item
	String name; //the name of the item, mainly used in the shop
	String desc; //the type of item that it is (Ex. tool, seed, crop)
	int revenue; //how much player gets if they ship the item
	
	public Item(Texture img, String name, String desc, int r, int price) {
		this.img = img;
		sprite = new Sprite(img);
		this.name = name;
		this.desc = desc;
		revenue = r;
		if(desc == "tool") { //full is only used for tools (made it like this in case i wanted to add a fishing pole later)
			full = 26;
		}
		if(desc == "seed") { //can only buy seeds, and can only use seeds multiple times
			this.price = price;
			numLeft = 9;
		}
	}
	
	public void oneLess() {
		//whenever the seed is used
		numLeft--; 
	}
}

//-----------------INVENTORY CLASS-----------------
class Inventory{
	//This class is used to represent the inventory of the player. It contains items that the player has picked up
	//or already has in their inventory. This keeps track of how many items are in it, and when the player switches items.
	LinkedList<Item> items = new LinkedList<Item>(); //the items that are currently in the inventory
	Texture img;
	int max = 10; //the linked list should only hold 10 items at once
	int used; //how much of the list has been used so far
	int index; //the index of the item that the player is currently selecting
	public Inventory(LinkedList<Item> items) {
		this.items = items;
		index = 0; //starts off at 0
		img = new Texture("inventory.png");
		used = items.size(); //the amount used is the size of the list that we used to make the inventory
	}
	
	public Item getItem() {return items.get(index);} //gets the item the player is currently on
	public Item getItem(int i) {return items.get(i);} //gets the item that the user wants
	
	public Item getItem(String name) {//gets the item using the name of the item
		for(Item item : items) {
			if(item.name == name) {
				return item;
			}
		}
		return null;
	}
	
	public int getItemIndex(String name) { //gets the index of the item that the player wants, using its name
		for(int i = 0; i<used; i++) {
			if(items.get(i).name == name) {
				return i;
			}
		}
		return 0;
	}
	public int getItemIndex(Item item) { //gets the index of the item that the player wants, using Item
		for(int i = 0; i<used; i++) {
			if(items.get(i) == item) {
				return i;
			}
		}
		return -1;
	}
	
	public void add(Item item) {
		//adds the item wanted to the list, as long as we aren't at the max capacity
		if(used != max) {
			items.add(item);
			used++;
		}
	}
	
	public void delete() {
		//deletes the item that is currently selected
		items.remove(index);
		used--;
	}
	public void delete(Item item) {
		//deletes the item that the user chooses, by Item
		items.remove(item);
		used--;
	}
	
	public void switchItem() {
		//changes the index of the inventory, based on whether player presses A or S
		//(same concept as switchMenuOption() and switchShopItem()
		if(Gdx.input.isKeyJustPressed(Input.Keys.A)){
			if(index == 0) {
				index = used-1;
			}
			else {
				index--;
		
			}
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
			if(index == used-1) {
				index = 0;
			}
			else {
				index++;
			}
		}
	}
}

//-----------------PLANT CLASS-----------------
class Plant{
	Texture[] stages; //the stages that the plant will grow through
	int currStage;//the index of the current stage
	Item crop; //the item that will be harvested when it's ready
	Sprite sprite;
	
	public Plant(Texture[] stages, Item crop, int x, int y) {
		this.stages = stages;
		this.crop = crop;
		currStage = 0; //starts off at 0
		sprite = new Sprite(stages[currStage]);
		sprite.setPosition(x, y);
	}
	
	public void changeStage() {
		//changes the stage only if the current stage is at the second-last stage
		if(currStage < stages.length-2) {
			currStage++;
			sprite.setTexture(stages[currStage]);
		}
	}
}