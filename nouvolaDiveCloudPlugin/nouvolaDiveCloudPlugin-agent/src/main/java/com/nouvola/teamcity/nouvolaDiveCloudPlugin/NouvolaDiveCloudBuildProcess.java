package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

public class NouvolaDiveCloudBuildProcess extends BasicBuildProcess {

    private final AgentRunningBuild build;
    private final BuildRunnerContext context;

    public NouvolaDiveCloudBuildProcess(AgentRunningBuild build, BuildRunnerContext context){
        this.build = build;
        this.context = context;
    }

    public BuildFinishedStatus process(){
        //process here
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
