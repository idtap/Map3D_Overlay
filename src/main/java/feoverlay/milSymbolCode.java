package feoverlay;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;

public class milSymbolCode {
	public static final String CONST_SHAPE_POINT = "POINT";
	public static final String CONST_SHAPE_LINE = "LINE";
	public static final String CONST_SHAPE_AREA = "AREA";
	
	private final SimpleStringProperty name;
    private final SimpleStringProperty code;
    private final SimpleStringProperty shape;

    public milSymbolCode(String name, String code,String shape) {
        this.name = new SimpleStringProperty(name);
        this.code = new SimpleStringProperty(code);
        this.shape = new SimpleStringProperty(shape);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String fName) {
        name.set(fName);
    }

    public String getCode() {
        return code.get();
    }

    public void setCode(String value) {
    	code.set(value);
    }

	public String getShape() {
		return shape.get();
	}
}