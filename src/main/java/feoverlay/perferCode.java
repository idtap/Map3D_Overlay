package feoverlay;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class perferCode {

	private static ArrayList<String> codes = new ArrayList<>();

	private static class TreeItemCreationContentHandler extends DefaultHandler {
		private String code = "";
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			
			if (attributes.getLength() > 1) 
				code = attributes.getValue("name");
			else 
				code = "";
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			// This method is called every time the parser finds a closing tag
			if (!code.equals(""))
				codes.add(code);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// This method is called every time the parser offers us to read the element
			// value
		}
	}

}
