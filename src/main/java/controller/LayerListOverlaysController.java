package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;

import feoverlay.MapManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class LayerListOverlaysController implements Initializable {
	@FXML
	private StackPane mainPane;

	@FXML
	private Label rightWindowText;

	@FXML
	private ImageView minimizeButton;

	@FXML
	private TreeView layerTreeView;

	private Stage _stage_main;
	private AnchorPane parentPane;
	private MapContentController mapContentController;

	private CheckBoxTreeItem<String> rootItem;

	public static HashMap<String, ArrayList<GraphicsOverlay>> layer_Hashmap2D = new HashMap<>();
	public static HashMap<String, ArrayList<GraphicsOverlay>> layer_Hashmap3D = new HashMap<>();
	
	LayerListOverlaysController(Stage _stage_main, AnchorPane parentPane, MapContentController mapContentController) {
		this._stage_main = _stage_main;
		this.parentPane = parentPane;
		this.mapContentController = mapContentController;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		minimizeButton.setOnMouseClicked((e) -> {
			parentPane.setVisible(false);
		});
		
		InitTreeView();
	}

	/**
	 * 加入GraphicsOverlay圖層至圖層清單
	 * @param name
	 * @param layer2D
	 * @param layer3D
	 */
	public void AddGraphicsOverlay(String name, ArrayList<GraphicsOverlay> layer2D, ArrayList<GraphicsOverlay> layer3D) {
		CheckBoxTreeItem<String> checkBoxTreeItem = new CheckBoxTreeItem<String>(name);
		checkBoxTreeItem.setSelected(true);
		
		layer_Hashmap2D.put(name, layer2D);
		layer_Hashmap3D.put(name, layer3D);
		checkBoxTreeItem.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if (null == newVal)
				return;

			ArrayList<GraphicsOverlay> selected = layer_Hashmap2D.get(checkBoxTreeItem.getValue());
			selected.forEach((layer) -> {
				layer.setVisible(newVal);
			});
			
			ArrayList<GraphicsOverlay> selected2 = layer_Hashmap3D.get(checkBoxTreeItem.getValue());
			selected2.forEach((layer) -> {
				layer.setVisible(newVal);
			});
		});
		rootItem.getChildren().add(checkBoxTreeItem);
	}
	
	@SuppressWarnings("unchecked")
	private void InitTreeView() {
		rootItem = new CheckBoxTreeItem<String>("透明圖圖層清單");
		rootItem.setExpanded(true);

		// TreeView選取Item發生變化(ZoomTo圖層)
		layerTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				if (newValue == null)
					return;

				TreeItem<String> selectedItem = (TreeItem<String>) newValue;
				String name = selectedItem.getValue();

				// ZoomTo圖層
//				GraphicsOverlay layer = layer_Hashmap2D.get(name);
//				if (null == layer)
//					return;
//				Envelope e = layer.getFullExtent();
//				Viewpoint vp = new Viewpoint(e);

//				if (MapManager.is3DView)
//					MapManager.sceneView.setViewpoint(vp);
//				else
//					MapManager.mapView.setViewpoint(vp);
			}
		});

		layerTreeView.setCellFactory(new Callback<TreeView<String>, CheckBoxTreeCell<String>>() {
			@Override
			public CheckBoxTreeCell<String> call(TreeView<String> p) {
				CheckBoxTreeCell<String> cell = new CheckBoxTreeCell<String>() {
					@Override
					public void updateItem(String file, boolean empty) {
						super.updateItem(file, empty);
						if (empty) {
							setText(null);
						} else {
							setText(file.toString());
						}
					}
				};

				ContextMenu cm = createContextMenu(cell);
				cell.setContextMenu(cm);
				return cell;
			}
		});

		layerTreeView.setRoot(rootItem);
		layerTreeView.setShowRoot(true);
	}

	/*
	 * TreeItem建立子選單
	 */
	private ContextMenu createContextMenu(CheckBoxTreeCell<String> cell) {
		ContextMenu cm = new ContextMenu();
		MenuItem openItem = new MenuItem("刪除圖層");

		openItem.setOnAction(event -> {
			String fileName = cell.getItem();
			RemoveLayer(fileName);
			rootItem.getChildren().remove(cell.getTreeItem());
		});
		cm.getItems().add(openItem);
		return cm;
	}

	/*
	 * 刪除圖層
	 */
	private void RemoveLayer(String fileName) {
		if (fileName != null) {
			ArrayList<GraphicsOverlay> list1 = layer_Hashmap2D.get(fileName);
			list1.forEach((layer) -> {
				MapManager.mapView.getGraphicsOverlays().remove(layer);
			});
			layer_Hashmap2D.remove(fileName);

			ArrayList<GraphicsOverlay> list2 = layer_Hashmap3D.get(fileName);
			list2.forEach((layer) -> {
				MapManager.sceneView.getGraphicsOverlays().remove(layer);
			});
			layer_Hashmap3D.remove(fileName);

		}
	}

	/**
	 * 依據圖層Key設定顯隱
	 */
	public void setLayerVisibleWithKey(String key, boolean value) {
		CheckBoxTreeItem<String> item =  getTreeViewItem(rootItem, key);
		if (null != item) {
			item.setSelected(value);
		}
	}

	/**
	 * 取得TreeItem
	 */
	public CheckBoxTreeItem getTreeViewItem(CheckBoxTreeItem<String> root, String value) {
		if (root != null && root.getValue().equals(value))
			return root;

		for (TreeItem<String> child : root.getChildren()) {
			CheckBoxTreeItem<String> s = getTreeViewItem((CheckBoxTreeItem<String>) child, value);
			if (s != null)
				return s;

		}
		return null;
	}
}
