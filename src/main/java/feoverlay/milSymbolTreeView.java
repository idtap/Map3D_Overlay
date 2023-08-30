package feoverlay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class milSymbolTreeView {

	@FunctionalInterface
	public interface SymbolSelectedEventHandler {
		void invoke(String SIC, String Shape);
	}

	/** 選擇符號通知 */
	public static Event<SymbolSelectedEventHandler> SymbolSelectedEvent = new Event<SymbolSelectedEventHandler>();

	private void RaiseSymbolSelectedEvent(String SIC, String Shape) {
		// invoke all listeners:
		for (SymbolSelectedEventHandler listener : SymbolSelectedEvent.listeners()) {
			listener.invoke(SIC, Shape);
		}
	}
	
	@FunctionalInterface
	public interface SymbolAddFavoriteEventHandler {
		void invoke(String key, milSymbolCode attr);
	}
	
	/** 加入常用通知 */
	public static Event<SymbolAddFavoriteEventHandler> SymbolAddFavoriteEvent = new Event<SymbolAddFavoriteEventHandler>();

	private void RaiseSymbolAddFavoriteEvent(String key, milSymbolCode attr) {
		// invoke all listeners:
		for (SymbolAddFavoriteEventHandler listener : SymbolAddFavoriteEvent.listeners()) {
			listener.invoke(key, attr);
		}
	}
	
	
	/** TreeView root item */
	public FilterableTreeItem<String> rootXML;
	public TreeView<String> treeView;

	/** key:軍隊符號太空航跡衛星 , value:milSymbolCode  */
	public HashMap<String, milSymbolCode> milSymbol = new HashMap<String, milSymbolCode>();

	public FilterableTreeItem<String> selectedItem;
	
	private String limitShape = "";
	
	/**
	* 處理點選TreeItem事件
	**/
	public void handleSelectedEvent(FilterableTreeItem<String> selectedItem) {
		this.selectedItem = selectedItem;
		if (selectedItem == null || !selectedItem.isLeaf())
			return;

		String key = getNodeParentKey(selectedItem, "") + "-" + selectedItem.getValue();
		
		if (milSymbol.containsKey(key)) {
			milSymbolCode attr = milSymbol.get(key);
			if (!limitShape.equals("") && !limitShape.equals(attr.getShape()))
				AlertDialog.errorAlert("選錯圖形樣式!!", false);
			else
				RaiseSymbolSelectedEvent(attr.getCode(), attr.getShape());
		}
	}

	/**
	 * 讀取FE.xml(軍隊符號清單檔) 
	 */
	public FilterableTreeItem<String> readData() throws SAXException, ParserConfigurationException, IOException {
		InputStream in = getClass().getResourceAsStream("/xml/FE.xml");
		InputSource is = new InputSource(in);
		
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		TreeItemCreationContentHandler contentHandler = new TreeItemCreationContentHandler();

		// parse file using the content handler to create a FilterableTreeItem representation
		reader.setContentHandler(contentHandler);
		reader.parse(is);

		// use first child as root (the FilterableTreeItem initially created does not contain data
		// from the file)
		FilterableTreeItem<String> item = (FilterableTreeItem<String>)contentHandler.item.getChildren().get(0);
		contentHandler.item.getChildren().clear();
		return item;
	}

	ArrayList<String> codesLimit = null;

	/**
	 * 產生樹狀結構
	 */
	@SuppressWarnings("unchecked")
	private void drawTreeView() {
		try {
			rootXML = readData();
			rootXML.setExpanded(true);

			if (treeView == null) 
				treeView = new TreeView<>(rootXML);
			else
				treeView.setRoot(rootXML);
			treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
				@Override
				public void changed(@SuppressWarnings("rawtypes") ObservableValue observable, Object oldValue,
						Object newValue) {

					FilterableTreeItem<String> selectedItem = (FilterableTreeItem<String>) newValue;
					handleSelectedEvent(selectedItem);
				}
			});

			treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
				@Override
				public TreeCell<String> call(TreeView<String> p) {
					TreeCell<String> cell = new TreeCell<String>() {
						@Override
						public void updateItem(String value, boolean empty) {
							super.updateItem(value, empty);
							if (empty) {
								setText(null);
							} else {
								setText(value.toString());
							}
						}
					};
					
					ContextMenu cm = createContextMenu(cell);
					cell.setContextMenu(cm);
					return cell;
				}
			});
			
			
			treeView.setShowRoot(true);
			treeView.refresh();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 為透明圖群組清單建立子選單
	 */
	private ContextMenu createContextMenu(TreeCell<String> item) {
		ContextMenu cm = new ContextMenu();
		MenuItem openItem = new MenuItem("加入我的最愛");
		
		openItem.setOnAction(event -> {
			if (selectedItem == null || !selectedItem.isLeaf())
				return;

			String key = getNodeParentKey(selectedItem, "") + "-" + selectedItem.getValue();
			
			if (milSymbol.containsKey(key)) {
				milSymbolCode attr = milSymbol.get(key);
				RaiseSymbolAddFavoriteEvent(key, attr);
			}
		});
		cm.getItems().add(openItem);
		// other menu items...
		return cm;
	}
	
	public void setLimitShape(String Shape) {
		this.limitShape = Shape;
	}
	
	public void setBySidc(String sidc) {
		TreeItem selected = getTreeViewItem(rootXML, sidc);
		treeView.getSelectionModel().select(selected);
	}
	
	/**
	 *  取得TreeItem
	 */
	public TreeItem getTreeViewItem(TreeItem<String> root , String value) 
	{
	  if (root != null && root.getValue().equals(value))
	    return  root;

	  for (TreeItem<String> child : root.getChildren()){
	   TreeItem<String> s= getTreeViewItem(child, value);
	   if(s!=null)
	       return s;

	  }
	  return null;
	}
	
	public String getNodeParentKey(FilterableTreeItem item, String key) {
		if (item.getParent().getValue().toString().indexOf("MIL-STD") < 0) {
			return getNodeParentKey((FilterableTreeItem)item.getParent(), "-" + item.getParent().getValue() + key);
		} else {
			return key;
		}
	}
	
	public milSymbolTreeView() {
		drawTreeView();
	}

	private class TreeItemCreationContentHandler extends DefaultHandler {

		private FilterableTreeItem<String> item = new FilterableTreeItem<String>("");

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			// finish this node by going back to the parent
			this.item = (FilterableTreeItem<String>) this.item.getParent();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			// start a new node and use it as the current item
			String cname = attributes.getValue("text");
			FilterableTreeItem<String> item = new FilterableTreeItem<String>(cname);
			this.item.getInternalChildren().add(item);
			this.item = item;

			if (attributes.getLength() > 1) {
				String key = getNodeParentKey(item, "")  + "-" + cname;
				milSymbol.put(key, new milSymbolCode(cname, attributes.getValue("name"), attributes.getValue("shape")));
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			String s = String.valueOf(ch, start, length).trim();
			if (!s.isEmpty()) {
				// add text content as new child
				this.item.getInternalChildren().add(new FilterableTreeItem<String>(s));
			}
		}
		
		public String getNodeParentKey(FilterableTreeItem item, String key) {
			if (item.getParent().getValue().toString().indexOf("MIL-STD") < 0) {
				return getNodeParentKey((FilterableTreeItem)item.getParent(), "-" + item.getParent().getValue() + key);
			} else {
				return key;
			}
		}
	}

}
