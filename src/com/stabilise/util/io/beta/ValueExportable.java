package com.stabilise.util.io.beta;

import java.io.IOException;


public interface ValueExportable {
    
    void io(String name, DataObject o, boolean write) throws IOException;
    
}
