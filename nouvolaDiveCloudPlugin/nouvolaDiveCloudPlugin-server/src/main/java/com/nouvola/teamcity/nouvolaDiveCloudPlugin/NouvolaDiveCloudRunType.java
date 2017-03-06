package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
 
/**
* Entry Point to Nouvola DiveCloud Build Runner
*/ 
public class NouvolaDiveCloudRunType extends RunType{

    private PluginDescriptor pluginDescriptor;
    
    public NouvolaDiveCloudRunType(@NotNull RunTypeRegistry runTypeRegistry,
                                   @NotNull PluginDescriptor pluginDescriptor){
        this.pluginDescriptor = pluginDescriptor;
        runTypeRegistry.registerRunType(this);
    }

    @Override
    @NotNull
    public String getType(){
        return NouvolaDiveCloudConstants.RUNNER_TYPE;
    }

    @Override
    @NotNull
    public String getDisplayName(){
        return NouvolaDiveCloudConstants.RUNNER_DISPLAY_NAME;
    }

    @Override
    @NotNull
    public String getDescription(){
        return NouvolaDiveCloudConstants.RUNNER_DESCRIPTION;
    }
    
    @Override
    @Nullable
    public String getEditRunnerParamsJspFilePath()
    {
        return this.pluginDescriptor.getPluginResourcesPath("editNouvolaDiveCloudSettings.jsp");
    }

    @Override
    @Nullable
    public String getViewRunnerParamsJspFilePath()
    {
        return this.pluginDescriptor.getPluginResourcesPath("viewNouvolaDiveCloudSettings.jsp");
    }

    @Override
    @Nullable
    public Map<String, String> getDefaultRunnerProperties()
    {
        Map<String, String> parameters = new HashMap<String, String>();

        return parameters;
    }

    @Override
    @Nullable
    public PropertiesProcessor getRunnerPropertiesProcessor()
    {
        return new NouvolaDiveCloudPropertiesProcessor();
    }
}
