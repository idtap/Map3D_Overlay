package controller;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.swing.JFrame;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.MultipointBuilder;
import com.esri.arcgisruntime.geometry.Part;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.RasterElevationSource;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.GeoView;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties.SurfacePlacement;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.symbology.ColorUtil;
import com.esri.arcgisruntime.symbology.MarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.esri.arcgisruntime.toolkit.Compass;
import com.esri.arcgisruntime.toolkit.OverviewMap;
import com.pixelduke.control.ribbon.RibbonGroup;

import feoverlay.AlertDialog;
import feoverlay.Event;
import feoverlay.Functions;
import feoverlay.GroupGraphicsClass;
import feoverlay.MapManager;
import feoverlay.OverlayFileClass;
import feoverlay.OverlayGroupClass;
import feoverlay.SketchEditorFE;
import feoverlay.milSymbolPickerController;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MapContentController {

    @FXML
    private StackPane mapStackPane;

    @FXML
    private SceneView sceneView;

    @FXML
    private MapView mapView;

    @FXML
    private AnchorPane topRightPane;

    @FXML
    private ImageView imageViewHome;

    @FXML
    private ImageView imageViewZoomIn;

    @FXML
    private ImageView imageViewZoomOut;

    @FXML
    private ImageView imageView2DView;

    @FXML
    private VBox vboxLeft;

    private ArcGISScene arcgisScene;
    private ArcGISMap arcgisMap;
    // 20230213 Win 加
    private SketchEditor baseSketchEditor;
   
    /**
     * 圖層清單物件擺放的Pane
     */
    private StackPane oLayerList = null;

    /**
     * 透明圖圖層清單物件擺放的Pane
     */
    public StackPane oLayerListOverlays = null;

    private int initBasemapIdx = 0;

    // *** 軍隊符號
    private milSymbolPickerController milSymbolPicker;
    private Stage stagePicker;
    private Stage stageFEVisiblity;

    public SketchEditorFE sketchEditorFE;

    private BorderPane ScreenShotScene;
    private BorderPane RealtimePlayScene;
    private BorderPane RealtimeSettingScene;
    public LayerListOverlaysController layerListOverlaysController;

    private GeoView geoView = null;
    private DecimalFormat df = new DecimalFormat("#,###"); 
    
    /**
     * 2525B軍隊符號透明圖編輯圖層
     */
    private GraphicsOverlay overlayDrapedMilitarySymbol;

    private RibbonGroup ribbonGroup軍隊符號;
    private RibbonGroup ribbonGroup基本圖形;
    private RibbonGroup ribbonGroup群組功能;
    private RibbonGroup ribbonGroup刪除功能;

    private Button btnNewFE;
    private Button btnEditFE;
    private Button btnCancelFE;
    private Button btnComploeteFE;
    private Button btnDeleteGraphic;

    // 軍隊符號 ***

    // *** 群組功能

    private Button btnGroup;
    private Button btnUnGroup;
    private ToggleButton btnBoxSelection;
    private ToggleButton btnBoxZoomTo;
    public GroupGraphicsClass groupManager;

    // 群組功能 ***

    // *** 基本圖形

    /**
     * 基本圖形符號"顯示"圖層
     */
    private GraphicsOverlay overlayDrapedBaseMilitarySymbol;

    /**
     * 基本圖形符號"顯示"圖層
     */
    private GraphicsOverlay overlayDrapedBaseMilitarySymbol_3D;

    private Button btnAddPoint;
    private Button btnEditBase;
    private Button btnCompleteBase;
    private Button btnCancelBase;
    private Button btnAddMultiPoint;
    private Button btnAddPolyline;
    private Button btnAddPolygon;
    private Button btnAddFreePolyline;
    private Button btnAddFreePolygon;
    private Button btnAddRectangle;
    private Button btnAddArc;
    private Button btnAddCircle;
    private Button btnAddCurve;
    private Button btnAddSector;
    private Button btnAddText;
    private Button btnAddTraceLine;
    private Button btnAddClipLine;
    private Button btnMeasureBase;
    private Button btnBookMark;
    private Button btnSwipe;
    private Button btnCopyPaste;
    // 20230418 Win
    private Button btnAnalysisAlarm;
    // 20230428 Win
    private Button btnAnalysisSight;
    // 20230503 Win 加
    private Button btnPrint;
    // 20230505 Win 加
    private Button btnAnalysisEye;
    // 20230517 Win 加
    private Button btnAlarmEnable;
    public boolean isEnableAlarm = false;
    // 20230530 Win 加
    private Button btnMapDownload;
    // 20230609 Win 加
    private Button btnAnalysisRoute;

    // 基本圖形 ***

  
    @FXML
    public void initialize() {
        initMap();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    timer.cancel();
                } catch (Exception e) { 
                    e.printStackTrace();
                }
            }
        }, 5000);
        
      
        topRightPane.setVisible(false); // 關閉右邊功能視窗
    }

    /**
     * 初始化主3D之圖台
     */
    private void init3DScene() {
        // 3D
        sceneView.setVisible(MapManager.is3DView);
        MapManager.sceneView = sceneView;

        arcgisScene = new ArcGISScene();
        sceneView.setArcGISScene(arcgisScene);

        String rasterURL = new File(Functions.apPath + "/samples-data/TaiwanDem/TaiwanDem_new_P.tif").getAbsolutePath();
        ArrayList<String> list = new ArrayList<String>();
        list.add(rasterURL);

        Surface surface = new Surface();
        surface.getElevationSources().add(new RasterElevationSource(list));
        surface.setElevationExaggeration(0.9f);
        arcgisScene.setBaseSurface(surface);

        // initialize the viewpoint
        MapManager.setDefaultCamera(sceneView);
        sceneView.setAttributionTextVisible(false);
    }

    private void init2DMap() {
        mapView.setVisible(!MapManager.is3DView);
        MapManager.mapView = mapView;
        mapView.setAttributionTextVisible(false);

       
        try {
            Point coloradoNorthWestPoint = new Point(-19999999, -10600000);
            Point coloradoSouthEastPoint = new Point(19999999, 10600000);
            Envelope envelope = new Envelope(coloradoNorthWestPoint, coloradoSouthEastPoint);

            arcgisMap = new ArcGISMap();
            arcgisMap.setMaxScale(1000);
            arcgisMap.setMaxExtent(envelope);
            mapView.setMap(arcgisMap);

            // 20230213 Win 加 -> 一併建立基本圖修改的 sketchEditor
            baseSketchEditor = new SketchEditor();
            mapView.setSketchEditor(baseSketchEditor);
            
            // initialize the viewpoint
            MapManager.setDefaultCamera(mapView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化View的滑鼠,鍵盤事件
     */
    private void initViewEventHadler() {
        mapView.setOnMouseMoved(e -> {
            // 當軍隊符號被選擇, 或是新增曲線軍隊符號時
            if (null != sketchEditorFE && 
                    (null != sketchEditorFE.getSelectedGraphic() || (null != milSymbolPicker && milSymbolPicker.isCurveLine())))
                sketchEditorFE.handleMapViewMouseMoved(e);

        });

        mapView.setOnKeyPressed(e -> {
            if (null != groupManager)
                groupManager.handleViewKeyPressed(e);
        });

        mapView.setOnKeyReleased(e -> {
            if (null != groupManager)
                groupManager.handleViewKeyReleased(e);
        });


        mapView.setOnMouseClicked(e -> {
            if (null != groupManager)
                groupManager.handleViewMouseClicked(e);            
        });
    }

    /**
     * 處理地圖上MouseClick事件
     */
    private void handleAfterGroupMouseClick(MouseEvent e) {
        btnEditFE.setDisable(true);

        Graphic overlayGraphic = groupManager.getSelectedOneGraphic();
        if (null != overlayGraphic) {
            btnDeleteGraphic.setDisable(false);
            if (overlayGraphic.getSymbol() == null) {
                // 單選軍隊符號圖形
                if (groupManager.specialFlag)
                    groupManager.specialFlag = false;
                else
                    sketchEditorFE.handleMapViewMouseClicked(e);
                btnEditFE.setDisable(false);
                btnCopyPaste.setDisable(false);
                btnEditBase.setDisable(true);
            } else {
                // 單選基本圖形
                btnCopyPaste.setDisable(false);
                btnEditBase.setDisable(false);
            }
            controlAddButtonsDisabled(true);
        } else {
            // 沒選到透明圖圖形
            btnDeleteGraphic.setDisable(true);
            btnCopyPaste.setDisable(true);
            btnEditBase.setDisable(true);
            if (!groupManager.isCanGroup())
                controlAddButtonsDisabled(false);
            sketchEditorFE.handleMapViewMouseClicked(e);
        }
    }

    /**
     * 初始化地圖功能
     */
    private void initMap() {
        String stand = "runtimestandard,1000,rud000433798,none,NKLFD4SZ8L2K8YAJM070";
        String analysis = "runtimeanalysis,1000,rud000012252,none,YYPH5AZAM8J8AZJTR058";
        List<String> ls = new ArrayList<String>();
        ls.add(analysis);
        ArcGISRuntimeEnvironment.setLicense(stand, ls);

        init2DMap();

        init3DScene();

        try {
            MapManager.changeBaseMap();
        } catch (Exception ex) {
            AlertDialog.errorAlert(ex.getMessage(), false);
        }

        initViewEventHadler();

        init2525BLayer();

        initBaseLayer();

        initOthersComponent();

        initLayerlistOverlays();

        initAddShapefiles();
    }

    private void initAddShapefiles() {
    	try {
    		File f1 = new File(Functions.apPath + "/samples-data/shape/line.shp");
			MapManager.addShapefile(f1.getCanonicalPath());
			
    		File f2 = new File(Functions.apPath + "/samples-data/shape/polygon.shp");
			MapManager.addShapefile(f2.getCanonicalPath());		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
     * 透明圖工具初始化
     */
    public void initOverlayTool(String fileName) {
        btnDeleteGraphic.setDisable(true);
        btnCopyPaste.setDisable(true);
        btnEditBase.setDisable(true);
        controlAddButtonsDisabled(false);
        MapManager.overlayGroupObject = new OverlayGroupClass(fileName);
        MapManager.overlayGroupObject.Open();
        MapManager.overlayFileObject = new OverlayFileClass(MapManager.overlayGroupObject.getOverlaysString());

        // 軍隊符號透明圖編輯類別物件
        sketchEditorFE = new SketchEditorFE(MapManager.mapView, overlayDrapedMilitarySymbol, baseSketchEditor);
        sketchEditorFE.CanCompleteEvent.addListener("handleCanCompleteEvent", () -> handleCanCompleteEvent());
        sketchEditorFE.CanEditDeleteEvent.addListener("handleCanEditDelete",
                (enabled) -> handleCanEditDeleteFE(enabled));

        // 透明圖群組功能類別物件
        groupManager = new GroupGraphicsClass(MapManager.mapView,
                new ArrayList<GraphicsOverlay>(MapManager.OverlayLayers.values()));
        groupManager.CanGroupOrNotEvent.addListener("handleCanGroupOrNotEvent", (enabled, graphic) -> {
            handleCanGroupOrNotEvent(enabled, graphic);
        });
        groupManager.CanUngroupOrNotEvent.addListener("handleCanUngroupOrNotEvent", (enabled) -> {
            handleCanUngroupOrNotEvent(enabled);
        });
        groupManager.AfterGroupMouseClickHandle.addListener("handleGroupMouseClick", (e) -> {
            handleAfterGroupMouseClick(e);
        });
    }

    public void closeOverlayTool() {
        MapManager.overlayFileObject = null;
        MapManager.overlayGroupObject = null;

        sketchEditorFE.CanCompleteEvent.removeListener("handleCanCompleteEvent");
        sketchEditorFE.CanEditDeleteEvent.removeListener("handleCanEditDelete");
        sketchEditorFE = null;

        groupManager.CanGroupOrNotEvent.removeListener("handleCanGroupOrNotEvent");
        groupManager.CanUngroupOrNotEvent.removeListener("handleCanUngroupOrNotEvent");
        groupManager.AfterGroupMouseClickHandle.removeListener("handleGroupMouseClick");
        groupManager = null;

        MapManager.OverlayLayers.forEach((key, layer) -> {
            layer.getGraphics().clear();
        });
    }

    /**
     * 處理可否透明圖解除群組通知
     * 
     * @param enabled
     */
    private void handleCanUngroupOrNotEvent(boolean enabled) {
        btnUnGroup.setDisable(!enabled);
        if (enabled) {
            btnDeleteGraphic.setDisable(false);
            btnCopyPaste.setDisable(true);
            btnEditBase.setDisable(true);
            btnGroup.setDisable(true);
        }
        btnEditFE.setDisable(enabled);
    }

    private void controlAddButtonsDisabled(boolean value) {
        btnNewFE.setDisable(value);
        btnAddPoint.setDisable(value);
        btnAddMultiPoint.setDisable(value);
        btnAddPolyline.setDisable(value);
        btnAddPolygon.setDisable(value);
        btnAddFreePolyline.setDisable(value);
        btnAddFreePolygon.setDisable(value);
        btnAddRectangle.setDisable(value);
        btnAddArc.setDisable(value);
        btnAddCircle.setDisable(value);
        btnAddCurve.setDisable(value);
        btnAddSector.setDisable(value);
        btnAddText.setDisable(value);
        btnAddTraceLine.setDisable(value);
        btnAddClipLine.setDisable(value);
    }

    /**
     * 處理可否透明圖組成群組通知
     * 
     * @param enabled
     */
    private void handleCanGroupOrNotEvent(boolean enabled, Graphic graphic) {
        btnGroup.setDisable(!enabled);
        if (enabled) {
            btnUnGroup.setDisable(true);
            btnEditFE.setDisable(true);
            btnCopyPaste.setDisable(true);
            btnEditBase.setDisable(true);
        } else {
            // 當使用者只選一個透明圖,則判斷是哪個圖層的Graphic
            if (graphic != null) {
                if (graphic.getAttributes().get("layername").equals(MapManager.KEY_OverlayDrapedMilitarySymbol)) {
                    sketchEditorFE.setSelectedGraphic(graphic);
                    btnEditFE.setDisable(false);
                    btnEditBase.setDisable(true);
                } else if (graphic.getAttributes().get("layername").equals(MapManager.KEY_OverlayDrapedBaseSymbol)) {
                    btnEditBase.setDisable(false);
                    btnEditFE.setDisable(true);
                }
                controlAddButtonsDisabled(true);
            }
        }
    }

    /**
     * 初始化軍隊符號透明圖圖層
     */
    private void init2525BLayer() {
        overlayDrapedMilitarySymbol = MapManager.getNewMilitaryOverlay();
        MapManager.mapView.getGraphicsOverlays().add(overlayDrapedMilitarySymbol);
        MapManager.OverlayLayers.put(MapManager.KEY_OverlayDrapedMilitarySymbol, overlayDrapedMilitarySymbol);
    }

    /**
     * 初始化基本圖形透明圖圖層
     */
    private void initBaseLayer() {
        overlayDrapedBaseMilitarySymbol = new GraphicsOverlay();
        overlayDrapedBaseMilitarySymbol.getSceneProperties().setSurfacePlacement(SurfacePlacement.DRAPED_BILLBOARDED);
        MapManager.mapView.getGraphicsOverlays().add(overlayDrapedBaseMilitarySymbol);
        MapManager.OverlayLayers.put(MapManager.KEY_OverlayDrapedBaseSymbol, overlayDrapedBaseMilitarySymbol);

        overlayDrapedBaseMilitarySymbol_3D = new GraphicsOverlay();
        overlayDrapedBaseMilitarySymbol_3D.getSceneProperties()
                .setSurfacePlacement(SurfacePlacement.DRAPED_BILLBOARDED);
        MapManager.sceneView.getGraphicsOverlays().add(overlayDrapedBaseMilitarySymbol_3D);
    }

    /**
     * 初始化圖台操作按鈕功能
     */
    private void initOthersComponent() {
        // 畫面圖示按鈕加提示說明
        Tooltip.install(imageViewHome, new Tooltip("原始位置"));
        Tooltip.install(imageViewZoomIn, new Tooltip("地圖拉近"));
        Tooltip.install(imageViewZoomOut, new Tooltip("地圖拉遠"));
        Tooltip.install(imageView2DView, new Tooltip("2D/3D切換"));

        // 設定地圖上按鈕事件
        imageViewHome.setOnMouseClicked(e -> MapManager.setDefaultCamera(null));
        imageViewZoomIn.setOnMouseClicked(e -> MapManager.ZoomIn());
        imageViewZoomOut.setOnMouseClicked(e -> MapManager.ZoomOut());
        imageView2DView.setOnMouseClicked(e -> {
            handleViewKindSwitch();
        });

        changeViewKindIcon();
    }

    /**
     * 切換2D/3D View
     */
    public ListenableFuture<Boolean> handleViewKindSwitch(boolean showAlert) {
        return handleViewKindSwitch();
    }

    /**
     * 切換2D/3D View
     */
    public ListenableFuture<Boolean> handleViewKindSwitch() {
        ListenableFuture<Boolean> ret = MapManager.switchViewKind();

        changeViewKindIcon();

        return ret;
    }

    /**
     * 2D/3D 按鈕圖示切換
     */
    private void changeViewKindIcon() {
        String url = "";
        if (MapManager.is3DView)
            url = getClass().getResource("/icons/view-3d-32.png").toString();
        else
            url = getClass().getResource("/icons/view-2d-32.png").toString();
        Image icon = new Image(url, 44, 44, false, true);
        imageView2DView.setImage(icon);
    }


    public void openLayerListOverlays() {
        topRightPane.getChildren().clear();
        topRightPane.setVisible(true); // 顯示右邊功能視窗
        topRightPane.getChildren().addAll(oLayerListOverlays);
    }

    public void closeTopRightPane() {
        topRightPane.getChildren().clear();
        topRightPane.setVisible(true); // 顯示右邊功能視窗
    }

    private void initLayerlistOverlays() {
        // 圖層清單物件
        layerListOverlaysController = new LayerListOverlaysController(Functions.primaryStage, topRightPane, this);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LayerListOverlays.fxml"));
        loader.setController(layerListOverlaysController);
        try {
            oLayerListOverlays = (StackPane) loader.load();
            AnchorPane.setTopAnchor(oLayerListOverlays, 0.0);
            AnchorPane.setLeftAnchor(oLayerListOverlays, 0.0);
            AnchorPane.setRightAnchor(oLayerListOverlays, 0.0);
            AnchorPane.setBottomAnchor(oLayerListOverlays, 0.0);
        } catch (IOException e) {
             
            e.printStackTrace();
        }
    }

    /**
     * 釋放資源
     */
    public void ReleaseResource() {
      
    }

    public void initOverlayRibbonGroup(RibbonGroup ribbonGroup軍隊符號, RibbonGroup ribbonGroup基本圖形,
            RibbonGroup ribbonGroup群組功能, RibbonGroup ribbonGroup刪除功能) {
        this.ribbonGroup軍隊符號 = ribbonGroup軍隊符號;
        this.ribbonGroup基本圖形 = ribbonGroup基本圖形;
        this.ribbonGroup群組功能 = ribbonGroup群組功能;
        this.ribbonGroup刪除功能 = ribbonGroup刪除功能;
    }

    public void setDisableOverlayRibbonGroup(boolean disableGroup軍隊符號, boolean disableGroup基本圖形,
            boolean disableGroup群組功能, boolean disableGroup刪除功能) {
        ribbonGroup軍隊符號.setDisable(disableGroup軍隊符號);
        ribbonGroup基本圖形.setDisable(disableGroup基本圖形);
        ribbonGroup群組功能.setDisable(disableGroup群組功能);
        ribbonGroup刪除功能.setDisable(disableGroup刪除功能);
    }

    /**
     * 初始設定軍隊符號編輯功能，包含按鈕事件註冊
     * @param btnNew
     * @param btnEdit
     * @param btnComploete
     * @param btnCancel
     * @param btnGroup
     * @param btnUnGroup
     * @param btnBoxSelection
     * @param btnDeleteGraphic
     * @param btnBoxZoomTo
     */
    public void initFERibbonControl(Button btnNew, Button btnEdit, Button btnComploete, Button btnCancel,
            Button btnGroup, Button btnUnGroup, ToggleButton btnBoxSelection, Button btnDeleteGraphic,
            ToggleButton btnBoxZoomTo) {
        this.btnNewFE = btnNew;
        this.btnEditFE = btnEdit;
        this.btnCancelFE = btnCancel;
        this.btnComploeteFE = btnComploete;
        this.btnGroup = btnGroup;
        this.btnUnGroup = btnUnGroup;
        this.btnBoxSelection = btnBoxSelection;
        this.btnDeleteGraphic = btnDeleteGraphic;
        this.btnBoxZoomTo = btnBoxZoomTo;

        this.btnNewFE.setOnAction(e -> handleNewFE(e));
        this.btnEditFE.setOnAction(e -> handleEditFE(e));
        this.btnComploeteFE.setOnAction(e -> handleCompleteFEDraw(e));
        this.btnCancelFE.setOnAction(e -> handleCancelFEDraw(e));
        this.btnGroup.setOnAction(e -> handleGroupClicked(e));
        this.btnUnGroup.setOnAction(e -> handleUngroupClicked(e));
        this.btnBoxSelection.setOnAction(e -> handleBoxSelection(e));
        this.btnDeleteGraphic.setOnAction(e -> handleDeleteGraphic(e));

        btnComploete.setDisable(true);
        btnCancel.setDisable(true);
        btnEdit.setDisable(true);
        btnGroup.setDisable(true);
        btnUnGroup.setDisable(true);
    }

    /**
     * 點選刪除(透明圖)按鈕
     * 
     * @param e
     */
    private void handleDeleteGraphic(ActionEvent e) {
        if (MapManager.isAddEditOverlay)
            return;

        Graphic graphicSelected = groupManager.getSelectedOneGraphic();
        List<Graphic> groupGraphicsSelected = groupManager.getSelectedGroupGraphics();

        Optional<ButtonType> result = null;
        if (groupGraphicsSelected.size() > 1 || null != graphicSelected)
            result = AlertDialog.confirmationAlert("確認刪除", "確認刪除所選透明圖?");
//        else if (null != graphicSelected)
//            result = AlertDialog.confirmationAlert("確認刪除", "確認刪除該透明圖?");
        else
            return;

        if (result.get() == ButtonType.OK) {
            if (groupGraphicsSelected.size() > 1) {
                groupGraphicsSelected.forEach((g) -> {
                    DeleteOverlayGraphic(g);
                });
                groupManager.setUnGroup();
            } else if (null != graphicSelected)
                DeleteOverlayGraphic(graphicSelected);

            groupManager.clearAllLayserSelections();
            sketchEditorFE.CancelDraw();
//          if (MapManager.isAddEditOverlay) {
//              MapManager.isAddEditOverlay = false;
//              //
//              sketchEditorFE.CancelDraw();            
//              
//              //Cancel透明圖
//              cleanOperate();
//              this.btnCompleteBase.setDisable(true);
//              this.btnCancelBase.setDisable(true);
//          }
        }
    }

    /**
     * 刪除透明圖方法
     * 
     * @param graphicSelected
     */
    private void DeleteOverlayGraphic(Graphic graphicSelected) {
        if (null != graphicSelected && graphicSelected.getAttributes().containsKey("layername")) {
            String layerName = graphicSelected.getAttributes().get("layername").toString();
            if (MapManager.OverlayLayers.containsKey(layerName))
                MapManager.OverlayLayers.get(layerName).getGraphics().remove(graphicSelected);

            // 儲存透明圖至外部檔案
            MapManager.overlayFileObject.SaveOverlayLayerToJson();
        }
    }

    public void loadGrapicsOverlay(String filePath) {
        String fileName = new File(filePath).getName();

        // 新增"2D透明圖圖層"
        ArrayList<GraphicsOverlay> list1 = new ArrayList<GraphicsOverlay>();
        // 基本圖形圖層
        GraphicsOverlay baselayer1 = new GraphicsOverlay();
        MapManager.mapView.getGraphicsOverlays().add(baselayer1);
        list1.add(baselayer1);
        // 2525B圖層
        GraphicsOverlay overlayDrapedMilitarySymbol = MapManager.getNewMilitaryOverlay();
        MapManager.mapView.getGraphicsOverlays().add(overlayDrapedMilitarySymbol);
        list1.add(overlayDrapedMilitarySymbol);

        // 新增"3D透明圖圖層"
        ArrayList<GraphicsOverlay> list2 = new ArrayList<GraphicsOverlay>();
        // 基本圖形圖層
        GraphicsOverlay baselayer2 = new GraphicsOverlay();
        MapManager.sceneView.getGraphicsOverlays().add(baselayer2);
        list2.add(baselayer2);
        // 2525B圖層
        GraphicsOverlay overlayDrapedMilitarySymbol_3D = MapManager.getNewMilitaryOverlay();
        MapManager.sceneView.getGraphicsOverlays().add(overlayDrapedMilitarySymbol_3D);
        list2.add(overlayDrapedMilitarySymbol_3D);

        layerListOverlaysController.AddGraphicsOverlay(fileName, list1, list2);

        openFileAndDrawGraphics(filePath, list1, list2);
    }

    /**
     * 讀取Json檔案,將Graphics內容加入新的圖層
     */
    public void openFileAndDrawGraphics(String filePath, ArrayList<GraphicsOverlay> overlay2D,
            ArrayList<GraphicsOverlay> overlay3D) {
        OverlayGroupClass overlayGroupObject = new OverlayGroupClass(filePath);
        overlayGroupObject.Open();

        // **處理圖形部分
        HashMap<String, String> hashMap = (HashMap<String, String>) JSONValue
                .parse(overlayGroupObject.getOverlaysString());
        hashMap.forEach((key, value) -> {
            Graphic g2 = OverlayFileClass.jsonToGraphic(value); // for 2D
            Graphic g3 = OverlayFileClass.jsonToGraphic(value); // for 3D

            // 依據圖形的種類放進不同圖層
            if (null != g2.getGeometry()) {
                if (null == g2.getSymbol()) {
                    // 2525B Graphic 沒有Symbol
                    overlay2D.get(1).getGraphics().add(g2);
                    overlay3D.get(1).getGraphics().add(g3);
                } else {
//                  if (g2.getSymbol() != null) {
//                      Class symbolType = g2.getSymbol().getClass();
//                      if (symbolType.equals(SimpleMarkerSceneSymbol.class))
//                          continue;
//                      else if (symbolType.equals(ModelSceneSymbol.class))
//                          continue;
//                      else {
                    // "點","線","面", "文字"圖形
                    overlay2D.get(0).getGraphics().add(g2);
                    overlay3D.get(0).getGraphics().add(g3);
//                      }
//                  }
                }
            }
        });
    }

    /********************
     * 以下開始是軍隊符號繪製相關內容
     **********************************************************/

    /**
     * 處理透明圖組成群組點選事件
     * 
     * @param e
     */
    private void handleGroupClicked(ActionEvent e) {
        if (groupManager != null)
            groupManager.setSelectedToGroup();
    }

    /**
     * 處理透明圖解除群組點選事件
     * 
     * @param e
     */
    private void handleUngroupClicked(ActionEvent e) {
        groupManager.setUnGroup();
    }

    /**
     * 框選透明圖
     * 
     * @param e
     */
    private void handleBoxSelection(ActionEvent e) {
        setDisableOverlayRibbonGroup(true, true, true, true);

        MapManager.setOverlayEditStatus();
        cleanOperate();

        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowMoveGraphic = new Graphic();
        nowMoveGraphic.setSymbol(DefaultLineMoveSymbol);
        sketchlayer.getGraphics().add(nowMoveGraphic);

        firstPosPoint3D = null;
        secondPosPoint3D = null;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    if (firstPosPoint3D == null) {
                        firstPosPoint3D = relativeSurfacePoint;
                    } else {
                        secondPosPoint3D = relativeSurfacePoint;
                        // 由第一點和第二點框出區域
                        PolygonBuilder rectBuilder = new PolygonBuilder(mapView.getSpatialReference());
                        rectBuilder.addPoint(firstPosPoint3D);
                        Point next_p = new Point(firstPosPoint3D.getX(), secondPosPoint3D.getY());
                        rectBuilder.addPoint(next_p);
                        rectBuilder.addPoint(secondPosPoint3D);
                        next_p = new Point(secondPosPoint3D.getX(), firstPosPoint3D.getY());
                        rectBuilder.addPoint(next_p);
                        Geometry polygon = rectBuilder.toGeometry();
                        polygon = GeometryEngine.project(polygon, SpatialReferences.getWgs84());

                        int count = groupManager.setBoxOverlaysSelected(polygon);

                        // 結束框選
                        MapManager.isAddEditOverlay = false;
                        clearMoveGraphic();
                        cleanOperate();

                        setDisableOverlayRibbonGroup(false, false, false, false);
                        btnBoxSelection.setSelected(false);
                        if (count > 0)
                            btnDeleteGraphic.setDisable(false);
//                      else if (count == 1) {
//                          btnEditFE.setDisable(false);
//                      }
                    }
                }
            }
        });
        prevMoveFun = mapView.getOnMouseMoved();
        mapView.setOnMouseMoved(event -> {
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
            if (relativeSurfacePoint != null) {
                if (firstPosPoint3D != null) {
                    secondPosPoint3D = relativeSurfacePoint;
                    // 由第一點和第二點框出區域
                    PolygonBuilder rectBuilder = new PolygonBuilder(mapView.getSpatialReference());
                    rectBuilder.addPoint(firstPosPoint3D);
                    Point next_p = new Point(firstPosPoint3D.getX(), secondPosPoint3D.getY());
                    rectBuilder.addPoint(next_p);
                    rectBuilder.addPoint(secondPosPoint3D);
                    next_p = new Point(secondPosPoint3D.getX(), firstPosPoint3D.getY());
                    rectBuilder.addPoint(next_p);
                    nowMoveGraphic.setGeometry(rectBuilder.toGeometry());
                }
            }
        });
    }

    /** 開始畫2525B符號,開啟軍符選擇視窗 */
    void handleNewFE(ActionEvent event) {
        groupManager.clearAllLayserSelections();
        setDisableOverlayRibbonGroup(false, true, true, true);

        // 設定unselected基本圖形圖層已選取之Graphic
        GraphicsOverlay layer = MapManager.OverlayLayers.get(MapManager.KEY_OverlayDrapedBaseSymbol);
        layer.getGraphics().forEach((g) -> {
            g.setSelected(false);
        });
        groupManager.clearGraphicSelected();

        MapManager.setOverlayEditStatus();
        btnNewFE.setDisable(true);
        btnEditFE.setDisable(true);

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        try {
            if (stagePicker == null) {
                milSymbolPicker = new milSymbolPickerController();
                milSymbolPicker._stage_main = Functions.primaryStage;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/feoverlay/milSymbolPicker.fxml"));
                loader.setController(milSymbolPicker);
                Parent root = (Parent) loader.load();
                Scene scene = new Scene(root, 500, 800);
                scene.getStylesheets().add(Functions.appStylesheet);
                stagePicker = new Stage();
                milSymbolPicker.setDialogStage(stagePicker);
                stagePicker.setX(primScreenBounds.getWidth() - scene.getWidth() - 10);
                stagePicker.setY((primScreenBounds.getHeight() - scene.getHeight()) / 2);
                stagePicker.setScene(scene);
                stagePicker.initModality(Modality.NONE);
                stagePicker.initOwner(Functions.primaryStage);
                stagePicker.setMaximized(false);
                stagePicker.setResizable(false);
                stagePicker.setTitle("軍隊符號(2525B)選擇視窗");
                stagePicker.setScene(scene);
            }
            milSymbolPicker.setNewMode();
            stagePicker.showAndWait();

            if (milSymbolPicker.isApplyClicked()) {
                btnCancelFE.setDisable(false);
                String selectedSIC = milSymbolPicker.getDrawSymbolID();
                String selectedShape = milSymbolPicker.getSelectedShape();
                boolean isCurveLine = milSymbolPicker.isCurveLine();
                sketchEditorFE.BeginNewDraw(selectedSIC, selectedShape, milSymbolPicker.getFEAttributes(), isCurveLine);
            } else {
                setDisableOverlayRibbonGroup(false, false, false, false);
                btnNewFE.setDisable(false);
                btnComploeteFE.setDisable(true);
                btnCancelFE.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 點選編輯,開始編輯軍符 */
    void handleEditFE(ActionEvent event) {
        if (null == sketchEditorFE.getSelectedGraphic())
            return;

        setDisableOverlayRibbonGroup(false, true, true, true);

        sketchEditorFE.BeginEditDraw();

        btnNewFE.setDisable(true);
        btnEditFE.setDisable(true);
        btnComploeteFE.setDisable(false);
        btnCancelFE.setDisable(false);

        try {
            if (stagePicker == null) {
                milSymbolPicker = new milSymbolPickerController();
                milSymbolPicker._stage_main = Functions.primaryStage;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/feoverlay/milSymbolPicker.fxml"));
                loader.setController(milSymbolPicker);
                Parent root = (Parent) loader.load();
                Scene scene = new Scene(root, 500, 800);
                scene.getStylesheets().add(Functions.appStylesheet);
                stagePicker = new Stage();
                milSymbolPicker.setDialogStage(stagePicker);
                stagePicker.setX(1400);
                stagePicker.setY(100);
                stagePicker.setScene(scene);
                stagePicker.initModality(Modality.NONE);
                stagePicker.initOwner(Functions.primaryStage);
                stagePicker.setMaximized(false);
                stagePicker.setResizable(false);
                stagePicker.setTitle("軍隊符號(2525B)選擇視窗");
                stagePicker.setScene(scene);
            }
            milSymbolPicker.setEditMode(sketchEditorFE.getSelectedGraphic());
            stagePicker.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 完成2525B 15碼選擇,於地圖上畫軍符 */
    void handleCompleteFEDraw(ActionEvent event) {
        btnNewFE.setDisable(false);
        btnEditFE.setDisable(false);
        btnComploeteFE.setDisable(true);
        btnCancelFE.setDisable(true);
        setDisableOverlayRibbonGroup(false, false, false, false);

        if (null != sketchEditorFE.getSelectedGraphic()) {
            milSymbolPicker.editAttributes.forEach((key, value) -> {
                sketchEditorFE.getSelectedGraphic().getAttributes().put(key, value);
            });
        }
        sketchEditorFE.CompleteDraw();

        if (milSymbolPicker.getDialogStage().isShowing())
            milSymbolPicker.getDialogStage().close();
    }

    /**
     * 點選取消
     */
    void handleCancelFEDraw(ActionEvent event) {
        btnNewFE.setDisable(false);
        btnEditFE.setDisable(false);
        btnComploeteFE.setDisable(true);
        btnCancelFE.setDisable(true);
        setDisableOverlayRibbonGroup(false, false, false, false);

        sketchEditorFE.CancelDraw();

        if (milSymbolPicker.getDialogStage().isShowing())
            milSymbolPicker.getDialogStage().close();
    }

    /**
     * 處理可否軍隊符號透明圖編輯刪除通知
     * 
     * @param enabled
     */
    private void handleCanEditDeleteFE(boolean enabled) {
        // 當可以解群組時,是無法編輯或刪除的
//        if (btnUnGroup.isDisable()) {
//            btnEditFE.setDisable(!enabled);
//        }
    }

    private void handleCanCompleteEvent() {
        btnComploeteFE.setDisable(false);
    }

    /****************************
     * 軍隊符號繪製相關內容結尾
     **********************************************************/

    /********************
     * 以下開始是透明圖繪製相關內容
     **********************************************************/

    // *** 透明圖
    public void initBaseRibbonControl(Button btnAddPoint, Button btnEditBase, Button btnCompleteBase,
            Button btnCancelBase, Button btnAddMultiPoint, Button btnAddPolyline, Button btnAddPolygon,
            Button btnAddFreePolyline, Button btnAddFreePolygon, Button btnAddRectangle, Button btnAddArc,
            Button btnAddCircle, Button btnAddCurve, Button btnAddSector, Button btnAddText, Button btnAddTraceLine,
            Button btnAddClipLine, Button btnMeasureBase, Button btnBookMark, Button btnSwipe, Button btnCopyPaste,
            Button btnAnalysisAlarm, Button btnAnalysisSight, Button btnPrint, Button btnAnalysisEye,
            Button btnAlarmEnable, Button btnMapDownload, Button btnAnalysisRoute) {

        this.btnAddPoint = btnAddPoint;
        this.btnEditBase = btnEditBase;
        this.btnCompleteBase = btnCompleteBase;
        this.btnCancelBase = btnCancelBase;
        this.btnAddMultiPoint = btnAddMultiPoint;
        this.btnAddPolyline = btnAddPolyline;
        this.btnAddPolygon = btnAddPolygon;
        this.btnAddFreePolyline = btnAddFreePolyline;
        this.btnAddFreePolygon = btnAddFreePolygon;
        this.btnAddRectangle = btnAddRectangle;
        this.btnAddArc = btnAddArc;
        this.btnAddCircle = btnAddCircle;
        this.btnAddCurve = btnAddCurve;
        this.btnAddSector = btnAddSector;
        this.btnAddText = btnAddText;
        this.btnAddTraceLine = btnAddTraceLine;
        this.btnAddClipLine = btnAddClipLine;
        this.btnMeasureBase = btnMeasureBase;
        this.btnBookMark = btnBookMark;
        this.btnSwipe = btnSwipe;
        this.btnCopyPaste = btnCopyPaste;
        this.btnAnalysisAlarm = btnAnalysisAlarm;
        this.btnAnalysisSight = btnAnalysisSight;
        this.btnPrint = btnPrint;
        this.btnAnalysisEye = btnAnalysisEye;
        this.btnAlarmEnable = btnAlarmEnable;
        this.btnMapDownload = btnMapDownload;
        this.btnAnalysisRoute = btnAnalysisRoute;

        btnAddPoint.setOnAction(e -> handleAddPoint(e));
        btnCompleteBase.setOnAction(e -> handleCompleteBase(e));
        btnCancelBase.setOnAction(e -> handleCancelBase(e));
        btnAddMultiPoint.setOnAction(e -> handleAddMultiPoint(e));
        btnAddPolyline.setOnAction(e -> handleAddPolyline(e));
        btnAddPolygon.setOnAction(e -> handleAddPolygon(e));
        btnAddFreePolyline.setOnAction(e -> handleAddFreePolyline(e));
        btnAddFreePolygon.setOnAction(e -> handleAddFreePolygon(e));
        btnAddRectangle.setOnAction(e -> handleAddRectangle(e));
        btnAddArc.setOnAction(e -> handleAddArc(e));
        btnAddCircle.setOnAction(e -> handleAddCircle(e));
        btnAddCurve.setOnAction(e -> handleAddCurve(e));
        btnAddSector.setOnAction(e -> handleAddSector(e));
        btnAddText.setOnAction(e -> handleAddText(e));
        btnAddTraceLine.setOnAction(e -> handleAddTraceLine(e));
        btnAddClipLine.setOnAction(e -> handleAddClipLine(e));
        btnEditBase.setOnAction(e -> handleEditBase(e));
        btnCopyPaste.setOnAction(e -> handleCopyPaste(e));

        btnCompleteBase.setDisable(true);
        btnCancelBase.setDisable(true);
    }

    private static JFrame paramFrame = null;
    private static FXMLLoader paramLoader = null;

    private static EventHandler<? super javafx.scene.input.KeyEvent> prevKeyPressedFun = null;
    private static EventHandler<? super javafx.scene.input.KeyEvent> prevKeyReleasedFun = null;
    private static EventHandler<? super MouseEvent> prevClickFun = null;
    private static EventHandler<? super MouseEvent> prevMoveFun = null;
    private static EventHandler<? super MouseEvent> prevDragedFun = null;

    private static SimpleMarkerSymbol PosMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE,
            ColorUtil.colorToArgb(Color.RED), 12);
    private static SimpleLineSymbol DefaultLineMoveSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DOT, 0xFFFF0000,
            3);
    private static Graphic firstPosGraphic = null;
    private static Graphic secondPosGraphic = null;
    private static Point firstPosPoint3D = null;
    private static Point secondPosPoint3D = null;
    private static Point lastPosPoint3D = null;

    private static Graphic nowGraphic = null;
    private static Graphic nowMoveGraphic = null;
    private static Graphic prevGraphic = null;
    private static int clickPointCount = 0;
    private static ArrayList<Point> curvePointArray3D = new ArrayList<Point>();
    private static int curvePointCount = 0;

    private static FeatureLayer traceFeatureLayer = null;
    private static GeometryType traceFeatureType = GeometryType.POLYLINE; // 待追蹤的資料型態 polyline 或 polygon
                                                                            // 追蹤方式不同
    private static Polyline traceFeatureLine = null; // 待追蹤的 feature polyline
    private static Polygon traceFeaturePolygon = null; // 待追蹤的 feature polyline
    private static int traceFirstIndex = -1;
    private static int traceLastIndex = -1;
    private static int traceNowIndex = -1;
    private static SimpleLineSymbol DefaultTraceMoveSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
            0xFFFFFF00, 5);
    private static int traceDirection = 0; // -1/逆向，0/未定，1/順向

    private static SimpleFillSymbol clipRectSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x220000FF,
            new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFFFF00, 2));

    private static ArrayList<Graphic> clipGraphicArray = new ArrayList<Graphic>();

    private static Graphic baseEditGraphic = null;
    private static Graphic baseEditGraphic_3D = null;
    private static PolygonFXController baseEditPolygonController = null;
    private static PolylineFXController baseEditPolylineController = null;
    private static PointFXController baseEditPointController = null;
    private static TextFXController baseEditTextController = null;

    // 共用參數設定視窗
    private void openParamPanel(String Title, String fxmlPath, int width, int height) {
        try {
            paramLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = paramLoader.load();
            Scene scene = new Scene(root);
            final JFXPanel jfxPanel = new JFXPanel();
            jfxPanel.setScene(scene);

            paramFrame = new JFrame(Title);
            paramFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            paramFrame.setAlwaysOnTop(true);
            paramFrame.add(jfxPanel);
            paramFrame.setSize(width, height);
            paramFrame.setLocation(0, 205);
            paramFrame.setVisible(true);
        } catch (Exception e) {
            // on any error, display the stack trace.
            //e.printStackTrace();
        }
    }

    // 關閉參數視窗
    private void closeParamPanel() {
        if (paramFrame != null) {
            paramFrame.dispatchEvent(new WindowEvent(paramFrame, WindowEvent.WINDOW_CLOSING));
            paramFrame = null;
        }
    }

    // 清除操作
    private void cleanOperate() {
        closeParamPanel();

        // sketchEditor 釋放
        if (baseEditGraphic != null) {
            baseSketchEditor.stop();
            baseEditGraphic = null;
        }

        if (prevKeyPressedFun != null) {
            mapView.setOnKeyPressed(prevKeyPressedFun);
            prevKeyPressedFun = null;
        }

        if (prevKeyReleasedFun != null) {
            mapView.setOnKeyReleased(prevKeyReleasedFun);
            prevKeyReleasedFun = null;
        }

        if (prevClickFun != null) {
            mapView.setOnMouseClicked(prevClickFun);
            prevClickFun = null;
        }

        if (prevMoveFun != null) {
            mapView.setOnMouseMoved(prevMoveFun);
            prevMoveFun = null;
        }

        if (prevDragedFun != null) {
            mapView.setOnMouseDragged(prevDragedFun);
            prevDragedFun = null;
        }

        if (nowGraphic != null) {
            overlayDrapedBaseMilitarySymbol.getGraphics().remove(nowGraphic);
            nowGraphic = null;
        }

        clearMoveGraphic();

    }

    private void clearMoveGraphic() {

        if (nowMoveGraphic != null) {
            overlayDrapedBaseMilitarySymbol.getGraphics().remove(nowMoveGraphic);
            nowMoveGraphic = null;
        }

        if (firstPosGraphic != null) {
            overlayDrapedBaseMilitarySymbol.getGraphics().remove(firstPosGraphic);
            firstPosGraphic = null;
        }

        if (secondPosGraphic != null) {
            overlayDrapedBaseMilitarySymbol.getGraphics().remove(secondPosGraphic);
            secondPosGraphic = null;
        }

        // 點下位置一併清
        firstPosPoint3D = null;
        secondPosPoint3D = null;

        if (traceFeatureLayer != null) {
            traceFeatureLayer.clearSelection();
            traceFeatureLayer = null;
        }

    }

    // 確認
    private void handleCompleteBase(ActionEvent e) {
        MapManager.isAddEditOverlay = false;
        clearMoveGraphic();

        SpatialReference reference_4326 = SpatialReference.create(4326);
        // sketcheditor 編輯存檔(修正座標即可，因僅edit位置)
        if (baseEditGraphic != null) {
            Geometry sketchGeometry = GeometryEngine.project(baseSketchEditor.getGeometry(), reference_4326);
            if (sketchGeometry != null) {
                // 先存座標
                baseEditGraphic.setGeometry(sketchGeometry);
                if (baseEditGraphic_3D != null)
                    baseEditGraphic_3D.setGeometry(sketchGeometry);
                // Symbol 依點線面字方式存
                SimpleMarkerSymbol pointMarkerSymbol = null;
                TextSymbol textSymbol = null;
                SimpleLineSymbol.Style polylineStyle = null;
                SimpleLineSymbol polylineSymbol = null;
                SimpleLineSymbol.Style polygonlineStyle = null;
                SimpleFillSymbol.Style polygonStyle = null;
                SimpleFillSymbol polygonFillSymbol = null;
                switch (baseEditGraphic.getGeometry().getGeometryType()) {
                case POLYGON:
                    polygonlineStyle = SimpleLineSymbol.Style.SOLID;
                    if (baseEditPolygonController.getPolygonLineStyle() == "虛線")
                        polygonlineStyle = SimpleLineSymbol.Style.DOT;
                    polylineSymbol = new SimpleLineSymbol(polygonlineStyle,
                            ColorUtil.colorToArgb(baseEditPolygonController.getPolygonLineColor()),
                            baseEditPolygonController.getPolygonLineSize());
                    polygonStyle = SimpleFillSymbol.Style.SOLID;
                    if (baseEditPolygonController.getPolygonStyle() == "網底")
                        polygonStyle = SimpleFillSymbol.Style.DIAGONAL_CROSS;
                    polygonFillSymbol = new SimpleFillSymbol(polygonStyle,
                            0x66000000 + ColorUtil.colorToArgb(baseEditPolygonController.getPolygonColor()),
                            polylineSymbol);
                    baseEditGraphic.setSymbol(polygonFillSymbol);
                    if (baseEditGraphic_3D != null)
                        baseEditGraphic_3D.setSymbol(polygonFillSymbol);
                    break;
                case POLYLINE:
                    polylineStyle = SimpleLineSymbol.Style.SOLID;
                    if (baseEditPolylineController.getPolylineStyle() == "虛線")
                        polylineStyle = SimpleLineSymbol.Style.DOT;
                    polylineSymbol = new SimpleLineSymbol(polylineStyle,
                            ColorUtil.colorToArgb(baseEditPolylineController.getPolylineColor()),
                            baseEditPolylineController.getPolylineSize());
                    baseEditGraphic.setSymbol(polylineSymbol);
                    if (baseEditGraphic_3D != null)
                        baseEditGraphic_3D.setSymbol(polylineSymbol);
                    break;
                case MULTIPOINT:
                    pointMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                            ColorUtil.colorToArgb(baseEditPointController.getPointColor()),
                            baseEditPointController.getPointSize());
                    baseEditGraphic.setSymbol(pointMarkerSymbol);
                    if (baseEditGraphic_3D != null)
                        baseEditGraphic_3D.setSymbol(pointMarkerSymbol);
                    break;
                case POINT:
                    if (baseEditGraphic.getSymbol().toJson().contains("esriTS")) {
                        textSymbol = new TextSymbol(baseEditTextController.getTextSize(),
                                baseEditTextController.getTextContent(),
                                ColorUtil.colorToArgb(baseEditTextController.getTextColor()),
                                TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.BOTTOM);
                        textSymbol.setFontFamily(baseEditTextController.getTextFontFamily());
                        baseEditGraphic.setSymbol(textSymbol);
                        if (baseEditGraphic_3D != null)
                            baseEditGraphic_3D.setSymbol(textSymbol);
                    } else {
                        pointMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                                ColorUtil.colorToArgb(baseEditPointController.getPointColor()),
                                baseEditPointController.getPointSize());
                        baseEditGraphic.setSymbol(pointMarkerSymbol);
                        if (baseEditGraphic_3D != null)
                            baseEditGraphic_3D.setSymbol(pointMarkerSymbol);
                    }
                }
                // 儲存透明圖
                MapManager.overlayFileObject.SaveOverlayLayerToJson();
            }
        }

        if (nowGraphic != null) {
            Geometry geometry = GeometryEngine.project(nowGraphic.getGeometry(), reference_4326);
            nowGraphic.setGeometry(geometry);
            nowGraphic.getAttributes().put("layername", MapManager.KEY_OverlayDrapedBaseSymbol);
            nowGraphic.getAttributes().put("id", java.util.UUID.randomUUID().toString());
            // 儲存透明圖至外部檔案
            MapManager.overlayFileObject.SaveOverlayLayerToJson();
            nowGraphic = null;
        }
        // 截切資料保留
        for (Graphic item : clipGraphicArray) {
            Geometry geometry = GeometryEngine.project(item.getGeometry(), reference_4326);
            item.setGeometry(geometry);
            item.getAttributes().put("layername", MapManager.KEY_OverlayDrapedBaseSymbol);
            item.getAttributes().put("id", java.util.UUID.randomUUID().toString());
            // 儲存透明圖至外部檔案
            MapManager.overlayFileObject.SaveOverlayLayerToJson();
//            // 3D 顯示要加
//            Graphic tempGraphic = new Graphic(item.getGeometry(),item.getAttributes());
//            tempGraphic.setSymbol(item.getSymbol());            
//            overlayDrapedBaseMilitarySymbol_3D.getGraphics().add(tempGraphic);
        }
        clipGraphicArray.clear();

        overlayDrapedBaseMilitarySymbol.getGraphics().forEach((g) -> {
            g.setSelected(false);
        });
        cleanOperate();
        this.btnCompleteBase.setDisable(true);
        this.btnCancelBase.setDisable(true);
        btnCopyPaste.setDisable(true);
        this.btnEditBase.setDisable(true);
        setDisableOverlayRibbonGroup(false, false, false, false);
    }

    // 取消
    private void handleCancelBase(ActionEvent e) {
        // 截切資料清除
        for (Graphic item : clipGraphicArray)
            overlayDrapedBaseMilitarySymbol.getGraphics().remove(item);
        clipGraphicArray.clear();

        MapManager.isAddEditOverlay = false;
        overlayDrapedBaseMilitarySymbol.getGraphics().forEach((g) -> {
            g.setSelected(false);
        });
        cleanOperate();
        this.btnCompleteBase.setDisable(true);
        this.btnCancelBase.setDisable(true);
        this.btnEditBase.setDisable(true);
        setDisableOverlayRibbonGroup(false, false, false, false);
    }

    // 繪點
    private void handleAddPoint(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("點繪製參數", "/fxml/pointFX.fxml", 300, 200);
        PointFXController pointControllerHandle = (PointFXController) paramLoader.getController();
        pointControllerHandle.loadParameter();

        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        prevClickFun = mapView.getOnMouseClicked();
        prevGraphic = null;
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    if (prevGraphic != null)
                        sketchlayer.getGraphics().remove(prevGraphic);
                    nowGraphic = new Graphic(relativeSurfacePoint);
                    pointControllerHandle.saveParameter();
                    SimpleMarkerSymbol pointMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                            ColorUtil.colorToArgb(pointControllerHandle.getPointColor()),
                            pointControllerHandle.getPointSize());
                    nowGraphic.setSymbol(pointMarkerSymbol);
                    sketchlayer.getGraphics().add(nowGraphic);
                    prevGraphic = nowGraphic;
                }
            }
        });
    }

    // 繪多點
    private void handleAddMultiPoint(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("多點繪製參數", "/fxml/pointFX.fxml", 300, 200);
        PointFXController pointControllerHandle = (PointFXController) paramLoader.getController();
        pointControllerHandle.loadParameter();

        PointCollection multiPointCollection = new PointCollection(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    multiPointCollection.add(relativeSurfacePoint);
                    MultipointBuilder multiPointBuilder = new MultipointBuilder(multiPointCollection);
                    pointControllerHandle.saveParameter();
                    SimpleMarkerSymbol pointMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                            ColorUtil.colorToArgb(pointControllerHandle.getPointColor()),
                            pointControllerHandle.getPointSize());
                    nowGraphic.setSymbol(pointMarkerSymbol);
                    nowGraphic.setGeometry(multiPointBuilder.toGeometry());
                }
            }
        });
    }

    // 繪線
    private void handleAddPolyline(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("線繪製參數", "/fxml/polylineFX.fxml", 300, 250);
        PolylineFXController polylineControllerHandle = (PolylineFXController) paramLoader.getController();
        polylineControllerHandle.loadParameter();

        PolylineBuilder polylineBuilder = new PolylineBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        nowMoveGraphic = new Graphic();
        nowMoveGraphic.setSymbol(DefaultLineMoveSymbol);
        sketchlayer.getGraphics().add(nowMoveGraphic);

        clickPointCount = 0;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    polylineBuilder.addPoint(relativeSurfacePoint);
                    clickPointCount++;
                    if (clickPointCount >= 1) {
                        polylineControllerHandle.saveParameter();
                        SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                        if (polylineControllerHandle.getPolylineStyle() == "虛線")
                            polylineStyle = SimpleLineSymbol.Style.DOT;
                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polylineStyle,
                                ColorUtil.colorToArgb(polylineControllerHandle.getPolylineColor()),
                                polylineControllerHandle.getPolylineSize());
                        nowGraphic.setSymbol(polylineSymbol);
                        nowGraphic.setGeometry(polylineBuilder.toGeometry());
                    }
                }
            }
        });
        prevMoveFun = mapView.getOnMouseMoved();
        mapView.setOnMouseMoved(event -> {
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
            if (relativeSurfacePoint != null) {
                if (clickPointCount > 0) {
                    Part part = polylineBuilder.getParts().get(0);
                    Point last_p = part.getEndPoint();
                    PolylineBuilder line_temp = new PolylineBuilder(mapView.getSpatialReference());
                    line_temp.addPoint(last_p);
                    line_temp.addPoint(relativeSurfacePoint);
                    nowMoveGraphic.setGeometry(line_temp.toGeometry());
                }
            }
        });
    }

    // 繪面
    private void handleAddPolygon(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("面繪製參數", "/fxml/polygonFX.fxml", 300, 280);
        PolygonFXController polygonControllerHandle = (PolygonFXController) paramLoader.getController();
        polygonControllerHandle.loadParameter();

        PolygonBuilder polygonBuilder = new PolygonBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        nowMoveGraphic = new Graphic();
        nowMoveGraphic.setSymbol(DefaultLineMoveSymbol);
        sketchlayer.getGraphics().add(nowMoveGraphic);

        clickPointCount = 0;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    polygonBuilder.addPoint(relativeSurfacePoint);
                    clickPointCount++;
                    if (clickPointCount >= 1) {
                        polygonControllerHandle.saveParameter();
                        SimpleLineSymbol.Style polygonlineStyle = SimpleLineSymbol.Style.SOLID;
                        if (polygonControllerHandle.getPolygonLineStyle() == "虛線")
                            polygonlineStyle = SimpleLineSymbol.Style.DOT;
                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polygonlineStyle,
                                ColorUtil.colorToArgb(polygonControllerHandle.getPolygonLineColor()),
                                polygonControllerHandle.getPolygonLineSize());
                        SimpleFillSymbol.Style polygonStyle = SimpleFillSymbol.Style.SOLID;
                        if (polygonControllerHandle.getPolygonStyle() == "網底")
                            polygonStyle = SimpleFillSymbol.Style.DIAGONAL_CROSS;
                        SimpleFillSymbol polygonFillSymbol = new SimpleFillSymbol(polygonStyle,
                                0x66000000 + ColorUtil.colorToArgb(polygonControllerHandle.getPolygonColor()),
                                polylineSymbol);
                        nowGraphic.setSymbol(polygonFillSymbol);
                        nowGraphic.setGeometry(polygonBuilder.toGeometry());
                    }
                }
            }
        });
        prevMoveFun = mapView.getOnMouseMoved();
        mapView.setOnMouseMoved(event -> {
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
            if (relativeSurfacePoint != null) {
                if (clickPointCount > 0) {
                    Part part = polygonBuilder.getParts().get(0);
                    Point last_p = part.getEndPoint();
                    Point first_p = part.getStartPoint();
                    PolylineBuilder polygon_temp = new PolylineBuilder(mapView.getSpatialReference());
                    polygon_temp.addPoint(last_p);
                    polygon_temp.addPoint(relativeSurfacePoint);
                    polygon_temp.addPoint(first_p);
                    nowMoveGraphic.setGeometry(polygon_temp.toGeometry());
                }
            }
        });
    }

    // 自由線
    private void handleAddFreePolyline(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("自由線繪製參數", "/fxml/polylineFX.fxml", 300, 250);
        PolylineFXController polylineControllerHandle = (PolylineFXController) paramLoader.getController();
        polylineControllerHandle.loadParameter();

        PolylineBuilder polylineBuilder = new PolylineBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);

        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
        });
        prevDragedFun = mapView.getOnMouseDragged();
        mapView.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    polylineBuilder.addPoint(relativeSurfacePoint);
                    polylineControllerHandle.saveParameter();
                    SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                    if (polylineControllerHandle.getPolylineStyle() == "虛線")
                        polylineStyle = SimpleLineSymbol.Style.DOT;
                    SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polylineStyle,
                            ColorUtil.colorToArgb(polylineControllerHandle.getPolylineColor()),
                            polylineControllerHandle.getPolylineSize());
                    nowGraphic.setSymbol(polylineSymbol);
                    nowGraphic.setGeometry(polylineBuilder.toGeometry());
                }
            }
        });
    }

    // 自由面
    private void handleAddFreePolygon(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("自由面繪製參數", "/fxml/polygonFX.fxml", 300, 280);
        PolygonFXController polygonControllerHandle = (PolygonFXController) paramLoader.getController();
        polygonControllerHandle.loadParameter();

        PolygonBuilder polygonBuilder = new PolygonBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);

        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
        });
        prevDragedFun = mapView.getOnMouseDragged();
        mapView.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    polygonBuilder.addPoint(relativeSurfacePoint);
                    polygonControllerHandle.saveParameter();
                    SimpleLineSymbol.Style polygonlineStyle = SimpleLineSymbol.Style.SOLID;
                    if (polygonControllerHandle.getPolygonLineStyle() == "虛線")
                        polygonlineStyle = SimpleLineSymbol.Style.DOT;
                    SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polygonlineStyle,
                            ColorUtil.colorToArgb(polygonControllerHandle.getPolygonLineColor()),
                            polygonControllerHandle.getPolygonLineSize());
                    SimpleFillSymbol.Style polygonStyle = SimpleFillSymbol.Style.SOLID;
                    if (polygonControllerHandle.getPolygonStyle() == "網底")
                        polygonStyle = SimpleFillSymbol.Style.DIAGONAL_CROSS;
                    SimpleFillSymbol polygonFillSymbol = new SimpleFillSymbol(polygonStyle,
                            0x66000000 + ColorUtil.colorToArgb(polygonControllerHandle.getPolygonColor()),
                            polylineSymbol);
                    nowGraphic.setSymbol(polygonFillSymbol);
                    nowGraphic.setGeometry(polygonBuilder.toGeometry());
                }
            }
        });
    }

    // 繪矩形
    private void handleAddRectangle(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("矩形繪製參數", "/fxml/polygonFX.fxml", 300, 250);
        PolygonFXController polygonControllerHandle = (PolygonFXController) paramLoader.getController();
        polygonControllerHandle.loadParameter();

        PolygonBuilder polygonBuilder = new PolygonBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        nowMoveGraphic = new Graphic();
        nowMoveGraphic.setSymbol(DefaultLineMoveSymbol);
        sketchlayer.getGraphics().add(nowMoveGraphic);

        firstPosPoint3D = null;
        secondPosPoint3D = null;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    if (firstPosPoint3D == null) {
                        firstPosPoint3D = relativeSurfacePoint;
                    } else {
                        Point2D secondPosPoint = screenPoint;
                        secondPosPoint3D = relativeSurfacePoint;
                        Point2D firstPosPoint = mapView.locationToScreen(firstPosPoint3D);
                        PolygonBuilder rectBuilder = new PolygonBuilder(mapView.getSpatialReference());
                        Point2D next_p = firstPosPoint;
                        Point next_p_surf = mapView.screenToLocation(next_p);
                        rectBuilder.addPoint(next_p_surf);
                        next_p = new Point2D(secondPosPoint.getX(), firstPosPoint.getY());
                        next_p_surf = mapView.screenToLocation(next_p);
                        rectBuilder.addPoint(next_p_surf);
                        rectBuilder.addPoint(secondPosPoint3D);
                        next_p = new Point2D(firstPosPoint.getX(), secondPosPoint.getY());
                        next_p_surf = mapView.screenToLocation(next_p);
                        rectBuilder.addPoint(next_p_surf);
                        next_p_surf = mapView.screenToLocation(firstPosPoint);
                        rectBuilder.addPoint(next_p_surf);
                        polygonControllerHandle.saveParameter();
                        SimpleLineSymbol.Style polygonlineStyle = SimpleLineSymbol.Style.SOLID;
                        if (polygonControllerHandle.getPolygonLineStyle() == "虛線")
                            polygonlineStyle = SimpleLineSymbol.Style.DOT;
                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polygonlineStyle,
                                ColorUtil.colorToArgb(polygonControllerHandle.getPolygonLineColor()),
                                polygonControllerHandle.getPolygonLineSize());
                        SimpleFillSymbol.Style polygonStyle = SimpleFillSymbol.Style.SOLID;
                        if (polygonControllerHandle.getPolygonStyle() == "網底")
                            polygonStyle = SimpleFillSymbol.Style.DIAGONAL_CROSS;
                        SimpleFillSymbol polygonFillSymbol = new SimpleFillSymbol(polygonStyle,
                                0x66000000 + ColorUtil.colorToArgb(polygonControllerHandle.getPolygonColor()),
                                polylineSymbol);
                        nowGraphic.setSymbol(polygonFillSymbol);
                        nowGraphic.setGeometry(rectBuilder.toGeometry());
                    }
                }
            }
        });
        prevMoveFun = mapView.getOnMouseMoved();
        mapView.setOnMouseMoved(event -> {
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
            if (relativeSurfacePoint != null) {
                if (firstPosPoint3D != null) {
                    Point2D secondPosPoint = screenPoint;
                    secondPosPoint3D = relativeSurfacePoint;
                    Point2D firstPosPoint = mapView.locationToScreen(firstPosPoint3D);
                    PolylineBuilder rect_temp = new PolylineBuilder(mapView.getSpatialReference());
                    Point next_p_surf = mapView.screenToLocation(firstPosPoint);
                    rect_temp.addPoint(next_p_surf);
                    Point2D next_p = new Point2D(secondPosPoint.getX(), firstPosPoint.getY());
                    next_p_surf = mapView.screenToLocation(next_p);
                    rect_temp.addPoint(next_p_surf);
                    rect_temp.addPoint(secondPosPoint3D);
                    next_p = new Point2D(firstPosPoint.getX(), secondPosPoint.getY());
                    next_p_surf = mapView.screenToLocation(next_p);
                    rect_temp.addPoint(next_p_surf);
                    next_p_surf = mapView.screenToLocation(firstPosPoint);
                    rect_temp.addPoint(next_p_surf);
                    nowMoveGraphic.setGeometry(rect_temp.toGeometry());
                }
            }
        });
    }

    // 繪弧線
    private void handleAddArc(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("弧線繪製參數", "/fxml/polylineFX.fxml", 300, 250);
        PolylineFXController polylineControllerHandle = (PolylineFXController) paramLoader.getController();
        polylineControllerHandle.loadParameter();

        PolylineBuilder polylineBuilder = new PolylineBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        // 1 2 點初始
        firstPosGraphic = null;
        secondPosGraphic = null;
        firstPosPoint3D = null;
        secondPosPoint3D = null;

        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    // 1 2 點標示
                    if (firstPosPoint3D == null) {
                        firstPosPoint3D = relativeSurfacePoint;
                        firstPosGraphic = new Graphic(firstPosPoint3D);
                        firstPosGraphic.setSymbol(PosMarkerSymbol);
                        sketchlayer.getGraphics().add(firstPosGraphic);
                    } else if (secondPosPoint3D == null) {
                        secondPosPoint3D = relativeSurfacePoint;
                        secondPosGraphic = new Graphic(secondPosPoint3D);
                        secondPosGraphic.setSymbol(PosMarkerSymbol);
                        sketchlayer.getGraphics().add(secondPosGraphic);
                    }
                    // 否則畫弧
                    else {
                        Point2D firstPosPoint = mapView.locationToScreen(firstPosPoint3D);
                        Point2D secondPosPoint = mapView.locationToScreen(secondPosPoint3D);

                        polylineControllerHandle.saveParameter();
                        SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                        if (polylineControllerHandle.getPolylineStyle() == "虛線")
                            polylineStyle = SimpleLineSymbol.Style.DOT;
                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polylineStyle,
                                ColorUtil.colorToArgb(polylineControllerHandle.getPolylineColor()),
                                polylineControllerHandle.getPolylineSize());

                        nowGraphic.setSymbol(polylineSymbol);
                        DrawArcFromThreePoint(firstPosPoint.getX(), -firstPosPoint.getY(), secondPosPoint.getX(),
                                -secondPosPoint.getY(), screenPoint.getX(), -screenPoint.getY());
                    }
                }
            }
        });
    }

    // 三點畫弧
    private void DrawArcFromThreePoint(double x1, double y1, double x2, double y2, double x3, double y3) {
        double a = x1 - x2;
        double b = y1 - y2;
        double c = x1 - x3;
        double d = y1 - y3;
        double e = ((x1 * x1 - x2 * x2) + (y1 * y1 - y2 * y2)) / 2.0;
        double f = ((x1 * x1 - x3 * x3) + (y1 * y1 - y3 * y3)) / 2.0;

        double det = b * c - a * d;
        if (Math.abs(det) > 0.001) {
            // 原點
            double x0 = -(d * e - b * f) / det;
            double y0 = -(a * f - c * e) / det;
            // 半徑
            double radius = Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));

            // 三點角度
            double angle1;
            double angle2;
            double angle3;

            angle1 = Math.acos((x1 - x0) / radius);
            if ((y1 - y0) < 0)
                angle1 = 2 * 3.14 - angle1;
            angle1 = angle1 / 3.14 * 180;
            angle2 = Math.acos((x2 - x0) / radius);
            if ((y2 - y0) < 0)
                angle2 = 2 * 3.14 - angle2;
            angle2 = angle2 / 3.14 * 180;
            angle3 = Math.acos((x3 - x0) / radius);
            if ((y3 - y0) < 0)
                angle3 = 2 * 3.14 - angle3;
            angle3 = angle3 / 3.14 * 180;

            boolean PosDown = false;

            angle1 = 90 - angle1;
            angle2 = 90 - angle2;
            angle3 = 90 - angle3;

            double Delta13;
            if (angle1 < angle3) {
                Delta13 = angle3 - angle1;
            } else
                Delta13 = angle3 - angle1 + 360;
            double Delta12;

            if (angle1 < angle2) {
                Delta12 = angle2 - angle1;
            } else
                Delta12 = angle2 - angle1 + 360;

            if (Delta13 > Delta12)
                PosDown = true;
            else
                PosDown = false;

            if (PosDown) {
                if (angle3 > angle1)
                    DrawArc3D(x0, y0, radius, angle1, angle3 - angle1, 2);
                else
                    DrawArc3D(x0, y0, radius, angle1, angle3 - angle1 + 360, 2);
            } else {
                if (angle1 > angle3)
                    DrawArc3D(x0, y0, radius, angle3, angle1 - angle3, 2);
                else
                    DrawArc3D(x0, y0, radius, angle3, angle1 - angle3 + 360, 2);
            }

        }
    }

    // 畫弧副程式
    private void DrawArc3D(double x0, double y0, double radius, double start_ang, double len_ang, double sec_ang) {
        PolylineBuilder rect_temp = new PolylineBuilder(mapView.getSpatialReference());
        for (double angle = start_ang; angle < (start_ang + len_ang); angle = angle + sec_ang) {
            double x = radius * Math.sin(Math.toRadians(angle)) + x0;
            double y = radius * Math.cos(Math.toRadians(angle)) + y0;
            Point2D next_p = new Point2D(x, -y);
            Point next_p_surf = mapView.screenToLocation(next_p);
            rect_temp.addPoint(next_p_surf);
        }
        nowGraphic.setGeometry(rect_temp.toGeometry());
    }

    // 繪圓/橢圓
    private void handleAddCircle(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("圓/橢圓繪製參數", "/fxml/polygonFX.fxml", 300, 280);
        PolygonFXController polygonControllerHandle = (PolygonFXController) paramLoader.getController();
        polygonControllerHandle.loadParameter();

        PolygonBuilder polygonBuilder = new PolygonBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        nowMoveGraphic = new Graphic();
        nowMoveGraphic.setSymbol(DefaultLineMoveSymbol);
        sketchlayer.getGraphics().add(nowMoveGraphic);

        firstPosPoint3D = null;
        secondPosPoint3D = null;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            boolean isShift = false;
            if (event.isShiftDown())
                isShift = true;
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    if (firstPosPoint3D == null) {
                        firstPosPoint3D = relativeSurfacePoint;
                    } else {
                        Point2D secondPosPoint = screenPoint;
                        secondPosPoint3D = relativeSurfacePoint;
                        Point2D firstPosPoint = mapView.locationToScreen(firstPosPoint3D);

                        if (isShift) {
                            if (Math.abs(secondPosPoint.getX() - firstPosPoint.getX()) > Math
                                    .abs(secondPosPoint.getY() - firstPosPoint.getY())) {
                                if (secondPosPoint.getY() > firstPosPoint.getY()) {
                                    secondPosPoint = new Point2D(secondPosPoint.getX(),
                                            secondPosPoint.getY()
                                                    + Math.abs(secondPosPoint.getX() - firstPosPoint.getX())
                                                    - Math.abs(secondPosPoint.getY() - firstPosPoint.getY()));
                                } else {
                                    secondPosPoint = new Point2D(secondPosPoint.getX(),
                                            secondPosPoint.getY()
                                                    - Math.abs(secondPosPoint.getX() - firstPosPoint.getX())
                                                    + Math.abs(secondPosPoint.getY() - firstPosPoint.getY()));
                                }
                            } else {
                                if (secondPosPoint.getX() > firstPosPoint.getX()) {
                                    secondPosPoint = new Point2D(
                                            secondPosPoint.getX()
                                                    + Math.abs(secondPosPoint.getX() - firstPosPoint.getX())
                                                    - Math.abs(secondPosPoint.getY() - firstPosPoint.getY()),
                                            secondPosPoint.getY());
                                } else {
                                    secondPosPoint = new Point2D(
                                            secondPosPoint.getX()
                                                    - Math.abs(secondPosPoint.getX() - firstPosPoint.getX())
                                                    + Math.abs(secondPosPoint.getY() - firstPosPoint.getY()),
                                            secondPosPoint.getY());
                                }
                            }
                            secondPosPoint3D = mapView.screenToLocation(secondPosPoint);
                        }

                        PolygonBuilder circleBuilder = new PolygonBuilder(mapView.getSpatialReference());
                        polygonControllerHandle.saveParameter();
                        SimpleLineSymbol.Style polygonlineStyle = SimpleLineSymbol.Style.SOLID;
                        if (polygonControllerHandle.getPolygonLineStyle() == "虛線")
                            polygonlineStyle = SimpleLineSymbol.Style.DOT;
                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polygonlineStyle,
                                ColorUtil.colorToArgb(polygonControllerHandle.getPolygonLineColor()),
                                polygonControllerHandle.getPolygonLineSize());
                        SimpleFillSymbol.Style polygonStyle = SimpleFillSymbol.Style.SOLID;
                        if (polygonControllerHandle.getPolygonStyle() == "網底")
                            polygonStyle = SimpleFillSymbol.Style.DIAGONAL_CROSS;
                        SimpleFillSymbol polygonFillSymbol = new SimpleFillSymbol(polygonStyle,
                                0x66000000 + ColorUtil.colorToArgb(polygonControllerHandle.getPolygonColor()),
                                polylineSymbol);
                        nowGraphic.setSymbol(polygonFillSymbol);
                        // 計算中心點及長短軸半值
                        double center_x = (firstPosPoint.getX() + secondPosPoint.getX()) / 2;
                        double center_y = (firstPosPoint.getY() + secondPosPoint.getY()) / 2;
                        double half_x = Math.abs(firstPosPoint.getX() - secondPosPoint.getX()) / 2;
                        double half_y = Math.abs(firstPosPoint.getY() - secondPosPoint.getY()) / 2;
                        DrawCircle3D(circleBuilder, center_x, -center_y, half_x, half_y);
                        nowGraphic.setGeometry(circleBuilder.toGeometry());
                    }
                }
            }
        });
        prevMoveFun = mapView.getOnMouseMoved();
        mapView.setOnMouseMoved(event -> {
            boolean isShift = false;
            if (event.isShiftDown())
                isShift = true;
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
            if (relativeSurfacePoint != null) {
                if (firstPosPoint3D != null) {
                    Point2D secondPosPoint = screenPoint;
                    secondPosPoint3D = relativeSurfacePoint;
                    Point2D firstPosPoint = mapView.locationToScreen(firstPosPoint3D);

                    if (isShift) {
                        if (Math.abs(secondPosPoint.getX() - firstPosPoint.getX()) > Math
                                .abs(secondPosPoint.getY() - firstPosPoint.getY())) {
                            if (secondPosPoint.getY() > firstPosPoint.getY()) {
                                secondPosPoint = new Point2D(secondPosPoint.getX(),
                                        secondPosPoint.getY() + Math.abs(secondPosPoint.getX() - firstPosPoint.getX())
                                                - Math.abs(secondPosPoint.getY() - firstPosPoint.getY()));
                            } else {
                                secondPosPoint = new Point2D(secondPosPoint.getX(),
                                        secondPosPoint.getY() - Math.abs(secondPosPoint.getX() - firstPosPoint.getX())
                                                + Math.abs(secondPosPoint.getY() - firstPosPoint.getY()));
                            }
                        } else {
                            if (secondPosPoint.getX() > firstPosPoint.getX()) {
                                secondPosPoint = new Point2D(
                                        secondPosPoint.getX() + Math.abs(secondPosPoint.getX() - firstPosPoint.getX())
                                                - Math.abs(secondPosPoint.getY() - firstPosPoint.getY()),
                                        secondPosPoint.getY());
                            } else {
                                secondPosPoint = new Point2D(
                                        secondPosPoint.getX() - Math.abs(secondPosPoint.getX() - firstPosPoint.getX())
                                                + Math.abs(secondPosPoint.getY() - firstPosPoint.getY()),
                                        secondPosPoint.getY());
                            }
                        }
                        secondPosPoint3D = mapView.screenToLocation(secondPosPoint);
                    }

                    PolygonBuilder circleBuilder = new PolygonBuilder(mapView.getSpatialReference());
                    SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
                            ColorUtil.colorToArgb(Color.RED), 2);
                    SimpleFillSymbol polygonFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.NULL,
                            ColorUtil.colorToArgb(Color.WHITE), polylineSymbol);
                    nowMoveGraphic.setSymbol(polygonFillSymbol);
                    // 計算中心點及長短軸半值
                    double center_x = (firstPosPoint.getX() + secondPosPoint.getX()) / 2;
                    double center_y = (firstPosPoint.getY() + secondPosPoint.getY()) / 2;
                    double half_x = Math.abs(firstPosPoint.getX() - secondPosPoint.getX()) / 2;
                    double half_y = Math.abs(firstPosPoint.getY() - secondPosPoint.getY()) / 2;
                    DrawCircle3D(circleBuilder, center_x, -center_y, half_x, half_y);
                    nowMoveGraphic.setGeometry(circleBuilder.toGeometry());
                }
            }
        });
    }

    private void DrawCircle3D(PolygonBuilder rect_temp, double x0, double y0, double radius_x, double radius_y) {
        for (double angle = 0; angle < 360; angle = angle + 2) {
            double x = radius_x * Math.cos(Math.toRadians(angle)) + x0;
            double y = radius_y * Math.sin(Math.toRadians(angle)) + y0;
            Point2D next_p = new Point2D(x, -y);
            Point next_p_surf = mapView.screenToLocation(next_p);
            rect_temp.addPoint(next_p_surf);
        }
    }

    // 繪曲線
    private void handleAddCurve(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("曲線繪製參數", "/fxml/polylineFX.fxml", 300, 250);
        PolylineFXController polylineControllerHandle = (PolylineFXController) paramLoader.getController();
        polylineControllerHandle.loadParameter();

        PolylineBuilder polylineBuilder = new PolylineBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        nowMoveGraphic = new Graphic();
        nowMoveGraphic.setSymbol(DefaultLineMoveSymbol);
        sketchlayer.getGraphics().add(nowMoveGraphic);

        curvePointArray3D.clear();
        curvePointCount = 0;
        lastPosPoint3D = null;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    lastPosPoint3D = relativeSurfacePoint;
                    curvePointArray3D.add(lastPosPoint3D);
                    curvePointCount++;
                    // 三點以上即可繪製曲線
                    if (curvePointCount > 2) {
                        polylineControllerHandle.saveParameter();
                        SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                        if (polylineControllerHandle.getPolylineStyle() == "虛線")
                            polylineStyle = SimpleLineSymbol.Style.DOT;
                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polylineStyle,
                                ColorUtil.colorToArgb(polylineControllerHandle.getPolylineColor()),
                                polylineControllerHandle.getPolylineSize());
                        nowGraphic.setSymbol(polylineSymbol);
                        DrawCurve();
                    }
                }
            }
        });
        prevMoveFun = mapView.getOnMouseMoved();
        mapView.setOnMouseMoved(event -> {
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
            if (relativeSurfacePoint != null) {
                if (lastPosPoint3D != null) {
                    Point2D lastPosPoint = mapView.locationToScreen(lastPosPoint3D);
                    Point last_p = mapView.screenToLocation(lastPosPoint);
                    PolylineBuilder line_temp = new PolylineBuilder(mapView.getSpatialReference());
                    line_temp.addPoint(last_p);
                    line_temp.addPoint(relativeSurfacePoint);
                    nowMoveGraphic.setGeometry(line_temp.toGeometry());
                }
            }
        });
    }

    // 畫曲線
    private void DrawCurve() {
        PolylineBuilder rect_temp = new PolylineBuilder(mapView.getSpatialReference());
        // 首點
        Point2D next_p = mapView.locationToScreen(curvePointArray3D.get(0));
        Point next_p_surf = mapView.screenToLocation(next_p);
        rect_temp.addPoint(next_p_surf);
        for (int i = 0; i < (curvePointCount - 2); i++) {
            // 找出此圓角第一點
            Point2D p1 = mapView.locationToScreen(curvePointArray3D.get(i));
            Point2D p2 = mapView.locationToScreen(curvePointArray3D.get(i + 1));
            double x1 = p1.getX() + (p2.getX() - p1.getX()) * 0.5;
            double y1 = p1.getY() + (p2.getY() - p1.getY()) * 0.5;
            // 找出此圓角第二點
            p1 = mapView.locationToScreen(curvePointArray3D.get(i + 1));
            p2 = mapView.locationToScreen(curvePointArray3D.get(i + 2));
            double x2 = p1.getX() + (p2.getX() - p1.getX()) * 0.5;
            double y2 = p1.getY() + (p2.getY() - p1.getY()) * 0.5;
            // 繪製此曲線
            DrawBasizer(rect_temp, x1, y1, p1.getX(), p1.getY(), x2, y2);
        }
        // 尾點
        next_p = mapView.locationToScreen(curvePointArray3D.get(curvePointCount - 1));
        next_p_surf = mapView.screenToLocation(next_p);
        rect_temp.addPoint(next_p_surf);
        // 設給 curveGraphic
        nowGraphic.setGeometry(rect_temp.toGeometry());
    }

    // 貝茲曲線
    private void DrawBasizer(PolylineBuilder rect_temp, double x1, double y1, double x2, double y2, double x3,
            double y3) {
        double dt = 1.0 / 10;
        for (int i = 0; i < 10; i++) {
            double t = i * dt;

            double nx = Math.pow(1.0 - t, 2) * x1 + 2 * t * (1.0 - t) * x2 + Math.pow(t, 2) * x3;
            double ny = Math.pow(1.0 - t, 2) * y1 + 2 * t * (1.0 - t) * y2 + Math.pow(t, 2) * y3;

            Point2D next_p = new Point2D(nx, ny);
            Point next_p_surf = mapView.screenToLocation(next_p);
            rect_temp.addPoint(next_p_surf);
        }
        Point2D next_p = new Point2D(x3, y3);
        Point next_p_surf = mapView.screenToLocation(next_p);
        rect_temp.addPoint(next_p_surf);
    }

    // 繪扇形
    private void handleAddSector(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("扇形繪製參數", "/fxml/polygonFX.fxml", 300, 250);
        PolygonFXController polygonControllerHandle = (PolygonFXController) paramLoader.getController();
        polygonControllerHandle.loadParameter();

        PolygonBuilder polygonBuilder = new PolygonBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        // 首點初始
        firstPosGraphic = null;
        firstPosPoint3D = null;
        secondPosGraphic = null;
        secondPosPoint3D = null;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    // 首點標示
                    if (firstPosPoint3D == null) {
                        firstPosPoint3D = relativeSurfacePoint;
                        firstPosGraphic = new Graphic(firstPosPoint3D);
                        firstPosGraphic.setSymbol(PosMarkerSymbol);
                        sketchlayer.getGraphics().add(firstPosGraphic);
                    } else if (secondPosPoint3D == null) {
                        secondPosPoint3D = relativeSurfacePoint;
                        secondPosGraphic = new Graphic(secondPosPoint3D);
                        secondPosGraphic.setSymbol(PosMarkerSymbol);
                        sketchlayer.getGraphics().add(secondPosGraphic);
                    }
                    // 否則畫扇形
                    else {
                        Point2D firstPosPoint = mapView.locationToScreen(firstPosPoint3D);
                        Point2D secondPosPoint = mapView.locationToScreen(secondPosPoint3D);

                        polygonControllerHandle.saveParameter();
                        SimpleLineSymbol.Style polygonlineStyle = SimpleLineSymbol.Style.SOLID;
                        if (polygonControllerHandle.getPolygonLineStyle() == "虛線")
                            polygonlineStyle = SimpleLineSymbol.Style.DOT;
                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polygonlineStyle,
                                ColorUtil.colorToArgb(polygonControllerHandle.getPolygonLineColor()),
                                polygonControllerHandle.getPolygonLineSize());
                        SimpleFillSymbol.Style polygonStyle = SimpleFillSymbol.Style.SOLID;
                        if (polygonControllerHandle.getPolygonStyle() == "網底")
                            polygonStyle = SimpleFillSymbol.Style.DIAGONAL_CROSS;
                        SimpleFillSymbol polygonFillSymbol = new SimpleFillSymbol(polygonStyle,
                                0x66000000 + ColorUtil.colorToArgb(polygonControllerHandle.getPolygonColor()),
                                polylineSymbol);
                        nowGraphic.setSymbol(polygonFillSymbol);
                        DrawSectorFromThreePoint(firstPosPoint.getX(), -firstPosPoint.getY(), secondPosPoint.getX(),
                                -secondPosPoint.getY(), screenPoint.getX(), -screenPoint.getY());
                    }
                }
            }
        });
    }

    // 三點畫扇形
    private void DrawSectorFromThreePoint(double x0, double y0, double x1, double y1, double x2, double y2) {
        // x0 y0 為圓心
        // x1 y1 與 x0 y0 為半徑
        double radius = Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        // x2 y2 要依半徑重新計算
        double n = radius;
        double m = Math.sqrt((x2 - x0) * (x2 - x0) + (y2 - y0) * (y2 - y0));
        double nx2 = x0 + (x2 - x0) * n / m;
        double ny2 = y0 + (y2 - y0) * n / m;
        // 角度
        double angle1;
        angle1 = Math.acos((x1 - x0) / radius);
        if ((y1 - y0) < 0)
            angle1 = 2 * 3.14 - angle1;
        angle1 = angle1 / 3.14 * 180;
        double angle3;
        angle3 = Math.acos((nx2 - x0) / radius);
        if ((ny2 - y0) < 0)
            angle3 = 2 * 3.14 - angle3;
        angle3 = angle3 / 3.14 * 180;
        double angle2;
        if (angle3 > angle1)
            if ((angle3 - angle1) >= 180)
                angle2 = angle1 - (360 - angle3 + angle1) / 2;
            else
                angle2 = angle1 + (angle3 - angle1) / 2;
        else if ((angle1 - angle3) >= 180)
            angle2 = angle1 + (360 - angle1 + angle3) / 2;
        else
            angle2 = angle1 - (angle1 - angle3) / 2;
        double nx1 = x0 + radius * Math.cos(angle2 * 3.14 / 180);
        double ny1 = y0 + radius * Math.sin(angle2 * 3.14 / 180);
        DrawSectorFromTwoPoint(x0, y0, nx1, ny1, Math.abs(angle1 - angle2));

    }

    // 兩點+角度畫扇形
    private void DrawSectorFromTwoPoint(double x0, double y0, double x2, double y2, double span_ang) {
        // 半徑
        double radius = Math.sqrt((x2 - x0) * (x2 - x0) + (y2 - y0) * (y2 - y0));
        // 角度
        double angle2;
        angle2 = Math.acos((x2 - x0) / radius);
        if ((y2 - y0) < 0)
            angle2 = 2 * 3.14 - angle2;
        angle2 = angle2 / 3.14 * 180;
        double angle1 = angle2 + span_ang;
        double angle3 = angle2 - span_ang;

        boolean PosDown = false;

        angle1 = 90 - angle1;
        angle2 = 90 - angle2;
        angle3 = 90 - angle3;

        double Delta13;
        if (angle1 < angle3) {
            Delta13 = angle3 - angle1;
        } else
            Delta13 = angle3 - angle1 + 360;
        double Delta12;

        if (angle1 < angle2) {
            Delta12 = angle2 - angle1;
        } else
            Delta12 = angle2 - angle1 + 360;

        if (Delta13 > Delta12)
            PosDown = true;
        else
            PosDown = false;

        if (PosDown) {
            if (angle3 > angle1)
                DrawSector3D(x0, y0, radius, angle1, angle3 - angle1, 2);
            else
                DrawSector3D(x0, y0, radius, angle1, angle3 - angle1 + 360, 2);
        } else {
            if (angle1 > angle3)
                DrawSector3D(x0, y0, radius, angle3, angle1 - angle3, 2);
            else
                DrawSector3D(x0, y0, radius, angle3, angle1 - angle3 + 360, 2);
        }
    }

    // 畫扇形副程式
    private void DrawSector3D(double x0, double y0, double radius, double start_ang, double len_ang, double sec_ang) {
        PolygonBuilder rect_temp = new PolygonBuilder(mapView.getSpatialReference());
        Point2D next_p = new Point2D(x0, -y0);
        Point next_p_surf = mapView.screenToLocation(next_p);
        rect_temp.addPoint(next_p_surf);
        for (double angle = start_ang; angle < (start_ang + len_ang); angle = angle + sec_ang) {
            double x = radius * Math.sin(Math.toRadians(angle)) + x0;
            double y = radius * Math.cos(Math.toRadians(angle)) + y0;
            next_p = new Point2D(x, -y);
            next_p_surf = mapView.screenToLocation(next_p);
            rect_temp.addPoint(next_p_surf);
        }
        next_p = new Point2D(x0, -y0);
        next_p_surf = mapView.screenToLocation(next_p);
        rect_temp.addPoint(next_p_surf);
        nowGraphic.setGeometry(rect_temp.toGeometry());
    }

    // 繪文字
    private void handleAddText(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("文字繪製參數", "/fxml/textFX.fxml", 300, 250);
        TextFXController textControllerHandle = (TextFXController) paramLoader.getController();
        textControllerHandle.loadParameter();

        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        prevClickFun = mapView.getOnMouseClicked();
        prevGraphic = null;
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    if (prevGraphic != null)
                        sketchlayer.getGraphics().remove(prevGraphic);
                    nowGraphic = new Graphic(relativeSurfacePoint);
                    textControllerHandle.saveParameter();
                    TextSymbol textSymbol = new TextSymbol(textControllerHandle.getTextSize(),
                            textControllerHandle.getTextContent(),
                            ColorUtil.colorToArgb(textControllerHandle.getTextColor()),
                            TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.BOTTOM);
                    // 變更字型
                    textSymbol.setFontFamily(textControllerHandle.getTextFontFamily());

                    nowGraphic.setSymbol(textSymbol);
                    sketchlayer.getGraphics().add(nowGraphic);
                    prevGraphic = nowGraphic;
                }
            }
        });
    }

    // 繪追蹤線
    private void handleAddTraceLine(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("追蹤線繪製參數", "/fxml/polylineFX.fxml", 300, 250);
        PolylineFXController polylineControllerHandle = (PolylineFXController) paramLoader.getController();
        polylineControllerHandle.loadParameter();

        PolylineBuilder polylineBuilder = new PolylineBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        nowGraphic = new Graphic();
        sketchlayer.getGraphics().add(nowGraphic);
        nowMoveGraphic = new Graphic();
        nowMoveGraphic.setSymbol(DefaultTraceMoveSymbol);
        sketchlayer.getGraphics().add(nowMoveGraphic);

        traceFeatureLine = null;
        traceFeaturePolygon = null;
        traceFirstIndex = -1;
        traceLastIndex = -1;
        traceDirection = 0;

        clickPointCount = 0;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    // 無待追蹤線段先 select
                    if (traceFeatureLine == null && traceFeaturePolygon == null) {
                        autoChoiceTraceFeature(screenPoint, polylineBuilder, sketchlayer);
                    } else {
                        Point near_p = getTraceNearPosition(relativeSurfacePoint);
                        // 從 first last 決定往前或往後找
                        boolean find_bo = false;
                        if (traceFeatureType == GeometryType.POLYLINE) {
                            if (traceLastIndex >= traceFirstIndex && traceNowIndex > traceLastIndex) {
                                find_bo = true;
                                // 從 lastindex 到 nowindex 各點加入
                                for (int i = traceLastIndex + 1; i <= traceNowIndex; i++) {
                                    Point np = new Point(traceFeatureLine.getParts().get(0).getPoint(i).getX(),
                                            traceFeatureLine.getParts().get(0).getPoint(i).getY());
                                    polylineBuilder.addPoint(np);
                                }
                                traceLastIndex = traceNowIndex;
                                polylineBuilder.addPoint(near_p);
                            } else if (traceLastIndex <= traceFirstIndex && traceNowIndex < traceLastIndex) {
                                find_bo = true;
                                // 從 lastindex 到 nowindex 各點加入
                                for (int i = traceLastIndex - 1; i >= traceNowIndex; i--) {
                                    Point np = new Point(traceFeatureLine.getParts().get(0).getPoint(i).getX(),
                                            traceFeatureLine.getParts().get(0).getPoint(i).getY());
                                    polylineBuilder.addPoint(np);
                                }
                                traceLastIndex = traceNowIndex;
                                polylineBuilder.addPoint(near_p);
                            }
                        } else {
                            // Polygon 方式要考慮跨 0 pointcount 首尾狀況(順向+1考慮跨pointcount，逆向-1考慮跨0)
                            // 處理順向，正常不跨越 now>last>first
                            if (traceDirection == 1
                                    || (traceNowIndex > traceLastIndex && traceLastIndex >= traceFirstIndex)) {
                                find_bo = true;
                                // 從 lastindex 到 nowindex 各點加入
                                int i = traceLastIndex + 1;
                                for (; i != traceNowIndex && i != traceFirstIndex; i++) {
                                    if (i > (traceFeaturePolygon.getParts().get(0).getPointCount() - 1))
                                        i = 0;
                                    Point np = new Point(traceFeaturePolygon.getParts().get(0).getPoint(i).getX(),
                                            traceFeaturePolygon.getParts().get(0).getPoint(i).getY());
                                    polylineBuilder.addPoint(np);
                                }
                                if (i == traceFirstIndex)
                                    find_bo = false;
                                else {
                                    traceLastIndex = traceNowIndex;
                                    polylineBuilder.addPoint(near_p);
                                }
                                traceDirection = 1;
                            }
                            // 處理逆向，正常不跨越 first>last>now
                            else if (traceDirection == -1
                                    || (traceFirstIndex >= traceLastIndex && traceLastIndex > traceNowIndex)) {
                                find_bo = true;
                                // 從 lastindex 到 nowindex 各點加入
                                int i = traceLastIndex - 1;
                                for (; i != traceNowIndex && i != traceFirstIndex; i--) {
                                    if (i < 0)
                                        i = traceFeaturePolygon.getParts().get(0).getPointCount() - 1;
                                    Point np = new Point(traceFeaturePolygon.getParts().get(0).getPoint(i).getX(),
                                            traceFeaturePolygon.getParts().get(0).getPoint(i).getY());
                                    polylineBuilder.addPoint(np);
                                }
                                if (i == traceFirstIndex)
                                    find_bo = false;
                                else {
                                    traceLastIndex = traceNowIndex;
                                    polylineBuilder.addPoint(near_p);
                                }
                                traceDirection = -1;
                            }
                        }
                        if (find_bo) {
                            clickPointCount++;
                            if (clickPointCount > 1) {
                                polylineControllerHandle.saveParameter();
                                SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                                if (polylineControllerHandle.getPolylineStyle() == "虛線")
                                    polylineStyle = SimpleLineSymbol.Style.DOT;
                                SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(polylineStyle,
                                        ColorUtil.colorToArgb(polylineControllerHandle.getPolylineColor()),
                                        polylineControllerHandle.getPolylineSize());
                                nowGraphic.setSymbol(polylineSymbol);
                                nowGraphic.setGeometry(polylineBuilder.toGeometry());
                            }
                        }
                    }
                }
            }
        });
        prevMoveFun = mapView.getOnMouseMoved();
        mapView.setOnMouseMoved(event -> {
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
            if (relativeSurfacePoint != null) {
                if (clickPointCount > 0) {
                    Part part = polylineBuilder.getParts().get(0);
                    Point last_p = part.getEndPoint();
                    PolylineBuilder line_temp = new PolylineBuilder(mapView.getSpatialReference());
                    line_temp.addPoint(last_p);
                    Point near_p = getTraceNearPosition(relativeSurfacePoint);
                    boolean find_bo = false;
                    if (traceFeatureType == GeometryType.POLYLINE) {
                        if (traceLastIndex >= traceFirstIndex && traceNowIndex > traceLastIndex) {
                            find_bo = true;
                            // 從 lastindex 到 nowindex 各點加入
                            for (int i = traceLastIndex + 1; i <= traceNowIndex; i++) {
                                Point np = new Point(traceFeatureLine.getParts().get(0).getPoint(i).getX(),
                                        traceFeatureLine.getParts().get(0).getPoint(i).getY());
                                line_temp.addPoint(np);
                            }
                        } else if (traceLastIndex <= traceFirstIndex && traceNowIndex < traceLastIndex) {
                            find_bo = true;
                            // 從 lastindex 到 nowindex 各點加入
                            for (int i = traceLastIndex - 1; i >= traceNowIndex; i--) {
                                Point np = new Point(traceFeatureLine.getParts().get(0).getPoint(i).getX(),
                                        traceFeatureLine.getParts().get(0).getPoint(i).getY());
                                line_temp.addPoint(np);
                            }
                        }
                    } else {
                        // Polygon 方式要考慮跨 0 pointcount 首尾狀況(順向+1考慮跨pointcount，逆向-1考慮跨0)
                        // 處理順向，正常不跨越 now>last>first
                        if (traceDirection == 1
                                || (traceNowIndex > traceLastIndex && traceLastIndex >= traceFirstIndex)) {
                            find_bo = true;
                            // 從 lastindex 到 nowindex 各點加入
                            int i = traceLastIndex + 1;
                            for (; i != traceNowIndex && i != traceFirstIndex; i++) {
                                if (i > (traceFeaturePolygon.getParts().get(0).getPointCount() - 1))
                                    i = 0;
                                Point np = new Point(traceFeaturePolygon.getParts().get(0).getPoint(i).getX(),
                                        traceFeaturePolygon.getParts().get(0).getPoint(i).getY());
                                line_temp.addPoint(np);
                            }
                            if (i == traceFirstIndex)
                                find_bo = false;
                        }
                        // 處理逆向，正常不跨越 first>last>now
                        else if (traceDirection == -1
                                || (traceFirstIndex >= traceLastIndex && traceLastIndex > traceNowIndex)) {
                            // 從 lastindex 到 nowindex 各點加入
                            find_bo = true;
                            int i = traceLastIndex - 1;
                            for (; i != traceNowIndex && i != traceFirstIndex; i--) {
                                if (i < 0)
                                    i = traceFeaturePolygon.getParts().get(0).getPointCount() - 1;
                                Point np = new Point(traceFeaturePolygon.getParts().get(0).getPoint(i).getX(),
                                        traceFeaturePolygon.getParts().get(0).getPoint(i).getY());
                                line_temp.addPoint(np);
                            }
                            if (i == traceFirstIndex)
                                find_bo = false;
                        }

                    }
                    if (find_bo) {
                        line_temp.addPoint(near_p);
                        nowMoveGraphic.setGeometry(line_temp.toGeometry());
                    }
                }
            }
        });
    }

    private void autoChoiceTraceFeature(Point2D screenPoint, PolylineBuilder polylineBuilder,
            GraphicsOverlay sketchlayer) {
        Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
        // 依序搜尋各圖層找出點下處10公尺內是否有
        arcgisMap.getOperationalLayers().forEach(testLayer -> {
            if (testLayer.getClass().toString().contains("Feature")) {
                final ListenableFuture<IdentifyLayerResult> results = mapView.identifyLayerAsync(testLayer, screenPoint,
                        10, false, 1);
                results.addDoneListener(() -> {
                    try {
                        IdentifyLayerResult layer = results.get();
                        if (layer.getElements().size() > 0) {
                            traceFeatureLayer = (FeatureLayer) testLayer;

                            // search the layers for identified features
                            List<Feature> traceFeatures = layer.getElements().stream()
                                    .filter(geoElement -> geoElement instanceof Feature).map(g -> (Feature) g)
                                    .collect(Collectors.toList());
                            boolean bo = true;
                            if (traceFeatures.get(0).getGeometry().getGeometryType() == GeometryType.POLYLINE) {
                                traceFeatureType = GeometryType.POLYLINE;
                                // select features
                                traceFeatureLayer.selectFeatures(traceFeatures);
                                // 保留此線後續追蹤
                                SpatialReference reference_102100 = SpatialReference.create(102100);
                                Polyline thisLine = (Polyline) traceFeatures.get(0).getGeometry();
                                traceFeatureLine = (Polyline) GeometryEngine.project(thisLine, reference_102100);
                            } else if (traceFeatures.get(0).getGeometry().getGeometryType() == GeometryType.POLYGON) {
                                // Polygon 點到範圍內也符合 100 公尺，必須改以邊線判斷
                                SpatialReference reference_102100 = SpatialReference.create(102100);
                                Point surfacePoint = (Point) GeometryEngine.project(relativeSurfacePoint,
                                        reference_102100);
                                if (pt2polygonDistance(surfacePoint,
                                        (Polygon) traceFeatures.get(0).getGeometry()) <= 100) {
                                    traceFeatureType = GeometryType.POLYGON;
                                    // select features
                                    traceFeatureLayer.selectFeatures(traceFeatures);
                                    // 保留此線後續追蹤
                                    Polygon thisPolygon = (Polygon) traceFeatures.get(0).getGeometry();
                                    traceFeaturePolygon = (Polygon) GeometryEngine.project(thisPolygon,
                                            reference_102100);
                                } else
                                    bo = false;
                            } else {
                                bo = false;
                            }

                            if (bo) {
                                // 找此最近點一併定為首點
                                Point near_p = getTraceNearPosition(relativeSurfacePoint);
                                traceFirstIndex = traceNowIndex;
                                traceLastIndex = traceNowIndex;
                                polylineBuilder.addPoint(near_p);
                                clickPointCount++;

                                firstPosGraphic = new Graphic(near_p);
                                firstPosGraphic.setSymbol(PosMarkerSymbol);
                                sketchlayer.getGraphics().add(firstPosGraphic);

                                // 找到即可退出
                                return;
                            }
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                });
            }
        });
    }

    private Point pt2LineNearPoint(double x, double y, double x1, double y1, double x2, double y2) {
        double A = x - x1;
        double B = y - y1;
        double C = x2 - x1;
        double D = y2 - y1;
        double dot = A * C + B * D;
        double len = C * C + D * D;
        double param = dot / len;

        double xx = x1 + param * C;
        double yy = y1 + param * D;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        }
        double dx = x - xx;
        double dy = y - yy;

        return (new Point(xx, yy));
    }

    private double pt2polygonDistance(Point point, Polygon polygon) {
        Point near_p = pt2LineNearPoint(point.getX(), point.getY(), polygon.getParts().get(0).getPoint(0).getX(),
                polygon.getParts().get(0).getPoint(0).getY(), polygon.getParts().get(0).getPoint(1).getX(),
                polygon.getParts().get(0).getPoint(1).getY());
        double min_distance = Math.sqrt((point.getX() - near_p.getX()) * (point.getX() - near_p.getX())
                + (point.getY() - near_p.getY()) * (point.getY() - near_p.getY()));
        for (int i = 1; i < polygon.getParts().get(0).getPointCount() - 1; i++) {
            Point now_p = pt2LineNearPoint(point.getX(), point.getY(), polygon.getParts().get(0).getPoint(i).getX(),
                    polygon.getParts().get(0).getPoint(i).getY(), polygon.getParts().get(0).getPoint(i + 1).getX(),
                    polygon.getParts().get(0).getPoint(i + 1).getY());
            double now_distance = Math.sqrt((point.getX() - now_p.getX()) * (point.getX() - now_p.getX())
                    + (point.getY() - now_p.getY()) * (point.getY() - now_p.getY()));
            if (now_distance < min_distance) {
                min_distance = now_distance;
                near_p = now_p;
            }
        }

        return (min_distance);
    }

    private Point getTraceNearPosition(Point fromPoint) {
        Point near_p = null;

        if (traceFeatureType == GeometryType.POLYLINE) {
            near_p = pt2LineNearPoint(fromPoint.getX(), fromPoint.getY(),
                    traceFeatureLine.getParts().get(0).getPoint(0).getX(),
                    traceFeatureLine.getParts().get(0).getPoint(0).getY(),
                    traceFeatureLine.getParts().get(0).getPoint(1).getX(),
                    traceFeatureLine.getParts().get(0).getPoint(1).getY());
            double min_distance = Math.sqrt((fromPoint.getX() - near_p.getX()) * (fromPoint.getX() - near_p.getX())
                    + (fromPoint.getY() - near_p.getY()) * (fromPoint.getY() - near_p.getY()));
            traceNowIndex = 0;
            for (int i = 1; i < traceFeatureLine.getParts().get(0).getPointCount() - 1; i++) {
                Point now_p = pt2LineNearPoint(fromPoint.getX(), fromPoint.getY(),
                        traceFeatureLine.getParts().get(0).getPoint(i).getX(),
                        traceFeatureLine.getParts().get(0).getPoint(i).getY(),
                        traceFeatureLine.getParts().get(0).getPoint(i + 1).getX(),
                        traceFeatureLine.getParts().get(0).getPoint(i + 1).getY());
                double now_distance = Math.sqrt((fromPoint.getX() - now_p.getX()) * (fromPoint.getX() - now_p.getX())
                        + (fromPoint.getY() - now_p.getY()) * (fromPoint.getY() - now_p.getY()));
                if (now_distance < min_distance) {
                    min_distance = now_distance;
                    near_p = now_p;
                    traceNowIndex = i;
                }
            }
        } else {
            near_p = pt2LineNearPoint(fromPoint.getX(), fromPoint.getY(),
                    traceFeaturePolygon.getParts().get(0).getPoint(0).getX(),
                    traceFeaturePolygon.getParts().get(0).getPoint(0).getY(),
                    traceFeaturePolygon.getParts().get(0).getPoint(1).getX(),
                    traceFeaturePolygon.getParts().get(0).getPoint(1).getY());
            double min_distance = Math.sqrt((fromPoint.getX() - near_p.getX()) * (fromPoint.getX() - near_p.getX())
                    + (fromPoint.getY() - near_p.getY()) * (fromPoint.getY() - near_p.getY()));
            traceNowIndex = 0;
            for (int i = 1; i < traceFeaturePolygon.getParts().get(0).getPointCount() - 1; i++) {
                Point now_p = pt2LineNearPoint(fromPoint.getX(), fromPoint.getY(),
                        traceFeaturePolygon.getParts().get(0).getPoint(i).getX(),
                        traceFeaturePolygon.getParts().get(0).getPoint(i).getY(),
                        traceFeaturePolygon.getParts().get(0).getPoint(i + 1).getX(),
                        traceFeaturePolygon.getParts().get(0).getPoint(i + 1).getY());
                double now_distance = Math.sqrt((fromPoint.getX() - now_p.getX()) * (fromPoint.getX() - now_p.getX())
                        + (fromPoint.getY() - now_p.getY()) * (fromPoint.getY() - now_p.getY()));
                if (now_distance < min_distance) {
                    min_distance = now_distance;
                    near_p = now_p;
                    traceNowIndex = i;
                }
            }
        }
        return (near_p);
    }

    private Point getCrossPoint(Point a, Point b, Point c, Point d) {
        double area_abc = (a.getX() - c.getX()) * (b.getY() - c.getY()) - (a.getY() - c.getY()) * (b.getX() - c.getX());
        double area_abd = (a.getX() - d.getX()) * (b.getY() - d.getY()) - (a.getY() - d.getY()) * (b.getX() - d.getX());
        if (area_abc * area_abd >= 0) {
            return null;
        }

        double area_cda = (c.getX() - a.getX()) * (d.getY() - a.getY()) - (c.getY() - a.getY()) * (d.getX() - a.getX());
        double area_cdb = area_cda + area_abc - area_abd;
        if (area_cda * area_cdb >= 0) {
            return null;
        }

        double t = area_cda / (area_abd - area_abc);
        double dx = t * (b.getX() - a.getX());
        double dy = t * (b.getY() - a.getY());
        return (new Point(a.getX() + dx, a.getY() + dy));
    }

    // 繪截切線
    private void handleAddClipLine(ActionEvent e) {
        MapManager.setOverlayEditStatus();
        ;
        this.btnCompleteBase.setDisable(false);
        this.btnCancelBase.setDisable(false);
        setDisableOverlayRibbonGroup(true, false, true, true);
        cleanOperate();

        openParamPanel("截切線繪製參數", "/fxml/polylineFX.fxml", 300, 250);
        PolylineFXController polylineControllerHandle = (PolylineFXController) paramLoader.getController();
        polylineControllerHandle.loadParameter();

        PolylineBuilder polylineBuilder = new PolylineBuilder(mapView.getSpatialReference());
        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;

        clipGraphicArray.clear();

        nowMoveGraphic = new Graphic();
        nowMoveGraphic.setSymbol(clipRectSymbol);
        sketchlayer.getGraphics().add(nowMoveGraphic);

        firstPosPoint3D = null;
        secondPosPoint3D = null;
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                Point2D screenPoint = new Point2D(event.getX(), event.getY());
                Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
                if (relativeSurfacePoint != null) {
                    if (firstPosPoint3D == null) {
                        firstPosPoint3D = relativeSurfacePoint;
                    } else {
                        secondPosPoint3D = relativeSurfacePoint;
                        final Point firstPos = firstPosPoint3D;
                        final Point secondPos = secondPosPoint3D;
                        // 點下第二點即可開始截切
                        // 換用邏輯 -> 對 polyline/polygon 每一點判斷是否落在框取起訖範圍內，是則從開始到結束的
                        // 連續點(必須連續落在範圍內)，將這些點組成紅線輸出
                        arcgisMap.getOperationalLayers().forEach(testLayer -> {
                            if (testLayer.getClass().toString().contains("Feature")) {
                                polylineControllerHandle.saveParameter();

                                FeatureLayer testFeatureLayer = (FeatureLayer) testLayer;
                                QueryParameters query = new QueryParameters();
                                query.setWhereClause("1=1");
                                ListenableFuture<FeatureQueryResult> tableQueryResult = testFeatureLayer
                                        .getFeatureTable().queryFeaturesAsync(query);
                                tableQueryResult.addDoneListener(() -> {
                                    try {
                                        FeatureQueryResult result = tableQueryResult.get();
                                        // if (result.iterator().hasNext()) {
                                        // Feature feature = result.iterator().next();
                                        for (Iterator<Feature> it = result.iterator(); it.hasNext();) {
                                            Feature feature = it.next();
                                            // 是 polyline/polygon 才處理
                                            if (feature.getGeometry().getGeometryType() == GeometryType.POLYLINE) {
                                                Geometry thisGeometry = feature.getGeometry();
                                                SpatialReference reference_102100 = SpatialReference.create(102100);
                                                Polyline thisLines = (Polyline) GeometryEngine.project(thisGeometry,
                                                        reference_102100);
                                                // 對各線段處理
                                                for (int i = 0; i < thisLines.getParts().size(); i++) {
                                                    int start_index = -1;
                                                    int end_index = -1;
                                                    Point prev_pp = null;
                                                    PolylineBuilder line_temp = new PolylineBuilder(
                                                            mapView.getSpatialReference());
                                                    for (int j = 0; j < thisLines.getParts().get(i)
                                                            .getPointCount(); j++) {
                                                        // 此點落在 first second 之間即是
                                                        Point pp = thisLines.getParts().get(i).getPoint(j);
                                                        if (pp.getX() >= Math.min(firstPos.getX(), secondPos.getX())
                                                                && pp.getX() <= Math.max(firstPos.getX(),
                                                                        secondPos.getX())
                                                                && pp.getY() >= Math.min(firstPos.getY(),
                                                                        secondPos.getY())
                                                                && pp.getY() <= Math.max(firstPos.getY(),
                                                                        secondPos.getY())) {
                                                            if (start_index == -1) {
                                                                start_index = j;
                                                                // 從前一點沒落在範圍內，此點有，則要找此點與前一
                                                                // 點是否有與此框的交點，有則將交點 addPoint
                                                                if (prev_pp != null) {
                                                                    Point cross_pp = null;
                                                                    if ((cross_pp = getCrossPoint(prev_pp, pp, firstPos,
                                                                            new Point(secondPos.getX(),
                                                                                    firstPos.getY()))) != null) {
                                                                        line_temp.addPoint(cross_pp);
                                                                    } else if ((cross_pp = getCrossPoint(prev_pp, pp,
                                                                            firstPos, new Point(firstPos.getX(),
                                                                                    secondPos.getY()))) != null) {
                                                                        line_temp.addPoint(cross_pp);
                                                                    } else if ((cross_pp = getCrossPoint(prev_pp, pp,
                                                                            secondPos, new Point(secondPos.getX(),
                                                                                    firstPos.getY()))) != null) {
                                                                        line_temp.addPoint(cross_pp);
                                                                    } else if ((cross_pp = getCrossPoint(prev_pp, pp,
                                                                            secondPos, new Point(firstPos.getX(),
                                                                                    secondPos.getY()))) != null) {
                                                                        line_temp.addPoint(cross_pp);
                                                                    }
                                                                }
                                                            } else {
                                                                // 前一點與此點都落在範圍內則不處理
                                                                end_index = j;
                                                            }
                                                            line_temp.addPoint(pp);
                                                        }
                                                        // 否則(此點未落在範圍內)，此要判斷與前一點是否與框有交點，
                                                        // 有則將交點加到 line_temp
                                                        else {
                                                            // 此時若有交點(應有兩點)保留到 temp
                                                            if (prev_pp != null) {
                                                                Point cross_pp = null;
                                                                if ((cross_pp = getCrossPoint(prev_pp, pp, firstPos,
                                                                        new Point(secondPos.getX(),
                                                                                firstPos.getY()))) != null) {
                                                                    line_temp.addPoint(cross_pp);
                                                                    end_index = j;
                                                                }
                                                                if ((cross_pp = getCrossPoint(prev_pp, pp, firstPos,
                                                                        new Point(firstPos.getX(),
                                                                                secondPos.getY()))) != null) {
                                                                    line_temp.addPoint(cross_pp);
                                                                    end_index = j;
                                                                }
                                                                if ((cross_pp = getCrossPoint(prev_pp, pp, secondPos,
                                                                        new Point(secondPos.getX(),
                                                                                firstPos.getY()))) != null) {
                                                                    line_temp.addPoint(cross_pp);
                                                                    end_index = j;
                                                                }
                                                                if ((cross_pp = getCrossPoint(prev_pp, pp, secondPos,
                                                                        new Point(firstPos.getX(),
                                                                                secondPos.getY()))) != null) {
                                                                    line_temp.addPoint(cross_pp);
                                                                    end_index = j;
                                                                }
                                                            }

                                                            // 之前 line_temp 已有資料則存入
                                                            if (end_index != -1) {
                                                                SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                                                                if (polylineControllerHandle.getPolylineStyle() == "虛線")
                                                                    polylineStyle = SimpleLineSymbol.Style.DOT;
                                                                SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(
                                                                        polylineStyle,
                                                                        ColorUtil.colorToArgb(polylineControllerHandle
                                                                                .getPolylineColor()),
                                                                        polylineControllerHandle.getPolylineSize());
                                                                Graphic tempGraphic = new Graphic();
                                                                tempGraphic.setSymbol(polylineSymbol);
                                                                tempGraphic.setGeometry(line_temp.toGeometry());
                                                                clipGraphicArray.add(tempGraphic);
                                                                sketchlayer.getGraphics().add(tempGraphic);
                                                                start_index = -1;
                                                                end_index = -1;
                                                                line_temp = new PolylineBuilder(
                                                                        mapView.getSpatialReference());
                                                            }
                                                        }
                                                        prev_pp = pp;
                                                    }
                                                    // 線的尾端落在框內則有尾線
                                                    if (end_index != -1) {
                                                        SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                                                        if (polylineControllerHandle.getPolylineStyle() == "虛線")
                                                            polylineStyle = SimpleLineSymbol.Style.DOT;
                                                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(
                                                                polylineStyle,
                                                                ColorUtil.colorToArgb(
                                                                        polylineControllerHandle.getPolylineColor()),
                                                                polylineControllerHandle.getPolylineSize());
                                                        Graphic tempGraphic = new Graphic();
                                                        tempGraphic.setSymbol(polylineSymbol);
                                                        tempGraphic.setGeometry(line_temp.toGeometry());
                                                        clipGraphicArray.add(tempGraphic);
                                                        sketchlayer.getGraphics().add(tempGraphic);
                                                    }
                                                }
                                            } else if (feature.getGeometry()
                                                    .getGeometryType() == GeometryType.POLYGON) {
                                                Geometry thisGeometry = feature.getGeometry();
                                                SpatialReference reference_102100 = SpatialReference.create(102100);
                                                Polygon thisLines = (Polygon) GeometryEngine.project(thisGeometry,
                                                        reference_102100);
                                                // 對各線段處理
                                                for (int i = 0; i < thisLines.getParts().size(); i++) {
                                                    int start_index = -1;
                                                    int end_index = -1;
                                                    Point prev_pp = null;
                                                    PolylineBuilder line_temp = new PolylineBuilder(
                                                            mapView.getSpatialReference());
                                                    for (int j = 0; j < thisLines.getParts().get(i)
                                                            .getPointCount(); j++) {
                                                        // 此點落在 first second 之間即是
                                                        Point pp = thisLines.getParts().get(i).getPoint(j);
                                                        if (pp.getX() >= Math.min(firstPos.getX(), secondPos.getX())
                                                                && pp.getX() <= Math.max(firstPos.getX(),
                                                                        secondPos.getX())
                                                                && pp.getY() >= Math.min(firstPos.getY(),
                                                                        secondPos.getY())
                                                                && pp.getY() <= Math.max(firstPos.getY(),
                                                                        secondPos.getY())) {
                                                            if (start_index == -1) {
                                                                start_index = j;
                                                                if (prev_pp != null) {
                                                                    Point cross_pp = null;
                                                                    if ((cross_pp = getCrossPoint(prev_pp, pp, firstPos,
                                                                            new Point(secondPos.getX(),
                                                                                    firstPos.getY()))) != null) {
                                                                        line_temp.addPoint(cross_pp);
                                                                    } else if ((cross_pp = getCrossPoint(prev_pp, pp,
                                                                            firstPos, new Point(firstPos.getX(),
                                                                                    secondPos.getY()))) != null) {
                                                                        line_temp.addPoint(cross_pp);
                                                                    } else if ((cross_pp = getCrossPoint(prev_pp, pp,
                                                                            secondPos, new Point(secondPos.getX(),
                                                                                    firstPos.getY()))) != null) {
                                                                        line_temp.addPoint(cross_pp);
                                                                    } else if ((cross_pp = getCrossPoint(prev_pp, pp,
                                                                            secondPos, new Point(firstPos.getX(),
                                                                                    secondPos.getY()))) != null) {
                                                                        line_temp.addPoint(cross_pp);
                                                                    }
                                                                }
                                                            } else {
                                                                end_index = j;
                                                            }
                                                            line_temp.addPoint(pp);
                                                        } else {
                                                            if (prev_pp != null) {
                                                                Point cross_pp = null;
                                                                if ((cross_pp = getCrossPoint(prev_pp, pp, firstPos,
                                                                        new Point(secondPos.getX(),
                                                                                firstPos.getY()))) != null) {
                                                                    line_temp.addPoint(cross_pp);
                                                                    end_index = j;
                                                                }
                                                                if ((cross_pp = getCrossPoint(prev_pp, pp, firstPos,
                                                                        new Point(firstPos.getX(),
                                                                                secondPos.getY()))) != null) {
                                                                    line_temp.addPoint(cross_pp);
                                                                    end_index = j;
                                                                }
                                                                if ((cross_pp = getCrossPoint(prev_pp, pp, secondPos,
                                                                        new Point(secondPos.getX(),
                                                                                firstPos.getY()))) != null) {
                                                                    line_temp.addPoint(cross_pp);
                                                                    end_index = j;
                                                                }
                                                                if ((cross_pp = getCrossPoint(prev_pp, pp, secondPos,
                                                                        new Point(firstPos.getX(),
                                                                                secondPos.getY()))) != null) {
                                                                    line_temp.addPoint(cross_pp);
                                                                    end_index = j;
                                                                }
                                                            }

                                                            if (end_index != -1) {
                                                                SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                                                                if (polylineControllerHandle.getPolylineStyle() == "虛線")
                                                                    polylineStyle = SimpleLineSymbol.Style.DOT;
                                                                SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(
                                                                        polylineStyle,
                                                                        ColorUtil.colorToArgb(polylineControllerHandle
                                                                                .getPolylineColor()),
                                                                        polylineControllerHandle.getPolylineSize());
                                                                Graphic tempGraphic = new Graphic();
                                                                tempGraphic.setSymbol(polylineSymbol);
                                                                tempGraphic.setGeometry(line_temp.toGeometry());
                                                                clipGraphicArray.add(tempGraphic);
                                                                sketchlayer.getGraphics().add(tempGraphic);
                                                                start_index = -1;
                                                                end_index = -1;
                                                                line_temp = new PolylineBuilder(
                                                                        mapView.getSpatialReference());
                                                            }
                                                        }
                                                        prev_pp = pp;
                                                    }
                                                    if (end_index != -1) {
                                                        SimpleLineSymbol.Style polylineStyle = SimpleLineSymbol.Style.SOLID;
                                                        if (polylineControllerHandle.getPolylineStyle() == "虛線")
                                                            polylineStyle = SimpleLineSymbol.Style.DOT;
                                                        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(
                                                                polylineStyle,
                                                                ColorUtil.colorToArgb(
                                                                        polylineControllerHandle.getPolylineColor()),
                                                                polylineControllerHandle.getPolylineSize());
                                                        Graphic tempGraphic = new Graphic();
                                                        tempGraphic.setSymbol(polylineSymbol);
                                                        tempGraphic.setGeometry(line_temp.toGeometry());
                                                        clipGraphicArray.add(tempGraphic);
                                                        sketchlayer.getGraphics().add(tempGraphic);
                                                    }
                                                }
                                            }
                                        }

                                    } catch (Exception ee) {
                                        // on any error, display the stack trace
                                        //ee.printStackTrace();
                                    }
                                });
                            }
                        });
                        firstPosPoint3D = null;
                        secondPosPoint3D = null;
                        nowMoveGraphic.setGeometry(null);
                    }
                }
            }
        });
        prevMoveFun = mapView.getOnMouseMoved();
        mapView.setOnMouseMoved(event -> {
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            Point relativeSurfacePoint = mapView.screenToLocation(screenPoint);
            if (relativeSurfacePoint != null) {
                if (firstPosPoint3D != null) {
                    secondPosPoint3D = relativeSurfacePoint;
                    PolygonBuilder rectBuilder = new PolygonBuilder(mapView.getSpatialReference());
                    rectBuilder.addPoint(firstPosPoint3D);
                    rectBuilder.addPoint(new Point(firstPosPoint3D.getX(), secondPosPoint3D.getY()));
                    rectBuilder.addPoint(secondPosPoint3D);
                    rectBuilder.addPoint(new Point(secondPosPoint3D.getX(), firstPosPoint3D.getY()));
                    rectBuilder.addPoint(firstPosPoint3D);
                    nowMoveGraphic.setGeometry(rectBuilder.toGeometry());
                }
            }
        });
    }

    // 透明圖修改
    private void handleEditBase(ActionEvent e) {
        Graphic graphicSelected = groupManager.getSelectedOneGraphic();
        // 不是基本圖不處理
        if (graphicSelected == null || graphicSelected.getSymbol() == null)
            return;

        List<Graphic> groupGraphicsSelected = groupManager.getSelectedGroupGraphics();
        // 有 select 到才處理
        if (groupGraphicsSelected.size() > 1 || null != graphicSelected) {
            cleanOperate();
            if (graphicSelected != null)
                baseEditGraphic = graphicSelected;
            else
                baseEditGraphic = groupGraphicsSelected.get(0);

            MapManager.isAddEditOverlay = true;
            prevClickFun = mapView.getOnMouseClicked();
            prevMoveFun = mapView.getOnMouseMoved();
            prevDragedFun = mapView.getOnMouseDragged();
            // 依 graphic 種類開啟參數視窗
            // System.out.println(baseEditGraphic.getSymbol().toJson());
            JSONObject obj, obj1 = null;
            JSONArray arr = null;
            switch (baseEditGraphic.getGeometry().getGeometryType()) {
            case POLYGON:
                openParamPanel("[面]圖徵異動參數", "/fxml/polygonFX.fxml", 300, 280);
                baseEditPolygonController = (PolygonFXController) paramLoader.getController();
                baseEditPolygonController.loadParameter();
                obj = (JSONObject) JSONValue.parse(baseEditGraphic.getSymbol().toJson());
                arr = (JSONArray) obj.get("color");
                baseEditPolygonController.setPolygonColor(
                        String.format("#%02x%02x%02x%02x", arr.get(0), arr.get(1), arr.get(2), arr.get(3)));
                baseEditPolygonController.setPolygonStyle(obj.get("style").toString());
                obj1 = (JSONObject) JSONValue.parse(obj.get("outline").toString());
                arr = (JSONArray) obj1.get("color");
                baseEditPolygonController.setPolygonLineColor(
                        String.format("#%02x%02x%02x%02x", arr.get(0), arr.get(1), arr.get(2), arr.get(3)));
                baseEditPolygonController.setPolygonLineSize(obj1.get("width").toString());
                baseEditPolygonController.setPolygonLineStyle(obj1.get("style").toString());
                break;
            case POLYLINE:
                openParamPanel("[線]圖徵異動參數", "/fxml/polylineFX.fxml", 300, 250);
                baseEditPolylineController = (PolylineFXController) paramLoader.getController();
                baseEditPolylineController.loadParameter();
                obj = (JSONObject) JSONValue.parse(baseEditGraphic.getSymbol().toJson());
                arr = (JSONArray) obj.get("color");
                baseEditPolylineController.setPolylineColor(
                        String.format("#%02x%02x%02x%02x", arr.get(0), arr.get(1), arr.get(2), arr.get(3)));
                baseEditPolylineController.setPolylineSize(obj.get("width").toString());
                baseEditPolylineController.setPolylineStyle(obj.get("style").toString());
                break;
            case MULTIPOINT:
                openParamPanel("[點]圖徵異動參數", "/fxml/pointFX.fxml", 300, 200);
                baseEditPointController = (PointFXController) paramLoader.getController();
                baseEditPointController.loadParameter();
                obj = (JSONObject) JSONValue.parse(baseEditGraphic.getSymbol().toJson());
                arr = (JSONArray) obj.get("color");
                baseEditPointController.setPointColor(
                        String.format("#%02x%02x%02x%02x", arr.get(0), arr.get(1), arr.get(2), arr.get(3)));
                baseEditPointController.setPointSize(obj.get("size").toString());
                break;
            case POINT:
                if (baseEditGraphic.getSymbol().toJson().contains("esriTS")) {
                    openParamPanel("文字繪製參數", "/fxml/textFX.fxml", 300, 250);
                    baseEditTextController = (TextFXController) paramLoader.getController();
                    baseEditTextController.loadParameter();
                    obj = (JSONObject) JSONValue.parse(baseEditGraphic.getSymbol().toJson());
                    arr = (JSONArray) obj.get("color");
                    baseEditTextController.setTextColor(
                            String.format("#%02x%02x%02x%02x", arr.get(0), arr.get(1), arr.get(2), arr.get(3)));
                    baseEditTextController.setTextContent(obj.get("text").toString());
                    obj1 = (JSONObject) JSONValue.parse(obj.get("font").toString());
                    baseEditTextController.setTextFontFamily(obj1.get("family").toString());
                    baseEditTextController.setTextSize(obj1.get("size").toString());
                } else {
                    openParamPanel("[點]圖徵異動參數", "/fxml/pointFX.fxml", 300, 200);
                    baseEditPointController = (PointFXController) paramLoader.getController();
                    baseEditPointController.loadParameter();
                    obj = (JSONObject) JSONValue.parse(baseEditGraphic.getSymbol().toJson());
                    arr = (JSONArray) obj.get("color");
                    baseEditPointController.setPointColor(
                            String.format("#%02x%02x%02x%02x", arr.get(0), arr.get(1), arr.get(2), arr.get(3)));
                    baseEditPointController.setPointSize(obj.get("size").toString());
                }
            }
            // SketchEdit.Start前須Keep KeyEventHandler
            prevKeyPressedFun = mapView.getOnKeyPressed();
            prevKeyReleasedFun = mapView.getOnKeyReleased();
            // 開啟 editor
            baseSketchEditor.start(baseEditGraphic.getGeometry());
            // 按鈕致能
            btnCopyPaste.setDisable(true);
            this.btnEditBase.setDisable(true);
            this.btnCompleteBase.setDisable(false);
            this.btnCancelBase.setDisable(false);
        }
    }

    /****************************
     * 透明圖繪製相關內容結尾
     **********************************************************/

    /****************************
     * 複製貼上相關內容開頭
     **********************************************************/

    private static CopyPasteFXController copypasteControllerHandle = null;
    private static JFrame copypasteFrame = null;
    private static ArrayList<Graphic> pasteGraphicList = new ArrayList<Graphic>();
    private static Graphic copyGraphicSelected = null;
    private static Graphic pasteGraphicSelected = null;

    private void handleCopyPaste(ActionEvent e) {
        // 取得選取的圖徵
        copyGraphicSelected = groupManager.getSelectedOneGraphic();

        if (copyGraphicSelected == null) { 
            AlertDialog.errorAlert("尚未點選複製對象，或點選的不是基本圖\n請重新選取", false);
            return;
        }

        MapManager.isAddEditOverlay = true;
        this.btnCopyPaste.setDisable(true);
        copypasteOpen();
        copypasteControllerHandle = (CopyPasteFXController) paramLoader.getController();
        copypasteControllerHandle.setCopyGraphicID(copyGraphicSelected);

        // 雙按滑鼠貼上操作邏輯
        this.btnCompleteBase.setDisable(true);
        this.btnCancelBase.setDisable(true);
        this.btnEditBase.setDisable(true);
        cleanOperate();

        GraphicsOverlay sketchlayer = overlayDrapedBaseMilitarySymbol;
        GraphicsOverlay sketchlayerFE = overlayDrapedMilitarySymbol;
        
        pasteGraphicList.clear();
        prevClickFun = mapView.getOnMouseClicked();
        mapView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                if (event.getClickCount() == 2) {
                    Point2D screenPoint = new Point2D(event.getX(), event.getY());
                    Point pastePosPoint = mapView.screenToLocation(screenPoint);
                    if (pastePosPoint != null) {
                        switch (copyGraphicSelected.getGeometry().getGeometryType()) {
                        case POLYGON:
                            if (copyGraphicSelected.getSymbol() == null) {
                                pasteGraphicSelected = newFECopyPastePolygon(copyGraphicSelected, pastePosPoint);
                                pasteGraphicList.add(pasteGraphicSelected);
                                sketchlayerFE.getGraphics().add(pasteGraphicSelected);
                                copypasteControllerHandle.addPasteItem("polygon", pasteGraphicSelected);                                
                            } else {
                                pasteGraphicSelected = newCopyPastePolygon(copyGraphicSelected, pastePosPoint);
                                pasteGraphicList.add(pasteGraphicSelected);
                                sketchlayer.getGraphics().add(pasteGraphicSelected);
                                copypasteControllerHandle.addPasteItem("polygon", pasteGraphicSelected);
                            }
                            break;
                        case POLYLINE:
                            if (copyGraphicSelected.getSymbol() == null) {
                                pasteGraphicSelected = newFECopyPastePolyline(copyGraphicSelected, pastePosPoint);
                                pasteGraphicList.add(pasteGraphicSelected);
                                sketchlayerFE.getGraphics().add(pasteGraphicSelected);
                                copypasteControllerHandle.addPasteItem("polyline", pasteGraphicSelected);
                            } else {
                                pasteGraphicSelected = newCopyPastePolyline(copyGraphicSelected, pastePosPoint);
                                pasteGraphicList.add(pasteGraphicSelected);
                                sketchlayer.getGraphics().add(pasteGraphicSelected);
                                copypasteControllerHandle.addPasteItem("polyline", pasteGraphicSelected);
                            }
                            break;
                        case MULTIPOINT:
                            pasteGraphicSelected = newCopyPasteMultipoint(copyGraphicSelected, pastePosPoint);
                            pasteGraphicList.add(pasteGraphicSelected);
                            sketchlayer.getGraphics().add(pasteGraphicSelected);
                            copypasteControllerHandle.addPasteItem("multipoint", pasteGraphicSelected);
                            break;
                        case POINT:
                            if (copyGraphicSelected.getSymbol() == null) {
                                pasteGraphicSelected = addFEGraphicIDSpatial4326(copyGraphicSelected, pastePosPoint);
                                pasteGraphicList.add(pasteGraphicSelected);
                                sketchlayerFE.getGraphics().add(pasteGraphicSelected);
                                copypasteControllerHandle.addPasteItem("point", pasteGraphicSelected);
                            }
                            else {
                                pasteGraphicSelected = new Graphic(pastePosPoint, copyGraphicSelected.getSymbol());
                                pasteGraphicSelected = addGraphicIDSpatial4326(pasteGraphicSelected);
                                pasteGraphicList.add(pasteGraphicSelected);
                                sketchlayer.getGraphics().add(pasteGraphicSelected);
                                copypasteControllerHandle.addPasteItem("point", pasteGraphicSelected);
                            }
                        }
                    }
                }
            }
        });

        // 結束
        copypasteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                copypasteClose();
            }
        });
    }

    private Graphic newCopyPastePolygon(Graphic copyGraphicSelected, Point pastePosPoint) {
        SpatialReference reference_4326 = SpatialReference.create(4326);
        Point sourcePoint = ((Polygon) copyGraphicSelected.getGeometry()).getExtent().getCenter();
        Point pastePoint = (Point) GeometryEngine.project(pastePosPoint, reference_4326);
        List<Point> Points = StreamSupport
                .stream(((Polygon) copyGraphicSelected.getGeometry()).getParts().getPartsAsPoints().spliterator(),
                        false)
                .collect(Collectors.toList());
        List<Point> destPoints = GeometryCopy(sourcePoint, pastePoint, Points);
        PointCollection pointCollection = new PointCollection(reference_4326);
        pointCollection.addAll(destPoints);
        Polygon polygon = new Polygon(pointCollection);
        Graphic pasteGraphic = new Graphic(polygon, copyGraphicSelected.getSymbol());
        pasteGraphic = addGraphicIDSpatial4326(pasteGraphic);
        return pasteGraphic;
    }

    private Graphic newFECopyPastePolygon(Graphic copyGraphicSelected, Point pastePosPoint) {
        SpatialReference reference_4326 = SpatialReference.create(4326);
        Point sourcePoint = ((Polygon) copyGraphicSelected.getGeometry()).getExtent().getCenter();
        Point pastePoint = (Point) GeometryEngine.project(pastePosPoint, reference_4326);
        List<Point> Points = StreamSupport
                .stream(((Polygon) copyGraphicSelected.getGeometry()).getParts().getPartsAsPoints().spliterator(),
                        false)
                .collect(Collectors.toList());
        List<Point> destPoints = GeometryCopy(sourcePoint, pastePoint, Points);
        PointCollection pointCollection = new PointCollection(reference_4326);
        pointCollection.addAll(destPoints);
        Polygon polygon = new Polygon(pointCollection);
        Graphic pasteGraphic = new Graphic(polygon);
        copyGraphicSelected.getAttributes().forEach((key, value) -> {
            if (key.equals("id"))
                pasteGraphic.getAttributes().put("id", java.util.UUID.randomUUID().toString());
            else
                pasteGraphic.getAttributes().put(key, value);
        });

        return pasteGraphic;
    }
    
    private Graphic newCopyPastePolyline(Graphic copyGraphicSelected, Point pastePosPoint) {
        SpatialReference reference_4326 = SpatialReference.create(4326);
        Point sourcePoint = ((Polyline) copyGraphicSelected.getGeometry()).getExtent().getCenter();
        Point pastePoint = (Point) GeometryEngine.project(pastePosPoint, reference_4326);
        List<Point> Points = StreamSupport
                .stream(((Polyline) copyGraphicSelected.getGeometry()).getParts().getPartsAsPoints().spliterator(),
                        false)
                .collect(Collectors.toList());
        List<Point> destPoints = GeometryCopy(sourcePoint, pastePoint, Points);
        PointCollection pointCollection = new PointCollection(reference_4326);
        pointCollection.addAll(destPoints);
        Polyline polyline = new Polyline(pointCollection);
        Graphic pasteGraphic = new Graphic(polyline, copyGraphicSelected.getSymbol());
        pasteGraphic = addGraphicIDSpatial4326(pasteGraphic);
        return pasteGraphic;
    }

    private Graphic newFECopyPastePolyline(Graphic copyGraphicSelected, Point pastePosPoint) {
        SpatialReference reference_4326 = SpatialReference.create(4326);
        Point sourcePoint = ((Polyline) copyGraphicSelected.getGeometry()).getExtent().getCenter();
        Point pastePoint = (Point) GeometryEngine.project(pastePosPoint, reference_4326);
        List<Point> Points = StreamSupport
                .stream(((Polyline) copyGraphicSelected.getGeometry()).getParts().getPartsAsPoints().spliterator(),
                        false)
                .collect(Collectors.toList());
        List<Point> destPoints = GeometryCopy(sourcePoint, pastePoint, Points);
        PointCollection pointCollection = new PointCollection(reference_4326);
        pointCollection.addAll(destPoints);
        Polyline polyline = new Polyline(pointCollection);
        Graphic pasteGraphic = new Graphic(polyline);       
        copyGraphicSelected.getAttributes().forEach((key, value) -> {
            if (key.equals("id"))
                pasteGraphic.getAttributes().put("id", java.util.UUID.randomUUID().toString());
            else
                pasteGraphic.getAttributes().put(key, value);
        });

        return pasteGraphic;
    }
    
    
    private Graphic newCopyPasteMultipoint(Graphic copyGraphicSelected, Point pastePosPoint) {
        SpatialReference reference_4326 = SpatialReference.create(4326);
        Point sourcePoint = copyGraphicSelected.getGeometry().getExtent().getCenter();
        Point pastePoint = (Point) GeometryEngine.project(pastePosPoint, reference_4326);
        List<Geometry> geometrys = Arrays.asList(copyGraphicSelected.getGeometry());
        List<Point> Points = new ArrayList<Point>();
        for (Geometry geometry : geometrys) {
            // Point point = new Point(geometry.toJson());
            // Points.add(point);
            // System.out.println(geometry.toJson());
            JSONObject obj = (JSONObject) JSONValue.parse(geometry.toJson());
            JSONArray arr = (JSONArray) obj.get("points");
            for (int ii = 0; ii < arr.size(); ii++) {
                JSONArray arr1 = (JSONArray) arr.get(ii);
                Points.add(new Point(Double.valueOf(arr1.get(0).toString()), Double.valueOf(arr1.get(1).toString()),
                        reference_4326));
            }
        }
        List<Point> destPoints = GeometryCopy(sourcePoint, pastePoint, Points);
        PointCollection pointCollection = new PointCollection(reference_4326);
        pointCollection.addAll(destPoints);
        MultipointBuilder multiPointBuilder = new MultipointBuilder(pointCollection);
        Graphic pasteGraphic = new Graphic();
        pasteGraphic.setSymbol(copyGraphicSelected.getSymbol());
        pasteGraphic.setGeometry(multiPointBuilder.toGeometry());
        pasteGraphic = addGraphicIDSpatial4326(pasteGraphic);
        return pasteGraphic;
    }

    private Graphic addGraphicIDSpatial4326(Graphic sourGraphic) {
        SpatialReference reference_4326 = SpatialReference.create(4326);
        Graphic targetGraphic = sourGraphic;
        Geometry geometry = GeometryEngine.project(sourGraphic.getGeometry(), reference_4326);
        targetGraphic.setGeometry(geometry);
        targetGraphic.getAttributes().put("layername", MapManager.KEY_OverlayDrapedBaseSymbol);
        targetGraphic.getAttributes().put("id", java.util.UUID.randomUUID().toString());
        return targetGraphic;
    }

    private Graphic addFEGraphicIDSpatial4326(Graphic sourGraphic, Point newPoint) {
        Graphic pasteGraphicSelected = new Graphic();
        SpatialReference reference_4326 = SpatialReference.create(4326);
        Geometry geometry = GeometryEngine.project(newPoint, reference_4326);
        pasteGraphicSelected.setGeometry(geometry);
        sourGraphic.getAttributes().forEach((key, value) -> {
            if (key.equals("id"))
                pasteGraphicSelected.getAttributes().put("id", java.util.UUID.randomUUID().toString());
            else
                pasteGraphicSelected.getAttributes().put(key, value);
        });
        
        return pasteGraphicSelected;
    }
    
    private List<Point> GeometryCopy(Point pointSource, Point pointDestination, List<Point> Points) {
        GeodeticDistanceResult ret = GeometryEngine.distanceGeodetic(pointSource, pointDestination,
                new LinearUnit(LinearUnitId.KILOMETERS), new AngularUnit(AngularUnitId.DEGREES),
                GeodeticCurveType.GEODESIC);

        // 兩點間距離
        double dis = ret.getDistance();
        // 兩點的方位角
        double angle = ret.getAzimuth1();

        // 回傳即是新Graphic的所有點
        List<Point> ps = GeometryEngine.moveGeodetic(Points, dis, new LinearUnit(LinearUnitId.KILOMETERS), angle,
                new AngularUnit(AngularUnitId.DEGREES), GeodeticCurveType.GEODESIC);

        return ps;
    }

    public void goCopyPasteItem(int index) {
        mapView.setViewpointGeometryAsync(pasteGraphicList.get(index).getGeometry());
    }

    public void removeCopyPasteItem(int index) {
        if (null == pasteGraphicList.get(index).getSymbol())
            overlayDrapedMilitarySymbol.getGraphics().remove(pasteGraphicList.get(index));
        else 
            overlayDrapedBaseMilitarySymbol.getGraphics().remove(pasteGraphicList.get(index));
        pasteGraphicList.remove(index);
    }

    private void copypasteOpen() {
        try {
            paramLoader = new FXMLLoader(getClass().getResource("/fxml/copypasteFX.fxml"));
            Parent root = paramLoader.load();
            Scene scene = new Scene(root);
            final JFXPanel jfxPanel = new JFXPanel();
            jfxPanel.setScene(scene);

            copypasteFrame = new JFrame("複製貼上作業");
            copypasteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            copypasteFrame.setAlwaysOnTop(true);
            copypasteFrame.add(jfxPanel);
            copypasteFrame.setSize(300, 436);
            copypasteFrame.setLocation(50, 205);
            copypasteFrame.setVisible(true);
        } catch (Exception e) {
            // on any error, display the stack trace.
            //e.printStackTrace();
        }
    }

    private void copypasteClose() {

        MapManager.isAddEditOverlay = false;

        // 貼上的暫存項目存檔
        if (pasteGraphicList.size() > 0)
            MapManager.overlayFileObject.SaveOverlayLayerToJson();
        pasteGraphicList.clear();

        if (prevClickFun != null) {
            mapView.setOnMouseClicked(prevClickFun);
            prevClickFun = null;
        }

        this.btnEditBase.setDisable(false);
        this.btnCopyPaste.setDisable(false);
    }

    /****************************
     * 複製貼上相關內容結尾
     **********************************************************/


 }
