# SQL Fingerprint Storage Plugin

![Build](https://ci.jenkins.io/job/Plugins/job/sql-fingerprint-storage-plugin/job/main/badge/icon)
[![Coverage](https://ci.jenkins.io/job/Plugins/job/sql-fingerprint-storage-plugin/job/main/badge/icon?status=${instructionCoverage}&subject=coverage&color=${colorInstructionCoverage})](https://ci.jenkins.io/job/Plugins/job/sql-fingerprint-storage-plugin/job/main)
[![LOC](https://ci.jenkins.io/job/Plugins/job/sql-fingerprint-storage-plugin/job/main/badge/icon?job=test&status=${lineOfCode}&subject=line%20of%20code&color=blue)](https://ci.jenkins.io/job/Plugins/job/sql-fingerprint-storage-plugin/job/main)
![Contributors](https://img.shields.io/github/contributors/jenkinsci/sql-fingerprint-storage-plugin.svg?color=blue)
![Gitter](https://badges.gitter.im/jenkinsci/external-fingerprint-storage.svg)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/sql-fingerprint-storage-plugin.svg?label=changelog)](https://github.com/jenkinsci/sql-fingerprint-storage-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/sql-fingerprint-storage.svg?color=blue)](https://plugins.jenkins.io/sql-fingerprint-storage)

## Introduction

The plugin allows users to configure SQL database for storing fingerprints.

This plugin was forked and extend [postgresql-fingerprint-storage-plugin](https://github.com/jenkinsci/postgresql-fingerprint-storage-plugin) to support MySQL, MariaDB, PostgreSQL, and SQL Server databases.

![image](docs/images/logo.png)

## Getting started

Once the plugin has been installed, you can configure the Database plugin
server details by following the steps below:

Ensure you have configured a global database

1.  Select `Manage Jenkins`
2.  Select `Configure System`

![image](docs/images/database_config.png)


## Contributing

Review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

