(ns context.core
  (:require [reagent.core :as r]
            ["react" :as react]
            [reagent-context.core :as context
             :refer [defconsumer]]))

(defonce my-context (context/create))

(defconsumer
  testing
  my-context
  [{:keys [color toggle testing]} & children]
  (js/console.log testing)
  (into [:div {:style {:background-color color}}] children))

(defn toggle-color [{:keys [color] :as state}]
  (merge state (if (= color "red") {:color "blue"} {:color "red"})))

(defn page []
  (let [state (r/atom {:color "pink" :toggle nil :testing (r/atom "purple")})
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

(defn toggle-testing [testing]
  (js/console.log testing)
  (if (= testing "green") "light-blue" "green"))

(defn app-2 []
  (let [state (r/atom 0)
        other-state (r/atom "green")
        click! #(reset! other-state "red")
        click-inc! #(swap! state inc)]
    (fn []
      (js/console.log @other-state)
      [context/provider
       {:context my-context :value {:testing other-state}}
       [testing
        [:div
         [:div "state: " @state
          [:button {:on-click click-inc!} "+"]
          [:button {:on-click click!} "color"]]]]])))

(defn start []
  (r/render [page] (.getElementById js/document "app"))
  (r/render [app-2] (.getElementById js/document "app2")))


(defn ^:export init [] (start))
