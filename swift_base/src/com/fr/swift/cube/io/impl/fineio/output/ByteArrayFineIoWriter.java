package com.fr.swift.cube.io.impl.fineio.output;

import com.fr.swift.cube.io.impl.fineio.input.LongFineIoReader;
import com.fr.swift.cube.io.input.LongReader;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.cube.io.output.ByteArrayWriter;
import com.fr.swift.cube.io.output.ByteWriter;
import com.fr.swift.cube.io.output.IntWriter;
import com.fr.swift.cube.io.output.LongWriter;
import com.fr.swift.util.Crasher;

/**
 * @author anchore
 */
public class ByteArrayFineIoWriter extends BaseFineIoWriter implements ByteArrayWriter {
    private ByteWriter contentWriter;
    private LongWriter positionWriter;
    private IntWriter lengthWriter;

    private LongWriter lastPosWriter;

    private long curPos;

    private ByteArrayFineIoWriter(ByteWriter contentWriter, LongWriter positionWriter, IntWriter lengthWriter, LongWriter lastPosWriter) {
        this.contentWriter = contentWriter;
        this.positionWriter = positionWriter;
        this.lengthWriter = lengthWriter;
        this.lastPosWriter = lastPosWriter;
    }

    public static ByteArrayWriter build(IResourceLocation location, boolean isOverwrite) {
        ByteArrayFineIoWriter bafw = getByteArrayFineIoWriter(location, isOverwrite);
        if (!isOverwrite) {
            bafw.curPos = getLastPosition(location);
        }
        return bafw;
    }

    private static ByteArrayFineIoWriter getByteArrayFineIoWriter(IResourceLocation location, boolean isOverwrite) {

        // 获得内容部分的byte类型Writer
        IResourceLocation contentLocation = location.buildChildLocation(CONTENT);
        ByteWriter contentWriter = ByteFineIoWriter.build(contentLocation, isOverwrite);

        // 获得位置部分的long类型Writer
        IResourceLocation positionLocation = location.buildChildLocation(POSITION);
        LongWriter positionWriter = LongFineIoWriter.build(positionLocation, isOverwrite);

        // 获得长度部分的int类型Writer
        IResourceLocation lengthLocation = location.buildChildLocation(LENGTH);
        IntWriter lengthWriter = IntFineIoWriter.build(lengthLocation, isOverwrite);

        // 获得最后位置部分的long类型Writer
        IResourceLocation lastPosLocation = location.buildChildLocation(LAST_POSITION);
        LongWriter lastPosWriter = LongFineIoWriter.build(lastPosLocation, true);

        return new ByteArrayFineIoWriter(contentWriter, positionWriter, lengthWriter, lastPosWriter);
    }

    private static long getLastPosition(IResourceLocation location) {
        try {
            IResourceLocation lastPosLocation = location.buildChildLocation(LAST_POSITION);
            LongReader lastPosReader = LongFineIoReader.build(lastPosLocation);
            return lastPosReader.isReadable() ? lastPosReader.get(0) : 0;
        } catch (Exception e) {
            return Crasher.crash("cannot get last position", e);
        }
    }

    @Override
    public void flush() {
        contentWriter.flush();
        positionWriter.flush();
        lengthWriter.flush();

        lastPosWriter.put(0, curPos);
        lastPosWriter.flush();
    }

    @Override
    public void release() {
        contentWriter.release();
        positionWriter.release();
        lengthWriter.release();
        lastPosWriter.release();
    }

    @Override
    public void put(long pos, byte[] val) {
        if (val == null) {
            val = NULL_VALUE;
        }
        int len = val.length;
        positionWriter.put(pos, curPos);
        lengthWriter.put(pos, len);
        for (int i = 0; i < len; i++) {
            contentWriter.put(curPos + i, val[i]);
        }

        curPos += len;
    }
}
