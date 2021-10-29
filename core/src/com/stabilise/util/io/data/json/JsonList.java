package com.stabilise.util.io.data.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.utils.JsonValue;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataList;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.IData;

@Incomplete
public class JsonList extends AbstractDataList {
    
    
    private boolean dirty = true;
    private JsonValue json;
    
    public JsonList() {
        super();
    }
    
    JsonValue toJson() {
        if(!dirty)
            return json;
        dirty = false;
        
        json = new JsonValue(JsonValue.ValueType.array);
        
        return json;
    }
    
    JsonList fromJson(JsonValue json) {
        dirty = false;
        return this;
    }
    
    @Override
    public int size() {
        return data.size();
    }
    
    @Override
    public boolean hasNext() {
        return index < size();
    }
    
    @Override
    public void addData(IData d) {
        super.addData(d);
        dirty = true;
    }
    
    @Override
    protected void addData2(IData d) {
        super.addData2(d);
        dirty = true;
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
    
}
