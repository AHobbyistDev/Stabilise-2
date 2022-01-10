package com.stabilise.util.io.data.json;

import com.badlogic.gdx.utils.JsonValue;
import com.stabilise.util.box.F64ArrBox;
import com.stabilise.util.box.I64ArrBox;
import com.stabilise.util.io.data.IData;


/**
 * Utility functions for working with Json.
 */
class JsonUtils {
    
    private JsonUtils() {} // non-instantiable
    
    
    /**
     * Converts a byte array to a JsonValue object.
     */
    public static JsonValue I8ArrToJson(byte[] data) {
        JsonValue v = new JsonValue(JsonValue.ValueType.array);
        if(data.length == 0)
            return v;
        JsonValue child, prev;
        child = new JsonValue(data[0]);
        v.addChild(child);
        
        // Do this manually since repeatedly calling v.addChild() is O(n^2)
        for(int i = 1; i < data.length; i++) {
            prev = child;
            child = new JsonValue(data[i]);
            child.parent = v;
            prev.next = child;
            child.prev = prev;
            v.size++;
        }
        
        return v;
    }
    
    /**
     * Converts an in array to a JsonValue object.
     */
    public static JsonValue I32ArrToJson(int[] data) {
        JsonValue v = new JsonValue(JsonValue.ValueType.array);
        if(data.length == 0)
            return v;
        JsonValue child, prev;
        child = new JsonValue(data[0]);
        v.addChild(child);
        
        // Do this manually since repeatedly calling v.addChild() is O(n^2)
        for(int i = 1; i < data.length; i++) {
            prev = child;
            child = new JsonValue(data[i]);
            child.parent = v;
            prev.next = child;
            child.prev = prev;
            v.size++;
        }
        
        return v;
    }
    
    /**
     * Converts a long array to a JsonValue object.
     */
    public static JsonValue I64ArrToJson(long[] data) {
        JsonValue v = new JsonValue(JsonValue.ValueType.array);
        if(data.length == 0)
            return v;
        JsonValue child, prev;
        child = new JsonValue(data[0]);
        v.addChild(child);
        
        // Do this manually since repeatedly calling v.addChild() is O(n^2)
        for(int i = 1; i < data.length; i++) {
            prev = child;
            child = new JsonValue(data[i]);
            child.parent = v;
            prev.next = child;
            child.prev = prev;
            v.size++;
        }
        
        return v;
    }
    
    /**
     * Converts a float array to a JsonValue object.
     */
    public static JsonValue F32ArrToJson(float[] data) {
        JsonValue v = new JsonValue(JsonValue.ValueType.array);
        if(data.length == 0)
            return v;
        JsonValue child, prev;
        child = new JsonValue(data[0]);
        v.addChild(child);
        
        // Do this manually since repeatedly calling v.addChild() is O(n^2)
        for(int i = 1; i < data.length; i++) {
            prev = child;
            child = new JsonValue(data[i]);
            child.parent = v;
            prev.next = child;
            child.prev = prev;
            v.size++;
        }
        
        return v;
    }
    
    /**
     * Converts a double array to a JsonValue object.
     */
    public static JsonValue F64ArrToJson(double[] data) {
        JsonValue v = new JsonValue(JsonValue.ValueType.array);
        if(data.length == 0)
            return v;
        JsonValue child, prev;
        child = new JsonValue(data[0]);
        v.addChild(child);
        
        // Do this manually since repeatedly calling v.addChild() is O(n^2)
        for(int i = 1; i < data.length; i++) {
            prev = child;
            child = new JsonValue(data[i]);
            child.parent = v;
            prev.next = child;
            child.prev = prev;
            v.size++;
        }
        
        return v;
    }
    
    /**
     * Reads an array from a JsonValue. The given object, which is of an array
     * type, will be converted to either a List, or an I64Arr, or an F64Arr,
     * if possible.
     *
     * @throws IllegalStateException if the given value is not of type {@code
     * array}, or its elements aren't of a uniform type.
     */
    public static IData readJsonArray(JsonValue v) {
        if(!v.isArray())
            throw new IllegalStateException("Not an array!");
        if(v.size == 0 || v.child == null)
            return new JsonList();
        switch(v.child.type()) {
            case longValue:
                return new I64ArrBox(v.asLongArray());
            case doubleValue:
                return new F64ArrBox(v.asDoubleArray());
            default:
                return new JsonList(v);
        }
    }
    
}
