package ind.syu.kubeIsp.service

import afu.org.checkerframework.checker.oigj.qual.O
import ind.syu.kubeIsp.entity.OperateResult
import ind.syu.kubeIsp.entity.Platform
import ind.syu.kubeIsp.entity.PlatformOperation
import ind.syu.kubeIsp.entity.Role
import ind.syu.kubeIsp.repository.PlatformRepository
import ind.syu.kubeIsp.repository.RoleRepository
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable

@Service
class PlatformService(
        val authorService: AuthorService,
        val platformRepository: PlatformRepository,
        val roleRepository: RoleRepository
) {

    @Transactional
    fun create(platform: Platform):OperateResult  {
        val op=OperateResult()
        try {
            val user = authorService.currentUser()
            val role = Role()
            role.code = platform.code+"_manager"
            role.name = platform.name+"管理员"
            role.type = Role.RoleType.PLATFORMMANAGER.name
            role.users = arrayListOf(user)
            roleRepository.save(role)

            platform.kubeNamespace = platform.code+"-ns"
            platform.managerRoleId=role.id
            platformRepository.save(platform)
            op.success=true
        } catch (e: Exception) {
            e.printStackTrace()
            op.success=false
            op.msg=e.localizedMessage
        }
        return op
    }

    @Transactional
    fun list():List<Platform>{
        val user = authorService.currentUser()
        val roles = user.roles
        return platformRepository.findAll().map {
            if (roles.map{role -> role.id  }.contains(it.managerRoleId)){
                it.editable = true
            }
            it
        }
    }

    @Transactional
    fun operations(id: Int):List<PlatformOperation>{
        val platform = platformRepository.findById(id).get()
        val list = platform.operations
        list.size
        return list
    }

    fun delete(id:Int):OperateResult{
        val op=OperateResult()
        try {
            platformRepository.deleteById(id)
            op.success=true
        } catch (e: Exception) {
            e.printStackTrace()
            op.success=false
            op.msg=e.localizedMessage
        }
        return op
    }

    fun update(platform: Platform): OperateResult{
        val op=OperateResult()
        try {
            platformRepository.save(platform)
            op.success=true
        } catch (e: Exception) {
            e.printStackTrace()
            op.success=false
            op.msg=e.localizedMessage
        }
        return op
    }
}