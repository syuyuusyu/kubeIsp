package ind.syu.kubeIsp.controller

import ind.syu.kubeIsp.repository.PlatformRepository
import ind.syu.kubeIsp.repository.UserRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/activiti")
class ActivitiController(
        val userRepository: UserRepository,
        val platformRepository: PlatformRepository
) {
    @Transactional
    @GetMapping("/userPlatformAccess/{username}/{apply}")
    fun list(@PathVariable username: String, @PathVariable apply: String): Map<String,Any?> {
        val result = HashMap<String,Any?>()
        var user = userRepository.findByField("user_name",username).get()

        val isApply = if (apply=="apply") true else false
        val plist = platformRepository.findAll()
                .filter { isApply && !it.users.contains(user) }
                .map { ActForm(it.name!!,it.code!!,"check",true,"false") }
        var message =""
        var unnecessary = false
        if(plist.size==0){
            unnecessary=true;
            if(isApply){
                message="已经拥有所有平台的访问权限,无需申请!"
            }else {
                message="没有任何平台权限,无法注销!"
            }
        }
        result["message"]=message
        result["unnecessary"]=unnecessary
        result["nextForm"] = plist
        return result
    }
}

data class ActForm(
        val label:String,
        val key:String,
        val type:String,
        val editable:Boolean,
        val value:String
)