package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.jetbrains.annotations.NotNull;

public class NouvolaDiveCloudAgentBuildRunner implements AgentBuildRunner, AgentBuildRunnerInfo {

    @NotNull
    private final ArtifactsWatcher artifactsWatcher;

    public NouvolaDiveCloudAgentBuildRunner(@NotNull ArtifactsWatcher artifactsWatcher)
    {
        this.artifactsWatcher = artifactsWatcher;
    }

    public BuildProcess createBuildProcess(@NotNull AgentRunningBuild build,
                                           @NotNull BuildRunnerContext context) throws RunBuildException{

        return new NouvolaDiveCloudBuildProcess(build, context, this.artifactsWatcher);
    }

    public AgentBuildRunnerInfo getRunnerInfo()
    {
        return this;
    }

    public String getType()
    {
        return NouvolaDiveCloudConstants.RUNNER_TYPE;
    }

    public boolean canRun(BuildAgentConfiguration bac)
    {
        return true;
    }
}
