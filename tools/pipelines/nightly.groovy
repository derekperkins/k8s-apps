def run(helm_home, namespace_prefix, kubernetes_domain) {
  stage("Add repo") {
    withEnv(["HELM_HOME=${helm_home}"]) {
      sh('./helm repo add mirantisworkloads https://mirantisworkloads.storage.googleapis.com/')
    }
  }

  stage("Dependencies") {
    sh("go get github.com/kubernetes/apimachinery/pkg/util/yaml")
  }

  stage("Run tests") {
    try {
      withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
        withEnv([
          'HELM_HOME=' + helm_home,
          'HELM_CMD=' + pwd() + '/helm',
          'KUBECTL_CMD=' + pwd() + '/kubectl',
        ]) {
          sh("set -o pipefail; exec 3>&1; go test -v -timeout 90m -args --charts --kubernetes-domain ${kubernetes_domain} --image-repo mirantisworkloads --exclude tweepub,tweepub-fake,tweetics,tweeviz,tweeviz_api,tweeviz_ui,kibana,logstash,bus-floating-data,monocular,rollout --prefix ${namespace_prefix}- 2>&1 3>&- | tee /dev/fd/3 | ./go-junit-report > report.xml 3>&-")
        }
      }
    } finally {
      junit('report.xml')
    }
  }
}

return this;
