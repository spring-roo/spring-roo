package org.springframework.roo.project;

import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Immutable representation of an configuration specification for a (maven) build plugin
 * 
 * @author Alan Stewart
 * @since 1.1
 */
public final class Configuration {
	private final Element configuration;
	private final String configAsString;

	public Configuration(Element configuration) {
		Assert.notNull(configuration, "configuration must be specified");
		this.configuration = configuration;
		this.configAsString = XmlUtils.nodeToString(configuration);
	}

	public Element getConfiguration() {
		return configuration;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configAsString == null) ? 0 : configAsString.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		return obj != null && obj instanceof Configuration && this.compareTo((Configuration) obj) == 0;
	}

	public int compareTo(Configuration o) {
		if (o == null) {
			throw new NullPointerException();
		}
		return configAsString.compareTo(o.configAsString);
	}
	
	public String toString() {
		return configAsString;
	}
}
