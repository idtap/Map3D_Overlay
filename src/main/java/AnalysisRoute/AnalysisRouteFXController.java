package AnalysisRoute;

import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.TravelMode;

import feoverlay.MapManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;

public class AnalysisRouteFXController {

    private static String[] travelModesOptions = new String[] { "Fastest", "Shortest" };
    @FXML private ComboBox<TravelMode> travelModes;

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
}
