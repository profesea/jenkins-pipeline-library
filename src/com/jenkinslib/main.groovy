#!/usr/bin/groovy

package com.jenkinslib

import com.jenkinslib.GlobalVars
import com.jenkinslib.pipelines.*
import com.jenkinslib.helpers.*


def main(script) {
    withFolderProperties{


        p = new GlobalVars(script.env)

        def repoutils = new RepoUtils()
        def discordutils = new DiscordUtils()
        def helmutils = new HelmUtils()

        String pipelineType = script.env.PIPELINE

        try {
            if (pipelineType == "helm-chart") {
                new helmchart().runStages(p)
            } else if (pipelineType == "multibranch") {
                new multibranch().runStages(p)
            } 
        }
        catch (Exception e) {
            echo "Error : " + e.toString()
            
            if (e instanceof org.jenkinsci.plugins.workflow.steps.FlowInterruptedException) {
                discordutils.notify(p, "abort")

            }
            else {
                discordutils.notifyFailed(p, e.toString())
            }
            throw e
            
        }
    }

}

return this