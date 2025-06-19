#!/usr/bin/groovy

package com.jenkinslib.helpers

import com.jenkinslib.GlobalVars

String getRegistryHost(String buildEnv, String projectName) {
    if (buildEnv == 'staging') {
        return "asia-southeast2-docker.pkg.dev/profesea-463213/staging"
        
    } else if (buildEnv == 'production') {
        return "asia-southeast2-docker.pkg.dev/profesea-463213/production"
    }

    return ''
}


def getImageUri(GlobalVars p) {
    String branchCode = p.branchName.replace('/', '')
    String registryHost = getRegistryHost(p.buildEnv, p.projectName)
    echo "buildEnv: ${p.buildEnv}, projectName: ${p.projectName}"
    echo "${registryHost}/${p.repositoryName}:${p.buildEnv}-${branchCode}-${p.version}"

    return "${registryHost}/${p.repositoryName}:${p.buildEnv}-${branchCode}-${p.version}"
}

def buildDockerImage(GlobalVars p) {
    if (p.buildWithArg == "true") {
        return docker.build(getImageUri(p), "-f ${p.dockerfileName} --build-arg buildArg=${p.buildArg} .")
    } else {
        return docker.build(getImageUri(p), "-f ${p.dockerfileName} .")
    }
}

def buildDockerImageTest(GlobalVars p) {
    return docker.build(getImageUri(p), "-f ${p.dockerfileTestName} .")
}

void buildAndPushDockerImage(GlobalVars p) {
    serviceImage = buildDockerImage(p)
    serviceImage.push()
}

void removeLocalDockerImage(GlobalVars p) {
    imageUri = getImageUri(p)
    sh "docker rmi -f ${imageUri}"
}

def pullGSM(GlobalVars p) {
    if (p.repositoryName == p.serviceName) {
        sh "gcloud secrets versions access latest --secret=${p.repositoryName}-${p.buildEnv}-secret --project=profesea-463213 > ${env.WORKSPACE}/tmp-env.json"
    } else {
        sh "gcloud secrets versions access latest --secret=${p.serviceName}-secret --project=profesea-463213 > ${env.WORKSPACE}/tmp-env.json"
    }
    sh "jq -r 'to_entries | .[] | \"\\(.key)=\\(.value)\"' ${env.WORKSPACE}/tmp-env.json > ${env.WORKSPACE}/.env"

}