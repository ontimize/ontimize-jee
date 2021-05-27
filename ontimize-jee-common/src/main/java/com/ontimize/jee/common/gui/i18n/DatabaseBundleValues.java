package com.ontimize.jee.common.gui.i18n;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;

public class DatabaseBundleValues implements Serializable {

    protected String[] availableLocales;

    protected List bundleValues = new ArrayList();

    public DatabaseBundleValues(String[] availableLocales) {
        this.availableLocales = availableLocales;
    }

    public void addBundleValue(String textKey, String bundleName, Map translationValues) {
        BundleValue bundleValue = new BundleValue(textKey, bundleName, translationValues);

        // Search for the non-existent item
        // int index = bundleValues.indexOf(bundleValue);
        int index = Collections.binarySearch(this.bundleValues, bundleValue);
        if (index >= 0) {
            this.bundleValues.add(index, bundleValue);
            this.bundleValues.remove(index + 1);
        } else {
            // The list must be orderer
            this.bundleValues.add(-index - 1, bundleValue);
        }
    }

    public List getBundleValues() {
        return this.bundleValues;
    }

    public static class BundleValue implements Comparable, Serializable {

        protected String textKey;

        protected String bundleClassName;

        protected Map translationValues;

        public BundleValue(String textKey, String bundleName, Map tranlationValues) {
            this.textKey = textKey;
            this.bundleClassName = bundleName;
            this.translationValues = tranlationValues;
        }

        public String getTextKey() {
            return this.textKey;
        }

        public void setTextKey(String textKey) {
            this.textKey = textKey;
        }

        public String getBundleClassName() {
            return this.bundleClassName;
        }

        public void setBundleClassName(String bundleClassName) {
            this.bundleClassName = bundleClassName;
        }

        public Map getTranslationValues() {
            return this.translationValues;
        }

        public void setTranslationValues(Map translationValues) {
            this.translationValues = translationValues;
        }

        public boolean sameBundleValue(BundleValue obj) {
            if ((obj != null) && obj.getBundleClassName().equals(this.getBundleClassName())
                    && obj.getTextKey().equals(this.getTextKey())) {
                return true;
            }
            return false;
        }

        /**
         * Compares this object with the specified object for order. Returns a negative integer, zero, or a
         * positive integer as this object is less than, equal to, or greater than the specified object.
         */
        @Override
        public int compareTo(Object o) {
            if ((o != null) && (o instanceof BundleValue)) {
                int comparationResult = this.getBundleClassName().compareTo(((BundleValue) o).getBundleClassName());
                if (comparationResult == 0) {
                    comparationResult = this.getTextKey().compareTo(((BundleValue) o).getTextKey());
                }
                return comparationResult;
            }
            return -1;
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

}
