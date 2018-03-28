package com.fr.swift.segment.column.impl.base;

import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.cube.io.BuildConf;
import com.fr.swift.cube.io.ResourceDiscovery;
import com.fr.swift.cube.io.ResourceDiscoveryImpl;
import com.fr.swift.cube.io.Types.DataType;
import com.fr.swift.cube.io.Types.IoType;
import com.fr.swift.cube.io.Types.WriteType;
import com.fr.swift.cube.io.input.BitMapReader;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.cube.io.output.BitMapWriter;
import com.fr.swift.segment.column.BitmapIndexedColumn;

/**
 * @author anchore
 * @date 2017/11/9
 */
public class BitMapColumn implements BitmapIndexedColumn {
    private static final String INDEX = "index";

    private static final ResourceDiscovery DISCOVERY = ResourceDiscoveryImpl.getInstance();

    private BitMapWriter indexWriter;
    private BitMapReader indexReader;
    private IResourceLocation indexLocation;

    public BitMapColumn(IResourceLocation parent) {
        this.indexLocation = parent.buildChildLocation(INDEX);
    }

    private void initIndexWriter() {
        if (indexWriter == null) {
            indexWriter = DISCOVERY.getWriter(indexLocation, new BuildConf(IoType.WRITE, DataType.BITMAP, WriteType.EDIT));
        }
    }

    private void initIndexReader() {
        if (indexReader == null) {
            indexReader = DISCOVERY.getReader(indexLocation, new BuildConf(IoType.READ, DataType.BITMAP));
        }
    }

    @Override
    public void putBitMapIndex(int index, ImmutableBitMap bitmap) {
        initIndexWriter();
        indexWriter.put(index, bitmap);
    }

    @Override
    public ImmutableBitMap getBitMapIndex(int index) {
        initIndexReader();
        return indexReader.get(index);
    }

    @Override
    public void putNullIndex(ImmutableBitMap bitMap) {
        putBitMapIndex(0, bitMap);
    }

    @Override
    public ImmutableBitMap getNullIndex() {
        return getBitMapIndex(0);
    }

    @Override
    public void flush() {
        if (indexWriter != null) {
            indexWriter.flush();
        }
    }

    @Override
    public void release() {
        if (indexWriter != null) {
            indexWriter.release();
            indexWriter = null;
        }
        if (indexReader != null) {
            indexReader.release();
            indexReader = null;
        }
    }
}
