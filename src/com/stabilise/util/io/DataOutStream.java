package com.stabilise.util.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.stabilise.util.maths.Maths;


public class DataOutStream extends DataOutputStream {
    
    public DataOutStream(OutputStream out) {
        super(out);
    }
    
    /**
     * Writes a string.
     * 
     * @throws NullPointerException if {@code string} is {@code null}.
     * @throws IllegalArgumentException if {@code string} exceeds 65535
     * characters.
     * @throws IOException
     */
    public void writeString(String string) throws IOException {
        if(string.length() > Maths.USHORT_MAX_VALUE)
            throw new IllegalArgumentException("The given string is too large!");
        writeShort(string.length());
        writeChars(string);
    }
    
    /**
     * Writes an int array.
     * 
     * @throws NullPointerException if {@code arr} is {@code null}.
     * @throws IOException
     */
    public void writeIntArray(int[] arr) throws IOException {
        for(int i = 0; i < arr.length; i++)
            writeInt(arr[i]);
    }
    
}
