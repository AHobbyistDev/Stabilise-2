package com.stabilise.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.io.Files;
import com.stabilise.util.annotation.PrivateBecauseInelegant;

/**
 * This class provides some static utility IO methods.
 */
public class IOUtil {
	
	// non-instantiable
	private IOUtil() {}
	
	/**
	 * Creates a file directory, including all nonexistent parent directories,
	 * if it does not already exist. This method fails fast by throwing an
	 * {@code IOException} if the directory could not be created. Note that if
	 * this operation fails it may have succeeded in creating some of the
	 * necessary parent directories.
	 * 
	 * @param dir The directory to create.
	 * 
	 * @return The {@code File} passed as the {@code dir} parameter, for
	 * chaining operations.
	 * @throws NullPointerException if {@code dir} is {@code null}.
	 * @throws IOException if the directory failed to be created.
	 */
	public static File createDir(File dir) throws IOException {
		if(dir == null)
			throw new NullPointerException("dir is null");
		
		if(!dir.exists() && !dir.mkdirs())
			throw new IOException("Directory \"" + dir.getAbsolutePath() + "\" could not be created!");
		
		return dir;
	}
	
	/**
	 * Creates a file directory, including all nonexistent parent directories,
	 * if it does not already exist. Note that if this operation fails it may
	 * have succeeded in creating some of the necessary parent directories.
	 * Unlike {@link #createDir(File)}, this will not throw an exception if the
	 * directory could not be created, and so it it not guaranteed that the
	 * operation was successful.
	 * 
	 * @param dir The directory create.
	 * 
	 * @return The {@code File} passed as the {@code dir} parameter, for
	 * chaining operations.
	 * @throws NullPointerException if {@code dir} is {@code null}.
	 */
	public static File createDirQuietly(File dir) {
		try {
			createDir(dir);
		} catch(IOException e) {
			//Log.exception(e);
		}
		
		return dir;
	}
	
	/**
	 * Creates a file's parent directory, including all nonexistent parent
	 * directories of that directory, if it does not already exist. This
	 * method fails fast by throwing an {@code IOException} if the directory
	 * could not be created. Note that if this operation fails it may have
	 * succeeded in creating some of the necessary parent directories.
	 * 
	 * @param file The file whose parent directory to create.
	 * 
	 * @return The {@code File} passed as the {@code file} parameter, for
	 * chaining operations.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 * @throws IOException if the directory failed to be created.
	 */
	public static File createParentDir(File file) throws IOException {
		if(file == null)
			throw new NullPointerException("file is null");
		
		File parentDir = file.getAbsoluteFile().getParentFile();
		
		if(parentDir != null)
			createDir(parentDir);
		
		return file;
	}
	
	/**
	 * Creates a file's parent directory, including all nonexistent parent
	 * directories of that directory, if it does not already exist. Note that
	 * if this operation fails it may have succeeded in creating some of the
	 * necessary parent directories. Unlike {@link #createParentDir(File)},
	 * this will not throw an exception if the file's parent directory could
	 * not be created, and so it it not guaranteed that the operation was
	 * successful.
	 * 
	 * @param file The file whose parent directory to create.
	 * 
	 * @return The {@code File} passed as the {@code file} parameter, for
	 * chaining operations.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 */
	public static File createParentDirQuietly(File file) {
		try {
			createParentDir(file);
		} catch(IOException e) {
			//Log.exception(e);
		}
		return file;
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
	 * @throws IllegalArgumentException if {@code file} lacks a parent
	 * directory.
	 */
	public static File getNewFile(File file) {
		if(!file.exists())
			return file;
		
		String originalName = file.getName();
		File parentDir = file.getAbsoluteFile().getParentFile();
		
		return getNewFile(originalName, parentDir);
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
	public static File getNewFile(String fileName, File parentDir) {
		if(parentDir == null)
			throw new NullPointerException("parentDir is null!");
		
		String originalName = Files.getNameWithoutExtension(fileName);
		String extension = Files.getFileExtension(fileName);
		if(!extension.equals(""))
			extension = "." + extension;
		File file;
		
		for(int i = 1; (file = new File(parentDir, fileName)).exists(); i++)
			fileName = originalName + " - " + i + extension;
		
		// Outside the scope of this method
		//createDir(parentDir);
		
		return file;
	}
	
	/**
	 * Ensures that the given string contains only legal filename characters,
	 * and returns a modified version of the string such that it is legal.
	 * 
	 * @param string The string.
	 * 
	 * @return The modified string.
	 * @throws NullPointerException if {@code string} is {@code null}.
	 */
	public static String getLegalString(String string) {
		// Allows lowercase+uppercase letters, all numbers, spaces (\u0020),
		// parentheses, periods, dashes, and inverted commas 
		return string.replaceAll("[^a-zA-Z0-9\\u0020().'-]", "_");
	}
	
	/**
	 * Safely saves a file by writing the data to a temporary file and then
	 * renaming the temporary file to the desired file name. This is done as to
	 * ensure data is not lost if for some reason the save process is
	 * interrupted and it is desirable to retain the earlier version of the
	 * file.
	 * 
	 * <p>The {@code saveMethod} parameter specifies the method which will
	 * execute the actual process of saving. This method <i>must</i> possess a
	 * {@link java.io.File File} object as its first argument as the file to
	 * which to safe the file; any further arguments are optional, and the
	 * {@code args} parameter specifies any further arguments which are to be
	 * passed to the save method.
	 * 
	 * @param file The file.
	 * @param saveMethod The save method.
	 * @param arg The save method's optional further arguments.
	 * 
	 * @throws Exception if the save method failed to invoke reflectively, or
	 * threw an exception.
	 * @throws IOException if the old file was not deleted.
	 */
	@PrivateBecauseInelegant(alternative={"safelySaveFile1", "safelySaveFile2"})
	private static void safelySaveFile(File file, Method saveMethod, Object... arg) throws Exception {
		safelySaveFile(file, null, saveMethod, arg);
	}
	
	/**
	 * Safely saves a file by writing the data to a temporary file and then
	 * renaming the temporary file to the desired file name. This is done as to
	 * ensure data is not lost if for some reason the save process is
	 * interrupted and it is desirable to retain the earlier version of the
	 * file.
	 * 
	 * <p>The {@code saveMethod} parameter specifies the method which will
	 * execute the actual process of saving. This method <i>must</i> possess a
	 * {@link java.io.File File} object as its first argument as the file to
	 * which to safe the file; any further arguments are optional, and the
	 * {@code args} parameter specifies any further arguments which are to be
	 * passed to the save method. The object passed as the {@code invoker}
	 * parameter will be treated as the object to have invoked the save method;
	 * it most cases it should be a {@code this} - however, if the method is
	 * static, the {@code invoker} parameter is allowed to be {@code null}
	 * (or, alternatively, you can refer to
	 * {@link #safelySaveFile(File, Method, Object...)}).
	 * 
	 * @param file The file.
	 * @param invoker The invoker object.
	 * @param saveMethod The save method.
	 * @param args The save method's optional further arguments.
	 * 
	 * @throws Exception if the save method failed to invoke reflectively, or
	 * threw an exception.
	 * @throws IOException if the old file was not deleted.
	 */
	@PrivateBecauseInelegant(alternative={"safelySaveFile1", "safelySaveFile2"})
	private static void safelySaveFile(File file, Object invoker, Method saveMethod, Object... args) throws Exception {
		File tempFile = new File(file.getAbsolutePath() + "_tmp");
		
		if(tempFile.exists())
			tempFile.delete();
		
		// Prepend tempFile to the list of arguments
		Object[] arguments = new Object[args.length + 1];
		arguments[0] = tempFile;
		for(int i = 0; i < args.length; i++) {
			arguments[i+1] = args[i];
		}
		
		try {
			saveMethod.invoke(invoker, arguments);		// Can throw any number of exceptions
		} catch(Exception e) {
			if(e instanceof InvocationTargetException)	// Try to rethrow any exceptions thrown by the save method
				throw (Exception)e.getCause();
			else
				throw e;
		}
		
		if(file.exists())
			file.delete();
		
		if(file.exists())
			throw new IOException("Failed to delete " + file);
		else
			tempFile.renameTo(file);
	}
	
	/**
	 * Performs the first part of a safe file save operation by preparing and
	 * then returning the temporary file to which to write to. This should be
	 * used together with {@link #safelySaveFile2(File)} as in a manner
	 * similar to:
	 * 
	 * <pre>
	 * File tempFile = IOUtil.safelySaveFile1(someFile);
	 * saveTheFile(tempFile);
	 * IOUtil.safelySaveFile2(someFile);</pre>
	 * 
	 * <p>This together with <tt>safelySaveFile2(File)</tt> ensures that a file
	 * is safely saved by writing the data to a temporary file and then
	 * renaming the temporary file to the desired file name. This is done as to
	 * ensure data is not lost if for some reason the save process is
	 * interrupted and it is desirable to retain the earlier version of the
	 * file.
	 * 
	 * @param file The file.
	 * 
	 * @return The temporary file to which to write.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 */
	public static File safelySaveFile1(File file) {
		File tempFile = new File(file.getAbsolutePath() + "_tmp");
		
		if(tempFile.exists())
			tempFile.delete();
		
		return tempFile;
	}
	
	/**
	 * Performs the second part of a safe file save operation by deleting the
	 * original file and renaming the temporary file to which the data was
	 * written to the name of the original file. This should be used together
	 * with {@link #safelySaveFile1(File)} as in a manner similar to:
	 * 
	 * <pre>
	 * File tempFile = IOUtil.safelySaveFile1(someFile);
	 * saveTheFile(tempFile);
	 * IOUtil.safelySaveFile2(someFile);</pre>
	 * 
	 * <p>This together with <tt>safelySaveFile1(File)</tt> ensures that a
	 * file is safely saved by writing the data to a temporary file and then
	 * renaming the temporary file to the desired file name. This is done as to
	 * ensure data is not lost if for some reason the save process is
	 * interrupted and it is desirable to retain the earlier version of the
	 * file.
	 * 
	 * @param file The file.
	 * 
	 * @throws NullPointerException if {@code file} is {@code null}.
	 * @throws RuntimeException if the original file was not deleted.
	 */
	public static void safelySaveFile2(File file) {
		if(file.exists())
			file.delete();
		
		if(file.exists())
			// A checked IOException may be annoying, so use an unchecked RuntimeException
			//throw new IOException("Failed to delete " + file);
			throw new RuntimeException("Failed to delete " + file);
		else
			(new File(file.getAbsolutePath() + "_tmp")).renameTo(file);
	}

}
