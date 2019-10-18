package ind.syu.kubeIsp.controller

import ind.syu.kubeIsp.config.actionOperation
import ind.syu.kubeIsp.entity.OperateResult
import ind.syu.kubeIsp.entity.Platform
import ind.syu.kubeIsp.entity.PlatformOperation
import ind.syu.kubeIsp.repository.PlatformOperationRepository
import ind.syu.kubeIsp.repository.PlatformRepository
import ind.syu.kubeIsp.service.PlatformService
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/sys/platform")
class PlatformController(
        val platformService: PlatformService,
        val platformRepository: PlatformRepository,
        val platformOperationRepository: PlatformOperationRepository
) {

    @PostMapping
    fun create(@RequestBody platform: Platform)= when(platform.id) {
        null -> platformService.create(platform)
        else -> platformService.update(platform)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id:Int):Platform{
        val platform = platformRepository.findById(id).get()
        return platform
    }

    @GetMapping("/operation/{platformId}")
    @Transactional
    fun operations(@PathVariable platformId:Int) :List<PlatformOperation> {
        val platform = platformRepository.findById(platformId).get()
        val list = platform.operations.filter { it.type!="3" }
        list.size
        return list
    }

    @DeleteMapping("/operation/{id}")
    fun deleteoperation(@PathVariable id:Int):OperateResult  = actionOperation { platformOperationRepository.deleteById(id)  }

    @GetMapping
    fun list(): ResponseEntity<List<Platform>> {
        val list = platformService.list();
        //return ResponseEntity.ok(list)
        return ResponseEntity.badRequest().build()
    }

    @DeleteMapping("/{id}")
    fun dlete(@PathVariable id:Int) :OperateResult = platformService.delete(id)

    @GetMapping("/checkUnique/{code}")
    fun checkUnique(@PathVariable code:String):Map<String,Any>{
        val total = platformRepository.findByCode(code).size
        return mapOf("total" to total)
    }
}