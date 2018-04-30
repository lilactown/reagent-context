(ns context.core
  (:require [reagent.core :as r]
            ["react" :as react]
            [reagent-context.core :as context
             :refer [defconsumer]]))

(defonce my-context (context/create))

(defconsumer
  testing
  my-context
  [state & children]
  ;; (js/console.log state)
  (let [{:keys [color toggle testing]} @state]
    (into [:div {:style {:background-color color}}] children)))

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
           [:button {:on-click toggle} "color"]])]]))
  )

(defn toggle-testing [testing]
  (js/console.log testing)
  (if (= testing "green") "light-blue" "green"))

;; (defn magic-consumer [& children]
;;   (let [context-state (r/atom nil)]
;;     (fn [& children]
;;       (into
;;        [:<>
;;         [context/consumer {:context my-context}
;;          (fn [v] (reset! context-state v)
;;            nil)]
;;         [:div "Current color:" @context-state]]
;;        children))))

(defn app-2 []
  (let [color (r/atom "green")
        counter (r/atom 0)
        click-color! #(swap! color toggle-testing)
        click-inc! #(swap! counter inc)]
    (fn []
      ;; (js/console.log color)
      [context/provider {:context my-context :value @color}
       [context/consumer {:context my-context}
        (fn [color']
          [:div "Current (non-magic) color: " color'
           [:div "Count (non-magic): " @counter]])]
       [:button {:on-click click-color!} "color"]
       [:button {:on-click click-inc!} "increment"]])))

(defconsumer magic-consumer my-context
  [color counter]
  [:div
   [:div "Color: " @color]
   [:div "Counter: " @counter]])

(defn app-3 []
  (let [color (r/atom "green")
        counter (r/atom 0)
        click-color! #(swap! color toggle-testing)
        click-inc! #(swap! counter inc)]
    (fn []
      ;; (js/console.log color)
      [context/provider {:context my-context :value @color}
       [:div [magic-consumer counter]]
       [:button {:on-click click-color!} "color"]
       [:button {:on-click click-inc!} "increment"]])))

(defn start []
  (r/render [page] (.getElementById js/document "app"))
  (r/render [app-2] (.getElementById js/document "app2"))
  (r/render [app-3] (.getElementById js/document "app3")))


(defn ^:export init [] (start))
