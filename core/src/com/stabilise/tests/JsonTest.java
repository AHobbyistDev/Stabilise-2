package com.stabilise.tests;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Resources;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;

public class JsonTest {
	
	private static FileHandle file = Resources.DIR_TESTS.child("jsonTest");
	
	public static void main(String[] args) throws IOException {
		//writeTest();
		readTest();
	}
	
	public static void writeTest() throws IOException {
		DataCompound c = Format.JSON.newCompound();
		c.put("Int1", 420);
		c.put("Double1", 12345.678D);
		DataCompound c2 = c.createCompound("SubThingy");
		c2.put("Int1.1", 64);
		
		IOUtil.write(file, c, Compression.UNCOMPRESSED);
		
		System.out.println("JSON file:");
		System.out.println(c.toString());
	}
	
	public static void readTest() throws IOException {
		DataCompound c = IOUtil.read(file, Format.JSON, Compression.UNCOMPRESSED);
		
		System.out.println("Read JSON file.");
		System.out.println(c.toString());
	}

}
