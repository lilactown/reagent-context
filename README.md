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

### [provider {:keys [context value]} & children}] 

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
  (:require [reagent.context.core :refer [defconsumer]]))

(defconsumer themed-button theme-context
  [theme props child]
  [:button (merge {:class theme} props) child])
```


## Using React libraries that leverage context

Sometimes React libraries expose their own context instance for us to use. We
can easily use it by passing the context instance to the `->Context` constructor.

```clojure
(ns some-app
  (:require [reagent-context.core :refer [->Context]]
            ;; a React library that exposes a context instance
            ["some-lib" :as lib]))

(def lib-context (->Context lib/context-instance))
```

Sometimes, we don't get access to the instance itself but instead are simply
given a `Provider` & `Consumer`. We can use the `interop` function to create a
valid context for us:

```clojure
(def lib-context (interop lib/Provider lib/Consumer))
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
  (child-as-fn
    lib/ThemeConsumer
    render-fn))
    
(defn themed-button []
  [with-theme
   (fn [theme]
     [:button {:class theme} "I'm using a React theming library"])])
```

## Gotchas

Currently, **ratoms do not trigger re-renders inside of components passed into
child-as-function**. Therefore, any components that depend on the state of a
ratom that is being used inside of a context consumer will not reactively
re-render.


## License

MIT. Copyright Will Acton 2018.
