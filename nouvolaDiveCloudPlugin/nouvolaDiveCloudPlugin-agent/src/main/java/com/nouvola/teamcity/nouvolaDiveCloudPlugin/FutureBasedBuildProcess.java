package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import org.jetbrains.annotations.NotNull;

public abstract class FutureBasedBuildProcess implements BuildProcess,
                                              Callable<BuildFinishedStatus>{

    private Future<BuildFinishedStatus> future;

    public void start() throws RunBuildException{
        try{
            future = Executors.newSingleThreadExecutor().submit(this);
        }
        catch(final RejectedExecutionException ex){
            throw new RunBuildException(ex);
        }
        
    }

    @NotNull
    public BuildFinishedStatus waitFor() throws RunBuildException{
        try{
            final BuildFinishedStatus status = future.get();
            return status;
        }
        catch(final InterruptedException ex){
            throw new RunBuildException(ex);
        }
        catch (final ExecutionException ex){
            throw new RunBuildException(ex);
        }
        catch (final CancellationException ex){
            return BuildFinishedStatus.INTERRUPTED;
        }
    }

    public void interrupt(){
        future.cancel(true);
    }

    public boolean isInterrupted(){
        return future.isCancelled() && isFinished();
    }

    public boolean isFinished(){
        return future.isDone();
    }

}
