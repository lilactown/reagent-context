# reagent-context

A small library providing a nice way to access React 16's Context API in
[reagent](https://github.com/reagent-project/reagent).

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/lilactown/reagent-context.svg)](https://clojars.org/lilactown/reagent-context)

## Basic usage

First, we require `reagent-context.core` into our namespace.

```clojure
(ns my-reagent-app.core
  (:require [reagent-context.core :as context]))
```

`reagent-context.core` has a few functions for working with context:

### (create)

`create` instantiates a new React context that we can use to make providers
and consumers. It implements the `IContext` protocol.

```clojure
(def my-context (context/create))
```

### [provider {:keys [context value]} & children] 

`provider` is a reagent component that takes a props map with the keys
`:context` (required) and an initial value `:value` (optional). The value at the
`:context` key must be something that implements the `IContext` protocol
(see [create](#create)).

Any other args passed to provider is treated as children.

```clojure
(defn my-app []
 [context/provider {:context my-context :value "initial"}
  [:div "Here's a child"]
  [:div "And another one"]])
```

### [consumer {:keys [context]} render]

`consumer` is a reagent component that access the React context we created
and have provided. Takes a map of props with a `:context` key, whose value must
be something that implements the `IContext` protocol (see [create](#create)).

The second argument to consumer is a **render function**: a `fn` that returns
reagent hiccup data.

```clojure
(defn some-component []
 [context/consumer
  {:context my-context}
  (fn [state]
    [:div "My state: " state])])
```

### defconsumer macro

`defconsumer` is a helper macro for defining components that rely on a context.
It takes the place of what React.js uses HOCs for. E.g. this example on the
[React context docs](https://reactjs.org/docs/context.html#consuming-context-with-a-hoc)
can be written as:

```clojure
(ns some-app
  (:require [reagent.context.core :as context :refer [defconsumer]]))

(defconsumer themed-button theme-context
  [theme props child]
  [:button (merge {:class theme} props) child])
```


## Using React libraries that leverage context

Sometimes React libraries expose their own context instance for us to use. We
can easily use it by passing the context instance to the `->Context` constructor.

```clojure
(ns some-app
  (:require [reagent-context.core :as context :refer [->Context]]
            ;; a React library that exposes a context instance
            ["some-lib" :as lib]))

(def lib-context (->Context lib/context-instance))
```

Sometimes, we don't get access to the instance itself but instead are simply
given a `Provider` & `Consumer`. We can use the `interop` function to create a
valid context for us:

```clojure
(def lib-context (context/interop lib/Provider lib/Consumer))
```

## Other helpful things

Sometimes we just don't care about all this protocol provider/consumer malarkey
and just want to get some context (maybe that's provided by some library under
the hood). The simple abstraction all of this is built on is child-as-function.

`reagent-context.core` exposes a function called `child-as-fn` that helps with
interoperating between React components that take children-as-a-function and
reagent.

```clojure
(defn with-theme
  [render-fn]
  (context/child-as-fn
    lib/ThemeConsumer
    render-fn))
    
(defn themed-button []
  [with-theme
   (fn [theme]
     [:button {:class theme} "I'm using a React theming library"])])
```

## Gotchas

Currently, **ratoms do not trigger re-renders inside of components passed into
child-as-function**, because they do not exist within brackets `[]` like reagent
expects.

The effects of this can be mitigated somewhat by creating a component that holds
all of the dependencies on context, and pass into that component any children
that depend on a ratom. 

That's a bit of a mouthful. Here's an example:

```clojure
(defn my-component []
  (let [counter (r/atom 0)
        inc! #(swap! counter inc)]
    (fn []
      [context/consumer {:context theme-context}
       (fn [theme]
         [:div {:class theme}
          "Counter: " @counter
          [:button {:on-click inc!} "increment"]])])))
```

This example shows a counter with an increment button, that derives a value
`theme` from a context consumer.

Unfortunately, our increment button doesn't quite work - the value of the atom
changes, but our component never re-renders because the atom is not dereferenced
inside of a reagent hiccup form (it does not treat the second `(fn [theme] ...)`
correctly).

*How do we fix this?* It's a bit inane, but for now, it works. We need the deref
of the counter atom to show up in a reagent form. So we pull out the bit that
relies on the context into it's own component:

```clojure
;; NOTE: we use `into` to avoid warnings about `key` prop in these examples

(defn themed-box [& children]
 [context/consumer {:context theme-context}
  (fn [theme]
    (into [:div {:class theme}] children))])
    
;; or using the defconsumer macro

(defconsumer themed-box theme-context
  [theme & children]
  (into [:div {:class theme}] children))
```

And then use it like so:

```clojure
(defn my-component []
  (let [counter (r/atom 0)
        inc! #(swap! counter inc)]
    (fn []
      [themed-box
       "Counter: " @counter
       [:button {:on-click inc!} "increment"]])))
```

Now our counter re-renders as we would expect, because reagent sees our atom
being dereferenced inside of a hiccup form.

This doesn't mitigate *all* problems. The two big ones I see are:

1. We cannot consume context and deref an atom in the same component, which is
clunky. 

2. Because of #1, we cannot deref an atom passed down through context.

What this implies is that, if a context value depends on an atom, then that atom
must be dereferenced by the *provider*, so that it will trigger a re-render to
consumers that are listening to that context.

## License

MIT. Copyright Will Acton 2018.
