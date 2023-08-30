package controller;

import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class PointFXController {
    @FXML
    private ColorPicker ParamPointColor ;
    @FXML
    private TextField ParamPointSize ;

    public void loadParameter() {
        Preferences prefs = Preferences.userNodeForPackage(PointFXController.class);
        String pointColor = prefs.get("pointColor", null);
        if (pointColor != null) {
            ParamPointColor.setValue(Color.web(pointColor));
        }
        String pointSize = prefs.get("pointSize", null);
        if (pointSize != null) {
            ParamPointSize.setText(pointSize);
        }
    }

    public void saveParameter() {
        Preferences prefs = Preferences.userNodeForPackage(PointFXController.class);
        prefs.put("pointColor", ParamPointColor.getValue().toString()  );
        prefs.put("pointSize", ParamPointSize.getText() );
    }

    public Color getPointColor() {
        return( ParamPointColor.getValue());
    }
    public float getPointSize() {
        return( Float.valueOf(ParamPointSize.getText()) );
    }
    public void setPointColor(String pointColor) {
        ParamPointColor.setValue(Color.web(pointColor));
    }
    public void setPointSize(String pointSize) {
        ParamPointSize.setText(Float.toString( Float.valueOf(pointSize)*3.0f/2.25f ));
    }
}
