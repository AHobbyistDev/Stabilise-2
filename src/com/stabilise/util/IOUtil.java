package com.stabilise.util;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.google.common.io.Files;

/**
 * This class provides some static utility IO methods.
 */
public class IOUtil {
	
	// non-instantiable
	private IOUtil() {}
	
	/**
	 * Creates a file directory, including all nonexistent parent directories,
	 * if it does not already exist, as per the standard {@link File#mkdirs()}
	 * contract.
	 * 
	 * @param handle The handle for the directory to create.
	 * 
	 * @return {@code handle}, for chaining operations.
	 * @throws NullPointerException if {@code handle} is {@code null}.
	 * @throws GDXRuntimeException if {@code handle} is an internal or
	 * classpath handle.
	 */
	public static FileHandle createDir(FileHandle handle) {
		handle.mkdirs();
		return handle;
	}
	
	/**
	 * Creates a file or directory's parent directory, including all
	 * nonexistent parent directories, if it does not already exist, as per the
	 * standard {@link File#mkdirs()} contract.
	 * 
	 * @param handle The handle for the directory to create.
	 * 
	 * @return {@code handle}, for chaining operations.
	 * @throws NullPointerException if {@code handle} is {@code null}.
	 * @throws GDXRuntimeException if {@code handle} is an internal or
	 * classpath handle.
	 */
	public static FileHandle createParentDir(FileHandle handle) {
		handle.parent().mkdirs();
		return handle;
	}
	
	/**
	 * Checks to see if a file exists - if not, the file is returned, and if
	 * so, the file has a number appended to its name until a unique value is
	 * found. This method respects file extensions, such that two files with
	 * the same name but different extensions are considered different. Note
	 * that the returned file's parent directory may not necessarily exist.
	 * 
	 * <p>Consider, for example, a directory containing the files:
	 * 
	 * <ul>
	 * <li><tt>file1.txt</tt>
	 * <li><tt>file1 - 1.txt</tt>
	 * <li><tt>file2.txt</tt>
	 * <li><tt>file3</tt>
	 * </ul>
	 * 
	 * If the {@code file} parameter is that of the file:
	 * <ul>
	 * <li><tt>file1.txt</tt>, a {@code File} representing a file by the name
	 *     <tt>file1 - 2.txt</tt> will be returned.
	 * <li><tt>file1 - 1.txt</tt>, a {@code File} representing a file by the
	 *     name <tt>file1 - 1 - 1.txt</tt> will be returned.
	 * <li><tt>file2.txt</tt>, a {@code File} representing the file
	 *     <tt>file2 - 1.txt</tt> will be returned.
	 * <li><tt>file2.png</tt>, it will be returned unmodified.
	 * <li><tt>file3</tt>, a {@code File} representing the file
	 *     <tt>file3 - 1</tt> will be returned.
	 * <li><tt>someOtherFile.txt</tt>, it will be returned unmodified.
	 * </ul>
	 * 
	 * @param file The file.
	 * 
	 * @return The first valid file.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 */
	public static FileHandle getNewFile(FileHandle file) {
		if(!file.exists())
			return file;
		return getNewFile(file.name(), file.parent());
	}
	
	/**
	 * Checks to see if a file exists - if not, the file is returned, and if
	 * so, the file has a number appended to its name until a unique value is
	 * found. This method respects file extensions, such that two files with
	 * the same name but different extensions are considered different. Note
	 * that the returned file's parent directory may not necessarily exist.
	 * 
	 * <p>Consider, for example, a directory containing the files:
	 * 
	 * <ul>
	 * <li><tt>file1.txt</tt>
	 * <li><tt>file1 - 1.txt</tt>
	 * <li><tt>file2.txt</tt>
	 * <li><tt>file3</tt>
	 * </ul>
	 * 
	 * If the {@code file} parameter is that of the file:
	 * <ul>
	 * <li><tt>file1.txt</tt>, a {@code File} representing a file by the name
	 *     <tt>file1 - 2.txt</tt> will be returned.
	 * <li><tt>file1 - 1.txt</tt>, a {@code File} representing a file by the
	 *     name <tt>file1 - 1 - 1.txt</tt> will be returned.
	 * <li><tt>file2.txt</tt>, a {@code File} representing the file
	 *     <tt>file2 - 1.txt</tt> will be returned.
	 * <li><tt>file2.png</tt>, it will be returned unmodified.
	 * <li><tt>file3</tt>, a {@code File} representing the file
	 *     <tt>file3 - 1</tt> will be returned.
	 * <li><tt>someOtherFile.txt</tt>, it will be returned unmodified.
	 * </ul>
	 * 
	 * @param fileName The name of the file.
	 * @param parentDir The file's parent directory.
	 * 
	 * @return The first valid file.
	 * @throws NullPointerException if either {@code fileName} or {@code
	 * parentDir} are {@code null}.
	 */
	public static FileHandle getNewFile(String fileName, FileHandle parentDir) {
		String originalName = Files.getNameWithoutExtension(fileName);
		String extension = Files.getFileExtension(fileName);
		if(extension.length() != 0)
			extension = "." + extension;
		FileHandle file;
		
		for(int i = 1; (file = parentDir.child(fileName)).exists(); i++)
			fileName = originalName + " - " + i + extension;
		
		return file;
	}
	
	/**
	 * Ensures that the given string contains only legal filename characters,
	 * and returns a modified version of the string such that it is legal.
	 * 
	 * @param str The string.
	 * 
	 * @return The modified string.
	 * @throws NullPointerException if {@code str} is {@code null}.
	 */
	public static String getLegalString(String str) {
		// Allows lowercase+uppercase letters, all numbers, spaces (\u0020),
		// parentheses, periods, dashes, and inverted commas 
		return str.replaceAll("[^a-zA-Z0-9\\u0020().'-]", "_");
	}
	
	/**
	 * Performs the first part of a safe file save operation by preparing and
	 * then returning the temporary file to which to write to. This should be
	 * used together with {@link #safelySaveFile2(FileHandle)} as in a manner
	 * similar to:
	 * 
	 * <pre>
	 * FileHandle tempFile = IOUtil.safelySaveFile1(someFile);
	 * saveTheFile(tempFile);
	 * IOUtil.safelySaveFile2(someFile);</pre>
	 * 
	 * <p>This together with {@code safelySaveFile2(FileHandle)} ensures that
	 * a file is safely saved by writing the data to a temporary file and then
	 * renaming the temporary file to the desired file name. This is done as to
	 * ensure data is not lost if for some reason the save process is
	 * interrupted and it is desirable to retain the earlier version of the
	 * file.
	 * 
	 * @param file The file.
	 * 
	 * @return The temporary file to which to write.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 * @throws GdxRuntimeException if {@code file} is an internal or classpath
	 * file.
	 */
	public static FileHandle safelySaveFile1(FileHandle file) {
		FileHandle tmp = file.sibling(file.name() + "_tmp");
		if(tmp.exists())
			tmp.delete();
		return tmp;
	}
	
	/**
	 * Performs the second part of a safe file save operation by deleting the
	 * original file and renaming the temporary file to which the data was
	 * written to the name of the original file. This should be used together
	 * with {@link #safelySaveFile1(FileHandle)} as in a manner similar to:
	 * 
	 * <pre>
	 * FileHandle tempFile = IOUtil.safelySaveFile1(someFile);
	 * saveTheFile(tempFile);
	 * IOUtil.safelySaveFile2(someFile);</pre>
	 * 
	 * <p>This together with {@code safelySaveFile1(FileHandle)} ensures that
	 * a file is safely saved by writing the data to a temporary file and then
	 * renaming the temporary file to the desired file name. This is done as to
	 * ensure data is not lost if for some reason the save process is
	 * interrupted and it is desirable to retain the earlier version of the
	 * file.
	 * 
	 * @param file The file.
	 * 
	 * @throws NullPointerException if {@code file} is {@code null}.
	 * @throws RuntimeException if the original file was not deleted.
	 * @throws GdxRuntimeException if {@code file} is an internal or classpath
	 * file.
	 */
	public static void safelySaveFile2(FileHandle file) {
		if(file.exists() && !file.delete())
			// A checked IOException may be annoying, so use an unchecked RuntimeException
			//throw new IOException("Failed to delete " + file);
			throw new RuntimeException("Failed to delete " + file);
		else
			file.sibling(file.name() + "_tmp").file().renameTo(file.file());
	}

}
