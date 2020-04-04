package com.forcetower.sagres.operation.ohmyzsh

import com.forcetower.sagres.operation.BaseCallback
import com.forcetower.sagres.operation.Status

class DoneCallback internal constructor(status: Status) : BaseCallback<DoneCallback>(status)