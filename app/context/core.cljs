(ns context.core
  (:require [reagent.core :as r]
            ["react" :as react]
            [reagent-context.core :as context
             :refer [defconsumer]]))

(defonce my-context (context/create))

(defconsumer
  testing
  my-context
  [{:keys [color toggle]}]
  (into [:div {:style {:background-color color}}] "whatever"))

(defn toggle-color [{:keys [color] :as state}]
  (merge state (if (= color "red") {:color "blue"} {:color "red"})))

(defn page []
  (let [state (r/atom {:color "pink" :toggle nil})
        toggle #(swap! state toggle-color)]
    (swap! state assoc :toggle toggle)
    (fn []
      ;; (js/console.log @state)
      [context/provider {:context my-context :value @state}
       [testing "hi"]
       [context/consumer
        {:context my-context}
        (fn [{:keys [color toggle]}]
          [:div
           [:div color]
           [:button {:on-click toggle} "color"]])]
       [:h1 "hi"]
       [:h2 "hey"]]))
  )

(defn app-2 []
  (let [state (r/atom 0)
        click! #(swap! state inc)]
    (fn []
      [context/provider
       {:context my-context :value nil}
       [:div
        [:div "state: " @state
         [:button {:on-click click!} "+"]]]])))

(defn start []
  (r/render [page] (.getElementById js/document "app"))
  (r/render [app-2] (.getElementById js/document "app2")))


(defn ^:export init [] (start))
