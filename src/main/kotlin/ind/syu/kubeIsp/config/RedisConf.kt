package ind.syu.kubeIsp.config

import ind.syu.kubeIsp.entity.User
import ind.syu.kubeIsp.utils.SpringUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.*
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.support.collections.RedisMap
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.stereotype.Service
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer


@Configuration
class RedisConf(
) {

    @Bean
    fun keyCommands(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveKeyCommands {
        return reactiveRedisConnectionFactory.reactiveConnection.keyCommands()
    }

    @Bean
    fun stringCommands(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveStringCommands {
        return reactiveRedisConnectionFactory.reactiveConnection.stringCommands()
    }

    @Bean
    fun reactiveRedisTemplateString(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, RedisSerializationContext.string())
    }


    @Bean
    fun redisContainer(redisConnectionFactory: RedisConnectionFactory): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(redisConnectionFactory)
        //container.addMessageListener(MessageListenerAdapter(RedisMessageSubscriber()), ChannelTopic("kknd"))
        return container
    }
}


class RedisMessageSubscriber : MessageListener {

    override fun onMessage(message: Message, pattern: ByteArray?) {
        println(String(pattern!!))
        println("Message received: " + message.toString())
    }
}

object StaticMap {
    var redisMap = HashMap<Class<*>, ReactiveRedisTemplate<String, *>>()
}

fun <T> redisTemplate(clzz: Class<T>): ReactiveRedisTemplate<String, T> {
    if (StaticMap.redisMap.containsKey(clzz)) {
        return StaticMap.redisMap[clzz] as ReactiveRedisTemplate<String, T>
    }
    var factory = SpringUtil.getBean("redisConnectionFactory") as ReactiveRedisConnectionFactory
    val keySerializer = StringRedisSerializer()
    val valueSerializer = Jackson2JsonRedisSerializer(clzz) as RedisSerializer<T>
    val builder = RedisSerializationContext.newSerializationContext<String, T>(keySerializer)
    val context = builder.value(valueSerializer).build()
    StaticMap.redisMap[clzz] = ReactiveRedisTemplate<String, T>(factory, context)
    return ReactiveRedisTemplate<String, T>(factory, context)
}
