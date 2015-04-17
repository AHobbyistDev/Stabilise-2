package com.stabilise.util.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.stabilise.util.IOUtil;

/**
 * This class provides I/O operations for NBT files. In most cases, input and
 * output operations should be performed as such:
 * 
 * <pre>
 * FileHandle myNBTFile = ...
 * NBTTagCompound tag = ...
 * 
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
	 * @throws IOException if {@code file} represents a directory, does not
	 * exist, or an I/O error occurs.
	 */
	public static NBTTagCompound read(FileHandle file) throws IOException {
		InputStream is = null;
		try {
			is = file.read(); // usually a FileInputStream
			return read(is);
		} catch(GdxRuntimeException e) {
			throw new IOException(e);
		} finally {
			if(is != null)
				is.close();
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
	 * @throws IOException if an I/O error occurs.
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
	 * @throws IOException if {@code file} represents a directory, does not
	 * exist, or an I/O error occurs.
	 */
	public static NBTTagCompound readCompressed(FileHandle file) throws IOException {
		GZIPInputStream gis = null;
		try {
			gis = new GZIPInputStream(file.read());
			return read(gis);
		} catch(GdxRuntimeException e) {
			throw new IOException(e);
		} finally {
			if(gis != null)
				gis.close();
		}
	}
	
	/**
	 * Reads an NBT file.
	 * 
	 * @param in The input stream.
	 * 
	 * @return The compound tag constituting the file.
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws IOException if an I/O error occurs.
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
	 * @param in The input stream.
	 * 
	 * @return The tag.
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws IOException if an I/O error occurs.
	 */
	static NBTTag readTag(DataInputStream in) throws IOException {
		byte id = in.readByte();
		
		if(id == NBTTag.COMPOUND_END) {
			return new NBTTagCompoundEnd();
		} else {
			String name = in.readUTF();
			NBTTag tag = NBTTag.createTag(id, name); // should never be null
			tag.load(in);		// May throw an IOException (or an NPE if
			return tag;			// something went really wrong)
		}
	}
	
	//--------------------==========--------------------
	//-----------------=====Output=====-----------------
	//--------------------==========--------------------
	
	/**
	 * Writes a compound NBT tag to a file. If the file already exists, it will
	 * be deleted and replaced by this one.
	 * 
	 * @param file The file to write the tag to.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException if {@code file} represents a directory, is an
	 * invalid type (classpath or internal), or an I/O error occurs.
	 */
	public static void write(FileHandle file, NBTTagCompound tag) throws IOException {
		if(file.exists())
			file.delete();
		OutputStream out = null;
		try {
			out = file.write(false); // usually a FileOutputStream
			write(out, tag);
		} catch(GdxRuntimeException e) {
			throw new IOException(e);
		} finally {
			if(out != null)
				out.close();
		}
	}
	
	/**
	 * Writes a compound NBT tag to a file.
	 * 
	 * @param out The output stream.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException if an I/O error occurs.
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
	 * Writes a compound NBT tag to a file in a compressed format. If the file
	 * already exists, it will be deleted and replaced by this one.
	 * 
	 * @param file The file to write the tag to.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException if {@code file} represents a directory, is an
	 * invalid type (classpath or internal), or an I/O error occurs.
	 */
	public static void writeCompressed(FileHandle file, NBTTagCompound tag) throws IOException {
		if(file.exists())
			file.delete();
		GZIPOutputStream gos = null;
		try {
			gos = new GZIPOutputStream(file.write(false));
			write(gos, tag);
		} catch(GdxRuntimeException e) {
			throw new IOException(e);
		} finally {
			if(gos != null)
				gos.close();
		}
	}
	
	/**
	 * Writes an NBT tag to the given output stream.
	 * 
	 * @param out The output stream.
	 * @param tag The tag to write.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException if an I/O error occurs.
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
	 * @throws NullPointerException if either argument {@code null}.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void safeWrite(FileHandle file, NBTTagCompound tag) throws IOException {
		FileHandle tempFile = IOUtil.safelySaveFile1(file);
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
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void safeWriteCompressed(FileHandle file, NBTTagCompound tag) throws IOException {
		FileHandle tempFile = IOUtil.safelySaveFile1(file);
		writeCompressed(tempFile, tag);
		IOUtil.safelySaveFile2(file);
	}
	
}
