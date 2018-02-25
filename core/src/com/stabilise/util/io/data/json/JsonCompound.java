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
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.box.*;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractMapCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.ITag;


@Incomplete
public class JsonCompound extends AbstractMapCompound {
    
    private boolean dirty = true;
    private JsonValue json = null;
    
    public JsonValue toJson() {
        if(!dirty)
            return json;
        dirty = false;
        
        json = new JsonValue(ValueType.object);
        JsonValue child = null;
        
        for(Map.Entry<String, ITag> entry : data.entrySet()) {
            String name = entry.getKey();
            ITag tag = entry.getValue();
            JsonValue v = null;
            
            if(tag instanceof JsonCompound) {
                v = ((JsonCompound)tag).toJson();
            } else if(tag instanceof JsonList) {
                v = ((JsonList)tag).toJson();
            } else if(tag instanceof BoolBox) {
                v = new JsonValue(((BoolBox)tag).get());
            } else if(tag instanceof ByteArrBox) {
                //v = new JsonValue(((ByteArrBox)tag).get());
            } else if(tag instanceof ByteBox) {
                v = new JsonValue(((ByteBox)tag).get());
            } else if(tag instanceof CharBox) {
                v = new JsonValue(((CharBox)tag).get());
            } else if(tag instanceof DoubleBox) {
                v = new JsonValue(((DoubleBox)tag).get());
            } else if(tag instanceof FloatBox) {
                v = new JsonValue(((FloatBox)tag).get());
            } else if(tag instanceof IntArrBox) {
                //v = new JsonValue(((IntArrBox)tag).get());
            } else if(tag instanceof IntBox) {
                v = new JsonValue(((IntBox)tag).get());
            } else if(tag instanceof LongBox) {
                v = new JsonValue(((LongBox)tag).get());
            } else if(tag instanceof ShortBox) {
                v = new JsonValue(((ShortBox)tag).get());
            } else if(tag instanceof StringBox) {
                v = new JsonValue(((StringBox)tag).get());
            } else {
                throw new RuntimeException("Unrecognised Tag type");
            }
            
            v.name = name;
            if(child == null) {
                child = v;
                json.child = child;
            } else {
                child.next = v;
                child = v;
            }
        }
        
        return json;
    }
    
    public JsonCompound fromJson(JsonValue json) {
        if(json.type() != ValueType.object)
            throw new RuntimeException("Not an object");
        this.json = json;
        
        for(JsonValue val = json.child; val != null; val = val.next) {
            switch(val.type()) {
                case array:
                    putData(val.name, new JsonList().fromJson(val));
                    break;
                case booleanValue:
                    putData(val.name, box(val.asBoolean()));
                    break;
                case doubleValue:
                    putData(val.name, box(val.asDouble()));
                    break;
                case longValue:
                    putData(val.name, box(val.asLong()));
                    break;
                case nullValue:
                    throw new RuntimeException("no null values allowed");
                case object:
                    putData(val.name, new JsonCompound().fromJson(val));
                    break;
                case stringValue:
                    putData(val.name, box(val.asString()));
                    break;
                default:
                    throw new AssertionError();
            }
        }
        
        dirty = false;
        return this;
    }
    
    @Override
    public <T extends ITag> T putData(String name, T t) {
        dirty = true;
        return super.putData(name, t);
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
