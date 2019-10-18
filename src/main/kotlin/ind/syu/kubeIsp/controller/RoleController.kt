package ind.syu.kubeIsp.controller

import ind.syu.kubeIsp.entity.Role
import ind.syu.kubeIsp.repository.RoleRepository
import ind.syu.kubeIsp.service.RoleService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*



@RestController
@RequestMapping("/sys/role")
class RoleController (
        val roleRepository: RoleRepository,
        var roleService: RoleService
){

    @PostMapping
    @Transactional
    fun create(@RequestBody role:Role): Map<String,Any>{
        var result = HashMap<String,Any>()
        roleRepository.save(role)
        return result
    }

    @GetMapping("/addToUser/{userId}")
    @Transactional
    fun addToUser(@PathVariable userId:Int,@RequestParam roleIds:List<Int>):Map<String,Any>{
        var result = HashMap<String,Any>()
        var flag =roleService.attachRolesToUser(userId,roleIds)
        result["usccess"]=flag
        return result
    }

    @GetMapping("/addToRole/{roleId}")
    @Transactional
    fun addToRole(@PathVariable roleId:Int,@RequestParam userIds:List<Int>):Map<String,Any>{
        var result = HashMap<String,Any>()
        var flag =roleService.attachUsersToRole(roleId,userIds)
        result["usccess"]=flag
        return result
    }
}