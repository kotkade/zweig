# Zweig – a groovy AST DSL

    Zweig, der
      kleiner Ast

## Description

“Zweig” is a high-level DSL over Groovy's AST. I had trouble with
Groovy's `AstBuilder`. Either it required detailed knowledge of the
underlying AST structue, or it couldn't do what I wanted because
of other technical restrictions.

Also, I found the overall interface not as clear as I hoped it to be.

Together this lead me to start working on “Zweig.” It is a high level
DSL to put together groovy AST transformations. Instead of specifying
the AST itself you let the library know, what you want. How the actual
AST looks like is left to the library.

## Examples

The basic idea is to specify the desired outcome as a map specification
with all necessary information. The library turns this specification
into the corresponding AST snippet.

    foo.bar("baz", 5, frob)

Take for example this method call. To get the corresponding AST you
simple give a map with the description of the call to “Zweig.”

    ZweigBuilder.fromSpec([
        call: "bar",
        on:   [variable: "foo"],
        with: ["baz", 5, [variable: "frob"]]
    ])

Note, how you don't have to know anything about the underlying structures.

This is completely flexible. Maybe you have the part of the AST for the
object expression already? No problem. Just feed it into the expression!

    def fooVarExpr = new VariableExpression("foo")

    ZweigBuilder.fromSpec([
        call: "bar",
        on:   fooVarExpr,
        with: ["baz", 5, [variable: "frob"]]
    ])

The specifications are really only plain old data structures. You can
easily extract the definition of parts into sub methods. You can construct
larger specifications from single parts with the normal collection
functions groovy already provides. All is pure groovy! No special rules
apply. Only the call to `ZweigBuilder.fromSpec` finally translates the
data structure into the AST representation.

## Usage

Include the maven vector in your dependencies:

    repositories {
        maven { url "http://clojars.org/repo" }
    }

    dependencies {
        compile "de.kotka.groovy:zweig:<version>"
    }

The latest version can be found in the [clojars repository](http://clojars.org/de.kotka.groovy.zweig/zweig).

## Compatibility

Currently I tested “Zweig” only against groovy 1.8.
