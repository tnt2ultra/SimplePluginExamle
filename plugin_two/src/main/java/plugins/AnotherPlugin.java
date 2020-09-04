package plugins;

import main.Plugin;

public class AnotherPlugin implements Plugin {
	@Override
	public void initialize() // overrided to show user's home directory
	{
		System.out.println("User home directory: " + System.getProperty("user.home"));
	}
}