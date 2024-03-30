package hellofx;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class base extends Application {

    private BorderPane root;
    private VBox sideNav;
    private VBox contentArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DB Builder");

        root = new BorderPane();

        sideNav = createSideNavBar();
        sideNav.setMinWidth(200);
        sideNav.setStyle("-fx-background-color: #2c3e50;");

        contentArea = new VBox();
        contentArea.setPadding(new Insets(50));
        Text welcomeText = new Text(" Welcome to the Car Database Application! \n \n Select a table from the sidebar to start operations. ");
        welcomeText.setFont(Font.font("Inter", FontWeight.BOLD, 22));
        contentArea.getChildren().add(welcomeText);

        root.setLeft(sideNav);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1000, 500);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private VBox createSideNavBar() {
        VBox sideNavBar = new VBox();
        sideNavBar.setSpacing(10);
        sideNavBar.setPadding(new Insets(20, 10, 10, 10));

        String[] tableNames = {"address", "car", "car_part", "customer", "device", "manufacture", "orders"};
        for (String item : tableNames) {
            Button button = createButton(item);
            sideNavBar.getChildren().add(button);
        }

        return sideNavBar;
    }

    private Button createButton(String itemName) {
        Button button = new Button(itemName);
        button.setTextFill(Color.WHITE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPrefWidth(200);
        Font font = Font.font( "Inter", 16);
        button.setFont(font);
        button.setPadding(new Insets(15, 15, 15, 15)); // Adjust the left padding as needed
        button.setStyle("-fx-background-color: transparent; -fx-border-width: 0px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #34495e;"));
        button.setOnMouseExited(e -> {
            if (!button.isFocused()) {
                button.setStyle("-fx-background-color: transparent; -fx-border-width: 0px;");
            }
        });
        button.setOnAction(e -> {
            for (var node : sideNav.getChildren()) {
                if (node instanceof Button) {
                    ((Button) node).setStyle("-fx-background-color: transparent; -fx-border-width: 0px;");
                }
            }
            button.setStyle("-fx-background-color: #34495e;");
            button.requestFocus();

            displayContent(itemName);
        });

        return button;
    }

    private void displayContent(String content) {
        view rr = new view(content);
    
        contentArea.getChildren().clear();
    
        Text selectedText = new Text(content.toUpperCase() + " Table ");
        selectedText.setFont(Font.font("Inter", FontWeight.BOLD, 22));
        selectedText.setFill(Color.DARKSLATEGRAY);
        Text sigText = new Text("\n\n\n\n\n\n\n\n\n\n\n\n\n \u00a9 AyahAlTamimi 2023");
        sigText.setFont(Font.font("Inter", FontWeight.NORMAL, 16));
    
        Node tableViewNode = rr.createTableView();
    
        VBox centeringBox = new VBox(selectedText, tableViewNode, sigText);
        centeringBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(centeringBox);
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
}