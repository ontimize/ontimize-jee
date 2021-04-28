package com.ontimize.jee.common.gui;

import com.ontimize.jee.common.util.remote.BytesBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;


public class DataFile implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DataFile.class);

    protected BytesBlock file = null;

    protected String fileName = null;

    public DataFile(String fileName, BytesBlock bb) {
        this.fileName = fileName;
        this.file = bb;
    }

    public DataFile(File file) throws IOException {
        this(file, null);
    }

    public DataFile(File f, String fName) throws IOException {
        super();
        FileInputStream fIn = null;
        BufferedInputStream bIn = null;
        ByteArrayOutputStream bOut = null;
        try {
            fIn = new FileInputStream(f);
            bIn = new BufferedInputStream(fIn);
            bOut = new ByteArrayOutputStream(10 * 1024);
            int b = -1;
            while ((b = bIn.read()) != -1) {
                bOut.write(b);
            }
            bOut.flush();
            this.file = new BytesBlock(bOut.toByteArray());
            if (fName != null) {
                this.fileName = fName;
            } else {
                this.fileName = f.getName();
            }
        } catch (Exception e) {
            DataFile.logger.error(null, e);
        } finally {
            if (bIn != null) {
                bIn.close();
            }
            if (fIn != null) {
                fIn.close();
            }
            if (bOut != null) {
                bOut.close();
            }
        }
    }

    public BytesBlock getBytesBlock() {
        return this.file;
    }

    public String getFileName() {
        return this.fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof DataFile) {
            DataFile eObj = (DataFile) obj;

            if (this.fileName != null) {
                if (!this.fileName.equals(eObj.getFileName())) {
                    return false;
                }
            } else {
                if (eObj.getFileName() != null) {
                    return false;
                }
            }
            if (this.file != null) {
                if (!this.file.equals(eObj.getBytesBlock())) {
                    return false;
                }
            } else {
                if (eObj.getBytesBlock() != null) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
