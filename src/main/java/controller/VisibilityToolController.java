package controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureCollection;
import com.esri.arcgisruntime.data.FeatureCollectionTable;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.raster.ColormapRenderer;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingBoolean;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingFeatures;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingJob;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingParameter;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingParameters;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingRaster;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingString;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask;

import feoverlay.Functions;
import feoverlay.MapManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class VisibilityToolController implements Initializable {
	@FXML
	private Button btnGenerate;
	@FXML
	private Button btnSaveVisibility;
	
	private Stage _stage_main;
	private AnchorPane parentPane;

	
	private FeatureCollection featureCollection;
	public static GeoprocessingTask gpTask;
	private String tmpTif;
	private Point pointLocation;
	private Stage stageVisibility;
	private VisibilityToolController visibilityController;
	private SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000,
			10);
	
	
	VisibilityToolController(Stage _stage_main) {
		this._stage_main = _stage_main;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		try {
			tmpTif = new File(System.getProperty("data.dir"), "./samples-data/test.tif").getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 產出(觀測)點的Raster圖層
	 */
	private void createFeatureCollectionTableWithPointFeature() {
		featureCollection = new FeatureCollection();

		// create name field for polyline
		List<Field> pointField = new ArrayList<>();
		pointField.add(Field.createString("Name", "Name of feature", 20));

		// create a feature collection table
		FeatureCollectionTable featureCollectionTable = new FeatureCollectionTable(pointField, GeometryType.POINT,
				SpatialReferences.getWebMercator(), true, false);

		// add the feature collection table to the feature collection and load it
		featureCollection.addDoneLoadingListener(() -> {
			if (featureCollection.getLoadStatus() == LoadStatus.LOADED) {

				// add the feature collection table to the feature collection, and create a
				// feature from it, using the polyline
				// sketched by the user
				featureCollection.getTables().add(featureCollectionTable);
				Map<String, Object> attributes = new HashMap<>();
				attributes.put(pointField.get(0).getName(), "DectionPoint");
				Feature addedFeature = featureCollectionTable.createFeature(attributes, pointLocation);

				// add feature to collection table
				featureCollectionTable.addFeatureAsync(addedFeature);

			} else {
				new Alert(AlertType.ERROR, "Feature collection failed to load").show();
			}
		});
		featureCollection.loadAsync();
	}

	/**
	 * 點籍分析按鈕
	 */
	@FXML
	protected void handleGenerateVisibility() {
		if (pointLocation == null)
			return;
		
		btnGenerate.setDisable(true);
		
		clearResults();
		createFeatureCollectionTableWithPointFeature();
		doViewShed();
	}

	/**
	 * 執行視域分析Geoprocessing
	 */
	private void doViewShed() {
		// tracking progress of creating contour map
//		progressBar.setVisible(true);
		// create parameter using interval set
		GeoprocessingParameters gpParameters = new GeoprocessingParameters(
				GeoprocessingParameters.ExecutionType.ASYNCHRONOUS_SUBMIT);

		String rasterURL = new File(System.getProperty("data.dir"), "samples-data/raster-file/TaiwanDem_new_P.tif")
				.getAbsolutePath();

		final Map<String, GeoprocessingParameter> inputs = gpParameters.getInputs();
		inputs.put("in_raster", new GeoprocessingRaster(rasterURL, "Raster"));
		inputs.put("in_observer_features", new GeoprocessingFeatures(featureCollection.getTables().get(0)));
		inputs.put("nonvisible_cell_value", new GeoprocessingBoolean(true)); // NoData is assigned to nonvisible cells
		inputs.put("inner_radius", new GeoprocessingString("0"));
		inputs.put("outer_radius", new GeoprocessingString("20000"));
//		inputs.put("horizontal_start_angle", new GeoprocessingString("0"));
//		inputs.put("horizontal_end_angle", new GeoprocessingString("180"));
//		inputs.put("vertical_lower_angle", new GeoprocessingString("-90"));
//		inputs.put("vertical_upper_angle", new GeoprocessingString("90"));

		// adds contour lines to map
		GeoprocessingJob gpJob = gpTask.createJob(gpParameters);

		gpJob.addProgressChangedListener(() -> {
//			progressBar.setProgress(((double) gpJob.getProgress()) / 100);
		});

		gpJob.addJobDoneListener(() -> {
			if (gpJob.getStatus() == Job.Status.SUCCEEDED) {
				Map<String, GeoprocessingParameter> result = gpJob.getResult().getOutputs();
				GeoprocessingRaster r = (GeoprocessingRaster) result.get("out_raster");

				System.out.println(r.getUrl());

				File f = new File(tmpTif);
				if (f.exists())
					f.delete();

				//取得URL下載分析後之圖層至暫存檔
				try (BufferedInputStream in = new BufferedInputStream(new URL(r.getUrl()).openStream());
						FileOutputStream fileOutputStream = new FileOutputStream(tmpTif)) {
					byte dataBuffer[] = new byte[1024];
					int bytesRead;
					while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
						fileOutputStream.write(dataBuffer, 0, bytesRead);
					}

					// Load the raster file
					loadRasterLayerFile(tmpTif, pointLocation);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					btnGenerate.setDisable(false);
					btnSaveVisibility.setDisable(false);
				}
			} else {
				btnGenerate.setDisable(false);
				
				Alert dialog = new Alert(AlertType.ERROR);
				dialog.setHeaderText("Geoprocess Job Fail");
				dialog.setContentText("Error: " + gpJob.getError().getAdditionalMessage());
				dialog.showAndWait();
			}
//			progressBar.setVisible(false);
		});
		gpJob.start();

	}

	private void loadRasterLayerFile(String path, Point center) {
		// Load the raster file
		Raster myRasterFile = new Raster(path);
		// Create the layer
		RasterLayer myRasterLayer = new RasterLayer(myRasterFile);
		// get a colormap renderer.
		ColormapRenderer colormapRenderer = getColorMap(); 
		// Set the colormap renderer on the raster layer.
		myRasterLayer.setRasterRenderer(colormapRenderer);
		
		myRasterLayer.addDoneLoadingListener(() -> {
			if (myRasterLayer.getLoadStatus() == LoadStatus.LOADED) {
				// Add the layer to the map
				MapManager.sceneView.getArcGISScene().getOperationalLayers().add(myRasterLayer);
				
				if (center != null) {
					Viewpoint vp = new Viewpoint(center, 100000);
					MapManager.sceneView.setViewpoint(vp);
				}
			} else {
				showMessage("載入失敗", "圖檔載入失敗!原因:" + myRasterLayer.getLoadError().getMessage());
			}
		});
		myRasterFile.loadAsync();
	}
	
	/**
	 * 取得視域分析結果色盤
	 * @return
	 */
	private ColormapRenderer getColorMap() {
		// Create a color map where values 0-149 are red and 150-249 are yellow.
		List<Color> colors = new ArrayList<Color>();
		colors.add(Color.RED);
		colors.add(Color.GREEN);

		// Create a colormap renderer.
		return new ColormapRenderer(colors);
	}
	
	/**
	 * 點擊載入已儲存之視域分析結果圖層
	 */
	@FXML
	protected void handleLoadResults() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("data.dir"), "./samples-data"));
		fileChooser.getExtensionFilters()
				.addAll(new FileChooser.ExtensionFilter("TIFF",
						"*.tif;"));

		File selectedFile = fileChooser.showOpenDialog(Functions.primaryStage);
		if (selectedFile == null)
			return;
		

		// Load the raster file
		try {
			loadRasterLayerFile(selectedFile.getCanonicalPath(), null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 點擊儲存視域分析結果圖層
	 */
	@FXML
	protected void handleSaveResults() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		LocalDateTime now = LocalDateTime.now();
		String tmpFileName = dtf.format(now) + ".tif";
		
		File f = new File(tmpTif);
		File out = new File(System.getProperty("data.dir"), "./samples-data/" + tmpFileName);
		try {
			copyFileUsingStream(f, out);
			System.out.print(tmpFileName + "儲存完成!!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 檔案複製 
	 */
	private static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	
	/**
	 * 清空視域分析結果圖層
	 */
	private void clearResults() {
		if (featureCollection != null)
			featureCollection.getTables().clear();
		
		if (MapManager.sceneView.getArcGISScene().getOperationalLayers().size() >= 1) {
			MapManager.sceneView.getArcGISScene().getOperationalLayers().remove(0);
			btnGenerate.setDisable(false);
		}
	}
	
	private void showMessage(String title, String message) {

		Platform.runLater(() -> {
			Alert dialog = new Alert(AlertType.INFORMATION);
			dialog.initOwner(MapManager.sceneView.getScene().getWindow());
			dialog.setHeaderText(title);
			dialog.setContentText(message);
			dialog.showAndWait();

			Platform.exit();
		});
	}

}
