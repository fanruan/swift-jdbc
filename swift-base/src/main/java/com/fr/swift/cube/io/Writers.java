package com.fr.swift.cube.io;

import com.fr.swift.config.service.SwiftCubePathService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.io.Types.WriteType;
import com.fr.swift.cube.io.impl.fineio.FineIoWriters;
import com.fr.swift.cube.io.impl.mem.MemIoBuilder;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.cube.io.output.Writer;
import com.fr.swift.io.nio.NioConf;
import com.fr.swift.io.nio.Nios;
import com.fr.swift.util.Crasher;

/**
 * @author anchore
 */
public final class Writers {
    private static final SwiftCubePathService PATH_SERVICE = SwiftContext.get().getBean(SwiftCubePathService.class);

    /**
     * delegate to FineIoWriters, MemIoBuilder
     *
     * @see FineIoWriters
     * @see MemIoBuilder
     */
    public static Writer build(IResourceLocation location, BuildConf conf) {
        switch (location.getStoreType()) {
            case FINE_IO:
                return FineIoWriters.build(location, conf);
            case MEMORY:
                return MemIoBuilder.build(location, conf);
            case NIO:
                return Nios.of(new NioConf(
                        String.format("%s/%s", PATH_SERVICE.getSwiftPath(), location.getPath()),
                        conf.writeType == WriteType.APPEND ? NioConf.IoType.APPEND : NioConf.IoType.OVERWRITE), conf.dataType);
            default:
        }
        return Crasher.crash(String.format("illegal cube build config: %s\nlocation: %s", conf, location));
    }

}
