package com.stabilise.network;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.util.nbt.export.NBTExporter;

/**
 * A ReflectivePacket reads and writes itself by converting itself to and from
 * an NBT tag (using {@link NBTExporter}) and sending that tag over a
 * connection.
 */
public abstract class ReflectivePacket extends Packet {
    
    @Override
    public void readData(DataInStream in) throws IOException {
        NBTExporter.importCompletely(this, NBTTagCompound.read(in));
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        NBTExporter.exportCompletely(this).writeData(out);
    }
    
}
