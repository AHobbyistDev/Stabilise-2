package com.stabilise.util.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.stabilise.util.IOUtil;

/**
 * This class provides I/O operations for NBT files. In most cases, input and
 * output operations should be performed as such:
 * 
 * <pre>
 * // Input/Loading
 * NBTTagCompound tag = {@link #read(File) NBTIO.read(myNBTFile)};
 * // Output/Saving
 * {@link #write(File, NBTTagCompound) NBTIO.write(myNBTFile, tag)};
 * 
 * // Alternatively - Safe Output/Saving (use this for important files)
 * {@link #safeWrite(File, NBTTagCompound) NBTIO.safeWrite(myNBTFile, tag)};
 * </pre>
 * 
 * <p>Compressed alternatives are provided for both reading and writing, and
 * are incompatible with their uncompressed variants. The compression method
 * used is GZIP compression, and is accomplished by wrapping the input and
 * output streams in {@link GZIPInputStream} and {@link GZIPOutputStream}
 * objects where appropriate.
 */
public class NBTIO {
	
	// non-instantiable
	private NBTIO() {}
	
	//--------------------==========--------------------
	//------------------=====Input=====-----------------
	//--------------------==========--------------------
	
	/**
	 * Reads an NBT file.
	 * 
	 * @param file The file to read from.
	 * 
	 * @return The compound tag constituting the file.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 * @throws IOException if an I/O exception is encountered while loading the
	 * file.
	 */
	public static NBTTagCompound read(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return read(fis);
		} finally {
			fis.close();
		}
	}
	
	/**
	 * Reads an NBT file.
	 * 
	 * @param in The input stream to read from, typically a FileInputStream.
	 * This stream is not closed by this method.
	 * 
	 * @return The compound tag constituting the file.
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws IOException if an I/O exception is encountered while loading the
	 * file.
	 */
	public static NBTTagCompound read(InputStream in) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		DataInputStream dis = new DataInputStream(bis);
		
		try {
			return read(dis);
		} finally {
			dis.close();
			bis.close();
		}
	}
	
	/**
	 * Reads a compressed NBT file.
	 * 
	 * @param file The file to read from.
	 * 
	 * @return The compound tag constituting the file.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 * @throws IOException if an I/O exception is encountered while loading the
	 * file.
	 */
	public static NBTTagCompound readCompressed(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		GZIPInputStream gis = new GZIPInputStream(fis);
		try {
			return read(gis);
		} finally {
			gis.close();
			fis.close();
		}
	}
	
	/**
	 * Reads an NBT file.
	 * 
	 * @param in The DataInputStream object linking to the file to be read
	 * from.
	 * 
	 * @return The compound tag constituting the file.
	 * @throws NullPointerException Thrown if {@code in} is {@code null}.
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * loading the file.
	 */
	private static NBTTagCompound read(DataInputStream in) throws IOException {
		NBTTag tag = readTag(in);

        if(tag instanceof NBTTagCompound)
            return (NBTTagCompound)tag;
        else
            throw new IOException("Root tag must be a named compound tag");
	}
	
	/**
	 * Reads an NBT tag from the given input stream.
	 * 
	 * @param in The DataInputStream to read the tag from.
	 * 
	 * @return The tag.
	 * @throws NullPointerException Thrown if {@code in} is {@code null}.
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * loading the file.
	 */
	static NBTTag readTag(DataInputStream in) throws IOException {
		byte id = in.readByte();
		
		if(id == NBTTag.COMPOUND_END) {
			return new NBTTagCompoundEnd();
		} else {
			String name = in.readUTF();
			NBTTag tag = NBTTag.createTag(id, name);
			tag.load(in);		// May throw an IOException
			return tag;
		}
	}
	
	//--------------------==========--------------------
	//-----------------=====Output=====-----------------
	//--------------------==========--------------------
	
	/**
	 * Writes a compound NBT tag to a file.
	 * 
	 * @param file The file to write the tag to.
	 * @param tag The tag to write.
	 * 
	 * @throws IllegalArgumentException if {@code file} is {@code null}.
	 * @throws NullPointerException 
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * writing the file.
	 */
	public static void write(File file, NBTTagCompound tag) throws IOException {
		IOUtil.createParentDirQuietly(file);
		FileOutputStream out = new FileOutputStream(file);
		try {
			write(out, tag);
		} finally {
			out.close();
		}
	}
	
	/**
	 * Writes a compound NBT tag to a file.
	 * 
	 * @param out The OutputStream linking to the file to be written to; this
	 * is typically a FileOutputStream.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if either {@code out} or {@code tag} are
	 * {@code null}.
	 * @throws IOException if an I/O exception is encountered while writing the
	 * file.
	 */
	public static void write(OutputStream out, NBTTagCompound tag) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(out);
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			writeTag(dos, tag);
		} finally {
			dos.close();
			bos.close();
		}
	}
	
	/**
	 * Writes a compound NBT tag to a file in a compressed format.
	 * 
	 * @param file The file to write the tag to.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if either {@code file} or {@code tag} are
	 * {@code null}.
	 * @throws IOException if an I/O exception is encountered while
	 * writing the file.
	 */
	public static void writeCompressed(File file, NBTTagCompound tag) throws IOException {
		IOUtil.createParentDirQuietly(file);
		FileOutputStream fos = new FileOutputStream(file);
		GZIPOutputStream gos = new GZIPOutputStream(fos);
		try {
			write(gos, tag);
		} finally {
			gos.close();
			fos.close();
		}
	}
	
	/**
	 * Writes an NBT tag to the given output stream.
	 * 
	 * @param out The OutputStream object linking to the file to be written
	 * to.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if {@code out} or {@code tag} is {@code
	 * null}.
	 * @throws IOException if an I/O exception is encountered while writing the
	 * file.
	 */
	static void writeTag(DataOutputStream out, NBTTag tag) throws IOException {
		out.writeByte(tag.getId());
		
		if(tag.getId() != NBTTag.COMPOUND_END) {
			out.writeUTF(tag.name);
			tag.write(out);
		}
	}
	
	/**
	 * Safely writes an NBT compound tag to a file - that is, only deletes the
	 * pre-existing file once the write is complete. This is to prevent a loss
	 * of data if the save process is interrupted midway.
	 * 
	 * @param file The file to write the tag to.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if either {@code file} or {@code tag} are
	 * {@code null}.
	 * @throws IOException if an I/O exception is encountered while writing the
	 * file.
	 */
	public static void safeWrite(File file, NBTTagCompound tag) throws IOException {
		File tempFile = IOUtil.safelySaveFile1(file);
		write(tempFile, tag);
		IOUtil.safelySaveFile2(file);
	}
	
	/**
	 * Safely writes an NBT compound tag to a compressed file - that is, only
	 * deletes the pre-existing file once the write is complete. This is to
	 * prevent a loss of data if the save process is interrupted midway.
	 * 
	 * @param file The file to write the tag to.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if either {@code file} or {@code tag} are
	 * {@code null}.
	 * @throws IOException if an I/O exception is encountered while writing the
	 * file.
	 */
	public static void safeWriteCompressed(File file, NBTTagCompound tag) throws IOException {
		File tempFile = IOUtil.safelySaveFile1(file);
		writeCompressed(tempFile, tag);
		IOUtil.safelySaveFile2(file);
	}

}
