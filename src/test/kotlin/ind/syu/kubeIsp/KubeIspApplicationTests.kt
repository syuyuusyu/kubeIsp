package ind.syu.kubeIsp

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import io.kubernetes.client.util.Reflect.getItems
import io.kubernetes.client.models.V1Pod
import io.kubernetes.client.models.V1PodList
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.Configuration.setDefaultApiClient
import java.io.FileReader
import io.kubernetes.client.util.KubeConfig
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.ApiClient
import io.kubernetes.client.Configuration
import org.springframework.beans.factory.annotation.Autowired
import java.io.IOException
import com.google.common.io.ByteStreams
import io.kubernetes.client.Exec
import com.sun.tools.javac.tree.TreeInfo.args
import io.kubernetes.client.util.Config
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.BasicParser;
import org.springframework.boot.runApplication
import java.util.*
import java.util.concurrent.TimeUnit
import io.kubernetes.client.util.Reflect.getItems
import io.kubernetes.client.PodLogs
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.models.V1ConfigMapBuilder
import reactor.core.publisher.Flux
import reactor.test.StepVerifier


@SpringBootTest
class KubeIspApplicationTests {

	@Autowired
	lateinit var kubeApi:CoreV1Api

	@Autowired
	lateinit var apiClient: ApiClient

	@Autowired
	lateinit var extensionApi:ExtensionsV1beta1Api

	@Test
	fun contextLoads() {

		val list = kubeApi.listPodForAllNamespaces(null, null, null, null, null, null, null, null)
		for (item in list.items) {
			println(item.metadata.name)
		}
	}

	@Test
	fun kknd() {
		var podlog = PodLogs(apiClient)
		var input = podlog.streamNamespacedPodLog("isp-kube","el-deployment-c6447876-n6qxk","server")
		var flux=Flux.generate<String>{ sink->
			var readCount= input.available()
			if(readCount>0){
				var b =ByteArray(readCount)
				input.read(b)
				sink.next(String(b))
			}

		}
	}

	@Test
	fun execExample(){
		val options = Options()
		options.addOption(Option("p", "pod", true, "The name of the pod"))
		options.addOption(Option("n", "namespace", true, "The namespace of the pod"))

		val parser = BasicParser()
		var args: Array<String> = arrayOf( "echo foo")
		val cmd = parser.parse(options, args)

		val podName = cmd.getOptionValue("p", "alpine")
		val namespace = cmd.getOptionValue("n", "kube-isp")
		val commands = ArrayList<String>()

		args = cmd.getArgs()
		for (i in 0 until args.size) {
			commands.add(args[i])
		}


		val client = Config.defaultClient()
		client.httpClient.setReadTimeout(0L, TimeUnit.MILLISECONDS)
		Configuration.setDefaultApiClient(client)

		val exec = Exec()
		val tty = System.console() != null
		val proc = exec.exec("kube-isp", "alpine", arrayOf("sh"), true, true);

		val `in` = Thread(
				Runnable {
					try {
						ByteStreams.copy(System.`in`, proc.outputStream)
					} catch (ex: IOException) {
						ex.printStackTrace()
					}
				})
		`in`.start()

		val out = Thread(
				Runnable {
					try {
						ByteStreams.copy(proc.inputStream, System.out)
					} catch (ex: IOException) {
						ex.printStackTrace()
					}
				})
		val error = Thread(
				Runnable {
					try {
						ByteStreams.copy(proc.errorStream, System.out)
					} catch (ex: Exception) {
						ex.printStackTrace()
					}
				})
		out.start()
		error.start()
		Thread.sleep(2000);
		proc.waitFor()
		out.join()
		error.join()
//
		proc.destroy()

//
		System.exit(proc.exitValue())
	}

	@Test
	fun log(){
		var podlog = PodLogs(apiClient)
		var input = podlog.streamNamespacedPodLog("kube-isp","el-deployment-c6447876-n6qxk","server")
		println(22222)
		var flux=Flux.generate<String>{ sink->
			var readCount= input.available()
			println(readCount)
			if(readCount>0) {
				var b =ByteArray(readCount)
				input.read(b)
				sink.next(String(b))
			}

		}
		flux.subscribe(::println)
	}
}
