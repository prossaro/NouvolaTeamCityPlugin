package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import org.json.*;

public class NouvolaDiveCloudBuildProcess extends FutureBasedBuildProcess {

    private final AgentRunningBuild build;
    private final BuildRunnerContext context;
    private final ArtifactsWatcher artifactsWatcher;
    private BuildProgressLogger logger;

    public NouvolaDiveCloudBuildProcess(@NotNull AgentRunningBuild build,
                                        @NotNull BuildRunnerContext context,
                                        @NotNull ArtifactsWatcher artifactsWatcher){
        this.build = build;
        this.context = context;
        this.artifactsWatcher = artifactsWatcher;
    }

    private String getParameter(@NotNull final String parameterName){
        final String value = context.getRunnerParameters().get(parameterName);
        if(value == null || value.trim().length() == 0) return null;
        return value.trim();
    }

    /**
     * Object for process status and messages
     */
    private static class ProcessStatus{
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
                    writer.close();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                String result = "";

                while ((line = reader.readLine()) != null){
                    result = result.concat(line);

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

    /**
     * Write to a file
     * Return an error message if failed else return an empty string
     */
    private String writeToFile(String filename, String content){
        String result = "";
        File buildDir = this.build.getBuildTempDirectory();
        File resultsFile = new File(buildDir, filename);
        try{
            if(!resultsFile.exists()) resultsFile.createNewFile();
            Writer writer = new BufferedWriter(new FileWriter(resultsFile));
            writer.write(content);
            writer.close();
            this.artifactsWatcher.addNewArtifactsPath(resultsFile.getAbsolutePath());
        }
        catch(IOException ex){
            result = ex.toString();
        }
        return result;
    }


    @NotNull
    public BuildFinishedStatus call() throws Exception{
        //process here
        logger = build.getBuildLogger();
        ProcessStatus status;
        String planId = getParameter("planId");
        String apiKey = getParameter("APIKey");
        String waitTime = getParameter("waitTime");
        String returnUrl = getParameter("returnURL");
        String listenTimeOut = getParameter("timeOut");
        String registerUrl = "https://divecloud.nouvola.com/api/v1/hooks";
        String triggerUrl = "https://divecloud.nouvola.com/api/v1/plans/" + planId + "/run";
        String testId = "";
        String pollUrl = "https://divecloud.nouvola.com/api/v1/test_instances/";
        boolean isWebhook = false;
        int listenPort = -1;
        String results_begin = "<!DOCTYPE html><html><body>View test results: <a href='";
        String results_link = "";
        String results_middle = "'>";
        String results_end = "</a></body></html>";
        String results_file = "results_link.html";

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
                logger.progressMessage("Return URL OK");
                isWebhook = true;
            }
            catch(MalformedURLException ex){
                logger.progressMessage("The return URL given is invalid. Polling DiveCloud instead.");
            }
        }

        // Register the return URL with the webhook service
        if(isWebhook){
            logger.progressMessage("Agent setting up to listen for a callback at url " + returnUrl);
            JSONObject regData = new JSONObject();
            regData.put("event", "run_plan");
            regData.put("resource_id", planId);
            regData.put("url", returnUrl);
            status = sendHTTPRequest(registerUrl, "POST", apiKey, regData.toString());
            if(!status.pass){
                logger.progressMessage("Registration failed: " + status.message);
                return BuildFinishedStatus.FINISHED_FAILED;
            }
            logger.progressMessage("Setup Success");
        }
        logger.progressFinished();

        // trigger the test
        logger.progressStarted("Triggering the DiveCloud Test Plan: " + triggerUrl);                
        status = sendHTTPRequest(triggerUrl, "POST", apiKey, null);
        if(!status.pass){
            logger.progressMessage("Triggering failed: " + status.message);
            logger.progressFinished();
            return BuildFinishedStatus.FINISHED_FAILED;
        }
        status = parseJSONString(status.message, "test_id");
        if(!status.pass){
            logger.progressMessage("Could not get a test ID: " + status.message);
            return BuildFinishedStatus.FINISHED_FAILED;
        }
        testId = status.message;
        logger.progressMessage("Got test ID: " + status.message);
        logger.progressFinished();

        // wait for results
        String jsonMsg = "";
        if(isWebhook){
            logger.progressStarted("Listening for a callback on port " + listenPort + "...");
            try{
                boolean posted = false;
                int timeout = 60; //timeout defaults to 60 minutes
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
        }
        else{
            // no webhook means poll for results
            boolean finished = false;
            long wait = 1; //default to 1 minute
            if(waitTime != null) wait = Long.parseLong(waitTime);
            logger.progressStarted("Polling for results at: " + pollUrl + testId + " after " + wait + " minutes...");
            try{
                Thread.sleep(wait * 60000);
            }
            catch(InterruptedException ex){
                logger.progressMessage("Job interrupted. Check test status at Nouvola DiveCloud");
                logger.progressFinished();
                Thread.currentThread().interrupt();
                return BuildFinishedStatus.FINISHED_FAILED;
            }
            logger.progressMessage("Polling started...");
            while(!finished){
                status = sendHTTPRequest(pollUrl + testId, "GET", apiKey, null);
                if(!status.pass){
                    logger.progressMessage(status.message);
                    logger.progressFinished();
                    return BuildFinishedStatus.FINISHED_FAILED;
                }
                jsonMsg = status.message;
                status = parseJSONString(jsonMsg, "status");
                if(!status.pass){
                    logger.progressMessage(status.message);
                    logger.progressFinished();
                    return BuildFinishedStatus.FINISHED_FAILED;
                }
                if(status.message.equals("Emailed")) finished = true;
                else{
                    try{
                        Thread.sleep(60000);
                    }
                    catch(InterruptedException ex){
                        logger.progressMessage("Polling interrupted. Check test status at Nouvola DiveCloud");
                        logger.progressFinished();
                        Thread.currentThread().interrupt();
                        return BuildFinishedStatus.FINISHED_FAILED;
                    }
                }
            }
        }

        boolean test_pass = false;
        if(!jsonMsg.isEmpty()){
            logger.progressMessage(jsonMsg);
            status = parseJSONString(jsonMsg, "outcome");
            if(status.pass && status.message.equals("Pass")){
                logger.progressMessage("DiveCloud test passed");
                test_pass = true;
            }
            else{
                logger.progressMessage("Test Failed: " + status.message);
            }
            // create artifact
            ProcessStatus link_status = parseJSONString(jsonMsg, "shareable_link");
            if(!link_status.pass){
                logger.progressMessage(link_status.message);
                logger.progressFinished();
                return BuildFinishedStatus.FINISHED_FAILED;
            }
            results_link = link_status.message;
            String link = results_begin + results_link + results_middle + results_link + results_end;
            String writeStatus = writeToFile(results_file, link);
            if(!writeStatus.isEmpty()){
                logger.progressMessage("Failed to create artifact: " + writeStatus);
                logger.progressFinished();
                return BuildFinishedStatus.FINISHED_FAILED;
            }
            logger.progressMessage("Report ready: " + results_link);
        }
        else{
            logger.progressMessage("Nothing returned by DiveCloud Test - empty JSON message");
            logger.progressFinished();
            return BuildFinishedStatus.FINISHED_FAILED;
        }

        logger.progressFinished();
        if(!test_pass) return BuildFinishedStatus.FINISHED_FAILED;
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
