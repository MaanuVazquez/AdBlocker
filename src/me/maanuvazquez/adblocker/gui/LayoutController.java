package me.maanuvazquez.adblocker.gui;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.maanuvazquez.adblocker.AdBlock;
import me.maanuvazquez.adblocker.Database;
import me.maanuvazquez.adblocker.OSEnum;
import me.maanuvazquez.adblocker.Utils;
import me.maanuvazquez.adblocker.models.Domain;

public class LayoutController {

	@FXML
	private ProgressBar progressBar;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private StackPane stackPaneTitle;

	@FXML
	private StackPane stackPaneTop;

	@FXML
	private StackPane stackPaneBottom;

	@FXML
	private StackPane stackPaneAdd;

	@FXML
	private StackPane stackPaneDelete;

	@FXML
	private StackPane stackPaneBuild;

	@FXML
	private HBox hBoxWindowButtons;

	@FXML
	private HBox hBoxPortfolio;

	@FXML
	private Button buttonPaypal;

	@FXML
	private Button buttonGitHub;

	@FXML
	private Button buttonBehance;

	@FXML
	private Button buttonMinimize;

	@FXML
	private Button buttonExit;

	@FXML
	private Button buttonAdd;

	@FXML
	private Button buttonDelete;

	@FXML
	private Button buttonBuild;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab tabHostsList;

	@FXML
	private AnchorPane anchorPaneHostsList;

	@FXML
	private TableView<Domain> tableViewHostsList;

	@FXML
	private TableColumn<Domain, String> columnHostsList;

	@FXML
	private Tab tabWhiteList;

	@FXML
	private AnchorPane anchorPaneWhiteList;

	@FXML
	private TableView<Domain> tableViewWhiteList;

	@FXML
	private TableColumn<Domain, String> columnWhiteList;

	@FXML
	private Tab tabMyHosts;

	@FXML
	private AnchorPane anchorPaneMyHosts;

	@FXML
	private TableView<Domain> tableViewMyHosts;

	@FXML
	private TableColumn<Domain, String> columnMyHosts;

	@FXML
	private HBox hBox;

	private ObservableList<Domain> dataHostsList;
	private ObservableList<Domain> dataWhiteList;
	private ObservableList<Domain> dataMyHosts;

	private Database database;

	private AdBlock adblocker;

	private double mouseX;
	private double mouseY;

	@FXML
	private void initialize() {

		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {

				/* Create the database in the folder and create the table */
				database = new Database(Utils.getAppFolder("ar.manumaanu.adblocker") + "/adblocker.db");
				database.createTable();

				/*
				 * If the HOSTSLIST table is empty we fill with some hostsfiles
				 */
				ResultSet rs = database.getResultSet("HOSTSLIST");
				try {
					if (rs.isClosed()) {
						database.insert("HOSTSLIST", "http://adaway.org/hosts.txt");
						database.insert("HOSTSLIST",
								"https://raw.githubusercontent.com/StevenBlack/hosts/master/data/StevenBlack/hosts");
						database.insert("HOSTSLIST", "http://winhelp2002.mvps.org/hosts.txt");
						database.insert("HOSTSLIST", "http://someonewhocares.org/hosts/zero/");
						database.insert("HOSTSLIST",
								"http://pgl.yoyo.org/as/serverlist.php?hostformat=hosts&showintro=0&mimetype=plaintext");
						database.insert("HOSTSLIST", "http://www.malwaredomainlist.com/hostslist/hosts.txt");

					} else {
						rs.close();
					}
					database.closeConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}

				/* Create a new instance of AdBlock class */
				adblocker = new AdBlock();

				return null;
			}

		};

		task.setOnSucceeded(event -> {

			/* Initialize and build the TableView */
			tableViewHostsList.setEditable(true);
			initializeColumns();
			buildTable();

		});

		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();

	}

	@FXML
	public void checkAdminRights() {

		if (!Utils.checkAdminRights()) {
			alertError("The application needs admin rights");
			Stage stage = (Stage) anchorPane.getScene().getWindow();
			stage.close();
		}
	}

	private void initializeColumns() {

		/* Table HOSTSLIST */
		columnHostsList.setCellValueFactory(new PropertyValueFactory<Domain, String>("domain"));
		columnHostsList.setCellFactory(TextFieldTableCell.forTableColumn());

		columnHostsList.setOnEditCommit(new EventHandler<CellEditEvent<Domain, String>>() {
			@Override
			public void handle(CellEditEvent<Domain, String> t) {

				Domain domain = t.getRowValue();

				if (!domain.getDomain().equals(t.getNewValue())) {
					try {
						domain.setDomain(t.getNewValue());
						database.update("HOSTSLIST", domain.getId(), t.getNewValue());
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error updating host");
					}

				}

			}
		});

		/* Table WHITELIST */
		columnWhiteList.setCellValueFactory(new PropertyValueFactory<Domain, String>("domain"));
		columnWhiteList.setCellFactory(TextFieldTableCell.forTableColumn());

		columnWhiteList.setOnEditCommit(new EventHandler<CellEditEvent<Domain, String>>() {
			@Override
			public void handle(CellEditEvent<Domain, String> t) {

				Domain domain = t.getRowValue();

				if (!domain.getDomain().equals(t.getNewValue())) {
					try {
						domain.setDomain(t.getNewValue());
						database.update("WHITELIST", domain.getId(), t.getNewValue());
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error updating whitelist");
					}

				}

			}
		});

		/* Table MYHOSTS */
		columnMyHosts.setCellValueFactory(new PropertyValueFactory<Domain, String>("domain"));
		columnMyHosts.setCellFactory(TextFieldTableCell.forTableColumn());

		columnMyHosts.setOnEditCommit(new EventHandler<CellEditEvent<Domain, String>>() {
			@Override
			public void handle(CellEditEvent<Domain, String> t) {

				Domain domain = t.getRowValue();

				if (!domain.getDomain().equals(t.getNewValue())) {
					try {
						domain.setDomain(t.getNewValue());
						database.update("MYHOSTS", domain.getId(), t.getNewValue());
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error updating myhosts");
					}

				}

			}
		});
	}

	private void buildTable() {

		dataHostsList = FXCollections.observableArrayList();

		try {

			ResultSet resultSet = this.database.getResultSet("HOSTSLIST");

			while (resultSet.next()) {

				Domain domain = new Domain(resultSet.getInt("id"), resultSet.getString("website"));

				dataHostsList.add(domain);

			}

			tableViewHostsList.setItems(dataHostsList);
			resultSet.close();
			this.database.closeConnection();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on Building HOSTSLIST");
		}

		dataWhiteList = FXCollections.observableArrayList();

		try {

			ResultSet resultSet = this.database.getResultSet("WHITELIST");

			while (resultSet.next()) {

				Domain domain = new Domain(resultSet.getInt("id"), resultSet.getString("domain"));

				dataWhiteList.add(domain);

			}

			tableViewWhiteList.setItems(dataWhiteList);
			resultSet.close();
			this.database.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on Building WHITELIST");
		}

		dataMyHosts = FXCollections.observableArrayList();

		try {

			ResultSet resultSet = this.database.getResultSet("MYHOSTS");

			while (resultSet.next()) {

				Domain domain = new Domain(resultSet.getInt("id"), resultSet.getString("domain"));

				dataMyHosts.add(domain);

			}
			tableViewMyHosts.setItems(dataMyHosts);
			;
			resultSet.close();
			this.database.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on Building MYHOSTS");
		}
	}

	private void alertError(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		Stage primaryStage = (Stage) anchorPane.getScene().getWindow();
		stage.getIcons().addAll(primaryStage.getIcons());
		alert.setTitle("ADBLOCKER: Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.initModality(Modality.WINDOW_MODAL);
		alert.showAndWait();
	}

	private void alertInformation(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		Stage primaryStage = (Stage) anchorPane.getScene().getWindow();
		stage.getIcons().addAll(primaryStage.getIcons());
		alert.setTitle("ADBLOCKER: Build Done");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private boolean alertDeleteConfirm() {
		boolean confirmation = false;
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		Stage primaryStage = (Stage) anchorPane.getScene().getWindow();
		stage.getIcons().addAll(primaryStage.getIcons());
		alert.setTitle("ADBLOCKER: Delete Confirmation");
		alert.setHeaderText("");
		alert.setContentText("Are you sure you want to delete that item?");
		alert.initModality(Modality.WINDOW_MODAL);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			confirmation = true;
		} else {
			// DO NOTHING
		}

		return confirmation;
	}

	@FXML
	private void anchorPaneOnDragDetected(MouseEvent mouseEvent) {
		anchorPane.getScene().getWindow().setX(mouseEvent.getScreenX() + mouseX);
		anchorPane.getScene().getWindow().setY(mouseEvent.getScreenY() + mouseY);
	}

	@FXML
	private void anchorPaneOnMousePressed(MouseEvent mouseEvent) {

		mouseX = anchorPane.getScene().getWindow().getX() - mouseEvent.getScreenX();
		mouseY = anchorPane.getScene().getWindow().getY() - mouseEvent.getScreenY();
	}

	@FXML
	private void buttonAddOnAction() {

		Dialog<String> dialogAdd = new Dialog<>();

		Stage primaryStage = (Stage) anchorPane.getScene().getWindow();
		Stage stage = (Stage) dialogAdd.getDialogPane().getScene().getWindow();
		stage.getIcons().addAll(primaryStage.getIcons());

		String titleText = ("Add a HOST site");
		String headerText = ("Adding a website to fetch blocks");
		String labelAddText = ("HOSTS:");
		String exampleText = ("http://example.com/hosts.txt");

		if (this.tabPane.getSelectionModel().getSelectedItem() == this.tabWhiteList) {
			titleText = ("Add a WHITELIST Domain");
			headerText = ("Adding a domain to be whitelisted");
			labelAddText = ("DOMAIN:");
			exampleText = ("example.com");
		} else if (this.tabPane.getSelectionModel().getSelectedItem() == this.tabMyHosts) {
			titleText = ("Add a Block Domain");
			headerText = ("Adding a domain to be blacklisted");
			labelAddText = ("DOMAIN:");
			exampleText = ("example.com");
		}

		dialogAdd.setTitle(titleText);
		dialogAdd.setHeaderText(headerText);
		dialogAdd.setResizable(false);

		Label labelAdd = new Label(labelAddText + " ");
		TextField textField = new TextField();
		Label labelExample = new Label(exampleText);
		labelExample.setPadding(new Insets(10, 0, 0, 0));
		labelExample.setTextFill(Color.GREY);

		GridPane grid = new GridPane();
		grid.add(labelAdd, 1, 1);
		grid.add(textField, 2, 1);
		grid.add(labelExample, 2, 2);
		dialogAdd.getDialogPane().setContent(grid);

		ButtonType buttonTypeOk = new ButtonType("Add", ButtonData.OK_DONE);
		dialogAdd.getDialogPane().getButtonTypes().add(buttonTypeOk);

		dialogAdd.setResultConverter(dialogButton -> {
			if (dialogButton == buttonTypeOk) {
				return new String(textField.getText());
			}
			return null;
		});
		Optional<String> result = dialogAdd.showAndWait();

		result.ifPresent(domain -> {
			if (!domain.trim().isEmpty()) {
				if (this.tabPane.getSelectionModel().getSelectedItem() == this.tabHostsList) {
					boolean repeated = false;
					for (Domain domainInTable : tableViewHostsList.getItems()) {
						if (domainInTable.getDomain().equals(domain)) {
							repeated = true;
						}
					}
					if (!repeated) {
						this.database.insert("HOSTSLIST", domain);
					} else {
						alertError("That domain already exists");
					}

				} else if (this.tabPane.getSelectionModel().getSelectedItem() == this.tabWhiteList) {

					boolean repeated = false;
					for (Domain domainInTable : tableViewWhiteList.getItems()) {
						if (domainInTable.getDomain().equals(domain)) {
							repeated = true;
						}
					}
					if (!repeated) {
						this.database.insert("WHITELIST", domain);
					} else {
						alertError("That domain already exists");
					}
				} else {
					boolean repeated = false;
					for (Domain domainInTable : tableViewMyHosts.getItems()) {
						if (domainInTable.getDomain().equals(domain)) {
							repeated = true;
						}
					}
					if (!repeated) {
						this.database.insert("MYHOSTS", domain);
					} else {
						alertError("That domain already exists");
					}

				}

				buildTable();
			} else {
				alertError("The text field is empty");
			}

		});

	}

	@FXML
	private void buttonDeleteOnAction() {

		if (this.tabPane.getSelectionModel().getSelectedItem() == this.tabHostsList) {
			Domain domain = tableViewHostsList.getSelectionModel().getSelectedItem();
			if (domain != null) {
				if (alertDeleteConfirm()) {
					this.database.delete("HOSTSLIST", domain.getId());
					tableViewHostsList.getSelectionModel().clearSelection();
				}
			}
		} else if (this.tabPane.getSelectionModel().getSelectedItem() == this.tabWhiteList) {
			Domain domain = tableViewWhiteList.getSelectionModel().getSelectedItem();
			if (domain != null) {
				if (alertDeleteConfirm()) {
					this.database.delete("WHITELIST", domain.getId());
					tableViewWhiteList.getSelectionModel().clearSelection();
				}
			}
		} else {
			Domain domain = tableViewMyHosts.getSelectionModel().getSelectedItem();
			if (domain != null) {
				if (alertDeleteConfirm()) {
					this.database.delete("MYHOSTS", domain.getId());
					tableViewMyHosts.getSelectionModel().clearSelection();
				}
			}
		}

		buildTable();
	}

	@FXML
	private void buttonBuildOnAction() {

		tableViewHostsList.setEditable(false);
		tableViewWhiteList.setEditable(false);
		tableViewMyHosts.setEditable(false);
		buttonAdd.setDisable(true);
		buttonDelete.setDisable(true);
		buttonBuild.setDisable(true);
		progressBar.progressProperty().unbind();
		progressBar.setProgress(0);

		if (Utils.getOperativeSystem() == OSEnum.MAC) {
			alertError("Unsupported Operative System");
			Stage stage = (Stage) anchorPane.getScene().getWindow();
			stage.close();
		}

		Task<int[]> task = new Task<int[]>() {
			@Override
			public int[] call() {
				int stats[] = new int[5];
				try {
					int max = 8 + database.getCount("HOSTSLIST");
					int i = 0;

					stats[0] = adblocker.fetchCurrentHostFile();
					i++;
					updateProgress(i, max);

					stats[1] = adblocker.fetchAdBlockerList();
					i++;
					updateProgress(i, max);

					ResultSet resultSet = database.getResultSet("HOSTSLIST");
					while (resultSet.next()) {
						adblocker.fetchOnlineHostFile(resultSet.getString("website"));
						i++;
						updateProgress(i, max);
					}
					resultSet.close();
					database.closeConnection();

					stats[2] = adblocker.cleanOnlineHostsList();
					i++;
					updateProgress(i, max);

					List<String> whiteList = new ArrayList<String>();
					resultSet = database.getResultSet("WHITELIST");
					while (resultSet.next()) {
						whiteList.add(resultSet.getString("domain"));
					}
					resultSet.close();
					database.closeConnection();
					stats[3] = adblocker.addToWhiteList(whiteList);
					i++;
					updateProgress(i, max);

					List<String> myHosts = new ArrayList<String>();
					resultSet = database.getResultSet("MYHOSTS");
					while (resultSet.next()) {
						myHosts.add(resultSet.getString("domain"));
					}
					resultSet.close();
					database.closeConnection();
					stats[4] = adblocker.addMyList(myHosts);
					i++;
					updateProgress(i, max);

					// adblocker.backupCurrentHosts();
					// i++;
					// updateProgress(i, max);
					//
					// adblocker.makeNewHostFile();
					// i++;
					// updateProgress(i, max);

					database.optimize();
					i++;
					updateProgress(i, max);

				} catch (Exception e) {
					e.printStackTrace();
				}
				return stats;
			}

		};

		task.setOnSucceeded(event -> {
			int[] stats = task.getValue();
			if (Utils.getOperativeSystem() == OSEnum.WINDOWS) {
				alertInformation("Old Host File Fetched Lines: " + stats[0] + " \n" + "AdBlocker Hosts Fetched Lines: "
						+ stats[1] + " \n" + "Online Hosts Fetched Lines: " + stats[2] + " \n"
						+ "Whitelist Added Lines: " + stats[3] + " \n" + "Blacklist Added Lines: " + stats[4] + " \n");
			} else {
				alertInformation("Old Host File Fetched Lines: " + stats[0] + " \n" + "AdBlocker Hosts Fetched Lines: "
						+ stats[1] + " \n" + "Online Hosts Fetched Lines: " + stats[2] + " \n"
						+ "Whitelist Added Lines: " + stats[3] + " \n" + "Blacklist Added Lines: " + stats[4]);
			}
			tableViewHostsList.setEditable(true);
			tableViewWhiteList.setEditable(true);
			tableViewMyHosts.setEditable(true);
			buttonAdd.setDisable(false);
			buttonDelete.setDisable(false);
			buttonBuild.setDisable(false);
		});

		progressBar.progressProperty().bind(task.progressProperty());
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();

	}

	@FXML
	private void buttonExitOnAction() {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
		stage.close();

	}

	@FXML
	private void buttonMinimizeOnAction() {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
		stage.setIconified(true);
	}

	@FXML
	private void buttonGitHubOnAction() {

		Utils.openDocument("https://github.com/MaanuVazquez");

	}

	@FXML
	private void buttonBehanceOnAction() {

		Utils.openDocument("https://www.behance.net/saki");
	}

	@FXML
	private void buttonPaypalOnAction() {

		Utils.openDocument("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=L9VQQWFEGGSAU");

	}

}
