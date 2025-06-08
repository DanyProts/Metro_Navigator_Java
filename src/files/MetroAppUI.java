package files;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MetroAppUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Загрузка изображения
        Image image = new Image("files/Gui_laba2.png");  // Укажи путь к своему изображению
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(800); // Увеличено
        imageView.setPreserveRatio(true);

        // Поля ввода
        TextField fromField = new TextField();
        fromField.setPromptText("Введите начальную станцию");

        TextField toField = new TextField();
        toField.setPromptText("Введите конечную станцию");

        Button searchButton = new Button("Найти путь");

        // Обработчик кнопки
        searchButton.setOnAction(e -> {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();
            System.out.println("Станция отправления: " + from);
            System.out.println("Станция назначения: " + to);
        });

        VBox inputBox = new VBox(10, fromField, toField, searchButton);
        inputBox.setStyle("-fx-padding: 20;");

        HBox root = new HBox(30, imageView, inputBox);
        root.setStyle("-fx-padding: 30;");

        // Увеличенное окно
        Scene scene = new Scene(root, 1600, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Поиск пути на метро");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
