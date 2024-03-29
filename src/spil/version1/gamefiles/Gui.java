package spil.version1.gamefiles;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import spil.version1.client.Client;
import spil.version1.client.GuiThread;


public class Gui extends Application{

	public static final int size = 30; 
	public static final int scene_height = size * 20 + 100;
	public static final int scene_width = size * 20 + 250;

	public static Image image_floor;
	public static Image image_wall;
	public static Image hero_right;
	public static Image hero_left;
	public static Image hero_up;
	public static Image hero_down;

	GameLogic gameLogic = Client.localLogic;


	private static Label[][] fields;
	private TextArea scoreList;
	


	
	// -------------------------------------------
	// | Maze: (0,0)              | Score: (1,0) |
	// |-----------------------------------------|
	// | boardGrid (0,1)          | scorelist    |
	// |                          | (1,1)        |
	// -------------------------------------------

	@Override
	public void start(Stage primaryStage){
		try {
			
			
			GridPane grid = new GridPane();
			grid.setHgap(20);
			grid.setVgap(20);
			grid.setPadding(new Insets(20, 20, 20, 20));

			Text mazeLabel = new Text("Maze:");
			mazeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
	
			Text scoreLabel = new Text("Score:");
			scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

			scoreList = new TextArea();
			
			GridPane boardGrid = new GridPane();


			image_wall  = new Image(getClass().getResourceAsStream("Image/wall4.png"),size,size,false,false);
			image_floor = new Image(getClass().getResourceAsStream("Image/floor1.png"),size,size,false,false);

			fields = new Label[20][20];
			for (int j=0; j<20; j++) {
				for (int i=0; i<20; i++) {
					switch (Generel.board[j].charAt(i)) {
					case 'w':
						fields[i][j] = new Label("", new ImageView(image_wall));
						break;
					case ' ':					
						fields[i][j] = new Label("", new ImageView(image_floor));
						break;
					default: throw new Exception("Illegal field value: " + Generel.board[j].charAt(i));
					}
					boardGrid.add(fields[i][j], i, j);
				}
			}
			scoreList.setEditable(false);
			
			
			grid.add(mazeLabel,  0, 0); 
			grid.add(scoreLabel, 1, 0); 
			grid.add(boardGrid,  0, 1);
			grid.add(scoreList,  1, 1);
						
			Scene scene = new Scene(grid,scene_width,scene_height);
			primaryStage.setScene(scene);
			primaryStage.show();

			scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
				switch (event.getCode()) {
				case UP:    playerMoved(0,-1,"up"); Client.sendMoveToServer("up"); break;
				case DOWN:  playerMoved(0,+1,"down"); Client.sendMoveToServer("down"); break;
				case LEFT:  playerMoved(-1,0,"left"); Client.sendMoveToServer("left"); break;
				case RIGHT: playerMoved(+1,0,"right"); Client.sendMoveToServer("right"); break;
				case ESCAPE:System.exit(0); 
				default: break;
				}
			});
			


			scoreList.setText(getScoreList());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void removePlayerOnScreen(pair oldpos) {
		Platform.runLater(() -> {
			fields[oldpos.getX()][oldpos.getY()].setGraphic(new ImageView(image_floor));
			});
	}
	
	public static void placePlayerOnScreen(pair newpos, String direction, Player p) {
		Platform.runLater(() -> {
			int newx = newpos.getX();
			int newy = newpos.getY();
			if (direction.equals("right")) {
				hero_right = new Image(Gui.class.getResourceAsStream(p.getHeroRightIconPath()),size,size,false,false);
				fields[newx][newy].setGraphic(new ImageView(hero_right));
			};
			if (direction.equals("left")) {
				hero_left = new Image(Gui.class.getResourceAsStream(p.getHeroLeftIconPath()),size,size,false,false);
				fields[newx][newy].setGraphic(new ImageView(hero_left));
			};
			if (direction.equals("up")) {
				hero_up = new Image(Gui.class.getResourceAsStream(p.getHeroUpIconPath()),size,size,false,false);
				fields[newx][newy].setGraphic(new ImageView(hero_up));
			};
			if (direction.equals("down")) {
				hero_down = new Image(Gui.class.getResourceAsStream(p.getHeroDownIconPath()),size,size,false,false);
				fields[newx][newy].setGraphic(new ImageView(hero_down));
			};
			});
	}
	
	public static void movePlayerOnScreen(pair oldpos, pair newpos, String direction, Player p)
	{
		removePlayerOnScreen(oldpos);
		placePlayerOnScreen(newpos, direction, p);
	}
	

	
	public void updateScoreTable()
	{
		Platform.runLater(() -> {
			scoreList.setText(getScoreList());
			});
	}

	public void playerMoved(int delta_x, int delta_y, String direction) {
		gameLogic.updatePlayer(Client.getME(),delta_x,delta_y,direction);
		updateScoreTable();
	}
	
	public String getScoreList() {
		StringBuffer b = new StringBuffer(100);
		for (Player p : gameLogic.players) {
			b.append(p + "\r\n");
		}
		return b.toString();
	}

	public static void clearBoard() {
		Platform.runLater(() -> {
			for (int j = 0; j < fields.length; j++) {
				for (int i = 0; i < fields[j].length; i++) {
					// Tjekker, om det aktuelle felt er et gulv
					if (Generel.board[j].charAt(i) == ' ') {
						fields[i][j].setGraphic(new ImageView(image_floor));
					}
					// Du kan tilføje flere betingelser her, hvis der er andre typer felter, du gerne vil nulstille
				}
			}
		});
	}
	
}

