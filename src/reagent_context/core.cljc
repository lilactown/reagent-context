(ns reagent-context.core)

(defmacro defconsumer
  "Defines a reagent component of a given name that consumes the given context.
   `context-atom` will be given a reagent atom that can be dereferenced to
   re-render when the context state changes.

   (defconsumer component-name my-context
     [context-atom text]
     ;; props are bound for the body below, as well as context-value
     [:button {:class @context-atom} text])

   Use like:
    [component-name \"I'm a button with a theme\"]"
  [name# context# [context-value# & props#] & body#]
  `(defn ~name# [~@props#]
     (let [~context-value# (reagent.ratom/atom nil)]
       (fn [~@props#]
         [:<>
          [reagent-context.core/consumer
           {:context ~context#}
           (fn [v#]
             (reset! ~context-value# v#)
             nil)]
           ~@body#]))))
