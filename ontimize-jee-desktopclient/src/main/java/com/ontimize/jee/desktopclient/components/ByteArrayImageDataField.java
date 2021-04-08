package com.ontimize.jee.desktopclient.components;

import java.util.Hashtable;

import com.ontimize.gui.ValueEvent;
import com.ontimize.gui.field.ImageDataField;
import com.ontimize.util.remote.BytesBlock;

public class ByteArrayImageDataField extends ImageDataField {

    public ByteArrayImageDataField(Hashtable param) {
        super(param);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof byte[]) {
            // Cache:
            Object oCurrentValue = this.getValue();
            if (!this.isEmpty()) {
                if (oCurrentValue.equals(value)) {
                    this.valueSave = this.getValue();
                    return;
                }
            }
            this.bytesImage = null;
            this.bytesImage = (byte[]) value;
            // Update the label
            this.update();
            this.valueSave = this.getValue();
            this.fireValueChanged(this.valueSave, oCurrentValue, ValueEvent.PROGRAMMATIC_CHANGE);
        } else if (value instanceof BytesBlock) {
            super.setValue(value);
        } else {
            this.deleteData();
        }
    }

    @Override
    public Object getValue() {
        if (this.isEmpty()) {
            return null;
        }
        return this.bytesImage;
    }

}
