package ind.syu.kubeIsp.repository

import ind.syu.kubeIsp.entity.*
import ind.syu.kubeIsp.entity.Dictionary
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

interface UserRepository : PagingAndSortingRepository<User,Int>{

    fun findByField(field:String,id:String):Optional<User>
}

class UserRepositoryImpl{

    @PersistenceContext
    lateinit var  em: EntityManager

    fun findByField(field:String, id:String): Optional<User> {
        var id=id
        if(field=="idNumber") {
             id = encode(id)!!
        }
        val hql= " from  User where $field='$id'"
        //var user= em.createQuery(hql,User::class.java).singleResult
        var list=em.createQuery(hql,User::class.java).resultList
        if(list.size==1){
            return Optional.of(list[0])
        }else{
            return Optional.empty()
        }
    }
}

interface RoleRepository :PagingAndSortingRepository<Role,Int>{}


interface OrgRepository : PagingAndSortingRepository<Orgnation,Int>{}

interface MenuRepository: CrudRepository<Menu,Int>

interface PlatformRepository:CrudRepository<Platform,Int>{
    fun findByManagerRoleId(id:Int) : Platform
    fun findByCode(code:String) : List<Platform>
}

interface PlatformOperationRepository:PagingAndSortingRepository<PlatformOperation,Int>{

}



