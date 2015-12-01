package com.stabilise.util.io.beta;

import java.io.IOException;

public interface Sendable {
    
    void send(DataSender sender) throws IOException;
    
}
