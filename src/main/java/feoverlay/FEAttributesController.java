package feoverlay;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.symbology.ColorUtil;
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle;
import com.esri.arcgisruntime.symbology.Symbol;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tornadofx.control.DateTimePicker;

public class FEAttributesController implements Initializable {
	public Stage _stage_main;
	private DictionarySymbolStyle dictionarySymbol;
	private String _SIC = "SPGP-----------";
	
    private Map<String, Object> _FEAttribues;
    
	private Map<String, String> _StrengthHashMap = new LinkedHashMap<>();
	private ObservableMap<String, String> _Strength = FXCollections.observableMap(_StrengthHashMap);

	private Map<String, String> _RatingHashMap = new LinkedHashMap<>();
	private ObservableMap<String, String> _Rating = FXCollections.observableMap(_RatingHashMap);
	
	@FXML
	private DateTimePicker dtpDateTime;
			
	@FXML
	private ComboBox cboRating;

	@FXML
	private ComboBox cboStrength;
    
	@FXML
	private TextField txtAltDepth;

	@FXML
	private TextField txtComment;

	@FXML
	private TextField txtDirection;

	@FXML
	private TextField txtEffectiveness;

	@FXML
	private TextField txtHQ;

	@FXML
	private TextField txtIFFSIF;

	@FXML
	private TextField txtLocation;

	@FXML
	private TextField txtMoreInfo;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtParent;

	@FXML
	private TextField txtQuantity;

	@FXML
	private TextField txtSignature;

	@FXML
	private TextField txtSpeed;

	@FXML
	private TextField txtType;
	
	@FXML
	private ImageView imgViewSymbol;

	public void CleanValues() {
    	dtpDateTime.setValue(null);
    	txtAltDepth.setText("");
    	txtLocation.setText("");
    	txtType.setText("");
    	txtName.setText("");
    	txtSpeed.setText("");
    	txtQuantity.setText("");
    	txtHQ.setText("");	
    	txtDirection.setText("");
   		cboStrength.getSelectionModel().select(null);
    	txtComment.setText("");	
    	txtMoreInfo.setText("");	
    	txtParent.setText("");	
   		cboRating.getSelectionModel().select(null);
    	txtEffectiveness.setText("");	
    	txtSignature.setText("");	
    	txtIFFSIF.setText("");
 	}
	
    /** 設定註記屬性內容,點選確認按鈕 */
    public void Confirm() {
    	FEAttributes fe = new FEAttributes(false);

    	if (dtpDateTime.getEditor().getText().equals(""))
    		fe.DateTimeValid = "";
    	else {
		    Date value = Date.from(dtpDateTime.getDateTimeValue().atZone(ZoneId.systemDefault()).toInstant());
		    if (value != null)
		    	fe.DateTimeValid = FEAttributes.getValidFromDate(value);
		    else
		    	fe.DateTimeValid = ""; 	
    	}
    	if (!txtAltDepth.getText().equals(""))
    		fe.AltDepth = txtAltDepth.getText();
    	if (!txtLocation.getText().equals(""))
    		fe.Location = txtLocation.getText();
    	if (!txtType.getText().equals(""))
    		fe.Type = txtType.getText();
    	if (!txtName.getText().equals(""))
    		fe.Uniquedesignation = txtName.getText();	
    	if (!txtSpeed.getText().equals(""))
    		fe.Speed = String.valueOf(Integer.parseInt(txtSpeed.getText())); 	
    	if (!txtQuantity.getText().equals(""))
    		fe.Quantity = txtQuantity.getText(); 		
    	if (!txtHQ.getText().equals(""))
    		fe.HQ = txtHQ.getText();	
    	if (!txtDirection.getText().equals(""))
    		fe.Direction = String.valueOf(Integer.parseInt(txtDirection.getText())); 		
    	if (cboStrength.getSelectionModel().getSelectedItem() != null)
    		fe.Strength = cboStrength.getSelectionModel().getSelectedItem().toString();
    	if (!txtComment.getText().equals(""))
    		fe.Comment = txtComment.getText();	
    	if (!txtMoreInfo.getText().equals(""))
    		fe.MoreInfo = txtMoreInfo.getText();	  	
    	if (!txtParent.getText().equals(""))
    		fe.Parent = txtParent.getText();
    	if (cboRating.getSelectionModel().getSelectedItem() != null)
    		fe.Rating = cboRating.getSelectionModel().getSelectedItem().toString();
    	if (!txtEffectiveness.getText().equals(""))
    		fe.Effectiveness = txtEffectiveness.getText();	
    	if (!txtSignature.getText().equals(""))
    		fe.Signature = txtSignature.getText();	
    	if (!txtIFFSIF.getText().equals(""))
    		fe.IFFSIF = txtIFFSIF.getText();	
    	
    	if (this._FEAttribues == null)
    		this._FEAttribues = new HashMap<>(); 

    }
    
    /** 輸入Graphic之Attributes Map,顯示註記屬性內容於畫面 */
    public void setFEAttributes(Map<String, Object> labelSet) {
    	this._FEAttribues = labelSet;
    	
    	FEAttributes fe = new FEAttributes(false);
    	fe.setFEAttributes(labelSet);
    	
    	if (!fe.DateTimeValid.equals("")) {
    		LocalDateTime ldt = LocalDateTime.ofInstant(FEAttributes.getDateFromValid(fe.DateTimeValid).toInstant(), ZoneId.systemDefault());
    		dtpDateTime.setDateTimeValue(ldt);
    	}
    	else {
    		dtpDateTime.setValue(null);
    	}
    	txtAltDepth.setText(fe.AltDepth);
    	txtLocation.setText(fe.Location);
    	txtType.setText(fe.Type);
    	txtName.setText(fe.Uniquedesignation);
    	txtSpeed.setText(fe.Speed);
    	txtQuantity.setText(fe.Quantity);
    	txtHQ.setText(fe.HQ);	
    	txtDirection.setText(fe.Direction);
    	if (!fe.Strength.equals("")) {
    		cboStrength.getSelectionModel().select(fe.Strength);
    	}
    	txtComment.setText(fe.Comment);	
    	txtMoreInfo.setText(fe.MoreInfo);	
    	txtParent.setText(fe.Parent);	
    	if (!fe.Rating.equals("")) {
    		cboRating.getSelectionModel().select(fe.Rating);
    	}
    	txtEffectiveness.setText(fe.Effectiveness);	
    	txtSignature.setText(fe.Signature);	
    	txtIFFSIF.setText(fe.IFFSIF);	
    }
    
    /**
     * 取得註記屬性
     * @return
     */
	public Map<String, Object> getFEAttributes() {
		return _FEAttribues;
	}
    
	/** 初始化下拉選單資料 */
	private void initData() {
		_Strength.put("+", "+");
		_Strength.put("-", "-");
		_Strength.put("+/-", "+/-");
		_Strength.put("(+)", "(+)");
		_Strength.put("(-)", "(-)");
		_Strength.put("(+/-)", "(+/-)");
		
		_Rating.put("A1","A1");
		_Rating.put("A2","A2");
		_Rating.put("A3","A3");
		_Rating.put("A4","A4");
		_Rating.put("A5","A5");
		_Rating.put("A6","A6");
		_Rating.put("B1","B1");
		_Rating.put("B2","B2");
		_Rating.put("B3","B3");
		_Rating.put("B4","B4");
		_Rating.put("B5","B5");
		_Rating.put("B6","B6");
		_Rating.put("C1","C1");
		_Rating.put("C2","C2");
		_Rating.put("C3","C3");
		_Rating.put("C4","C4");
		_Rating.put("C5","C5");
		_Rating.put("C6","C6");
		_Rating.put("D1","D1");
		_Rating.put("D2","D2");
		_Rating.put("D3","D3");
		_Rating.put("D4","D4");
		_Rating.put("D5","D5");
		_Rating.put("D6","D6");
		_Rating.put("E1","E1");
		_Rating.put("E2","E2");
		_Rating.put("E3","E3");
		_Rating.put("E4","E4");
		_Rating.put("E5","E5");
		_Rating.put("E6","E6");
		_Rating.put("F1","F1");
		_Rating.put("F2","F2");
		_Rating.put("F3","F3");
		_Rating.put("F4","F4");
		_Rating.put("F5","F5");
		_Rating.put("F6","F6");		
	}

	/** 初始化下拉選單 */
	@SuppressWarnings("unchecked")
	private void initComponent() {
		_Strength.addListener(new MapChangeListener<String, String>() {
			@Override
			public void onChanged(Change<? extends String, ? extends String> change) {
				if (change.wasAdded()) {
					cboStrength.getItems().add(change.getValueAdded());
				} else if (change.wasRemoved()) {
					cboStrength.getItems().remove(change.getValueRemoved());
				}
			}
		});
		
		_Rating.addListener(new MapChangeListener<String, String>() {
			@Override
			public void onChanged(Change<? extends String, ? extends String> change) {
				if (change.wasAdded()) {
					cboRating.getItems().add(change.getValueAdded());
				} else if (change.wasRemoved()) {
					cboRating.getItems().remove(change.getValueRemoved());
				}
			}
		});
		
		// force the field to be numeric only
		txtSpeed.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	txtSpeed.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		
		txtDirection.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	txtDirection.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		initComponent();
		initData();
		dictionarySymbol = MapManager.initDictionarySymbol();
		setSIDCImage(imgViewSymbol, _SIC);
	}

	/**
	 * 設定15碼
	 * @param SIC
	 */
	public void SetSIC(String SIC) {
		setSIDCImage(imgViewSymbol, SIC);
	}
	
	/**
	 * 指定SIDC內碼
	 * @param sic
	 */
	public void setSIDCImage(ImageView imgView, String sic) {

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("sidc", sic);

		try {
			ListenableFuture<Symbol> symbolFuture = dictionarySymbol.getSymbolAsync(attributes);
			symbolFuture.addDoneListener(() -> {
				try {
					Symbol symbol = symbolFuture.get();
					if (symbol == null) {
						return;
					}

					ListenableFuture<Image> imgFuture = symbol
							.createSwatchAsync(150 , 150, 1.0f, ColorUtil.colorToArgb(Color.TRANSPARENT));
					imgFuture.addDoneListener(() -> {
						try {
							Image img = imgFuture.get();
							imgView.setImage(img);
						} catch (ExecutionException | InterruptedException e) {
							e.printStackTrace();
						}
					});

				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
