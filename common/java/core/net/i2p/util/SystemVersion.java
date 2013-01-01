package net.i2p.util;

/*
 * public domain
 */

import java.lang.reflect.Field;

/**
 * Methods to find out what system we are running on
 *
 * @since 0.9.3 consolidated from various places
 */
public abstract class SystemVersion {

    private static final boolean _isWin = System.getProperty("os.name").startsWith("Win");
    private static final boolean _isMac = System.getProperty("os.name").startsWith("Mac");
    private static final boolean _isAndroid;
    private static final boolean _isApache;
    private static final boolean _isGNU;
    private static final boolean _is64 = "64".equals(System.getProperty("sun.arch.data.model")) ||
                                         System.getProperty("os.arch").contains("64");
    private static final boolean _hasWrapper = System.getProperty("wrapper.version") != null;

    private static final boolean _oneDotSix;
    private static final int _androidSDK;

    static {
        String vendor = System.getProperty("java.vendor");
        _isAndroid = vendor.contains("Android");
        _isApache = vendor.startsWith("Apache");
        _isGNU = vendor.startsWith("GNU Classpath") ||               // JamVM
                 vendor.startsWith("Free Software Foundation");      // gij

        int sdk = 0;
        if (_isAndroid) {
            try {
                Class ver = Class.forName("android.os.Build.VERSION", true, ClassLoader.getSystemClassLoader());
                Field field = ver.getField("SDK_INT");
                sdk = field.getInt(null);
            } catch (Exception e) {}
        }
        _androidSDK = sdk;

        if (_isAndroid) {
            _oneDotSix = _androidSDK >= 9;
        } else {
            _oneDotSix = (new VersionComparator()).compare(System.getProperty("java.version"), "1.6") >= 0;
        }
    }

    public static boolean isWindows() {
        return _isWin;
    }

    public static boolean isMac() {
        return _isMac;
    }

    public static boolean isAndroid() {
        return _isAndroid;
    }

    /**
     *  Apache Harmony JVM, or Android
     */
    public static boolean isApache() {
        return _isApache || _isAndroid;
    }

    /**
     *  gij or JamVM with GNU Classpath
     */
    public static boolean isGNU() {
        return _isGNU;
    }

    /**
     *  Better than (new VersionComparator()).compare(System.getProperty("java.version"), "1.6") >= 0
     *  as it handles Android also, where java.version = "0".
     *
     *  @return true if Java 1.6 or higher, or Android API 9 or higher
     */
    public static boolean isJava6() {
        return _oneDotSix;
    }

    /**
     * This isn't always correct.
     * http://stackoverflow.com/questions/807263/how-do-i-detect-which-kind-of-jre-is-installed-32bit-vs-64bit
     * http://mark.koli.ch/2009/10/javas-osarch-system-property-is-the-bitness-of-the-jre-not-the-operating-system.html
     * http://mark.koli.ch/2009/10/reliably-checking-os-bitness-32-or-64-bit-on-windows-with-a-tiny-c-app.html
     * sun.arch.data.model not on all JVMs
     * sun.arch.data.model == 64 => 64 bit processor
     * sun.arch.data.model == 32 => A 32 bit JVM but could be either 32 or 64 bit processor or libs
     * os.arch contains "64" could be 32 or 64 bit libs
     */
    public static boolean is64Bit() {
        return _is64;
    }

    /**
     *  Identical to android.os.Build.VERSION.SDK_INT.
     *  For use outside of Android code.
     *  @return The SDK (API) version, e.g. 8 for Froyo, 0 if unknown
     */
    public static int getAndroidVersion() {
        return _androidSDK;
    }

    /**
     *  Is the wrapper present?
     *  Same as I2PAppContext.hasWrapper()
     */
    public static boolean hasWrapper() {
        return _hasWrapper;
    }
}
