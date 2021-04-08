package com.ontimize.jee.common.tools;

import com.ontimize.gui.ValueChangeListener;
import com.ontimize.gui.ValueEvent;

public class ObjectWrapper<T> {

    private T wrapped;

    private ValueChangeListener listener;

    public ObjectWrapper() {
        super();
    }

    public ObjectWrapper(T value) {
        super();
        this.setValue(value);
    }

    public void setValue(T wrapped) {
        T old = this.wrapped;
        this.wrapped = wrapped;
        if (this.listener != null) {
            this.listener.valueChanged(new ValueEvent(this, wrapped, old, 0));
        }
    }

    public T getValue() {
        return this.wrapped;
    }

    public void setListener(ValueChangeListener listener) {
        this.listener = listener;
    }

}
