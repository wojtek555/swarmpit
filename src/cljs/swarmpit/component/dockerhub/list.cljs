(ns swarmpit.component.dockerhub.list
  (:require [material.icon :as icon]
            [material.component :as comp]
            [material.component.list.basic :as list]
            [material.component.list.util :as list-util]
            [swarmpit.component.mixin :as mixin]
            [swarmpit.component.state :as state]
            [swarmpit.ajax :as ajax]
            [swarmpit.routes :as routes]
            [swarmpit.storage :as storage]
            [swarmpit.url :refer [dispatch!]]
            [sablono.core :refer-macros [html]]
            [cljs.core :as core]
            [rum.core :as rum]
            [swarmpit.component.common :as common]))

(enable-console-print!)

(def render-metadata
  {:table {:summary [{:name      "Username"
                      :render-fn (fn [item] (:username item))}
                     {:name      "Name"
                      :render-fn (fn [item] (:name item))}
                     {:name      "Company"
                      :render-fn (fn [item] (:company item))}
                     {:name      "Name"
                      :render-fn (fn [item] (if (:public item) "yes" "no"))}]}
   :list  {:primary-key (fn [item] (:username item))}})

(defn- onclick-handler
  [item]
  (dispatch! (routes/path-for-frontend :dockerhub-user-info {:id (:_id item)})))

(defn- users-handler
  []
  (ajax/get
    (routes/path-for-backend :dockerhub-users)
    {:state      [:loading?]
     :on-success (fn [{:keys [response]}]
                   (state/update-value [:items] response state/form-value-cursor))}))

(defn form-search-fn
  [event]
  (state/update-value [:filter :query] (-> event .-target .-value) state/form-state-cursor))

(def form-actions
  [{:button (comp/icon-button
              {:color   "inherit"
               :onClick #(dispatch! (routes/path-for-frontend :dockerhub-user-create))} icon/add-circle)
    :name   "Add user"}])


(defn- init-form-state
  []
  (state/set-value {:loading? false
                    :filter   {:query ""}} state/form-state-cursor))

(def mixin-init-form
  (mixin/init-form
    (fn [_]
      (init-form-state)
      (users-handler))))

(rum/defc form < rum/reactive
                 mixin-init-form
                 mixin/subscribe-form
                 mixin/focus-filter [_]
  (let [{:keys [items]} (state/react state/form-value-cursor)
        {:keys [loading? filter]} (state/react state/form-state-cursor)
        filtered-items (-> (core/filter #(= (:owner %) (storage/user)) items)
                           (list-util/filter (:query filter)))]
    (common/list "Docker Hub Accounts"
                 items
                 filtered-items
                 render-metadata
                 onclick-handler)))
