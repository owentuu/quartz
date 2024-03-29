package com.example.quartz.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@DisallowConcurrentExecution
@Component
public class DynamicJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //JobDetail中的JobDataMap是共用的,从getMergedJobDataMap获取的JobDataMap是全新的对象
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        String jarPath = map.getString("jarPath");
        String parameter = map.getString("parameter");
        String vmParam = map.getString("vmParam");
        log.info("Running Job name : {} ", map.getString("name"));
        log.info("Running Job description : " + map.getString("JobDescription"));
        log.info("Running Job group: {} ", map.getString("group"));
        log.info("Running Job cron : " + map.getString("cronExpression"));
        log.info("Running Job jar path : {} ", jarPath);
        log.info("Running Job parameter : {} ", parameter);
        log.info("Running Job vmParam : {} ", vmParam);
        long startTime = System.currentTimeMillis();
        if (StringUtils.isNotBlank(jarPath)) {
            File jar = new File(jarPath);
            if (jar.exists()) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.directory(jar.getParentFile());
                List<String> commands = new ArrayList<>();
                commands.add("java");
                if (StringUtils.isNotBlank(vmParam)) commands.add(vmParam);
                commands.add("-jar");
                commands.add(jarPath);
                if (StringUtils.isNotBlank(parameter)) commands.add(parameter);
                processBuilder.command(commands);
                log.info("Running Job details as follows >>>>>>>>>>>>>>>>>>>>: ");
                log.info("Running Job commands : {}  ", commands);
                try {
                    Process process = processBuilder.start();
                    logProcess(process.getInputStream(), process.getErrorStream());
                } catch (IOException e) {
                    throw new JobExecutionException(e);
                }
            } else throw new JobExecutionException("Job Jar not found >>  " + jarPath);
        }
        long endTime = System.currentTimeMillis();
        log.info(">>>>>>>>>>>>> Running Job has been completed , cost time :  " + (endTime - startTime) + "ms\n");
    }

    //打印Job执行内容的日志
    private void logProcess(InputStream inputStream, InputStream errorStream) throws IOException {
        String inputLine;
        String errorLine;
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
        while ((inputLine = inputReader.readLine()) != null) log.info(inputLine);
        while ((errorLine = errorReader.readLine()) != null) log.error(errorLine);
    }
}
