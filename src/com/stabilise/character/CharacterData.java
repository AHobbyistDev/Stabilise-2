package com.stabilise.character;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.files.FileHandle;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.stabilise.core.Constants;
import com.stabilise.core.Resources;
import com.stabilise.item.BoundedContainer;
import com.stabilise.item.Container;
import com.stabilise.util.IOUtil;
import com.stabilise.util.Log;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;

/**
 * Data about a character.
 */
public class CharacterData {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The file name of the character data file. */
	public static final String FILE_DATA = "data";
	/** The name of the directory holding a player's private world. */
	public static final String DIR_PRIVATE_WORLD = "privatedimension/";
	
	//--------------------==========--------------------
	//------------=====Member Variables=====------------
	//--------------------==========--------------------
	
	/** The character's "file system name". This is usually the same as their
	 * actual name. */
	public String fileSystemName;
	/** The character's creation date. */
	public long creationDate;
	/** The date at which the character was last played. */
	public long lastPlayed;
	
	/** The character's name. */
	public String name;
	/** The character's hash, which is used to distinguish characters with the
	 * same name. This is stored as a string as it is used as the name of NBT
	 * tags. */
	public String hash;
	
	/** The character's level. */
	public int level;
	/** The character's xp. */
	public int xp;
	
	/** The character's max health. */
	public int maxHealth;
	/** The character's max stamina. */
	public int maxStamina;
	/** The character's max mana. */
	public int maxMana;
	
	/** The character's inventory. */
	public Container inventory;
	
	
	/**
	 * Creates a new CharacterData.
	 * 
	 * @param characterName The character's name. This serves as both the file
	 * system name and the aesthetic name by default.
	 * 
	 * @throws NullPointerException if {@code characterName} is {@code null}.
	 * @throws IllegalArgumentException if {@code characterName} is empty.
	 */
	public CharacterData(String characterName) {
		if(characterName == null)
			throw new NullPointerException("characterName is null");
		if(characterName == "")
			throw new IllegalArgumentException("characterName is empty");
		
		fileSystemName = characterName;
		name = characterName;
	}
	
	/**
	 * Confirms the creation of the character by setting {@code creationDate}
	 * to that of the current time, and generating the character's hash.
	 */
	public void create() {
		creationDate = System.currentTimeMillis();//new Date().getTime();
		lastPlayed = creationDate;
		
		// Hash based on a bunch of things hopefully unique to the user to
		// minimise hash collisions
		HashFunction hf = Hashing.sha256();
		HashCode hc = hf.newHasher()
		       .putLong(creationDate)
		       .putLong(System.nanoTime())
		       .putString(System.getProperty("java.version"), Charsets.UTF_8)
		       .putString(System.getProperty("os.name"), Charsets.UTF_8)
		       .putString(System.getProperty("os.version"), Charsets.UTF_8)
		       .putString(System.getProperty("user.name"), Charsets.UTF_8)
		       .putLong(new Random().nextLong())// In Java 8, constructing new Random is sensitive to prior
		       .hash();							// constructions, so we allow the hash to be influenced by
												// earlier actions by the user in this sense.
		hash = hc.toString();
	}
	
	/**
	 * Loads the character data.
	 * 
	 * @throws IOException if an I/O exception is encountered while attempting
	 * to load the character data.
	 */
	public void load() throws IOException {
		NBTTagCompound tag = NBTIO.readCompressed(getFile());
		
		// TODO: For now only the hash and name are configured to throw
		// IOExceptions, as they're the only important details
		
		hash = tag.getStringUnsafe("hash");
		
		name = tag.getStringUnsafe("name");
		
		level = tag.getInt("level");
		xp = tag.getInt("xp");
		
		maxHealth = tag.getInt("maxHealth");
		maxStamina = tag.getInt("maxStamina");
		maxMana = tag.getInt("maxMana");
		
		inventory = new BoundedContainer(Constants.INVENTORY_CAPACITY);
		inventory.fromNBT(tag.getList("inventory"));
	}
	
	/**
	 * Saves the character data.
	 * 
	 * @throws IOException if an I/O exception is encountered while attempting
	 * to save the character data.
	 */
	public void save() throws IOException {
		lastPlayed = new Date().getTime();
		
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.addString("hash", hash);
		
		tag.addString("name", name);
		
		tag.addInt("level", level);
		tag.addInt("xp", xp);
		
		tag.addInt("maxHealth", maxHealth);
		tag.addInt("maxStamina", maxStamina);
		tag.addInt("maxMana", maxMana);
		
		tag.addList("inventory", inventory.toNBT());
		
		NBTIO.safeWriteCompressed(getFile(), tag);
	}
	
	/**
	 * Gets this character's filesystem directory.
	 */
	public FileHandle getDir() {
		return getCharacterDir(fileSystemName);
	}
	
	/**
	 * Gets this character data's filesystem handle.
	 */
	public FileHandle getFile() {
		return getDir().child(FILE_DATA);
	}
	
	/**
	 * Gets this character's private dimension's filesystem directory.
	 */
	public FileHandle getDimensionDir() {
		return getDir().child(DIR_PRIVATE_WORLD);
	}
	
	@Override
	public int hashCode() {
		return hash != null ? hash.hashCode() : 0;
	}
	
	public String toString() {
		return (name != null && hash != null) ?
				"CharacterData[\"" + name + "\"," + hash + "]" :
				"UnitialisedCharacterData";
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets the list of created characters. The returned array is sorted in
	 * descending order of the time and date at which the character was last
	 * used, such that the most recently used character is at the head of the
	 * array.
	 * 
	 * @return An array of created characters.
	 */
	public static CharacterData[] getCharactersList() {
		IOUtil.createDir(Resources.CHARACTERS_DIR);
		FileHandle[] characterDirs = Resources.CHARACTERS_DIR.list();
		
		List<CharacterData> characters = new ArrayList<>(characterDirs.length);
		
		// Cycle over all the folders in the characters directory and determine
		// their validity.
		for(FileHandle charDir : characterDirs) {
			CharacterData character = new CharacterData(charDir.name());
			try {
				character.load();
				characters.add(character);
			} catch(IOException e) {
				Log.get().postSevere("Could not load character data for character \""
						+ charDir.name() + "\"!");
			}
		}
		
		// Now, we convert the List to a conventional array
		CharacterData[] characterArr = characters.toArray(new CharacterData[0]);
		
		// Sort the characters using Java's built-in Comparable interface
		Arrays.sort(characterArr);
		
		return characterArr;
	}
	
	/**
	 * Gets a character's directory.
	 * 
	 * @param characterName The character's file system name.
	 * 
	 * @return The file representing the character's directory on the file 
	 * system.
	 * @throws NullPointerException if {@code characterName} is {@code null}.
	 * @throws IllegalArgumentException if {@code characterName} is empty.
	 */
	public static FileHandle getCharacterDir(String characterName) {
		if(characterName.length() == 0)
			throw new IllegalArgumentException("characterName is empty");
		return Resources.CHARACTERS_DIR.child(IOUtil.getLegalString(characterName) + "/");
	}
	
	/**
	 * Gets the data for the default 'Player' character.
	 * 
	 * @return The default character's data.
	 */
	public static CharacterData defaultCharacter() {
		CharacterData data = new CharacterData("Player");
		data.hash = "";
		return data;
	}
	
}
