package com.stabilise.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.stabilise.core.Resources;

/**
 * A config file is a file containing simple tags representing config values.
 * Such a config file typically has the layout:
 * 
 * <pre>
 * property1:value1
 * property2:value2
 * etc.
 * </pre>
 */
public class ConfigFile {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The file extension to use for config files. */
	public static final String CONFIG_FILE_EXTENSION = ".txt";
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The tags stored in the config file. Contains K/V mappings of each tag's
	 * name to their represented property, stored in string form. */
	public LinkedHashMap<String, String> tags = new LinkedHashMap<String, String>();
	
	/** The name of the config file. */
	private final String name;
	
	
	/**
	 * Creates a new config file.
	 * 
	 * @param name The name of the config file.
	 */
	public ConfigFile(String name) {
		this.name = name;
	}
	
	/**
	 * Checks for whether or not the config file holds a tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return {@code true} if the config file contains the tag; {@code false}
	 * if it does not.
	 */
	public boolean hasTag(String tagName) {
		return tags.containsKey(tagName);
	}
	
	/**
	 * Sets a tag. The data type of the {@code value} parameter is detected
	 * and the tag is added as its appropriate type. If {@code value} is not
	 * a {@code String} or an object wrapper for any of the seven primitive
	 * types, the tag will not be added.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} contains a colon
	 * (:) and {@code value} holds a valid data type.
	 */
	public void addTag(String name, Object value) {
		if(value instanceof Boolean) {
			addBoolean(name, ((Boolean)value).booleanValue());
		} else if(value instanceof Byte) {
			addByte(name, ((Byte)value).byteValue());
		} else if(value instanceof Short) {
			addShort(name, ((Short)value).shortValue());
		} else if(value instanceof Integer) {
			addInteger(name, ((Integer)value).intValue());
		} else if(value instanceof Long) {
			addLong(name, ((Long)value).longValue());
		} else if(value instanceof Float) {
			addFloat(name, ((Float)value).floatValue());
		} else if(value instanceof Double) {
			addDouble(name, ((Double)value).doubleValue());
		} else if(value instanceof String) {
			addString(name, (String)value);
		} else {
			Log.critical("Invalid tag type " + value.getClass().toString() + " for tag \"" + name + "\"!");
		}
	}
	
	/**
	 * Sets a boolean tag.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} contains a colon
	 * (:).
	 */
	public void addBoolean(String name, boolean value) {
		tags.put(validate(name), Boolean.toString(value));
	}
	
	/**
	 * Sets a byte tag.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} contains a colon
	 * (:).
	 */
	public void addByte(String name, byte value) {
		tags.put(validate(name), Byte.toString(value));
	}
	
	/**
	 * Sets a short tag.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} contains a colon
	 * (:).
	 */
	public void addShort(String name, short value) {
		tags.put(validate(name), Short.toString(value));
	}
	
	/**
	 * Sets an integer tag.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} contains a colon
	 * (:).
	 */
	public void addInteger(String name, int value) {
		tags.put(validate(name), Integer.toString(value));
	}
	
	/**
	 * Sets a long tag.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} contains a colon
	 * (:).
	 */
	public void addLong(String name, long value) {
		tags.put(validate(name), Long.toString(value));
	}
	
	/**
	 * Sets a float tag.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} contains a colon
	 * (:).
	 */
	public void addFloat(String name, float value) {
		tags.put(validate(name), Float.toString(value));
	}
	
	/**
	 * Sets a double tag.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} contains a colon
	 * (:).
	 */
	public void addDouble(String name, double value) {
		tags.put(validate(name), Double.toString(value));
	}
	
	/**
	 * Sets a string tag.
	 * 
	 * @param name The name of the tag.
	 * @param value The value for the tag.
	 * 
	 * @throws IllegalArgumentException Thrown if either {@code name} or
	 * {@code value} contains a colon (:).
	 */
	public void addString(String name, String value) {
		tags.put(validate(name), validate(value));
	}
	
	/**
	 * Sets a tag. The returned object will be of the class specified by
	 * {@code tagClass}, provided the value passed is either the {@code String}
	 * class or one of the seven primitive wrapper classes.
	 * 
	 * @param name The name of the tag.
	 * @param tagClass The class of the tag's data type.
	 * 
	 * @return The value of the tag, or an otherwise specified default value if
	 * the tag cannot be found or cannot be parsed, or {@code null} if the
	 * {@code tagClass} parameter is an unrecognised class.
	 */
	public Object getTag(String name, Class<?> tagClass) {
		if(tagClass == Boolean.class) {
			return new Boolean(getBoolean(name));
		} else if(tagClass == Byte.class) {
			return new Byte(getByte(name));
		} else if(tagClass == Short.class) {
			return new Short(getShort(name));
		} else if(tagClass == Integer.class) {
			return new Integer(getInteger(name));
		} else if(tagClass == Long.class) {
			return new Long(getLong(name));
		} else if(tagClass == Float.class) {
			return new Float(getFloat(name));
		} else if(tagClass == Double.class) {
			return new Double(getDouble(name));
		} else if(tagClass == String.class) {
			return getString(name);
		} else {
			Log.critical("Invalid tag class \"" + tagClass.toString() + "\" for tag \"" + name + "\"!");
			return null;
		}
	}
	
	/**
	 * Gets a boolean tag.
	 * 
	 * @param name The name of the tag.
	 * 
	 * @return The value of the tag, or {@code false} if the tag is not present
	 * or could not be parsed as a boolean.
	 */
	public boolean getBoolean(String name) {
		if(hasTag(name))
			return Boolean.parseBoolean(tags.get(name));
		else
			return false;
	}
	
	/**
	 * Gets a byte tag.
	 * 
	 * @param name The name of the tag.
	 * 
	 * @return The value of the tag, or {@code -1} if the tag is not present or
	 * could not be parsed as a byte.
	 */
	public byte getByte(String name) {
		if(hasTag(name)) {
			byte value;
			try {
				value = Byte.parseByte(tags.get(name));
			} catch(NumberFormatException e) {
				return -1;
			}
			return value;
		} else {
			return -1;
		}
	}
	
	/**
	 * Gets a short tag.
	 * 
	 * @param name The name of the tag.
	 * 
	 * @return The value of the tag, or {@code -1} if the tag is not present or
	 * could not be parsed as a short.
	 */
	public short getShort(String name) {
		if(hasTag(name)) {
			short value;
			try {
				value = Short.parseShort(tags.get(name));
			} catch(NumberFormatException e) {
				return -1;
			}
			return value;
		} else {
			return -1;
		}
	}
	
	/**
	 * Gets an integer tag.
	 * 
	 * @param name The name of the tag.
	 * 
	 * @return The value of the tag, or {@code -1} if the tag is not present or
	 * could not be parsed as an integer.
	 */
	public int getInteger(String name) {
		if(hasTag(name)) {
			int value;
			try {
				value = Integer.parseInt(tags.get(name));
			} catch(NumberFormatException e) {
				return -1;
			}
			return value;
		} else {
			return -1;
		}
	}
	
	/**
	 * Gets a long tag.
	 * 
	 * @param name The name of the tag.
	 * 
	 * @return The value of the tag, or {@code -1} if the tag is not present or
	 * could not be parsed as a long.
	 */
	public long getLong(String name) {
		if(hasTag(name)) {
			long value;
			try {
				value = Long.parseLong(tags.get(name));
			} catch(NumberFormatException e) {
				return -1L;
			}
			return value;
		} else {
			return -1L;
		}
	}
	
	/**
	 * Gets a float tag.
	 * 
	 * @param name The name of the tag.
	 * 
	 * @return The value of the tag, or {@code -1} if the tag is not present or
	 * could not be parsed as a float.
	 */
	public float getFloat(String name) {
		if(hasTag(name)) {
			float value;
			try {
				value = Float.parseFloat(tags.get(name));
			} catch(NumberFormatException e) {
				return -1.0f;
			}
			return value;
		} else {
			return -1.0f;
		}
	}
	
	/**
	 * Gets a double tag.
	 * 
	 * @param name The name of the tag.
	 * 
	 * @return The value of the tag, or {@code -1} if the tag is not present or
	 * could not be parsed as a double.
	 */
	public double getDouble(String name) {
		if(hasTag(name)) {
			double value;
			try {
				value = Double.parseDouble(tags.get(name));
			} catch(NumberFormatException e) {
				return -1.0D;
			}
			return value;
		} else {
			return -1.0D;
		}
	}
	
	/**
	 * Gets a string tag.
	 * 
	 * @param name The name of the tag.
	 * 
	 * @return The value of the tag, or {@code null} if the tag is not present.
	 */
	public String getString(String name) {
		if(hasTag(name))
			return tags.get(name);
		else
			return null;
	}
	
	/**
	 * Checks for whether or not the config file exists on the file system.
	 * 
	 * @return {@code true} if the config file exists; {@code false} if it does
	 * not.
	 */
	public boolean exists() {
		return getFile().exists();
	}
	
	/**
	 * Loads the config file. Any tags loaded from the file will replace ones
	 * currently set.
	 * 
	 * @return The ConfigFile, for chain construction.
	 * @throws IOException Thrown if an I/O exception was encountered while
	 * attempting to load the config file.
	 */
	public ConfigFile load() throws IOException {
		//tags.clear();
		
		String[] lines = Resources.loadTextFileFromFileSystem(getFile());
		String[] keyValuePair;
		for(String s : lines) {
			keyValuePair = s.split(":");
			try {
				tags.put(keyValuePair[0], keyValuePair[1]);
			} catch(ArrayIndexOutOfBoundsException e) {
				// ignored - we'll just skip invalid lines
			}
		}
		
		/*
		FileReader fr = new FileReader(getFile());		// May throw a FileNotFoundException
		BufferedReader br = new BufferedReader(fr);
		
		String s;
		String[] keyValue;
		
		try {
			while((s = br.readLine()) != null) {
				keyValue = s.split(":");
				tags.put(keyValue[0], keyValue[1]);
			}
		} finally {
			br.close();
			fr.close();
		}
		*/
		
		return this;
	}
	
	/**
	 * Saves the config file.
	 * 
	 * @throws IOException Thrown if an I/O exception was encountered while
	 * attempting to save the config file.
	 */
	public void save() throws IOException {
		save(getFile());
	}
	
	/**
	 * Saves the config file.
	 * 
	 * @param file The file to which to save the file.
	 * 
	 * @throws IOException Thrown if an I/O exception was encountered while
	 * attempting to save the config file.
	 */
	private void save(File file) throws IOException {
		FileWriter fw = new FileWriter(file);			// May throw a FileNotFoundException
		BufferedWriter bw = new BufferedWriter(fw);
		
		try {
			for(Entry<String, String> e : tags.entrySet()) {
				bw.write(e.getKey() + ":" + e.getValue());
				bw.newLine();
			}
		} finally {
			bw.close();
			fw.close();
		}
	}
	
	/**
	 * Safely saves the config file.
	 * 
	 * @throws IOException Thrown if an I/O exception was encountered while
	 * attempting to save the config file.
	 */
	public void safeSave() throws IOException {
		File file = getFile();
		File tempFile = IOUtil.safelySaveFile1(file);
		save(tempFile);
		IOUtil.safelySaveFile2(file);
	}
	
	/**
	 * Gets the file reference for the config file.
	 * 
	 * @return The file representing the config file.
	 */
	private File getFile() {
		return IOUtil.createParentDirQuietly(new File(Resources.CONFIG_DIR, name + CONFIG_FILE_EXTENSION));
	}
	
	/**
	 * Checks for whether or not a string is valid as either a tag name or
	 * field. If the string contains a colon (:), this method will throw an
	 * {@code IllegalArgumentException}, as colons are intended to be used
	 * solely as control characters within config files.
	 * 
	 * @param s The string.
	 * 
	 * @return The string, for chaining operations.
	 * @throws IllegalArgumentException Thrown if the string contains a colon.
	 */
	private String validate(String s) {
		if(s.contains(":"))
			throw new IllegalArgumentException("The string \"" + s + "\" contains a colon, " +
					" and as such is not valid as either a tag name or field within a config file!");
		return s;
	}
	
}
