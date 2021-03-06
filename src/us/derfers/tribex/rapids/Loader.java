/*
    RapidS - Web style development for the desktop.
    Copyright (C) 2014 TribeX Software Development

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


package us.derfers.tribex.rapids;
import static us.derfers.tribex.rapids.Utilities.debugMsg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import us.derfers.tribex.rapids.GUI.Swing.GUI;
import us.derfers.tribex.rapids.jvCoreLib.Sys;

/**
 * Starts the JavaScript engine and begins loading XML for widgets and layout information.
 *
 * @author TribeX, Nateowami
 *
 */
public class Loader {
    //Javascript engine initialization
    /** The initial JavaScript engine */
    public ScriptEngine engine = new ScriptEngine();

    public GUI GUI = new GUI();


    /**
     * Where widget variables are stored.  Format: WIDGETID {WIDGETID {WIDGET}, class {CLASSNAME}, And so on for the rest of the parameters}
     */
    //public Map<String, Map<String, Object>> XMLObjects = new HashMap<String, Map<String, Object>>();

    /** Counts Taken ID's for ID-less widgets */
    public Integer XMLObjects__NO__ID = 0;

    /** The escaped compiled RSM file(s) */
    public String escapedFile = "";

    /**
     * The startup method. Starts the JavaScript engine and runs loadAll.
     * @param filePath The file to load initially.
     */
    public void startLoader(String filePath) {
        String fileEscaped = "";
        try {
            fileEscaped = Utilities.EscapeScriptTags(FileUtils.readFileToString(new File(filePath)));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //JavaScript Engine Initialization
        //---------------------------------------------------------------------------//
        //Start Engine:
        debugMsg("JavaScript Engine Started", 4);

        //Import standard functions. Runs before the file loads
        try {
            //Import the standard JavaScript library (Java section)
            engine.eval("importPackage(Packages.us.derfers.tribex.rapids.jvCoreLib);", "RapidS Loader: Line 92");

            debugMsg("Imported JavaScript Standard Library (Java-based)", 3);

        } catch (Exception e1) {
            e1.printStackTrace();
            Utilities.showError("Error initializing JavaScript engine, please make sure you have Java 6+\n\n"
                    + "If you do, please report this error:\n"+e1.getMessage());
            System.exit(1);
        }


        //XXX: Loader section :XXX\\
        //JAVASCRIPT INITIALIZATION:
        //Run JavaScript that must be run before preload. Mainly provides require() and similar routines.
        recursiveLoadJS(engine, "core/init");
        debugMsg("Imported JavaScript Initialization Library (Init)", 4);

        //PRELOAD:
        //Loop through the JavaScript standard library for JavaScript and import all .js files in the preload folder.
        recursiveLoadJS(engine, "core/preload");
        debugMsg("Imported JavaScript Standard Library (PreLoad)", 3);

        //LOAD:
        //Begin loading the XML file(s)
        debugMsg("Loading "+filePath+"", 2);
        loadAll(fileEscaped);

        //POSTLOAD:
        //Loop through the JavaScript standard library for JavaScript and import all .js files in the postload folder.
        recursiveLoadJS(engine, "core/postload");
        debugMsg("Imported JavaScript Standard Library (PostLoad)", 3);

        //Run the program.onload property to allow the program to run scripts after the GUI has loaded.
        engine.call("program.onload", (Object[]) Main.programArguments);

        //XXX: End loader :XXX\\
    }

    /**
     * Starts loading the GUI. Sets Swing look and feel, then loads the GUI using the GUI_Swing object.
     * @param escapedFile The content .rsm file to load UI elements from.
     * @param parent The (optional) parent Object, Eg, a JFrame or JPanel.
     * @param engine The JavaScript engine to pass to GUI_Swing
     */
    public void loadAll(String escapedFile) {

        //Attempt to load .rsm file filePath
        try {

            //Parse filePath
            Document doc = Utilities.XMLStringToDocument(escapedFile);

            //Stabilize parsed document
            doc.normalize();

            //Get body element
            NodeList mainNodeList = doc.getElementsByTagName("rsm");

            //Make sure there is only ONE body element
            if (mainNodeList.getLength() == 1) {
                debugMsg("Parsing Main Element", 4);
                //Get rsm Element
                Element mainElement = (Element) mainNodeList.item(0);

                debugMsg("Setting Theme", 4);
                //If the mainElement has the attribute "theme"
                if (mainElement.getAttributeNode("theme") != null) {
                    //Get the value of the attribute theme for the body element
                    Attr swing_Theme = mainElement.getAttributeNode("theme");

                    //See if the rsm file specifies a theme other than camo
                    if (swing_Theme != null && !swing_Theme.getNodeValue().equalsIgnoreCase("camo")) {
                        try {
                            //Split the theme into the jarfile and the classname (JARFILE.jar : com.stuff.stuff.theme)
                            String[] splitTheme = swing_Theme.getNodeValue().split(":");

                            System.out.println(splitTheme[0].trim()+"**"+splitTheme[1].trim());
                            //Attempt to dynamically load the specified jarfile
                            Sys.addJarToClasspath(Globals.getCWD(splitTheme[0].trim()));

                            //Attempt to set the look'n'feel to the theme specified by the file
                            UIManager.setLookAndFeel(splitTheme[1].trim());

                            debugMsg("Look and Feel set to '"+swing_Theme.getNodeValue()+"'.", 3);

                        } catch (Exception e) {
                            //If unable to set to .rsm's theme, use the system look'n'feel
                            try {
                                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                            } catch (Exception a) {
                                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                            }
                            Utilities.showError("Error loading Look and Feel Specified, Look and Feel set to System");
                            e.printStackTrace();

                        }
                    } else {
                        //If swing_Theme == camo or is not set, use the system look'n'feel
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception a) {
                            a.printStackTrace();
                            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                        }
                        debugMsg("Look and Feel (Swing) set to System", 3);
                    }
                } else {
                    //If swing_Theme == camo or is not set, use the system look'n'feel
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception a) {
                        a.printStackTrace();
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    }
                    debugMsg("Look and Feel (Swing) set to System", 3);

                }

                //Parse styles
                for (int i = 0; i < mainElement.getElementsByTagName("style").getLength(); i++) {
                    Element styleElement = (Element) mainElement.getElementsByTagName("style").item(i);
                    //Load all styles from the style tags
                    if (styleElement.getAttributeNode("href") != null) {
                        loadStyles(null, styleElement.getTextContent());
                    } else {
                        loadStyles(styleElement.getTextContent(), null);

                    }
                }

                //Parse links
                for (int i = 0; i < mainElement.getElementsByTagName("link").getLength(); i++) {
                    Element linkElement = (Element) mainElement.getElementsByTagName("link").item(i);
                    parseLinks(linkElement, engine);
                }

                //Parse JavaScript in <script> tags
                Main.loader.loadJS(escapedFile, engine);

                //Parse GUI
                for (int i = 0; i < mainElement.getElementsByTagName("window").getLength(); i++) {
                    GUI.loadWindow((Element) mainElement.getElementsByTagName("window").item(i), engine);
                }




            } else { //There was more than one body tag, or 0 body tags

                //Display Error and quit, as we cannot recover from an abnormally formatted file
                Utilities.showError("Error: More or less than one <rsm> tag in '"+escapedFile+"'.\n\n"
                        + "Please add ONE <rsm> tag to '"+escapedFile+"'.");
                System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Discovers any script tags in the document and sends them to JSIterator to be parsed.
     * @param escapedFile The escaped file containing script(s) to run.
     * @param engine the JavaScript engine to load the script tags into.
     * @return Boolean telling whether or not it completed
     */
    public boolean loadJS(String escapedFile, ScriptEngine engine) {
        Utilities.debugMsg("Loading JavaScript from <script> tags.");
        //XXX: JAVASCRIPT HANDLING SECTION :XXX\\
        try {
            //Parse filePath
            Document doc = Utilities.XMLStringToDocument(escapedFile);

            //Stabilize parsed document
            doc.normalize();
            //Execute anything in script tags. JavaScript
            //Load all <script> tags

            NodeList scriptNodes = doc.getElementsByTagName("script");

            JSIterator(scriptNodes, engine);

            return true;
            //XXX: END JAVASCRIPT HANDLING :XXX\\
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showError("Error loading Javascripts from '"+escapedFile+"'. Please check their validity.");
            return false;
        }
    }

    /**
     * Iterates through a nodelist of script tags and parses them into the JavaScript engine.
     * @param scriptNodes A NodeList of all script tags.
     * @param engine The engine to run the scripts in.
     */
    private void JSIterator(NodeList scriptNodes, ScriptEngine engine) {
        for (int i=0; i < scriptNodes.getLength(); i++) {
            //Get the specific <script> tag for this loop
            Node scriptNode = scriptNodes.item(i);

            //Load code in <script> tags
            if (scriptNode.getNodeName().equals("script")) {
                debugMsg("Loading Script tag: "+(i+1));
                //Run all the code inside the <script> tags
                try {
                    engine.eval(scriptNode.getTextContent(), "<script></script> element in rsm file.");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Loads all JavaScript files under 'folder' into 'engine'
     * @param engine The ScriptEngine to run all files in folder in.
     * @param folder The folder to load .js files from.
     */
    private static void recursiveLoadJS(ScriptEngine engine, String folder) {
        File dir = new File(Utilities.getJarDirectory()+"/"+folder);
        try {

            File [] fileList = dir.listFiles();

            Arrays.sort(fileList);

            for (File file : fileList) {
                if (file.isDirectory()) {
                    recursiveLoadJS(engine, folder+"/"+file.getName());
                } else if (file.toString().endsWith(".js")) {
                    engine.eval(new FileReader(new File(Utilities.getJarDirectory()+folder+"/"+file.getName())), folder+"/"+file.getName());
                    debugMsg("Imported JavaScript File: "+file.getName(), 4);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Loads scripts and styles from link tags.
     * @param linkElement The element from which to load the content from.
     * @param engine The ScriptEngine to run links to scripts in.
     * @return
     */
    private boolean parseLinks(Element linkElement, ScriptEngine engine) {

        //Check and see if the <link> tag contains a rel and href attribute
        if (linkElement.getAttribute("href") != null) {
            //If it links to a stylesheet
            if ((linkElement.getAttributeNode("rel") != null &&linkElement.getAttributeNode("rel").getTextContent().equals("stylesheet"))
                    || linkElement.getAttributeNode("href").getTextContent().endsWith(".css")) {

                //Check and see if the href and URL exist.
                if (linkElement.getAttributeNode("href").getNodeValue().contains("://")) {
                    try {
                        if (this.loadStyles(IOUtils.toString(new URL(linkElement.getAttributeNode("href").getNodeValue())), null) == false) {
                            Utilities.showError("Error: invalid file in link tag pointing to "+linkElement.getAttributeNode("href").getTextContent());
                            return false;
                        }

                        if (linkElement.getAttributeNode("cache") != null) {
                            try {
                                FileUtils.writeStringToFile(new File(Globals.getCWD(linkElement.getAttributeNode("cache").getTextContent())), IOUtils.toString(new URL(linkElement.getAttributeNode("href").getNodeValue())));
                            } catch (Exception e2) {
                                Utilities.showError("Error: unable to cache to file "+linkElement.getAttributeNode("cache").getTextContent());
                            }
                        }

                    } catch (Exception e) {
                        Utilities.showError("Unable to locate "+linkElement.getAttributeNode("href").getNodeValue());
                        //Attempt to load from the fallback file. (If tag and file exist)
                        if (linkElement.getAttributeNode("fallback") != null) {
                            if (this.loadStyles(null, Globals.getCWD(linkElement.getAttributeNode("fallback").getTextContent())) == false) {
                                Utilities.showError("Error: invalid file in fallback tag pointing to "+linkElement.getAttributeNode("fallback").getTextContent());
                                return false;
                            };
                        }
                    }
                    //Load from file
                } else {
                    if (this.loadStyles(null, Globals.getCWD(linkElement.getAttributeNode("href").getTextContent())) == false) {
                        Utilities.showError("Error: invalid file in link tag pointing to "+linkElement.getAttributeNode("href").getTextContent());
                        return false;
                    };
                }

                //If it links to a script
            } else if ((linkElement.getAttributeNode("rel") != null &&linkElement.getAttributeNode("rel").getTextContent().equals("script"))
                    || linkElement.getAttributeNode("href").getTextContent().endsWith(".js")) {

                //Check and see if the file exists
                if (linkElement.getAttributeNode("href").getNodeValue().contains("://")) {
                    //Run script in file
                    URL url;
                    try {
                        url = new URL(linkElement.getAttributeNode("href").getTextContent());

                        URLConnection connection = url.openConnection();
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);
                        engine.eval(new InputStreamReader(connection.getInputStream()), "Remote file: "+linkElement.getAttributeNode("href").getTextContent());

                        if (linkElement.getAttributeNode("cache") != null) {
                            try {
                                FileUtils.writeStringToFile(new File(Globals.getCWD(linkElement.getAttributeNode("cache").getTextContent())), IOUtils.toString(new URL(linkElement.getAttributeNode("href").getNodeValue())));
                            } catch (Exception e2) {
                                Utilities.showError("Error: unable to cache to file "+linkElement.getAttributeNode("cache").getTextContent());
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        //Attempt to load from the fallback file. (If tag and file exist)
                        if (linkElement.getAttributeNode("fallback") != null) {
                            try {
                                engine.eval(new java.io.FileReader(Globals.getCWD(linkElement.getAttributeNode("fallback").getTextContent())),
                                        linkElement.getAttributeNode("fallback").getTextContent());
                            } catch (Exception e2) {
                                Utilities.showError("Error: invalid file in fallback tag pointing to "+linkElement.getAttributeNode("fallback").getTextContent());
                            }
                        } else {
                            Utilities.showError("Unable to load "+linkElement.getAttributeNode("href").getTextContent()+". No fallback found.");
                        }

                    }

                } else {
                    try {
                        //Run script in file
                        engine.eval(new java.io.FileReader(Globals.getCWD(linkElement.getAttributeNode("href").getTextContent())),
                                linkElement.getAttributeNode("href").getTextContent());
                        return true;

                    } catch (FileNotFoundException e) {
                        Utilities.showError("Error: invalid file in link tag pointing to "+linkElement.getAttributeNode("href").getTextContent());
                        e.printStackTrace();
                        return false;
                    } catch (DOMException e) {
                        Utilities.showError("Error: Improperly formatted XML");
                        e.printStackTrace();
                        return false;
                    } catch (Exception e) {
                        Utilities.showError("Error: invalid script in file "+linkElement.getAttributeNode("href").getTextContent());
                        e.printStackTrace();
                        return false;
                    }
                }
            } else {
                //Attempt to load as a .rsm file

                //If the file is a URL on the internet:
                if (linkElement.getAttributeNode("href").getNodeValue().contains("://")) {
                    try {
                        //Load the file from the internet.
                        Main.loader.loadAll(Utilities.EscapeScriptTags(IOUtils.toString(new URL(linkElement.getAttributeNode("href").getNodeValue()))));

                        if (linkElement.getAttributeNode("cache") != null) {
                            try {
                                FileUtils.writeStringToFile(new File(Globals.getCWD(linkElement.getAttributeNode("cache").getTextContent())), IOUtils.toString(new URL(linkElement.getAttributeNode("href").getNodeValue())));
                            } catch (Exception e2) {
                                Utilities.showError("Error: unable to cache to file "+linkElement.getAttributeNode("cache").getTextContent());
                            }
                        }
                    } catch (Exception e) {
                        if (linkElement.getAttributeNode("fallback") != null) {
                            try {
                                Main.loader.loadAll(Utilities.EscapeScriptTags(FileUtils.readFileToString(new File(Globals.getCWD(linkElement.getAttributeNode("fallback").getNodeValue())))));
                            } catch (Exception e2) {
                                Utilities.showError("Error: invalid file in fallback tag pointing to "+linkElement.getAttributeNode("fallback").getTextContent());
                            }
                        }
                    }

                    //Load the file from the Hard Drive
                } else {
                    try {
                        Main.loader.loadAll(Utilities.EscapeScriptTags(FileUtils.readFileToString(new File(Globals.getCWD(linkElement.getAttributeNode("href").getNodeValue())))));
                    } catch (DOMException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        } else {
            System.out.println(linkElement.toString());
            Utilities.showError("Warning: <link> tags must contain a href attribute and a rel attribute. Skipping tag.");
        }
        return false;
    }

    /**
     * Gets all styles from the String content, or file.
     * @param content String containing valid CSS, or null.
     * @param file String containing the path to a valid CSS file, or null.
     * @return True on success, false on failure.
     */
    private boolean loadStyles(String content, String file) {
        //If the user has specified loading from a string, not file
        if (file == null && content != null) {
            engine.call("css.parseString", content);
        } else if (content == null) {
            //Attempt to get the style information from the file
            try {
                //Load the file into the string toParse
                String toParse = FileUtils.readFileToString(new File(file));

                engine.call("css.parseString", toParse);
                return true;
            } catch (IOException e) {
                Utilities.showError("Error: Invalid CSS formatting in file: "+file);
                e.printStackTrace();
                return false;
            }


        }
        //In case something went wrong that was not caught
        return false;
    }
}
