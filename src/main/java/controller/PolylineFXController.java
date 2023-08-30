package controller;

import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class PolylineFXController {
    @FXML
    private ColorPicker ParamPolylineColor ;
    @FXML
    private TextField ParamPolylineSize ;
    @FXML
    private ComboBox ParamPolylineStyle;

    private static String[] polylineStyleOptions = new String[] { "實線", "虛線" };

    public void loadParameter() {
        Preferences prefs = Preferences.userNodeForPackage(PolylineFXController.class);
        String polylineColor = prefs.get("polylineColor", null);
        if (polylineColor != null) {
            ParamPolylineColor.setValue(Color.web(polylineColor));
        }
        String polylineSize = prefs.get("polylineSize", null);
        if (polylineSize != null) {
            ParamPolylineSize.setText(polylineSize);
        }
        ParamPolylineStyle.getItems().addAll(polylineStyleOptions);
        String polylineStyle = prefs.get("polylineStyle", null);
        if ( polylineStyle != null) {
            ParamPolylineStyle.getSelectionModel().select(polylineStyle);
        }
        else
            ParamPolylineStyle.getSelectionModel().select("實線");
    }

    public void saveParameter() {
        Preferences prefs = Preferences.userNodeForPackage(PolylineFXController.class);
        prefs.put("polylineColor", ParamPolylineColor.getValue().toString()  );
        prefs.put("polylineSize", ParamPolylineSize.getText() );
        prefs.put("polylineStyle", polylineStyleOptions[ParamPolylineStyle.getSelectionModel().getSelectedIndex()] );
    }

    public Color getPolylineColor() {
        return( ParamPolylineColor.getValue());
    }
    public void setPolylineColor(String polylineColor) {
        ParamPolylineColor.setValue(Color.web(polylineColor));
    }
    public float getPolylineSize() {
        return( Float.valueOf(ParamPolylineSize.getText()) );
    }
    public void setPolylineSize(String polylineSize) {
        ParamPolylineSize.setText( Float.toString( Float.valueOf(polylineSize)*3.0f/2.25f ) );
    }
    public String getPolylineStyle() {
        if( ParamPolylineStyle.getSelectionModel().getSelectedIndex() == 0 )
            return( "實線" );
        else
            return( "虛線" );
    }
    public void setPolylineStyle( String polylineStyle ) {
        if( polylineStyle.equals("esriSLSSolid") )
            ParamPolylineStyle.getSelectionModel().select("實線");
        else
            ParamPolylineStyle.getSelectionModel().select("虛線");
    }
}
