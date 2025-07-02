package com.netease.nim.samples.main.model

import com.netease.nim.samples.base.list.model.ItemModel

class MethodItemModel(val methodName: String, val methodDescription: String) : ItemModel(
    """
        $methodName
        $methodDescription
        """.trimIndent()
    ,methodName
)
