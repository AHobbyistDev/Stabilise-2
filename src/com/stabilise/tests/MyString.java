package com.stabilise.tests;

public class MyString {
	
	private char[] chars;
	
	public MyString() {
		this(new char[0]);
	}
	
	public MyString(MyString string) {
		this(string.chars);
	}
	
	public MyString(char[] chars) {
		setValue(chars);
	}
	
	public MyString(String string) {
		setValue(getChars(string));
	}
	
	private void setValue(char[] chars) {
		if(chars != null)
			this.chars = chars;
		else
			this.chars = new char[0];
	}
	
	public int getLength() {
		// easy way - sadly defeats the purpose of this
		//return chars.length;
		
		// silly way
		boolean noErrorYet = true;
		int index = 0;
		while(noErrorYet) {
			try {
				@SuppressWarnings("unused")
				char c = chars[index];
			} catch(Exception e) {
				noErrorYet = false;
				break;
			}
			index++;
		}
		return index;
	}
	
	public String toString() {
		return new String(chars);
	}
	
	public static char[] getChars(String string) {
		int length = string.length();
		char[] chars = new char[length];
		for(int i = 0; i < length; i++) {
			chars[i] = string.charAt(i);
		}
		return chars;
	}
	
	public static MyString convert(String string) {
		return new MyString(getChars(string));
	}
	
	public static MyString concatenate(MyString string1, MyString string2) {
		int string1Length = string1.getLength();
		int string2Length = string2.getLength();
		char[] chars = new char[string1Length + string2Length];
		
		int index = 0;
		for(; index < string1Length; index++) {
			chars[index] = string1.chars[index];
		}
		int count = 0;
		for(; count < string2Length; index++, count++) {
			chars[index] = string2.chars[count];
		}
		return new MyString(chars);
	}
	
	/**
	 * Inserts a string into another string at the specified index.
	 * 
	 * @param original The original string.
	 * @param string The string to insert.
	 * @param index The index at which to insert the string.
	 * 
	 * @return The new string.
	 * @throws IllegalArgumentException Thrown if the index specified is < 0 or
	 * greater than the length of the original string.
	 */
	public static MyString insert(MyString original, MyString string, int index) {
		if(index < 0 || index >= original.getLength()) {
			
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		MyString str1 = new MyString("I am the beginning ");
		MyString str2 = new MyString("of a sentence.");
		MyString str3 = concatenate(str1, str2);
		
		System.out.println(str3.toString());
	}

}
