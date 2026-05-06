# keyring-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fkeyring--kotlin-blue.svg)](https://github.com/KotlinMania/keyring-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/keyring-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/keyring-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/keyring-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/keyring-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`open-source-cooperative/keyring-rs`](https://github.com/open-source-cooperative/keyring-rs).

**Original Project:** This port is based on [`open-source-cooperative/keyring-rs`](https://github.com/open-source-cooperative/keyring-rs). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `open-source-cooperative/keyring-rs`

> The text below is reproduced and lightly edited from [`https://github.com/open-source-cooperative/keyring-rs`](https://github.com/open-source-cooperative/keyring-rs). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## Keyring-rs

[![build](https://github.com/open-source-cooperative/keyring-rs/actions/workflows/ci.yaml/badge.svg)](https://github.com/open-source-cooperative/keyring-rs/actions)
[![dependencies](https://deps.rs/repo/github/open-source-cooperative/keyring-rs/status.svg)](https://deps.rs/repo/github/open-source-cooperative/keyring-rs)
[![crates.io](https://img.shields.io/crates/v/keyring.svg)](https://crates.io/crates/keyring)
[![docs.rs](https://docs.rs/keyring/badge.svg)](https://docs.rs/keyring)

This crate provides a simple CLI for the [Rust keyring ecosystem](https://github.com/open-source-cooperative/keyring-rs/wiki/Keyring). It also provides sample Rust code for developers who are looking to use the keyring infrastructure in their projects and an inventory of available credential store modules.

## History

This crate has a long history. It was first written by [Walther Chen](https://github.com/hwchen) as an "API library plus credential store" combination. Currently maintained by [Dan Brotsky](https://github.com/brotskydotcom), it is now just a "sample code" crate, with the library/API parts now part of the [keyring-core crate](https://crates.io/crates/keyring-core) and the credential stores all in [separate crates](https://crates.io/search?q=keyring%20credential%20store) of their own. The [Contributors file](https://github.com/open-source-cooperative/keyring-rs/blob/HEAD/Contributors.md) lists the many, many people who have contributed to all generations of this crate.

## Do not depend on this crate!

If you are writing an application that uses keyring-compatible credential stores, you should _not_ take a depedency on this crate!! You should _instead_ be relying on the [keyring-core crate](https://crates.io/crates/keyring-core).

If you have an existing application that relies on v3.x of this crate, do _not_ update it to use v4 of this crate! Instead replace your dependency on this crate with a dependency on the [keyring-core crate](https://crates.io/crates/keyring-core). The docs for that crate explain the changes you will need to make in your application.

## Rust CLI

The `keyring` binary produced by building this crate is a command-line interface for issuing one keyring call at a time and examining its results. Issue the command
```shell
keyring --help
```
for usage information.

## Python Module

The CLI provided by this crate is neither efficient nor convenient for scripting, because each invocation loads a credential store, issues just one command against it, and then outputs the results in a format that is hard to parse. If you are looking to do scripting of keyring commands, you are better off using the Python wrapper for this crate available on PyPI in the [rust-native-keyring project](https://pypi.org/project/rust-native-keyring/). Use the shell command
```shell
pip install rust-native-keyring
```
to install it and
```python
import rust_native_keyring
```
to load it into your Python REPL. The sources for this Python module are built using [PyO3](https://github.com/PyO3/pyo3) and can be found in [this repository](https://github.com/open-source-cooperative/keyring-for-python).

## Cross-platform GUI

There is a [Tauri 2.0](https://tauri.app/) cross-platform GUI for Keyring in [this repository](https://github.com/open-source-cooperative/keyring-demo). This GUI allows you to poke around in any of the keyring-compatible credential stores available on your platform. This GUI is currently in public beta testing on iOS, macOS, and Android (instructions [here for iOS/macOS](https://github.com/open-source-cooperative/keyring-demo/issues/2) and [here for Android](https://github.com/open-source-cooperative/keyring-demo/issues/1)), and it’s available for MacOS (not sandboxed), Linux, and Windows on [CrabNebula](https://web.crabnebula.cloud/brotskydotcom/keyring-demo/releases).

## Credential Stores Wanted!

If you are a credential store module developer, you are strongly encouraged to contribute a connector for your module to the library in this crate, thus making it available to users (in the test apps) and application developers (via sample code). See the [module documentation](https://docs.rs/keyring/latest/keyring/) for details.

## License

Licensed under either of

* Apache License, Version 2.0, ([LICENSE-APACHE](https://github.com/open-source-cooperative/keyring-rs/blob/HEAD/LICENSE-APACHE) or http://www.apache.org/licenses/LICENSE-2.0)
* MIT license ([LICENSE-MIT](https://github.com/open-source-cooperative/keyring-rs/blob/HEAD/LICENSE-MIT) or http://opensource.org/licenses/MIT)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in the work by you, as defined in the Apache-2.0 license, shall be dual licensed as above, without any additional terms or conditions.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:keyring-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`open-source-cooperative/keyring-rs`](https://github.com/open-source-cooperative/keyring-rs). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the keyring-rs authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`open-source-cooperative/keyring-rs`](https://github.com/open-source-cooperative/keyring-rs) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
