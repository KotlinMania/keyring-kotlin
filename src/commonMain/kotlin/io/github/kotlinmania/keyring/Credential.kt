// port-lint: source src/credential.rs
package io.github.kotlinmania.keyring

/*
 * # Platform-independent secure storage model
 *
 * This module defines a plug and play model for platform-specific credential
 * stores. The model comprises two interfaces: [CredentialBuilderApi] for the
 * underlying store and [CredentialApi] for the entries in the store. These
 * interfaces must be implemented in a thread-safe way, a requirement
 * captured in the [CredentialBuilder] and [Credential] type aliases that
 * wrap them.
 *
 * Note that you must have an instance of a credential builder in your hands
 * in order to call the [CredentialBuilder] API. Because each credential
 * builder implementation lives in a platform-specific module, the
 * cross-platform way to get your hands on the one currently being used to
 * create entries is to ask for the builder from the `default` module alias.
 * For example, to determine whether the credential builder currently being
 * used persists its credentials across machine reboots, you might use a
 * snippet like this:
 *
 * ```kotlin
 * val persistence = default.defaultCredentialBuilder().persistence()
 * if (persistence == CredentialPersistence.UntilDelete) {
 *     println("The default credential builder persists credentials on disk!")
 * } else {
 *     println("The default credential builder doesn't persist credentials on disk!")
 * }
 * ```
 */

/** The API that [Credential] implementations expose. */
interface CredentialApi {
    /**
     * Set the credential's password (a string).
     *
     * This will persist the password in the underlying store.
     */
    fun setPassword(password: String): Result<Unit> =
        setSecret(password.encodeToByteArray())

    /**
     * Set the credential's secret (a byte array).
     *
     * This will persist the secret in the underlying store.
     */
    fun setSecret(password: ByteArray): Result<Unit>

    /**
     * Retrieve the password (a string) from the underlying credential.
     *
     * This has no effect on the underlying store. If there is no credential
     * for this entry, an [Error.NoEntry] error is returned.
     */
    fun getPassword(): Result<String> {
        val secret = getSecret().getOrElse { return Result.failure(it) }
        return decodePassword(secret)
    }

    /**
     * Retrieve a secret (a byte array) from the credential.
     *
     * This has no effect on the underlying store. If there is no credential
     * for this entry, an [Error.NoEntry] error is returned.
     */
    fun getSecret(): Result<ByteArray>

    /**
     * Get the secure store attributes on this entry's credential.
     *
     * Each credential store may support reading and updating different named
     * attributes; see the documentation on each of the stores for details.
     * Note that the keyring itself uses some of these attributes to map
     * entries to their underlying credential; these _controlled_ attributes
     * are not available for reading or updating.
     *
     * We provide a default (no-op) implementation of this method for backward
     * compatibility with stores that don't implement it.
     */
    fun getAttributes(): Result<Map<String, String>> {
        // this should err in the same cases as getSecret, so first call that for effect
        getSecret().getOrElse { return Result.failure(it) }
        // if we got this far, return success with no attributes
        return Result.success(emptyMap())
    }

    /**
     * Update the secure store attributes on this entry's credential.
     *
     * Each credential store may support reading and updating different named
     * attributes; see the documentation on each of the stores for details.
     * The implementation will ignore any attribute names that you supply that
     * are not available for update. Because the names used by the different
     * stores tend to be distinct, you can write cross-platform code that will
     * work correctly on each platform.
     *
     * We provide a default no-op implementation of this method for backward
     * compatibility with stores that don't implement it.
     */
    fun updateAttributes(attributes: Map<String, String>): Result<Unit> {
        // this should err in the same cases as getSecret, so first call that for effect
        getSecret().getOrElse { return Result.failure(it) }
        // if we got this far, return success after setting no attributes
        return Result.success(Unit)
    }

    /**
     * Delete the underlying credential, if there is one.
     *
     * This is not idempotent if the credential existed! A second call to
     * [deleteCredential] will return an [Error.NoEntry] error.
     */
    fun deleteCredential(): Result<Unit>

    /**
     * Return the underlying concrete object cast to [Any].
     *
     * This allows clients to downcast the credential to its concrete type so
     * they can do platform-specific things with it (e.g., query its
     * attributes in the underlying store).
     */
    fun asAny(): Any

    /**
     * The debug representation for the object.
     *
     * This is used to back [toString] on this type; it allows generic code to
     * provide debug printing as provided by the underlying concrete object.
     *
     * We provide a (useless) default implementation for backward
     * compatibility with existing implementors who may have not provided a
     * meaningful textual representation for their credential objects.
     */
    fun debugFmt(): String = asAny().toString()
}

/** A thread-safe implementation of the [CredentialApi]. */
typealias Credential = CredentialApi

/**
 * A descriptor for the lifetime of stored credentials, returned from a
 * credential store's [CredentialBuilderApi.persistence] call.
 */
enum class CredentialPersistence {
    /** Credentials vanish when the entry vanishes (stored in the entry). */
    EntryOnly,

    /** Credentials vanish when the process terminates (stored in process memory). */
    ProcessOnly,

    /** Credentials persist until the machine reboots (stored in kernel memory). */
    UntilReboot,

    /** Credentials persist until they are explicitly deleted (stored on disk). */
    UntilDelete,
}

/** The API that [CredentialBuilder] implementations expose. */
interface CredentialBuilderApi {
    /**
     * Create a credential identified by the given target, service, and user.
     *
     * This typically has no effect on the content of the underlying store. A
     * credential need not be persisted until its password is set.
     */
    fun build(target: String?, service: String, user: String): Result<Credential>

    /**
     * Return the underlying concrete object cast to [Any].
     *
     * Because credential builders need not have any internal structure, this
     * call is not so much for clients as it is to allow automatic derivation
     * of a textual representation for builders.
     */
    fun asAny(): Any

    /**
     * The lifetime of credentials produced by this builder.
     *
     * A default implementation is provided for backward compatibility, since
     * this API was added in a minor release. The default assumes that
     * keystores use disk-based credential storage.
     */
    fun persistence(): CredentialPersistence = CredentialPersistence.UntilDelete
}

/** A thread-safe implementation of the [CredentialBuilderApi]. */
typealias CredentialBuilder = CredentialBuilderApi
