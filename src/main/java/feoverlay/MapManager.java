package feoverlay;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.json.JSONException;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.hydrography.EncCell;
import com.esri.arcgisruntime.hydrography.EncDataset;
import com.esri.arcgisruntime.hydrography.EncExchangeSet;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.layers.EncLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.Viewpoint.Type;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.GeoView;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties.SurfacePlacement;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.ColorUtil;
import com.esri.arcgisruntime.symbology.DictionaryRenderer;
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle;
import com.esri.arcgisruntime.symbology.MarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.toolkit.OverviewMap;

import controller.MapContentController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class MapManager {
	public static MapContentController mapController;
	public static SceneView sceneView;
	public static MapView mapView;
	public static boolean is3DView = false; // 預設2D圖台顯示

	private static String[] vtpkBaseLayerfileNames;

	// SceneView底圖
	private static Basemap basemap_Raster;
	private static Basemap basemap_Vector;
	private static Basemap basemap_Vector2;
	private static Basemap basemap_Vector3;
	private static Basemap basemap_Vector4;

	// 鷹眼圖底圖2D
	private static Basemap overview_basemap_Raster;
	private static Basemap overview_basemap_Vector;
	private static Basemap overview_basemap_Vector2;
	private static Basemap overview_basemap_Vector3;
	private static Basemap overview_basemap_Vector4;

	// 鷹眼圖底圖3D
	private static Basemap overview3D_basemap_Raster;
	private static Basemap overview3D_basemap_Vector;
	private static Basemap overview3D_basemap_Vector2;
	private static Basemap overview3D_basemap_Vector3;
	private static Basemap overview3D_basemap_Vector4;

	private static final String FILE_DEFAULT_VIEWPOINT = "cfg/Viewpoint.json";

	/**
	 * 透明圖圖層清單
	 */
	public static HashMap<String, GraphicsOverlay> OverlayLayers = new HashMap<>();
	/**
	 * 3D展示圖層清單
	 */
	public static ObservableList<GraphicsOverlay> simulateDemoLayerList = FXCollections.observableArrayList();


	public static boolean isAddEditOverlay = false;
	public static OverlayFileClass overlayFileObject;
	public static OverlayGroupClass overlayGroupObject;

	// 軍隊符號透明圖圖層的Key值
	public static final String KEY_OverlayDrapedMilitarySymbol = "overlayDrapedMilitarySymbol";
	// 基本圖形透明圖圖層的Key值
	public static final String KEY_OverlayDrapedBaseSymbol = "overlayDrapedBaseSymbol";

	// Graphic的Shape Kind
	public static final String SHAPE_MilitarySymbol = "MilitarySymbol";

	/**
	 * 載入.tpkx圖資回傳Basemap
	 * 
	 * @throws Exception
	 */
	public static Basemap getBasemapFromTpkx(String fileName) throws Exception {
		ArcGISTiledLayer tiledLayer_Raster = getArcGISTiledLayerFromTpkx(fileName);
		return new Basemap(tiledLayer_Raster);
	}

	/**
	 * 讀取tpkx檔案回傳ArcGISTiledLayer
	 * 
	 * @param fileName
	 * @return
	 */
	public static ArcGISTiledLayer getArcGISTiledLayerFromTpkx(String fileName) {
		try {
			String mapPath = new File("basemap/" + fileName).getCanonicalPath();
			File tmpDir = new File(mapPath);
			if (tmpDir.exists() == false) {
				return null;
			}
			tmpDir.setExecutable(false);
			tmpDir.setReadable(true);
			tmpDir.setWritable(false);
			TileCache tileCache_Raster = new TileCache(mapPath);
			return new ArcGISTiledLayer(tileCache_Raster);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 載入.vpkx圖資回傳Basemap
	 * 
	 * @throws Exception
	 */
	public static Basemap getBasemapFromVtpk(String fileName) throws Exception {
		try {
			String mapPath = new File("basemap/" + fileName).getCanonicalPath();
			ArcGISVectorTiledLayer tiledLayer = getVectorTileLayerFromVtpk(mapPath);
			if (null == tiledLayer)
				return null;
			return new Basemap(tiledLayer);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static ArcGISVectorTiledLayer getVectorTileLayerFromVtpk(String fileName) throws Exception {
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				return null;
			}
			file.setExecutable(false);
			file.setReadable(true);
			file.setWritable(false);
			return new ArcGISVectorTiledLayer(file.getCanonicalPath());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static int iBasemap = 0;
	public static boolean baseMapReload2;
	public static boolean baseMapReload4;

	/**
	 * 切換底圖
	 * 
	 * @throws Exception
	 */
	public static void changeBaseMap() throws Exception {
		basemap_Vector = MapManager.getBasemapFromVtpk("GlobeOpenStreetMap.vtpk");
		MapManager.mapView.getMap().setBasemap(basemap_Vector);
		MapManager.sceneView.getArcGISScene().setBasemap(basemap_Vector.copy());
	}

	/**
	 * 設定原始Camera
	 */
	public static void setDefaultCamera(GeoView gv) {
		if (null == gv) {
			if (is3DView)
				gv = sceneView;
			else
				gv = mapView;
		}
		File f = new File(FILE_DEFAULT_VIEWPOINT);
		if (f.exists() && !f.isDirectory()) {
			try {
				f.setExecutable(false);
				f.setReadable(true);
				f.setWritable(true);
				String result = Functions.readJsonFromPath(FILE_DEFAULT_VIEWPOINT);
				Viewpoint vp = Viewpoint.fromJson(result);
				gv.setViewpointAsync(vp, 2.0f);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Point p = new Point(120.500, 22.601, 99989);
			Camera camera = new Camera(p, 0, 0, 0);
			Viewpoint vp = new Viewpoint(p, 10000, camera);
			gv.setViewpointAsync(vp, 2.0f);
		}
	}

	/**
	 * 存入預設場景
	 */
	public static void saveCurrentViewpoint() {
		Viewpoint vp = null;
		if (MapManager.is3DView)
			vp = sceneView.getCurrentViewpoint(Type.CENTER_AND_SCALE);
		else
			vp = mapView.getCurrentViewpoint(Type.CENTER_AND_SCALE);
		String s = vp.toJson();
		Functions.SaveFile(FILE_DEFAULT_VIEWPOINT, s);
	}

	public static void setView2D() {
		sceneView.setVisible(false);
		mapView.setVisible(true);
	}

	public static void setView3D() {
		sceneView.setVisible(true);
		mapView.setVisible(false);
	}

	/**
	 * 切換2D/3D
	 * 
	 * @return
	 * @return
	 */
	public static ListenableFuture<Boolean> switchViewKind() {
		if (is3DView) {
			is3DView = false;
			setView2D();

			Viewpoint vp = sceneView.getCurrentViewpoint(Type.CENTER_AND_SCALE);
			if (null != vp)
				mapView.setViewpoint(vp);
			else {
				Point2D screenCenter = sceneView.screenToLocal(sceneView.getWidth() * 0.5, sceneView.getHeight() * 0.5);
				Point mapPoint = sceneView.screenToBaseSurface(screenCenter);
				ListenableFuture<Boolean> ret = mapView.setViewpointCenterAsync(mapPoint);
				return ret;
			}
		} else {
			is3DView = true;
			setView3D();

			Viewpoint vp = mapView.getCurrentViewpoint(Type.CENTER_AND_SCALE);
			ListenableFuture<Boolean> ret = sceneView.setViewpointAsync(vp);
			return ret;
		}
		return null;
	}

	public static void setOverlayEditStatus() {
		if (is3DView)
			mapController.handleViewKindSwitch(true);
		isAddEditOverlay = true;
	}

	/**
	 * Zoom In
	 */
	public static void ZoomIn() {
		GeoView gv = null;
		if (is3DView)
			gv = sceneView;
		else
			gv = mapView;
		Viewpoint current = gv.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE);
		Viewpoint zoomedIn = new Viewpoint((Point) current.getTargetGeometry(), current.getTargetScale() / 2.0);
		gv.setViewpointAsync(zoomedIn);
	}

	/**
	 * Zoom out
	 */
	public static void ZoomOut() {
		GeoView gv = null;
		if (is3DView)
			gv = sceneView;
		else
			gv = mapView;
		Viewpoint current = gv.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE);
		Viewpoint zoomedOut = new Viewpoint((Point) current.getTargetGeometry(), current.getTargetScale() * 2.0);
		gv.setViewpointAsync(zoomedOut);
	}

	public static void ZoomTo(Polygon polygon) {
		GeoView gv = null;
		if (is3DView)
			gv = sceneView;
		else
			gv = mapView;

		Viewpoint viewPoint = new Viewpoint(polygon.getExtent());
		gv.setViewpointAsync(viewPoint);
	}
	
	public static void RemoveOtherPreLoad() {
		for (int i = MapManager.sceneView.getArcGISScene().getOperationalLayers().size() - 1; i >= 0; i--) {
			Layer l = MapManager.sceneView.getArcGISScene().getOperationalLayers().get(i);
			if  (l.getClass() != FeatureLayer.class)
				MapManager.sceneView.getArcGISScene().getOperationalLayers().remove(l);
		};
	}

	/**
	 * 新增縣市界及道路圖圖層
	 */
	public static void addVtpkBaseLayers() {
		vtpkBaseLayerfileNames = new String[] {"taiwancounty.vtpk", "taiwanroad.vtpk" };

		for (int i = 0; i < vtpkBaseLayerfileNames.length; i++) {
			String fileName = vtpkBaseLayerfileNames[i];
			try {

				ArcGISVectorTiledLayer tiledLayer = getVectorTileLayerFromVtpk(
						new File("basemap/" + fileName).getCanonicalPath());
				if (null == tiledLayer)
					return;
				tiledLayer.setName(fileName);
				tiledLayer.loadAsync();
				tiledLayer.addDoneLoadingListener(() -> {
					if (tiledLayer.getLoadStatus() == LoadStatus.LOADED) {
						tiledLayer.setVisible(false);
						MapManager.sceneView.getArcGISScene().getOperationalLayers().add(tiledLayer);
						ArcGISVectorTiledLayer tiledLayer_Raster2 = tiledLayer.copy();
						tiledLayer_Raster2.setVisible(false);
						MapManager.mapView.getMap().getOperationalLayers().add(tiledLayer_Raster2);
					}
				});
				Thread.sleep(200);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 切換SceneView轉至2D視角,方便編輯Geometry
	 * 
	 * @param geometry
	 */
	private void change2DView(Geometry geometry) {
		Point center = geometry.getExtent().getCenter();
		Camera currentCamera = sceneView.getCurrentViewpointCamera();
		Camera newCamera = currentCamera.rotateAround(center, 0.0, -currentCamera.getPitch(), 0.0);
		// set the sceneview to the new camera
		sceneView.setViewpointCameraAsync(newCamera);
	}

	/**
	 * 切換SceneView轉至2D視角(依sceneView中心點)
	 */
	public void change2DView() {
		Point2D screenCenter = sceneView.screenToLocal(sceneView.getWidth() * 0.5, sceneView.getHeight() * 0.5);
		ListenableFuture<Point> mapPoint = sceneView.screenToLocationAsync(screenCenter);

		mapPoint.addDoneListener(() -> {
			// get the current camera
			Camera currentCamera = sceneView.getCurrentViewpointCamera();

			if (currentCamera == null || currentCamera.getPitch() == 0.0)
				return;

			// rotate the camera using the delta pitch value
			Camera newCamera;
			try {
				Point p = mapPoint.get();
				if (!(p.getX() == 0.0 && p.getY() == 0.0)) {
					newCamera = currentCamera.rotateAround(p, 0.0, -currentCamera.getPitch(), 0.);
					// set the sceneview to the new camera
					sceneView.setViewpointCameraAsync(newCamera);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}

	public static GraphicsOverlay getNewMilitaryOverlay() {
		try {
			// 新增及加入"軍隊符號透明圖圖層"
			DictionarySymbolStyle dictionarySymbol = initDictionarySymbol();

			GraphicsOverlay layer = new GraphicsOverlay();
			layer.getSceneProperties().setSurfacePlacement(SurfacePlacement.DRAPED_BILLBOARDED);
			DictionaryRenderer renderer2D = new DictionaryRenderer(dictionarySymbol);
			layer.setRenderer(renderer2D);
			return layer;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 初始化載入軍符Stylx
	 */
	public static DictionarySymbolStyle initDictionarySymbol() {
		File stylxFile = new File(ArcGISRuntimeEnvironment.getResourcesDirectory() + "/symbols/mil2525bc2_idt.stylx");
		stylxFile.setExecutable(false);
		stylxFile.setReadable(true);
		stylxFile.setWritable(false);
		DictionarySymbolStyle dictionarySymbol = DictionarySymbolStyle.createFromFile(stylxFile.getAbsolutePath());
		dictionarySymbol.loadAsync();
		return dictionarySymbol;
	}

	/**
	 * .shp檔案載入處理
	 */
	@SuppressWarnings("removal")
	public static void addShapefile(String path) {
		ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(path);
		FeatureLayer featureLayer = new FeatureLayer(shapefileFeatureTable);

		featureLayer.addDoneLoadingListener(() -> {
			if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
				if (featureLayer.getFeatureTable().getGeometryType() == GeometryType.POINT) {
					//點(style=CROSS,color=紅色,size=5)
					MarkerSymbol MARKER_SYMBOL = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS,
							ColorUtil.colorToArgb(Color.RED), 5);
					SimpleRenderer simpleRenderer2 = new SimpleRenderer(MARKER_SYMBOL);
					featureLayer.setRenderer(simpleRenderer2);
				} else if (featureLayer.getFeatureTable().getGeometryType() == GeometryType.POLYLINE) {
					//線(style=SOLID,color=紅色,size=5)
					SimpleLineSymbol symbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
							ColorUtil.colorToArgb(Color.RED), 5);
					SimpleRenderer simpleRenderer = new SimpleRenderer(symbol);
					featureLayer.setRenderer(simpleRenderer);
				} else if (featureLayer.getFeatureTable().getGeometryType() == GeometryType.POLYGON) {
					//面線條(style=DASH,color=紅色,size=3)
					SimpleLineSymbol redDashLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH,
							ColorUtil.colorToArgb(Color.RED), 3);
					//面填滿(style=SOLID,color=亮灰)
					SimpleFillSymbol lightGraySolidFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
							ColorUtil.colorToArgb(Color.LIGHTGRAY), redDashLineSymbol);
					SimpleRenderer simpleRenderer = new SimpleRenderer(lightGraySolidFillSymbol);
					featureLayer.setRenderer(simpleRenderer);
				}

				// Add the feature layer to the map
				MapManager.mapView.getMap().getOperationalLayers().add(featureLayer);
				FeatureLayer featureLayer2 = featureLayer.copy();
				MapManager.sceneView.getArcGISScene().getOperationalLayers().add(featureLayer2);

//				Envelope e = featureLayer.getFullExtent();
//				Viewpoint vp = new Viewpoint(e);
//
//				if (MapManager.is3DView)
//					MapManager.sceneView.setViewpoint(vp);
//				else
//					MapManager.mapView.setViewpoint(vp);
			} else {
				if (featureLayer.getLoadError().getErrorCode() == 1001) // 1001表資料表內沒有紀錄
					AlertDialog.informationAlert("讀取圖檔沒有發現任何內容!!", false);
				else
					AlertDialog.errorAlert("圖檔載入失敗!原因:" + featureLayer.getLoadError().getMessage(), false);
			}
		});

		shapefileFeatureTable.loadAsync();
	}
}
