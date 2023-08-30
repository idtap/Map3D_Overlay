package controller;

import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class TextFXController {
    @FXML
    private ColorPicker ParamTextColor ;
    @FXML
    private TextField ParamTextContent ;
    @FXML
    private TextField ParamTextSize ;
    @FXML
    private ComboBox ParamFontFamily;

    private static String[] fontFamilyOptions = new String[] { "標楷體", "正黑體", "細明體" };

    public void loadParameter() {
        Preferences prefs = Preferences.userNodeForPackage(TextFXController.class);
        String textColor = prefs.get("textColor", null);
        if (textColor != null) {
            ParamTextColor.setValue(Color.web(textColor));
        }
        String textSize = prefs.get("textSize", null);
        if (textSize != null) {
            ParamTextSize.setText(textSize);
        }
        String textValue = prefs.get("textValue", null);
        if (textValue != null) {
            ParamTextContent.setText(textValue);
        }
        ParamFontFamily.getItems().addAll(fontFamilyOptions);
        String fontFamily = prefs.get("fontFamily", null);
        if ( fontFamily != null) {
            ParamFontFamily.getSelectionModel().select(fontFamily);
        }
        else
            ParamFontFamily.getSelectionModel().select("正黑體");
    }

    public void saveParameter() {
        Preferences prefs = Preferences.userNodeForPackage(TextFXController.class);
        prefs.put("textColor", ParamTextColor.getValue().toString()  );
        prefs.put("textSize", ParamTextSize.getText() );
        prefs.put("textValue", ParamTextContent.getText() );
        prefs.put("fontFamily", fontFamilyOptions[ParamFontFamily.getSelectionModel().getSelectedIndex()] );
    }

    public Color getTextColor() {
        return( ParamTextColor.getValue());
    }
    public void setTextColor(String textColor) {
        ParamTextColor.setValue(Color.web(textColor));
    }
    public String getTextContent() {
        return( ParamTextContent.getText() );
    }
    public void setTextContent( String textValue ) {
        ParamTextContent.setText(textValue);
    }
    public float getTextSize() {
        return( Float.valueOf(ParamTextSize.getText())*4/3 );
    }
    public void setTextSize( String textSize ) {
        ParamTextSize.setText( textSize );
    }
    public String getTextFontFamily() {
        if( ParamFontFamily.getSelectionModel().getSelectedIndex() == 0 )
            return( "DFKai-sb" );
        else if( ParamFontFamily.getSelectionModel().getSelectedIndex() == 1 )
            return( "Microsoft JhengHei" );
        else
            return( "MingLiU" );
    }
    public void setTextFontFamily( String engFontFamily ) {
        if( engFontFamily.equals("DFKai-sb") ) 
            ParamFontFamily.getSelectionModel().select(fontFamilyOptions[0]);
        else if( engFontFamily.equals("Microsoft JhengHei") )
            ParamFontFamily.getSelectionModel().select(fontFamilyOptions[1]);
        else
            ParamFontFamily.getSelectionModel().select(fontFamilyOptions[2]);
    }
}
