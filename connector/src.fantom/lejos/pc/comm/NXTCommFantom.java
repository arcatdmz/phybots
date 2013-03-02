package lejos.pc.comm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of NXTComm using the the LEGO Fantom API.
 */
public class NXTCommFantom {
	private static final int MIN_TIMEOUT = 5000;
	private static final int MAX_ERRORS = 10;

	private static boolean hasTriedToLoad = false;
	private static boolean hasLoaded = false;

	private long nxt;

	public NXTCommFantom() {
		if (!loadLibrary()) {
			throw new InstantiationError("Failed to load the jfantom library.");
		}
	}

	private boolean loadLibrary() {
		if (hasTriedToLoad) {
			return hasLoaded;
		}
		hasTriedToLoad = true;

		// Get proper library file name.
		String libFileName = getLibraryPath();
		if (libFileName == null) return false;

		// Create a temporary file.
		File file = getLibraryFile(libFileName);
		if (file == null) {
			// Copy contents of the jfantom library to the temporary file.
			file = createTempFile();
			InputStream is = getClass().getClassLoader()
					.getResourceAsStream(libFileName);
			if (!saveAsFile(is, file)) return false;
		}
		if (file == null) return false;

		// Load the copied library.
		try {
			System.load(file.getAbsolutePath());
			System.out.println(String.format(
					"Loaded jfantom library: %s",
					file.getAbsolutePath()));
		} catch (Exception e) {
			System.err.println(
					String.format(
							"Failed to load the jfantom library: %s",
							file.getAbsolutePath()));
			return false;
		}
		hasLoaded = true;
		return true;
	}

	private static String getLibraryPath() {
		if (System.getProperty("os.arch").indexOf("64") >= 0) {
			System.err.println("Current jfantom library does not support 64bit Java VM.");
			return null;
		}
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0)
			return "windows/x86/jfantom.dll";
		if (os.indexOf("mac") >= 0)
			return "macosx/libjfantom.jnilib";
		System.err.println(String.format("Unsupported os type for the jfantom library: %s", os));
		return null;
	}

	private static File getLibraryFile(String libFileName) {
		String userDir = System.getProperty("user.dir");
		File libFile = new File(userDir +
				File.separator + "lib" +
				File.separator + "jfantom" +
				File.separator + libFileName);
		if (libFile.exists()) return libFile;
		return null;
	}

	private static File createTempFile() {
		File file;
		try {
			file = File.createTempFile("temp", Long.toString(System.nanoTime()));
		} catch (IOException e) {
			System.err.println(
					"Failed to create a temporary file for the jfantom library");
			return null;
		}
		try {
			file.deleteOnExit();
		} catch (Exception e) {
			System.err.println(
					"Failed to setup a temporary file for the jfantom library");
			file.delete();
			return null;
		}
		return file;
	}

	private static boolean saveAsFile(InputStream is, File fd) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fd);
			byte b[] = new byte[1000];
			int len;
			while ((len = is.read(b)) >= 0) {
				fos.write(b, 0, len);
			}
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println(
					String.format(
							"Failed to copy the jfantom library to a temporary file: %s",
							fd.getAbsolutePath()));
			return false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// Do nothing.
				}
			}
		}
	}
	public static void main(String[] args) {
		
		// JNI direct call
		System.out.println("--- JNI direct call");
		NXTCommFantom f = new NXTCommFantom();
		for (String nxtId : f.jfantom_find()) {
			System.out.println(String.format("connected to %s", nxtId));
			long nxt = f.jfantom_open(nxtId);
			int wrote = f.jfantom_send_data(
					nxt,
					new byte[]{ (byte) 0x01, (byte) 0x88 },
					0,
					2);
			System.out.println(String.format("wrote %d bytes", wrote));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			byte[] data = new byte[7];
			int recv = f.jfantom_read_data(nxt, data, 0, 7);
			System.out.println(String.format("received %d bytes", recv));
			f.jfantom_close(nxt);
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Wrapped call
		System.out.println("--- NXTCommFantom wrapped call");
		for (String nxtId : f.find()) {
			System.out.println(String.format("connected to %s", nxtId));
			f.open(nxtId);
			int wrote = f.sendData(
					new byte[]{ (byte) 0x01, (byte) 0x88 },
					0,
					2);
			System.out.println(String.format("wrote %d bytes", wrote));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			byte[] data = new byte[7];
			int recv = f.readData(data, 0, 7);
			System.out.println(String.format("received %d bytes", recv));
			f.close();
		}
	}

	private native String[] jfantom_find();
	public String[] find() {
		return jfantom_find();
	}

	public boolean isConnected() {
		return nxt != 0;
	}

	private native long jfantom_open(String nxt);
	public boolean open(String connectionString) {
		nxt = jfantom_open(connectionString);
		return nxt != 0;
	}

	private native void jfantom_close(long nxt);
	public boolean close() {
		if (nxt == 0) return false;
		jfantom_close(nxt);
		return true;
	}

	private native int jfantom_send_data(
			long nxt, byte[] message, int offset, int len);
	public int sendData(byte[] message, int offset, int len) {
		if (nxt == 0) return -1;
		return jfantom_send_data(nxt, message, offset, len);
	}

	private native int jfantom_read_data(
			long nxt, byte[] data, int offset, int len);
	public int readData(byte[] data, int offset, int len) {
		if (nxt == 0) return -1;
		return jfantom_read_data(nxt, data, offset, len);
	}

}
