package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

public class NouvolaDiveCloudAgentBuildRunner implements AgentBuildRunner, AgentBuildRunnerInfo {

    public BuildProcess createBuildProcess(@NotNull AgentRunningBuild arb,
                                           @NotNull BuildRunnerContext brc) throws RunBuildException{

        return new NouvolaDiveCloudBuildProcess(arb, brc);
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
