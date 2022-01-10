package com.stabilise.util.io.data.json;

import java.io.IOException;

import com.badlogic.gdx.utils.JsonValue;
import com.stabilise.util.box.BoolBox;
import com.stabilise.util.box.F64Box;
import com.stabilise.util.box.I64Box;
import com.stabilise.util.box.StringBox;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataList;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.IData;


public class JsonList extends AbstractDataList {
    
    private boolean dirty = true;
    /** Cached json. */
    private JsonValue json;
    
    
    /**
     * Creates an empty JsonList.
     */
    public JsonList() {
        super();
    }
    
    /**
     * Creates a new JsonList and populates it from the given JsonValue.
     *
     * @throws IllegalArgumentException if {@code !json.isArray()}
     */
    public JsonList(JsonValue json) {
        super(json.size);
        fromJson(json);
    }
    
    JsonValue toJson() {
        if(!dirty)
            return json;
        dirty = false;
    
        json = new JsonValue(JsonValue.ValueType.array);
        if(data.size() == 0)
            return json;
        JsonValue child = null;
        JsonValue prev = null;
    
        for(IData val : data) {
            switch(val.type()) {
                case COMPOUND:
                    child = ((JsonCompound) val).toJson();
                    break;
                case LIST:
                    child = ((JsonList) val).toJson();
                    break;
                case BOOL:
                    child = new JsonValue(val.asBool().get());
                    break;
                case I8:
                    child = new JsonValue(val.asI8().get());
                    break;
                case I16:
                    child = new JsonValue(val.asI16().get());
                    break;
                case I32:
                    child = new JsonValue(val.asI32().get());
                    break;
                case I64:
                    child = new JsonValue(val.asI64().get());
                    break;
                case F32:
                    child = new JsonValue(val.asF32().get());
                    break;
                case F64:
                    child = new JsonValue(val.asF64().get());
                    break;
                case I8ARR:
                    child = JsonUtils.I8ArrToJson(val.asI8Arr().get());
                    break;
                case I32ARR:
                    child = JsonUtils.I32ArrToJson(val.asI32Arr().get());
                    break;
                case I64ARR:
                    child = JsonUtils.I64ArrToJson(val.asI64Arr().get());
                    break;
                case F32ARR:
                    child = JsonUtils.F32ArrToJson(val.asF32Arr().get());
                    break;
                case F64ARR:
                    child = JsonUtils.F64ArrToJson(val.asF64Arr().get());
                    break;
                case STRING:
                    child = new JsonValue(val.asString().get());
                    break;
            }
            
            child.parent = json;
            if(prev == null) {
                json.child = child;
            } else {
                prev.next = child;
                child.prev = prev;
            }
            prev = child;
            json.size++;
        }
        
        return json;
    }
    
    JsonList fromJson(JsonValue json) {
        if(!json.isArray())
            throw new IllegalArgumentException("Not an array!");
        
        data.clear();
        
        if(json.size == 0 || json.child == null) {
            this.json = json;
            dirty = false;
            return this;
        }
        
        // Need to ensure all entries are of the same type
        JsonValue.ValueType type = json.child.type();
        
        for(JsonValue child = json.child; child != null; child = child.next) {
            if(child.type() != type)
                throw new IllegalArgumentException("List contains two incompatible " +
                        "types: " + type + " and " + child.type());
    
            switch(json.child.type()) {
                case object:
                    data.add(new JsonCompound(child));
                    break;
                case array:
                    data.add(new JsonList(child));
                    break;
                case booleanValue:
                    data.add(new BoolBox(child.asBoolean()));
                    break;
                case stringValue:
                    data.add(new StringBox(child.asString()));
                    break;
                case longValue:
                    // Shouldn't get to this branch if done properly but ah well
                    data.add(new I64Box(child.asLong()));
                    break;
                case doubleValue:
                    // Shouldn't get to this branch if done properly but ah well
                    data.add(new F64Box(child.asDouble()));
                    break;
                default:
                    throw new IllegalStateException("Invalid data type " + json.child.type());
            }
        }
        
        this.json = json;
        dirty = false;
        
        return this;
    }
    
    @Override
    protected void setDirty() {
        dirty = true;
        json = null; // no longer needed
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
