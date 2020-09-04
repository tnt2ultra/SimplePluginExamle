package main;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApplication extends Application {
	static HashSet<Plugin> plugins = new HashSet<Plugin>();
	VBox loadedPlugins = new VBox(6);
	File[] files;
	ArrayList<URL> urls;
	ArrayList<String> classes = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) throws MalformedURLException, IOException {
		if (getJarFiles()) {
			fillUrls();
			URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
			fillPlugins(urlClassLoader);
			printHeader();
			pluginsInitialize();
		} else {
			printMessage("Plugins not found");
		}
		showStage(primaryStage);
	}

	private void pluginsInitialize() {
		plugins.forEach(plugin -> {
			plugin.initialize();
			printMessage(plugin.name());
		});
	}

	private void printHeader() {
		if (!plugins.isEmpty()) {
			printMessage("Loaded plugins:");
		}
	}

	private void printMessage(String text) {
		loadedPlugins.getChildren().add(new Label(text));
	}

	private void showStage(Stage primaryStage) {
		loadedPlugins.setAlignment(Pos.CENTER);
		Rectangle2D screenbounds = Screen.getPrimary().getVisualBounds();
		Scene scene = new Scene(loadedPlugins, screenbounds.getWidth() / 2, screenbounds.getHeight() / 2);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void fillPlugins(URLClassLoader urlClassLoader) {
		classes.forEach(className -> {
			try {
				Class<?> cls = urlClassLoader.loadClass(className.replaceAll("/", ".").replace(".class", ""));
				Class<?>[] interfaces = cls.getInterfaces();
				for (Class<?> intface : interfaces) {
					if (intface.equals(Plugin.class)) {
						Plugin plugin = (Plugin) cls.newInstance();
						plugins.add(plugin);
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void fillUrls() throws IOException, MalformedURLException {
		urls = new ArrayList<>(files.length);
		for (File file : files) {
			JarFile jar = new JarFile(file);
			fillClasses(jar);
			URL url = file.toURI().toURL();
			urls.add(url);
			jar.close();
		}
	}

	private void fillClasses(JarFile jar) {
		jar.stream().forEach(jarEntry -> addClass(jarEntry));
	}

	private void addClass(JarEntry jarEntry) {
		if (jarEntry.getName().endsWith(".class")) {
			classes.add(jarEntry.getName());
		}
	}

	private boolean getJarFiles() {
		File pluginDirectory = new File("plugins");
		if (!pluginDirectory.exists()) {
			pluginDirectory.mkdir();
		}
		files = pluginDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
		return files != null && files.length > 0;
	}

	public static void main(String[] a) {
		launch(a);
	}
}
