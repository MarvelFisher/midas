package com.cyanspring.id.Library.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*****************************************************************************
 class CompressorStreamFactory {

 public static final java.lang.String BZIP2 = "bzip2";	  
 public static final java.lang.String GZIP = "gz";	  
 public static final java.lang.String PACK200 = "pack200";	  
 public static final java.lang.String XZ = "xz";	  
 public static final java.lang.String LZMA = "lzma";	  
 public static final java.lang.String SNAPPY_FRAMED = "snappy-framed";	  
 public static final java.lang.String SNAPPY_RAW = "snappy-raw";	  
 public static final java.lang.String Z = "z";	  
 public static final java.lang.String DEFLATE = "deflate";
 }  
 ********************************************************************************/

/**
 * BZip2 tool
 * 
 * @since 1.0
 */
public class ZipUtil {

	
	private static final Logger log = LoggerFactory.getLogger(ZipUtil.class);
	
	public static final String BZIP2 = CompressorStreamFactory.BZIP2; // "bzip2";
	public static final String GZIP = CompressorStreamFactory.GZIP; // "gz";
	public static final String PACK200 = CompressorStreamFactory.PACK200; // "pack200";
	public static final String XZ = CompressorStreamFactory.XZ; // "xz";
	public static final String LZMA = CompressorStreamFactory.LZMA; // "lzma";
	public static final String SNAPPY_FRAMED = CompressorStreamFactory.SNAPPY_FRAMED; // "snappy-framed";
	public static final String SNAPPY_RAW = CompressorStreamFactory.SNAPPY_RAW; // "snappy-raw";
	public static final String Z = CompressorStreamFactory.Z; // "z";
	public static final String DEFLATE = "deflate";

	public static final int BUFFER = 1024;
	public static final String DEFAULT_TYPE = BZIP2;

	/**
	 * get file extension by type
	 * 
	 * @param type
	 * @return
	 */
	public static CharSequence getExt(String type) {
		CharSequence defaultExt = ".zzz";
		switch (type) {
		case BZIP2: {
			defaultExt = ".bz2";
		}
			break;
		case DEFLATE: {
			defaultExt = ".deflate";
		}
			break;
		case GZIP: {
			defaultExt = ".gz";
		}
			break;
		case LZMA: {
			defaultExt = ".lzma";
		}
			break;
		case PACK200: {
			defaultExt = ".pk200";
		}
			break;
		case SNAPPY_FRAMED: {
			defaultExt = ".snf";
		}
			break;
		case SNAPPY_RAW: {
			defaultExt = ".snr";
		}
			break;
		case XZ: {
			defaultExt = ".xz";
		}
			break;
		case Z: {
			defaultExt = ".z";
		}
			break;
		default:
			break;
		}
		return defaultExt;
	}

	/*************************************************
	 * compress
	 *************************************************/

	/**
	 * data compress
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] compress(byte[] data) throws Exception {
		return compress(DEFAULT_TYPE, data);
	}

	/**
	 * data compress with type
	 * 
	 * @param type
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] compress(String type, byte[] data) throws Exception {

		
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		compress(type, bais, baos);

		byte[] output = baos.toByteArray();

		baos.flush();
		baos.close();
		bais.close();

		log.debug(String.format("compress original length = %d, zipped length = %d", data.length, output.length));
		data = null;
		return output;
	}

	/**
	 * file compress
	 * 
	 * @param file
	 * @throws Exception
	 */
	public static void compress(File file) throws Exception {
		compress(file, true);
	}

	/**
	 * file compress with type
	 * 
	 * @param type
	 * @param file
	 * @throws Exception
	 */
	public static void compress(String type, File file) throws Exception {
		compress(type, file, true);
	}

	/**
	 * file compress
	 * 
	 * @param file
	 * @param delete
	 *            delete original file
	 * @throws Exception
	 */
	public static void compress(File file, boolean delete) throws Exception {
		compress(DEFAULT_TYPE, file, delete);
	}

	/**
	 * file compress with type
	 * 
	 * @param type
	 * @param file
	 * @param delete
	 *            delete original file
	 * @throws Exception
	 */
	public static void compress(String type, File file, boolean delete)
			throws Exception {
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream(file.getPath()
				+ getExt(type));

		compress(type, fis, fos);

		fis.close();
		fos.flush();
		fos.close();

		if (delete) {
			file.delete();
		}
	}

	/**
	 * stream compress
	 * 
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void compress(InputStream is, OutputStream os)
			throws Exception {
		compress(DEFAULT_TYPE, is, os);
	}

	/**
	 * stream compress with type
	 * 
	 * @param type
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void compress(String type, InputStream is, OutputStream os)
			throws Exception {
		CompressorOutputStream gos = new CompressorStreamFactory()
				.createCompressorOutputStream(type, os);

		int count;
		byte data[] = new byte[BUFFER];
		while ((count = is.read(data, 0, BUFFER)) != -1) {
			gos.write(data, 0, count);
		}

		// gos.finish();
		gos.flush();
		gos.close();
	}

	/**
	 * file compress
	 * 
	 * @param path
	 * @throws Exception
	 */
	public static void compress(String path) throws Exception {
		compress(DEFAULT_TYPE, path, true);
	}

	/**
	 * file compress with type
	 * 
	 * @param type
	 * @param path
	 * @throws Exception
	 */
	public static void compress(String type, String path) throws Exception {
		compress(type, path, true);
	}

	/**
	 * file compress
	 * 
	 * @param path
	 * @param delete
	 *            delete original file
	 * @throws Exception
	 */
	public static void compress(String path, boolean delete) throws Exception {
		File file = new File(path);
		compress(file, delete);
	}

	/**
	 * file compress with type
	 * 
	 * @ param type
	 * 
	 * @param path
	 * @param delete
	 *            if delete original file
	 * @throws Exception
	 */
	public static void compress(String type, String path, boolean delete)
			throws Exception {
		File file = new File(path);
		compress(type, file, delete);
	}

	/*****************************************************************
	 * decompress
	 *****************************************************************/

	/**
	 * data decompress
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] decompress(byte[] data) throws Exception {
		return decompress(DEFAULT_TYPE, data);
	}

	/**
	 * data decompress with type
	 * 
	 * @param type
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] decompress(String type, byte[] data) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		decompress(type, bais, baos);
		data = baos.toByteArray();

		baos.flush();
		baos.close();

		bais.close();

		return data;
	}

	/**
	 * file decompress
	 * 
	 * @param file
	 * @throws Exception
	 */
	public static void decompress(File file) throws Exception {
		decompress(DEFAULT_TYPE, file, true);
	}

	/**
	 * file decompress with type
	 * 
	 * @param type
	 * @param file
	 * @throws Exception
	 */
	public static void decompress(String type, File file) throws Exception {
		decompress(type, file, true);
	}

	/**
	 * file decpmpress
	 * 
	 * @param file
	 * @param delete
	 *            if delete original file
	 * @throws Exception
	 */
	public static void decompress(File file, boolean delete) throws Exception {
		decompress(DEFAULT_TYPE, file, delete);
	}

	/**
	 * file decpmpress with type
	 * 
	 * @param type
	 * @param file
	 * @param delete
	 *            delete original file
	 * @throws Exception
	 */
	public static void decompress(String type, File file, boolean delete)
			throws Exception {
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream(file.getPath().replace(
				getExt(type), ""));
		decompress(fis, fos);
		fis.close();
		fos.flush();
		fos.close();

		if (delete) {
			file.delete();
		}
	}

	/**
	 * stream decompress
	 * 
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void decompress(InputStream is, OutputStream os)
			throws Exception {
		decompress(DEFAULT_TYPE, is, os);
	}

	/**
	 * stream decompress with type
	 * 
	 * @param type
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void decompress(String type, InputStream is, OutputStream os)
			throws Exception {
		CompressorInputStream gis = new CompressorStreamFactory()
				.createCompressorInputStream(type, is);

		int count;
		byte data[] = new byte[BUFFER];
		while ((count = gis.read(data, 0, BUFFER)) != -1) {
			os.write(data, 0, count);
		}

		gis.close();
	}

	/**
	 * file decompress
	 * 
	 * @param path
	 * @throws Exception
	 */
	public static void decompress(String path) throws Exception {
		decompress(path, true);
	}

	/**
	 * file decompress with type
	 * 
	 * @param type
	 * @param path
	 * @throws Exception
	 */
	public static void decompress(String type, String path) throws Exception {
		decompress(type, path, true);
	}

	/**
	 * file decompress
	 * 
	 * @param path
	 * @param delete
	 *            delete original file
	 * @throws Exception
	 */
	public static void decompress(String path, boolean delete) throws Exception {
		decompress(DEFAULT_TYPE, path, delete);
	}

	/**
	 * file decompress with type
	 * 
	 * @param type
	 * @param path
	 * @param delete
	 *            delete original file
	 * @throws Exception
	 */
	public static void decompress(String type, String path, boolean delete)
			throws Exception {
		File file = new File(path);
		decompress(type, file, delete);
	}

	public static void ZipTest() {
		Path path = Paths.get("");
		String inFile = String.format("%s/test.bin", path.toAbsolutePath()
				.toString());

		path = Paths.get(inFile);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e1) {
			LogUtil.logException(log, e1);
		}
		if (data == null) {
			LogUtil.logError(log, "Read file fail %s", inFile);
			return;
		}
		byte[] zipdata = null;
		try {
			zipdata = ZipUtil.compress(ZipUtil.BZIP2, data);
		} catch (Exception e1) {
			LogUtil.logException(log, e1);
		}
		path = Paths.get(inFile + ".out");
		try {
			Files.write(path, zipdata);
		} catch (IOException e) {
			LogUtil.logException(log, e);
		}
	}

	public static void UnZipTest() {
		Path path = Paths.get("");
		String inFile = String.format("%s/test.bin.out", path.toAbsolutePath()
				.toString());
		String outFile = String.format("%s/test.bin.in", path.toAbsolutePath()
				.toString());

		path = Paths.get(inFile);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e1) {
			LogUtil.logException(log, e1);
		}
		if (data == null) {
			LogUtil.logError(log, "Read file fail %s", inFile);
			return;
		}

		try {
			byte[] uncomp = ZipUtil.decompress(ZipUtil.BZIP2, data);
			path = Paths.get(outFile);
			Files.write(path, uncomp);
		} catch (Exception e) {
			LogUtil.logException(log, e);
		}
	}
}
