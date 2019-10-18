package ind.syu.kubeIsp.controller


import ind.syu.kubeIsp.service.AuthorService
import ind.syu.kubeIsp.service.PodService

import io.kubernetes.client.*
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1Pod

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import java.time.Duration

import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.KubeConfig
import org.springframework.beans.factory.annotation.Value
import java.io.FileReader
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.reactive.function.BodyInserters
import java.util.*
import com.fasterxml.jackson.databind.ObjectMapper
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.models.V1Status
import org.springframework.util.StringUtils
import reactor.core.publisher.Signal.subscribe
import reactor.core.publisher.SynchronousSink
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


@RestController
@RequestMapping("/kube/Pod")
class PodController(
        val kubeApi: CoreV1Api,
        val protoClient: ProtoClient,
        var authorService: AuthorService,
        val podService: PodService,
        val apiClient:ApiClient

) {

    @Value("\${kube-isp.kubeConfigPath}")
    lateinit var kubeConfigPath: String

    companion object {
        private val log: Logger = LoggerFactory.getLogger(PodController::class.java)
    }

    @GetMapping("/{ns}")
    fun list(@PathVariable ns: String): Flux<Map<String, Any?>> {
        //val list = kubeApi.listPodForAllNamespaces(null, null, null, null, null, "true", null, null,null)

        var labelSelector = ""
        val list = podService.pods(arrayOf(ns).asList())
        return Flux.fromIterable(list)
    }

    @GetMapping(path = ["/log/{ns}/{pname}/{cname}"], produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    fun log(@PathVariable ns: String, @PathVariable pname: String, @PathVariable cname: String): Flux<String> {

        val firstlog = kubeApi.readNamespacedPodLog(pname, ns, if (cname == "undefined") null else cname, false,
                Int.MAX_VALUE, null, false, Int.MAX_VALUE, 100, false)

        return Flux.interval(Duration.ofSeconds(2))
                .map {
                    val clog = kubeApi.readNamespacedPodLog(pname, ns, if (cname == "undefined") null else cname, false,
                            null, null, false, 2, 10, false)
                    if (it == 0L) firstlog else if (StringUtils.isEmpty(clog)) "" else clog
                }
    }

    @GetMapping("/{ns}/{podName}")
    fun podJson(@PathVariable ns: String, @PathVariable podName: String): V1Pod {
        val pod = kubeApi.readNamespacedPod(podName, ns, "true", false, false)
        pod.metadata.creationTimestamp = null
        pod.status = null
        return pod;

    }

    @DeleteMapping("/{ns}/{podName}")
    fun delete(@PathVariable ns: String, @PathVariable podName: String): Map<String, Any> {
        val result = HashMap<String, Any>()
        try {
            kubeApi.deleteNamespacedPodWithHttpInfo(podName, ns, "false", null, null, null, null, null)
            result["success"] = true
            result["msg"] = "删除成功"
            var st = V1Status()

        } catch (e: ApiException) {
            e.printStackTrace()
            KubeController.errorMsg(e, result)
        } catch (ex: Exception) {
            ex.printStackTrace()
            result["success"] = false
        }
        return result;
    }

    @PutMapping()
    fun update( @RequestBody pod: V1Pod): Map<String, Any> {
        val result = HashMap<String, Any>()
        try {
            val client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(FileReader(kubeConfigPath)))
                    .setOverridePatchFormat(V1Patch.PATCH_FORMAT_JSON_MERGE_PATCH).build()
            client.setDebugging(true)
            CoreV1Api(client).replaceNamespacedPodWithHttpInfo(pod.metadata.name, pod.metadata.namespace, pod,
                    "true", null, null)
            result["success"] = true
            result["msg"] = "更新成功"
        } catch (e: ApiException) {
            e.printStackTrace()
            KubeController.errorMsg(e, result)
        }
        return result;
    }

    @PostMapping("/{ns}")
    fun createPod(@PathVariable ns: String, @RequestBody body: V1Pod): Map<String, Any> {
        val result = HashMap<String, Any>()
        try {
            kubeApi.createNamespacedPodWithHttpInfo(ns, body, "true", null, null)
            result["success"] = true
            result["msg"] = "更新成功"
        } catch (e: ApiException) {
            e.printStackTrace()
            KubeController.errorMsg(e, result)
        }
        return result;
    }
}