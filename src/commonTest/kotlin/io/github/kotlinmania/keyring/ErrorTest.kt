// port-lint: tests error.rs
package io.github.kotlinmania.keyring

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.fail

class ErrorTest {
    @Test
    fun testBadPassword() {
        // malformed sequences here taken from:
        // https://www.cl.cam.ac.uk/~mgk25/ucs/examples/UTF-8-test.txt
        val cases =
            listOf(
                byteArrayOf(0x80.toByte()),
                byteArrayOf(0xbf.toByte()),
                byteArrayOf(0xed.toByte(), 0xa0.toByte(), 0xa0.toByte()),
            )
        for (bytes in cases) {
            val outcome = decodePassword(bytes.copyOf())
            outcome.fold(
                onSuccess = { s ->
                    fail("Bad password (${bytes.toList()}) decode gave results: $s")
                },
                onFailure = { other ->
                    if (other is Error.BadEncoding) {
                        assertContentEquals(bytes, other.bytes)
                    } else {
                        fail("Bad password (${bytes.toList()}) decode gave wrong error: $other")
                    }
                },
            )
        }
    }
}
