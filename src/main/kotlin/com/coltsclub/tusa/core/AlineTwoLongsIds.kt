package com.coltsclub.tusa.core

class AlineTwoLongsIds {
    companion object {
        fun aline(id1: Long, id2: Long): Pair<Long, Long> {
            return if (id1 < id2) {
                Pair(id1, id2)
            } else {
                Pair(id2, id1)
            }
        }
    }
}