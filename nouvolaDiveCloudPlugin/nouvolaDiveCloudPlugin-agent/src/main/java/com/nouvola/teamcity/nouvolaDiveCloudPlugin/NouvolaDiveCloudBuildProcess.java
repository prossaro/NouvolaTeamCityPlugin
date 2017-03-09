package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

public class NouvolaDiveCloudBuildProcess extends FutureBasedBuildProcess {

    private final AgentRunningBuild build;
    private final BuildRunnerContext context;

    public NouvolaDiveCloudBuildProcess(@NotNull AgentRunningBuild build,
                                        @NotNull BuildRunnerContext context){
        this.build = build;
        this.context = context;
    }

    @NotNull
    public BuildFinishedStatus call() throws Exception{
        //process here
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
