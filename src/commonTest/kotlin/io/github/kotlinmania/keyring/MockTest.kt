// port-lint: tests mock.rs
package io.github.kotlinmania.keyring

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class MockTest {
    private fun generateRandomString(len: Int = 30): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..len).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private fun generateRandomBytes(len: Int = 24): ByteArray {
        val bytes = ByteArray(len)
        Random.nextBytes(bytes)
        return bytes
    }

    private fun entryNew(service: String, user: String): Entry {
        val credential = MockCredential.newWithTarget(null, service, user).getOrThrow()
        return Entry.newWithCredential(credential)
    }

    private fun testRoundTrip(case: String, entry: Entry, inPass: String) {
        entry.setPassword(inPass).getOrElse { err ->
            fail("Can't set password for $case: $err")
        }
        val outPass =
            entry.getPassword().getOrElse { err ->
                fail("Can't get password for $case: $err")
            }
        assertEquals(inPass, outPass, "Passwords don't match for $case: set='$inPass', get='$outPass'")
        entry.deleteCredential().getOrElse { err ->
            fail("Can't delete password for $case: $err")
        }
        val password = entry.getPassword()
        assertTrue(password.isFailure, "Read deleted password for $case")
        assertIs<Error.NoEntry>(password.exceptionOrNull(), "Read deleted password for $case")
    }

    private fun testRoundTripSecret(case: String, entry: Entry, inSecret: ByteArray) {
        entry.setSecret(inSecret).getOrElse { err ->
            fail("Can't set secret for $case: $err")
        }
        val outSecret =
            entry.getSecret().getOrElse { err ->
                fail("Can't get secret for $case: $err")
            }
        assertContentEquals(inSecret, outSecret, "Secrets don't match for $case")
        entry.deleteCredential().getOrElse { err ->
            fail("Can't delete password for $case: $err")
        }
        val secret = entry.getSecret()
        assertTrue(secret.isFailure, "Read deleted secret for $case")
        assertIs<Error.NoEntry>(secret.exceptionOrNull(), "Read deleted secret for $case")
    }

    @Test
    fun testPersistence() {
        assertEquals(
            CredentialPersistence.EntryOnly,
            defaultCredentialBuilder().persistence(),
        )
    }

    @Test
    fun testMissingEntry() {
        val name = generateRandomString()
        val entry = entryNew(name, name)
        val result = entry.getPassword()
        assertTrue(result.isFailure, "Missing entry has password")
        assertIs<Error.NoEntry>(result.exceptionOrNull(), "Missing entry error is not NoEntry")
    }

    @Test
    fun testEmptyPassword() {
        val name = generateRandomString()
        val entry = entryNew(name, name)
        testRoundTrip("empty password", entry, "")
    }

    @Test
    fun testRoundTripAsciiPassword() {
        val name = generateRandomString()
        val entry = entryNew(name, name)
        testRoundTrip("ascii password", entry, "test ascii password")
    }

    @Test
    fun testRoundTripNonAsciiPassword() {
        val name = generateRandomString()
        val entry = entryNew(name, name)
        testRoundTrip("non-ascii password", entry, "このきれいな花は桜です")
    }

    @Test
    fun testRoundTripRandomSecret() {
        val name = generateRandomString()
        val entry = entryNew(name, name)
        val secret = generateRandomBytes(24)
        testRoundTripSecret("random secret", entry, secret)
    }

    @Test
    fun testUpdate() {
        val name = generateRandomString()
        val entry = entryNew(name, name)
        testRoundTrip("initial ascii password", entry, "test ascii password")
        testRoundTrip("updated non-ascii password", entry, "このきれいな花は桜です")
    }

    @Test
    fun testGetUpdateAttributes() {
        val name = generateRandomString()
        val entry = entryNew(name, name)
        val initialAttrs = entry.getAttributes()
        assertTrue(initialAttrs.isFailure, "Read missing credential in attribute test")
        assertIs<Error.NoEntry>(initialAttrs.exceptionOrNull())

        val map = mapOf("test attribute name" to "test attribute value")
        val initialUpdate = entry.updateAttributes(map)
        assertTrue(initialUpdate.isFailure, "Updated missing credential in attribute test")
        assertIs<Error.NoEntry>(initialUpdate.exceptionOrNull())

        entry.setPassword("test password for attributes").getOrElse { err ->
            fail("Can't set password for attribute test: $err")
        }
        val attrs =
            entry.getAttributes().getOrElse { err ->
                fail("Couldn't get attributes: $err")
            }
        assertTrue(attrs.isEmpty(), "Unexpected attributes: $attrs")

        entry.updateAttributes(map).getOrElse { err ->
            fail("Couldn't update attributes in attribute test: $err")
        }
        val attrsAfter =
            entry.getAttributes().getOrElse { err ->
                fail("Couldn't get attributes after update: $err")
            }
        assertTrue(attrsAfter.isEmpty(), "Unexpected attributes after update: $attrsAfter")

        entry.deleteCredential().getOrElse { err ->
            fail("Can't delete credential for attribute test: $err")
        }
        val finalAttrs = entry.getAttributes()
        assertTrue(finalAttrs.isFailure, "Read deleted credential in attribute test")
        assertIs<Error.NoEntry>(finalAttrs.exceptionOrNull())
    }

    @Test
    fun testSetError() {
        val name = generateRandomString()
        val entry = entryNew(name, name)
        val password = "test ascii password"
        val mock = entry.getCredential() as MockCredential

        mock.setError(Error.Invalid("mock error", "is an error"))
        val setRes1 = entry.setPassword(password)
        assertTrue(setRes1.isFailure, "set: No error")
        assertIs<Error.Invalid>(setRes1.exceptionOrNull())

        entry.setPassword(password).getOrElse {
            fail("set: Error not cleared")
        }

        mock.setError(Error.NoEntry)
        val getRes1 = entry.getPassword()
        assertTrue(getRes1.isFailure, "get: No error")
        assertIs<Error.NoEntry>(getRes1.exceptionOrNull())

        val storedPassword =
            entry.getPassword().getOrElse {
                fail("get: Error not cleared")
            }
        assertEquals(password, storedPassword, "Retrieved and set ascii passwords don't match")

        mock.setError(Error.TooLong("mock", 3u))
        val delRes1 = entry.deleteCredential()
        assertTrue(delRes1.isFailure, "delete: No error")
        assertIs<Error.TooLong>(delRes1.exceptionOrNull())

        entry.deleteCredential().getOrElse {
            fail("delete: Error not cleared")
        }

        val getRes2 = entry.getPassword()
        assertTrue(getRes2.isFailure, "Able to read a deleted ascii password")
        assertIs<Error.NoEntry>(getRes2.exceptionOrNull())
    }
}
