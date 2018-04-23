package com.stabilise.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.ObjectExporter;


public class IOtest {
    
    @SuppressWarnings("unused")
    private static class A {
        public int a = 5;
        public String b = "lel";
        public long c = Long.MAX_VALUE;
        private boolean d = false;
        private final int e = 10;
        private transient float f = 1f;
        private volatile B g = new B();
        protected List<Integer> h = new ArrayList<>();
    }
    
    private static class B {
        @SuppressWarnings("unused")
        private int[] lel = new int[16];
    }
    
    public static void main(String[] args) throws IOException {
        DataCompound c1 = ObjectExporter.exportObj(new A(), Format.NBT);
        DataCompound c2 = ObjectExporter.exportObj(new A(), Format.BYTE_STREAM);
        A a = new A();
        a.a = 0;
        a.b = null;
        a.c = 0;
        a.d = true;
        ObjectExporter.importObj(a, c1);
        
        System.out.println(c1);
        System.out.println(c1.convert(Format.BYTE_STREAM));
        System.out.println(c2);
        System.out.println(ObjectExporter.exportObj(a, Format.NBT));
        System.out.println(IOUtil.countBytes(c1, Format.NBT, Compression.UNCOMPRESSED));
        System.out.println(IOUtil.countBytes(c1, Format.NBT, Compression.GZIP));
        System.out.println(IOUtil.countBytes(c1, Format.NBT, Compression.ZLIB));
        System.out.println(IOUtil.countBytes(c1, Format.BYTE_STREAM, Compression.UNCOMPRESSED));
        System.out.println(IOUtil.countBytes(c1, Format.BYTE_STREAM, Compression.GZIP));
        System.out.println(IOUtil.countBytes(c1, Format.BYTE_STREAM, Compression.ZLIB));
        System.out.println(IOUtil.countBytes(c2, Format.BYTE_STREAM, Compression.UNCOMPRESSED));
        System.out.println(IOUtil.countBytes(c2, Format.BYTE_STREAM, Compression.GZIP));
        System.out.println(IOUtil.countBytes(c2, Format.BYTE_STREAM, Compression.ZLIB));
    }
    
}
