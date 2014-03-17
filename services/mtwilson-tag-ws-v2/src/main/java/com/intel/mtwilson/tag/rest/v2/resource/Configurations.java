/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.Configuration;
import com.intel.mtwilson.tag.model.ConfigurationCollection;
import com.intel.mtwilson.tag.model.ConfigurationFilterCriteria;
import com.intel.mtwilson.tag.model.ConfigurationLocator;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.rest.v2.repository.ConfigurationRepository;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: move to separate configuration feature
 *
 * @author ssbangal
 */
@V2
@Path("/configurations")
public class Configurations extends AbstractJsonapiResource<Configuration, ConfigurationCollection, ConfigurationFilterCriteria, NoLinks<Configuration>, ConfigurationLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private ConfigurationRepository repository;
    
    public Configurations() {
        repository = new ConfigurationRepository();
    }
    
    @Override
    protected ConfigurationCollection createEmptyCollection() {
        return new ConfigurationCollection();
    }

    @Override
    protected ConfigurationRepository getRepository() {
        return repository;
    }
        
}
