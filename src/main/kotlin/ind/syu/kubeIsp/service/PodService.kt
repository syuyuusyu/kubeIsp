package ind.syu.kubeIsp.service

import ind.syu.kubeIsp.utils.SpringUtil
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1Pod
import io.kubernetes.client.models.V1PodList
import org.bouncycastle.asn1.x500.style.RFC4519Style.name
import org.springframework.stereotype.Service

@Service
class PodService(
        val  kubeApi : CoreV1Api,
        var authorService: AuthorService
) {
    fun pods(nameSpaces:List<String>,labelSelector:String=""):List<Map<String,Any?>>{

        return allPods(nameSpaces,labelSelector).map {
            val map = HashMap<String,Any?>()
            map["name"] = it.metadata.name
            map["ns"] = it.metadata.namespace
            map["labels"] = it.metadata.labels
            map["status"] = it.status.phase
            it.status.containerStatuses.forEach { state->
                state.state.running
            }
            map["uid"] = it.metadata.uid
            map["containers"] = it.spec.containers.map { con->
                val c= HashMap<String,Any?>()
                c["name"] = con.name
                c["image"]= con.image
                c
            }
            map
        }
    }

    fun allPods( nameSpaces:List<String>,labelSelector:String=""):List<V1Pod> {
        var reuslt= ArrayList<V1Pod>()
        nameSpaces.parallelStream().forEach {
            var a= kubeApi.listNamespacedPod(it,"true",null,null,null,
                    null,null,null,null) .items
            reuslt.addAll(a)
        }
        return reuslt
    }

}