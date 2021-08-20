package com.ontimize.jee.common.gui;

public class LongString implements java.io.Serializable {

    protected String s = null;

    public LongString(String s) {
        this.s = s;
    }

    public String getString() {
        return this.s;
    }

    @Override
    public String toString() {
        return this.s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o != null) && (o instanceof LongString)) {
            return this.s.equals(((LongString) o).getString());
        } else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return this.s.hashCode();
    }

}
