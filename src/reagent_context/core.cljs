(ns reagent-context.core
  (:require [reagent.core]
            ["react" :as react]
            [goog.object :as gobject]) 
  (:require-macros [reagent-context.core]))

(defn- map->js [x]
  (let [m (js-obj)]
    (doseq [[k v] x]
      (gobject/set m (key->js k) v))
    m))

(defn child-as-fn
  "Provides interop with a React component that takes a render function as its
   child.

   If you pass props in, it applies the equivalent of the #js reader to them.
   You must convert any nested data structures to a JS obj if desired."
  ([Component render]
   (reagent.core/create-element
    Component
    #js {:children
         (fn [& v]
           (reagent.core/as-element (apply render v)))}))
  ([Component props render]
   (reagent.core/create-element
    Component
    (-> {:children
         (fn [& v]
           (reagent.core/as-element (apply render v)))}
        (merge props)
        (map->js)))))

(defprotocol IContext
  "Provides an interface for obtaining provider and consumer components
   of a particular React context"
  (-provider [this])
  (-consumer [this]))

(deftype Context [instance]
  IContext
  (-provider [this] (.-Provider instance))
  (-consumer [this] (.-Consumer instance)))

(defn create
  "Creates a new React context"
  ([] (Context. (.createContext react))))

(comment (create)

         (-provider (create))

         (-consumer (create))

         )

(defn interop
  [provider consumer]
  (->Context
   #js {"Provider" provider
        "Consumer" consumer}))

(comment (interop "foo" "bar")

         (-provider (interop "foo" "bar"))

         (-consumer (interop "foo" "bar"))
         )


(defn provider
  "A Reagent component that serves as a provider for the provided context.

   (def my-context (create))

   (defn app []
    [provider {:context my-context
               :value \"initial state\"}
     [:div \"children\"]])"
  [{:keys [context value]} & children]
  (reagent.core/create-element
   (-provider context)
   #js {:value value
        :children (reagent.core/as-element (into [:<>] children))}))

(defn consumer
  "A Reagent component that serves as a consumer for the provided context.
   Takes a render function as it's second parameter that will be called with
   the context value, and should return hiccup syntax.

   (def my-context (create))

   (defn my-component []
    [consumer {:context my-context}
     (fn [context-state]
      [:div \"The state is: \" context-state])])"
  [{:keys [context]} render-fn]
  (child-as-fn
    (-consumer context)
    render-fn))
