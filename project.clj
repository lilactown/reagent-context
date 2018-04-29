;; this project.clj is only used for releases.
;; shadow-cljs.edn should be treated as the source of truth
;; for building & testing
(defproject lilactown/reagent-context "0.0.2"
  :license {:name "MIT"}
  :description "Easy access to React context in ClojureScript & Reagent"
  :url "https://github.com/Lokeh/reagent-context"
  :dependencies [[reagent "0.8.0"]]
  :source-paths ["src"])
