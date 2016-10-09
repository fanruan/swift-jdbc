package com.fr.bi.cal.generate.timerTask.quartz;

import com.finebi.cube.conf.CubeBuild;
import com.finebi.cube.conf.CubeGenerationManager;
import com.fr.bi.base.BIUser;
import com.fr.bi.cal.generate.BuildCubeTask;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.third.org.quartz.Job;
import com.fr.third.org.quartz.JobDataMap;
import com.fr.third.org.quartz.JobExecutionContext;
import com.fr.third.org.quartz.JobExecutionException;

import java.util.Date;

/**
 * Created by kary on 16/6/29.
 */

public class JobTask implements Job {

    public JobTask() {
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        long userId = Long.valueOf(data.get("userId").toString());
        String jobName = data.getString("jobName");
//        String sourceName = data.getString("sourceName");

//        boolean tableExisted = null != TimerScheduleAdapter.tableCheck(userId, sourceName);
        /*删除表时会删除该表更新信息，不需要在这边再做检查*/
//        if (!tableExisted && !DBConstant.CUBE_UPDATE_TYPE.GLOBAL_UPDATE.equals(sourceName)) {
//            return;
//        }
        CubeBuild cubeBuild = (CubeBuild) data.get("CubeBuild");
        String message = "timerTask started!Current time is:" + new Date() + "\n Current task：" + jobName + "\nCurrent User：" + userId + "\n";
        BILoggerFactory.getLogger().info(message);
        CubeGenerationManager.getCubeManager().addTask(new BuildCubeTask(new BIUser(userId), cubeBuild), userId);
    }
}
