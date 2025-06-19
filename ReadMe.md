# Jenkins Deployment Pipeline Shared Library

This repository provides a Jenkins Shared Library for deployment pipelines. It contains reusable pipeline steps, utilities, and best practices to standardize and simplify CI/CD processes across multiple Jenkins projects.

## Features

- Common pipeline stages (build, test, deploy)
- Utility functions for notifications, artifact management, and more
- Easy integration with Jenkinsfiles via `@Library`
- Promotes DRY (Don't Repeat Yourself) principles

## Usage

1. Reference the library in your Jenkinsfile:
    ```groovy
    @Library('jenkins-pipeline-library') _
    ```

2. Use the shared steps and utilities as needed in your pipeline scripts.

