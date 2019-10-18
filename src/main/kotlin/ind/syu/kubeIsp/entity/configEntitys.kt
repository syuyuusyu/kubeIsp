package ind.syu.kubeIsp.entity

import javax.persistence.*

@Entity
@Table(name = "entity", schema = "kubernetes", catalog = "")
class TableConf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "tableName", nullable = false)
    var tableName: String? = null

    @Column(name = "entityCode", nullable = true)
    var entityCode: String? = null

    @Column(name = "entityName", nullable = true)
    var entityName: String? = null

    @Column(name = "nameField", nullable = true)
    var nameField: String? = null

    @Column(name = "queryField", nullable = true)
    var queryField: String? = null

    @get:Column(name = "parentEntityId", nullable = true)
    var parentEntityId: Int? = null

    @get:Column(name = "idField", nullable = false)
    var idField: String? = null

    @get:Column(name = "pidField", nullable = true)
    var pidField: String? = null

    @get:Column(name = "deleteFlagField", nullable = true)
    var deleteFlagField: String? = null

    @get:Column(name = "editAble", nullable = true)
    var editAble: String? = null

    @get:Column(name = "tableLength", nullable = true)
    var tableLength: Int? = null

    @get:Column(name = "orderField", nullable = true)
    var orderField: String? = null

    @get:Column(name = "mmQueryField", nullable = true)
    var mmQueryField: String? = null

    @get:Column(name = "fuzzyQueryField", nullable = true)
    var fuzzyQueryField: String? = null

    @OneToMany(cascade= [CascadeType.ALL],fetch=FetchType.LAZY,mappedBy="table")
    var columns: List<ColumnConf>? = null


    override fun toString(): String =
            "Entity of type: ${javaClass.name} ( " +
                    "id = $id " +
                    "tableName = $tableName " +
                    "entityCode = $entityCode " +
                    "entityName = $entityName " +
                    "nameField = $nameField " +
                    "queryField = $queryField " +
                    "parentEntityId = $parentEntityId " +
                    "idField = $idField " +
                    "pidField = $pidField " +
                    "deleteFlagField = $deleteFlagField " +
                    "editAble = $editAble " +
                    "tableLength = $tableLength " +
                    "orderField = $orderField " +
                    "mmQueryField = $mmQueryField " +
                    "fuzzyQueryField = $fuzzyQueryField " +
                    ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TableConf

        if (id != other.id) return false

        return true
    }

}

@Entity
@Table(name = "entity_column", schema = "kubernetes", catalog = "")
class ColumnConf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null


    @ManyToOne(cascade = [CascadeType.DETACH], fetch = FetchType.EAGER)
    @JoinColumn(name = "entityId")
    var table: TableConf? = null

//    @get:Column(name = "entityId", nullable = false)
//    var entityId: Int? = null

    @get:Column(name = "tableName", nullable = true)
    var tableName: String? = null

    @get:Column(name = "columnIndex", nullable = false)
    var columnIndex: Int? = null

    @get:Column(name = "entityCode", nullable = true)
    var entityCode: String? = null

    @get:Column(name = "columnType", nullable = true)
    var columnType: String? = null

    @get:Column(name = "columnName", nullable = true)
    var columnName: String? = null

    @get:Column(name = "text", nullable = true)
    var text: String? = null

    @get:Column(name = "isUnique", nullable = true)
    var isUnique: String? = null

    @get:Column(name = "required", nullable = true)
    var required: String? = null

    @get:Column(name = "width", nullable = true)
    var width: Int? = null

    @get:Column(name = "render", nullable = true)
    var render: String? = null

    @get:Column(name = "hidden", nullable = true)
    var hidden: String? = null

    @get:Column(name = "dicGroupId", nullable = true)
    var dicGroupId: Int? = null

    @get:Column(name = "foreignKeyId", nullable = true)
    var foreignKeyId: Int? = null

    @get:Column(name = "foreignKeyNameId", nullable = true)
    var foreignKeyNameId: Int? = null

    @get:Column(name = "comme", nullable = true)
    var comme: String? = null


    override fun toString(): String =
            "Entity of type: ${javaClass.name} ( " +
                    "id = $id " +
                    "tableName = $tableName " +
                    "columnIndex = $columnIndex " +
                    "entityCode = $entityCode " +
                    "columnType = $columnType " +
                    "columnName = $columnName " +
                    "text = $text " +
                    "isUnique = $isUnique " +
                    "required = $required " +
                    "width = $width " +
                    "render = $render " +
                    "hidden = $hidden " +
                    "dicGroupId = $dicGroupId " +
                    "foreignKeyId = $foreignKeyId " +
                    "foreignKeyNameId = $foreignKeyNameId " +
                    "comme = $comme " +
                    ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ColumnConf

        if (id != other.id) return false
        if (comme != other.comme) return false

        return true
    }

}

@Entity
@Table(name = "entity_dictionary", schema = "kubernetes", catalog = "")
class Dictionary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @get:Column(name = "groupId", nullable = false)
    var groupId: Int? = null

    @get:Column(name = "groupName", nullable = true)
    var groupName: String? = null

    @get:Column(name = "text", nullable = false)
    var text: String? = null

    @get:Column(name = "value", nullable = false)
    var value: String? = null


    override fun toString(): String =
            "Entity of type: ${javaClass.name} ( " +
                    "id = $id " +
                    "groupId = $groupId " +
                    "groupName = $groupName " +
                    "text = $text " +
                    "value = $value " +
                    ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Dictionary

        if (id != other.id) return false

        return true
    }

}

@Entity
@Table(name = "entity_mony_to_mony", schema = "kubernetes", catalog = "")
class MonyToMony {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @get:Column(name = "name", nullable = true)
    var name: String? = null

    @get:Column(name = "firstTable", nullable = false)
    var firstTable: String? = null

    @get:Column(name = "secondTable", nullable = false)
    var secondTable: String? = null

    @get:Column(name = "firstIdField", nullable = false)
    var firstIdField: String? = null

    @get:Column(name = "secondIdField", nullable = false)
    var secondIdField: String? = null

    @get:Column(name = "relationTable", nullable = false)
    var relationTable: String? = null


    override fun toString(): String =
            "Entity of type: ${javaClass.name} ( " +
                    "id = $id " +
                    "name = $name " +
                    "firstTable = $firstTable " +
                    "secondTable = $secondTable " +
                    "firstIdField = $firstIdField " +
                    "secondIdField = $secondIdField " +
                    "relationTable = $relationTable " +
                    ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MonyToMony

        if (id != other.id) return false

        return true
    }

}