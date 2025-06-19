#!/usr/bin/groovy

package com.jenkinslib.helpers

import com.jenkinslib.GlobalVars

String getGitBranchOrTags(String buildEnv, branchName) {
    return "${branchName}"
}

String gitCommitMessage(String buildEnv) {
    return sh (
        script: 'git log -1',
        returnStdout: true
    )
}


String getGithubPRTitle(GlobalVars p) {
    def PR_NUMBER = sh(script: "gh pr list -s merged --json number --jq '.[0].number'", returnStdout: true).trim()
    if (!PR_NUMBER) {
        def PR_TITLE = sh(
            script: "git log -1",
            returnStdout: true)
    } else {
        def PR_TITLE = sh(
                script: "gh pr view ${PR_NUMBER} --json title --jq '.title'",
                returnStdout: true
            )
    }
}

String getDiscordToken(String buildEnv, namespaceName) {
    return "discordToken"
}


def notifyStarted(GlobalVars p) {


    String discordToken = getDiscordToken(p.buildEnv, p.namespaceName)

    String gitMessage = getGithubPRTitle(p)
    gitMessage = gitMessage.replaceAll('\n', '\\\\n')

    gitBranch = getGitBranchOrTags(p.buildEnv, p.branchName)


    sh """
    curl -XPOST \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{
        \"content\": \"Deployment is started : ${env.BUILD_URL}\",
        "embeds":[{
            \"title\": \"Repository name\",
            \"description\": \"${p.repositoryName}\",
            \"color\": \"12566463\",
            \"fields\": [
                {
                    \"name\": \"PR Title\", 
                    \"value\": \"${gitMessage}\"
                }, {
                    \"name\": \"Tags\", 
                    \"value\": \"${gitBranch}\"
                }
            ]}
        ]}' \
    "https://discord.com/api/webhooks/${discordToken}"
    """

}

def notifySuccess(GlobalVars p) {
    String discordToken = getDiscordToken(p.buildEnv, p.namespaceName)
    // String gitMessage = gitCommitMessage(p.buildEnv)
    String gitMessage = getGithubPRTitle(p)

    gitMessage = gitMessage.replaceAll('\n', '\\\\n')


    sh """
    curl -XPOST \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{
        \"content\": \"Deployment is Success : ${env.BUILD_URL}\",
        "embeds":[{
            \"title\": \"Repository name\",
            \"description\": \"${p.repositoryName}\",
            \"color\": \"65280\",
            \"fields\": [
                {
                    \"name\": \"PR Title\", 
                    \"value\": \"${gitMessage}\"
                },
                {
                    \"name\": \"Tags\", 
                    \"value\": \"${p.branchName}\"
                }
            ]}
        ]}' \
    "https://discord.com/api/webhooks/${discordToken}"
    """
}


def notifyFailed(GlobalVars p, errorMessage) {
    String discordToken = getDiscordToken(p.buildEnv, p.namespaceName)
    // String gitMessage = gitCommitMessage(p.buildEnv)
    String gitMessage = getGithubPRTitle(p)

    gitMessage = gitMessage.replaceAll('\n', '\\\\n')

    gitBranch = getGitBranchOrTags(p.buildEnv, p.branchName)


    sh """
    curl -XPOST \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{
        \"content\": \"Deployment is FAILED : ${env.BUILD_URL}\",
        "embeds":[{
            \"title\": \"Repository name\",
            \"description\": \"${p.repositoryName}\",
            \"color\": \"16711680\",
            \"fields\": [
                {
                    \"name\": \"PR Title\", 
                    \"value\": \"${gitMessage}\"
                },
                {
                    \"name\": \"Tags\", 
                    \"value\": \"${gitBranch}\"
                },
                {
                    \"name\": \"Error\", 
                    \"value\": \"${errorMessage}\"
                }
            ]}
        ]}' \
    "https://discord.com/api/webhooks/${discordToken}"
    """
}


def notify(GlobalVars p, message) {
    String discordToken = getDiscordToken(p.buildEnv, p.namespaceName)
    String color = ""
    String content = ""
    if (message == "abort") {
        color = "16711680"
        content = "Beta promotion has been aborted"
    } else if (message == "success") {
        color = "65280"
        content = "Deployment has been successful"
    } else if (message == "beta") {
        color = "16705372"
        content = "Beta is ready to testing. Click here to release beta deployment to public : ${env.JOB_URL}"
    }


    sh """
    curl -XPOST \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{
        \"content\": \"${content}\",
        "embeds":[{
            \"title\": \"Repository name\",
            \"description\": \"${p.repositoryName}\",
            \"color\": \"${color}\",
            \"fields\": [
                {
                    \"name\": \"Tags\", 
                    \"value\": \"${p.branchName}\"
                }
            ]}
        ]}' \
    "https://discord.com/api/webhooks/${discordToken}"
    """
}