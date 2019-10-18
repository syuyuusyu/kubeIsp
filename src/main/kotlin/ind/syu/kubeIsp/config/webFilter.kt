package ind.syu.kubeIsp.config

import ind.syu.kubeIsp.entity.OperateResult
import ind.syu.kubeIsp.service.AuthorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono



@Component
class CorsFilter(
        val properties: KubeProperties,
        val authorService: AuthorService
) : WebFilter {
    companion object{
        private val log: Logger = LoggerFactory.getLogger(CorsFilter::class.java)
    }

    override fun filter(ctx: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = ctx.request.uri.path
        val method = ctx.request.methodValue
        log.info("CorsFilter filter path:{},method:{}",path,method)
        if (ctx != null) {
            ctx.response.headers.add("Access-Control-Allow-Origin", properties.allowOrigin)
            ctx.response.headers.add("Access-Control-Allow-Methods", properties.allowMethods)
            ctx.response.headers.add("Access-Control-Allow-Headers",properties.allowHeads )
            if (ctx.request.method == HttpMethod.OPTIONS) {
                ctx.response.headers.add("Access-Control-Max-Age", "1728000")
                ctx.response.statusCode = HttpStatus.NO_CONTENT
                return Mono.empty()
            } else {
                return autherFilter(ctx,chain)
            }
        } else {
            log.info("ServerWebExchange is null!!!! CorsFilter filter path:{},method:{}",path,method)
            return  autherFilter(ctx,chain)
        }
    }

    fun autherFilter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        log.info("AuthorFilter filter path:{}",path)

        for ( p in properties.ignorePath ){
            if(p.toRegex().containsMatchIn(path) ){
                return chain.filter(exchange)?: Mono.empty()
            }
        }
        var toekn= exchange.request.headers["access-token"]?.get(0)
        toekn?.let {
            if( !StringUtils.isEmpty(it) && authorService.verifyToken(it)){
                return chain.filter(exchange)?: Mono.empty()
            }
        }
        log.info("UNAUTHORIZED token:{}",toekn)
        exchange.response.setStatusCode(HttpStatus.UNAUTHORIZED)
        return  return Mono.empty()
    }
}

//@Component
class AuthorFilter(
        val properties: KubeProperties,
        val authorService: AuthorService
) : WebFilter{

    companion object{
        private val log: Logger = LoggerFactory.getLogger(AuthorFilter::class.java)
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        log.info("AuthorFilter filter path:{}",path)

        for ( p in properties.ignorePath ){
            if(p.toRegex().containsMatchIn(path) ){
                return chain.filter(exchange)?: Mono.empty()
            }

        }
        var toekn= exchange.request.headers["access_token"]?.get(0)
        toekn?.let {
            if( !StringUtils.isEmpty(it) && authorService.verifyToken(it)){
                return chain.filter(exchange)?: Mono.empty()
            }
        }
        log.info("UNAUTHORIZED")
        exchange.response.setStatusCode(HttpStatus.UNAUTHORIZED)
        return  return Mono.empty()
    }
}

fun actionOperation(successMsg: String = "成功", errorMsg:String="失败", operation:()->Unit ):OperateResult{
    val op = OperateResult()
    try{
        operation()
        op.success=true
        op.msg=successMsg
    }catch (e:Exception){
        e.printStackTrace()
        op.success=false
        op.msg=errorMsg
    }
    return op
}

fun createOperion(fn:()->Unit) = actionOperation("新建成功","新建失败",fn)
fun updateOperion(fn:()->Unit) = actionOperation("更新成功","更新失败",fn)
fun deleteOperion(fn:()->Unit) = actionOperation("删除成功","删除失败",fn)