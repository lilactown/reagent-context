(ns reagent-context.core)

(defmacro defconsumer
  "Defines a reagent component of a given name that consumes the given context.

   (defconsumer component-name my-context
     [context-value & props]
     ;; props are bound for the body below, as well as context-value
     ...reagent component body)"
  [name# context# [context-value# & props#] & body#]
  `(defn ~name# [~@props#]
     [reagent-context.core/consumer
      {:context ~context#}
      (fn [value#]
        (let [~context-value# value#]
          ~@body#))]))
