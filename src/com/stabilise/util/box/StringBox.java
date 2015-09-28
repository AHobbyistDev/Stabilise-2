package com.stabilise.util.box;

import java.util.Objects;

import com.stabilise.util.box.Boxes.ABox;

public class StringBox extends ABox<String> implements IBox {
    
    /**
     * Creates a new StringBox holding an empty string.
     */
    public StringBox() {
        super("");
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public StringBox(String value) {
        super(Objects.requireNonNull(value));
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    @Override
    public void set(String value) {
        super.set(Objects.requireNonNull(value));
    }
    
}
