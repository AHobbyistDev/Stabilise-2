package com.stabilise.tests;

import java.util.Random;

import com.stabilise.util.TaskTimer;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.util.nbt.NBTTagList;
import com.stabilise.util.nbt.export.ExportToNBT;
import com.stabilise.util.nbt.export.Exportable;
import com.stabilise.util.nbt.export.NBTExporter;


public class NBTExportTest {
	
	@ExportToNBT
	int anInt;
	@ExportToNBT
	long aLong;
	@ExportToNBT
	float aFloat;
	@ExportToNBT
	double aDouble;
	
	@ExportToNBT
	int[] intArr;
	@ExportToNBT
	byte[] byteArr;
	
	@ExportToNBT
	InnerClass1 obj;
	
	@ExportToNBT
	InnerClass1[] objArr;
	
	int ignoredField;
	
	public NBTExportTest() {
		Random rnd = new Random();
		anInt = rnd.nextInt();
		aLong = rnd.nextLong();
		aFloat = rnd.nextFloat();
		aDouble = rnd.nextDouble();
		intArr = new int[10 + rnd.nextInt(10)];
		for(int i = 0; i < intArr.length; i++)
			intArr[i] = rnd.nextInt();
		byteArr = new byte[10 + rnd.nextInt(10)];
		for(int i = 0; i < byteArr.length; i++)
			byteArr[i] = (byte)rnd.nextInt();
		
		obj = new InnerClass1();
		populate(rnd, obj);
		
		objArr = new InnerClass1[5 + rnd.nextInt(5)];
		for(int i = 0; i < objArr.length; i++)
			populate(rnd, objArr[i] = new InnerClass1());
		
		ignoredField = rnd.nextInt();
	}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.addInt("anInt", anInt);
		tag.addLong("aLong", aLong);
		tag.addFloat("aFloat", aFloat);
		tag.addDouble("aDouble", aDouble);
		tag.addIntArray("intArr", intArr);
		tag.addByteArray("byteArr", byteArr);
		tag.addCompound("obj", obj.toNBT());
		NBTTagList list = new NBTTagList();
		for(InnerClass1 o : objArr)
			list.appendTag(o.toNBT());
		tag.addList("objArr", list);
		return tag;
	}
	
	private void populate(Random rnd, InnerClass1 obj) {
		obj.numCacti = rnd.nextInt();
		obj.aShort = (short)rnd.nextInt();
		obj.aByte = (byte)rnd.nextInt();
		obj.ignoredField = rnd.nextFloat();
	}
	
	@Exportable
	private static class InnerClass1 {
		@ExportToNBT
		private int numCacti;
		@ExportToNBT
		short aShort;
		@ExportToNBT
		byte aByte;
		
		@SuppressWarnings("unused")
		float ignoredField;
		
		NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.addInt("numCacti", numCacti);
			tag.addShort("aShort", aShort);
			tag.addByte("aByte", aByte);
			return tag;
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		final int numObjs = 50000;
		final int tests = 10;
		NBTExportTest[] objs = new NBTExportTest[numObjs];
		for(int i = 0; i < numObjs; i++)
			objs[i] = new NBTExportTest();
		for(int i = 0; i < tests; i++) {
			test(objs);
			Thread.sleep(50L);
		}
	}
	
	private static void test(NBTExportTest[] objs) {
		TaskTimer t1 = new TaskTimer("Normal");
		TaskTimer t2 = new TaskTimer("Reflection");
		
		t1.start();
		for(int i = 0; i < objs.length; i++)
			objs[i].toNBT();
		t1.stop();
		
		t2.start();
		for(int i = 0; i < objs.length; i++)
			NBTExporter.exportObj(objs[i]);
		t2.stop();
		
		t1.printResult();
		t2.printResult();
		t1.printComparison(t2);
	}
	
}
