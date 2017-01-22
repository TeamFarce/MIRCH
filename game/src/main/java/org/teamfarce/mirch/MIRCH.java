package org.teamfarce.mirch;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import org.teamfarce.mirch.dialogue.*;

/**
 * MIRCH is used to generate all graphics in the program. It initialises the scenario generator and game state
 * and provides all interactions with the back end of the program.
 * @author jacobwunwin
 *
 */
public class MIRCH extends ApplicationAdapter{
	private static final boolean playAnnoyingMusic = false; //set to true to play incredibly annoying background music that ruins your songs
	private Texture detectiveTexture;
	private Texture doorwayTexture;
	private SpriteBatch batch;
	private GameSnapshot gameSnapshot;
	
	private DisplayController displayController;
	private InputController inputController;
	
	private Skin uiSkin;

	
	private ArrayList<RenderItem> rooms;
	private ArrayList<RenderItem> objects;
	private ArrayList<RenderItem> characters;
	private ArrayList<RenderItem> doors;
	
	private float move = 3; //sets the speed at which the player move
	private float characterMove = 1f;
	private int moveStep = 50;
	private int step; //stores the current loop number
	private int characterWidth = 60;

	private Sprite player;
	
	private OrthographicCamera camera;
	
	private Music music_background;
	
	
	/**
	 * Returns a RenderItem referencing the current room that the player sprite is in.
	 * @param rooms
	 * @param player
	 * @return
	 */
	private RenderItem getCurrentRoom(ArrayList<RenderItem> rooms, Sprite player){
		for (RenderItem room : rooms){
			
			if ((player.getX() > room.sprite.getX()) && (player.getX() + player.getWidth() < room.sprite.getX() + room.sprite.getWidth())){
				if ((player.getY()> room.sprite.getY()) && (player.getY() + player.getHeight() < room.sprite.getY() + room.sprite.getHeight())){
					return room;
				}
			}
		}
		return new RenderItem(new Sprite(), new Room("", new Vector2(0, 0)));
	}
	
	/**
	 * Returns true if the sprite is in a doorway.
	 * @param doors
	 * @param player
	 * @return
	 */
	protected boolean inDoor(ArrayList<Door> doors, Sprite player){
		boolean toReturn = false;
		//System.out.println("Checking door");
		for (Door door: doors){
			//System.out.println(door.startX);
			//System.out.println(player.getX());
			//ystem.out.println(door.endX);
			if ((player.getX() > door.startX - (characterWidth / 2)) && (player.getX() < door.endX - (characterWidth / 2))){ //reduce by characterWidth/2 as sprites are located from bottom left corner
				//System.out.println("in x");
				
				if ((player.getY() > door.startY - 50) && (player.getY() < door.endY + 50)){
					toReturn = true;
					//System.out.println("in y");
				}
			}
		}
		
		return toReturn;
	}

	
	/**
	 * Plays music in the background
	 */
	private void playMusic(){
		music_background = Gdx.audio.newMusic(Gdx.files.internal("assets/music/Minima.mp3"));
		music_background.setLooping(true);
		music_background.play();
	}

	/**
	 * Initialises all variables in the game and sets up the game for play.
	 */
	@Override
	public void create() {
		//++++INITIALISE THE GAME++++
		
		step = 0; //initialise the step variable
		//create temporary required items, eventually ScenarioBuilder will generate these
		ArrayList<Suspect> tempSuspects = new ArrayList<Suspect>();
		
		Suspect tempSuspect = new Suspect("Devil_sprite.png", new Vector2(400, 400));

        tempSuspect.dialogueTree = new DialogueTree();
		tempSuspect.name = "The Devil";
		tempSuspect.description = "He is the devil";
		
		tempSuspects.add(tempSuspect);

        QuestionIntent qi1 = new QuestionIntent("Did you see anything suspicious?");
        tempSuspect.dialogueTree.addQuestionIntent(qi1);

        QuestionAndResponse qar11 = new QuestionAndResponse(
            "You look suspicious, what are you hiding?",
            "Aggressive",
            "What?! Nothing I swear",
            new ArrayList<>()
        );
        qi1.addQuestion(qar11);

        QuestionAndResponse qar12 = new QuestionAndResponse(
            "Have you seen anything unusual or suspicious?",
            "Direct",
            "The vampire has put on surprisingly good fake blood since the start of the party.",
            new ArrayList<>()
        );
        qi1.addQuestion(qar12);

        QuestionAndResponse qar13 = new QuestionAndResponse(
            "*Grunts mildly*, *points at suspect*",
            "Grunts and Points",
            "*Beats chest*",
            new ArrayList<>()

        );
        qi1.addQuestion(qar13);

        QuestionIntent qi2 = new QuestionIntent("How long ago did you notice this");
        IDialogueTreeAdder qi2_add = new SingleDialogueTreeAdder(tempSuspect.dialogueTree, qi2);

        qar12.addDialogueTreeAdder(qi2_add);

        QuestionAndResponse qar21 = new QuestionAndResponse(
            "Why didn't you tell me earlier? Tell me now!",
            "Aggressive",
            "I don't know!",
            new ArrayList<>()
        );
        qi2.addQuestion(qar21);

        QuestionAndResponse qar22 = new QuestionAndResponse(
            "When do you think you noticed this?",
            "Direct",
            "Maybe an hour ago. Not too long before the body was dumped.",
            new ArrayList<>()
        );
        qi2.addQuestion(qar22);

        QuestionAndResponse qar23 = new QuestionAndResponse(
            "*Grunts harshly*, *Beat chest*",
            "Grunts and Points",
            "*Screams in the fifth dimension*",
            new ArrayList<>()
        );
        qi2.addQuestion(qar23);

        qar23.addDialogueTreeAdder(new SingleDialogueTreeAdder(tempSuspect.dialogueTree, qi2));

		ArrayList<Room> tempRooms = new ArrayList<Room>();
		
		Room temp2 = new Room("Classroom_2.png", new Vector2(200, 490));
		tempRooms.add(temp2);
		
		Room temp1 = new Room("Classroom_1.png", new Vector2(200, 200)); //generate a sample room for testign purposes
		tempRooms.add(temp1);

		ArrayList<Prop> tempProps = new ArrayList<Prop>();

		Prop prop = new Prop("Axe.png", temp1, new Vector2(50, 50)); //generate a sample prop for testing purposes
		prop.description = "A bloody axe...";
		prop.name = "Axe";
		tempProps.add(prop);

		ArrayList<Door> tempDoors = new ArrayList<Door>();
		tempDoors.add(new Door(300, 490, 350, 520));
		
		gameSnapshot = new GameSnapshot(tempSuspects, tempProps, tempRooms); //generate the GameSnapshot object
		
		//generate RenderItems from each room
		rooms = new ArrayList<RenderItem>();
		for (Room room : gameSnapshot.getRooms()){
			Sprite newSprite = new Sprite(new Texture(Gdx.files.internal("assets/rooms/" + room.filename)));
			newSprite.setPosition(room.position.x, room.position.y); //generate a sprite for the room
			rooms.add(new RenderItem(newSprite, (Room) room)); //create a new renderItem for the room
		}
		
		//generate RenderItems for each prop
		objects = new ArrayList<RenderItem>();
		for (Prop sprop : gameSnapshot.getProps()){
			Sprite newSprite = new Sprite(new Texture(Gdx.files.internal("assets/objects/" + sprop.filename)));
			newSprite.setPosition(sprop.currentRoom.position.x + sprop.roomPosition.x, sprop.currentRoom.position.y + sprop.roomPosition.y);
			objects.add(new RenderItem(newSprite, sprop));
		}
		
		//generate RenderItems for each suspect
		characters = new ArrayList<RenderItem>();
		for (Suspect suspect : gameSnapshot.getSuspects()){
			Sprite newSprite = new Sprite(new Texture(Gdx.files.internal("assets/characters/" + suspect.filename)));
			newSprite.setPosition(suspect.mapPosition.x, suspect.mapPosition.y);
			characters.add(new RenderItem(newSprite, (Suspect) suspect));
		}
		
		//Generate an ArrayList of RenderItems to store every door in the gameSnapshot
		doorwayTexture = new Texture(Gdx.files.internal("assets/door.png")); //create the doorway texture
		doors = new ArrayList<RenderItem>();
		for (Door door : gameSnapshot.getDoors()){			
			Sprite newSprite = new Sprite(doorwayTexture);
			float xScale = (door.endX - door.startX)/(newSprite.getWidth());
			float yScale = (door.endY - door.startY)/(newSprite.getHeight());		
			//newSprite.setScale(xScale, yScale);

			newSprite.setSize(newSprite.getWidth() * xScale, newSprite.getHeight() * yScale);

			newSprite.setPosition(door.startX, door.startY);
			//newSprite.setRegion(door.startX, door.startY, (door.endX - door.startX)/2, door.endY - door.startY);
			doors.add(new RenderItem(newSprite, door));
		}
		
		//render the title screen texture
		detectiveTexture = new Texture(Gdx.files.internal("assets/Detective_sprite.png"));
		camera = new OrthographicCamera(); //set up the camera as an Orthographic camera
		camera.setToOrtho(false, 1366, 768); //set the size of the window

		batch = new SpriteBatch(); //create a new sprite batch - used to display sprites onto the screen		
		//initialise the player sprite
		player = new Sprite(detectiveTexture);
		player.setPosition(250, 250);

		
		//starts music "Minima.mp3" - Kevin Macleod
		if (playAnnoyingMusic){ 
			playMusic();
		}
		
		uiSkin = new Skin(Gdx.files.internal("assets/skins/skin_pretty/skin.json")); //load ui skin from assets
		//uiSkin = new Skin(Gdx.files.internal("assets/skins/skin_default/uiskin.json")); //load ui skin from assets
		
		this.displayController = new DisplayController(uiSkin, gameSnapshot, batch);
		this.inputController = new InputController();

	}
	
	/**
	 * The render function deals with all game logic. It recieves inputs from the input controller, 
	 * carries out logic and pushes outputs to the screen through the display controller
	 */
	@Override
	public void render() {
	      Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 0f);
	      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	      
	      batch.setProjectionMatrix(camera.combined);
	      	      
	      //Draw the map here
	      if (gameSnapshot.getState() == GameState.map){

	    	  //store the players current room and position, so that we can later check that the player has not stepped over the room bounds
	    	  RenderItem currentRoom = getCurrentRoom(rooms, player); //find the current room that the player is in
	    	  Float currentX = player.getX();
	    	  Float currentY = player.getY();
	    	  
	    	  Vector2 playerMove = inputController.fetchPlayerPositionUpdate(); //get the player movement input
	    	  
	    	  playerMove = playerMove.scl(move); //scale the move amounts
	    	  
	    	  player.translate(playerMove.x, playerMove.y);
	    	  
	    	  RenderItem newRoom = getCurrentRoom(rooms, player); //find the new current room of the player
	    	  
	    	  //if we are no longer in the previous room and haven't entered a door, we move the player back
	    	  //to the old position
	    	  if (!currentRoom.equals(newRoom) && !inDoor(gameSnapshot.getDoors(), player)){
	    		  player.setX(currentX);
	    		  player.setY(currentY); 
	    	  }
	    	  
	    	  java.util.Random random = new java.util.Random();
	    	  //System.out.println(random.nextInt(10));
	    	  
	    	  //loop through each suspect character, moving them randomly
	    	  for (RenderItem character: characters){
	    		  if ((step % moveStep) == 0){
	    			  //System.out.println("Updating move step");
	    			  //Carries out a probability check to determine whether the character should move or stay stationary
	    			  //This gives the characters a 'meandering' look
	    			  if (random.nextInt(2) >= 1){ 
	    				  //calculate the new move vector for the character
		    			  float randX =  (float) random.nextInt((int) characterMove * 2 + 1) - characterMove;
			    		  float randY = (float) random.nextInt((int) characterMove * 2 + 1) - characterMove;
			    		  //System.out.println(randX); 
		    			  Suspect suspect = (Suspect) character.object; //store the characters current room
		    			  suspect.moveStep = new Vector2(randX, randY);
		    			  character.object = suspect;
	    			  } else {
	    				  Suspect suspect = (Suspect) character.object;
		    			  suspect.moveStep = new Vector2(0f, 0f);
		    			  character.object = suspect;
	    			  }
	    		  }
	    		  
	    		  //Check to ensure character is still in room
    			  Suspect suspect = (Suspect) character.object; //retrieve the SUspect object from the renderItem

	    		  
	    		  float thisX = character.sprite.getX();
	    		  float thisY = character.sprite.getY();
	    		  //find the objects current room
	    		  RenderItem thisRoom = getCurrentRoom(rooms, character.sprite);
					
	    		  character.sprite.translate(suspect.moveStep.x, suspect.moveStep.y); //translate the character
	    		  
	    		  //find the characters new room
	    		  RenderItem thisNextRoom = getCurrentRoom(rooms, character.sprite);
	    		  
	    		  //check if the character has illegally left the rooms bounds, if it has move it back to its previous location
	    		  if (!thisRoom.equals(thisNextRoom) && !inDoor(gameSnapshot.getDoors(), character.sprite)){
		    		  character.sprite.setX(thisX);
		    		  character.sprite.setY(thisY); 
		    	  }
	    		  
	    		  suspect.currentRoom = (Room) thisNextRoom.object; //update the current room the suspect is in in the back end
		    	  
	    	  } 
	    	  
	    	  //check if a character has been clicked
	    	  if (inputController.isObjectClicked(characters, camera)){
	    		  RenderItem character = inputController.getClickedObject(characters, camera);
				  this.displayController.drawGUI().initialiseInterviewGUI((Suspect) character.object, player);
				  gameSnapshot.setState(GameState.dialogueIntention); 
				  
	    	  } else if (inputController.isObjectClicked(objects, camera)){
	    		  RenderItem object = inputController.getClickedObject(objects, camera);
	    		  if (gameSnapshot.journal.getProps().indexOf((Prop) object.object) == -1){
    				  this.displayController.drawItemDialogue((Prop) object.object);
					  gameSnapshot.journalAddProp((Prop) object.object); 
				  } else {
					  //otherwise we report to the user that the object is already in the journal
    				  this.displayController.drawItemAlreadyFoundDialogue((Prop) object.object);
				  }
	    	  }
	    	  
	    	  //Draw the map to the display
	    	  camera.position.set (new Vector3(player.getX(), player.getY(), 1)); //move the camera to follow the player
		      camera.update();
	    	  displayController.drawMap(rooms, doors, objects, characters);
	    	  
	    	  batch.begin();
	    	  player.draw(batch);
	    	  batch.end();
	      
	    	  //Draw the journal here
	      } else if (gameSnapshot.getState() == GameState.journalHome){
	    	  camera.position.set (new Vector3(camera.viewportWidth / 2, camera.viewportHeight / 2, 1)); //move the camera to follow the player
		      camera.update();
		      this.displayController.drawGUI().useJournalHomeView();
	    	  
	      } else if (gameSnapshot.getState() == GameState.journalClues){	    	  
	    	  camera.position.set (new Vector3(camera.viewportWidth / 2, camera.viewportHeight / 2, 1)); //move the camera to follow the player
		      camera.update();
		      this.displayController.drawGUI().useJournalCluesView();
	    	  
	      } else if (gameSnapshot.getState() == GameState.journalQuestions){
	    	  camera.position.set (new Vector3(camera.viewportWidth / 2, camera.viewportHeight / 2, 1)); //move the camera to follow the player
		      camera.update();
		      this.displayController.drawGUI().useJournalInterviewView();
	    	  
	      } else if (gameSnapshot.getState() == GameState.journalNotepad){
	    	  camera.position.set (new Vector3(camera.viewportWidth / 2, camera.viewportHeight / 2, 1)); //move the camera to follow the player
		      camera.update();
	    	  this.displayController.drawGUI().useJournalNotepadView();	    	  
	    	  
	      } else if (gameSnapshot.getState() == GameState.dialogueIntention){
	    	  camera.position.set (new Vector3(camera.viewportWidth / 2, camera.viewportHeight / 2, 1)); //move the camera to follow the player
		      camera.update();
		      this.displayController.drawGUI().drawInterviewGUI();
		      
	      }  else if (gameSnapshot.getState() == GameState.accuse){
	    	  camera.position.set (new Vector3(camera.viewportWidth / 2, camera.viewportHeight / 2, 1)); //move the camera to follow the player
		      camera.update();
		      this.displayController.drawGUI().drawAccuseGUI();
	      } else if (gameSnapshot.getState() == GameState.gameWon){
	    	  camera.position.set (new Vector3(camera.viewportWidth / 2, camera.viewportHeight / 2, 1)); //move the camera to follow the player
		      camera.update();
		      this.displayController.drawGUI().drawWinScreen();
	      }
	      
	      step++; //increment the step counter
		
	}
	
	@Override
	public void dispose() {
		
	}
}
