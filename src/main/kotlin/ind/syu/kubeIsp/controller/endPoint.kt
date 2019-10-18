package ind.syu.kubeIsp.controller

import org.springframework.context.support.beans
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono


fun  endPoints() = beans {
    bean {
        router {
            GET("/test") { ok().contentType(MediaType.TEXT_PLAIN).body(Mono.just("sdsdsdsd")) }
        }
    }
}


