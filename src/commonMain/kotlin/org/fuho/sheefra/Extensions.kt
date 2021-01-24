package org.fuho.sheefra

val Int.isOdd: Boolean get() = this % 2 == 1
val Int.isEven: Boolean get() = !this.isOdd