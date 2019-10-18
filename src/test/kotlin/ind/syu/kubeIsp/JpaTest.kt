package ind.syu.kubeIsp

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import ind.syu.kubeIsp.config.KubeProperties
import ind.syu.kubeIsp.config.redisTemplate
import ind.syu.kubeIsp.entity.Orgnation
import ind.syu.kubeIsp.entity.User
import ind.syu.kubeIsp.repository.*
import ind.syu.kubeIsp.service.AuthorService
import ind.syu.kubeIsp.service.EntityService
import ind.syu.kubeIsp.service.MenuService
import ind.syu.kubeIsp.utils.SpringUtil
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.JacksonSerializer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import io.jsonwebtoken.security.Keys
import io.kubernetes.client.JSON
import io.kubernetes.client.ProtoClient
import io.kubernetes.client.proto.V1
import io.kubernetes.client.proto.V1beta1Apiextensions
import io.kubernetes.client.proto.V1beta1Extensions
import org.hibernate.EntityMode
import org.hibernate.Session
import org.hibernate.cfg.ImprovedNamingStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import javax.crypto.SecretKey
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Signal.subscribe
import reactor.core.publisher.toMono
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


@SpringBootTest
class JpaTest {

    @Autowired
    lateinit var userRepository:UserRepository

    @Autowired
    lateinit var secretKey: SecretKey


    @Autowired
    lateinit var properties: KubeProperties

    @Autowired
    lateinit var orgRepository: OrgRepository

    @Autowired
    lateinit var entityService: EntityService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var tableConfRepository: TableConfRepository

    @Autowired
    lateinit var menuRepository: MenuRepository

    @Autowired
    lateinit var menuService: MenuService

    @Autowired
    lateinit var platformRepository: PlatformRepository


    @Autowired
    lateinit var reactiveRedisTemplateString : ReactiveRedisTemplate<String, String>

    @Autowired
    lateinit var  protoClient: ProtoClient


    @Value("\${kube-isp.envTest}")
    lateinit var envTest:String


    @Test
    fun test(){
        var user =User()
        user.name="管理员";
        user.idNumber="i900jdjas"
        user.userName="admin"
        user.phone="789876756"
        user.password="123456"
        userRepository.save(user)

    }

    @Test
    fun delete(){
        var user = userRepository.findById(1);

        userRepository.delete(user.get())
    }

    @Test
    fun test2(){
       for(path in properties.ignorePath){
           println(path)
           var r = path.toRegex().containsMatchIn("/aaaa/pod/hello")
           println(r)
       }
    }

    @Test
    fun test3(){
        //val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
        var user = userRepository.findById(1).get()
        user.phone="123467788"
        userRepository.save(user)
    }

    @Test
    fun test4(){
        var user = userRepository.findByField("userName","kknd")
        var orgnation = Orgnation()
        orgnation.hierarchy
        println(user.isPresent)
    }

    @Test
    @Transactional
    fun test5(){
        AuthorService.sessionNameThread.set("kknd")

        var org = Orgnation()
        org.name="集团公司3"
        org.parentId=1
        org.hierarchy=1



        orgRepository.save(org)
    }

    @Test
    @Transactional
    fun test6(){
        var s=orgRepository.findById(3).get()
        println(s.name)
        println(s.isLeaf)


    }

    @Test
    fun test7(){

        var list = protoClient.list<V1beta1Extensions.IngressList>(V1beta1Extensions.IngressList.newBuilder(),
                "/apis/extensions/v1beta1/namespaces/kube-isp/ingresses")

        var ing=protoClient.get<V1beta1Extensions.Ingress>(V1beta1Extensions.Ingress.newBuilder(),"/apis/extensions/v1beta1/namespaces/kube-isp/ingresses/kubia")
        var obj= list.`object`
        val jsonString = ""
        var s= JsonFormat.printer().print(obj)
        var ingstr = JsonFormat.printer().print(ing.`object`)
        println(s)
        println("-------")
        println(ingstr)
    }

    @Test
    fun test8(){
        println(envTest)
     var  count =  AtomicInteger(1);   // 1
        Flux.generate<String>{sink ->
            sink.next(count.get().toString() + " : " +  Date());   // 2
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (e:InterruptedException) {
                e.printStackTrace();
            }
            if (count.getAndIncrement() >= 5) {
                sink.complete();     // 3
            }
        }.subscribe(System.out::println);  // 4
    }

    @Test
    fun test9(){
        var redis= redisTemplate(ApprovalSresult::class.java)
        var r = ApprovalSresult("assigneeName","message","type")
        var a = redis.convertAndSend("chat",r)
        a.subscribe()


    }

    @Test
    fun test10(){
        var redis= redisTemplate(String::class.java)
        var a = redis.convertAndSend("chat","efef")
        a.subscribe()
    }

    @Test
    @Transactional
    fun test11(){
        var parnt= orgRepository.findById(1).get()
       var list = parnt.child
        println(list!!.size)
    }

    @Test
    @Transactional
    fun test12(){
        var parnt= platformRepository.findById(1).get()
        var list = parnt.operations
        println(list.size)
    }


}


data class ApprovalSresult (
    var assigneeName: String,
    var message: String,
    var type: String

)