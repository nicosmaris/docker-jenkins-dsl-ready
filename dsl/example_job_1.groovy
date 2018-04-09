[
  [gitlabgroup: 'ember', repo: 'addon1', verb: 'test'],
].each { Map config ->

  job("${config.verb}___${config.repo}") {
    description "Main job for ${config.gitlabgroup}/${config.repo}"

      concurrentBuild()
      label('docker')
      environmentVariables {
        keepBuildVariables(true)
          keepSystemVariables(true)
      }
    wrappers {
      preBuildCleanup()
        injectPasswords {
          injectGlobalPasswords()
        }
      maskPasswords()
        colorizeOutput()
    }
    logRotator {
      artifactNumToKeep(10)
        numToKeep(10)
    }
    properties {
      gitLabConnection {
        gitLabConnection('gitlab.x.prv')
      }
    }
    triggers {
      gitlabPush {
        buildOnMergeRequestEvents(true)
          buildOnPushEvents(false)
          enableCiSkip(true)
          setBuildDescription(true)
          rebuildOpenMergeRequest('both')
      }
      gitlab {
        secretToken('ad2f98c583784ff80514fd7ce1b6a0bf')
      }
    }
    scm {
      git {
        extensions {
          choosingStrategy {
            inverse()
          }
          mergeOptions {
            remote('origin')
              branch('${gitlabTargetBranch}')
          }
          cleanAfterCheckout()
            wipeOutWorkspace()
            disableRemotePoll()
            cloneOptions {
              shallow()
                timeout(10)
            }
          localBranch('origin/${gitlabSourceBranch}')
            relativeTargetDirectory('localrepo')
        }
        remote {
          name('origin')
            url ("git@gitlab.x.prv:${config.gitlabgroup}/${config.repo}.git")
            credentials ("jenkins")
        }
        browser{
          gitLab('https://gitlab.x.prv/ember/addon1', '10.3')
        }
      }
    }
    steps {
      shell('bash jenkinsfile.sh')
    }
    publishers {
      publishHtml {
        report('test-result-brakeman.html') {
          reportName('Brakeman')
            keepAll()
            allowMissing()
        }
        report('coverage/index.html') {
          reportName('Ember coverage')
            keepAll()
            allowMissing()
        }
      }
      archiveJunit('test-results-*.xml') {
        allowEmptyResults()
          retainLongStdout()
          healthScaleFactor(1.0)
          testDataPublishers {
            publishTestStabilityData()
          }
      }
      gitLabCommitStatusPublisher {
        name 'jenkins'
          markUnstableAsSuccess false
      }
      gitLabMessagePublisher {
        onlyForFailure true
          replaceSuccessNote false
          replaceFailureNote false
          replaceAbortNote false
          successNoteText ''
          failureNoteText ''
          abortNoteText ''
      }
    }
  }
}
listView('managed') {
  filterBuildQueue()
    filterExecutors()
    jobs {
      name('managed')
        regex(/.+___.+/)
    }
  columns {
    status()
      weather()
      name()
      lastSuccess()
      lastFailure()
      lastDuration()
      buildButton()
  }
}
