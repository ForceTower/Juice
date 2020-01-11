package com.forcetower.sagres.impl

import com.forcetower.sagres.database.model.SagresClass
import com.forcetower.sagres.database.model.SagresCredential
import com.forcetower.sagres.database.model.SagresDisciplineResumed
import com.forcetower.sagres.database.model.SagresMessageScope
import com.forcetower.sagres.database.model.SagresPerson
import com.forcetower.sagres.persist.CachedPersistence
import com.forcetower.sagres.persist.Storage

class InMemoryCachePersistence : CachedPersistence {
    private val access = object: Storage<SagresCredential>() {
        private val map = mutableMapOf<String, SagresCredential>()
        override fun save(id: String, value: SagresCredential): Boolean {
            map[id] = value
            return true
        }
        override fun retrieve(id: String) = map[id]
        override fun retrieveFromLink(link: String): SagresCredential? = null
    }

    private val clazz = object: Storage<SagresClass>() {
        private val map = mutableMapOf<String, SagresClass>()
        override fun save(id: String, value: SagresClass): Boolean {
            map[id] = value
            return true
        }

        override fun retrieve(id: String) = map[id]

        override fun retrieveFromLink(link: String): SagresClass? {
            return map.values.firstOrNull { it.link == link }
        }
    }

    private val person = object: Storage<SagresPerson>() {
        private val map = mutableMapOf<String, SagresPerson>()
        override fun save(id: String, value: SagresPerson): Boolean {
            map[id] = value
            return true
        }

        override fun retrieve(id: String) = map[id]

        override fun retrieveFromLink(link: String): SagresPerson? {
            return map.values.firstOrNull { it.link == link }
        }
    }

    private val messageScope = object: Storage<SagresMessageScope>() {
        private val map = mutableMapOf<String, SagresMessageScope>()
        override fun save(id: String, value: SagresMessageScope): Boolean {
            map[id] = value
            return true
        }

        override fun retrieve(id: String) = map[id]

        override fun retrieveFromLink(link: String): SagresMessageScope? {
            return map.values.firstOrNull { it.uid == link }
        }
    }

    private val disciplineResumed = object: Storage<SagresDisciplineResumed>() {
        private val map = mutableMapOf<String, SagresDisciplineResumed>()
        override fun save(id: String, value: SagresDisciplineResumed): Boolean {
            map[id] = value
            return true
        }

        override fun retrieve(id: String) = map[id]

        override fun retrieveFromLink(link: String): SagresDisciplineResumed? {
            return map.values.firstOrNull { it.link == link }
        }
    }

    override fun access() = access
    override fun clazz() = clazz
    override fun person() = person
    override fun messageScope() = messageScope
    override fun disciplineResumed() = disciplineResumed
}