package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;

public class NouvolaDiveCloudBuildProcess extends FutureBasedBuildProcess {

    private final AgentRunningBuild build;
    private final BuildRunnerContext context;
    private BuildProgressLogger logger;

    public NouvolaDiveCloudBuildProcess(@NotNull AgentRunningBuild build,
                                        @NotNull BuildRunnerContext context){
        this.build = build;
        this.context = context;
    }

    private String getParameter(@NotNull final String parameterName){
        final String value = context.getRunnerParameters().get(parameterName);
        if(value == null || value.trim().length() == 0) return null;
        return value.trim();
    }

    /**
     * Object for process status and messages
     */
    private class ProcessStatus{
        public boolean pass;
        public String message;

        public ProcessStatus(boolean pass, String message){
            this.pass = pass;
            this.message = message;
        }
    }

    /**
     * Send HTTP request and return a process status
     */
    private ProcessStatus sendHTTPRequest(String url,
                                          String httpAction,
                                          String apiKey){
        ProcessStatus status = new ProcessStatus(true, "");
        try{
            URL urlObj = new URL(url);
            try{
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod(httpAction);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("x-api", apiKey);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                String result = "";

                while ((line = reader.readLine()) != null){
                    result = result + line;

                }
                reader.close();
                if(!result.isEmpty()){
                    status.message = result;
                }
                else{
                    status.pass = false;
                    status.message = "Nouvola DiveCloud API " + url + " did not return a result";
                }
            }
            catch(IOException ex){
                status.pass = false;
                status.message = ex.toString();
            }
        }
        catch(MalformedURLException ex){
            status.pass = false;
            status.message = ex.toString();
        }
        return status;
    }

    @NotNull
    public BuildFinishedStatus call() throws Exception{
        //process here
        logger = build.getBuildLogger();
        ProcessStatus status;
        String triggerUrl = "https://divecloud.nouvola.com/api/v1/plans/" + getParameter("planId") + "/run";
        logger.progressStarted("Starting DiveCloud Test");
        status = sendHTTPRequest(triggerUrl, "POST", getParameter("APIKey"));
        logger.progressMessage(status.message);
        logger.progressFinished();
        if(!status.pass) return BuildFinishedStatus.FINISHED_FAILED;
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
