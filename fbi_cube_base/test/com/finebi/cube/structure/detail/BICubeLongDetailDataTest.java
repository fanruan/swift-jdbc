package com.finebi.cube.structure.detail;

import com.finebi.cube.ICubeConfiguration;
import com.finebi.cube.exception.BICubeResourceAbsentException;
import com.finebi.cube.location.BICubeConfigurationTest;
import com.finebi.cube.location.BICubeResourceRetrieval;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.location.ICubeResourceRetrievalService;
import com.finebi.cube.structure.BITableKey;
import com.finebi.cube.tools.BITableSourceTestTool;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.bi.stable.utils.file.BIFileUtils;
import junit.framework.TestCase;

import java.io.File;

/**
 * This class created on 2016/5/2.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeLongDetailDataTest extends TestCase {
    private BICubeLongDetailData detailData;
    private ICubeResourceRetrievalService retrievalService;
    private ICubeConfiguration cubeConfiguration;
    private ICubeResourceLocation location;


    public BICubeLongDetailDataTest() {
        try {
            cubeConfiguration = new BICubeConfigurationTest();
            retrievalService = new BICubeResourceRetrieval(cubeConfiguration);
            location = retrievalService.retrieveResource(new BITableKey(BITableSourceTestTool.getDBTableSourceD()));
            detailData = new BICubeLongDetailData(location);
        } catch (BICubeResourceAbsentException e) {
            assertFalse(true);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ICubeResourceLocation location = retrievalService.retrieveResource(new BITableKey(BITableSourceTestTool.getDBTableSourceD()));
        File file = new File(location.getAbsolutePath());
        if (file.exists()) {
            BIFileUtils.delete(file);
        }
    }

    public void testAvailable() {
        try {
            assertFalse(detailData.isCubeWriterAvailable());
            assertFalse(detailData.isCubeReaderAvailable());
            detailData.addDetailDataValue(0, Long.valueOf("12"));
            assertTrue(detailData.isCubeWriterAvailable());
            assertFalse(detailData.isCubeReaderAvailable());
            assertEquals(Long.valueOf("12"), detailData.getOriginalValueByRow(0));
            assertTrue(detailData.isCubeReaderAvailable());
            assertTrue(detailData.isCubeWriterAvailable());
        } catch (Exception e) {
            BILogger.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }
}
