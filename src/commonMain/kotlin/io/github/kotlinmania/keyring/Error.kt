// port-lint: source src/error.rs
package io.github.kotlinmania.keyring

/*
 * Platform-independent error model.
 *
 * There is an escape hatch here for surfacing platform-specific error
 * information returned by the platform-specific storage provider, but the
 * concrete objects returned must be thread-safely shareable so they can be
 * moved from one thread to another. (Since most platform errors are integer
 * error codes, this requirement is not much of a burden on the
 * platform-specific store providers.)
 */

/**
 * Each variant of [Error] provides a summary of the error. More details, if
 * relevant, are contained in the associated value, which may be
 * platform-specific.
 *
 * This sealed hierarchy is intentionally open to additional variants so that
 * more values can be added to it without a SemVer break. Clients should
 * always have default handling for variants they don't understand.
 */
sealed class Error(override val message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause) {

    /**
     * This indicates runtime failure in the underlying platform storage
     * system. The details of the failure can be retrieved from the attached
     * platform error.
     */
    class PlatformFailure(val error: Throwable) :
        Error("Platform secure storage failure: ${error.message}", error)

    /**
     * This indicates that the underlying secure storage holding saved items
     * could not be accessed. Typically this is because of access rules in the
     * platform; for example, it might be that the credential store is locked.
     * The underlying platform error will typically give the reason.
     */
    class NoStorageAccess(val error: Throwable) :
        Error("Couldn't access platform secure storage: ${error.message}", error)

    /**
     * This indicates that there is no underlying credential entry in the
     * platform for this entry. Either one was never set, or it was deleted.
     */
    object NoEntry : Error("No matching entry found in secure storage")

    /**
     * This indicates that the retrieved password blob was not a UTF-8 string.
     * The underlying bytes are available for examination in the attached
     * value.
     */
    class BadEncoding(val bytes: ByteArray) : Error("Data is not UTF-8 encoded")

    /**
     * This indicates that one of the entry's credential attributes exceeded a
     * length limit in the underlying platform. The attached values give the
     * name of the attribute and the platform length limit that was exceeded.
     */
    class TooLong(val name: String, val len: UInt) :
        Error("Attribute '$name' is longer than platform limit of $len chars")

    /**
     * This indicates that one of the entry's required credential attributes
     * was invalid. The attached value gives the name of the attribute and the
     * reason it's invalid.
     */
    class Invalid(val attr: String, val reason: String) :
        Error("Attribute $attr is invalid: $reason")

    /**
     * This indicates that there is more than one credential found in the
     * store that matches the entry. Its value is a list of the matching
     * credentials.
     */
    class Ambiguous(val items: List<Credential>) :
        Error("Entry is matched by ${items.size} credentials: $items")
}

/** A [kotlin.Result] specialized to fail with a keyring [Error]. */
typealias Result<T> = kotlin.Result<T>

/** Try to interpret a byte array as a password string. */
fun decodePassword(bytes: ByteArray): Result<String> =
    try {
        Result.success(bytes.decodeToString(throwOnInvalidSequence = true))
    } catch (_: kotlin.text.CharacterCodingException) {
        Result.failure(Error.BadEncoding(bytes))
    }
