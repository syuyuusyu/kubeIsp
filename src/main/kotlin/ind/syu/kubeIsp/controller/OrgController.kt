package ind.syu.kubeIsp.controller

import ind.syu.kubeIsp.entity.Orgnation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sys/org")
class OrgController {

    @PostMapping
    fun create(@RequestBody org:Orgnation):ResponseEntity<String>{
        return ResponseEntity.ok("sdsdsd")
    }
}