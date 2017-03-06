package com.nouvola.teamcity.nouvolaDiveCloudPlugin;

import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.InvalidProperty;
 
/**
* Validation for user input for the runner
*/ 
public class NouvolaDiveCloudPropertiesProcessor implements PropertiesProcessor{
    
    public Collection<InvalidProperty> process(Map<String, String> properties){
        // don't do anything
        return new ArrayList<InvalidProperty>();
    }
}
