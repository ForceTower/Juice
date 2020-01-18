package com.forcetower.sagres.persist

import com.forcetower.sagres.database.model.SagresClass
import com.forcetower.sagres.database.model.SagresCredential
import com.forcetower.sagres.database.model.SagresDisciplineResumed
import com.forcetower.sagres.database.model.SagresMessageScope
import com.forcetower.sagres.database.model.SagresPerson

interface CachedPersistence {
    fun access(): Storage<SagresCredential>
    fun clazz(): Storage<SagresClass>
    fun person(): Storage<SagresPerson>
    fun messageScope(): Storage<SagresMessageScope>
    fun disciplineResumed(): Storage<SagresDisciplineResumed>
}
