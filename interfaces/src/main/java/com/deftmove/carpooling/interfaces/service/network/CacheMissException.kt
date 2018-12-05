package com.deftmove.carpooling.interfaces.service.network

import com.apollographql.apollo.api.Operation

class CacheMissException(
    operation: Operation<Operation.Data, Any, Operation.Variables>
) : Throwable("Cache miss for operation ${operation.name()}")
