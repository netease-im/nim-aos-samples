package com.netease.nim.samples.base.model

data class AddFragmentModel(val fragment: String, val params: Array<Any?>?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AddFragmentModel) return false

        if (fragment != other.fragment) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fragment.hashCode()
        result = 31 * result + (params?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AddFragmentModel(fragment='$fragment', params=${params?.contentToString()})"
    }
}

