package controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.jfoenix.controls.JFXHamburger;
import com.pixelduke.control.Ribbon;
import com.pixelduke.control.ribbon.RibbonGroup;
import com.pixelduke.control.ribbon.RibbonTab;

import feoverlay.AlertDialog;
import feoverlay.Functions;
import feoverlay.MapManager;
import feoverlay.OverlayGroupClass;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RibbonFormController {
	@FXML
	private Ribbon ribbon1;

	@FXML
	private RibbonTab RibbonTab透明圖;

	@FXML
	private RibbonGroup ribbonGroup軍隊符號;

	@FXML
	private RibbonGroup ribbonGroup基本圖形;

	@FXML
	private RibbonGroup ribbonGroup群組功能;

	@FXML
	private RibbonGroup ribbonGroup刪除功能;

	@FXML
	private Button btnEditOverlays;

	@FXML
	private Button btnCloseOverlays;

	@FXML
	private Button btnAddOverlays;

	@FXML
	private Button btnLoadOverlays;

	@FXML
	private Button btnOverlaysList;

	@FXML
	private Button btnNewFE;

	@FXML
	private Button btnEditFE;

	@FXML
	private Button btnCancelFE;

	@FXML
	private Button btnComploeteFE;

	@FXML
	private Button btnGroup;

	@FXML
	private Button btnUnGroup;

	@FXML
	private ToggleButton btnBoxSelection;

	@FXML
	private ToggleButton btnBoxZoomTo;

	@FXML
	private Button btnDeleteGraphic;

	@FXML
	private BorderPane mainPane;

	@FXML
	private StackPane drawerMapContent;

	@FXML
	private JFXHamburger hamburger;

	@FXML
	private ComboBox ComboBoxMultiplie;

	@FXML
	private AnchorPane root;

	// *** 基本圖形

	@FXML
	private Button btnAddPoint;

	@FXML
	private Button btnAddMultiPoint;

	@FXML
	private Button btnAddPolyline;

	@FXML
	private Button btnAddPolygon;

	@FXML
	private Button btnAddFreePolyline;

	@FXML
	private Button btnAddFreePolygon;

	@FXML
	private Button btnAddRectangle;

	@FXML
	private Button btnAddArc;

	@FXML
	private Button btnAddCircle;

	@FXML
	private Button btnAddCurve;

	@FXML
	private Button btnAddSector;

	@FXML
	private Button btnAddText;

	@FXML
	private Button btnAddTraceLine;

	@FXML
	private Button btnAddClipLine;

	@FXML
	private Button btnCompleteBase;

	@FXML
	private Button btnCancelBase;

	@FXML
	private Button btnEditBase;
	
	// 20230413 Win 加
	@FXML
	private Button btnCopyPaste;

	// 基本圖形 ***
	public Stage _stage_main;
	public static AnchorPane rootP;

	/**
	 * 正在編輯之透明圖圖檔
	 */
	private String currentEditOverlayFilename = "";

	@FXML
	public void initialize() {
		rootP = root;

		try {
			// 地圖區域
			FXMLLoader loaderContent = new FXMLLoader(getClass().getResource("/fxml/MapContent.fxml"));
			StackPane pane = loaderContent.load();
			MapManager.mapController = loaderContent.getController();

			MapManager.mapController.initOverlayRibbonGroup(ribbonGroup軍隊符號, ribbonGroup基本圖形, ribbonGroup群組功能,
					ribbonGroup刪除功能);

			MapManager.mapController.setDisableOverlayRibbonGroup(true, true, true, true);

			MapManager.mapController.initFERibbonControl(btnNewFE, btnEditFE, btnComploeteFE, btnCancelFE, btnGroup,
					btnUnGroup, btnBoxSelection, btnDeleteGraphic, null);
			MapManager.mapController.initBaseRibbonControl(btnAddPoint, btnEditBase, btnCompleteBase, btnCancelBase,
					btnAddMultiPoint, btnAddPolyline, btnAddPolygon, btnAddFreePolyline, btnAddFreePolygon,
					btnAddRectangle, btnAddArc, btnAddCircle, btnAddCurve, btnAddSector, btnAddText, btnAddTraceLine,
					btnAddClipLine, null, null, null, btnCopyPaste, null,
					null, null, null, null, null, null); // 20230609
																													// Win
																													// 修
			drawerMapContent.getChildren().add(pane);

			initEventHandler();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 設定透明圖及3D展示頁籤切換時處理功能
	 */
	private void initEventHandler() {
		RibbonTab透明圖.setOnSelectionChanged((e) -> {
			if (MapManager.is3DView)
				MapManager.mapController.handleViewKindSwitch(true);
		});
	}

	@FXML
	public void handleCloseClick() {
		ReleaseResource();
		Platform.exit();
        System.exit(0);
	}

	Map<String, Boolean> OverlayersVisible_Hashmap = new HashMap<>();

	// *** 透明圖檔操作

	/**
	 * 控制透明圖檔項目按鈕
	 */
	private void setOverlaysRibbonButtonDisable(boolean disableAddOverlays, boolean disableEditOverlays,
			boolean disableCloseOverlays, boolean disableLoadOverlays, boolean disableOverlaysList) {
		btnAddOverlays.setDisable(disableAddOverlays);
		btnEditOverlays.setDisable(disableEditOverlays);
		btnCloseOverlays.setDisable(disableCloseOverlays);
		btnLoadOverlays.setDisable(disableLoadOverlays);
		btnOverlaysList.setDisable(disableOverlaysList);
	}

	/**
	 * 用於透明圖圖層編輯時,控制已載入圖層的顯隱
	 * 
	 * @param overlayEditOrNot
	 */
	private void controlOverlaysVisible(boolean overlayEditOrNot) {
		if (overlayEditOrNot) {
			OverlayersVisible_Hashmap.clear();
			LayerListOverlaysController.layer_Hashmap2D.forEach((key, layerList) -> {
				OverlayersVisible_Hashmap.put(key, layerList.get(0).isVisible());
				MapManager.mapController.layerListOverlaysController.setLayerVisibleWithKey(key, false);
				MapManager.mapController.closeTopRightPane();
			});
		} else {
			OverlayersVisible_Hashmap.forEach((key, visible) -> {
				MapManager.mapController.layerListOverlaysController.setLayerVisibleWithKey(key, visible);
			});
			OverlayersVisible_Hashmap.clear();
		}
	}

	/**
	 * 透明圖檔建立
	 */
	@FXML
	private void handleAddOverlays() {

		if (MapManager.is3DView)
			MapManager.mapController.handleViewKindSwitch(true);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		LocalDateTime now = LocalDateTime.now();
		String tmpFileName = dtf.format(now);

		Optional<String> result = AlertDialog.showTextInputDialog(Functions.primaryStage, "建立透明圖檔", null, "透明圖檔名稱：",
				tmpFileName, "建立", "取消");
		if (result.isPresent()) {
			controlOverlaysVisible(true);

			String fileName = result.get().toLowerCase();
			if (!fileName.endsWith(".json"))
				fileName += ".json";

			currentEditOverlayFilename = fileName;
			String filePath = "cfg/Overlays/" + fileName;
			OverlayGroupClass overlayGroupObject = new OverlayGroupClass(filePath);
			overlayGroupObject.CreateNew();

			MapManager.mapController.initOverlayTool(filePath);
			MapManager.mapController.setDisableOverlayRibbonGroup(false, false, false, false);
			setOverlaysRibbonButtonDisable(true, true, false, true, true);
		}
	}

	/**
	 * 透明圖檔編輯
	 */
	@FXML
	private void handleEditOverlays() {

		if (MapManager.is3DView)
			MapManager.mapController.handleViewKindSwitch(true);

		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("cfg/Overlays"));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));

		File selectedFile = fileChooser.showOpenDialog(Functions.primaryStage);
		if (selectedFile == null)
			return;

		controlOverlaysVisible(true);

		currentEditOverlayFilename = selectedFile.getName();
		String filePath = selectedFile.getAbsolutePath();
		MapManager.mapController.initOverlayTool(filePath);
		MapManager.mapController.setDisableOverlayRibbonGroup(false, false, false, false);
		setOverlaysRibbonButtonDisable(true, true, false, true, true);
	}

	/**
	 * 關閉透明圖檔
	 */
	@FXML
	private void handleCloseOverlays() {
		if (btnComploeteFE.isDisable() == false || btnCompleteBase.isDisable() == false) {
			AlertDialog.warningAlert(this._stage_main, "透明圖圖形編輯中,請先結束編輯.", false);
			return;
		}

		controlOverlaysVisible(false);
		MapManager.mapController.closeOverlayTool();

		if (LayerListOverlaysController.layer_Hashmap2D.containsKey(currentEditOverlayFilename)) {
			// 若編輯的圖層載入地圖上,須更新圖層顯示
			ArrayList<GraphicsOverlay> list1 = LayerListOverlaysController.layer_Hashmap2D
					.get(currentEditOverlayFilename);
			ArrayList<GraphicsOverlay> list2 = LayerListOverlaysController.layer_Hashmap3D
					.get(currentEditOverlayFilename);
			list1.forEach((layer) -> {
				layer.getGraphics().clear();
			});
			list2.forEach((layer) -> {
				layer.getGraphics().clear();
			});
			MapManager.mapController.openFileAndDrawGraphics("cfg/Overlays/" + currentEditOverlayFilename, list1,
					list2);
		} else {
			// 詢問是否載入圖層
			Optional<ButtonType> result = AlertDialog.confirmationAlert("載入圖層", "是否載入該透明圖圖層?");
			if (result.get() == ButtonType.OK) {
				String filePath = "cfg/Overlays/" + currentEditOverlayFilename;
				String fileName = currentEditOverlayFilename;

				if (!LayerListOverlaysController.layer_Hashmap2D.containsKey(fileName))
					MapManager.mapController.loadGrapicsOverlay(filePath);
			}
		}

		MapManager.mapController.setDisableOverlayRibbonGroup(true, true, true, true);
		setOverlaysRibbonButtonDisable(false, false, true, false, false);
	}


	/**
	 * 載入透明圖檔
	 */
	@FXML
	private void handleLoadOverlays() {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("cfg/Overlays"));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));

		File selectedFile = fileChooser.showOpenDialog(Functions.primaryStage);
		if (selectedFile == null)
			return;

		String filePath = selectedFile.getAbsolutePath();
		String fileName = selectedFile.getName();

		if (!LayerListOverlaysController.layer_Hashmap2D.containsKey(fileName))
			MapManager.mapController.loadGrapicsOverlay(filePath);
	}

	/**
	 * 透明圖圖層清單
	 */
	@FXML
	private void handleOverlayLayerList() {
		MapManager.mapController.openLayerListOverlays();
	}



	public void setDisableRibbonTab(boolean disableRibbonTab地圖, boolean disableRibbonTab透明圖,
			boolean disableRibbonTab3D展示, boolean disableRibbonTab系統) {
		RibbonTab透明圖.setDisable(disableRibbonTab透明圖);
	}


	public void ReleaseResource() {
		if (null != MapManager.mapController) {
			try {
				MapManager.mapController.ReleaseResource();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
