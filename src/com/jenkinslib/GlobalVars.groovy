#!/usr/bin/env groovy
package com.jenkinslib

class GlobalVars {
    
    String repositoryName
    String branchName
    String dockerfileName
    String buildEnv
    String version
    String chartName
    String chartVersion
    String namespaceName
    String serviceName
    String projectName
    String pipelineType
    String pullSecret
    String buildArg
    String buildWithArg
    String useServiceAuth
    def env

    GlobalVars(env) {

        this.repositoryName = env.REPOSITORY
        this.branchName = env.BRANCH_NAME ? env.BRANCH_NAME : "main"
        this.dockerfileName = env.DOCKERFILE ? env.DOCKERFILE : "Dockerfile"
        this.buildEnv = env.BUILD_ENV ? env.BUILD_ENV : "staging"
        this.version = "v" + ((long)(new Date().getTime() / 1000)).toString()
        this.chartName = env.CHART_NAME ? env.CHART_NAME : "default-chart"
        this.chartVersion = env.CHART_VERSION ? env.CHART_VERSION: "1.0.0-release"
        this.namespaceName = env.NAMESPACE ? env.NAMESPACE: "staging"
        this.serviceName = env.SERVICE_NAME ? env.SERVICE_NAME: env.REPOSITORY
        this.projectName = env.PROJECT_NAME ? env.PROJECT_NAME : "profesea"
        this.pullSecret = env.PULL_SECRET ? env.PULL_SECRET: false
        this.buildWithArg = env.BUILD_WITH_ARG ? env.BUILD_WITH_ARG: false
        this.buildArg = env.BUILD_ARG
        this.env = env

    }

    void setEnv(env) {
        this.env = env
    }

    String getEnv() {
        return this.env.getEnvironment()
    }
}