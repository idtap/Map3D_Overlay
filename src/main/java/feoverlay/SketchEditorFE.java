package feoverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.symbology.ColorUtil;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class SketchEditorFE {
	@FunctionalInterface
	public interface CanCompleteEventHandler {
		void invoke();
	}

	/** 載彈狀態回復更新通知 */
	public static Event<CanCompleteEventHandler> CanCompleteEvent = new Event<CanCompleteEventHandler>();

	private void RaiseCanCompleteEvent() {
		// invoke all listeners:
		for (CanCompleteEventHandler listener : CanCompleteEvent.listeners()) {
			listener.invoke();
		}
	}

	@FunctionalInterface
	public interface CanEditDeleteEventHandler {
		void invoke(boolean enabled);
	}

	/** 載彈狀態回復更新通知 */
	public static Event<CanEditDeleteEventHandler> CanEditDeleteEvent = new Event<CanEditDeleteEventHandler>();

	private void RaiseCanEditDeleteEvent(boolean enabled) {
		// invoke all listeners:
		for (CanEditDeleteEventHandler listener : CanEditDeleteEvent.listeners()) {
			listener.invoke(enabled);
		}
	}

	private enum editStatusType {
		Normal, NewMilSymbol, EditMilSymbol,
	}

	private editStatusType currentEditStatus = editStatusType.Normal;

	private Graphic drawingPointGraphic = null;
	private Graphic drawingLineGraphic = null;
	private Graphic drawingAreaGraphic = null;
	private ArrayList<Graphic> drawedAnchorPointGraphics = new ArrayList<>();
	private ArrayList<Graphic> drawedAnchorLineGraphics = new ArrayList<>();

	private String selectedSIC = "";
	private String selectedShape = "";
	private Map<String, Object> feAttributes = null;

	private Graphic selectedGraphic = null;

	private PolygonBuilder areaBuilder = null;

	private MapView mapView;
	private GraphicsOverlay overlayDrapedMilitarySymbol;
	private GraphicsOverlay graphicsOverlay3D;

	private static final SimpleMarkerSymbol redSquareSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE,
			ColorUtil.colorToArgb(Color.RED), 10);
	private static final SimpleLineSymbol redDashLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH,
			ColorUtil.colorToArgb(Color.RED), 3);
	private static final SimpleLineSymbol redSolidlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
			ColorUtil.colorToArgb(Color.RED), 3);
	private static final SimpleFillSymbol lightGraySolidFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
			ColorUtil.colorToArgb(Color.LIGHTGRAY), redDashLineSymbol);

	/**
	 * 編輯節點旗標
	 */
	private boolean isEditNode = false;
	/**
	 * 移動Graphic旗標
	 */
	private boolean isMoveNode = false;

	/**
	 * 開始拖移Grapic的Point
	 */
	private Point pointBeginMove = null;

	// 畫曲線
	private boolean isCurveLine = true;
	private ArrayList<Point> curvePointArray3D = new ArrayList<Point>();
	private Graphic nowGraphic = null;
	private Graphic nowMoveGraphic = null;
	private SketchEditor feSketchEditor;
	private static EventHandler<? super javafx.scene.input.KeyEvent> prevKeyPressedFun = null;
	private static EventHandler<? super javafx.scene.input.KeyEvent> prevKeyReleasedFun = null;
	private static EventHandler<? super MouseEvent> prevClickFun = null;
	private static EventHandler<? super MouseEvent> prevMoveFun = null;

	/**
	 * 建構子
	 * 
	 * @param mapView
	 * @param overlayDrapedMilitarySymbol
	 */
	public SketchEditorFE(MapView mapView, GraphicsOverlay overlayDrapedMilitarySymbol, SketchEditor feSketchEditor) {
		this.mapView = mapView;
		this.overlayDrapedMilitarySymbol = overlayDrapedMilitarySymbol;

		graphicsOverlay3D = new GraphicsOverlay();
		mapView.getGraphicsOverlays().add(graphicsOverlay3D);

		this.feSketchEditor = feSketchEditor;
	}

	/**
	 * 新增軍符方法
	 * 
	 * @param selectedSIC
	 * @param selectedShape
	 */
	public void BeginNewDraw(String selectedSIC, String selectedShape, Map<String, Object> feAttributes,
			boolean isCurveLine) {
		this.isCurveLine = isCurveLine;
		ClearTempGraphics();

		switch (selectedShape) {
		case milSymbolCode.CONST_SHAPE_POINT:
			drawingPointGraphic = null;
			break;
		case milSymbolCode.CONST_SHAPE_LINE:
			if (isCurveLine) {
				// 畫曲線
				nowGraphic = new Graphic();
				nowGraphic.setSymbol(redSolidlineSymbol);
				graphicsOverlay3D.getGraphics().add(nowGraphic);

				nowMoveGraphic = new Graphic();
				nowMoveGraphic.setSymbol(redDashLineSymbol);
				graphicsOverlay3D.getGraphics().add(nowMoveGraphic);

				curvePointArray3D.clear();
			}
			break;
		case milSymbolCode.CONST_SHAPE_AREA:
			areaBuilder = new PolygonBuilder(getSpatialReference());
			break;
		}

		this.selectedSIC = selectedSIC;
		this.selectedShape = selectedShape;

		// 去除屬性內存在之SIDC
		this.feAttributes = new HashMap<String, Object>();
		feAttributes.forEach((key, value) -> {
			if (!key.equals("sidc"))
				this.feAttributes.put(key, value);
		});
		currentEditStatus = editStatusType.NewMilSymbol;
	}

	/**
	 * 判斷是否可編輯或刪除
	 * 
	 * @return
	 */
	public boolean CanEditOrDelete() {
		return (selectedGraphic != null);
	}

	/**
	 * 取得目前選取之Graphic
	 * 
	 * @return
	 */
	public Graphic getSelectedGraphic() {
		return selectedGraphic;
	}

	public void setSelectedGraphic(Graphic value) {
		selectedGraphic = value;
	}
	
	/**
	 * 開始編輯軍符方法
	 */
	public void BeginEditDraw() {
		if (selectedGraphic == null)
			return;

		MapManager.isAddEditOverlay = true;
		StartSketchEdit();
		currentEditStatus = editStatusType.EditMilSymbol;
	}

	private void drawTempLineByAnchors() {
		drawTempLineByAnchors(false);
	}

	/**
	 * 依據端點畫出虛線
	 */
	private void drawTempLineByAnchors(boolean removeOld) {
		// 清除所有虛線
		if (removeOld)
			graphicsOverlay3D.getGraphics().clear();

		// 重劃虛線
		for (int i = 0; i < drawedAnchorPointGraphics.size(); i++) {
			PointCollection points = new PointCollection(getSpatialReference());
			points.add((Point) drawedAnchorPointGraphics.get(i).getGeometry());
			if (i + 1 < drawedAnchorPointGraphics.size()) {
				points.add((Point) drawedAnchorPointGraphics.get(i + 1).getGeometry());
				Polyline polyline = new Polyline(points);
				Graphic lineGraphic = new Graphic(polyline, redDashLineSymbol);

				drawedAnchorLineGraphics.add(lineGraphic);
				graphicsOverlay3D.getGraphics().add(lineGraphic);
			}
		}

		// 如果為"面",則劃出最後一條連結虛線
		if (selectedShape == milSymbolCode.CONST_SHAPE_AREA) {
			PointCollection points = new PointCollection(getSpatialReference());
			points.add((Point) drawedAnchorPointGraphics.get(drawedAnchorPointGraphics.size() - 1).getGeometry());
			points.add((Point) drawedAnchorPointGraphics.get(0).getGeometry());
			Polyline polyline = new Polyline(points);
			Graphic lineGraphic = new Graphic(polyline, redDashLineSymbol);

			drawedAnchorLineGraphics.add(lineGraphic);
			graphicsOverlay3D.getGraphics().add(lineGraphic);
		}
	}

	/**
	 * 完成繪製軍符方法
	 */
	public void CompleteDraw() {
		MapManager.isAddEditOverlay = false;
		Geometry drawGeometry = null;
		Geometry drawGeometry_3D = null;

		if (currentEditStatus == editStatusType.NewMilSymbol) {
			Map<String, Object> attributes = new HashMap<>();

			// 可辨別key
			attributes.put("layername", MapManager.KEY_OverlayDrapedMilitarySymbol);
			String newUUID = java.util.UUID.randomUUID().toString();
			attributes.put("id", newUUID);
			attributes.put("shape", MapManager.SHAPE_MilitarySymbol);

			// 2525B
			attributes.put("_type", "position_report");
			attributes.put("_action", "update");
			attributes.put("sidc", selectedSIC);

			// 2525B註記
			feAttributes.forEach((key, value) -> {
				attributes.put(key, value);
			});

			switch (selectedShape) {
			case milSymbolCode.CONST_SHAPE_POINT:
				drawGeometry = drawingPointGraphic.getGeometry();
				drawGeometry_3D = drawingPointGraphic.getGeometry();
				break;
			case milSymbolCode.CONST_SHAPE_LINE:
				if (isCurveLine) {
					// 畫曲線
					graphicsOverlay3D.getGraphics().remove(nowGraphic);
					graphicsOverlay3D.getGraphics().remove(nowMoveGraphic);
					drawGeometry = GeometryEngine.project(nowGraphic.getGeometry(), SpatialReferences.getWgs84());
					drawGeometry_3D = GeometryEngine.project(nowGraphic.getGeometry(), SpatialReferences.getWgs84());
				} else {
					// 畫直線
					PolylineBuilder lineBuilder = new PolylineBuilder(getSpatialReference());
					for (int i = 0; i < drawedAnchorPointGraphics.size(); i++) {
						lineBuilder.addPoint((Point) drawedAnchorPointGraphics.get(i).getGeometry());
					}
					drawGeometry = lineBuilder.toGeometry();
					drawGeometry_3D = lineBuilder.toGeometry();
				}
				break;
			case milSymbolCode.CONST_SHAPE_AREA:
				drawGeometry = areaBuilder.toGeometry();
				drawGeometry_3D = areaBuilder.toGeometry();
				break;
			}

			Graphic millitaryGraphic = new Graphic(drawGeometry, attributes);
			overlayDrapedMilitarySymbol.getGraphics().add(millitaryGraphic);

//			Graphic millitaryGraphic_3D = new Graphic(drawGeometry_3D, attributes);
//			overlayDrapedMilitarySymbol_3D.getGraphics().add(millitaryGraphic_3D);
			ClearTempGraphics();

		} else if (currentEditStatus == editStatusType.EditMilSymbol) {
			drawGeometry = feSketchEditor.getGeometry();
			drawGeometry = GeometryEngine.project(drawGeometry, SpatialReferences.getWgs84());
			
			selectedGraphic.setGeometry(drawGeometry);
			selectedGraphic.setSelected(false);
			ClearTempGraphics();
			StopSketchEdit();
		}

		MapManager.overlayFileObject.SaveOverlayLayerToJson();
	}

	private void StartSketchEdit() {
		prevKeyPressedFun = mapView.getOnKeyPressed();
		prevKeyReleasedFun = mapView.getOnKeyReleased();
		prevClickFun = mapView.getOnMouseClicked();
		prevMoveFun = mapView.getOnMouseMoved();
		feSketchEditor.start(selectedGraphic.getGeometry());
	}

	private void StopSketchEdit() {
		feSketchEditor.stop();

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
	}

	/**
	 * 取消繪製軍符方法
	 */
	public void CancelDraw() {
		StopSketchEdit();

		if (isCurveLine) {
			// 畫曲線
			graphicsOverlay3D.getGraphics().remove(nowGraphic);
			graphicsOverlay3D.getGraphics().remove(nowMoveGraphic);
		}
		MapManager.isAddEditOverlay = false;
		ClearTempGraphics();
	}

	/**
	 * 刪除軍符方法
	 */
	public void DeleteGraphic() {
		if (selectedGraphic != null) {
			overlayDrapedMilitarySymbol.getGraphics().remove(selectedGraphic);
			MapManager.overlayFileObject.SaveOverlayLayerToJson();

			ClearTempGraphics();
		}
	}

	/**
	 * 編輯"線","面"MouseClicked事件處理,判斷為[修改節點]或[新增節點]
	 * 
	 * @param pointLocation
	 * @param screenPoint
	 */
	private void EditSymbolClickHandler(Point pointLocation, Point2D screenPoint) {
		ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphicsTmp = mapView
				.identifyGraphicsOverlayAsync(graphicsOverlay3D, screenPoint, 5, false);

		identifyGraphicsTmp.addDoneListener(() -> Platform.runLater(() -> {
			try {
				IdentifyGraphicsOverlayResult resultTmp = identifyGraphicsTmp.get();
				List<Graphic> graphicsTmp = resultTmp.getGraphics();
				if (!graphicsTmp.isEmpty()) {
					Graphic pointGraphic = null;
					Graphic linegraphic = null;
					for (int i = 0; i < graphicsTmp.size(); i++) {
						if (graphicsTmp.get(i).getGeometry().getGeometryType() == GeometryType.POINT) {
							pointGraphic = graphicsTmp.get(i);
						} else if (graphicsTmp.get(i).getGeometry().getGeometryType() == GeometryType.POLYLINE) {
							linegraphic = graphicsTmp.get(i);
						}
					}
					if (pointGraphic != null) {
						// 點選到節點可改變節點位置
						if (isEditNode == false) {
							drawingPointGraphic = pointGraphic;
							drawedAnchorLineGraphics.forEach(line -> {
								line.setSelected(false);
							});
							drawingPointGraphic.setSelected(true);
							isEditNode = true;
							return;
						}
					} else if (linegraphic != null) {
						// 點選到節線,可移動整個Graphic
						pointBeginMove = pointLocation;
						drawingPointGraphic.setSelected(false);

						drawedAnchorLineGraphics.forEach(line -> {
							line.setSelected(true);
						});
						isMoveNode = true;
						return;
					}
				}

				// 沒有點到節點,再新增節點
				drawDrawingTempLine(pointLocation);
				drawingPointGraphic = new Graphic(pointLocation, redSquareSymbol);
				drawingPointGraphic.setSelected(true);
				drawedAnchorPointGraphics.add(drawingPointGraphic);
				graphicsOverlay3D.getGraphics().add(drawingPointGraphic);

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}));
	}

	/**
	 * 滑鼠[移動事件]處裡
	 * 
	 * @param e
	 */
	public void handleMapViewMouseMoved(MouseEvent e) {
		Point2D screenPoint = new Point2D(e.getX(), e.getY());

		if (!selectedShape.equals(milSymbolCode.CONST_SHAPE_POINT)) {
			// 當"線","面"的節點時,畫出暫時虛擬圖像
			Point pointLocation = mapView.screenToLocation(screenPoint);

			try {

				if (currentEditStatus == editStatusType.EditMilSymbol && isMoveNode == true) {
					// "編輯"且"移動"狀態
					GeodeticDistanceResult ret = GeometryEngine.distanceGeodetic(pointBeginMove, pointLocation,
							new LinearUnit(LinearUnitId.KILOMETERS), new AngularUnit(AngularUnitId.DEGREES),
							GeodeticCurveType.GEODESIC);

					double dis = ret.getDistance();
					double angle = ret.getAzimuth1();

					List<Point> ps = new ArrayList<Point>();
					for (int i = 0; i < drawedAnchorPointGraphics.size(); i++) {
						ps.add((Point) drawedAnchorPointGraphics.get(i).getGeometry());
					}
					ps = GeometryEngine.moveGeodetic(ps, dis, new LinearUnit(LinearUnitId.KILOMETERS), angle,
							new AngularUnit(AngularUnitId.DEGREES), GeodeticCurveType.GEODESIC);
					for (int i = 0; i < drawedAnchorPointGraphics.size(); i++) {
						drawedAnchorPointGraphics.get(i).setGeometry(ps.get(i));
					}

					drawTempLineByAnchors(true);
					pointBeginMove = pointLocation;
				} else if (currentEditStatus == editStatusType.NewMilSymbol && isEditNode == false) {
					// "新增"且"編輯端點"狀態
					try {
						// 新增"線"持續畫出虛線,"面"持續畫出虛面
						if (selectedShape.equals(milSymbolCode.CONST_SHAPE_LINE)) {
							if (isCurveLine) {
								if (curvePointArray3D.size() > 0) {
									Point last_p = pointLocation;
									PolylineBuilder line_temp = new PolylineBuilder(SpatialReferences.getWgs84());
									Point previouse_p = (Point) GeometryEngine.project(
											curvePointArray3D.get(curvePointArray3D.size() - 1),
											SpatialReferences.getWgs84());
									line_temp.addPoint(previouse_p);
									line_temp.addPoint(last_p);
									nowMoveGraphic.setGeometry(line_temp.toGeometry());
								}
							} else {
								drawMouseMoveTempLine(pointLocation);
							}
						}

						if (selectedShape.equals(milSymbolCode.CONST_SHAPE_AREA)) {
							drawMouseMoveTempLine(pointLocation);
							drawMouseMoveTempArea(pointLocation);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

	/**
	 * 滑鼠[點選事件]處裡
	 * 
	 * @param e
	 */
	public void handleMapViewMouseClicked(MouseEvent e) {
		if (e.isStillSincePress()) {
			if (e.getButton() == MouseButton.PRIMARY) {
				Point2D screenPoint = new Point2D(e.getX(), e.getY());

				if (currentEditStatus == editStatusType.NewMilSymbol
						|| currentEditStatus == editStatusType.EditMilSymbol) {
					// 當"新增"或"編輯"軍隊符號
					Point pointLocation = (Point) GeometryEngine.project(mapView.screenToLocation(screenPoint),
							getSpatialReference());

					try {

						// 判斷是否定位在外太空
						if (pointLocation.getSpatialReference() == null)
							return;

						// 當為移動模式,算出前一個點跟目前點的距離及方向,劃出移動結果
						if (isMoveNode == true) {
							isMoveNode = false;

							GeodeticDistanceResult ret = GeometryEngine.distanceGeodetic(pointBeginMove, pointLocation,
									new LinearUnit(LinearUnitId.KILOMETERS), new AngularUnit(AngularUnitId.DEGREES),
									GeodeticCurveType.GEODESIC);

							double dis = ret.getDistance();
							double angle = ret.getAzimuth1();

							List<Point> ps = new ArrayList<Point>();
							for (int i = 0; i < drawedAnchorPointGraphics.size(); i++) {
								ps.add((Point) drawedAnchorPointGraphics.get(i).getGeometry());
							}
							ps = GeometryEngine.moveGeodetic(ps, dis, new LinearUnit(LinearUnitId.KILOMETERS), angle,
									new AngularUnit(AngularUnitId.DEGREES), GeodeticCurveType.GEODESIC);
							for (int i = 0; i < drawedAnchorPointGraphics.size(); i++) {
								drawedAnchorPointGraphics.get(i).setGeometry(ps.get(i));
							}

							drawTempLineByAnchors();
						} else if (isEditNode == false) {
							switch (selectedShape) {
							case milSymbolCode.CONST_SHAPE_POINT:
								if (drawingPointGraphic == null) {
									drawingPointGraphic = new Graphic(pointLocation, redSquareSymbol);
									drawingPointGraphic.setSelected(true);
									graphicsOverlay3D.getGraphics().add(drawingPointGraphic);
								} else {
									drawingPointGraphic.setGeometry(pointLocation);
								}
								RaiseCanCompleteEvent();
								break;
							case milSymbolCode.CONST_SHAPE_LINE:
								if (currentEditStatus == editStatusType.NewMilSymbol) {
									if (isCurveLine) {
										Point p5327 = (Point) GeometryEngine.project(pointLocation,
												SpatialReferences.getWebMercator());
										curvePointArray3D.add(p5327);
										// 新增"曲線"
										if (curvePointArray3D.size() >= 2) {
											DrawCurve();
											RaiseCanCompleteEvent();
										}
									} else {
										// 新增"直線"
										drawDrawingTempLine(pointLocation);

										drawingPointGraphic = new Graphic(pointLocation, redSquareSymbol);
										drawingPointGraphic.setSelected(true);
										drawedAnchorPointGraphics.add(drawingPointGraphic);
										graphicsOverlay3D.getGraphics().add(drawingPointGraphic);

										if (drawedAnchorPointGraphics.size() >= 2)
											RaiseCanCompleteEvent();
									}
								} else
									EditSymbolClickHandler(pointLocation, screenPoint);
								break;
							case milSymbolCode.CONST_SHAPE_AREA:
								if (currentEditStatus == editStatusType.NewMilSymbol) {
									drawDrawingTempLine(pointLocation);

									areaBuilder.addPoint(pointLocation);
									drawingPointGraphic = new Graphic(pointLocation, redSquareSymbol);
									drawingPointGraphic.setSelected(true);
									drawedAnchorPointGraphics.add(drawingPointGraphic);
									graphicsOverlay3D.getGraphics().add(drawingPointGraphic);

									if (drawedAnchorPointGraphics.size() >= 3)
										RaiseCanCompleteEvent();
								} else {
									EditSymbolClickHandler(pointLocation, screenPoint);
								}
								break;
							}
						} else {
							// 編輯節點
							drawingPointGraphic.setGeometry(pointLocation);
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					// 判定是否點選圖層上物件
					ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics2525B = mapView
							.identifyGraphicsOverlayAsync(overlayDrapedMilitarySymbol, screenPoint, 10, false);

					identifyGraphics2525B.addDoneListener(() -> Platform.runLater(() -> {
						try {
							IdentifyGraphicsOverlayResult result2525B = identifyGraphics2525B.get();
							List<Graphic> graphics2525B = result2525B.getGraphics();
							if (!graphics2525B.isEmpty()) {
								// 點到物件
								Graphic graphic = graphics2525B.get(0);
								selectedGraphic = graphic;
								RaiseCanEditDeleteEvent(true);
							} else {
								// 沒點到
								RaiseCanEditDeleteEvent(false);
							}

						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}));

				}
			}
		}
	}

	/** 取得地圖空間參考 */
	private SpatialReference getSpatialReference() {
		return SpatialReferences.getWgs84();
	}

	/** 畫出"線","面"輪廓 */
	private void drawDrawingTempLine(Point newPoint) {
		if (drawingPointGraphic != null) {
			PointCollection points = new PointCollection(getSpatialReference());
			points.add((Point) drawingPointGraphic.getGeometry());
			points.add(newPoint);

			Polyline polyline = new Polyline(points);
			Graphic lineGraphic = new Graphic(polyline, redDashLineSymbol);

			drawedAnchorLineGraphics.add(lineGraphic);
			graphicsOverlay3D.getGraphics().add(lineGraphic);
		}
	}

	/** 畫出虛擬線 */
	private void drawMouseMoveTempLine(Point newPoing) {
		if (drawingPointGraphic != null) {
			if (drawingLineGraphic != null)
				graphicsOverlay3D.getGraphics().remove(drawingLineGraphic);

			PointCollection points = new PointCollection(getSpatialReference());
			points.add((Point) drawingPointGraphic.getGeometry());
			points.add(newPoing);

			Polyline polyline = new Polyline(points);
			drawingLineGraphic = new Graphic(polyline, redDashLineSymbol);

			graphicsOverlay3D.getGraphics().add(drawingLineGraphic);
		}
	}

	/** 畫出虛擬區域 */
	private void drawMouseMoveTempArea(Point newPoing) {
		if (drawingPointGraphic != null) {
			if (drawingAreaGraphic != null)
				graphicsOverlay3D.getGraphics().remove(drawingAreaGraphic);

			if (drawedAnchorPointGraphics.size() < 2)
				return;

			PointCollection points = new PointCollection(getSpatialReference());
			for (int i = 0; i < drawedAnchorPointGraphics.size(); i++)
				points.add((Point) drawedAnchorPointGraphics.get(i).getGeometry());
			points.add(newPoing);

			Polygon polygon = new Polygon(points);
			drawingAreaGraphic = new Graphic(polygon, lightGraySolidFillSymbol);

			graphicsOverlay3D.getGraphics().add(drawingAreaGraphic);
		}
	}

	/** 清除新增,編輯所有狀態 */
	public void ClearTempGraphics() {
		graphicsOverlay3D.getGraphics().remove(drawingPointGraphic);
		graphicsOverlay3D.getGraphics().remove(drawingLineGraphic);
		graphicsOverlay3D.getGraphics().remove(drawingAreaGraphic);

		for (int i = 0; i < drawedAnchorPointGraphics.size(); i++)
			graphicsOverlay3D.getGraphics().remove(drawedAnchorPointGraphics.get(i));

		for (int i = 0; i < drawedAnchorLineGraphics.size(); i++)
			graphicsOverlay3D.getGraphics().remove(drawedAnchorLineGraphics.get(i));

		drawingPointGraphic = null;
		drawingLineGraphic = null;
		drawingAreaGraphic = null;
		drawedAnchorPointGraphics.clear();
		drawedAnchorLineGraphics.clear();

		if (selectedGraphic != null) {
			selectedGraphic.setVisible(true);
			selectedGraphic.setSelected(false);
		}
		selectedGraphic = null;
		RaiseCanEditDeleteEvent(false);
		selectedSIC = "";
		selectedShape = "";

		areaBuilder = null;
		isEditNode = false;
		isMoveNode = false;
		pointBeginMove = null;

		currentEditStatus = editStatusType.Normal;
	}

	// 畫曲線方法
	private void DrawCurve() {
		PolylineBuilder rect_temp = new PolylineBuilder(SpatialReferences.getWebMercator());

		// 首點
		Point next_p_surf = curvePointArray3D.get(0);
		rect_temp.addPoint(next_p_surf);
		for (int i = 0; i < (curvePointArray3D.size() - 2); i++) {
			// 找出此圓角第一點
			Point p1 = curvePointArray3D.get(i);
			Point p2 = curvePointArray3D.get(i + 1);
			double x1 = p1.getX() + (p2.getX() - p1.getX()) * 0.5;
			double y1 = p1.getY() + (p2.getY() - p1.getY()) * 0.5;
			// 找出此圓角第二點
			p1 = curvePointArray3D.get(i + 1);
			p2 = curvePointArray3D.get(i + 2);
			double x2 = p1.getX() + (p2.getX() - p1.getX()) * 0.5;
			double y2 = p1.getY() + (p2.getY() - p1.getY()) * 0.5;
			// 繪製此曲線
			DrawBasizer(rect_temp, x1, y1, p1.getX(), p1.getY(), x2, y2);
		}
		// 尾點
		next_p_surf = curvePointArray3D.get(curvePointArray3D.size() - 1);
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

			Point next_p_surf = new Point(nx, ny);
			rect_temp.addPoint(next_p_surf);
		}
		Point next_p_surf = new Point(x3, y3);
		rect_temp.addPoint(next_p_surf);
	}
}
