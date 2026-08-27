// port-lint: source mock.rs
package io.github.kotlinmania.keyring

/*
 * # Mock credential store
 *
 * To facilitate testing of clients, this library provides a Mock credential store
 * that is platform-independent, provides no persistence, and allows the client
 * to specify the return values (including errors) for each call. The credentials
 * in this store have no attributes at all.
 *
 * To use this credential store instead of the default, make this call during
 * application startup before creating any entries:
 * ```kotlin
 * setDefaultCredentialBuilder(defaultCredentialBuilder())
 * ```
 *
 * You can then create entries as you usually do, and call their usual methods
 * to set, get, and delete passwords. There is no persistence other than
 * in the entry itself, so getting a password before setting it will always result
 * in a [Error.NoEntry] error.
 *
 * If you want a method call on an entry to fail in a specific way, you can
 * downcast the entry to a [MockCredential] and then call [MockCredential.setError]
 * with the appropriate error. The next entry method called on the credential
 * will fail with the error you set. The error will then be cleared, so the next
 * call on the mock will operate as usual.
 */

/**
 * The (in-memory) persisted data for a mock credential.
 *
 * We keep a password, but unlike most keystores
 * we also keep an intended error to return on the next call.
 *
 * (Everything about this structure is public for transparency.
 * Most keystore implementations hide their internals.)
 */
data class MockData(
    var secret: ByteArray? = null,
    var error: Error? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MockData) return false
        if (secret != null) {
            if (other.secret == null) return false
            if (!secret.contentEquals(other.secret)) return false
        } else if (other.secret != null) {
            return false
        }
        return error == other.error
    }

    override fun hashCode(): Int {
        var result = secret?.contentHashCode() ?: 0
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}

/**
 * The concrete mock credential.
 *
 * Mocks use an internal mutability pattern since entries are read-only.
 */
class MockCredential(
    var data: MockData = MockData(),
) : CredentialApi {
    /**
     * Set a password on a mock credential.
     *
     * If there is an error in the mock, it will be returned
     * and the password will _not_ be set. The error will
     * be cleared, so calling again will set the password.
     */
    override fun setPassword(password: String): Result<Unit> {
        val err = data.error
        if (err != null) {
            data.error = null
            return Result.failure(err)
        }
        data.secret = password.encodeToByteArray()
        return Result.success(Unit)
    }

    /**
     * Set a secret on a mock credential.
     *
     * If there is an error in the mock, it will be returned
     * and the password will _not_ be set. The error will
     * be cleared, so calling again will set the password.
     */
    override fun setSecret(password: ByteArray): Result<Unit> {
        val err = data.error
        if (err != null) {
            data.error = null
            return Result.failure(err)
        }
        data.secret = password.copyOf()
        return Result.success(Unit)
    }

    /**
     * Get the password from a mock credential, if any.
     *
     * If there is an error set in the mock, it will
     * be returned instead of a password.
     */
    override fun getPassword(): Result<String> {
        val err = data.error
        if (err != null) {
            data.error = null
            return Result.failure(err)
        }
        val secret = data.secret ?: return Result.failure(Error.NoEntry)
        return decodePassword(secret)
    }

    /**
     * Get the secret from a mock credential, if any.
     *
     * If there is an error set in the mock, it will
     * be returned instead of a password.
     */
    override fun getSecret(): Result<ByteArray> {
        val err = data.error
        if (err != null) {
            data.error = null
            return Result.failure(err)
        }
        val secret = data.secret ?: return Result.failure(Error.NoEntry)
        return Result.success(secret.copyOf())
    }

    /**
     * Delete the password in a mock credential.
     *
     * If there is an error, it will be returned and
     * the deletion will not happen.
     *
     * If there is no password, a [Error.NoEntry] error
     * will be returned.
     */
    override fun deleteCredential(): Result<Unit> {
        val err = data.error
        if (err != null) {
            data.error = null
            return Result.failure(err)
        }
        if (data.secret == null) {
            return Result.failure(Error.NoEntry)
        }
        data.secret = null
        return Result.success(Unit)
    }

    /**
     * Return this mock credential concrete object cast to [Any],
     * so it can be downcast.
     */
    override fun asAny(): Any = this

    /**
     * Expose the concrete debug representation for use via the [Credential] interface.
     */
    override fun debugFmt(): String = "MockCredential(data=$data)"

    /**
     * Set an error to be returned from this mock credential.
     *
     * Error returns always take precedence over the normal
     * behavior of the mock. But once an error has been
     * returned it is removed, so the mock works thereafter.
     */
    fun setError(err: Error) {
        data.error = err
    }

    companion object {
        /**
         * Make a new mock credential.
         *
         * Since mocks have no persistence between sessions,
         * new mocks always have no password.
         */
        fun newWithTarget(target: String?, service: String, user: String): Result<MockCredential> =
            Result.success(MockCredential())
    }
}

/**
 * The builder for mock credentials.
 */
class MockCredentialBuilder : CredentialBuilderApi {
    /**
     * Build a mock credential for the given target, service, and user.
     *
     * Since mocks don't persist between sessions, all mocks
     * start off without passwords.
     */
    override fun build(target: String?, service: String, user: String): Result<Credential> =
        MockCredential.newWithTarget(target, service, user)

    /**
     * Get an [Any] reference to the mock credential builder.
     */
    override fun asAny(): Any = this

    /**
     * This keystore keeps the password in the entry.
     */
    override fun persistence(): CredentialPersistence = CredentialPersistence.EntryOnly
}

/**
 * Return a mock credential builder for use by clients.
 */
fun defaultCredentialBuilder(): CredentialBuilder = MockCredentialBuilder()
