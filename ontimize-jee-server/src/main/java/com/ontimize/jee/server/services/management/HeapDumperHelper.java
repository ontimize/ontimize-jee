package com.ontimize.jee.server.services.management;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.management.MBeanServer;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ontimize.jee.common.tools.ReflectionTools;

@Component
@Lazy(value = true)
public class HeapDumperHelper {

	// This is the name of the HotSpot Diagnostic MBean
	private static final String	HOTSPOT_BEAN_NAME	= "com.sun.management:type=HotSpotDiagnostic";

	// field to store the hotspot diagnostic MBean
	private volatile Object		hotspotMBean;

	public HeapDumperHelper() {
		super();
	}

	/**
	 * Call this method from your application whenever you want to dump the heap snapshot into a file.
	 *
	 * @param fileName
	 *            name of the heap dump file
	 * @param live
	 *            flag that tells whether to dump only the live objects
	 * @throws IOException
	 */
	public Path dumpHeap(boolean live) throws IOException {
		Path outFile = Files.createTempFile("dump", ".bin");
		Files.delete(outFile);
		// initialize hotspot diagnostic MBean
		this.initHotspotMBean();
		ReflectionTools.invoke(this.hotspotMBean, "dumpHeap", outFile.toString(), live);
		return outFile;
	}

	// initialize the hotspot diagnostic MBean field
	private void initHotspotMBean() {
		if (this.hotspotMBean == null) {
			synchronized (HeapDumperHelper.class) {
				if (this.hotspotMBean == null) {
					this.hotspotMBean = this.getHotspotMBean();
				}
			}
		}
	}

	// get the hotspot diagnostic MBean from the
	// platform MBean server
	private Object getHotspotMBean() {
		try {
			Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			Object bean = ManagementFactory.newPlatformMXBeanProxy(server, HeapDumperHelper.HOTSPOT_BEAN_NAME, clazz);
			return bean;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
	}

}