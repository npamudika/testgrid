/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.testgrid.infrastructure.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.InfrastructureProvider;
import org.wso2.carbon.testgrid.common.Script;
import org.wso2.carbon.testgrid.common.Utils;
import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;

import java.nio.file.Paths;

/**
 * This class creates the infrastructure for running tests
 */
public class ShellScriptProvider implements InfrastructureProvider {
    private static final Log log = LogFactory.getLog(ShellScriptProvider.class);
    private final static String SHELL_SCRIPT_PROVIDER = "Shell Executor";

    @Override
    public String getProviderName() {
        return SHELL_SCRIPT_PROVIDER;
    }

    @Override
    public boolean canHandle(Infrastructure infrastructure) {
        boolean isScriptsAvailable = true;
        for (Script script : infrastructure.getScripts()){
            if (!Script.ScriptType.INFRA_CREATE.equals(script.getScriptType()) &&
                    !Script.ScriptType.INFRA_DESTROY.equals( script.getScriptType())){
                isScriptsAvailable = false;
            }
        }
        return isScriptsAvailable;
    }

    @Override
    public Deployment createInfrastructure(Infrastructure infrastructure, String infraRepoDir) throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(infraRepoDir, "DeploymentPatterns" , infrastructure.getName()).toString();

        log.info("Creating the Kubernetes cluster...");
        Utils.executeCommand("bash " +
                Paths.get(testPlanLocation, getScriptToExecute(infrastructure, Script.ScriptType.INFRA_CREATE)),
                null);
        return null;
    }

    @Override
    public boolean removeInfrastructure(Infrastructure infrastructure, String infraRepoDir) throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(infraRepoDir, "DeploymentPatterns" , infrastructure.getName()).toString();

        log.info("Destroying test environment...");
        if(Utils.executeCommand("bash " +
                        Paths.get(testPlanLocation, getScriptToExecute(infrastructure, Script.ScriptType.INFRA_DESTROY)),
                null)) {
            return true;
        }
        return false;
    }

    private String getScriptToExecute(Infrastructure infrastructure, Script.ScriptType ScriptType) {
        for (Script script : infrastructure.getScripts()){
            if (ScriptType.equals(script.getScriptType())){
                return script.getFilePath();
            }
        }
        return null;
    }
}
