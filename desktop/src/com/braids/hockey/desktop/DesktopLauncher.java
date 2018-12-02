package com.braids.hockey.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.braids.hockey.GameInstance;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "HOCKEY LUMBERJACK";
		config.width = 800;
		config.height = 480;
		new LwjglApplication(new GameInstance(), config);
	}
}
