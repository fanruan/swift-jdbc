package com.fr.swift.service;

import com.fr.swift.config.bean.SegmentKeyBean;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.io.Types.StoreType;
import com.fr.swift.db.Table;
import com.fr.swift.db.impl.SegmentTransfer;
import com.fr.swift.db.impl.SwiftDatabase;
import com.fr.swift.event.SwiftEventDispatcher;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.segment.event.SegmentEvent;
import com.fr.swift.source.alloter.impl.line.LineAllotRule;
import com.fr.swift.util.concurrent.PoolThreadFactory;
import com.fr.swift.util.concurrent.SwiftExecutors;
import com.fr.third.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author anchore
 * @date 2018/7/27
 */
@Service
public class ScheduledRealtimeTransfer implements Runnable {
    private static final int MIN_PUT_THRESHOLD = LineAllotRule.MEM_STEP / 2;

    private final SwiftSegmentManager localSegments = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class);

    private ScheduledRealtimeTransfer() {
        SwiftExecutors.newSingleThreadScheduledExecutor(new PoolThreadFactory(getClass())).
                scheduleWithFixedDelay(this, 1, 1, TimeUnit.HOURS);
    }

    @Override
    public void run() {
        for (final Table table : SwiftDatabase.getInstance().getAllTables()) {
            List<SegmentKey> list = localSegments.getSegmentKeys(table.getSourceKey());
            if (null == list || list.isEmpty()) {
                return;
            }
            Collections.sort(list, new Comparator<SegmentKey>() {
                @Override
                public int compare(SegmentKey o1, SegmentKey o2) {
                    return o2.getOrder() - o1.getOrder();
                }
            });

            // 过滤order最大的
            boolean firstRealtime = false;
            for (final SegmentKey segKey : list) {
                try {
                    if (segKey.getStoreType().isPersistent()) {
                        continue;
                    }
                    if (!firstRealtime) {
                        firstRealtime = true;
                        continue;
                    }
                    Segment realtimeSeg = localSegments.getSegment(segKey);
                    if (realtimeSeg.isReadable()) {
                        SwiftEventDispatcher.fire(SegmentEvent.TRANSFER_REALTIME, segKey);
                    }
                } catch (Exception e) {
                    SwiftLoggers.getLogger().error("Segkey {} persist failed", segKey.getTable().getId(), e);
                }
            }
        }
    }

    public static class RealtimeToHistoryTransfer extends SegmentTransfer {

        public RealtimeToHistoryTransfer(SegmentKey realtimeSegKey) {
            super(realtimeSegKey, getHistorySegKey(realtimeSegKey));
        }

        private static SegmentKey getHistorySegKey(SegmentKey realtimeSegKey) {
            return new SegmentKeyBean(realtimeSegKey.getTable().getId(), realtimeSegKey.getUri(), realtimeSegKey.getOrder(), StoreType.FINE_IO, realtimeSegKey.getSwiftSchema());
        }

        @Override
        protected void onSucceed() {
            SwiftEventDispatcher.fire(SegmentEvent.UPLOAD_HISTORY, newSegKey);
        }
    }
}