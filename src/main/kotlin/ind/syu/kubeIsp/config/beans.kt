package ind.syu.kubeIsp.config

import ind.syu.kubeIsp.utils.SpringUtil
import io.jsonwebtoken.security.Keys
import io.kubernetes.client.ApiClient
import io.kubernetes.client.Configuration
import io.kubernetes.client.ProtoClient
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.KubeConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.beans
import java.io.FileReader
import javax.crypto.SecretKey


@ConfigurationProperties(prefix = "kube-isp")
class KubeProperties (
        val kubeConfigPath:String,
        val ignorePath:List<String>,
        val jwtkey:String,
        val allowMethods:String,
        val allowHeads:String,
        val allowOrigin:String
)


fun beans() = beans {


    bean<ApiClient>("apiClient"){
        val kubeConfigPath = ref<KubeProperties>().kubeConfigPath
        val client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(FileReader(kubeConfigPath))).build()
        Configuration.setDefaultApiClient(client)
        client.setDebugging(true)
        client
    }

    bean<ExtensionsV1beta1Api>("extensionApi"){
        val client = ref<ApiClient>()
        ExtensionsV1beta1Api(client)
    }

    bean<CoreV1Api>(name = "kubeApi" ) {
        val client = ref<ApiClient>()
        CoreV1Api(client)
    }

    bean<CoreV1Api>(name = "prototypeKubeApi",scope = BeanDefinitionDsl.Scope.PROTOTYPE ) {
        val kubeConfigPath = ref<KubeProperties>().kubeConfigPath
        val client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(FileReader(kubeConfigPath))).build()
        Configuration.setDefaultApiClient(client)
        client.setDebugging(true)
        println("new CoreV1Api")
        CoreV1Api(client)
    }



    bean<ProtoClient>("protoClient"){
        val client = ref<ApiClient>()
        ProtoClient(client)
    }
    bean<SecretKey>("secretKey") {
        var keystr=ref<KubeProperties>().jwtkey
        Keys.hmacShaKeyFor(keystr.toByteArray())
    }
}

fun newCoreV1Api(): CoreV1Api {
    var path = (SpringUtil.getBean("kube-isp-ind.syu.kubeIsp.config.KubeProperties") as KubeProperties).kubeConfigPath
    val client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(FileReader(path))).build()
    Configuration.setDefaultApiClient(client)
    client.setDebugging(true)
    println("new CoreV1Api")
    return CoreV1Api(client)
}

fun UpdateClient(): CoreV1Api {
    var path = (SpringUtil.getBean("kube-isp-ind.syu.kubeIsp.config.KubeProperties") as KubeProperties).kubeConfigPath
    val client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(FileReader(path)))
            .setOverridePatchFormat(V1Patch.PATCH_FORMAT_JSON_MERGE_PATCH).build()
    client.setDebugging(true)
    return CoreV1Api(client)
}