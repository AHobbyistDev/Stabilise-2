package com.stabilise.network;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.ObjectExporter;

/**
 * A ReflectivePacket reads and writes itself by converting itself to and from
 * an NBT tag (using {@link NBTExporter}) and sending that tag over a
 * connection.
 */
public abstract class ReflectivePacket extends Packet {
    
    @Override
    public void readData(DataInStream in) throws IOException {
        ObjectExporter.importObj(this, Format.NBT_SIMPLE.read(in));
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        ObjectExporter.exportObj(this, Format.NBT_SIMPLE).writeData(out);
    }
    
}
