---
trigger: always_on
---

# Documentation Rules

## Scope

These rules apply to all Javadoc, doc comments, block comments, inline comments, and generated documentation.

## Core Standard

- Default to no documentation.
- Add documentation only when it contributes non-obvious information that materially helps a developer use, maintain, or safely change the code.
- If a comment can be removed without loss of understanding, remove it.
- Brevity is required. Density is preferred over volume.

## Context Requirement

- Never document code in isolation.
- Before writing documentation, read enough surrounding code to understand the symbol or block in context: related types, call sites, tests, configuration, framework behavior, and nearby conventions as needed.
- If context is incomplete, do not write the documentation.
- Do not infer intent from names alone.

## Hard Prohibitions

Agents must not write documentation that:

- Restates what the code already makes obvious.
- Paraphrases names, signatures, types, parameters, return values, or thrown exceptions.
- Narrates visible control flow, assignments, loops, conditionals, or straightforward method steps.
- Explains code line by line.
- States the purpose of trivial getters, setters, constructors, DTOs, records, constants, or obvious private helpers.
- Exists only for coverage, completeness, consistency, symmetry, or style compliance.
- Repeats inherited docs without new behavior or constraints.
- Speculates about intent, guarantees, performance, thread-safety, or rationale without evidence.
- Uses TODO-style filler, placeholders, or generic boilerplate.
- Uses decorative comment blocks.
- Uses separator comments, banner comments, boxed comments, section dividers, or repeated punctuation such as `////`, `----`, `====`, `****`, or similar visual noise.
- Uses self-referencing or meta-documentation such as "This method does...", "This class is responsible for...", "The following code...", or "Helper method to..." when that content adds no real information.
- Writes comments whose only function is to visually organize code.

## High-Value Reasons to Document

Documentation is allowed only when it captures information that is not obvious from code, such as:

- Intent that is not recoverable from names and structure.
- Invariants, assumptions, constraints, or forbidden states.
- Preconditions or postconditions not enforced by types.
- Side effects that may surprise callers or maintainers.
- Edge cases, failure semantics, retry behavior, or partial-success behavior.
- Lifecycle, ordering, ownership, cleanup, or concurrency requirements.
- External contracts, protocol rules, serialization formats, persistence rules, hardware behavior, or framework coupling.
- Compatibility constraints, workarounds, or rationale for a non-obvious implementation.
- Warnings where an apparently simpler change would be incorrect.

If none apply, do not write the comment.

## Javadoc Rules

- Use Javadoc only when API contract or usage semantics are not obvious from the code.
- Prefer documenting public APIs, abstract contracts, extension points, callbacks, and boundaries with external systems.
- Usually skip Javadoc on trivial or self-evident code.
- Do not automatically add `@param`, `@return`, or `@throws`.
- Add tags only when they convey non-obvious, usage-critical information such as valid ranges, ownership, nullability expectations not otherwise expressed, side effects, or unusual error conditions.
- For overrides, do not duplicate inherited docs unless the override adds meaningful constraints, caveats, or behavior.

## Code Comment Rules

- Use regular comments only for local reasoning that would be difficult to recover from the code itself.
- Prefer comments that explain why, what must remain true, what can break, or what future edits must not violate.
- Do not comment obvious operations.
- Do not add comments as separators between logical sections; use code structure, extraction, and naming instead.
- Do not add inline comments to echo the statement they sit beside.

## Preferred Fixes Before Commenting

Before writing documentation, prefer these code improvements:

- Rename symbols to make intent clearer.
- Extract methods or constants.
- Simplify control flow.
- Replace magic values with named constructs.
- Strengthen types or signatures.

If code can be made self-explanatory through refactoring, do that instead of commenting.

## Writing Style

When documentation is justified:

- Be concise, specific, and technical.
- Prefer why, constraints, and contracts over what.
- Use direct statements; avoid throat-clearing and meta phrasing.
- Avoid repeating context already established by surrounding docs.
- Avoid fragile implementation narration that will go stale under refactoring.
- Make every sentence earn its maintenance cost.

## Decision Gate

Before adding any documentation, all of the following must be true:

- The code has been read in enough context.
- The documentation adds non-obvious information.
- The information helps prevent misunderstanding, misuse, or unsafe edits.
- The wording is concise and evidence-based.
- The comment is worth the long-term maintenance cost.

If any check fails, do not write the documentation.

## Enforcement Heuristics

When reviewing generated documentation, delete it if any of these are true:

- It can be replaced by reading the next few lines of code.
- It only mirrors the symbol name.
- It exists mainly to separate sections visually.
- It sounds generic enough to fit many unrelated codebases.
- It does not change how a maintainer would safely edit the code.
- It explains mechanics but not constraints, risks, or rationale.

## Final Rule

Respect developer attention.

Documentation must clarify, not pollute.
