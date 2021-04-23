package com.ontimize.jee.core.common.util.extend;

import org.w3c.dom.Document;

public class OrderDocument implements Comparable<OrderDocument> {

    public int index;

    public Document document;

    public OrderDocument(int index, Document document) {
        this.index = index;
        this.document = document;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public int getIndex() {
        return this.index;
    }

    public Document getDocument() {
        return this.document;
    }

    @Override
    public int compareTo(OrderDocument compareDocument) {
        int compareIndex = compareDocument.getIndex();
        return this.index - compareIndex;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
