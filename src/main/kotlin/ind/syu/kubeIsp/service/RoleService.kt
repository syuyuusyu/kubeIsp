package ind.syu.kubeIsp.service

import ind.syu.kubeIsp.entity.Role
import ind.syu.kubeIsp.entity.User
import ind.syu.kubeIsp.repository.RoleRepository
import ind.syu.kubeIsp.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
        var userRepository: UserRepository,
        var roleRepository: RoleRepository
) {

    fun attachRolesToUser(userid:Int,roleIds:List<Int>): Boolean{
        try {
            var user=userRepository.findById(userid).get()
            user.roles=ArrayList()
            userRepository.save(user)

            var roles = roleRepository.findAllById(roleIds) as List<Role>
            roles.forEach { it.type = Role.RoleType.USER.name }
            user.roles=roles
            userRepository.save(user)
            return true
        }catch (e:Exception){
            e.printStackTrace()
            return false;
        }
    }

    fun attachUsersToRole(roleId:Int,userIds:List<Int>):Boolean{
        try {
            var role=roleRepository.findById(roleId).get()
            role.users = ArrayList()
            roleRepository.save(role)

            role.users = userRepository.findAllById(userIds) as List<User>
            roleRepository.save(role)
            return true
        }catch (e:Exception){
            e.printStackTrace()
            return false;
        }
    }

    fun userKubeNameSpace():String{
        return "kube-isp"
    }

    fun attachRolesToMenu(){

    }

    fun attachMenusToRole(){

    }
}