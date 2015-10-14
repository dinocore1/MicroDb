package com.devsmart.microdb;

import com.devsmart.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public final class Loader {

    /**
     * Constant for OS X operating system.
     */
    private static final String OS_OSX = "osx";

    /**
     * Constant for OS X operating system.
     */
    private static final String OS_MACOSX = "macosx";

    /**
     * Constant for Linux operating system.
     */
    private static final String OS_LINUX = "linux";

    /**
     * Constant for Windows operating system.
     */
    private static final String OS_WINDOWS = "windows";

    /**
     * Constant for FreeBSD operating system.
     */
    private static final String OS_FREEBSD = "freebsd";

    /**
     * Constant for SunOS operating system.
     */
    private static final String OS_SUNOS = "sunos";

    /**
     * Constant for i386 architecture.
     */
    private static final String ARCH_I386 = "i386";

    /**
     * Constant for x86 architecture.
     */
    private static final String ARCH_X86 = "x86";

    /**
     * Constant for x86_64 architecture.
     */
    private static final String ARCH_X86_64 = "x86_64";

    /**
     * Constant for amd64 architecture.
     */
    private static final String ARCH_AMD64 = "amd64";

    /**
     * Constant for so file extension.
     */
    private static final String EXT_SO = "so";

    /**
     * Constant for dll file extension.
     */
    private static final String EXT_DLL = "dll";

    /**
     * Constant for dylib file extension.
     */
    private static final String EXT_DYLIB = "dylib";

    /**
     * The temporary directory for native libraries.
     */
    private static File tmp;

    /**
     * If library is already loaded.
     */
    private static boolean loaded = false;

    /**
     * Private constructor to prevent instantiation.
     */
    private Loader() {
        // Nothing to do here
    }

    /**
     * Returns the operating system name. This could be "linux", "windows" or
     * "osx" or (for any other non-supported platform) the value of the
     * "os.name" property converted to lower case and with removed space
     * characters.
     *
     * @return The operating system name.
     */
    private static String getOS() {
        final String os = System.getProperty("os.name").toLowerCase()
                .replace(" ", "");
        if (os.contains(OS_WINDOWS)) {
            return OS_WINDOWS;
        }
        if (os.equals(OS_MACOSX)) {
            return OS_OSX;
        }
        return os;
    }

    /**
     * Returns the CPU architecture. This will be "x86" or "x86_64" (Platform
     * names i386 und amd64 are converted accordingly) or (when platform is
     * unsupported) the value of os.arch converted to lower-case and with
     * removed space characters.
     *
     * @return The CPU architecture
     */
    private static String getArch() {
        final String arch = System.getProperty("os.arch").toLowerCase()
                .replace(" ", "");
        if (arch.equals(ARCH_I386)) {
            return ARCH_X86;
        }
        if (arch.equals(ARCH_AMD64)) {
            return ARCH_X86_64;
        }
        return arch;
    }

    /**
     * Returns the shared library extension name.
     *
     * @return The shared library extension name.
     */
    private static String getExt() {
        final String os = getOS();
        final String key = "microdb.libext." + getOS();
        final String ext = System.getProperty(key);
        if (ext != null) {
            return ext;
        }
        if (os.equals(OS_LINUX) || os.equals(OS_FREEBSD) || os.equals(OS_SUNOS)) {
            return EXT_SO;
        }
        if (os.equals(OS_WINDOWS)) {
            return EXT_DLL;
        }
        if (os.equals(OS_OSX)) {
            return EXT_DYLIB;
        }
        throw new RuntimeException("Unable to determine the shared library "
                + "file extension for operating system '" + os
                + "'. Please specify Java parameter -D" + key
                + "=<FILE-EXTENSION>");
    }

    /**
     * Creates the temporary directory used for unpacking the native libraries.
     * This directory is marked for deletion on exit.
     *
     * @return The temporary directory for native libraries.
     */
    private static File createTempDirectory() {
        // Return cached tmp directory when already created
        if (tmp != null) {
            return tmp;
        }

        try {
            tmp = File.createTempFile("microdblib", null);
            if (!tmp.delete()) {
                throw new IOException("Unable to delete temporary file " + tmp);
            }
            if (!tmp.mkdirs()) {
                throw new IOException("Unable to create temporary directory "
                        + tmp);
            }
            tmp.deleteOnExit();
            return tmp;
        } catch (final IOException e) {
            throw new RuntimeException("Unable to create temporary directory "
                    + "for usb4java natives: " + e, e);
        }
    }

    /**
     * Returns the platform name. This could be for example "linux-x86" or
     * "windows-x86_64".
     *
     * @return The architecture name. Never null.
     */
    private static String getPlatform() {
        return getOS() + "-" + getArch();
    }

    /**
     * Returns the name of the usb4java native library. This could be
     * "libusb4java.dll" for example.
     *
     * @return The usb4java native library name. Never null.
     */
    private static String getLibName() {
        return "libmicrodb-jni." + getExt();
    }


    /**
     * Extracts a single library.
     *
     * @param platform The platform name (For example "linux-x86")
     * @param lib      The library name to extract (For example "libusb0.dll")
     * @return The absolute path to the extracted library.
     */
    private static String extractLibrary(final String platform,
                                         final String lib) {
        // Extract the usb4java library
        final String source = '/'
                + Loader.class.getPackage().getName().replace('.', '/') + '/'
                + platform + "/" + lib;

        // Check if native library is present
        final URL url = Loader.class.getResource(source);
        if (url == null) {
            throw new RuntimeException("Native library not found in classpath: "
                    + source);
        }

        // If native library was found in an already extracted form then
        // return this one without extracting it
        if ("file".equals(url.getProtocol())) {
            try {
                return new File(url.toURI()).getAbsolutePath();
            } catch (final URISyntaxException e) {
                // Can't happen because we are not constructing the URI
                // manually. But even when it happens then we fall back to
                // extracting the library.
                throw new RuntimeException(e.toString(), e);
            }
        }

        // Extract the library and return the path to the extracted file.
        final File dest = new File(createTempDirectory(), lib);
        try {
            final InputStream stream = Loader.class.getResourceAsStream(source);
            if (stream == null) {
                throw new RuntimeException("Unable to find " + source
                        + " in the classpath");
            }
            IOUtils.pump(stream, new FileOutputStream(dest));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to extract native library "
                    + source + " to " + dest + ": " + e, e);
        }

        // Mark usb4java library for deletion
        dest.deleteOnExit();

        return dest.getAbsolutePath();
    }

    public static synchronized void load() {
        // Do nothing if already loaded (or still loading)
        if (loaded) {
            return;
        }

        loaded = true;
        final String platform = getPlatform();
        final String lib = getLibName();
        System.load(extractLibrary(platform, lib));
    }
}