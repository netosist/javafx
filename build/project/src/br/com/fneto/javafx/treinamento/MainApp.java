package br.com.fneto.javafx.treinamento;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.prefs.Preferences;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import br.com.fneto.javafx.treinamento.model.Person;
import br.com.fneto.javafx.treinamento.model.PersonListWrapper;
import br.com.fneto.javafx.treinamento.view.BirthdayStatisticsController;
import br.com.fneto.javafx.treinamento.view.PersonEditDialogController;
import br.com.fneto.javafx.treinamento.view.PersonOverviewController;
import br.com.fneto.javafx.treinamento.view.RootLayoutController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;

	/**
	 * Os dados como uma observable list de Persons.
	 */
	private ObservableList<Person> personData = FXCollections.observableArrayList();

	/**
	 * Construtor
	 */
	public MainApp() {
	}

	/**
	 * Retorna os dados como uma observable list de Persons. 
	 * @return
	 */
	public ObservableList<Person> getPersonData() {
		return personData;
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("AgendaApp");

		// Set the application icon.
		this.primaryStage.getIcons().add(new Image("file:resources/images/address_book_32.png"));

		initRootLayout();

		showPersonOverview();
	}

	/**
	 * Inicializa o root layout (layout base).
	 */
	public void initRootLayout() {
		try {
			// Carrega o root layout do arquivo fxml.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();

			// Mostra a scene (cena) contendo o root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			
			 // D� ao controller o acesso ao main app.
	        RootLayoutController controller = loader.getController();
	        controller.setMainApp(this);
			
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mostra o person overview dentro do root layout.
	 */
	public void showPersonOverview() {
		try {
			// Carrega o person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/PersonOverview.fxml"));
			AnchorPane personOverview = (AnchorPane) loader.load();

			// Define o person overview dentro do root layout.
			rootLayout.setCenter(personOverview);

			// D� ao controlador acesso � the main app.
			PersonOverviewController controller = loader.getController();
			controller.setMainApp(this);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retorna o palco principal.
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * Abre uma janela para editar detalhes para a pessoa especificada. Se o usu�rio clicar
	 * OK, as mudan�as s�o salvas no objeto pessoa fornecido e retorna true.
	 * 
	 * @param person O objeto pessoa a ser editado
	 * @return true Se o usu�rio clicou OK,  caso contr�rio false.
	 */
	public boolean showPersonEditDialog(Person person) {
		try {
			// Carrega o arquivo fxml e cria um novo stage para a janela popup.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/PersonEditDialog.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			// Cria o palco dialogStage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Edit Person");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/edit_32.png"));

			// Define a pessoa no controller.
			PersonEditDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setPerson(person);

			// Mostra a janela e espera at� o usu�rio fechar.
			dialogStage.showAndWait();

			return controller.isOkClicked();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Retorna o arquivo de prefer�ncias da pessoa, o �ltimo arquivo que foi aberto.
	 * As prefer�ncias s�o lidas do registro espec�fico do SO (Sistema Operacional). 
	 * Se tais pref�rencias n�o puderem  ser encontradas, ele retorna null.
	 * 
	 * @return
	 */
	public File getPersonFilePath() {
	    Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
	    String filePath = prefs.get("filePath", null);
	    if (filePath != null) {
	        return new File(filePath);
	    } else {
	        return null;
	    }
	}

	/**
	 * Define o caminho do arquivo do arquivo carregado atual. O caminho � persistido no
	 * registro espec�fico do SO (Sistema Operacional).
	 * 
	 * @param file O arquivo ou null para remover o caminho
	 */
	public void setPersonFilePath(File file) {
	    Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
	    if (file != null) {
	        prefs.put("filePath", file.getPath());

	        // Update the stage title.
	        primaryStage.setTitle("AgendaApp - " + file.getName());
	    } else {
	        prefs.remove("filePath");

	        // Update the stage title.
	        primaryStage.setTitle("AgendaApp");
	    }
	}
	
	/**
	 * Carrega os dados da pessoa do arquivo especificado. A pessoa atual
	 * ser� substitu�da.
	 * 
	 * @param file
	 */
	public void loadPersonDataFromFile(File file) {
	    try {
	        JAXBContext context = JAXBContext
	                .newInstance(PersonListWrapper.class);
	        Unmarshaller um = context.createUnmarshaller();

	        // Reading XML from the file and unmarshalling.
	        PersonListWrapper wrapper = (PersonListWrapper) um.unmarshal(file);

	        personData.clear();
	        personData.addAll(wrapper.getPersons());

	        // Save the file path to the registry.
	        setPersonFilePath(file);

	    } catch (Exception ex) { // catches ANY exception

	    	Alert alert = new Alert(AlertType.ERROR);
	    	alert.setTitle("Erro");
	    	alert.setHeaderText("Sobre");
	    	alert.setContentText("N�o foi poss�vel carregar dados do arquivo:\n" + file.getPath());
	    	
	    	// Create expandable Exception.
	    	StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	ex.printStackTrace(pw);
	    	String exceptionText = sw.toString();

	    	Label label = new Label("O Stacktrace da excess�o foi:");

	    	TextArea textArea = new TextArea(exceptionText);
	    	textArea.setEditable(false);
	    	textArea.setWrapText(true);

	    	textArea.setMaxWidth(Double.MAX_VALUE);
	    	textArea.setMaxHeight(Double.MAX_VALUE);
	    	GridPane.setVgrow(textArea, Priority.ALWAYS);
	    	GridPane.setHgrow(textArea, Priority.ALWAYS);

	    	GridPane expContent = new GridPane();
	    	expContent.setMaxWidth(Double.MAX_VALUE);
	    	expContent.add(label, 0, 0);
	    	expContent.add(textArea, 0, 1);

	    	// Set expandable Exception into the dialog pane.
	    	alert.getDialogPane().setExpandableContent(expContent);
	    	
	    	alert.showAndWait();

	    }
	}

	/**
	 * Salva os dados da pessoa atual no arquivo especificado.
	 * 
	 * @param file
	 */
	public void savePersonDataToFile(File file) {
	    try {
	        JAXBContext context = JAXBContext
	                .newInstance(PersonListWrapper.class);
	        Marshaller m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	        // Envolvendo nossos dados da pessoa.
	        PersonListWrapper wrapper = new PersonListWrapper();
	        wrapper.setPersons(personData);

	        // Enpacotando e salvando XML  no arquivo.
	        m.marshal(wrapper, file);

	        // Saalva o caminho do arquivo no registro.
	        setPersonFilePath(file);
	    } catch (Exception ex) { // catches ANY exception
	    	Alert alert = new Alert(AlertType.ERROR);
	    	alert.setTitle("Erro");
	    	alert.setHeaderText("Sobre");
	    	alert.setContentText("N�o foi poss�vel salvar os dados do arquivo:" + file.getPath());
	    	
	    	// Create expandable Exception.
	    	StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	ex.printStackTrace(pw);
	    	String exceptionText = sw.toString();

	    	Label label = new Label("O Stacktrace da excess�o foi:");

	    	TextArea textArea = new TextArea(exceptionText);
	    	textArea.setEditable(false);
	    	textArea.setWrapText(true);

	    	textArea.setMaxWidth(Double.MAX_VALUE);
	    	textArea.setMaxHeight(Double.MAX_VALUE);
	    	GridPane.setVgrow(textArea, Priority.ALWAYS);
	    	GridPane.setHgrow(textArea, Priority.ALWAYS);

	    	GridPane expContent = new GridPane();
	    	expContent.setMaxWidth(Double.MAX_VALUE);
	    	expContent.add(label, 0, 0);
	    	expContent.add(textArea, 0, 1);

	    	// Set expandable Exception into the dialog pane.
	    	alert.getDialogPane().setExpandableContent(expContent);
	    	
	    	alert.showAndWait();
	    }
	}
	
	/**
	 * Abre uma janela para mostrar as estat�sticas de anivers�rio.
	 */
	public void showBirthdayStatistics() {
	    try {
	        // Carrega o arquivo fxml e cria um novo palco para o popup.
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(MainApp.class.getResource("view/BirthdayStatistics.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();
	        Stage dialogStage = new Stage();
	        dialogStage.setTitle("Birthday Statistics");
	        dialogStage.initModality(Modality.WINDOW_MODAL);
	        dialogStage.initOwner(primaryStage);
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        // Define a pessoa dentro do controller.
	        BirthdayStatisticsController controller = loader.getController();
	        controller.setPersonData(personData);

	        dialogStage.show();

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
