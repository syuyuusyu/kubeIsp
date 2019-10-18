package ind.syu.kubeIsp

import ind.syu.kubeIsp.config.beans
import ind.syu.kubeIsp.controller.endPoints
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.GenericApplicationContext

@SpringBootApplication
open class KubeIspApplication{

	@Autowired
	fun addBean(ctx: GenericApplicationContext) = beans().initialize(ctx)

	@Autowired
	fun route(ctx: GenericApplicationContext) = endPoints().initialize(ctx)
}

fun main(args: Array<String>) {
	runApplication<KubeIspApplication>(*args)
}
