#!/usr/bin/env groovy

def runStage(){
    
    stage('Checkout') {
        echo "stage checkout"
    }

    stage('Build Image') {
        echo "stage build image"
    }

    stage('Deploy') {
        echo "stage deploy"
    }
}