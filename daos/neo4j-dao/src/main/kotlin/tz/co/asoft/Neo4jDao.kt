package tz.co.asoft

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.session.SessionFactory
import kotlin.reflect.KClass

open class Neo4jDao<T : Entity>(
    config: Configuration,
    override val clazz: KClass<T>,
    override val depth: Int = 10,
    vararg clazzes: KClass<*>
) : INeo4jDao<T> {

    constructor(
        protocal: String = "http",
        username: String = "neo4j",
        password: String = "neo4j",
        service: String,
        port: Int = 7474,
        depth: Int = 10,
        clazz: KClass<T>,
        vararg clazzes: KClass<*>
    ) : this(
        config = Configuration.Builder().uri("$protocal://$username:$password@$service:$port").build(),
        clazz = clazz,
        depth = depth,
        clazzes = *clazzes
    )

    override val session by lazy {
        val klasses = (setOf(clazz) + clazzes).map { it.java.canonicalName }.toSet()
        SessionFactory(config, *klasses.toTypedArray()).openSession()
    }

    override suspend fun create(list: Collection<T>) = withContext(Dispatchers.IO) {
        session.save(list, depth)
        //Check here
        val noIds = list.filter { it.uid != null }.map { it.apply { uid = uid.toString() } }
        if (noIds.isNotEmpty()) session.save(noIds, depth)
        session.clear()
        list.toList()
    }

    override suspend fun create(t: T) = create(listOf(t)).first()

    override suspend fun edit(list: Collection<T>) = create(list)

    override suspend fun edit(t: T) = edit(listOf(t)).first()

    override suspend fun delete(list: Collection<T>): List<T> = edit(list.map {
        it.deleted = true
        it
    })

    override suspend fun delete(t: T) = listOf(t).first()

    override suspend fun wipe(list: Collection<T>) = withContext(Dispatchers.IO) {
        session.delete(list)
        session.clear()
        list.toList()
    }

    override suspend fun wipe(t: T) = wipe(listOf(t)).first()

    override suspend fun load(ids: Collection<Any>): List<T> = coroutineScope {
        ids.toSet().map { async { load(it.toString()) } }.mapNotNull { it.await() }
    }

    override suspend fun load(id: String): T? = withContext(Dispatchers.IO) {
        val filter = Filter("uid", ComparisonOperator.EQUALS, id)
        session.loadAll(clazz.java, filter, depth).firstOrNull().apply { session.clear() }
    }

    override suspend fun load(id: Number) = load(id.toString())

    override suspend fun all(): List<T> = withContext(Dispatchers.IO) {
        val filter = Filter("deleted", ComparisonOperator.EQUALS, false)
        session.loadAll(clazz.java, filter, depth).toList().apply { session.clear() }
    }

    override suspend fun load(startAt: String?, limit: Int) = withContext(Dispatchers.IO) {
        emptyList<T>()
    }

    override suspend fun allDeleted(): List<T> = withContext(Dispatchers.IO) {
        val filter = Filter("deleted", ComparisonOperator.EQUALS, true)
        session.loadAll(clazz.java, filter, depth).toList().apply { session.clear() }
    }

    override fun pageLoader(predicate: (T) -> Boolean): PageLoader<*, T> = Neo4jPageLoader(session, clazz, depth, predicate)
}