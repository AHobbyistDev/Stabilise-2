package com.stabilise.util.io.data.json;

import static com.stabilise.util.box.Boxes.box;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.IData;


/**
 * Internally a MapCompound just like NBT, but converts to/from a JsonValue
 * object when reading/writing since there's no way in hell that I'm gonna
 * write code that reads/writes files properly compliant with the JSON
 * standard from scratch.
 */
public class JsonCompound extends AbstractDataCompound {
    
    private boolean dirty = true;
    private JsonValue json = null;
    
    
    /**
     * Creates an empty JsonCompound.
     */
    public JsonCompound() {
        super();
    }
    
    /**
     * Creates a new JsonCompound and populates it from the given JsonValue.
     *
     * @throws IllegalArgumentException if {@code !json.isObject()}
     */
    public JsonCompound(JsonValue value) {
        super();
        fromJson(value);
    }
    
    JsonValue toJson() {
        if(!dirty)
            return json;
        dirty = false;
        
        json = new JsonValue(ValueType.object);
        JsonValue child = null;
        
        for(Map.Entry<String, IData> entry : data.entrySet()) {
            String name = entry.getKey();
            IData tag = entry.getValue();
            JsonValue v;
            
            switch(tag.type()) {
                case COMPOUND:
                    v = ((JsonCompound)tag).toJson();
                    break;
                case LIST:
                    v = ((JsonList)tag).toJson();
                    break;
                case BOOL:
                    v = new JsonValue(tag.asBool().get());
                    break;
                case I8:
                    v = new JsonValue(tag.asI8().get());
                    break;
                case I16:
                    v = new JsonValue(tag.asI16().get());
                    break;
                case I32:
                    v = new JsonValue(tag.asI32().get());
                    break;
                case I64:
                    v = new JsonValue(tag.asI64().get());
                    break;
                case F32:
                    v = new JsonValue(tag.asF32().get());
                    break;
                case F64:
                    v = new JsonValue(tag.asF64().get());
                    break;
                case I8ARR:
                    v = JsonUtils.I8ArrToJson(tag.asI8Arr().get());
                    break;
                case I32ARR:
                    v = JsonUtils.I32ArrToJson(tag.asI32Arr().get());
                    break;
                case I64ARR:
                    v = JsonUtils.I64ArrToJson(tag.asI64Arr().get());
                    break;
                case F32ARR:
                    v = JsonUtils.F32ArrToJson(tag.asF32Arr().get());
                    break;
                case F64ARR:
                    v = JsonUtils.F64ArrToJson(tag.asF64Arr().get());
                    break;
                case STRING:
                    v = new JsonValue(tag.asString().get());
                    break;
                default:
                    throw new AssertionError();
            }
            
            v.name = name;
            if(child == null) {
                child = v;
                json.child = child;
            } else {
                child.next = v;
                v.prev = child;
                child = v;
            }
            json.size++;
        }
        
        return json;
    }
    
    JsonCompound fromJson(JsonValue json) {
        if(!json.isObject())
            throw new IllegalArgumentException("Not an object!");
    
        data.clear();
        this.json = json;
        dirty = false;
        
        for(JsonValue val = json.child; val != null; val = val.next) {
            switch(val.type()) {
                case object:
                    putData(val.name, new JsonCompound().fromJson(val));
                    break;
                case array:
                    putData(val.name, new JsonList().fromJson(val));
                    break;
                case booleanValue:
                    putData(val.name, box(val.asBoolean()));
                    break;
                case longValue:
                    putData(val.name, box(val.asLong()));
                    break;
                case doubleValue:
                    putData(val.name, box(val.asDouble()));
                    break;
                case stringValue:
                    putData(val.name, box(val.asString()));
                    break;
                case nullValue:
                    // arbitrarily just make it an empty list
                    putData(val.name, new JsonList());
                default:
                    throw new AssertionError();
            }
        }
        
        return this;
    }
    
    @Override
    protected void setDirty() {
        // Comment: the dirty flag actually isn't perfect as it doesn't pick up
        // on if any child lists or compounds have been modified.
        dirty = true;
        json = null;
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        try(Reader r = new InputStreamReader(in)) {
            fromJson(new JsonReader().parse(r));
        } catch(SerializationException e) {
            // Necessary because JsonReader.parse catches IOExceptions...
            // and spits them out as SerializationExceptions. >.<
            throw new IOException("Error reading JSON!", e);
        }
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        try(Writer w = new OutputStreamWriter(out)) {
            w.write(toJson().toString());
        }
    }
    
    @Override
    public Format format() {
        return Format.JSON;
    }
    
}
