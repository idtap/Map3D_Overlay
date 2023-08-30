package application;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import controller.RibbonFormController;
import feoverlay.Functions;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
	public static RibbonFormController ribbonFormcontroller;

	@Override
	public void start(Stage stage) throws Exception {
		Functions.apPath = new File(".").getCanonicalPath();

		Functions.primaryStage = stage;
		Functions.appStylesheet = getClass().getResource("/css/fullpackstyling.css").toExternalForm();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RibbonForm.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(Functions.appStylesheet);
		ribbonFormcontroller = loader.getController();
		ribbonFormcontroller._stage_main = stage;

		stage.setTitle("透明圖範例");
		stage.setMaximized(true);
		stage.setResizable(true);
		stage.setScene(scene);
		stage.show();

		stage.setOnCloseRequest(e -> {
			stop();
            Platform.exit();
            System.exit(0);
		});
	}
	
	@Override
	public void stop() {
		if (null != ribbonFormcontroller)
			ribbonFormcontroller.ReleaseResource();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
