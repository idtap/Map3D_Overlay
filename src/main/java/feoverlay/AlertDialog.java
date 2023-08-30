package feoverlay;
import java.util.Optional;

import controller.RibbonFormController;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class AlertDialog {
	
	public static void informationAlert(Window stage, String strMessage, boolean isWait) {
		showAlertDialog(stage, AlertType.INFORMATION, strMessage, isWait);
	}
	
	public static void informationAlert(String strMessage, boolean isWait) {
		showAlertDialog(null, AlertType.INFORMATION, strMessage, isWait);
	}
	
	public static void errorAlert(Window stage, String strMessage, boolean isWait){
		showAlertDialog(stage, AlertType.ERROR, strMessage, isWait);
	}
	
	public static void errorAlert(String strMessage, boolean isWait){
		showAlertDialog(null, AlertType.ERROR, strMessage, isWait);
	}

	public static void warningAlert(Window stage, String strMessage, boolean isWait){
		showAlertDialog(stage, AlertType.WARNING, strMessage, isWait);
	}
	
	public static void warningAlert(String strMessage, boolean isWait){
		showAlertDialog(null, AlertType.WARNING, strMessage, isWait);
	}
	
	public static void showAlertDialog(Window stage, AlertType alertType, String strMessage, boolean isWait){
		Alert alert = new Alert(alertType);
		DialogPane dialog = alert.getDialogPane();
		dialog.getStylesheets().add(Functions.appStylesheet);
		alert.initModality(Modality.APPLICATION_MODAL);;
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setContentText(strMessage);
		alert.initStyle(StageStyle.UNDECORATED);
		if (null != stage)
			alert.initOwner(stage);
		else if (null != Functions.primaryStage)
			alert.initOwner(Functions.primaryStage);
		// set Button text if Button Text is not empty
		Button buttonOK    = (Button)dialog.lookupButton(ButtonType.OK);	
		if(buttonOK!=null)
			buttonOK.setText("確認");
		Button buttonCancel = (Button)dialog.lookupButton(ButtonType.CANCEL);
		if(buttonCancel!=null)
			buttonCancel.setText("取消");
		
		if (isWait)
			alert.showAndWait();
		else
			alert.show();
	}
	
	public static Optional<ButtonType> confirmationAlert(Window stage, String strHeader, String strMessage){
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		DialogPane dialog = alert.getDialogPane();
		dialog.getStylesheets().add(Functions.appStylesheet);
		alert.initModality(Modality.APPLICATION_MODAL);;
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setHeaderText(strHeader);
		alert.setContentText(strMessage);
		alert.initStyle(StageStyle.UNDECORATED);
		if (null != stage)
			alert.initOwner(stage);
		else if (null != Functions.primaryStage)
			alert.initOwner(Functions.primaryStage);
		// set Button text if Button Text is not empty
		Button buttonOK    = (Button)dialog.lookupButton(ButtonType.OK);	
		if(buttonOK!=null)
			buttonOK.setText("確認");
		Button buttonCancel = (Button)dialog.lookupButton(ButtonType.CANCEL);
		if(buttonCancel!=null)
			buttonCancel.setText("取消");

		Optional<ButtonType> result = alert.showAndWait();
		return result;
	} 

	public static Optional<ButtonType> confirmationAlert(String strHeader, String strMessage) {
		return confirmationAlert(null, strHeader, strMessage);
	}
	
	
	/**
	 * 
	 * @param stage
	 * @param title
	 * @param message
	 * @param isWait
	 */
	public static void informationAlert(Window stage, String title, String message, boolean isWait){
		showAlertDialog(stage, AlertType.INFORMATION, title, null, message, isWait);
	}
	/**
	 * 
	 * @param stage
	 * @param title
	 * @param message
	 * @param isWait
	 */
	public static void errorAlert(Window stage, String title, String message, boolean isWait){
		showAlertDialog(stage, AlertType.ERROR, title, null, message, isWait);
	}
	/**
	 * 
	 * @param stage
	 * @param title
	 * @param message
	 * @param isWait
	 */
	public static void wARNINGAlert(Window stage, String title, String message, boolean isWait){
		showAlertDialog(stage, AlertType.WARNING, title, null, message, isWait);
	}	
	/**
	 * 
	 * @param stage
	 * @param alertType
	 * @param title
	 * @param header
	 * @param message
	 * @param isWait
	 */
	public static void showAlertDialog(Window stage, AlertType alertType, String title, String header, String message, boolean isWait){
		Alert alert = new Alert(alertType);
		DialogPane dialog = alert.getDialogPane();
		dialog.getStylesheets().add(Functions.appStylesheet);
		alert.initModality(Modality.APPLICATION_MODAL);;
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		if (header == null || header.isEmpty())
			dialog.setGraphic(null);	// remove dialog icon		
		//alert.initStyle(StageStyle.UNDECORATED);
		
		if (null != stage)
			alert.initOwner(stage);
		else if (null != Functions.primaryStage)
			alert.initOwner(Functions.primaryStage);
		// set Button text if Button Text is not empty
		Button buttonOK    = (Button)dialog.lookupButton(ButtonType.OK);	
		if(buttonOK!=null)
			buttonOK.setText("確認");
		Button buttonCancel = (Button)dialog.lookupButton(ButtonType.CANCEL);
		if(buttonCancel!=null)
			buttonCancel.setText("取消");
		
		if (isWait)
			alert.showAndWait();
		else
			alert.show();
	}
	
	/**
	 * 
	 * @param stage
	 * @param alertType
	 * @param Title
	 * @param strMessage
	 * @param isWait
	 */
	public static void showAlertDialog(Window stage, AlertType alertType, String title, String message, boolean isWait){
		showAlertDialog(stage, alertType, title, null, message, isWait);
	}	
	
	/**
	 * 顯示單行輸入dialog
	 * @param stage				父視窗 (null為預設PrimaryStage)
	 * @param title				dialog視窗標題
	 * @param header			dialog內容標頭 (包含icon部分)
	 * @param inputDefalutValue	input TextField 提示說明
	 * @param defalutValue		input TextField 顯示預設值
	 * @param buttonOKText		設定OK Button顯示文字
	 * @param buttonCancelText	設定Cancel Button顯示文字
	 * @return Optional&lt;String&gt;
	 */
	public static Optional<String> showTextInputDialog (Window stage, String title, String header, String inputLabelTip, String inputDefalutValue, String buttonOKText, String buttonCancelText) {
		TextInputDialog dialog = new TextInputDialog(inputDefalutValue.isEmpty() ? "新增名稱" : inputDefalutValue);
		
		// default initialization
		dialog.initModality(Modality.APPLICATION_MODAL);;
		if (null != stage)
			dialog.initOwner(stage);
		else if (null != Functions.primaryStage)
			dialog.initOwner(Functions.primaryStage);
		
		
		dialog.setTitle(title);
		dialog.setContentText(inputLabelTip);
		dialog.setHeaderText(header);
		if (header == null || header.isEmpty())			
			dialog.setGraphic(null);	// remove dialog icon
		
		// pane settings
		DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(Functions.appStylesheet);
		dialogPane.setMinWidth(Region.USE_PREF_SIZE);
		dialogPane.setMinHeight(Region.USE_PREF_SIZE);
		
		
		// set Button text if Button Text is not empty
		Button buttonOK    = (Button)dialogPane.lookupButton(ButtonType.OK);
		Button buttonCacel = (Button)dialogPane.lookupButton(ButtonType.CANCEL);		
		if (buttonOKText != null)
			buttonOK.setText(buttonOKText);
		if (buttonCancelText != null)
			buttonCacel.setText(buttonCancelText);
		
		
		// check if text is empty, if empty, disable OK button
		TextField inputField = dialog.getEditor();
		inputField.textProperty().addListener((observable, oldValue, newValue) -> {
			buttonOK.setDisable(newValue.isEmpty());
		});
		
		
		Optional<String> result = dialog.showAndWait();		
		return result;
	}
}	
