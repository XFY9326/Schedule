package lib.xfy9326.io.target.base

import java.io.OutputStream

interface OutputTarget<O: OutputStream>: IOTarget {
    suspend fun openOutputStream(): O
}