package com.ontimize.jee.common.gui;

import com.ontimize.jee.core.common.dto.EntityResult;
import com.ontimize.jee.core.common.locator.ClientReferenceLocator;
import com.ontimize.jee.core.common.locator.EntityReferenceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.StringTokenizer;

public abstract class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    public static int MINIMAL_BYTES = 20000;

    protected static ConnectionOptimizer optimizer = null;

    protected static String internalIPs = null;

    protected static String serverHostname = null;

    protected static String serverInternalIP = null;

    protected static boolean automaticInternalNetworkDetection = true;

    protected static boolean inInternalNetwork = false;

    public static void setAutomaticInternalNetworkDetection(boolean enabled) {
        if (enabled) {
            ConnectionManager.logger.debug("Automatic intranet detection enabled");
        } else {
            ConnectionManager.logger.debug("Automatic intranet detection disabled");
        }
        ConnectionManager.automaticInternalNetworkDetection = enabled;
    }

    public static void setConnectionOptimizer(ConnectionOptimizer opt) {
        ConnectionManager.optimizer = opt;
    }

    public static void setFQDNConversion(String hostnameServid, String ipsIntern, String ipInternaServid) {
        ConnectionManager.logger.debug("FQDN conversion established: {} , {} , {}", hostnameServid, ipsIntern,
                ipInternaServid);
        ConnectionManager.serverHostname = hostnameServid;
        ConnectionManager.internalIPs = ipsIntern;
        ConnectionManager.serverInternalIP = ipInternaServid;
    }

    public static void setInInternalNetwork(boolean in) {
        ConnectionManager.logger.debug("internal Network set to : {}", in);
        ConnectionManager.inInternalNetwork = in;
    }

    public static String getServerInternalIP() {
        return ConnectionManager.serverInternalIP;
    }

    public static String getServerHostname() {
        return ConnectionManager.serverHostname;
    }

    public static String resolveHostname(String host) {
        if (!ConnectionManager.automaticInternalNetworkDetection) {
            String res = ConnectionManager.inInternalNetwork ? ConnectionManager.serverInternalIP : host;
            ConnectionManager.logger.debug("Resolving {} to: {}", host, res);
            return res;
        }
        // If parameters are specified and the source host is inside the
        // internal
        // network then convert
        if ((ConnectionManager.serverInternalIP != null) && (ConnectionManager.internalIPs != null)) {
            // Checks:
            if (host.equalsIgnoreCase(ConnectionManager.serverHostname)) {
                boolean isInternalNetword = ConnectionManager.checkInternalNetwork();
                if (isInternalNetword) {
                    ConnectionManager.logger.debug("Resolving {} to: {}", host, ConnectionManager.serverInternalIP);
                    return ConnectionManager.serverInternalIP;
                } else {
                    ConnectionManager.logger.debug("Resolving {} to: {}", host, host);
                    return host;
                }
            } else {
                ConnectionManager.logger.debug("Resolving {} to: {}", host, host);
                return host;
            }
        } else {
            ConnectionManager.logger.debug("Resolving {} to: {}", host, host);
            return host;
        }
    }

    public static boolean checkInternalNetwork() {
        long t = System.currentTimeMillis();
        // If parameters are specified then return true if the host in in the
        // internal network
        if ((ConnectionManager.serverInternalIP != null) && (ConnectionManager.internalIPs != null)
                && (ConnectionManager.serverHostname != null)) {
            // First of all look the local ip.
            try {
                ConnectionManager.logger.debug("Checking intranet membership");
                InetAddress local = InetAddress.getLocalHost();
                String sLocalIP = local.getHostAddress();
                int[] localIPNumbers = new int[4];
                int i = 0;
                StringTokenizer st = new StringTokenizer(sLocalIP, ".");
                while (st.hasMoreTokens()) {
                    try {
                        localIPNumbers[i] = Integer.parseInt(st.nextToken());
                    } catch (Exception e) {
                        ConnectionManager.logger.error("Error in local IP: {}", sLocalIP, e);
                        return false;
                    }
                    i++;
                }
                // Now the string that specifies the internal IPs
                int slayerIndex = ConnectionManager.internalIPs.indexOf("/");
                if (slayerIndex < 4) {
                    ConnectionManager.logger.error("Error in string ipinternas: {}", ConnectionManager.internalIPs);
                    return false;
                }
                String prefixLength = ConnectionManager.internalIPs.substring(slayerIndex + 1);
                String sBaseIP = ConnectionManager.internalIPs.substring(0, slayerIndex);
                // Now the numbers
                st = new StringTokenizer(sBaseIP, ".");
                int[] sBaseIPNumbers = new int[4];
                i = 0;
                while (st.hasMoreTokens()) {
                    try {
                        sBaseIPNumbers[i] = Integer.parseInt(st.nextToken());
                    } catch (Exception e) {
                        ConnectionManager.logger.error("Error in base address: {}", sBaseIP, e);
                        return false;
                    }
                    i++;
                }
                int maskLength = 32;
                try {
                    maskLength = Integer.parseInt(prefixLength);
                } catch (Exception e) {
                    ConnectionManager.logger.error("Error in IP base address: {}", sBaseIP, e);
                    return false;
                }
                // Craate the binary strings to compare
                StringBuilder localBinaryIP = new StringBuilder();
                for (i = 0; i < localIPNumbers.length; i++) {
                    String bin = Integer.toBinaryString(localIPNumbers[i]);
                    String bin8 = bin.substring(Math.max(0, bin.length() - 8), bin.length());
                    while (bin8.length() < 8) {
                        bin8 = "0" + bin8;
                    }
                    localBinaryIP.append(bin8);
                }
                StringBuilder internalBinayIPs = new StringBuilder();
                for (i = 0; i < sBaseIPNumbers.length; i++) {
                    String bin = Integer.toBinaryString(sBaseIPNumbers[i]);
                    String bin8 = bin.substring(Math.max(0, bin.length() - 8), bin.length());
                    while (bin8.length() < 8) {
                        bin8 = "0" + bin8;
                    }
                    internalBinayIPs.append(bin8);
                }
                // Compare until the mask length
                String loc = localBinaryIP.substring(0, maskLength);
                String intern = internalBinayIPs.substring(0, maskLength);
                long t2 = System.currentTimeMillis();
                ConnectionManager.logger.debug(
                        "Local IP: {} . Mask length: {} . Binary Local IP: {} Binary Internal IP: {} , time: {}",
                        sLocalIP, maskLength, localBinaryIP,
                        internalBinayIPs, t2 - t);
                if (loc.equals(intern)) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                ConnectionManager.logger.trace(null, e);
                return false;
            }
        } else {
            return false;
        }
    }

    public static ConnectionOptimizer getConnectionOptimizer() {
        return ConnectionManager.optimizer;
    }

    public static int getCompresionThreshold(int bytesNumber, long transmisionTime) {
        if (bytesNumber < ConnectionManager.MINIMAL_BYTES) {
            return -1;
        }
        if (transmisionTime == 0) {
            transmisionTime = 1;
        }
        double bytesPS = bytesNumber / (transmisionTime / 1000.0);

        // // ERROR: this condition is wrong; bytesNumber can be uncompressed, so its value compressed would
        // be much lower
        // if (bytesPS > (bytesNumber / 3.0)) {
        // return -1;
        // }
        int i0 = 20000;
        int i1 = 100 * 1024;
        int i2 = 500 * 1024;
        int i3 = 1000 * 1024;
        int threshold = -1;
        if (bytesPS >= i3) {
            threshold = 2097152;
        } else if ((i2 <= bytesPS) && (bytesPS < i3)) {
            threshold = (int) ((int) bytesPS / 1.5);
        } else if ((i1 <= bytesPS) && (bytesPS < i2)) {
            threshold = (int) bytesPS / 4;
        } else if ((i0 <= bytesPS) && (bytesPS < i1)) {
            threshold = (int) bytesPS / 2;
        } else if (bytesPS < i0) {
            threshold = (int) bytesPS / 5;
        }

        StringBuilder sb = new StringBuilder("Network rate : ");
        sb.append(bytesPS);
        sb.append(" bytes/s ");
        sb.append(bytesNumber);
        sb.append(" / ");
        sb.append(transmisionTime / 1000.0);
        sb.append(" . Threshold = ");
        sb.append(threshold);
        ConnectionManager.logger.debug(sb.toString());

        return threshold;
    }

    public static void checkEntityResult(EntityResult res, EntityReferenceLocator locator) {
        if ((res != null) && (EntityResult.OPERATION_WRONG != res.getCode())) {

            // Test the net speed
            int compressionThreshold = ConnectionManager.getCompresionThreshold(res.getBytesNumber(),
                    res.getStreamTime());
            if (compressionThreshold > 0) {
                ConnectionOptimizer opt = ConnectionManager.getConnectionOptimizer();
                if ((opt != null) && (locator instanceof ClientReferenceLocator)) {
                    try {
                        opt.setDataCompressionThreshold(((ClientReferenceLocator) locator).getUser(),
                                locator.getSessionId(), compressionThreshold);
                        ConnectionManager.logger.debug("Compression threshold has been established for {} {} in : {}",
                                ((ClientReferenceLocator) locator).getUser(),
                                locator.getSessionId(), compressionThreshold);
                    } catch (Exception e) {
                        ConnectionManager.logger.error("Error establishing compression threshold", e);
                    }
                }
            }
        }
    }

}
