package controller;

import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.control.ComboBox;

public class PolygonFXController {
    @FXML
    private ColorPicker ParamPolygonLineColor ;
    @FXML
    private ColorPicker ParamPolygonColor ;
    @FXML
    private TextField ParamPolygonLineSize ;
    @FXML
    private ComboBox ParamPolygonLineStyle;
    @FXML
    private ComboBox ParamPolygonStyle;

    private static String[] polygonlineStyleOptions = new String[] { "實線", "虛線" };
    private static String[] polygonStyleOptions = new String[] { "網底", "填滿" };

    public void loadParameter() {
        Preferences prefs = Preferences.userNodeForPackage(PolygonFXController.class);
        String polygonlineColor = prefs.get("polygonlineColor", null);
        if (polygonlineColor != null) {
            ParamPolygonLineColor.setValue(Color.web(polygonlineColor));
        }
        String polygonColor = prefs.get("polygonColor", null);
        if (polygonColor != null) {
            ParamPolygonColor.setValue(Color.web(polygonColor));
        }
        String polygonlineSize = prefs.get("polygonlineSize", null);
        if (polygonlineSize != null) {
            ParamPolygonLineSize.setText(polygonlineSize);
        }
        ParamPolygonLineStyle.getItems().addAll(polygonlineStyleOptions);
        String polygonlineStyle = prefs.get("polygonlineStyle", null);
        if ( polygonlineStyle != null) {
            ParamPolygonLineStyle.getSelectionModel().select(polygonlineStyle);
        }
        else
            ParamPolygonLineStyle.getSelectionModel().select("實線");
        ParamPolygonStyle.getItems().addAll(polygonStyleOptions);
        String polygonStyle = prefs.get("polygonStyle", null);
        if ( polygonStyle != null) {
            ParamPolygonStyle.getSelectionModel().select(polygonStyle);
        }
        else
            ParamPolygonStyle.getSelectionModel().select("網底");
    }

    public void saveParameter() {
        Preferences prefs = Preferences.userNodeForPackage(PolygonFXController.class);
        prefs.put("polygonlineColor", ParamPolygonLineColor.getValue().toString()  );
        prefs.put("polygonColor", ParamPolygonColor.getValue().toString()  );
        prefs.put("polygonlineSize", ParamPolygonLineSize.getText() );
        prefs.put("polygonlineStyle", polygonlineStyleOptions[ParamPolygonLineStyle.getSelectionModel().getSelectedIndex()] );
        prefs.put("polygonStyle", polygonStyleOptions[ParamPolygonStyle.getSelectionModel().getSelectedIndex()] );
    }

    public Color getPolygonLineColor() {
        return( ParamPolygonLineColor.getValue());
    }
    public void setPolygonLineColor(String polygonlineColor) {
        ParamPolygonLineColor.setValue(Color.web(polygonlineColor));
    }
    public Color getPolygonColor() {
        return( ParamPolygonColor.getValue());
    }
    public void setPolygonColor(String polygonColor) {
        ParamPolygonColor.setValue(Color.web(polygonColor));
    }
    public float getPolygonLineSize() {
        return( Float.valueOf(ParamPolygonLineSize.getText()) );
    }
    public void setPolygonLineSize(String polygonlineSize) {
        ParamPolygonLineSize.setText( Float.toString( Float.valueOf(polygonlineSize)*3.0f/2.25f ) );
    }
    public String getPolygonLineStyle() {
        if( ParamPolygonLineStyle.getSelectionModel().getSelectedIndex() == 0 )
            return( "實線" );
        else
            return( "虛線" );
    }
    public void setPolygonLineStyle( String polygonlineStyle ) {
        if( polygonlineStyle.equals("esriSLSDot") )
            ParamPolygonLineStyle.getSelectionModel().select("虛線");
        else
            ParamPolygonLineStyle.getSelectionModel().select("實線");
    }
    public String getPolygonStyle() {
        if( ParamPolygonStyle.getSelectionModel().getSelectedIndex() == 0 )
            return( "網底" );
        else
            return( "填滿" );
    }
    public void setPolygonStyle( String polygonStyle ) {
        if( polygonStyle.equals("esriSFSSolid") )
            ParamPolygonStyle.getSelectionModel().select("填滿");
        else
            ParamPolygonStyle.getSelectionModel().select("網底");
    }
}
