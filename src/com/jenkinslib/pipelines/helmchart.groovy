package com.jenkinslib.pipelines

import com.jenkinslib.GlobalVars
import com.jenkinslib.helpers.DockerUtils
import com.jenkinslib.helpers.RepoUtils
import com.jenkinslib.helpers.DiscordUtils
import com.jenkinslib.helpers.HelmUtils

def runStages(GlobalVars p) {

    def dockerutils = new DockerUtils()
    def repoutils = new RepoUtils()
    def discordutils = new DiscordUtils()
    def helmutils = new HelmUtils()

    stage('Checkout') {
        repoutils.repoCheckout(p)
    }
    
    stage('Notify to Discord') {
        discordutils.notifyStarted(p)
    }

    stage('Build Helm Chart') {
        helmutils.buildChart(p)
    }

    stage('Done') {
        discordutils.notifySuccess(p)
    }


}