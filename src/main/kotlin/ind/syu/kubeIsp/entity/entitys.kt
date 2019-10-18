package ind.syu.kubeIsp.entity


import com.fasterxml.jackson.annotation.JsonIgnore
import ind.syu.kubeIsp.repository.MenuRepository
import ind.syu.kubeIsp.repository.OrgRepository
import ind.syu.kubeIsp.utils.HmacSHA256
import ind.syu.kubeIsp.utils.SpringUtil
import org.hibernate.annotations.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.GenerationType
import javax.persistence.GeneratedValue
import kotlin.collections.ArrayList


fun encode(value:String?) = value?.let{ String( Base64.getEncoder().encode(it.toByteArray())) }
fun decode(value:String?) = value?.let{ String( Base64.getDecoder().decode(it.toByteArray())) }

class OperateResult(){
    var success:Boolean=false
    var msg:String=""
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractJpaPersistable:Serializable {

    companion object {
        private val serialVersionUID = -5554308939380869754L
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column
    @CreationTimestamp
    @JsonIgnore
    var createTime:Date?=null
    @Column(length = 30)
    @CreatedBy
    @JsonIgnore
    var createBy:String?=null
    @Column
    @UpdateTimestamp
    @JsonIgnore
    var updateTime:Date?=null
    @Column(length = 30)
    @JsonIgnore
    @LastModifiedBy
    var updateBy:String?=null
    @Column(length = 1)
    @JsonIgnore
    var deleteFlag:String="1"

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (this === other) return true
        if (javaClass != ProxyUtils.getUserClass(other)) return false
        other as AbstractJpaPersistable
        return if (null == id) false else this.id == other.id
    }
    override fun toString() = "Entity of type ${this.javaClass.name} with id: $id"
}




@Entity
@Table(name="sys_user")
@Where(clause = "delete_flag = '1'")
@SQLDelete(sql = "UPDATE sys_user SET delete_flag = '0' WHERE id = ?", check = ResultCheckStyle.COUNT)
class User:AbstractJpaPersistable(){
    @Column(length = 30)
    var userName:String?=null
    @Column(length = 30)
    var name:String?=null
    @Column
    var password:String?=null
        set(value) {
            println(value + " password")
            field= value?.let { HmacSHA256.generate(it,salt!!) }?:value
        }
    @Column(length = 12)
    @JsonIgnore
    var salt:String?=null
    @Column(length = 20)
    var phone:String?=null
    @Column(length = 100)
    var idNumber: String?=null
        set(value) {
            field = encode(value)
        }
        get() = decode(field)
    @Column(length = 100)
    var email :String?=null

    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_role",
            joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")])
    var roles: List<Role> = ArrayList()

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_org_user",
            joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "org_id", referencedColumnName = "id")])
    var orgs: List<Orgnation> = ArrayList()

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_platform",
            joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "platform_id", referencedColumnName = "id")])
    var platforms: List<Platform> = ArrayList()

    fun vaildPwd(pwd:String) = password == HmacSHA256.generate(pwd,salt!!)

    init {
        salt=salt?:HmacSHA256.createSalt()
    }
}

@Entity
@Table(name="sys_role")
@Where(clause = "delete_flag = '1'")
@SQLDelete(sql = "UPDATE sys_role SET delete_flag = '0' WHERE id = ?", check = ResultCheckStyle.COUNT)
class Role:AbstractJpaPersistable(){
    enum class RoleType{
        USER,ORG,PLATFORMMANAGER
    }

    @Column(length = 30)
    var code:String?=null
    @Column(length = 50)
    var name:String?=null
    @Column(length = 100)
    var description:String?=null
    @Column
    var type:String?=null

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_role",
            joinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")])
    var users: List<User> = ArrayList()

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_org_role",
            joinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "org_id", referencedColumnName = "id")])
    var orgs: List<Orgnation> = ArrayList()

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_role_menu",
            joinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "menu_id", referencedColumnName = "id")])
    var menus: List<Menu> = ArrayList()
}

@Entity
@Table(name="sys_org")
@Where(clause = "delete_flag = '1'")
@SQLDelete(sql = "UPDATE sys_org SET delete_flag = '0' WHERE id = ?", check = ResultCheckStyle.COUNT)
class Orgnation: AbstractJpaPersistable(){

    @Transient
    var parentId:Int?=null
        set(value) {
            value?.let {
                var orgRepository = SpringUtil.getBean("orgRepository") as OrgRepository
                parent = orgRepository.findById(it).get()
            }
            field=value
        }

    @ManyToOne(cascade = [CascadeType.DETACH], fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @NotFound(action=NotFoundAction.IGNORE)
    var parent: Orgnation? = null

    @OneToMany(cascade= [CascadeType.ALL],fetch=FetchType.LAZY,mappedBy="parent")
    var child: List<Orgnation> = ArrayList()

    @Column(length = 50)
    var name:String?=null

    @Column
    var hierarchy:Int?=null

    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_org_role",
            joinColumns = [JoinColumn(name = "org_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")])
    var roles: List<Role> = ArrayList()

    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_org_user",
            joinColumns = [JoinColumn(name = "org_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")])
    var users: List<User> = ArrayList()

    val isLeaf:String
        get() {
            child?.size?.let {
                return if (it>0) "0" else "1"
            }
            return "1"
        }
}

@Entity
@Table(name = "sys_menu")
@Where(clause = "delete_flag = '1'")
@SQLDelete(sql = "UPDATE sys_menu SET delete_flag = '0' WHERE id = ?", check = ResultCheckStyle.COUNT)
class Menu : AbstractJpaPersistable() {

//    @Transient
//    var parentId:Int?=null
//        set(value) {
//            value?.let {
//                var menuRepository = SpringUtil.getBean("menuRepository") as MenuRepository
//                parent = menuRepository.findById(it).get()
//            }
//            field=value
//        }
//
//
//    @ManyToOne(cascade = [CascadeType.DETACH], fetch = FetchType.EAGER)
//    @JoinColumn(name = "parent_id")
//    @NotFound(action=NotFoundAction.IGNORE)
//    var parent: Menu? = null
//
//    @OneToMany(cascade= [CascadeType.ALL],fetch=FetchType.LAZY,mappedBy="parent")
//    var child: MutableList<Menu> = ArrayList()

    @Transient
    var children: MutableList<Menu> = ArrayList()

    @Column(name="parent_id")
    var parentId:Int?=null

    var hierachy: Int? = null

    var name: String? = null

    var icon: String? = null

    var text: String? = null

    var path: String? = null

    @Column(name = "is_leaf")
    var isLeaf: String? = null

    @Column(name = "page_path")
    var pagePath: String? = null

    @Column(name = "page_class")
    var pageClass: String? = null

    @Column(name = "load_method")
    var loadMethod: String? = null

    @Column(name = "defaultQueryObj")
    var defaultQueryObj: String? = null

    var function: String? = null

    @Column(name = "menu_order")
    var menuOrder: Int? = null


    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_role_menu",
            joinColumns = [JoinColumn(name = "menu_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")])
    var roles: List<Role> = ArrayList()

}


@Entity
@Table(name = "sys_platform")
@Where(clause = "delete_flag = '1'")
@SQLDelete(sql = "UPDATE sys_platform SET delete_flag = '0' WHERE id = ?", check = ResultCheckStyle.COUNT)
class Platform :AbstractJpaPersistable(){
    enum class AccType{
        SSO,PLAN
    }
    enum class DepolyType{
        K8S,SELF
    }

    @Column
    var code:String? = null
    @Column
    var name:String? = null
    @Column
    var url:String? = null

    @Column
    var icon:String?=null

    @Column(name = "kube_namespace")
    var kubeNamespace:String? = null

    @Column(name="manager_role_id")
    var managerRoleId:Int?=null

    @Column(name="acc_type")
    var accType: String?=null

    @Column(name="depoly_type")
    var depolyType: String?=null

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.PERSIST,CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_platform",
            joinColumns = [JoinColumn(name = "platform_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")])
    var users: List<User> = ArrayList()

    @JsonIgnore
    @OneToMany(cascade= [CascadeType.ALL],fetch=FetchType.LAZY,mappedBy="platform")
    var operations: List<PlatformOperation> = ArrayList()

    @Transient
    var editable = false
}




data class CmContext(
        val name:String,
        val ns:String,
        val key:String,
        val context:String
)

@Entity
@Table(name = "sys_platform_operation")
@Where(clause = "delete_flag = '1'")
@SQLDelete(sql = "UPDATE sys_platform_operation SET delete_flag = '0' WHERE id = ?", check = ResultCheckStyle.COUNT)
class PlatformOperation:AbstractJpaPersistable(){
    var name:String?=null
    var code:String?=null

    @ManyToOne(cascade = [CascadeType.DETACH], fetch = FetchType.EAGER)
    @JoinColumn(name = "platform_id")
    @NotFound(action=NotFoundAction.IGNORE)
    var platform: Platform? = null

    var type:String?=null
    var alias:String?=null
    var method:String?=null
    var path:String?=null
    var head:String?=null
    var body:String?=null
    var result:String?=null
    var info :String?=null
}





