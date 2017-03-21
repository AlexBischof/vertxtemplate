package de.bischinger.vertx;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Version implements Serializable
{
	private static final String VERSION = "${project.version}";
	private static final String TIMESTAMP = "${timestamp}";

	public String getVersion()
	{
		return VERSION;
	}

	public String getTimestamp()
	{
		return TIMESTAMP;
	}

	@Override
	public String toString()
	{
		return VERSION + "-" + TIMESTAMP;
	}
}