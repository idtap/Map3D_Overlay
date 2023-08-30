package controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import feoverlay.AlertDialog;
import feoverlay.MapManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Basemaps3DController implements Initializable {
	@FXML
	private StackPane mainPane;
	
	@FXML
	private ImageView minimizeButton;

	private Stage _stage_main;
	private MapContentController parentController;
	private AnchorPane parentPane;

	Basemaps3DController(Stage _stage_main, MapContentController parentController, AnchorPane parentPane) {
		this._stage_main = _stage_main;
		this.parentController = parentController;
		this.parentPane = parentPane;
	}
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

    	minimizeButton.setOnMouseClicked((e) -> {
    		parentPane.setVisible(false);
    	});

	}
}
