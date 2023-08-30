package feoverlay;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSceneSymbol;
import com.esri.arcgisruntime.symbology.Symbol;

/**
 * 存取透明圖圖層Graphics功能類別
 * 
 * @author Tommyw
 *
 */
public class OverlayFileClass {

	public OverlayFileClass(String value) {
		parseJsonImportGraphics(value);
	}

	/**
	 * 給"Gemotry","Symbol","attributs"JSON內容字串組合後,得到一個JSON字串
	 */
	private static String graphicToJson(String strGeometry, String strSymbol, String strAttributes) {
		JSONObject obj = new JSONObject();
		obj.put("geometry", strGeometry);
		obj.put("symbol", strSymbol);
		obj.put("Attributes", strAttributes);
		return obj.toJSONString();
	}

	/**
	 * 給JSON字串,得到Graphic物件
	 */
	public static Graphic jsonToGraphic(String Value) {
		JSONObject obj = (JSONObject) JSONValue.parse(Value);
		Geometry g = Geometry.fromJson(obj.get("geometry").toString());
		Symbol s = (obj.get("symbol").equals("") ? null : Symbol.fromJson(obj.get("symbol").toString()));
		Map<String, Object> attr = (Map<String, Object>) JSONValue.parse(obj.get("Attributes").toString());

		Graphic graphic;
		if (s == null) {
			// 沒有Symbol就是軍隊符號
			graphic = new Graphic(g, attr);
		} else {
			graphic = new Graphic(g, s);
			attr.forEach((key, val) -> {
				graphic.getAttributes().put((String) key, val);
			});
		}

		return graphic;
	}

	public static void importLayerGraphicsToMap(GraphicsOverlay layer, HashMap<String, String> hashMap, boolean containUnvisible) {
		if (!layer.getGraphics().isEmpty()) {
			for (Graphic g : layer.getGraphics()) {
				if (containUnvisible == false && g.isVisible() == false)
					continue;
				
				JSONObject attrObject = new JSONObject(g.getAttributes());
				String jsonObject = "";
				
				//依據圖形的種類放進不同圖層
				if (null != g.getGeometry()) {
					if (null == g.getSymbol()) {
						// 2525B Graphic 沒有Symbol
						jsonObject = graphicToJson(g.getGeometry().toJson(), "", attrObject.toJSONString());
					} else {
						if (g.getSymbol() != null) {
							Class symbolType = g.getSymbol().getClass();
							if (symbolType.equals(SimpleMarkerSceneSymbol.class))
								continue;
							else if (symbolType.equals(ModelSceneSymbol.class))
								continue;
							else {
								// "點","線","面", "文字"圖形
								jsonObject = graphicToJson(g.getGeometry().toJson(), g.getSymbol().toJson(),
										attrObject.toJSONString());
							}
						}
					}
					if (!jsonObject.equals("")) {
						hashMap.put(String.valueOf(hashMap.size()+1), jsonObject);
					}
				}
			}
		}
	}
	
	/**
	 * 將所有Graphics轉換成JSON字串
	 */
	public void SaveOverlayLayerToJson() {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		for (GraphicsOverlay layer : MapManager.OverlayLayers.values()) {
			importLayerGraphicsToMap(layer, hashMap, true);
		}
		String strAll = JSONObject.toJSONString(hashMap);

		MapManager.overlayGroupObject.setOverlaysString(strAll);
		MapManager.overlayGroupObject.Save();
	}

	/**
	 * 開啟JSON檔轉換成將所有軍隊符號Graphics
	 */
	public void parseJsonImportGraphics(String json) {
		try {
			HashMap<String, String> hashMap = (HashMap<String, String>) JSONValue.parse(json);
			hashMap.forEach((key, value) -> {
				GraphicsOverlay layer = null;
				Graphic g = jsonToGraphic(value);
				if (null == g.getSymbol()) {
					// 2525B Graphic 沒有Symbol
					layer = MapManager.OverlayLayers.get(MapManager.KEY_OverlayDrapedMilitarySymbol);
					layer.getGraphics().add(g);
				} else {
					Class symbolType = g.getSymbol().getClass();
					if (g.getSymbol().getClass().equals(SimpleMarkerSceneSymbol.class)) {
						// Do nothing

					} else if (g.getSymbol().getClass().equals(ModelSceneSymbol.class)) {
						// Do nothing
					} else {
						// "點","線","面", "文字"圖形
						layer = MapManager.OverlayLayers.get(MapManager.KEY_OverlayDrapedBaseSymbol);
						layer.getGraphics().add(g);
					} 
				}
				// MapManager.OverlayLayers

				// graphicsOverlay.getGraphics().add(g);
			});
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
