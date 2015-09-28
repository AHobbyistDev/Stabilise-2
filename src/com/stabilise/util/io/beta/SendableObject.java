package com.stabilise.util.io.beta;

import java.io.IOException;

import com.stabilise.util.box.IntBox;
import com.stabilise.util.box.StringBox;
import com.stabilise.util.io.beta.DataSender.Format;

public class SendableObject implements Sendable {
    
    private final StringBox name = new StringBox();
    private final IntBox id = new IntBox();
    
    public SendableObject() {
        
    }
    
    public void save() throws IOException {
        DataSender sender = new DataSender();
        sender.open(Format.NBT, true);
        send(sender);
        sender.close();
    }
    
    public void load() throws IOException {
        
    }
    
    @Override
    public void send(DataSender sender) throws IOException {
        sender.io("name", name);
        sender.io("id", id);
    }
    
}
