import hudson.model.*
import jenkins.model.Jenkins
import jenkins.security.s2m.AdminWhitelistRule
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.BranchSpec;

println 'Configuring JNLP agent protocols'
//https://github.com/samrocketman/jenkins-bootstrap-shared/blob/master/scripts/configure-jnlp-agent-protocols.groovy
Jenkins.instance.setAgentProtocols(['JNLP4-connect', 'Ping'] as Set<String>)
Jenkins.instance.save()

//https://github.com/samrocketman/jenkins-bootstrap-shared/blob/master/scripts/configure-csrf-protection.groovy
println 'Configuring CSRF protection'
Jenkins.instance.setCrumbIssuer(new hudson.security.csrf.DefaultCrumbIssuer(true))
Jenkins.instance.save()

println 'Configuring Slave to Master Access Control'
//https://github.com/samrocketman/jenkins-bootstrap-shared/blob/master/scripts/security-disable-agent-master.groovy
//https://wiki.jenkins.io/display/JENKINS/Slave+To+Master+Access+Control
Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)
Jenkins.instance.save()


println 'Creating/Updating \'github-webhook\' job'
def ghwhJobName='github-webhook';
def ghwhJob=Jenkins.instance.getItem(ghwhJobName);
def ghwhAction='update'
if (ghwhJob==null){
  ghwhAction='add'
  ghwhJob = new WorkflowJob(Jenkins.instance, ghwhJobName);
}

def ghwhJobScm = new GitSCM("https://github.com/cvarjao/openshift-jenkins-tools.git");
ghwhJobScm.branches = [new BranchSpec("*/master")];

ghwhJob.definition = new CpsScmFlowDefinition(ghwhJobScm, "github-webhook/Jenkinsfile");
ghwhJob.definition.setLightweight(true);


def payloadParameter = new StringParameterDefinition('payload', '{}', 'Github webhook payload')
def jobParameters = ghwhJob.getProperty(ParametersDefinitionProperty.class)

if (jobParameters == null) {
    def newArrList = new ArrayList<ParameterDefinition>(1)
    newArrList.add(payloadParameter)
    def newParamDef = new ParametersDefinitionProperty(newArrList)
    ghwhJob.addProperty(newParamDef)
}
else {
    // Parameters exist! We should check if this one exists already!
    if (jobParameters.parameterDefinitions.find({ it.name == 'payload' }) == null) {
        jobParameters.parameterDefinitions.add(payloadParameter)
    }
}
    
if ('add'.equals(ghwhAction)){
  Jenkins.instance.add(ghwhJob, ghwhJob.name);
  println 'job \'github-webhook\' has been created'
}else{
  ghwhJob.save()
  println 'job \'github-webhook\' has been updated'
}

def jenkins = Jenkins.getInstance()
User u = User.get("github-webhook")
println "username:${u.getId()}"
println "\'github-webhook\' API token:${u.getProperty(jenkins.security.ApiTokenProperty.class).getApiTokenInsecure()}"

jenkins.getAuthorizationStrategy().add(Jenkins.READ, "github-webhook")
jenkins.getAuthorizationStrategy().add(Item.BUILD, "github-webhook")
jenkins.getAuthorizationStrategy().add(Item.DISCOVER, "github-webhook")
jenkins.getAuthorizationStrategy().add(Item.READ, "github-webhook")


println 'Approving script signatures'
def sa = org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval.get();
def signatures=new XmlSlurper().parseText('''
<signature>
    <string>method hudson.model.AbstractItem updateByXml javax.xml.transform.stream.StreamSource</string>
    <string>method hudson.model.ItemGroup getItem java.lang.String</string>
    <string>method hudson.plugins.git.GitSCM getBranches</string>
    <string>method hudson.plugins.git.GitSCM getRepositories</string>
    <string>method hudson.plugins.git.GitSCM getUserRemoteConfigs</string>
    <string>method hudson.plugins.git.GitSCMBackwardCompatibility getExtensions</string>
    <string>method hudson.scm.SCM getBrowser</string>
    <string>method java.io.BufferedReader readLine</string>
    <string>method java.lang.AutoCloseable close</string>
    <string>method java.lang.String getBytes java.nio.charset.Charset</string>
    <string>method jenkins.model.Jenkins createProject java.lang.Class java.lang.String</string>
    <string>method jenkins.model.ModifiableTopLevelItemGroup createProjectFromXML java.lang.String java.io.InputStream</string>
    <string>new java.io.BufferedReader java.io.Reader</string>
    <string>new java.io.ByteArrayInputStream byte[]</string>
    <string>new javax.xml.transform.stream.StreamSource java.io.InputStream</string>
    <string>staticField java.nio.charset.StandardCharsets UTF_8</string>
    <string>staticMethod jenkins.model.Jenkins getInstance</string>
</signature>''');

signatures.string.each {
  sa.approveSignature(it.text());
}
