package spil.version1.gamefiles;

import java.io.Serializable;

public class Player implements Serializable {
	private static int nrOfPlayers = 0;
	private static String[] colours = new String[]{"(white)", "(red)", "(yellow)", "(blue)", "(green)"};
	String name;
	pair location;
	int point;
	public String direction;
	public int id;
	public String heroRightIconPath;
	public String heroLeftIconPath;
	public String heroUpIconPath;
	public String heroDownIconPath;

	public Player(String name, pair loc, String direction) {
		this.name = name;
		this.location = loc;
		this.direction = direction;
		this.point = 0;
		nrOfPlayers++;
		this.id = nrOfPlayers;
		heroRightIconPath = "Image/heroRight" + id + ".png";
		heroLeftIconPath = "Image/heroLeft" + id + ".png";
		heroUpIconPath = "Image/heroUp" + id + ".png";
		heroDownIconPath = "Image/heroDown" + id + ".png";
	};

	public pair getLocation() {
		return this.location;
	}

	public void setLocation(pair p) {
		this.location=p;
	}

	public int getXpos() {
		return location.x;
	}
	public void setXpos(int xpos) {
		this.location.x = xpos;
	}
	public int getYpos() {
		return location.y;
	}
	public void setYpos(int ypos) {
		this.location.y = ypos;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public void addPoints(int p) {
		point+=p;
	}
	public String toString() {
		return name + ", " + colours[id - 1] + ": " + point + ", (" + location.x +", " + location.y +")";
	}

	public String getName() {
		return name;
	}


	public int getId() {
		return id;
	}

	public String getHeroRightIconPath() {
		return heroRightIconPath;
	}

	public String getHeroLeftIconPath() {
		return heroLeftIconPath;
	}

	public String getHeroUpIconPath() {
		return heroUpIconPath;
	}

	public String getHeroDownIconPath() {
		return heroDownIconPath;
	}
}
