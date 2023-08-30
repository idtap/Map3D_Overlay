package feoverlay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONException;
import org.json.JSONObject;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.jfoenix.controls.JFXButton;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class milSymbolPickerController implements Initializable {
	private static final String CONST_FAVORITE_FILE = "cfg/FEfavorite.json";
	
	public Stage _stage_main;
	private boolean isNewMode = false;
	private milSymbolTreeView milSymbolTreeView;
	private sidcEditorController sidcEditor;
	private FEAttributesController attributesEditor;

	private Stage dialogStage;
	private boolean _applyClicked = false;
	private String selectedShape = milSymbolCode.CONST_SHAPE_POINT;

	private Hashtable<String, milSymbolCode> favoriteMap = new Hashtable<>();
	/** 編輯中(暫時性)軍隊符號的Attributes */
	public Map<String, Object> editAttributes = null;
	String limitShape = "";

	@FXML
	private Button btnOK;

	@FXML
	private Button btnCancel;

	@FXML
	private Label txtFilepath;

	@FXML
	private AnchorPane milTreeView;

	@FXML
	private AnchorPane sidcPane;

	@FXML
	private TextField filterField;

	@FXML
	private FlowPane flowPaneFavorite;
	
	@FXML
	private AnchorPane ArrtibutesPane;
	
	@FXML
	private Accordion accordion1;
	
	@FXML
	private TitledPane titledPane1;
			
	@FXML
	private TabPane tabPane;
	
	@FXML
	private Tab tabA;
	
	@FXML
	private Tab tabB;
	
	@FXML
	private Tab tabC;
	
	@FXML
	private ImageView imgPreViewSymbol;
	
	@FXML
	private JFXButton btnNewx;
	
	@FXML
	void NextClicked(ActionEvent event) {
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		if (selectionModel.getSelectedIndex() < 2)
			selectionModel.select(selectionModel.getSelectedIndex()+1);
	}
	
	/**
	 * 點選確認按鈕
	 * @param event
	 */
	@FXML
	void ApplyClicked(ActionEvent event) {
		attributesEditor.Confirm();
		if (isNewMode) {
			_applyClicked = true;
			dialogStage.close();
		} else {
			if (editAttributes != null) {
				editAttributes.put("sidc", sidcEditor.getSymbolID());
			}
			AlertDialog.informationAlert(_stage_main, "請於選單[透明圖]->[軍隊符號]區域，點選[確認]按鈕完成修改!!", false);
		}
	}

	/**
	 * 點選取消按鈕
	 * @param event
	 */
	@FXML
	void CancelClicked(ActionEvent event) {
		_applyClicked = false;
		dialogStage.close();
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public Stage getDialogStage() {
		return dialogStage;
	}

	/** 是否為套用更新 */
	public boolean isApplyClicked() {
		return _applyClicked;
	}

	/**
	 * 取得SIDC15碼
	 * @return
	 */
	public String getDrawSymbolID() {
		return sidcEditor.getSymbolID();
	}

	/**
	 * 取得軍符的Shape型態
	 * @return
	 */
	public String getSelectedShape() {
		return selectedShape;
	}
	
	public boolean isCurveLine() {
		return sidcEditor.isCurve();
	} 
	
	public Map<String, Object> getFEAttributes() {
		return attributesEditor.getFEAttributes();
	}

	/**
	 * 設定新增模式
	 */
	public void setNewMode() {
		limitShape = "";
		isNewMode = true;
		attributesEditor.CleanValues();

		btnCancel.setDisable(false);
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(0);
		
		milSymbolTreeView.setLimitShape("");
	}
	
	/**
	 * 設定編輯模式
	 */
	public void setEditMode(Graphic graphic) {
		isNewMode = false;
		attributesEditor.CleanValues();

		btnCancel.setDisable(true);
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(0);
		
		Geometry geometry = graphic.getGeometry();
		//取得一份(暫時性)Attributes，供編輯用
		editAttributes = new HashMap<>(graphic.getAttributes());
		
		if (geometry.getGeometryType() == GeometryType.POINT) 
			limitShape = milSymbolCode.CONST_SHAPE_POINT;
		else if (geometry.getGeometryType() == GeometryType.POLYLINE) 
			limitShape = milSymbolCode.CONST_SHAPE_LINE;
		else if (geometry.getGeometryType() == GeometryType.POLYGON) 
			limitShape = milSymbolCode.CONST_SHAPE_AREA;
		
		String sidc = editAttributes.get("sidc").toString();
		milSymbolTreeView.setBySidc(sidc);
		milSymbolTreeView.setLimitShape(limitShape);
		
		flowPaneFavorite.getChildren().forEach(p -> {
			//去除選取的框
			p.setStyle("");
		});
		
		sidcEditor.SetSIC(sidc, limitShape);
		sidcEditor.setSIDCImage(imgPreViewSymbol, sidc);	
		
		attributesEditor.SetSIC(sidc);
		attributesEditor.setFEAttributes(editAttributes);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		setUserComponents();
		createFilteredTree();
		readFavoriteJson();
	}

	/**
	 * 讀取檔案內容
	 * @param rd
	 * @return
	 * @throws IOException
	 */
	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	/** 讀取外部json檔案得到JSONObject */
	public JSONObject readJsonFromPath(String path) throws IOException, JSONException {
		FileInputStream is = new FileInputStream(path);
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	/**
	 * 讀取json檔案得到常用清單並顯示
	 */
	private void readFavoriteJson() {

		try {
			JSONObject result = readJsonFromPath(CONST_FAVORITE_FILE);
			Iterator<?> keys = result.keys();

			while (keys.hasNext()) {
				String key = (String) keys.next();
				JSONObject jobj = new JSONObject(result.get(key).toString());
				milSymbolCode attr = new milSymbolCode(jobj.getString("name"), jobj.getString("code"), jobj.getString("shape"));
				favoriteMap.put(key, attr);
				addItemToFlowPane(flowPaneFavorite, key, attr);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 初始化(左)樹狀結構,(右)SIDC-15碼屬性設定
	 */
	private void setUserComponents() {
		try {
			tabPane.getSelectionModel().selectedItemProperty().addListener(
				    new ChangeListener<Tab>() {
				        @Override
				        public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
				        	if (t1 == tabA || t1 == tabB)
				        		btnNewx.setDisable(false);
				        	else
				        		btnNewx.setDisable(true);
				        }
				    }
				);
			
			
			// 樹狀結構
			milSymbolTreeView = new milSymbolTreeView();
			milSymbolTreeView.SymbolSelectedEvent.addListener("setSidcEditorSymbolID",
					(SIC, Shape) -> setSidcEditorSymbolID(SIC, Shape));
			milSymbolTreeView.SymbolAddFavoriteEvent.addListener("addFavoriteSymbolID",
					(key, attr) -> addFavoriteSymbolID(key, attr));
			AnchorPane.setTopAnchor(milSymbolTreeView.treeView, 0.0);
			AnchorPane.setBottomAnchor(milSymbolTreeView.treeView, 0.0);
			AnchorPane.setLeftAnchor(milSymbolTreeView.treeView, 0.0);
			AnchorPane.setRightAnchor(milSymbolTreeView.treeView, 0.0);

			milTreeView.getChildren().add(milSymbolTreeView.treeView);
			
			// 右側SIDC-15碼屬性設定
			sidcEditor = new sidcEditorController(); 
			sidcEditor.ModifierChangedEvent.addListener("ModifierChangedEvent", (sidc) -> { ModifierChangedEvent(sidc); });
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/feoverlay/sidcEditor.fxml"));
			loader.setController(sidcEditor);
			AnchorPane root = (AnchorPane) loader.load();
			sidcEditor._stage_main = _stage_main;

			Scene scene = new Scene(root);
		    scene.getStylesheets().add(Functions.appStylesheet);
		    
			AnchorPane.setTopAnchor(root, 0.0);
			AnchorPane.setBottomAnchor(root, 0.0);
			AnchorPane.setLeftAnchor(root, 0.0);
			AnchorPane.setRightAnchor(root, 0.0);
			sidcPane.getChildren().addAll(root);
			
			// 註記屬性設定
			attributesEditor = new FEAttributesController(); 
			FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/fxml/feoverlay/FEAttributes.fxml"));
			loader2.setController(attributesEditor);
			AnchorPane root2 = (AnchorPane) loader2.load();
			attributesEditor._stage_main = _stage_main;
			
			Scene scene2 = new Scene(root2);
			scene2.getStylesheets().add(Functions.appStylesheet);
			AnchorPane.setTopAnchor(root2, 0.0);
			AnchorPane.setBottomAnchor(root2, 0.0);
			AnchorPane.setLeftAnchor(root2, 0.0);
			AnchorPane.setRightAnchor(root2, 0.0);
			ArrtibutesPane.getChildren().addAll(root2);
			
			accordion1.setExpandedPane(titledPane1);
			flowPaneFavorite.setFocusTraversable(false);
			
			//初始
			sidcEditor.setSIDCImage(imgPreViewSymbol, "SPGP-----------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void ModifierChangedEvent(String sidc) {
		if (null != attributesEditor)
			attributesEditor.SetSIC(sidc);
	}

	/**
	 * 依據Key刪除常用清單項目,並存入Json檔
	 * @param key
	 */
	private void removeFavoriteSymbolID(String key) {
		favoriteMap.remove(key);
		
		try (OutputStreamWriter oos = new OutputStreamWriter(new FileOutputStream(CONST_FAVORITE_FILE), Charset.forName("UTF-8"))) {
			JSONObject cacheObject = new JSONObject(favoriteMap);
			cacheObject.write(oos);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/** 加入項目至常用清單,並存入Json檔 */
	private void addFavoriteSymbolID(String key, milSymbolCode attr) {
		if (favoriteMap.containsKey(key)) {
			return;
		}
		
		favoriteMap.put(key, attr);
		addItemToFlowPane(flowPaneFavorite, key, attr);
		 
		try (OutputStreamWriter oos = new OutputStreamWriter(new FileOutputStream(CONST_FAVORITE_FILE), Charset.forName("UTF-8"))) {
			JSONObject cacheObject = new JSONObject(favoriteMap);
			cacheObject.write(oos);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 將我的最愛軍符加入FlowPane內
	 * @param flowPane
	 * @param key
	 * @param attr
	 */
	private void addItemToFlowPane(FlowPane flowPane, String key, milSymbolCode attr) {
		String cssBordering = "-fx-border-color:darkblue ; \n" //#090a0c
	            + "-fx-border-insets:3;\n"
	            + "-fx-border-radius:7;\n"
	            + "-fx-border-width:1.0";
		
		ImageView imageView = new ImageView();
		imageView.setPickOnBounds(true);
		imageView.setPreserveRatio(true);
		imageView.setId(key);
		sidcEditor.setSIDCImage(imageView, attr.getCode());
		
		BorderPane favoriteItem = new BorderPane();
		favoriteItem.setCenter(imageView);
		favoriteItem.setMaxHeight(100+1);
		favoriteItem.setMaxWidth(100+1);
		
		// 點選常用清單項目
		imageView.setOnMouseClicked(event -> {
			if (!limitShape.equals("") && !limitShape.equals(attr.getShape())) 
				AlertDialog.errorAlert("選擇圖形樣式!!", false);
			else {
				sidcEditor.SetSIC(favoriteMap.get(key).getCode(), favoriteMap.get(key).getShape());
				attributesEditor.SetSIC(favoriteMap.get(key).getCode());
				selectedShape = favoriteMap.get(key).getShape();
				flowPane.getChildren().forEach(p -> {
					//去除選取的框
					p.setStyle("");
				});
				
				//在圖示上加框,表示選取
				favoriteItem.setStyle(cssBordering);
			}
		});
		
		favoriteItem.setOnContextMenuRequested(e -> {
			ContextMenu contextMenu = new ContextMenu();
			MenuItem deleteItem = new MenuItem();
			deleteItem.textProperty().set("刪除"); //.bind(Bindings.format("刪除 \"%s\"", cell.itemProperty()));
			deleteItem.setOnAction(event -> { 
				removeFavoriteSymbolID(key); //cell.getItem());	
				flowPane.getChildren().remove(favoriteItem);
			});
			contextMenu.getItems().addAll(deleteItem);
			contextMenu.show(imageView, e.getScreenX(), e.getScreenY());
		});
		

		flowPane.getChildren().add(favoriteItem);
	}
	
	/** 設定目前選擇項目,顯示右邊15碼資料 */
	public void setSidcEditorSymbolID(String SIC, String Shape) {
		sidcEditor.SetSIC(SIC, Shape);
		attributesEditor.SetSIC(SIC);
		sidcEditor.setSIDCImage(imgPreViewSymbol, SIC);	
		this.selectedShape = Shape;		
	}

	/**
	 * 建立關鍵字結果TreeView
	 */
	private void createFilteredTree() {
		FilterableTreeItem<String> root = milSymbolTreeView.rootXML;
		root.predicateProperty().bind(Bindings.createObjectBinding(() -> {
			if (filterField.getText() == null || filterField.getText().isEmpty())
				return null;
			
			expandTreeView(root);
			return TreeItemPredicate.create(actor -> actor.toString().contains(filterField.getText()));
		}, filterField.textProperty()));
	}

	/**
	 *	擴展TreeItem 
	 */
	private void expandTreeView(TreeItem<?> item){
	    if(item != null && !item.isLeaf()){
	        item.setExpanded(true);
	        for(TreeItem<?> child:item.getChildren()){
	            expandTreeView(child);
	        }
	    }
	}
}
