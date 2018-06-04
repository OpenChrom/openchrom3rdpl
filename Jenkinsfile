pipeline {
    agent any
	tools { 
        jdk 'JDK8'
        maven 'MAVEN3' 
    }
    stages {
    	stage ('checkout') {
        	steps {
				checkout scm
        	}
        }

		stage('build') {
			steps {
				sh 'mvn -B -Dmaven.repo.local=.repository -f openchrom/cbi/net.openchrom.thirdpartylibraries.cbi/pom.xml install'
			}
		}
		stage('deploy') {
			when { branch 'develop' }
			steps {
				withCredentials([string(credentialsId: 'DEPLOY_HOST', variable: 'DEPLOY_HOST')]) {
				    sh 'scp -r openchrom/sites/net.openchrom.thirdpartylibraries.updateSite/target/site/* '+"${DEPLOY_HOST}community/latest/3rdparty"
				}
			}
		}
    }
    post {
        failure {
            emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
		         replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
		         to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
		                                 [$class: 'RequesterRecipientProvider']]))
        }
        success {
            cleanWs notFailBuild: true
        }
    }
}
