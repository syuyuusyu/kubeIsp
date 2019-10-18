package ind.syu.kubeIsp.service

import ind.syu.kubeIsp.entity.Menu
import ind.syu.kubeIsp.entity.Role
import ind.syu.kubeIsp.entity.User
import ind.syu.kubeIsp.repository.MenuRepository
import ind.syu.kubeIsp.repository.PlatformRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenuService(
        val menuRepository: MenuRepository,
        val authorService: AuthorService,
        val platformRepository: PlatformRepository
) {

    @Transactional
    fun userMenu():Menu{
        val user = authorService.currentUser()
        val set = HashSet<Menu>()
        val orgs = user.orgs
        var orgRole: List<Role> = ArrayList()
        if(orgs.size>0){
             orgRole = orgs.map { it.roles }.reduce { acc, orgnation -> acc+orgnation }
        }

        val roles = user.roles
        println(orgRole.size+roles.size)
        if(orgRole.size+roles.size >0){
            (orgRole+roles).map { it.menus }.reduce { acc, list -> acc+list }.forEach{set.add(it)}
        }else{
            return menuRepository.findById(1).get()
        }
        var menu = createMenuTree(set.toMutableList())

        platformManagerMenu(user)?.let {
            menu.children.add(0,it)
        }

        return menu
    }

    fun platformManagerMenu(user:User):Menu?{
        val pmanagetRoles = user.roles.filter { it.type == Role.RoleType.PLATFORMMANAGER.name }
        if(pmanagetRoles.size==0){
            return null
        }
        val menu = Menu()
        menu.name = "plat"
        menu.text = "平台部署管理"
        menu.icon = ""
        menu.isLeaf="0"
        menu.id = 10000
        menu.parentId = 1
        menu.children=pmanagetRoles.mapIndexed{ index, role ->
            var id = index+1
            println(role.id)
            val p = platformRepository.findByManagerRoleId(role.id!!)
            val m = Menu()
            m.icon=p.icon
            m.isLeaf="1"
            m.name=p.code
            m.text= p.name
            m.parentId = 10000
            m.id = id + m.parentId!!
            m.path = "/platform/${p.kubeNamespace}"
            m.pagePath = "kubernetes"
            m.pageClass = "KubeLayout"
            m
        } as MutableList<Menu>

        return menu
    }

    fun createMenuTree(list:MutableList<Menu>):Menu{
        fun tree(arr:MutableList<Menu>) {
            var leafArray = ArrayList<Menu>()
            arr.forEach{ _a ->
                if(arr.filter { a-> a.parentId == _a.id }.size == 0){
                    leafArray.add(_a)
                }
            }
            if ( 1 !in leafArray.map { it.id }){
                leafArray.forEach { arr.remove(it) }
                leafArray.forEach { l->
                    arr.forEach { a->
                        if(a.id == l.parentId){
                            a.children.add(l)
                        }
                    }
                }
                tree(arr)
            }
        }
        tree(list)
        return list[0]
    }

}