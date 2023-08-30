package feoverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ColorUtil;
import com.esri.arcgisruntime.symbology.Symbol;
import com.esri.arcgisruntime.symbology.TextSymbol;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class GroupGraphicsClass {

	@FunctionalInterface
	public interface CanGroupOrNotEventHandler {
		void invoke(boolean enabled, Graphic graphic);
	}

	public static Event<CanGroupOrNotEventHandler> CanGroupOrNotEvent = new Event<CanGroupOrNotEventHandler>();

	private void RaiseCanGroupOrNotEvent(boolean enabled, Graphic graphic) {
		// invoke all listeners:
		for (CanGroupOrNotEventHandler listener : CanGroupOrNotEvent.listeners()) {
			listener.invoke(enabled, graphic);
		}
	}

	@FunctionalInterface
	public interface CanUngroupOrNotEventHandler {
		void invoke(boolean enabled);
	}

	public static Event<CanUngroupOrNotEventHandler> CanUngroupOrNotEvent = new Event<CanUngroupOrNotEventHandler>();

	private void RaiseCanUngroupOrNotEvent(boolean enabled) {
		canUngroup = enabled;
		// invoke all listeners:
		for (CanUngroupOrNotEventHandler listener : CanUngroupOrNotEvent.listeners()) {
			listener.invoke(enabled);
		}
	}

	@FunctionalInterface
	public interface AfterGroupMouseClickHandleEventHandler {
		void invoke(MouseEvent e);
	}

	/** 載彈狀態回復更新通知 */
	public static Event<AfterGroupMouseClickHandleEventHandler> AfterGroupMouseClickHandle = new Event<AfterGroupMouseClickHandleEventHandler>();

	private void RaiseAfterGroupMouseClickHandle(MouseEvent e) {
		// invoke all listeners:
		for (AfterGroupMouseClickHandleEventHandler listener : AfterGroupMouseClickHandle.listeners()) {
			listener.invoke(e);
		}
	}


	private MapView mapView;
	/**
	 * 所有透明圖圖層
	 */
	private List<GraphicsOverlay> graphicsOverlayAll = new ArrayList<>();
	/**
	 * 所有已選取之Graphic
	 */
	private List<Graphic> graphicsSelected = new ArrayList<>();
	/**
	 * Graphics清單 (graphic id, group id)
	 */
	private HashMap<String, String> graphicsGroupedList = new HashMap<>();
	/**
	 * Group清單 (group id, List<graphic id>)
	 */
	private HashMap<String, List<String>> groupedList = new HashMap<>();
	/**
	 * 表使用者有按CTRL或SHIFT
	 */
	private boolean ShiftCtrlKeyPressed = false;
	/**
	 * 表目前可以群組
	 */
	private boolean canGroup = false;
	/**
	 * 表目前可以解除群組
	 */
	private boolean canUngroup = false;

	private String selectedGroupID = "";
	private Graphic selGraphic = null;

	// 取得單選一個透明圖圖形方法
	public Graphic getSelectedOneGraphic() {
		if (!canGroup && !canUngroup) {
			return selGraphic;
		}else
			return null;
	}

	public void clearGraphicSelected() {
		selGraphic = null;
	}
	
	/**
	 * 取得選擇群組的所有Graphic
	 * 
	 * @return
	 */
	public List<Graphic> getSelectedGroupGraphics() {
		return graphicsSelected;
	}

	/**
	 * 判斷是否可群組
	 * 
	 * @return
	 */
	public boolean isCanGroup() {
		return canGroup;
	}

	/**
	 * 判斷是否可解除群組
	 * 
	 * @return
	 */
	public boolean isCanUngroup() {
		return canUngroup;
	}

	/**
	 * 建構子
	 * 
	 * @param sceneView
	 * @param graphicsOverlayAll
	 */
	public GroupGraphicsClass(MapView mapView, List<GraphicsOverlay> graphicsOverlayAll) {
		this.mapView = mapView;
		this.graphicsOverlayAll = graphicsOverlayAll;

		//解析儲存Group資訊之json字串
		graphicsGroupedList = (HashMap<String, String>) JSONValue.parse(MapManager.overlayGroupObject.getGroupString());
		
		// 依據graphicsGroupedList,得出groupedList
		graphicsGroupedList.forEach((graphicId, groupId) -> {
			if (null != getGraphicByID(graphicId)) {
				if (!groupedList.containsKey(groupId)) {
					List<String> lst = new ArrayList<String>();
					lst.add(graphicId);
					groupedList.put(groupId, lst);
				} else {
					groupedList.get(groupId).add(graphicId);
				}
			}
		});

		// 資料清理(如Group內沒有兩個Graphics)
		List<String> removeLst = new ArrayList<String>();
		groupedList.forEach((key, value) -> {
			if (value.size() < 2) {
				removeLst.add(key);
			}
		});
		removeLst.forEach((key) -> {
			groupedList.remove(key);
		});
	}

	/**
	 * 將所有Graphics轉換成JSON字串並存檔
	 */
	public void SaveToJsonFile() {
		String strAll = JSONObject.toJSONString(graphicsGroupedList);
		MapManager.overlayGroupObject.setGroupString(strAll);
		MapManager.overlayGroupObject.Save();
	}
	
	/**
	 * 建立群組方法
	 */
	public void setSelectedToGroup() {
		selectedGroupID = java.util.UUID.randomUUID().toString();
		List<String> lst = new ArrayList<String>();

		for (int i = 0; i < graphicsSelected.size(); i++) {
			Graphic g = graphicsSelected.get(i);
			String graphicID = g.getAttributes().get("id").toString();
			graphicsGroupedList.put(graphicID, selectedGroupID);
			SaveToJsonFile();
			lst.add(graphicID);
		}
		groupedList.put(selectedGroupID, lst);

		RaiseCanUngroupOrNotEvent(true);
	}

	/**
	 * 解除群組方法
	 */
	public void setUnGroup() {
		if (selectedGroupID != "") {
			groupedList.get(selectedGroupID).forEach((graphicID) -> {
				graphicsGroupedList.remove(graphicID);
			});
			groupedList.remove(selectedGroupID);
			SaveToJsonFile();
			selectedGroupID = "";

			clearAllLayserSelections();
		}
	}

    private int count = 0;
   /**
    * 於透明圖圖層框選透明圖
    * @param polygon
    */
    public int setBoxOverlaysSelected(Geometry polygon) {
    	count = 0;
        //取消目前已選擇之圖形
    	clearAllLayserSelections();

  		MapManager.OverlayLayers.values().forEach((layer) -> {
  			layer.getGraphics().forEach((g) -> {
  				if (g.getGeometry() != null && g.getAttributes().size() != 0) {
	  	  			boolean intersect = GeometryEngine.intersects(g.getGeometry(), polygon);
	  				if (intersect) {
	  					//判斷是否該Graphic已被群組過
	  					//如果是就不匡列
	  					String groupID = graphicsGroupedList.get(g.getAttributes().get("id"));
	  					if (null == groupID || groupID.equals("")) {
		  					count++;
		  					setGraphicSelect(g, true);
							graphicsSelected.add(g);
	  					}
	  				}
  				}
  			});
  		});
  		
  		if (graphicsSelected.size() >= 2) {
			RaiseCanGroupOrNotEvent(true, null);
			canGroup = true;
		} else if (graphicsSelected.size() == 1) {
			selGraphic = graphicsSelected.get(0);
			graphicsSelected.clear();
			RaiseCanGroupOrNotEvent(false, selGraphic);
			canGroup = false;
		} else {
			selGraphic = null;
			RaiseCanGroupOrNotEvent(false, null);
			canGroup = false;
		}
  		
  		return count;
    }
	
	
	/**
	 * 處理滑鼠點選事件
	 * 
	 * @param e
	 */
	public void handleViewMouseClicked(MouseEvent e) {

		if (MapManager.isAddEditOverlay) {
			// 如果在做新增,編輯透明圖時,就不做辨識是否點選Graphic的動作
			RaiseAfterGroupMouseClickHandle(e);
			return;
		}

		int i = 0;
		identifyGraphicHandler(i, e);
	}

	//TRUE->當之前選了兩個Graphics可Group,但之後取消剩一個Graphic不可Group時
	//此可避免程式去誤判,最後unslected的Graphic,為可修改之Graphic
	public boolean specialFlag = false;
	
	/**
	 * 處理使用者滑鼠點選Graphic物件
	 * 
	 * @param idx
	 * @param mapViewPoint
	 */
	private void identifyGraphicHandler(int idx, MouseEvent e) {
		Point2D mapViewPoint = new Point2D(e.getX(), e.getY());
		GraphicsOverlay graphicsOverlay = graphicsOverlayAll.get(idx);
		ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics;
		identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, mapViewPoint, 10, false);

		identifyGraphics.addDoneListener(() -> {
			try {
				if (!identifyGraphics.get().getGraphics().isEmpty()) {
					// 點選到Graphic
					selGraphic = identifyGraphics.get().getGraphics().get(0);
					String graphicID = selGraphic.getAttributes().get("id").toString();

					if (graphicsGroupedList.containsKey(graphicID)) {
						// 該Graphic已群組
						if (graphicsSelected.size() > 0)
							clearAllLayserSelections();

						selectedGroupID = graphicsGroupedList.get(graphicID);
						if (selGraphic.isSelected() == false) {
							// 將該群組Graphic設定selected
							if (groupedList.containsKey(selectedGroupID)) {
								groupedList.get(selectedGroupID).forEach((grahicID) -> {
									Graphic g = getGraphicByID(grahicID);
									if (null != g) {
										setGraphicSelect(g, true);
//										g.setSelected(true);
										graphicsSelected.add(g);
									}
								});
								RaiseCanUngroupOrNotEvent(true);
								return;
							} else {
								// 異外: graphic有Groupid,但groupedList找不到該GroupID
								graphicsGroupedList.remove(graphicID);
								selectedGroupID = "";
							}
						}
						return;
					} else {
						// 該Graphic不在群組內
						if (selectedGroupID != "") {
							// 之前已經選到群組了
							selectedGroupID = "";
							clearAllLayserSelections();
						}
						if (canUngroup)
							RaiseCanUngroupOrNotEvent(false);
					}

					if (selGraphic.isSelected()) {
						setGraphicSelect(selGraphic, false);
						graphicsSelected.remove(selGraphic);
						if (graphicsSelected.size() == 1) {
							selGraphic = graphicsSelected.get(0);
							specialFlag = true;
						}
					} else {
						if (ShiftCtrlKeyPressed == false) {
							clearAllLayserSelections();
						}
						setGraphicSelect(selGraphic, true);
						graphicsSelected.add(selGraphic);
					}

					if (graphicsSelected.size() >= 2) {
						selGraphic = null;
						RaiseCanGroupOrNotEvent(true, null);
						canGroup = true;
					} else {
						RaiseCanGroupOrNotEvent(false, selGraphic);
						canGroup = false;
					}

					if (!canGroup && !canUngroup)
						RaiseAfterGroupMouseClickHandle(e);
					return;
				} else {
					// 沒有點選到Graphic
					selGraphic = null;

					if (idx + 1 < graphicsOverlayAll.size()) {
						// 還有下一個圖層
						identifyGraphicHandler(idx + 1, e);
					} else {
						// 巡到最後一層
						if (ShiftCtrlKeyPressed == false) {
							clearAllLayserSelections();
							if (canGroup)
								RaiseCanGroupOrNotEvent(false, null);
							canGroup = false;
						}
						if (canUngroup)
							RaiseCanUngroupOrNotEvent(false);

						RaiseAfterGroupMouseClickHandle(e);
						return;
					}
				}
			} catch (Exception x) {
				// on any error, display the stack trace
				x.printStackTrace();
			}
		});
	}

	private void setGraphicSelect(Graphic graphic, boolean selected) {
		Symbol symbol = graphic.getSymbol();
		if (symbol != null && symbol.getClass() == TextSymbol.class) {
			if (selected) {
				((TextSymbol) symbol).setHaloColor(ColorUtil.colorToArgb(Color.CYAN));
				((TextSymbol) symbol).setHaloWidth(3);
			} else
				((TextSymbol) symbol).setHaloWidth(0);
		} else {
			graphic.setSelected(selected);
		}
	}

	/**
	 * 處裡鍵盤Pressed事件
	 * 
	 * @param e
	 */
	public void handleViewKeyPressed(KeyEvent e) {
		if (e.getCode() == KeyCode.SHIFT || e.getCode() == KeyCode.CONTROL) {
			ShiftCtrlKeyPressed = true;
		}
	}

	/**
	 * 處裡鍵盤Released事件
	 * 
	 * @param e
	 */
	public void handleViewKeyReleased(KeyEvent e) {
		ShiftCtrlKeyPressed = false;
	}

	/**
	 * 依據Graphic的Attribute->id值,找到圖層內的Graphic
	 * 
	 * @param id
	 * @return
	 */
	private Graphic getGraphicByID(String id) {
		for (int i = 0; i < graphicsOverlayAll.size(); i++) {
			GraphicsOverlay graphicsOverlay = graphicsOverlayAll.get(i);
			for (int j = 0; j < graphicsOverlay.getGraphics().size(); j++) {
				Graphic g = graphicsOverlay.getGraphics().get(j);
				if (g.getAttributes().containsKey("id") && g.getAttributes().get("id").equals(id))
					return g;
			}
		}
		return null;
	}

	/*
	 * 清除圖台上所有圖層的Graphics選取
	 */
	public void clearAllLayserSelections() {
		for (int i = 0; i < graphicsOverlayAll.size(); i++) {
			GraphicsOverlay graphicsOverlay = graphicsOverlayAll.get(i);
			graphicsOverlay.getGraphics().forEach((g) -> {
				Symbol symbol = g.getSymbol();
				if (symbol != null && symbol.getClass() == TextSymbol.class)
					((TextSymbol) symbol).setHaloWidth(0);
				else
					g.setSelected(false);
			});
		}
		graphicsSelected.clear();
		
		RaiseCanGroupOrNotEvent(false, null);
		RaiseCanUngroupOrNotEvent(false);
	}
}
