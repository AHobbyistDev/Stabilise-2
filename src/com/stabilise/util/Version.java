package com.stabilise.util;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import javaslang.Tuple5;
import javaslang.control.Option;
import javaslang.control.Try;

/**
 * A version class compliant with the <a href=semver.org>Semantic Versioning
 * 2.0.0</a> standard.
 */
@Immutable
public class Version implements Comparable<Version>, Printable {
    
    private final int major;
    private final int minor;
    private final int patch;
    private final Option<String> preRe;
    private final Option<String> meta;
    
    /** Cached toString() value. */
    private String str;
    
    
    /**
     * Creates a new Version.
     * 
     * @param major Major version. Must be non-negative.
     * @param minor Minor version. Must be non-negative.
     * @param patch Patch version. Must be non-negative.
     * 
     * @throws IllegalArgumentException if the values given are invlaid.
     */
    public Version(int major, int minor, int patch) {
        this(major, minor, patch, null, null);
    }
    
    /**
     * Creates a new Version.
     * 
     * @param major Major version. Must be non-negative.
     * @param minor Minor version. Must be non-negative.
     * @param patch Patch version. Must be non-negative.
     * @param preRe Pre-release. May be null.
     * 
     * @throws IllegalArgumentException if the values given are invlaid.
     */
    public Version(int major, int minor, int patch, String preRe) {
        this(major, minor, patch, preRe, null);
    }
    
    /**
     * Creates a new Version.
     * 
     * @param major Major version. Must be non-negative.
     * @param minor Minor version. Must be non-negative.
     * @param patch Patch version. Must be non-negative.
     * @param preRe Pre-release. May be null.
     * @param meta Metadata. May be null.
     * 
     * @throws IllegalArgumentException if the values given are invlaid.
     */
    public Version(int major, int minor, int patch, String preRe, String meta) {
        this.major = Checks.testMin(major, 0);
        this.minor = Checks.testMin(minor, 0);
        this.patch = Checks.testMin(patch, 0);
        // We convert empty strings to null to prevent unpleasant loopbacks
        // from preRelease() and metadata().
        this.preRe = Option.of(preRe == "" ? null : preRe).peek(s -> check(s, "pre-release"));
        this.meta  = Option.of(meta  == "" ? null : meta ).peek(s -> check(s, "metadata"));
    }
    
    /**
     * Creates a new Version.
     * 
     * <p>It it always true that for any {@code Version v}, {@code
     * v.equals(new Version(v.toString()))} is {@code true}.
     * 
     * @throws NullPointerException if {@code version} is {@code null}.
     * @throws IllegalArgumentException if {@code version} is formatted
     * incorrectly.
     */
    public Version(String version) {
        Tuple5<Integer, Integer, Integer, String, String> tup = doParse(version);
        this.major = Checks.testMin(tup._1, 0);
        this.minor = Checks.testMin(tup._2, 0);
        this.patch = Checks.testMin(tup._3, 0);
        this.preRe = Option.of(tup._4).peek(s -> check(s, "pre-release"));
        this.meta  = Option.of(tup._5).peek(s -> check(s, "metadata"));
    }
    
    private Version(Version other) {
        this.major = other.major;
        this.minor = other.minor;
        this.patch = other.patch;
        this.preRe = other.preRe;
        this.meta  = other.meta;
    }
    
    /**
     * Gets the major version.
     */
    public int major() {
        return major;
    }
    
    /**
     * Gets the minor version.
     */
    public int minor() {
        return minor;
    }
    
    /**
     * Gets the patch version.
     */
    public int patch() {
        return patch;
    }
    
    /**
     * Gets the pre-release tag, or an empty string if no pre-release is set.
     */
    public String preRelease() {
        return preRe.orElse("");
    }
    
    /**
     * Gets the metadata tag, or an empty string if no metadata is set.
     */
    public String metaData() {
        return meta.orElse("");
    }
    
    /**
     * Returns {@code true} if this is an earlier version than {@code v}.
     */
    public boolean precedes(Version v) {
        return compareTo(v) == -1;
    }
    
    /**
     * Clones this version.
     */
    public Version clone() {
        return new Version(this);
    }
    
    @Override
    public int compareTo(Version v) {
        // Semantic versioning precedence
        
        // major.minor.patch comparison is straightforward
        if(major > v.major) return 1;
        if(major < v.major) return -1;
        if(minor > v.minor) return 1;
        if(minor < v.minor) return -1;
        if(patch > v.patch) return 1;
        if(patch < v.patch) return -1;
        
        // Now we compare pre-release information.
        
        // A version with a pre-release has lower precedence than one without
        // one.
        if(preRe.isDefined() && !v.preRe.isDefined()) return -1;
        if(!preRe.isDefined() && v.preRe.isDefined()) return 1;
        if(!preRe.isDefined() && !v.preRe.isDefined()) return 0; // neither = identical
        
        // We split the pre-releases into their identifiers and compare then
        // one-by-one.
        String[] pre1 = preRe.get().split("\\.");
        String[] pre2 = v.preRe.get().split("\\.");
        int max = Math.min(pre1.length, pre2.length);
        for(int i = 0; i < max; i++) {
            String s1 = pre1[i];
            String s2 = pre2[i];
            Try<Integer> n1 = Try.of(() -> Integer.parseInt(s1));
            Try<Integer> n2 = Try.of(() -> Integer.parseInt(s2));
            
            // If the identifiers can be compared numerically, do so
            if(n1.isSuccess() && n2.isSuccess()) {
                int n = n1.get() - n2.get();
                if(n != 0)
                    return n;
            // Numeric identifiers always have lower precedence than
            // non-numeric ones.
            } else if(n1.isSuccess() && n2.isFailure()) {
                return -1;
            } else if(n1.isFailure() && n2.isSuccess()) {
                return 1;
            // Otherwise, we compare lexically in ASCII sort order
            } else {
                int n = s1.compareTo(s2);
                if(n != 0)
                    return n;
            }
        }
        
        // If all identifiers are found to be equal, the larger set of pre-
        // release fields has greater precedence
        return pre1.length - pre2.length;
        
        // Note that metadata is not taken into consideration for comparisons
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Version)) return false;
        Version v = (Version) o;
        //return compareTo(v) == 0;
        return major == v.major && minor == v.minor && patch == v.patch
                && preRe.orElse("").equals(v.preRe.orElse(""))
                && meta.orElse("").equals(v.meta.orElse(""));
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    @Override
    public String toString() {
        if(str == null)
            str = major + "." + minor + "." + patch
                + preRe.map(s -> "-" + s).orElse("")
                +  meta.map(s -> "+" + s).orElse("");
        return str;
    }
    
    
    private static Tuple5<Integer, Integer, Integer, String, String> doParse(String str) {
        Objects.requireNonNull(str);
        String[] parts = str.split("[-+]", 2);
        String[] versions = parts[0].split("[.]", 3);
        if(versions.length != 3)
            throw new IllegalArgumentException("Version has less than 3 parts!");
        int major, minor, patch;
        try {
            major = parseUnsigned(versions[0]);
            minor = parseUnsigned(versions[1]);
            patch = parseUnsigned(versions[2]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version number (" + parts[0] + ")", e);
        }
        String preRe = null;
        String meta  = null;
        if(parts.length == 2) {
            // i.e. if parts[] was obtained from splitting str on "-" and not
            // "+"
            if(str.length() - str.indexOf('-') == parts[1].length() + 1) {
                parts = parts[1].split("[+]", 2);
                preRe = parts[0];
                if(parts.length == 2)
                    meta = parts[1];
            } else {
                meta = parts[1];
            }

        }
        return new Tuple5<>(major, minor, patch, preRe, meta);
    }
    
    private static int parseUnsigned(String s) {
        int n = Integer.parseUnsignedInt(s);
        if(s.startsWith("0") && (n != 0 || s.length() > 1))
            throw new NumberFormatException();
        return n;
    }
    
    private static final Pattern REGEX_TAG = Pattern.compile("[0-9A-Za-z-\\.]++");
    
    /** Checks pre-release and metadata fields (they have the same rules). */
    private static void check(String tag, String name) {
        if(!REGEX_TAG.matcher(tag).matches())
            err(tag, name);
        // Cheaty simple way of making sure no identifier may be empty (since
        // I don't know much regex).
        if(tag.contains("..") || tag.startsWith(".") || tag.endsWith("."))
            err(tag, name);
        for(String s : tag.split("\\.")) {
            //if(s.length() == 0) // not needed due to cheaty check
            //    err(tag, name);
            try {
                int n = Integer.parseUnsignedInt(s);
                if(s.startsWith("0") && (n != 0 || s.length() > 1))
                    err(tag, name);
            } catch(NumberFormatException e) {} // ignore
        }
    }
    
    private static void err(String tag, String name) {
        throw new IllegalArgumentException("Invalid " + name + " tag (" + tag + ")");
    }
    
}
