# s2i-jenkins

```
#oc import-image jenkins-2-rhel7:v3.7 --from=registry.access.redhat.com/openshift3/jenkins-2-rhel7:v3.7 --confirm
oc new-build registry.access.redhat.com/openshift3/jenkins-2-rhel7:v3.7~https://github.com/cvarjao/s2i-jenkins.git --name=jenkins

#save a coy of the template
oc get template -n openshift jenkins-ephemeral -o json > jenkins-ephemeral.json

oc process -f jenkins-ephemeral.json -p NAMESPACE=csnr-devops-lab-tools -p JENKINS_IMAGE_STREAM_TAG=jenkins:latest | oc create -f -
oc set resources deployment jenkins --limits=cpu=2000m,memory=2Gi --requests=cpu=1000m,memory=1Gi

```



#References:

https://github.com/jenkinsci/github-plugin/blob/master/src/main/java/com/cloudbees/jenkins/GitHubWebHook.java

