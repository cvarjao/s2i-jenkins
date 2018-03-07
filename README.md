# s2i-jenkins


#oc import-image jenkins-2-rhel7:v3.7 --from=registry.access.redhat.com/openshift3/jenkins-2-rhel7:v3.7 --confirm

oc new-build registry.access.redhat.com/openshift3/jenkins-2-rhel7:v3.7~https://github.com/cvarjao/s2i-jenkins.git --name=jenkins

#save a coy of the template
oc get template -n openshift jenkins-ephemeral -o json > jenkins-ephemeral.json

oc process -f jenkins-ephemeral.json -p NAMESPACE=csnr-devops-lab-tools -p JENKINS_IMAGE_STREAM_TAG=jenkins-2-rhel7:v3.7 | oc create -f -
