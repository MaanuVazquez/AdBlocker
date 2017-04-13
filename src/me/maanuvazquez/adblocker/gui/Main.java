package me.maanuvazquez.adblocker.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Layout.fxml"));
			AnchorPane root = (AnchorPane) loader.load();
			LayoutController controller = (LayoutController) loader.getController();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("Layout.css").toExternalForm());
			
			primaryStage.setTitle("ADBLOCKER");
			primaryStage.setScene(scene);
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.getIcons().add(new Image("/resources/icon.png"));
			primaryStage.centerOnScreen();
			primaryStage.setResizable(false);
			primaryStage.show();
			primaryStage.requestFocus();
			controller.checkAdminRights();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
