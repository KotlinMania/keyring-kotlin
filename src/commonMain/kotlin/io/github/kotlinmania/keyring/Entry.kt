// port-lint: source lib.rs
package io.github.kotlinmania.keyring

/*
 * # Keyring
 *
 * This is a cross-platform library that does storage and retrieval of passwords
 * (or other secrets) in an underlying platform-specific secure store.
 *
 * ## Design
 *
 * This library implements a very simple, platform-independent concrete object called an [Entry].
 * Each entry is identified by a <service name, user name> pair of UTF-8 strings,
 * optionally augmented by a target string (which can be used to distinguish two entries
 * that have the same service name and user name).
 * Entries support setting, getting, and forgetting (aka deleting) passwords (UTF-8 strings)
 * and binary secrets (byte arrays).
 *
 * Entries provide persistence for their passwords by wrapping credentials held in platform-specific
 * credential stores. The implementations of these platform-specific stores are captured
 * in two types (with associated interfaces):
 *
 * - a credential builder, represented by the [CredentialBuilder] type
 *   (and [CredentialBuilderApi] interface). Credential
 *   builders are given the identifying information provided for an entry and map
 *   it to the identifying information for a platform-specific credential.
 * - a credential, represented by the [Credential] type
 *   (and [CredentialApi] interface). The platform-specific credential
 *   identified by a builder for an entry is what provides the secure storage
 *   for that entry's password/secret.
 */

/**
 * Internal container for holding a configured default [CredentialBuilder].
 */
data class EntryBuilder(
    var inner: CredentialBuilder? = null,
)

private val defaultEntryBuilder = EntryBuilder()

/**
 * Set the credential builder used by default to create entries.
 *
 * This is really meant for use by clients who bring their own credential
 * store and want to use it everywhere. If you are using multiple credential
 * stores and want precise control over which credential is in which store,
 * then use [Entry.newWithCredential].
 *
 * It is meant to be called at app startup before you start creating entries.
 */
fun setDefaultCredentialBuilder(new: CredentialBuilder) {
    defaultEntryBuilder.inner = new
}

/**
 * Returns the currently active default credential builder, falling back to the
 * platform default credential builder if none was explicitly configured.
 */
fun getDefaultCredentialBuilder(): CredentialBuilder = defaultEntryBuilder.inner ?: defaultCredentialBuilder()

private fun buildDefaultCredential(target: String?, service: String, user: String): Result<Entry> {
    val builder = getDefaultCredentialBuilder()
    return builder.build(target, service, user).map { Entry(it) }
}

/**
 * An entry representing a stored credential in a secure keystore.
 */
class Entry(
    val inner: Credential,
) {
    /**
     * Set the password for this entry.
     *
     * Can return an [Error.Ambiguous] error
     * if there is more than one platform credential
     * that matches this entry.
     */
    fun setPassword(password: String): Result<Unit> = inner.setPassword(password)

    /**
     * Set the secret for this entry.
     *
     * Can return an [Error.Ambiguous] error
     * if there is more than one platform credential
     * that matches this entry.
     */
    fun setSecret(secret: ByteArray): Result<Unit> = inner.setSecret(secret)

    /**
     * Retrieve the password saved for this entry.
     *
     * Returns an [Error.NoEntry] error if there isn't one.
     */
    fun getPassword(): Result<String> = inner.getPassword()

    /**
     * Retrieve the secret saved for this entry.
     *
     * Returns an [Error.NoEntry] error if there isn't one.
     */
    fun getSecret(): Result<ByteArray> = inner.getSecret()

    /**
     * Get the attributes on the underlying credential for this entry.
     *
     * Returns an [Error.NoEntry] error if there isn't a credential for this entry.
     */
    fun getAttributes(): Result<Map<String, String>> = inner.getAttributes()

    /**
     * Update the attributes on the underlying credential for this entry.
     *
     * Returns an [Error.NoEntry] error if there isn't a credential for this entry.
     */
    fun updateAttributes(attributes: Map<String, String>): Result<Unit> = inner.updateAttributes(attributes)

    /**
     * Delete the underlying credential for this entry.
     *
     * Returns an [Error.NoEntry] error if there isn't one.
     */
    fun deleteCredential(): Result<Unit> = inner.deleteCredential()

    /**
     * Return a reference to this entry's wrapped credential.
     *
     * The reference is of the [Any] type, so it can be
     * downcast to a concrete credential object.
     */
    fun getCredential(): Any = inner.asAny()

    override fun toString(): String = "Entry(inner=${inner.debugFmt()})"

    companion object {
        /**
         * Create an entry for the given service and user.
         *
         * The default credential builder is used.
         */
        fun new(service: String, user: String): Result<Entry> =
            buildDefaultCredential(null, service, user)

        /**
         * Create an entry for the given target, service, and user.
         *
         * The default credential builder is used.
         */
        fun newWithTarget(target: String, service: String, user: String): Result<Entry> =
            buildDefaultCredential(target, service, user)

        /**
         * Create an entry that uses the given platform credential for storage.
         */
        fun newWithCredential(credential: Credential): Entry = Entry(credential)
    }
}
