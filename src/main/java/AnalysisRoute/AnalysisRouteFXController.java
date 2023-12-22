package AnalysisRoute;

import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.TravelMode;

import feoverlay.MapManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AnalysisRouteFXController {

    private static String[] travelModesOptions = new String[] { "Fastest", "Shortest" };
    @FXML private ComboBox<TravelMode> travelModes;
    @FXML private ToggleGroup toggleGroup2;

    @FXML
    private void initialize() {
        //travelModes.getItems().addAll(travelModesOptions);
        //travelModes.getSelectionModel().select("Fastest");
        travelModes.setConverter(new StringConverter<TravelMode>() {
            @Override
            public String toString(TravelMode travelMode) {
                return travelMode != null ? travelMode.getName() : "";
            }
            @Override
            public TravelMode fromString(String fileName) {
                return null;
            }
        });
        travelModes.getSelectionModel().selectedItemProperty().addListener(o -> {
            MapManager.mapController.setTravelMode(travelModes.getSelectionModel().getSelectedItem());
        });

        toggleGroup2.selectedToggleProperty().addListener(o -> {
            switchTravelMode();               
        });

        chkHightway.selectedProperty().addListener(o -> {
            switchTravelMode();               
        });
        
        txtSpeed.addEventFilter(KeyEvent.KEY_RELEASED, event -> {        	
        	if (event.getCode() == KeyCode.ENTER) {
        		//System.out.print(",key:"+event.getCode());
        		MapManager.mapController.setTravelMode(travelModes.getSelectionModel().getSelectedItem());
        	}
        });

    }

        
    public void addAllTravelModes( RouteTask routeTask ) {
        travelModes.getItems().addAll(routeTask.getRouteTaskInfo().getTravelModes());
    }

    @FXML private ToggleButton btnAddStop;
    public boolean btnAddStop_isSelected() {
        return btnAddStop.isSelected();
    }
    @FXML private ToggleButton btnAddBarrier;
    public boolean btnAddBarrier_isSelected() {
        return btnAddBarrier.isSelected();
    }

    @FXML private TextField barrierSize ;
    public double getBarrierSize() {
        return( Double.valueOf(barrierSize.getText()) );
    }

    @FXML private ListView<String> directionsList;
    public void directionsList_clear() {
        directionsList.getItems().clear();
    }
    public void  directionsList_add(String directionText) {
        directionsList.getItems().add(directionText);
    }
    @FXML private TitledPane routeInformationTitledPane;
    public void routeInformationTitledPane_setText(String output) {
        routeInformationTitledPane.setText(output);
    }

    @FXML private Button btnReset;
    public void btnReset_setDisable(boolean bo) {
        btnReset.setDisable(bo);
    }

    @FXML private void clearRouteAndGraphics() {
        MapManager.mapController.clearRouteAndGraphics();
    }
    
    @FXML
    private TextField txtSearch;
    
    @FXML
    public void btnSearch_click() {
        if (txtSearch.getText().trim() != "")
            MapManager.mapController.searchRoad(txtSearch.getText().trim());
    }
    
    @FXML
    public void btnClear_click() {
        MapManager.mapController.clearSearchResult();
    }

    @FXML private RadioButton optSpeed;
    public boolean optSpeed_isSelected() {
        return optSpeed.isSelected();
    }
    @FXML private RadioButton optBigCar;
    public boolean optBigCar_isSelected() {
        return optBigCar.isSelected();
    }
    @FXML private RadioButton optSmallCar;
    public boolean optSmallCar_isSelected() {
        return optSmallCar.isSelected();
    }
    @FXML private RadioButton optMen;
    public boolean optMen_isSelected() {
        return optMen.isSelected();
    }
    @FXML private TextField txtSpeed;
    public double getMoveSpeed() {
        return( Double.valueOf(txtSpeed.getText()) );
    }
    @FXML private CheckBox chkHightway;
    public boolean chkHightway_isSelected() {
        return chkHightway.isSelected();
    }

    public void selectTravelModeByName(String travelName)
    {
        TravelMode getTravel = null;
        for(TravelMode travel: travelModes.getItems() )
        {
            if( travel.getName().equals(travelName) )
            {
                getTravel = travel;
                break;
            }
        }
        if( getTravel != null )
            travelModes.getSelectionModel().select(getTravel);
    }

    // 依目前設定變更分析模式
    public void switchTravelMode() {
       if( optMen_isSelected() ) {
           selectTravelModeByName("行人");
       }
       else if( optSpeed_isSelected() ) {
           if( !chkHightway_isSelected() )
               selectTravelModeByName("最短距離");
           else
               selectTravelModeByName("最短距離_避走高速公路");
       }
       else if( optBigCar_isSelected() ) {
           if( !chkHightway_isSelected() )
               selectTravelModeByName("大貨車");
           else
               selectTravelModeByName("大貨車_避走高速公路");
       }
       else if( optSmallCar_isSelected() ) {
           if( !chkHightway_isSelected() )
               selectTravelModeByName("小客車");
           else
               selectTravelModeByName("小客車_避走高速公路");
       }
    }

}
