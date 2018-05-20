package com.stabilise.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.common.io.Files;
import com.stabilise.util.Checks;
import com.stabilise.util.Log;
import com.stabilise.util.StringUtil;
import com.stabilise.util.collect.EmptyCollection;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;

/**
 * This class provides some static utility IO methods.
 */
public class IOUtil {
    
    // Allows only lowercase+uppercase letters, all numbers, spaces (\u0020),
    // parentheses, periods, dashes, and inverted commas 
    /** Regex for illegal filename characters. */
    public static final Pattern ILLEGAL_FILENAME_REGEX = Pattern.compile("[^a-zA-Z0-9\\u0020().'-]");
    
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
     * @throws GdxRuntimeException if {@code handle} is an internal or
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
     * @throws GdxRuntimeException if {@code handle} is an internal or
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
        return ILLEGAL_FILENAME_REGEX.matcher(str).replaceAll("_");
    }
    
    /**
     * This method ensures a file is safely saved by writing the data to a
     * temporary file and then renaming the temp file to the desired file name.
     * This is done as to ensure data is not lost if for some reason the save
     * process is interrupted and it is desirable to retain the earlier version
     * of the file.
     * 
     * @param file The destination file.
     * @param saveOperation The save operation itself. It should write to the
     * file passed to it.
     * 
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws GdxRuntimeException if {@code file} is an internal or classpath
     * file.
     * @throws IOException if an I/O error occurs.
     */
    public static void safelySaveFile(FileHandle file,
            IOConsumer<FileHandle> saveOperation) throws IOException {
        FileHandle tmp = file.sibling(file.name() + "_tmp");
        if(tmp.exists() && !tmp.delete())
            throw new IOException("Failed to delete " + tmp);
        saveOperation.accept(tmp);
        if(file.exists() && !file.delete())
            throw new IOException("Failed to delete " + file);
        else if(!tmp.file().renameTo(file.file()))
            throw new IOException("Failed to rename " + tmp);
    }
    
    /**
     * Reads a file into a compound of the given format. The returned compound
     * will be in read mode.
     * 
     * @throws NullPointerException if any argument is null.
     * @throws IOException if an I/O error occurs.
     */
    public static DataCompound read(FileHandle file, Format format,
    		Compression compression) throws IOException {
        try(DataInStream in = new DataInStream(new BufferedInputStream(
                compression.wrap(file.read())))) {
            DataCompound c = format.read(in);
            return c;
        } catch(GdxRuntimeException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Reads a file into the given compound.
     * 
     * @throws NullPointerException if any argument is null.
     * @throws IOException if an I/O error occurs.
     */
    public static DataCompound read(FileHandle file, DataCompound c,
    		Compression compression) throws IOException {
        try(DataInStream in = new DataInStream(new BufferedInputStream(
                compression.wrap(file.read())))) {
            c.format().read(in, c);
            return c;
        } catch(GdxRuntimeException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Writes a file. If the file already exists, it will be overwritten.
     * 
     * @throws NullPointerException if any argument is null.
     * @throws IOException if an I/O error occurs.
     */
    public static void write(FileHandle file, DataCompound data,
    		Compression compression) throws IOException {
        try(DataOutStream out = new DataOutStream(new BufferedOutputStream(
                compression.wrap(file.write(false))))) {
            data.format().write(out, data);
        } catch(GdxRuntimeException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Safely (as per {@link #safelySaveFile(FileHandle, IOConsumer)
     * safelySaveFile}{@code ()}) saves a file (as per {@link
     * #write(FileHandle, DataCompound, Compression) write}{@code ()}).
     * 
     * @throws NullPointerException if any argument is null.
     * @throws IOException if an I/O error occurs.
     */
    public static void writeSafe(FileHandle file, DataCompound data,
            Compression compression) throws IOException {
        safelySaveFile(file, f -> write(f, data, compression));
    }
    
    
    /**
     * Counts the number of bytes which would be written by {@code data} in the
     * specified format with the specified compression.
     * 
     * @throws NullPointerException if any argument is null.
     */
    public static int countBytes(DataCompound data, Format format, Compression compression) {
        try(ByteCountingStream bcs = new ByteCountingStream();
            DataOutStream out = new DataOutStream(new BufferedOutputStream(
                    compression.wrap(bcs)))) {
            format.write(out, data.convert(format));
            return bcs.byteCount();
        } catch(Throwable t) {
            return 0;
        }
    }
    
    /**
     * Exports a file (or directory and its contents) to a ZIP file.
     * 
     * @param dir The file/directory to export.
     * @param zipFile The file to export to.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @see #unzip(FileHandle, FileHandle)
     */
    public static void zip(FileHandle dir, FileHandle zipFile) throws IOException {
        zip(dir, zipFile, EmptyCollection.get(), false);
    }
    
    /**
     * Exports a file (or directory and its contents) to a ZIP file.
     * 
     * @param dir The file/directory to export.
     * @param zipFile The file to export to.
     * @param excluded Files/directories to ignore if {@code invert} is false;
     * otherwise the files/directories to include.
     * @param invert If {@code excluded} should be treated as an inclusion list
     * instead.
     * 
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @see #unzip(FileHandle, FileHandle)
     */
    public static void zip(FileHandle dir, FileHandle zipFile,
            Collection<FileHandle> excluded, boolean invert)
            throws IOException {
        URI base = dir.file().toURI();
        
        Deque<FileHandle> queue = new LinkedList<>();
        queue.push(dir);
        
        //OutputStream out = null;
        //ZipOutputStream zout = null;
        
        try(ZipOutputStream zout = new ZipOutputStream(zipFile.write(false))) {
            while(!queue.isEmpty()) {
                dir = queue.pop();
                
                for(FileHandle f : dir.list()) {
                    boolean excl = excluded.contains(f);
                    
                    // check parents
                    if(!excl) { 
                        for(FileHandle h : excluded) {
                            if(f.path().contains(h.path())) {
                                excl = true;
                                break;
                            }
                        }
                    }
                    
                    if((excl && !invert) || (!excl && invert))
                        continue;
                    
                    String name = base.relativize(f.file().toURI()).getPath();
                    if(f.isDirectory()) {
                        queue.push(f);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        copy(f, zout);
                        zout.closeEntry();
                    }
                }
            }
        }
    }
    
    /**
     * Imports a file or directory from a zip file.
     * 
     * @param zipFile The file to import from.
     * @param dir The file or directory to import to.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @see #zip(FileHandle, FileHandle)
     */
    public static void unzip(FileHandle zipFile, FileHandle dir) throws IOException {
        try(ZipFile zFile = new ZipFile(zipFile.file())) {
            Enumeration<? extends ZipEntry> entries = zFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                FileHandle file = dir.child(entry.getName());
                if(entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.parent().mkdirs();
                    try(InputStream in = zFile.getInputStream(entry)) {
                        copy(in, file);
                    }
                }
            }
        }
    }
    
    /**
     * Sets the size of the byte buffer to use for copying, in {@link
     * #copy(InputStream, OutputStream)}. The default value is 8192 (8kb).
     * 
     * @throws IllegalArgumentException if {@code size <= 0}.
     * @see #copy(FileHandle, OutputStream)
     * @see #copy(FileHandle, OutputStream)
     * @see #copy(InputStream, FileHandle)
     */
    public static void setCopyBuffer(int size) {
        bufSize = Checks.testMin(size, 1);
        if(size < 1024)
            Log.get().postWarning("Very small buffer size set! " + size + " < 1024 (1kb)");
    }
    
    private static int bufSize = 8*1024; // 8kb
    
    /**
     * Copies everything from {@code in} into {@code out}.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @see #setCopyBuffer(int)
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[bufSize];
        int count;
        while((count = in.read(buf)) > 0) {
            out.write(buf, 0, count);
        }
    }
    
    /**
     * Copies up to {@code bytes} bytes from {@code in} into {@code out}.
     * 
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code bytes < 0}.
     * @throws IOException if an I/O error occurs.
     * @see #setCopyBuffer(int)
     */
    public static void copy(InputStream in, OutputStream out, long bytes) throws IOException {
        Checks.testMin(bytes, 0);
        int len = bufSize;
        byte[] buf = new byte[len];
        int count;
        while((count = in.read(buf, 0, (int)Math.min(len, bytes))) > 0) {
            out.write(buf, 0, count);
            bytes -= count;
        }
    }
    
    /**
     * Copies everything from {@code in} into {@code out}.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @see #copy(InputStream, OutputStream)
     * @see #setCopyBuffer(int)
     */
    public static void copy(FileHandle in, OutputStream out) throws IOException {
        try(InputStream is = in.read()) {
            copy(is, out);
        } catch(GdxRuntimeException e) {
            throw new IOException();
        }
    }
    
    /**
     * Copies everything from {@code in} into {@code out}.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @see #copy(InputStream, OutputStream)
     * @see #setCopyBuffer(int)
     */
    public static void copy(InputStream in, FileHandle out) throws IOException {
        try(OutputStream os = out.write(false)) {
            copy(in, os);
        } catch(GdxRuntimeException e) {
            throw new IOException();
        }
    }
    
    /**
     * Sends a file ({@code in}) across {@code out}. The file should be
     * received using {@link #receiveFile(DataInputStream, FileHandle)}.
     * 
     * @param checksum Whether or not to send a checksum of the file.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IOException if an I/O error occurs.
     */
    public static void sendFile(FileHandle in, DataOutputStream out, boolean checksum) throws IOException {
        // We send the file in chunks: we load into the buffer, send our peer
        // the amount loaded into the buffer, and send. Finally, we send an
        // optional checksum.
        byte[] buf = new byte[bufSize];
        int count;
        
        MessageDigest md = null;
        if(checksum) {
            try {
                md = MessageDigest.getInstance("MD5");
            } catch(NoSuchAlgorithmException e) {
                throw new Error(); // shouldn't happen
            }
        }
        
        out.writeBoolean(checksum);
        
        try(InputStream is = in.read()) {
            while((count = is.read(buf)) > 0) {
                out.writeInt(count);
                out.write(buf, 0, count);
                
                if(checksum)
                    md.update(buf, 0, count);
            }
        } catch(GdxRuntimeException e) {
            throw new IOException(e);
        }
        
        out.writeInt(0); // tells our peer there are no more bytes to read
        
        if(checksum) {
            buf = md.digest();
            out.writeInt(buf.length);
            out.write(buf);
        }
    }
    
    /**
     * Receives a file ({@code out}) from {@code in}. The file should have been
     * sent using {@link #sendFile(FileHandle, DataOutputStream, boolean)}.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IOException if an I/O error occurs.
     */
    public static void receiveFile(DataInputStream in, FileHandle out) throws IOException {
        int len = bufSize;
        byte[] buf = new byte[len];
        int count = 0, read;
        MessageDigest md = null;
        
        boolean checksum = in.readBoolean();
        if(checksum) {
            try {
                md = MessageDigest.getInstance("MD5");
            } catch(NoSuchAlgorithmException e) {
                throw new Error(); // shouldn't happen
            }
        }
        
        try(OutputStream os = out.write(false)) {
            while(count > 0 || (count = in.readInt()) > 0) {
                read = in.read(buf, 0, Math.min(len, count));
                if(read > 0) {
                    count -= read;
                    os.write(buf, 0, read);
                    
                    if(checksum)
                        md.update(buf, 0, read);
                } else {
                    // this branch shouldn't happen unless our peer abruptly
                    // disconnects or something
                    break;
                }
            }
        } catch(GdxRuntimeException e) {
            throw new IOException(e);
        }
        
        if(checksum) {
            byte[] ours = md.digest();
            byte[] theirs = new byte[in.readInt()];
            in.read(theirs);
            if(!Arrays.equals(ours, theirs))
                throw new UnequalChecksumException(theirs, ours);
        }
    }
    
    /**
     * Computes the MD5 checksum of the specified file.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public static byte[] checksum(FileHandle file) throws IOException {
        int len = bufSize;
        byte[] buf = new byte[len];
        int count = 0;
        MessageDigest md = null;
        
        try {
            md = MessageDigest.getInstance("MD5");
        } catch(NoSuchAlgorithmException e) {
            throw new Error(); // shouldn't happen
        }
        
        try(InputStream is = file.read()) {
            while((count = is.read(buf)) > 0) {
                md.update(buf, 0, count);
            }
        } catch(GdxRuntimeException e) {
            throw new IOException(e);
        }
        
        return md.digest();
    }
    
    /**
     * Reads a text file from the file system. Each element in the returned
     * array represents one line of the file.
     * 
     * @param file The file.
     * 
     * @return The file's contents.
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IOException if the file does not exist, or an I/O error occurs.
     */
    public static List<String> readTextFile(FileHandle file) throws IOException {
        if(!file.exists())
            throw new IOException("Text resource does not exist!");
        
        List<String> strings = new ArrayList<>();
        String s;
        
        try(BufferedReader br = new BufferedReader(file.reader())) {
            while((s = br.readLine()) != null)
                strings.add(s);
        } catch(GdxRuntimeException e) {
            throw new IOException(e);
        }
        
        return strings;
    }
    
    /**
     * Writes a text file to the file system.
     * 
     * @param file The file.
     * @param lines The lines to write.
     * 
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IOException if an I/O error occurs.
     */
    public static void writeTextFile(FileHandle file, String... lines) throws IOException {
        try(BufferedWriter bw = new BufferedWriter(file.writer(true))) {
            for(String line : lines)
                bw.append(line);
        } catch(GdxRuntimeException e) {
            throw new IOException(e);
        }
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * A alternative utility interface to {@link java.util.function.Consumer
     * Consumer} which may throw an IOException.
     */
    @FunctionalInterface
    public static interface IOConsumer<T> {
        
        /**
         * Performs an action on the given parameter.
         * 
         * @throws IOException if an I/O error occurs.
         */
        void accept(T t) throws IOException;
        
    }
    
    /**
     * A alternative utility interface to {@code Runnable} which may throw an
     * IOException.
     */
    @FunctionalInterface
    public static interface IORunnable {
        
        void run() throws IOException;
        
    }
    
    /**
     * An exception indicating that a calculated checksum is not equal to the
     * expected value.
     */
    public static class UnequalChecksumException extends IOException {
        private static final long serialVersionUID = 6321999553325845378L;
        
        public final byte[] expectedChecksum;
        public final byte[] calculatedChecksum;
        
        public UnequalChecksumException(byte[] expectedChecksum, byte[] calculatedChecksum) {
            super(msg(expectedChecksum, calculatedChecksum));
            this.expectedChecksum = expectedChecksum;
            this.calculatedChecksum = calculatedChecksum;
        }
        
        private static String msg(byte[] expected, byte[] ours) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unequal checksums! Expected value is \"");
            sb.append(StringUtil.toHexString(expected));
            sb.append("\", but we calculated \"");
            sb.append(StringUtil.toHexString(ours));
            sb.append("\".");
            return sb.toString();
        }
        
    }

}
