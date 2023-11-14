package feoverlay;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.json.JSONException;

import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Functions {
	public static String apPath;
	public static Stage primaryStage;
	public static String appStylesheet = null;

	/**
	 * 字串是否為數字
	 * @param value
	 * @return
	 */
	public static Boolean isNumber(String value) {
		if (value.isEmpty())
			return false;
		try {
			Double parseValue  = Double.parseDouble(value);
		}
		catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}
	
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
	
	/*
	 * @param doubleTextField
	 * @param minValue
	 * @param maxValue
	 */
	private static void setDoubleTextField(TextField doubleTextField, double minValue, double maxValue) {
		doubleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
	        try {
	            if (!newValue.isEmpty()) {
	                // Try to parse the new input as a double
	                double value = Double.parseDouble(newValue);
	                
	                // If the value is out of the specified range, set the text field to the old value	                
	                // Check if the value is within the specified limits
	                if (value < minValue) {
	                    doubleTextField.setText(String.valueOf(minValue));
	                } else if (value > maxValue) {
	                    doubleTextField.setText(String.valueOf(maxValue));
	                }
	            }
	        } catch (NumberFormatException e) {
	            // Handle non-numeric input
	            doubleTextField.setText(oldValue);
	        }
	    });
	}
}
