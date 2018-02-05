package com.fr.swift.cube.io.impl.fineio.output;

import com.fineio.FineIO;
import com.fineio.io.IntBuffer;
import com.fineio.io.file.IOFile;
import com.fineio.storage.Connector;
import com.fr.swift.cube.io.impl.fineio.connector.ConnectorManager;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.cube.io.output.IntWriter;

import java.net.URI;

/**
 * @author anchore
 */
public class IntFineIoWriter extends BaseFineIoWriter implements IntWriter {
    private IOFile<IntBuffer> ioFile;

    private IntFineIoWriter(URI uri, Connector connector, boolean isOverwrite) {
        if (isOverwrite) {
            ioFile = FineIO.createIOFile(connector, uri, FineIO.MODEL.WRITE_INT);
        } else {
            ioFile = FineIO.createIOFile(connector, uri, FineIO.MODEL.EDIT_INT);
        }
    }

    public static IntWriter build(IResourceLocation location, boolean isOverwrite) {
        return new IntFineIoWriter(
                location.getUri(),
                ConnectorManager.getInstance().getConnector(),
                isOverwrite
        );
    }

    @Override
    public void flush() {
        ioFile.close();
    }

    @Override
    public void release() {
        if (ioFile != null) {
            flush();
            ioFile = null;
        }
    }

    @Override
    public void put(long pos, int val) {
        FineIO.put(ioFile, pos, val);
    }
}