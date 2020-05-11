package imgui

import gli_.has


infix fun Byte.has(i: Int): Boolean = toInt().has(i)
infix fun Long.wo(l: Long): Long = and(l.inv())
infix fun Long.has(l: Long): Boolean = and(l) != 0L
infix fun Long.hasnt(l: Long): Boolean = and(l) == 0L

inline val ULong.L get() = toLong()

infix fun Long.and(uLong: ULong) = and(uLong.L)