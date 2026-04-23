---
trigger: always_on
---

Purpose

Write Javadoc only when it adds non-obvious information that helps future developers correctly use or maintain the code. The goal is clarity, not comment coverage.
Required Context

Do not write Javadoc from a symbol in isolation.

Before documenting, read enough of the codebase to understand the symbol in context, including as relevant:

    Containing class or interface.

    Parent types and overridden methods.

    Call sites and usages.

    Related collaborators.

    Tests.

    Framework, annotation, configuration, or DI behavior.

    Existing project conventions.

If context is insufficient, do not write the Javadoc.
Default Rule

Default to omission.

No Javadoc is better than redundant Javadoc.
Write Javadoc Only If It Adds Value

Add Javadoc only when it explains something not obvious from the name, signature, types, or implementation, such as:

    Intent or role in the system.

    Invariants, assumptions, or constraints.

    Preconditions or postconditions not enforced by types.

    Side effects.

    Error semantics or important edge cases.

    Lifecycle or ordering requirements.

    Concurrency or thread-safety expectations.

    External contracts, protocol rules, compatibility constraints, or workarounds.

    Non-obvious rationale that future maintainers would not recover easily.

If it does not reduce likely confusion, omit it.
Do Not Document

Do not write Javadoc that:

    Restates the name, signature, or visible behavior.

    Paraphrases parameters or return values.

    Narrates obvious implementation steps.

    Exists only for completeness or style compliance.

    Adds boilerplate, filler, or generic text.

    Duplicates inherited docs without new behavior.

    Speculates beyond what the codebase supports.

Priority Targets

If documentation is warranted, prioritize:

    Public APIs.

    Interfaces and abstract contracts.

    Extension points and callbacks.

    Methods with surprising side effects.

    Concurrency-sensitive code.

    Boundaries with I/O, persistence, networking, hardware, caching, or external systems.

    Workarounds, compatibility behavior, and fragile invariants.

Low-Value Targets

Usually skip:

    Trivial getters/setters.

    DTOs/records/data holders.

    Simple constructors.

    Obvious private helpers.

    Straightforward overrides.

    Constants with clear names.

Style

When writing Javadoc:

    Be concise, specific, and accurate.

    Explain intent, contract, constraints, and rationale before mechanics.

    Prefer durable information over fragile implementation details.

    Do not overclaim.

    Do not write @param, @return, or @throws tags unless they add non-obvious meaning.

Decision Test

Before adding Javadoc, confirm all are true:

    I understand the symbol from broader codebase context.

    The doc adds information not obvious from the code.

    The doc helps prevent misuse or misunderstanding.

    The text is concise, evidence-based, and likely to stay useful.

If any check fails, do not add Javadoc.
Final Standard

Respect developer attention. Documentation must clarify, not pollute.
