package com.stabilise.util.io.data.json;

import java.io.IOException;
import java.util.function.Consumer;

import com.badlogic.gdx.utils.JsonValue;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataList;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.ITag;

@Incomplete
public class JsonList extends AbstractDataList {
    
    private boolean dirty = true;
    private JsonValue json;
    
    JsonValue toJson() {
        if(!dirty)
            return json;
        dirty = false;
        
        return json;
    }
    
    JsonList fromJson(JsonValue json) {
        
        
        dirty = false;
        return this;
    }
    
    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public void addData(ITag data) {
        
    }
    
    @Override
    public ITag getNext() {
        return null;
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        throw new UnsupportedOperationException("Should not read a JsonList directly!");
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        throw new UnsupportedOperationException("Should not write a JsonList directly!");
    }
    
    @Override
    public Format format() {
        return Format.JSON;
    }
    
    @Override
    protected void forEach(Consumer<ITag> action) {
        // TODO Auto-generated method stub
    }
    
}
