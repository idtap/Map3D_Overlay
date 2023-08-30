package feoverlay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * 透明圖圖檔類別
 * @author Tommyw
 *
 */
public class OverlayGroupClass {
	private String fileName = "";
	private String overlaysString = "";
	private String groupString = "";

	public OverlayGroupClass(String fileName) {
		this.fileName = fileName;
	}	
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getOverlaysString() {
		return overlaysString;
	}

	public void setOverlaysString(String overlaysString) {
		this.overlaysString = overlaysString;
	}

	public String getGroupString() {
		return groupString;
	}

	public void setGroupString(String groupString) {
		this.groupString = groupString;
	}
	
	public void CreateNew() {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("Overlays", "{}");
		hashMap.put("Groups", "{}");
		String strAll = JSONObject.toJSONString(hashMap);
		
		if (fileName.equals(""))
			SaveAsFile(strAll, null);
		else
			SaveFile(strAll);
	}
	

	public void Save() {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("Overlays", overlaysString);
		hashMap.put("Groups", groupString);
		String strAll = JSONObject.toJSONString(hashMap);

		if (fileName.equals(""))
			SaveAsFile(strAll, null);
		else
			SaveFile(strAll);
	}

	/**
	 * 開啟透明圖檔讀取內容
	 */
	public void Open() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
			HashMap<String, String> hashMap = (HashMap<String, String>) JSONValue.parse(br);
			overlaysString = hashMap.get("Overlays");
			groupString =  hashMap.get("Groups");
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}			
	}
	
	/**
	 * 存檔方法
	 */
	private void SaveFile(String value) {
		FileWriter file = null;
		try {
			file = new FileWriter(fileName);
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

	/**
	 * 存檔方法
	 */
	private void SaveAsFile(String value, Window win) {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setInitialDirectory(new File("cfg/Overlays/"));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

		File selectedFile = fileChooser.showSaveDialog(win);
		if (selectedFile == null)
			return;

		fileName = selectedFile.getAbsolutePath();
		SaveFile(value);
	}
}
