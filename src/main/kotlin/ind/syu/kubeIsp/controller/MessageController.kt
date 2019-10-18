package ind.syu.kubeIsp.controller

import ind.syu.kubeIsp.config.RedisMessageSubscriber
import ind.syu.kubeIsp.service.AuthorService
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux



@RestController
@RequestMapping("/message")
class MessageController(
        val authorService: AuthorService,
        val redisContainer:RedisMessageListenerContainer
) {

    @GetMapping( produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    fun msg():Flux<String>{
        var user = authorService.currentUser()
        return Flux.create { sink->
            redisContainer.addMessageListener({message:Message, bytes:ByteArray? ->
                println(String(bytes!!))
                println(message.toString())
                sink.next(message.toString())

            }, listOf(ChannelTopic(user.userName!!)))
        }

    }
}


data class UserMessage(
        val username:String,
        val msg:String,
        var type:String
);