package com.forcetower.sagres.persist

abstract class Storage<T> {
    abstract fun save(id: String, value: T): Boolean
    abstract fun retrieve(id: String): T?
    abstract fun retrieveFromLink(link: String): T?
}