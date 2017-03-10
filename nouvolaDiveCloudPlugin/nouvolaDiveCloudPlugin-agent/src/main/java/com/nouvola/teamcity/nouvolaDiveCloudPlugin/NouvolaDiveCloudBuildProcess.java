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
import org.json.*;

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
                                          String apiKey,
                                          String data){
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
                
                if(data != null){
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    writer.write(data);
                    writer.flush();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
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

    /**
     * Process JSON Strings returned by requests to Nouvola API
     * test_id is the only one that is an int so we will check for it
     */
    private ProcessStatus parseJSONString(String jsonString, String key){
        ProcessStatus status = new ProcessStatus(true, "");
        try{
            JSONObject jObj = new JSONObject(jsonString);
            if(key.equals("test_id")){
                status.message = Integer.toString(jObj.getInt(key));
            }
            else{
                status.message = jObj.getString(key);
            }
        }
        catch(JSONException ex){
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
        String planId = getParameter("planId");
        String apiKey = getParameter("APIKey");
        String returnUrl = getParameter("returnURL");
        String listenTimeOut = getParameter("timeOut");
        String registerUrl = "https://divecloud.nouvola.com/api/v1/hooks";
        String triggerUrl = "https://divecloud.nouvola.com/api/v1/plans/" + planId + "/run";
        boolean isWebhook = false;
        int listenPort = -1;

        // first check if a return URL is available for webhooks
        logger.progressStarted("Checking if there is a return URL...");
        if(returnUrl != null){
            try{
                URL url = new URL(returnUrl);
                listenPort = url.getPort();
                if(listenPort == -1) listenPort = 9999;
                String protocol = url.getProtocol();
                String host = url.getHost();
                String path = url.getPath();
                returnUrl = protocol + "://" + host + ":" + listenPort + path;
                isWebhook = true;
            }
            catch(MalformedURLException ex){
                logger.progressMessage("The return URL given is invalid. Skipping listening for callback");
                logger.progressMessage("Please check Nouvola DiveCloud for test status");
            }
        }
        if(isWebhook){
            logger.progressMessage("Agent setting up to listen for a callback at url " + returnUrl);
            JSONObject regData = new JSONObject();
            regData.put("event", "run_plan");
            regData.put("resource_id", planId);
            regData.put("url", returnUrl);
            status = sendHTTPRequest(registerUrl, "POST", apiKey, regData.toString());
            logger.progressMessage(status.message);
            if(!status.pass) return BuildFinishedStatus.FINISHED_FAILED;
            logger.progressMessage("Setup Success");
        }
        else logger.progressMessage("No return URL given or invalid URL. Agent not listening for a callback");
        logger.progressFinished();

        // trigger the test
        logger.progressStarted("Triggering the DiveCloud Test Plan: " + triggerUrl);                
        status = sendHTTPRequest(triggerUrl, "POST", apiKey, null);
        if(!status.pass){
            logger.progressMessage("Triggering failed: " + status.message);
            logger.progressFinished();
            return BuildFinishedStatus.FINISHED_FAILED;
        }
        logger.progressMessage("Triggering passed: " + status.message);
        logger.progressFinished();
        if(!isWebhook) return BuildFinishedStatus.FINISHED_SUCCESS;

        // if there is a webhook open up a socket and listen for a callback
        logger.progressStarted("Listening for a callback on port " + listenPort + "...");
        try{
            boolean posted = false;
            int timeout = 60; //timeout defaults to 60 minutes
            String jsonMsg = "";
            ServerSocket server = new ServerSocket(listenPort);
            if(listenTimeOut != null){
                timeout = Integer.parseInt(listenTimeOut);
            }
            server.setSoTimeout(timeout * 60000);
            // listen until something is posted
            while(!posted){
                Socket socket = server.accept();
                BufferedReader clientSent = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                BufferedWriter clientResp = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                String line = clientSent.readLine();
                int contLength = 0;
                if(line != null && line.contains("POST")){
                    while(!line.isEmpty()){
                        if(line.contains("Content-Length")){
                            contLength = Integer.parseInt(line.substring(16));
                        }
                        line = clientSent.readLine();
                        if(line == null) line = "";
                    }
                    // now read the json message
                    int bufChar = 0;
                    while(contLength > 0){
                        bufChar = clientSent.read();
                        char msgChar = (char) bufChar;
                        jsonMsg = jsonMsg.concat(String.valueOf(msgChar));
                        contLength = contLength - 1;
                    }
                    clientResp.write("HTTP/1.1 200 OK\r\n\r\n" + "Accepted");
                    posted = true;
                }
                else{
                    clientResp.write("HTTP/1.1 200 OK\r\n\r\n" + "Accepts POST requests only");
                }
                clientResp.close();
                clientSent.close();
                socket.close();
            }
            if(server != null) server.close();
            if(!jsonMsg.isEmpty()){
                status = parseJSONString(jsonMsg, "outcome");
                if(!status.pass){
                    logger.progressMessage("Test Failed: " + status.message);
                    logger.progressFinished();
                    return BuildFinishedStatus.FINISHED_FAILED;
                }

            }
            else{
                logger.progressMessage("Nothing returned by DiveCloud Test");
                logger.progressFinished();
                return BuildFinishedStatus.FINISHED_FAILED;
            }
        }
        catch(SocketTimeoutException ex){
            logger.progressMessage("No callback received - timing out. Please check on your test at Nouvola DiveCloud");
            logger.progressFinished();
            return BuildFinishedStatus.FINISHED_FAILED;
        }
        catch(IOException ex){
            logger.progressMessage("Socket server error: " + ex);
            logger.progressFinished();
            return BuildFinishedStatus.FINISHED_FAILED;
        }
        logger.progressMessage("DiveCloud Test outcome: " + status.message);
        logger.progressFinished();
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
