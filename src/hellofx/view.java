package hellofx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public class view<E> {

     private TableView tableview;
     private VBox leftVBox;
     private ObservableList<ObservableList> data;
      Connection con;
      private List<TextField> textFields = new ArrayList<>();
      private List<ComboBox> combos = new ArrayList<>();
      private List<String> coloumnsname = new ArrayList<>();
      private List<String> fkcoloum = new ArrayList<>();
      private List<String>  selecteddata = new ArrayList<>();
      private List<String> searchdata = new ArrayList<>();

      
      List<String> primaryKeyColumns ;
      private  String tablename;
      Statement stmt;
      ResultSet rs;

      private Stage stage; // New field to store the Stage reference

    public view(String tablename) {
        this.tablename = tablename;
    }
    
   
    public void buildData(String Query , TableView tableView , VBox leftVBox , VBox rightVBox) {
        data = FXCollections.observableArrayList();
        if(!leftVBox.getChildren().isEmpty()|| !tableView.getColumns().isEmpty()||!rightVBox.getChildren().isEmpty()||!combos.isEmpty()||!selecteddata.isEmpty() ){
            leftVBox.getChildren().clear();
            rightVBox.getChildren().clear();
            textFields.clear();
            combos.clear();
            selecteddata.clear();
            tableView.getColumns().clear();

        }
        

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
             con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cars", "root", "");
           stmt = con.createStatement();
             rs = stmt.executeQuery(Query);

            ResultSet foreignKeys = con.getMetaData().getImportedKeys(null, null, tablename);
            Statement stmt2 ;
            ResultSet ss ;
            
            while(foreignKeys.next()){
              String   pkTableName = foreignKeys.getString("PKTABLE_NAME");
               String  pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
              String   fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");

                String sql = "SELECT " + pkColumnName + " FROM " + pkTableName;

                 ComboBox<E> comboBox = new ComboBox<E>();
                 comboBox.setPromptText("Select a "+ fkColumnName);
                comboBox.setStyle("-fx-font-size: 14px;"); 

                       stmt2 = con.prepareStatement(sql);
                       ss = stmt2.executeQuery(sql);
                       fkcoloum.add(fkColumnName);
                
                    while (ss.next()) {
                        String data = ss.getString(1); 
                        comboBox.getItems().add((E) data);

                    }
                    combos.add(comboBox);
                    
                }
            /**
             * ********************************
             * TABLE COLUMN ADDED DYNAMICALLY *
             *********************************
             */

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                try {
                    tableview.getColumns().addAll(col);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Column [" + i + "] ");

                Label label = new Label(rs.getMetaData().getColumnName(i + 1));
                label.setStyle("-fx-font-size: 18");
                leftVBox.getChildren().add(label);
                coloumnsname.add(label.getText());

                if (fkcoloum.contains(rs.getMetaData().getColumnName(i + 1))) {
                    int index = fkcoloum.indexOf(rs.getMetaData().getColumnName(i + 1));
                    rightVBox.getChildren().add(combos.get(index));
                     
                }
                else {
                    TextField textField = new TextField();
                    textField.setStyle("-fx-font-size: 15");
                    textField.setPromptText(rs.getMetaData().getColumnName(i + 1));
                    rightVBox.getChildren().add(textField);
                    textFields.add(textField);
                }
            }

            /**
             * ******************************
             * Data added to ObservableList *
             *******************************
             */
            while (rs.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            //FINALLY ADDED TO TableView
            tableview.setItems(data);

            if (!searchdata.isEmpty()) {
                for (int i = 0; i < searchdata.size(); i++) {
                    if (i < rightVBox.getChildren().size()) {
                        javafx.scene.Node node = rightVBox.getChildren().get(i);
                        if (node instanceof TextField) {
                            TextField textField = (TextField) node;
                            textField.setText(searchdata.get(i));
                        } else if (node instanceof ComboBox) {
                            ComboBox<E> comboBox = (ComboBox<E>) node;
                            comboBox.getSelectionModel().select((E) searchdata.get(i));
                        }
                    }
                }
                searchdata.clear();

            }
        } catch (Exception e) {
            e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Data Not Found");
            System.out.println("Error on Building Data");
        }
        boolean isRowSelected = false;

        tableView.setOnMouseClicked(event -> { 
            ObservableList<String> selectedRow = (ObservableList<String>) tableView.getSelectionModel().getSelectedItem();

            if (selectedRow != null) {
                
                for (int i = 0; i < selectedRow.size(); i++) {
                    javafx.scene.Node node = rightVBox.getChildren().get(i);
                    if (node instanceof TextField) {
                        TextField textField = (TextField) node;
                            textField.setText(selectedRow.get(i));
                            selecteddata.add(textField.getText());

                    } else {
                        ComboBox<E> comboBox = (ComboBox<E>) node;
                        comboBox.getSelectionModel().select((E) selectedRow.get(i));
                        selecteddata.add(comboBox.getValue().toString());
                    }
                }
            }
        });
        tableview.setOnKeyPressed(e -> {
             ObservableList<String> selectedRow = (ObservableList<String>) tableView.getSelectionModel().getSelectedItem();
        
            if (selectedRow != null) {
                for (int i = 0; i < selectedRow.size(); i++) {
                    javafx.scene.Node node = rightVBox.getChildren().get(i);
                    if (node instanceof TextField) {
                        TextField textField = (TextField) node;
                            textField.setText(selectedRow.get(i));
                            selecteddata.add(textField.getText());
                        
                    } else {
                        ComboBox<E> comboBox = (ComboBox<E>) node;
                        comboBox.getSelectionModel().select((E) selectedRow.get(i));
                        selecteddata.add(comboBox.getValue().toString());
                    }
                }
            }
        });

    }
        public boolean Valid(String data, int columnIndex) {
            try {
                String type = rs.getMetaData().getColumnTypeName(columnIndex + 1);
                int size = rs.getMetaData().getPrecision(columnIndex + 1);
        
                if (!matchesColumnType(data, type)) {
                    showErrorMessage("Mismatch Data Type", "The data type in " + coloumnsname.get(columnIndex) + " should be " + type, tablename);
                    return false;
                }
        
                if (data.length() > size) {
                    showErrorMessage("Mismatch Data Size", "The data in " + coloumnsname.get(columnIndex) + " must be less than " + size, tablename);
                    return false;
                }    
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        
        private boolean matchesColumnType(String data, String columnType) {
            switch (columnType.toUpperCase()) {
                case "VARCHAR":
                    return data instanceof String;
                case "INT":
                    try {
                        Integer.parseInt(data);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    case "DECIMAL":
                           try {
                          new java.math.BigDecimal(data);
                             return true;
                            } catch (NumberFormatException e) {
                             return false;
                               }
                default:
                    return true; 
            }
        }
               

        public AnchorPane createTableView() {

            AnchorPane root = new AnchorPane();
           
            VBox rightVBox = new VBox(10);
            rightVBox.setLayoutX(250);
            rightVBox.setLayoutY(56);
            rightVBox.setPrefHeight(214);
            rightVBox.setPrefWidth(250);
    
              leftVBox = new VBox(15);
            leftVBox.setLayoutX(150);
            leftVBox.setLayoutY(54);
            leftVBox.setPrefHeight(214);
            leftVBox.setPrefWidth(76);
    
            tableview = new TableView();
            tableview.setLayoutX(600);
            tableview.setLayoutY(54);
            tableview.setPrefHeight(319);
            tableview.setPrefWidth(468);
            tableview.setStyle("-fx-font-size: 14px;"); 

              Font font = Font.font( "Inter", 14);
                
            Button btnInsert = new Button("Insert");
            btnInsert.setLayoutX(170);
            btnInsert.setLayoutY(300);
            btnInsert.setPrefWidth(100);
            btnInsert.setOnAction(e -> { 
                buildData(Insert(rightVBox), tableview ,leftVBox , rightVBox);
            });
    
            Button btnSearch = new Button("Search");
            btnSearch.setLayoutX(280);
            btnSearch.setLayoutY(300);
            btnSearch.setPrefWidth(100); 
            btnSearch.setOnAction(e -> {
            buildData(search(rightVBox) , tableview ,leftVBox , rightVBox );
            });
           
     
            Button btnDelete = new Button("Delete");
            btnDelete.setLayoutX(390);
            btnDelete.setLayoutY(300);
            btnDelete.setPrefWidth(100);
            btnDelete.setOnAction(e -> {
            buildData(Delete(rightVBox) , tableview ,leftVBox , rightVBox );
            });
    
            
            Button btnUpdate = new Button("Update");
            btnUpdate.setLayoutX(170);
            btnUpdate.setLayoutY(335); 
            btnUpdate.setPrefWidth(210);
            btnUpdate.setOnAction(e -> {
            buildData(update(rightVBox),tableview ,leftVBox , rightVBox );
            });
             Button btnClear = new Button("Clear");
            btnClear.setLayoutX(390);
            btnClear.setLayoutY(335); 
            btnClear.setPrefWidth(100);
            btnClear.setOnAction(e -> {
            buildData(Clear(rightVBox),tableview ,leftVBox , rightVBox );
            });
            
            btnInsert.setFont(font);
            btnSearch.setFont(font);
            btnDelete.setFont(font);
            btnUpdate.setFont(font);
            btnClear.setFont(font);

             buildData("select * from "+ tablename, tableview ,leftVBox , rightVBox );
    
    
            root.getChildren().addAll(leftVBox, rightVBox, tableview ,btnInsert ,btnSearch , btnDelete ,btnUpdate , btnClear);
            return root;
            
    
    
        }
    private String Clear(VBox rightVBox) {
        for (javafx.scene.Node node : rightVBox.getChildren()) {
            if (node instanceof TextField) {
                TextField textField = (TextField) node;
                textField.clear();
            } else if (node instanceof ComboBox) {
                ComboBox comboBox = (ComboBox) node;
                comboBox.getSelectionModel().clearSelection();
            }
        }
            return "SELECT * FROM " + tablename;
  
      }


    public String update(VBox rightVBox) {
      
       

        String query = "UPDATE " + tablename + " SET ";
        boolean changesDetected = false;
        int columnIndex = 0;
   
   try{
       for (javafx.scene.Node node : rightVBox.getChildren()) {
           if (node instanceof TextField) {
               TextField textField = (TextField) node;
               String currentValue = textField.getText();
               if (!selecteddata.get(columnIndex).equals(currentValue) && !currentValue.isEmpty()) {
                 if (!Valid(textField.getText(), columnIndex)) {
                            return "SELECT * FROM " + tablename;
                          }
                   if (changesDetected) {
                       query += ", ";
                   }
                   query += coloumnsname.get(columnIndex) + " = '" + currentValue + "'";
                   changesDetected = true;
               }
               
           } else if (node instanceof ComboBox) {
               ComboBox comboBox = (ComboBox) node;
               String currentValue = comboBox.getValue() != null ? comboBox.getValue().toString() : "";
               if (!selecteddata.get(columnIndex).equals(currentValue) && !currentValue.isEmpty()) {
                 if (!Valid(comboBox.getValue().toString(), columnIndex)) {
                   return "SELECT * FROM " + tablename;
                     }
                   if (changesDetected) {
                       query += ", ";
                   }
                   query += coloumnsname.get(columnIndex) + " = '" + currentValue + "'";
                   changesDetected = true;
               }
           }
           columnIndex++;
           
       }

       if (changesDetected) {
           query += " WHERE ";
   
           for (int i = 0; i < selecteddata.size(); i++) {
               String selectedValue = selecteddata.get(i);
               if (i != 0 && !selectedValue.isEmpty()) {
                   query += " AND ";
               }
               if (!selectedValue.isEmpty()) {
                   query += coloumnsname.get(i) + " = '" + selectedValue + "'";
               }
           }
   
           query += " ; ";

           showAlert("Confirmation", "Are you sure you want to update this data?", query, leftVBox, rightVBox, tableview);  
       } else {
        showErrorMessage("No Changes", "No changes were made to update.\n Please update at least one field", tablename);
          
       }
   }catch(Exception e){
        showErrorMessage("Update Data Selection Required", "Please select data to Update", tablename);
   }
       return "SELECT * FROM " + tablename;
   }

  public String Insert(VBox rightVBox) {
     String query = "INSERT INTO " + tablename + " VALUES (";

    int i = 0;
    for (javafx.scene.Node node : rightVBox.getChildren()) {
        if (node instanceof TextField) {
            TextField textField = (TextField) node;
            if (!textField.getText().isEmpty()) {
                if (!Valid(textField.getText(), i)) {
                   return "SELECT * FROM " + tablename;
                }
                else{
                   if (i == 0)
                    query += "'" + textField.getText() + "'";
                   else
                    query += ", '" + textField.getText() + "'";}
            } else {
                return showErrorMessage("Empty Field Detected", "Please fill in all the fields!", tablename);
            }
        } else if (node instanceof ComboBox) {
            ComboBox comboBox = (ComboBox) node;
            if (comboBox.getValue() != null) {
                if (!Valid(comboBox.getValue().toString(), i)) {
                   return "SELECT * FROM " + tablename;
                }
                else{
                if (i == 0)
                    query += "'" + comboBox.getValue().toString() + "'";
                else
                    query += ", '" + comboBox.getValue().toString() + "'";}
            } else {
                return showErrorMessage("ComboBox is Empty", "Please fill in all the ComboBox!", tablename);
            }
        }
        i++;
    }
    query += ");";

    showAlert("Confirmation", "Are you sure you want to add this data?", query, leftVBox, rightVBox, tableview);

        return "SELECT * FROM " + tablename;
}


    
     public String Delete (VBox rightVBox){
            String ss = "DELETE FROM " + tablename + " WHERE ";
            
            int i = 0;
            for (javafx.scene.Node node : rightVBox.getChildren()) {
                if (node instanceof TextField) {
                    TextField textField = (TextField) node;
                    if (!textField.getText().isEmpty()) {
                        if (i != 0) {
                            ss += " AND ";
                        }
                        ss += coloumnsname.get(i) + " = '" + textField.getText() + "'";
                    }
                } else if (node instanceof ComboBox) {
                    ComboBox comboBox = (ComboBox) node;
                    if (comboBox.getValue() != null && !comboBox.getValue().toString().isEmpty()) {
                        if (i != 0) {
                            ss += " AND ";
                        }
                        ss += coloumnsname.get(i) + " = '" + comboBox.getValue().toString() + "'";
                    }
                }
                i++;
            }
            ss += ";";
    
            showAlert("Confirmation", "Are you sure you want to delete this data?", ss, leftVBox, rightVBox, tableview);
        
            return "SELECT * FROM " + tablename;
        }
    

        public String search(VBox rightVBox) {
            String ss = "SELECT * FROM " + tablename + " WHERE ";
            String temp = "SELECT * FROM " + tablename;
            int value = 0;
            int i = 0 ;
                
            for (javafx.scene.Node node : rightVBox.getChildren()) {
                if (node instanceof TextField) {
                    TextField textField = (TextField) node;
                    if (!textField.getText().isEmpty()) {
                         if (!Valid(textField.getText(), i)) {
                            return "SELECT * FROM " + tablename;
                          }
                          else{
                        value = 1;
                        break;}
                    }
                } else if (node instanceof ComboBox) {
                    ComboBox comboBox = (ComboBox) node;
                    if (comboBox.getValue() != null) {
                         if (!Valid(comboBox.getValue().toString(),i )) {
                           return "SELECT * FROM " + tablename;
                            }
                        value = 1;
                        break;
                    }
                }
                i++;
            }
        
            if (value == 0) {
                return temp; 
            } else {
                int index = 0;
                for (javafx.scene.Node node : rightVBox.getChildren()) {
                    if (node instanceof TextField) {
                        TextField textField = (TextField) node;
                        searchdata.add(textField.getText()+"");
                        if (!textField.getText().isEmpty()) {
                            if (ss.length() > 34) {
                                ss += " AND " + coloumnsname.get(index) + " = '" + textField.getText() + "'";
                            } else {
                                ss += coloumnsname.get(index) + " = '" + textField.getText() + "'";
                            }
                        }
                    } else if (node instanceof ComboBox) {
                        ComboBox comboBox = (ComboBox) node;
                        if (comboBox.getValue() != null && !comboBox.getValue().toString().isEmpty()) {
                            searchdata.add(comboBox.getValue().toString());
                            if (ss.length() > 34) {
                                ss += " AND " + coloumnsname.get(index) + " = '" + comboBox.getValue().toString() + "'";
                            } else {
                                ss += coloumnsname.get(index) + " = '" + comboBox.getValue().toString() + "'";
                            }
                        } else {
                            searchdata.add(""); 
                        }
                    }
                    index++;
                }
                return ss;
            }
        }

   
   private String showErrorMessage(String title, String header, String tablename) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("Error Dialog");
    alert.setHeaderText(title);
    alert.setContentText(header);
    alert.showAndWait();
    return "SELECT * FROM " + tablename;
   }

   private void showInformationMessage(String title, String content) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
   }
   private void showAlert(String title, String message, String query, VBox leftVBox, VBox rightVBox, TableView tableView) {
    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmationAlert.setTitle(title);
    confirmationAlert.setHeaderText(null);
    confirmationAlert.setContentText(message);

    ButtonType confirmButtonType = new ButtonType("Yes", ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
    confirmationAlert.getButtonTypes().setAll(confirmButtonType, cancelButtonType);

    confirmationAlert.showAndWait().ifPresent(buttonType -> {
        if (buttonType == confirmButtonType) {
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                showInformationMessage("Success", "Operation Successful!");
                buildData("SELECT * FROM " + tablename, tableView, leftVBox, rightVBox);
            } catch (Exception e) {
                if((((SQLException) e).getErrorCode() == 1022 || ((SQLException) e).getErrorCode() == 1062 || ((SQLException) e).getErrorCode() == 1586)
                && "23000".equals(((SQLException) e).getSQLState())) {
                    showErrorMessage("Duplicate Entry", "Cannot proceed due to a duplicate entry.", tablename);}
                else
                e.printStackTrace();
            }
        }
    });
}
   
}