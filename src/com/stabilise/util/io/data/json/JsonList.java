package com.stabilise.util.io.data.json;

import java.io.IOException;

import com.badlogic.gdx.utils.JsonValue;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataList;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.Tag;

@Incomplete
public class JsonList extends AbstractDataList {
    
    private boolean dirty = true;
    private JsonValue json;
    
    public JsonList() {
        // TODO Auto-generated constructor stub
    }
    
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
    public DataCompound addCompound() {
        return null;
    }
    
    @Override
    public DataList addList() {
        return null;
    }
    
    @Override
    public Tag getNext() {
        return null;
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        
    }
    
    @Override
    public void io(String name, DataCompound o, boolean write) {
        
    }
    
    @Override
    public void io(DataList l, boolean write) {
        
    }
    
    @Override
    protected boolean writeMode() {
        
        return false;
    }
    
    @Override
    public void add(Tag data) {
        
        
    }
    
}
