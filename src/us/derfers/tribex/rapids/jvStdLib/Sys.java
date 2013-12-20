package us.derfers.tribex.rapids.jvStdLib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import us.derfers.tribex.rapids.Globals;
import us.derfers.tribex.rapids.Main;
import us.derfers.tribex.rapids.Utilities;

public class Sys {

	//TODO: Document properly.
	//Method for adding jars to classpath before execution
	public static void addJarToClasspath(String fileString) {
		try {
			File file = new File(Utilities.getJarDirectory()+fileString);
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
			method.setAccessible(true);
			method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{file.toURI().toURL()});
		} catch (NoSuchMethodException e) {
			Utilities.showError("Error adding Jar to Classpath.  Are you not using a standard JRE?");
		} catch (SecurityException e) {
			Utilities.showError("Error adding Jar to Classpath.  Security Error");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			Utilities.showError("Error adding Jar to Classpath.  Security: Illegal Access.");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			Utilities.showError("Error adding Jar to Classpath.  Illegal argument");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Utilities.showError("Error adding Jar to Classpath.  Unable to load jar, improperly packaged?");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Utilities.showError("Error adding Jar to Classpath.  Bad Path");
			e.printStackTrace();
		}
	}


	//Method for running Javascript from a file.
	public static void importJS(String fileString) {
		try {
			//Load file from root directory.
			if (fileString.startsWith(System.getProperty("path.separator")) || fileString.startsWith("C:\\")) {
				Main.loader.engine.eval(new FileReader(fileString));
			
			//Load file from home directory. DOESNT WORK ON WINDOWS VISTA/7
			} else if (fileString.startsWith("~/")) {
				System.out.println(System.getProperty("user.home")+fileString.replace("~", ""));
				Main.loader.engine.eval(new FileReader(System.getProperty("user.home")+fileString.replace("~", "")));
			
			//Load file from jar directory.
			} else {
				System.out.println(Globals.selCWD);
				Main.loader.engine.eval(new FileReader(Globals.selCWD+fileString));
			}
		} catch (FileNotFoundException e) {
			Utilities.showError("File does not exist: '" +fileString+"'\n\n"
					+ "Unexpected program behavior may result.");
		}
	}
}