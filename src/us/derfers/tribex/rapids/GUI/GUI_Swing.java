package us.derfers.tribex.rapids.GUI;

import static us.derfers.tribex.rapids.Utilities.debugMsg;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import us.derfers.tribex.rapids.Loader;
import us.derfers.tribex.rapids.Main;
import us.derfers.tribex.rapids.Utilities;
import us.derfers.tribex.rapids.parsers.CSSParser;

public class GUI_Swing {
	
	//Constants that need to be defined to keep the GUI in one frame system.
	private JFrame window = new JFrame();
	private JPanel windowPanel = new JPanel();
	
	//Stylesheet information
	private Map<String, Map<String, String>> stylesMap = new HashMap<String, Map<String, String>>();
	
	public void loadGUI(String filePath, ScriptEngine engine, Object parent, Boolean clearWidgets) {
		//XXX: Initialization :XXX\\
		/*
		 * Basically: Port the following code to Swing, nothing hard. :P   
		*/
		//If this is the initial run, and therefore there is no shell or display, initialize them
		
		//Create ParentComposite variable
		JPanel parentComposite = null;
		
		//Check to see if the parent exists
		if (parent == null || parent.getClass().equals(JFrame.class)) {
			//If it does not exist, add the the default panel to the window
			window.getContentPane().add(windowPanel);
			
			//Set the parentComposite to the windowPanel
			parentComposite = windowPanel;
		} else {
			//Set the parentComposite to the Parent
			parentComposite = (JPanel) parent;
		}
		
		//TODO: Flexible layout types
		//Create a gridbaglayout
		windowPanel.setLayout(new GridBagLayout());

		try {

			//XML File Loading
			File file = new File(filePath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();

			//XXX: HEAD :XXX\\
			//Get Window information from the Head Element
			NodeList headElementList = doc.getElementsByTagName("head").item(0).getChildNodes();

			//Loop through the children of the Head element
			for (int counter=0; counter < headElementList.getLength(); counter++) {
				
				Node headNode = headElementList.item(counter);
				
				//Make sure we have real element nodes
				if (headNode.getNodeType() == Node.ELEMENT_NODE) {
					final Element headElement = (Element) headNode;
					//Set the shell title with the title or window_title node
					if (headElement.getNodeName().equals("title") || headElement.getNodeName().equals("window_title")) {
						window.setTitle(headElement.getTextContent());
					
					//Parse style information in the header
					} else if (headElement.getNodeName().equals("style")) {
						//Load all styles from the style tags
						if (headElement.getAttributeNode("href") != null) {
							loadStyles(null, headElement.getTextContent());
						} else {
							loadStyles(headElement.getTextContent(), null);

						}
						
					} else if (headElement.getNodeName().equals("link")) {
						parseLinks(headElement, engine);
					}
				}
			}
			//XXX: BODY : XXX\\
			//Loop through all children of the body element and add them
			loadInComposite(parentComposite, doc.getElementsByTagName("body").item(0), engine); 
			
		} catch (Exception e) {
			e.printStackTrace();
			Utilities.showError("Unable to properly initialize a SWT GUI. \n"+filePath+" may be corrupt or incorrectly formatted.");
		}

		//Fit the window to the elements in it.
		window.pack();

		//Create a subMap for holding the Window and any properties of it
		Map<String, Object> shellMap = new HashMap<String, Object>();

		shellMap.put("__WINDOW__", window);

		Main.loader.XMLWidgets.put("__WINDOW__", shellMap);
		
		//Loading the JS must be done ABSOLUTELY LAST before the open call, or some properties will be missed.
		Main.loader.loadJS(filePath, engine);
		
		//Open the window
		window.setVisible(true);

		//XXX: END WIDGET CREATION :XXX\\	
	}
	
	//TODO: Comment
	private void loadInComposite(JPanel parentComposite, Node node, ScriptEngine engine) {
		GridBagConstraints widgetConstraint = new GridBagConstraints(){

			private static final long serialVersionUID = 1L;

			{
				fill = GridBagConstraints.HORIZONTAL;
				anchor = GridBagConstraints.LINE_START;
				ipadx = 5;
				ipadx = 5;
				weightx = 1.0;
				weighty = 1.0;
				gridx = 0;
				gridy = GridBagConstraints.RELATIVE;
			}
		};
			//XXX: BODY : XXX\\
		debugMsg(parentComposite.toString());

			//Get Widgets from the Body Element
			NodeList bodyElementList = node.getChildNodes();
			//Loop through all children of the root element.
			for (int counter=0; counter < bodyElementList.getLength(); counter++) {

				Node widgetNode = bodyElementList.item(counter);

				if (widgetNode.getNodeType() == Node.ELEMENT_NODE) {
					final Element widgetElement = (Element) widgetNode;

					//Load all link tags
					if (widgetElement.getNodeName().equals("link")) {
						if (widgetElement.getAttributeNode("href") != null) {
							Main.loader.loadAll((widgetElement.getAttributeNode("href").getNodeValue()), parentComposite, false, engine);

						} else {
							Utilities.showError("Warning: <link> tags must contain a href attribute.");
						}
					
					//JButtons
					} else if (widgetElement.getNodeName().equals("button")) {
						JButton widget = new JButton();
						
						parentComposite.add(widget, widgetConstraint);
						//Set button text with the content of the <button></button> tags
						widget.setText(widgetElement.getTextContent());

						
						//Iterate through listener types and set listeners if they exist
						for (String listenerType : Main.loader.listenerTypesArray) {
							//Add a listener for listenerType if specified
							if (widgetElement.getAttributeNode(listenerType) != null) {
								addMethodListener(listenerType, widget, widgetElement.getAttributeNode(listenerType).getNodeValue(), engine);
							}
						
						}
						Loader.addWidgetToMaps(widgetElement, widget, engine);

					} else if (widgetElement.getNodeName().equals("spinner")) {
						//SPINNER
						final Element spinnerElement = (Element) widgetNode;
						//Create a new composite for sub-elements
						SpinnerNumberModel model = new SpinnerNumberModel();

						if (spinnerElement.getAttributeNode("value") != null) {
							model.setValue(Integer.valueOf(spinnerElement.getAttributeNode("value").getNodeValue()));
						}

						if (spinnerElement.getAttributeNode("max") != null) {
							model.setMaximum(Integer.valueOf(spinnerElement.getAttributeNode("max").getNodeValue()));
						}

						if (spinnerElement.getAttributeNode("min") != null) {
							model.setMinimum(Integer.valueOf(spinnerElement.getAttributeNode("min").getNodeValue()));
						}
						
						//Add widget to maps
						JSpinner widget = new JSpinner(model);
						parentComposite.add(widget, widgetConstraint);

						Loader.addWidgetToMaps(spinnerElement, widget, engine);
					
					//LABEL CODE
					} else if (widgetElement.getNodeName().equals("label")) {
						final Element labelElement = (Element) widgetNode;

						JLabel widget = new JLabel();

						widget.setText(labelElement.getTextContent());

						parentComposite.add(widget, widgetConstraint);

						Loader.addWidgetToMaps(labelElement, widget, engine);
						for (String listenerType : Main.loader.listenerTypesArray) {
							//Add a listener for listenerType if specified
							if (widgetElement.getAttributeNode(listenerType) != null) {
								addMethodListener(listenerType, (Component) widget, widgetElement.getAttributeNode(listenerType).getNodeValue(), engine);
							}
						
						}

					}
				
				}

			}
			//position and draw the widgets
			//parentComposite.doLayout();
			//window.pack();

	}
	
	//Parse <link> tags
	private boolean parseLinks(Element linkElement, ScriptEngine engine) {
		
		//Check and see if the <link> tag contains a rel and href attribute
		if (linkElement.getAttributeNode("rel") != null && linkElement.getAttribute("href") != null) {
			//If it links to a stylesheet
			if (linkElement.getAttributeNode("rel").getTextContent().equals("stylesheet")) {
				
				//Check and see if the file exists
				if (this.loadStyles(null, linkElement.getAttributeNode("href").getTextContent()) == false) {
					Utilities.debugMsg("Error: invalid file in link tag pointing to "+linkElement.getAttributeNode("href").getTextContent());
					return false;
				};
				
			//If it links to a script
			} else if (linkElement.getAttributeNode("rel").getTextContent().equals("script")) {
				
				//Check and see if the file exists
				try {
					//Run script in file
					engine.eval(new java.io.FileReader(linkElement.getAttributeNode("href").getTextContent()));
					return true;
					
				} catch (FileNotFoundException e) {
					Utilities.debugMsg("Error: invalid file in link tag pointing to "+linkElement.getAttributeNode("href").getTextContent());
					e.printStackTrace();
					return false;
				} catch (DOMException e) {
					Utilities.debugMsg("Error: Improperly formatted XML");
					e.printStackTrace();
					return false;
				} catch (ScriptException e) {
					Utilities.debugMsg("Error: invalid script in file "+linkElement.getAttributeNode("href").getTextContent());
					e.printStackTrace();
					return false;
				}
			} else {
				//Attempt to load as a .rsm file
				Main.loader.loadAll((linkElement.getAttributeNode("href").getNodeValue()), 
						Main.loader.XMLWidgets.get("__WINDOW__").get("__WINDOW__"), false, engine);
			}
			
		} else {
			Utilities.showError("Warning: <link> tags must contain a href attribute and a rel attribute. Skipping tag.");
		}
		return false;
	}
	
	//Style loading method
	private boolean loadStyles(String content, String file) {
		//If the user has specified loading from a string, not file
		if (file == null) {
			//Create a new CSSParser
			CSSParser parser = new CSSParser(content);
			
			//Put all the content of the parsed CSS into the stylesMap
			stylesMap.putAll(parser.parseAll());
			return true;
		//If the user has specified loading from a file, not string
		} else if (content == null) {
			//Attempt to get the style information from the file
			try {
				//Load the file into the string toParse
				String toParse = FileUtils.readFileToString(new File(file));
				
				//Create a new CSSParser
				CSSParser parser = new CSSParser(toParse);
				
				//Put all the content of the parsed CSS into the stylesMap
				stylesMap.putAll(parser.parseAll());
			
				return true;
			} catch (IOException e) {
				Utilities.showError("Error: Invalid CSS formatting in file: "+file);
				return false;
			}


		}
		//In case something went wrong that was not caught
		return false;
	}
	private boolean addMethodListener(String type, Component widget, final String value, final ScriptEngine engine) {		
		//Add event listener
		try {
			if (type.equals("onclick")) {
				((JComponent) widget).addMouseListener(new MouseAdapter(){
					@Override
					public void mouseClicked(MouseEvent arg0) {
						try {
							engine.eval(value);
						} catch (ScriptException e1) {
							Utilities.showError("Bad JavaScript: "+value);
							e1.printStackTrace();
						}

					}

				});
			} else if (type.equals("onmouseover")) {
				((JComponent) widget).addMouseListener(new MouseAdapter(){
					@Override
					public void mouseEntered(MouseEvent arg0) {
						try {
							engine.eval(value);
						} catch (ScriptException e1) {
							Utilities.showError("Bad JavaScript: "+value);
							e1.printStackTrace();
						}

					}

				});
			} else if (type.equals("onmouseout")) {
				((JComponent) widget).addMouseListener(new MouseAdapter(){
					@Override
					public void mouseExited(MouseEvent arg0) {
						try {
							engine.eval(value);
						} catch (ScriptException e1) {
							Utilities.showError("Bad JavaScript: "+value);
							e1.printStackTrace();
						}

					}

				});
			} else if (type.equals("onmousedown")) {
				((JComponent) widget).addMouseListener(new MouseAdapter(){
					@Override
					public void mousePressed(MouseEvent arg0) {
						try {
							engine.eval(value);
						} catch (ScriptException e1) {
							Utilities.showError("Bad JavaScript: "+value);
							e1.printStackTrace();
						}

					}

				});
			} else if (type.equals("onmouseup")) {
				((JComponent) widget).addMouseListener(new MouseAdapter(){
					@Override
					public void mouseReleased(MouseEvent arg0) {
						try {
							engine.eval(value);
						} catch (ScriptException e1) {
							Utilities.showError("Bad JavaScript: "+value);
							e1.printStackTrace();
						}

					}

				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}