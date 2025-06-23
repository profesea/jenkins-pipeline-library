package com.jenkinslib.pipelines

import com.jenkinslib.GlobalVars


import com.jenkinslib.helpers.DockerUtils
import com.jenkinslib.helpers.DiscordUtils
import com.jenkinslib.helpers.RepoUtils
import com.jenkinslib.helpers.HelmUtils




def runStages(GlobalVars p) {

    def dockerutils = new DockerUtils()
    def discordutils = new DiscordUtils()
    def repoutils = new RepoUtils()
    def helmutils = new HelmUtils()

    def deploymentPipeline = {
        stage('Checkout') {
            repoutils.repoCheckout(p)
            repoutils.repoIngressCheckout(p)
        }

        stage('Notify to Discord') {
            discordutils.notifyStarted(p)
        }

        if (p.pullSecret == "true") {
            stage('Pull Secrets') {
                dockerutils.pullGSM(p)
            }  
        }

        stage('Build Docker Image') {
            dockerutils.buildAndPushDockerImage(p)
            dockerutils.removeLocalDockerImage(p)
        }

        stage('Deploy to Cluster') {
            helmutils.installChartMultibranch(p)
        }
        
        stage('Done') {
            discordutils.notifySuccess(p)
        }

        stage('Cleanup Workspace') {
            cleanWs()
        }
    
    }



    if (p.branchName == "development") {
        p.buildArg = "staging"
        deploymentPipeline()
    } else if (p.branchName ==~ /v.*/) {        
        p.buildEnv = "production"
        p.buildArg = "production"
        deploymentPipeline()
    }
    
}
