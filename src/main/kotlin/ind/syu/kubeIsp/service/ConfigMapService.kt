package ind.syu.kubeIsp.service

import com.fasterxml.jackson.databind.ObjectMapper
import ind.syu.kubeIsp.config.UpdateClient
import ind.syu.kubeIsp.controller.KubeController
import ind.syu.kubeIsp.entity.CmContext
import io.kubernetes.client.ApiException
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1ConfigMap
import io.kubernetes.client.models.V1ConfigMapBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class ConfigMapService(
        val kubeApi: CoreV1Api
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConfigMapService::class.java)
    }

    fun create(context: CmContext) {
        var cm = V1ConfigMapBuilder()
                .withNewMetadata().withName(context.name).withNamespace(context.ns).endMetadata()
                .withData(mapOf(context.key to context.context))
                .build()
        kubeApi.createNamespacedConfigMap(context.ns,cm,"true",null,null)
    }

    fun update(context: CmContext) {
        var cm = kubeApi.readNamespacedConfigMap(context.name,context.ns,null,null,null)
        cm.data[context.key] = context.context
        update(cm)
    }
    fun read(ns: String, name: String): V1ConfigMap {
        var a = kubeApi.readNamespacedConfigMap(name, ns, "true", null, null)
        a.metadata.creationTimestamp = null
        return a
    }
    fun list(ns: String): List<Any> {
        return kubeApi.listNamespacedConfigMap(ns, "true",
                null, null, null, null, null, null, false)
                .items
                .map {
                    var map = HashMap<String, Any?>()
                    map["name"] = it.metadata.name
                    map["ns"] = it.metadata.namespace
                    map["labels"] = it.metadata.labels
                    map["uid"] = it.metadata.uid
                    map["data"] = it.data
                    map
                }
    }
    fun update(configMap: V1ConfigMap) {
        UpdateClient().replaceNamespacedConfigMapWithHttpInfo(configMap.metadata.name,
                configMap.metadata.namespace, configMap,
                "true", null, null)
    }
    fun delete(ns: String, name: String) {
        kubeApi.deleteNamespacedConfigMapWithHttpInfo(name, ns, "true", null, null, null, null, null)
    }
    fun create(ns: String,configMap: V1ConfigMap) {
        kubeApi.createNamespacedConfigMap(ns,configMap,"true",null,null)
    }
    fun deleteData(ns: String, name: String,key:String){
        var cm = read(ns,name)
        cm.data[key] = null
        update(cm)
    }
}