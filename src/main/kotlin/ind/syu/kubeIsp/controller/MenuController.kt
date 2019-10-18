package ind.syu.kubeIsp.controller

import ind.syu.kubeIsp.entity.Menu
import ind.syu.kubeIsp.service.MenuService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController



@RestController
@RequestMapping("/sys/menu")
class MenuController(
        val menuService: MenuService
) {

    @GetMapping
    @Transactional
    fun menu() :Menu{
        return  menuService.userMenu()
    }
}