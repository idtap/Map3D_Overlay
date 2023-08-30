package controller;

import java.util.ArrayList;
import java.util.Optional;
import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ListView;
import com.esri.arcgisruntime.mapping.Bookmark;
import com.esri.arcgisruntime.mapping.BookmarkList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;

import feoverlay.MapManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CopyPasteFXController {

    /********************************
      資料
    *********************************/

    @FXML
    private ListView<String> pasteIDList; 

    @FXML
    private Button btnDel;

    @FXML
    private Label copyGraphicID;

    private int nowSelectIndex = -1;

    /********************************
      程序
    *********************************/

    @FXML
    private void initialize() {
       // 點選某項
       pasteIDList.getSelectionModel().selectedItemProperty().addListener((ov, old_val, new_val) -> 
       {
           try {
               nowSelectIndex = pasteIDList.getSelectionModel().getSelectedIndex();
               MapManager.mapController.goCopyPasteItem( nowSelectIndex );
           } catch(Exception e) {
           }
       });
    }          

    public void setCopyGraphicID(Graphic copyGraphic) {
        copyGraphicID.setText(copyGraphic.getAttributes().get("id").toString().split("-")[0]+"...");
    }

    public void addPasteItem(String graphicType, Graphic pasteGraphic) {
        pasteIDList.getItems().add(graphicType+"："+
                             pasteGraphic.getAttributes().get("id").toString().split("-")[0]+"...");
    }

    @FXML
    private void handleDelBtnClicked() {
        if( nowSelectIndex != -1 && nowSelectIndex<pasteIDList.getItems().size()) {                                              
           try {
              int index = nowSelectIndex;
              pasteIDList.getItems().remove(index);
              MapManager.mapController.removeCopyPasteItem(index);
           } catch(Exception e) {
           }
        }
    }

}
