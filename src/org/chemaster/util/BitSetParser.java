package org.chemaster.util;

import java.util.BitSet;

/**
 *
 * @author chung
 */
public class BitSetParser {

    private final String string;
    private int size = 1024;

    public BitSetParser(final String string) {
        this.string = string;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public BitSet parse() {
        BitSet bs = new BitSet(size);
        String[] tokens = string.replaceAll("\\{", "").replaceAll("}", "").split(",");
        int i;
        if (tokens.length > 0) {
            for (String s : tokens) {
                s = s.trim();
                if (!s.isEmpty()) {
                    i = Integer.parseInt(s);
                    bs.set(i);
                } else {
                    break;
                }
            }
        }
        return bs;
    }
}
