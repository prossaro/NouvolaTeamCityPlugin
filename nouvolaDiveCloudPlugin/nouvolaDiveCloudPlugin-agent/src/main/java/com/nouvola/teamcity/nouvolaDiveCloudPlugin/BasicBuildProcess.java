package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import org.jetbrains.annotations.NotNull;

public class BasicBuildProcess implements BuildProcess {

    public void start() throws RunBuildException{
        // do something here
        
    }

    @NotNull
    public BuildFinishedStatus waitFor() throws RunBuildException{
        // wait for runner to finish or to interrupt
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }

    public void interrupt(){
        // tell build to stop
    }

    public boolean isInterrupted(){
        // check if the build has been interrupted
        return false;
    }

    public boolean isFinished(){
        // check if the build is done
        return true;
    }

}
