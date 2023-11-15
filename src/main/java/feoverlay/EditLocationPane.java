package feoverlay;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.MultipointBuilder;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.SelectedVertexChangedEvent;
import com.esri.arcgisruntime.mapping.view.SelectedVertexChangedListener;
import com.esri.arcgisruntime.mapping.view.SketchEditor;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class EditLocationPane implements SelectedVertexChangedListener {
	private enum EditMode {
		sketch, manual,
	}

	private SketchEditor sketchEditor;
	private HBox movablePane;
	private TextField txtLon;
	private TextField txtLat;
	private Button btnApply;
	private Point curVertex = null;
	private Graphic curGraphic = null;

	private double xOffset, yOffset;
	private EditMode curEditMode = EditMode.sketch;
	private PointCollection pointCollection;

	public EditLocationPane(SketchEditor sketchEditor, HBox movablePane, TextField txtLon, TextField txtLat,
			Button btnApply) {
		this.sketchEditor = sketchEditor;
		this.movablePane = movablePane;
		this.txtLon = txtLon;
		this.txtLat = txtLat;
		this.btnApply = btnApply;

		this.btnApply.setOnAction((e) -> {
			if (!checkItemValue())
				return;

			Apply();
		});

		setDoubleTextField(txtLon, -180, 180);
		setDoubleTextField(txtLat, -90, 90);

		movablePane.setLayoutX(750);
		movablePane.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});

		movablePane.setOnMouseDragged(event -> {
			double deltaX = event.getSceneX() - xOffset;
			double deltaY = event.getSceneY() - yOffset;

			movablePane.setLayoutX(movablePane.getLayoutX() + deltaX);
			movablePane.setLayoutY(movablePane.getLayoutY() + deltaY);
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();

			if (xOffset < 0.0 || yOffset < 0.0 || xOffset > 1024.0 || yOffset > 768.0) {
				movablePane.setLayoutX(0);
				movablePane.setLayoutY(0);
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}

		});

		sketchEditor.addSelectedVertexChangedListener(this);
		sketchEditor.getSketchEditConfiguration().setContextMenuEnabled(true);
	}

	/**
	 * 檢核欄位輸入值
	 * 
	 * @return
	 */
	private boolean checkItemValue() {
		String error = "";

		if (!isNumber(txtLon.getText()))
			error += "經度必須有值\n";

		if (!isNumber(txtLat.getText()))
			error += "緯度必須有值\n";

		if (error.equals(""))
			return true;
		else {
//			AlertDialog.errorAlert(this.getStage(), "輸入值錯誤", error, false);
			return false;
		}
	}

	/*
	 * @param doubleTextField
	 * 
	 * @param minValue
	 * 
	 * @param maxValue
	 */
	private void setDoubleTextField(TextField doubleTextField, double minValue, double maxValue) {
		doubleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (!newValue.isEmpty()) {
					// Try to parse the new input as a double
					double value = Double.parseDouble(newValue);

					// If the value is out of the specified range, set the text field to the old
					// value
					// Check if the value is within the specified limits
					if (value < minValue) {
						doubleTextField.setText(String.valueOf(oldValue));
					} else if (value > maxValue) {
						doubleTextField.setText(String.valueOf(oldValue));
					}
				}
			} catch (NumberFormatException e) {
				// Handle non-numeric input
				doubleTextField.setText(oldValue);
			}
		});
	}

	/**
	 * 字串是否為數字
	 * 
	 * @param value
	 * @return
	 */
	public static Boolean isNumber(String value) {
		if (value.isEmpty())
			return false;
		try {
			Double parseValue = Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	public void setCurVertex(Point vertex) {
		curEditMode = EditMode.sketch;

		movablePane.setVisible(true);
		this.curVertex = vertex;
		Point p = (Point) GeometryEngine.project(vertex, SpatialReferences.getWgs84());

		this.txtLon.setText(String.valueOf(Math.round(p.getX() * 100000.0) / 100000.0));
		this.txtLat.setText(String.valueOf(Math.round(p.getY() * 100000.0) / 100000.0));
	}

	private void Apply() {
		if (curEditMode == EditMode.sketch) {
			if (sketchEditor.getGeometry() == null)
				return;

			Point pChange = new Point(Double.parseDouble(txtLon.getText()), Double.parseDouble(txtLat.getText()),
					SpatialReferences.getWgs84());
			Point pTo = (Point) GeometryEngine.project(pChange, curVertex.getSpatialReference());

			if (sketchEditor.getGeometry().getGeometryType() == GeometryType.POINT) {
				sketchEditor.clearGeometry();
				sketchEditor.replaceGeometry(pTo);
				sketchEditor.start(pTo);
			} else if (sketchEditor.getGeometry().getGeometryType() == GeometryType.MULTIPOINT) {
				MultipointBuilder builder = new MultipointBuilder(curVertex.getSpatialReference());
				Multipoint multipoint = (Multipoint) sketchEditor.getGeometry();
				multipoint.getPoints().forEach((p) -> {
					if (p.equals(curVertex))
						builder.getPoints().add(pTo);
					else
						builder.getPoints().add(p);
				});

				sketchEditor.clearGeometry();
				Multipoint newMultipoint = builder.toGeometry();
				sketchEditor.replaceGeometry(newMultipoint);
				sketchEditor.start(newMultipoint);
			} else if (sketchEditor.getGeometry().getGeometryType() == GeometryType.POLYLINE) {
				PolylineBuilder builder = new PolylineBuilder(curVertex.getSpatialReference());
				Polyline line = (Polyline) sketchEditor.getGeometry();
				line.getParts().getPartsAsPoints().forEach((p) -> {
					if (p.equals(curVertex))
						builder.addPoint(pTo);
					else
						builder.addPoint(p);
				});

				sketchEditor.clearGeometry();
				Polyline newLine = builder.toGeometry();
				sketchEditor.replaceGeometry(newLine);
				sketchEditor.start(newLine);
			} else if (sketchEditor.getGeometry().getGeometryType() == GeometryType.POLYGON) {
				PolygonBuilder builder = new PolygonBuilder(curVertex.getSpatialReference());
				Polygon polygon = (Polygon) sketchEditor.getGeometry();
				polygon.getParts().getPartsAsPoints().forEach((p) -> {
					if (p.equals(curVertex))
						builder.addPoint(pTo);
					else
						builder.addPoint(p);
				});
				sketchEditor.clearGeometry();
				Polygon newPolygon = builder.toGeometry();
				sketchEditor.replaceGeometry(newPolygon);
				sketchEditor.start(newPolygon);
			}
			curVertex = null;
			
		} else if (curEditMode == EditMode.manual) {
			if (curGraphic == null)
				return;
			
			if (curGraphic.getGeometry().getGeometryType() == GeometryType.POINT) {
				Point pChange = new Point(Double.parseDouble(txtLon.getText()), Double.parseDouble(txtLat.getText()),
						SpatialReferences.getWgs84());
				Point pTo = (Point) GeometryEngine.project(pChange, curGraphic.getGeometry().getSpatialReference());
				curGraphic.setGeometry(pTo);
			} else if (curGraphic.getGeometry().getGeometryType() == GeometryType.MULTIPOINT) {
				Point pChange = new Point(Double.parseDouble(txtLon.getText()), Double.parseDouble(txtLat.getText()),
						SpatialReferences.getWgs84());
				Point pTo = (Point) GeometryEngine.project(pChange, curGraphic.getGeometry().getSpatialReference());
				Multipoint mp = changeMultiPoint((Multipoint)curGraphic.getGeometry(), pTo);
				curGraphic.setGeometry(mp);
			}
		}
		
		movablePane.setVisible(false);
	}

	@Override
	public void selectedVertexChanged(SelectedVertexChangedEvent event) {
		if (event.getSketchVertex() == null)
			return;
		Point curVertex = event.getSketchVertex().getPoint();
		setCurVertex(curVertex);
	}

	public void setEditPoint(Graphic graphic) {
		curEditMode = EditMode.manual;
		movablePane.setVisible(true);
		this.curGraphic = graphic;
		Point p = null;
		
		if (graphic.getGeometry().getGeometryType() == GeometryType.POINT) {
			p = (Point) GeometryEngine.project(graphic.getGeometry(), SpatialReferences.getWgs84());	
		} else if (graphic.getGeometry().getGeometryType() == GeometryType.MULTIPOINT) {
			Multipoint mp = (Multipoint)graphic.getGeometry();
			p = mp.getPoints().get(mp.getPoints().size() - 1);
			p = (Point) GeometryEngine.project(p, SpatialReferences.getWgs84());
		}
		
		this.txtLon.setText(String.valueOf(Math.round(p.getX() * 100000.0) / 100000.0));
		this.txtLat.setText(String.valueOf(Math.round(p.getY() * 100000.0) / 100000.0));
	}
	
	public void setUnvisible() {
		movablePane.setVisible(false);
		curVertex = null;
	}	
	
	public void setPointCollection(PointCollection pointCollection) {
		Point p = null;
		
		this.pointCollection = pointCollection;
		p = this.pointCollection.get(this.pointCollection.size() - 1);
		p = (Point) GeometryEngine.project(p, SpatialReferences.getWgs84());

		this.txtLon.setText(String.valueOf(Math.round(p.getX() * 100000.0) / 100000.0));
		this.txtLat.setText(String.valueOf(Math.round(p.getY() * 100000.0) / 100000.0));
	}
	
	
	private Multipoint changeMultiPoint(Multipoint mp, Point newP) {
		MultipointBuilder multiPointBuilder = new MultipointBuilder(mp.getSpatialReference());
		for (int i = 0; i <= mp.getPoints().size() - 1; i++) {
			Point p = mp.getPoints().get(i);
			if (i == mp.getPoints().size() - 1)
				multiPointBuilder.getPoints().add(newP);
			else
				multiPointBuilder.getPoints().add(p);
		}
		return multiPointBuilder.toGeometry();		
	}
	
}
