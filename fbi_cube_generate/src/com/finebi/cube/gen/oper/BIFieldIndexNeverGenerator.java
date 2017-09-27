package com.finebi.cube.gen.oper;

import com.finebi.cube.common.log.BILogger;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.utils.BILogHelper;
import com.finebi.cube.message.IMessage;
import com.finebi.cube.structure.Cube;
import com.finebi.cube.structure.column.BIColumnKey;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.program.BIStringUtils;
import com.fr.fs.control.UserControl;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

/**
 * Created by neil on 2017/8/4.
 */
public class BIFieldIndexNeverGenerator extends AbstractFieldIndexGenerator {

    private static final BILogger LOGGER = BILoggerFactory.getLogger(BIFieldIndexNeverGenerator.class);

    public BIFieldIndexNeverGenerator(Cube cube, CubeTableSource tableSource, ICubeFieldSource hostBICubeFieldSource, BIColumnKey targetColumnKey) {
        super(cube, tableSource, hostBICubeFieldSource, targetColumnKey);
    }

    @Override
    public Object mainTask(IMessage lastReceiveMessage) {

        LOGGER.info(BIStringUtils.append(logFileInfo(), " start building field index main task") +
                BILogHelper.logCubeLogTableSourceInfo(tableSource.getSourceID()));
        Stopwatch stopwatch = Stopwatch.createStarted();
        biLogManager.logIndexStart(UserControl.getInstance().getSuperManagerID());
        LOGGER.info(BIStringUtils.append(logFileInfo(), " finish building field index main task,elapse:", String.valueOf(stopwatch.elapsed(TimeUnit.SECONDS)), " second"));
        try {
            biLogManager.infoColumn(tableSource.getPersistentTable(), hostBICubeFieldSource.getFieldName(), stopwatch.elapsed(TimeUnit.SECONDS), Long.valueOf(UserControl.getInstance().getSuperManagerID()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void release() {
        //Do Nothing...
    }
}
