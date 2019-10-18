package ind.syu.kubeIsp.repository

import ind.syu.kubeIsp.entity.ColumnConf
import ind.syu.kubeIsp.entity.Dictionary
import ind.syu.kubeIsp.entity.MonyToMony
import ind.syu.kubeIsp.entity.TableConf
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TableConfRepository: CrudRepository<TableConf,Int>{
    fun findByEntityCode(code:String) :Optional<TableConf>
}

interface ColumnConfRepository:CrudRepository<ColumnConf,Int>

interface DictionaryRepository:CrudRepository<Dictionary,Int >

interface MonyToMonyRepository:CrudRepository<MonyToMony,Int>