package files;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

public class MetroMapApp extends Application {

    private static final double CENTER_X = 550;
    private static final double CENTER_Y = 550;
    private static final double RADIUS = 400;

    public static void showMap(String from, String to) {
        List<TreasurePathFinder.MetroStation> stationsToDraw = TreasurePathFinder.findPathAndBuildStations(from, to);
        double travelTime = 0.0;
        if (TreasurePathFinder.totalTime!=null){
            travelTime = TreasurePathFinder.totalTime;
        };

        Stage stage = new Stage();
        Pane root = new Pane();


        for (TreasurePathFinder.MetroStation station : stationsToDraw) {
            station.computeCoordinatesIfCircle();
        }


        for (var s : stationsToDraw) {
            drawStation(root, s);
        }


        for (int i = 0; i < stationsToDraw.size() - 1; i++) {
            var s1 = stationsToDraw.get(i);
            var s2 = stationsToDraw.get(i + 1);
            if (s1.color.equals(s2.color)) {
                if (s1.isCircle() && s2.isCircle()) {
                    drawArc(root, s1, s2, s1.color);
                } else {
                    drawLine(root, s1, s2, s1.color);
                }
            } else {
                drawTransfer(root, s1, s2);
            }
        }


        drawTravelTimeText(root, travelTime, 1000, 70);

        Button closeButton = new Button("Закрыть");
        closeButton.setLayoutX(900);
        closeButton.setLayoutY(60);
        closeButton.setOnAction(e -> stage.close());
        root.getChildren().add(closeButton);

        Scene scene = new Scene(root, 1200, 1200);
        stage.setTitle("Маршрут метро");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }


    private static void drawLine(Pane root, TreasurePathFinder.MetroStation s1, TreasurePathFinder.MetroStation s2, Color color) {
        Line line = new Line(s1.x, s1.y, s2.x, s2.y);
        line.setStroke(color);
        line.setStrokeWidth(6);
        root.getChildren().add(line);
    }
    private static void drawTravelTimeText(Pane root, double time, double x, double y) {
        String textContent = String.format("⏱ Время маршрута: %.1f мин.", time);
        Text timeText = new Text(x, y, textContent);
        timeText.setFill(Color.BLACK);
        timeText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        root.getChildren().add(timeText);
    }

    private static void drawArc(Pane root, TreasurePathFinder.MetroStation s1, TreasurePathFinder.MetroStation s2, Color color) {
        double startAngle = 360 - s1.angleDeg;
        double endAngle = 360 - s2.angleDeg;
        double sweep = (endAngle - startAngle + 360) % 360;
        if (sweep > 180) sweep -= 360;

        Arc arc = new Arc(CENTER_X, CENTER_Y, RADIUS, RADIUS, startAngle, sweep);
        arc.setType(ArcType.OPEN);
        arc.setStroke(color);
        arc.setStrokeWidth(6);
        arc.setFill(null);
        root.getChildren().add(arc);
    }

    private static void drawTransfer(Pane root, TreasurePathFinder.MetroStation s1, TreasurePathFinder.MetroStation s2) {
        Line line = new Line(s1.x, s1.y, s2.x, s2.y);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(5);
        line.getStrokeDashArray().addAll(10.0, 10.0);
        root.getChildren().add(line);
    }

    private static void drawStation(Pane root, TreasurePathFinder.MetroStation s) {
        Circle circle = new Circle(s.x, s.y, 7, Color.WHITE);
        circle.setStroke(s.color);
        circle.setStrokeWidth(3);
        Text text = new Text(s.x + 10, s.y - 5, s.name);
        root.getChildren().addAll(circle, text);
    }

    @Override
    public void start(Stage primaryStage) {
        Image image = new Image("files/Gui_laba2.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(800);
        imageView.setPreserveRatio(true);

        TextField fromField = new TextField();
        fromField.setPromptText("Введите начальную станцию");

        TextField toField = new TextField();
        toField.setPromptText("Введите конечную станцию");

        Label fromErrorLabel = new Label();
        fromErrorLabel.setTextFill(Color.RED);

        Label toErrorLabel = new Label();
        toErrorLabel.setTextFill(Color.RED);

        Button searchButton = new Button("Найти путь");

        searchButton.setOnAction(e -> {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();

            // Очистка прошлых сообщений об ошибках
            fromErrorLabel.setText("");
            toErrorLabel.setText("");

            boolean valid = true;

            if (TreasurePathFinder.getStationIdByName(from) == null) {
                fromErrorLabel.setText("Станции не существует, попробуйте снова!");
                valid = false;
            }
            if (TreasurePathFinder.getStationIdByName(to) == null) {
                toErrorLabel.setText("Станции не существует, попробуйте снова!");
                valid = false;
            }

            if (valid) {
                showMap(from, to);
            }
        });

        VBox fromBox = new VBox(5, fromField, fromErrorLabel);
        VBox toBox = new VBox(5, toField, toErrorLabel);
        VBox inputBox = new VBox(10, fromBox, toBox, searchButton);
        inputBox.setStyle("-fx-padding: 20;");

        HBox root = new HBox(30, imageView, inputBox);
        root.setStyle("-fx-padding: 30;");

        Scene scene = new Scene(root, 1600, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Поиск пути на метро");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }



    public static void main(String[] args) {
        Application.launch(args);
    }
}
