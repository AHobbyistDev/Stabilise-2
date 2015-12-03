package com.stabilise.util.io.beta;

import java.io.IOException;

public interface Exportable {
    
    void io(DataObject o, boolean write) throws IOException;
    
}
