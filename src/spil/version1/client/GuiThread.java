package spil.version1.client;

import javafx.application.Application;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Gui;
import spil.version1.gamefiles.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



public class GuiThread extends Thread{
	public void run(){
		Application.launch(Gui.class);
	}
}
