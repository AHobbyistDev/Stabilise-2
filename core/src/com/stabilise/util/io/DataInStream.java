package com.stabilise.util.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * An extension of DataInputStream which also provides convenience methods to
 * read strings and int arrays.
 */
public class DataInStream extends DataInputStream {
    
    public DataInStream(InputStream in) {
        super(in);
    }
    
    /**
     * Reads and returns a string.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public String readString() throws IOException {
        int length = readShort();
        StringBuilder sb = new StringBuilder(length);
        while(length-- > 0)
            sb.append(readChar());
        return sb.toString();
    }
    
    /**
     * Reads an int array and stores the data in the provided array. The number
     * of ints read is equal to the length of the array.
     * 
     * @throws NullPointerException if {@code arr} is {@code null}.
     * @throws IOException if an i/o error occurs.
     */
    public void readIntArray(int[] arr) throws IOException {
        for(int i = 0; i < arr.length; i++)
            arr[i] = readInt();
    }
    
}
