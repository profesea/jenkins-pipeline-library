#!/usr/bin/groovy

package com.jenkinslib.helpers

import com.jenkinslib.GlobalVars


def printTagName(GlobalVars p) {
    return sh (
        script: 'git tag --contains | tail -1',
        returnStdout: true
    )
}

def repoGitCommitSha (p) {
    return sh (
        script: 'git rev-parse HEAD',
        returnStdout: true
    ).trim()
}


def repoCheckout(GlobalVars p) {
    checkout([
        $class: 'GitSCM',
        branches: [[name: p.branchName]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [[
            $class: 'SubmoduleOption',
            disableSubmodules: false,
            parentCredentials: true,
            recursiveSubmodules: true,
            reference: '',
            trackingSubmodules: false
        ]],
        submoduleCfg: [],
        userRemoteConfigs: [[
            credentialsId: 'profesea-github-jenkins',
            url: "git@github.com:profesea/${p.repositoryName}.git"
        ]]
    ])
}


def repoTagsCheckout(GlobalVars p) {
    checkout([
        $class: 'GitSCM',
        branches: [[name: "refs/tags/*"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [[
            $class: 'SubmoduleOption',
            disableSubmodules: false,
            parentCredentials: true,
            recursiveSubmodules: true,
            reference: '',
            trackingSubmodules: false
        ]],
        submoduleCfg: [],
        userRemoteConfigs: [[
            credentialsId: 'profesea-github-jenkins',
            url: "git@github.com:profesea/${p.repositoryName}.git",
             refspec: '+refs/tags/*:refs/remotes/origin/tags/*'
        ]]
    ])

}


def getPRNumber(GlobalVars p) {
    def PR_NUMBER = sh(script: "gh pr list -s merged --json number --jq '.[0].number'", returnStdout: true).trim()
    return PR_NUMBER
}