package lib.xfy9326.io.target.base

import java.io.InputStream

interface InputTarget<I : InputStream>: IOTarget {
    fun openInputStream(): I
}