package ind.syu.kubeIsp.service

import ind.syu.kubeIsp.controller.PodController
import ind.syu.kubeIsp.entity.ColumnConf
import ind.syu.kubeIsp.entity.TableConf
import ind.syu.kubeIsp.repository.ColumnConfRepository
import ind.syu.kubeIsp.repository.DictionaryRepository
import ind.syu.kubeIsp.repository.MonyToMonyRepository
import ind.syu.kubeIsp.repository.TableConfRepository
import org.apache.commons.lang3.StringUtils
import org.bouncycastle.asn1.x500.style.RFC4519Style.c
import org.bouncycastle.asn1.x500.style.RFC4519Style.o
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class EntityService(
        val tableConfRepository: TableConfRepository,
        val columnConfRepository: ColumnConfRepository,
        val dictionaryRepository: DictionaryRepository,
        val monyToMonyRepository: MonyToMonyRepository,
        val jdbcTemplate: JdbcTemplate
) {


    companion object {
        private val log: Logger = LoggerFactory.getLogger(EntityService::class.java)
    }

    fun query(entityCode: String, queryObj: MutableMap<String, Any>): Map<String, Any> {
        val resultMap = HashMap<String, Any>()
        val tableOption = tableConfRepository.findByEntityCode(entityCode)
        if (!tableOption.isPresent) {
            resultMap["success"] = false
            resultMap["msg"] = "can't find entity witch code:$entityCode"
            return resultMap
        }
        val entity = tableOption.get()
        val entitys = tableConfRepository.findAll()
        val columns = columnConfRepository.findAll()
        val monyToMony = monyToMonyRepository.findAll()
        val entityColumns = columns.filter { it.table?.id == entity.id }

        if (entityColumns.size === 0) {
            resultMap["success"] = false
            return resultMap
        }

        val foreignColumns = entityColumns.filter { it.foreignKeyId != null }
                .map {
                    mapOf(
                            "entity" to entitys.find { e -> e.id == columns.find { c -> c.id == it.foreignKeyId }?.table?.id },
                            "thisCol" to it,
                            "idCol" to columns.find { c -> c.id == it.foreignKeyId },
                            "nameCol" to columns.find { c -> c.id == it.foreignKeyNameId }

                    )
                }
        var values = "select distinct ${entity.entityCode}.*"
        var tables = " from ${entity.tableName} ${entity.entityCode} "
        foreignColumns.forEach { fCol ->
            val en = fCol["entity"] as TableConf
            val idCol = fCol["idCol"] as ColumnConf
            val thisCol = fCol["thisCol"] as ColumnConf
            val nameCol = fCol["nameCol"] as ColumnConf
            values += ",${en.entityCode}.${nameCol.columnName} ${en.entityCode}_${nameCol.columnName}"
            tables += """ left join ${en.tableName} ${en.entityCode}
                on ${entity.entityCode}.\${thisCol.columnName} = ${en.entityCode}.${idCol.columnName}"""
            var fEntity = en
            if (fEntity.parentEntityId != null && fEntity.id == fEntity.parentEntityId) {
                if (queryObj.containsKey(thisCol.columnName)) {
                    val ids = this.childList(queryObj[thisCol.columnName] as Int, fEntity.idField!!, fEntity.pidField!!, fEntity.tableName!!);
                    queryObj[thisCol.columnName!!] = ids;

                }
            }
        }
        queryObj.forEach { (key, _) ->
            if (key.startsWith("mm")) {
                var eId: Int = 0
                var mmId: Int = 0
                key.replace(Regex("^mm_(\\d+)_(\\d+)$")) {
                    eId = it.groupValues[1].toInt()
                    mmId = it.groupValues[2].toInt()
                    ""
                }
                val en = entitys.find { it.id == eId };
                val mm = monyToMony.find { it.id == mmId };
                val fidField = if (mm?.firstTable === entity.tableName) mm?.firstIdField else mm?.secondIdField
                val sidField = if (mm?.firstTable === en?.tableName) mm?.firstIdField else mm?.secondIdField
                tables += " left join ${mm?.relationTable} ${mm?.relationTable} on ${mm?.relationTable}.${fidField}=${entity.entityCode}.${entity.idField}"
            }
        }

        var sql = "${values}${tables} where 1=1";
        var countSql = "select count(distinct ${entity.entityCode}.${entity.idField}) total ${tables} where 1=1";
        queryObj.forEach currentEach@{ (key, value) ->
            var fieldName = key
            if (fieldName == "start" || fieldName == "pageSize" || fieldName == "page" ||fieldName.startsWith("mm")) return@currentEach
            val opdic = mapOf(
                    "uneq_" to "<>",
                    "gt_" to ">",
                    "lt_" to "<",
                    "null_" to " is null",
                    "notnull_" to " is not null"
            )
            var op = "=";
            var prefix = ""
            arrayOf("uneq_", "gt_", "lt_", "null_", "notnull_").forEach {
                if (fieldName.startsWith(it)) {
                    fieldName = fieldName.replace(it, "");
                    op = opdic[it]!!;
                    prefix = it;
                }
            }

            if (fieldName.startsWith("fuzzy_")) {
                var fname = fieldName.replace(Regex("^fuzzy_(\\w+)"), "$1")
                entityColumns.find { it.columnName == fname }?.let { _ ->
                    sql += " and ${entity.entityCode}.${fname} like '%${value}%'"
                    countSql += " and ${entity.entityCode}.${fname} like '%${value}%'";
                }
            }

            entityColumns.find { it.columnName == fieldName }?.let { col ->
                if (col.columnType == "timestamp") {
                    var arr = value as List<String>
                    sql += """ and ${entity.entityCode}.${fieldName}
                            BETWEEN '${arr[0]}' and '${arr[1]}'""";
                    countSql += """ and ${entity.entityCode}.${fieldName}
                            BETWEEN '${arr[0]}' and '${arr[1]}'""";
                    return@currentEach
                }
            }

            when (value) {
                is List<*> -> {
                    var arrStr = ""
                    value.forEach {
                        if (it is String) {
                            arrStr += "'$it',"
                        }
                    }
                    arrStr.slice(0..arrStr.length - 1)
                    sql += " and ${entity.entityCode}.${fieldName} ${if (op == "=") "in" else "not in"}($arrStr )"
                    countSql += " and ${entity.entityCode}.${fieldName} ${if (op == "=") "in" else "not in"}($arrStr )"
                }
                is String -> {
                    sql += " ssand ${entity.entityCode}.${fieldName}${op}${if (prefix == "null_" || prefix == "notnull_") "" else value}"
                    countSql += " and ${entity.entityCode}.${fieldName}${op}${if (prefix == "null_" || prefix == "notnull_") "" else value}"
                }
            }

        }
        if(!StringUtils.isEmpty(entity.deleteFlagField)){
            sql += " and ${entity.entityCode}.${entity.deleteFlagField}='1'"
            countSql += " and ${entity.entityCode}.${entity.deleteFlagField}='1'"
        }


        queryObj.forEach { (key, value) ->
            println(key)
            if (key.startsWith("mm")) {
                var eId: Int = 0
                var mmId: Int = 0
                key.replace(Regex("^mm_(\\d+)_(\\d+)$")) {
                    eId = it.groupValues[1].toInt()
                    mmId = it.groupValues[2].toInt()
                    ""
                }
                val en = entitys.find { it.id == eId };
                val mm = monyToMony.find { it.id == mmId };
                val fidField = if (mm?.firstTable === entity.tableName) mm?.firstIdField else mm?.secondIdField
                val sidField = if (mm?.firstTable === en?.tableName) mm?.firstIdField else mm?.secondIdField
                if (en?.parentEntityId != null && en.id == en.parentEntityId) {
                    val ids = childList(value as Int, en.idField!!, en.pidField!!, en.tableName!!)
                    sql += " mmand ${mm?.relationTable}.${sidField} in (${ids.joinToString()})"
                    countSql += " mmand ${mm?.relationTable}.${sidField} in (${ids.joinToString()})"
                } else {
                    sql += " mmand ${mm?.relationTable}.${sidField} = ${value}"
                    countSql += " mmand ${mm?.relationTable}.${sidField}=${value}"
                }
            }
        }

        entity.orderField?.let { orderField ->
            sql += " order by "
            orderField.split(',').forEach {
                sql += " ${entity.entityCode}.${it}"
            };
        }
        var pageQuery = false
        //val total = jdbcTemplate.queryForObject(countSql, Int::class.java)
        if (queryObj.containsKey("start") && queryObj.containsKey("pageSize")) {
            val start = queryObj["start"] as Int
            val pageSize = queryObj["pageSize"] as Int
            sql += " limit ${start},${pageSize}"
            pageQuery = true;
        }

        log.info(sql)




        return resultMap
    }


    private fun childList(id: Int, idField: String, pidField: String, tableName: String): List<Int> {
        val result = arrayListOf<Int>(id)
        val sql = "select ${idField} id from ${tableName} where ${pidField} in(:ids)"
        _child(arrayListOf(id), result, sql)
        return result
    }

    private fun _child(currentIds: MutableList<Int>, result: MutableList<Int>, sql: String) {
        val parameters = MapSqlParameterSource()
        parameters.addValue("ids", currentIds)
        val ids = NamedParameterJdbcTemplate(jdbcTemplate).queryForList(sql, parameters, Int::class.java)
        if (ids.size > 0) {
            ids.forEach { result.add(it) }
            this._child(ids, result, sql)
        }
    }


}