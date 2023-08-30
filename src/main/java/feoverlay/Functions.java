package feoverlay;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.json.JSONException;

import com.esri.arcgisruntime.geometry.GeodesicSectorParameters;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.symbology.Symbol;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class Functions {
	public static String apPath;
	public static Stage primaryStage;
	public static String appStylesheet = null;

	/** 字串左邊添滿 */
	public static String leftPad(String inputString, int length, char letter) {
		return String.format("%1$" + length + "s", inputString).replace(' ', letter);
	}

	/** 座標度轉度分秒 */
	public static String DegreeToDMS(double value) {
		int sec = (int) Math.round(value * 3600);
		int deg = sec / 3600;
		sec = Math.abs(sec % 3600);
		int min = sec / 60;
		sec %= 60;

		String strDMS = "";
		strDMS += leftPad(String.valueOf(deg), 2, '0') + "°";
		strDMS += leftPad(String.valueOf(min), 2, '0') + "'";
		strDMS += leftPad(String.valueOf(sec), 2, '0') + "\"";
		return strDMS;
	}

	/**
	 * 存檔方法
	 */
	public static void SaveFile(String filePath, String value) {
		FileWriter file = null;
		try {
			file = new FileWriter(filePath);
			file.write(value);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				file.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/** 讀取外部json檔案得到JSONObject */
	public static String readJsonFromPath(String path) throws IOException, JSONException {
		FileInputStream is = new FileInputStream(path);
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			return jsonText;
		} finally {
			is.close();
		}
	}

	/**
	 * 讀取檔案內容
	 * 
	 * @param rd
	 * @return
	 * @throws IOException
	 */
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

}
