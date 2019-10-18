package ind.syu.kubeIsp.controller


import ind.syu.kubeIsp.config.createOperion
import ind.syu.kubeIsp.config.redisTemplate
import ind.syu.kubeIsp.entity.OperateResult
import ind.syu.kubeIsp.entity.User
import ind.syu.kubeIsp.repository.UserRepository
import ind.syu.kubeIsp.service.AuthorService
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/sys/author")
class AuthorController(
        val authorService: AuthorService,
        val userRepository: UserRepository
       // val redisTemplateFun : (Class<User>)-> ReactiveRedisTemplate<String, User>
) {
    @PostMapping("/login")
    fun login(@RequestBody map:Map<String,String>): Mono<Map<String,Any>>{
        var redis = redisTemplate(User::class.java)
        var result = HashMap<String,Any>()
        try{
            val id=map["identify"]
            val pwd=map["password"]
            var userOption = authorService.login(id!!,pwd!!)
            if(userOption.isPresent){
                var user=userOption.get()
                //print(user.roles.size)
                result.put("success",true)
                result["user"]=user
                var token = authorService.createToken(user)
                var setresult = redis.opsForValue().set(token,user)
                setresult.subscribe()
                //println(  setresult.block())
                result["token"]= token

            }else{
                result["success"]=false
            }
        }catch (e:Exception){
            e.printStackTrace()
            result["success"]=false
        }

        return Mono.just(result)
    }

    @PostMapping("/user")
    fun create(@RequestBody user: User) :OperateResult = createOperion { userRepository.save(user) }

}