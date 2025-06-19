#!/usr/bin/groovy

package com.jenkinslib.helpers

import com.jenkinslib.GlobalVars

String helmRepo(GlobalVars p) {
    return ""
}

String snakeCaseToCamelCase(String str) {
    return str.tokenize('_').indexed().collect {idx, item ->
        if(idx == 0) {
            return item.toLowerCase()
        } else {
            return item[0].toUpperCase() + item.substring(1).toLowerCase()
        }
    }.join("")
}

String helmBucketName(GlobalVars p) {
    return "oci://asia-southeast2-docker.pkg.dev/profesea-463213/helm-chart-repo"
}

String appChartName(GlobalVars p) {
    return "${helmBucketName(p)}/${p.chartName}"
}

String serviceName(GlobalVars p) {
    return "${p.serviceName}-${p.buildEnv}"
}

String image(GlobalVars p) {
    def dockerutils = new DockerUtils()
    return "${dockerutils.getImageUri(p)}"
}

String appEnv(GlobalVars p) {
    def envutils = new EnvUtils()
    envutils.pullConfigProperties(p)
    
    def allEnv = readProperties file: 'envConfigProperties'
    allEnv['HC_IMAGE'] = image(p)
    allEnv['HC_SERVICE_NAME'] = serviceName(p)
    p.namespaceName = allEnv['NAMESPACE']
    
    List<String> sets = []

    for(env in allEnv) {
        if(env.key.startsWith("HC_")) {
            String valName = snakeCaseToCamelCase(env.key.substring(3))
            sets.add("${valName}=${env.value}")
        }
    }

    return sets.isEmpty() ? "" : "--set " + sets.join(",")
}

def buildChart(GlobalVars p) {
    String chartDir = "./helm-chart/${p.chartName}"
    String chartVersion = readYaml(file: "${chartDir}/Chart.yaml").version
    String chartPackageFilename = "${p.chartName}-${chartVersion}.tgz"
    String helmBucketName = "asia-southeast2-docker.pkg.dev/profesea-463213/helm-chart-repo"


    sh "helm lint ${chartDir}"
    sh "helm package ${chartDir}"
    sh "helm push ${chartPackageFilename} ${helmBucketName}"
}


def pullEnvConfig(GlobalVars p) {
    sh "gcloud secrets versions access latest --secret=${p.serviceName}-${p.buildEnv}-env-secret --project=profesea-463213 > .env"
}

def pullConfigProperties(GlobalVars p) {
    sh "gcloud secrets versions access latest --secret=${p.repositoryName}-${p.buildEnv}-config-properties --project=profesea-463213 > envConfigProperties"
}

def installChartMultibranch(GlobalVars p) {
    def dockerutils = new DockerUtils()
    pullConfigProperties(p)

    String helmBucketName = "asia-southeast2-docker.pkg.dev/profesea-463213/helm-chart-repo"

    String appChartName = "${helmBucketName}/${p.chartName}"
    String serviceName = "${p.serviceName}-${p.buildEnv}"
    String image = dockerutils.getImageUri(p)

    // allEnv = p.getEnv()
    def allEnv = readProperties file: 'envConfigProperties'


    allEnv['HC_IMAGE'] = image
    allEnv['HC_SERVICE_NAME'] = serviceName
    p.namespaceName = allEnv['NAMESPACE']


    List<String> sets = []

    for(env in allEnv) {
        if(env.key.startsWith("HC_")) {
            String valName = snakeCaseToCamelCase(env.key.substring(3))
            sets.add("${valName}=${env.value}")
        }
    }

    String setCommand = sets ? "--set " + sets.join(",") : ""
    String versionCommand = p.chartVersion ? "--version ${p.chartVersion}" : ""

    docker.image("asia-southeast2-docker.pkg.dev/profesea-463213/profesea-deployer/${p.projectName}-${p.buildEnv}:latest")
        .inside("-v $HOME/cluster-config/${p.projectName}-${p.buildEnv}/config/:/config -u root") { 
            sh "gcloud auth application-default print-access-token | helm registry login -u oauth2accesstoken --password-stdin https://asia-southeast2-docker.pkg.dev"
            sh "helm upgrade --install --namespace ${p.namespaceName} --version ${p.chartVersion} ${setCommand} ${serviceName} ${appChartName}"    
            sh "kubectl rollout status deployment/${p.serviceName}-${p.buildEnv}-release-deployment -n ${p.namespaceName}"    
        }

}