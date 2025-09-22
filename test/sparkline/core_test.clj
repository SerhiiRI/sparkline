(ns sparkline.core-test
  (:require
   [clojure.test :refer :all]
   [sparkline.core :refer :all]))


;;--------------------------------------------------------------------
;; Spark Tests
;;--------------------------------------------------------------------

(deftest spark-base
  (is (= (spark '(1 5 22 13 5))
        "▁▂█▅▂"))
  (is (= (spark '(5.5 20))
        "▁█"))
  (is (= (spark '(1 2 3 4 100 5 10 20 50 300))
        "▁▁▁▁▃▁▁▁▂█"))
  (is (= (spark '(1 50 100))
        "▁▄█"))
  (is (= (spark '(2 4 8))
        "▁▃█"))
  (is (= (spark '(1 2 3 4 5))
        "▁▂▄▆█"))
  (is (= (spark '(0 30 55 80 33 150))
        "▁▂▃▄▂█")))


(deftest spark-limit-case
  ;; nil
  (is (= (spark nil)
        ""))
  ;; empty
  (is (= (spark '())
        ""))
  ;; singleton
  (is (= (spark '(42))
        "▁"))
  ;; constant
  (is (= (spark '(42 42))
        "▁▁")))


(deftest spark-min-max
  (is (= (spark '(0 30 55 80 33 150) :min -100)
        "▃▄▅▆▄█"))
  (is (= (spark '(0 30 55 80 33 150) :max 50)
        "▁▅██▅█"))
  (is (= (spark '(0 30 55 80 33 150) :min 30 :max 80)
        "▁▁▄█▁█")))


(deftest spark-numeric-ranges
  ;; spark. double-float, minus
  (is (= (spark '(1.000000005 0.000000005 1.0))
        "█▁▇"))
  (is (= (spark '(-1 0 -1))
        "▁█▁"))
  (is (= (spark '(-1.000000005 0.000000005 -1.0))
        "▁█▁")))


;;; *ticks*

(def ^:private ternary '(-1 0 1 -1 1 0 0 -1 1 1 0))

(deftest spark-tick-override
  ;; *ticks* overriding
  (is (= (spark ternary)
        "▁▄█▁█▄▄▁██▄"))
  (is (= (binding [*ticks* '(\_ \- \¯)]
           (spark ternary))
        "_-¯_¯--_¯¯-"))
  (is (= (binding [*ticks* '(\▄ \⎯ \▀)]
           (spark ternary))
        "▄⎯▀▄▀⎯⎯▄▀▀⎯"))
  (is (= (binding [*ticks* '(\E \O)]
           (spark '(4 8 15 22 42) :key (fn [n] (mod n 2))))
        "EEOEE")))


(defn- fib [n] 
  (apply +
    (take n 
      (map first (iterate (fn [[a b]] [b (+ a b)]) [0 1])))))

(deftest spark-key-arg
  ;; :key arg
  (is (= (spark (range 0 51)
           :key (fn [x] (Math/sin (* x Math/PI 1/4))))
        "▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█"))

  (is (= (spark (range 0 51)
           :key (fn [x] (Math/cos (* x Math/PI 1/4))))
        "█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄▂▁▂▄▆█▆▄"))

  (is (= (spark (range 0 51)
           :key (fn [x] (Math/abs (* x Math/PI 1/4))))
        "▁▁▁▁▁▁▁▁▂▂▂▂▂▂▂▃▃▃▃▃▃▃▄▄▄▄▄▄▄▅▅▅▅▅▅▅▆▆▆▆▆▆▆▇▇▇▇▇▇▇█"))

  (is (= (spark (range 1 7))                             "▁▂▃▅▆█"))
  (is (= (spark (range 1 7) :key (fn [e] (Math/log e)))   "▁▃▅▆▇█"))
  (is (= (spark (range 1 7) :key (fn [e] (Math/sqrt e)))  "▁▃▄▅▆█"))
  (is (= (spark (range 1 7) :key (fn [e] (Math/exp e)))   "▁▁▁▁▃█"))
  (is (= (spark (range 1 7) :key fib)                    "▁▁▂▃▅█"))
  (is (= (spark (range 0 15) :key fib)          "▁▁▁▁▁▁▁▁▁▁▂▂▃▅█")))


(defn look-bits [n]
  (spark (map (fn [e] (Integer/parseInt (str e))) 
           (Integer/toBinaryString n))))

(deftest spark-various
  (is (= (look-bits 42) "█▁█▁█▁"))
  (is (= (look-bits 43) "█▁█▁██"))
  (is (= (look-bits 44) "█▁██▁▁"))
  (is (= (look-bits 45) "█▁██▁█")))


;;--------------------------------------------------------------------
;; Vspark Tests
;;--------------------------------------------------------------------

(deftest vspark-base
  (is (= (vspark nil)
        ""))
  ;; empty
  (is (= (vspark '())
        ""))
  ;; singleton
  (is (= (vspark '(1))
        "▏
├-----------------------+-----------------------┤
1                      1.5                      2"))
  ;; constant
  (is (= (vspark '(1 1))
        "▏
▏
├-----------------------+-----------------------┤
1                      1.5                      2"))


  (is (= (vspark '(0 30 55 80 33 150))
        "▏
█████████▊
█████████████████▉
██████████████████████████▏
██████████▊
█████████████████████████████████████████████████
├-----------------------+-----------------------┤
0                     75.0                    150")))


(deftest vspark-min-max-args

  (is (vspark '(0 30 55 80 33 150) :min -100)
    "███████████████████▋
█████████████████████████▌
██████████████████████████████▍
███████████████████████████████████▎
██████████████████████████▏
█████████████████████████████████████████████████
├-----------------------+-----------------------┤
-100                   25.0                   150")

  (is (= (vspark '(0 30 55 80 33 150) :max 50)
        "▏
█████████████████████████████▍
█████████████████████████████████████████████████
█████████████████████████████████████████████████
████████████████████████████████▍
█████████████████████████████████████████████████
├-----------------------+-----------------------┤
0                     25.0                     50"))

  (is (= (vspark '(0 30 55 80 33 150) :min 30 :max 80)
        "▏
▏
████████████████████████▌
█████████████████████████████████████████████████
██▉
█████████████████████████████████████████████████
├-----------------------+-----------------------┤
30                     55.0                    80")))


(deftest vspark-labels
  (is (= (vspark '(1 0 0.5) :labels '("on" "off" "unknown")
           :size 1
           :scale? nil)
        "     on █
    off ▏
unknown ▌"))

  (is (= (vspark '(1 0 0.5) :labels '("on" "off")
           :size 1
           :scale? nil) 
        " on █
off ▏
    ▌"))

  (is (= (vspark '(1 0) :labels '("on" "off" "unknown")
           :size 1
           :scale? nil)
        " on █
off ▏")))


(deftest vspark-key-size-scale
  ;; key, size, scale args
  (is (= (vspark '(0 1 2 3 4 5 6 7 8) :key (fn [x] (Math/sin (* x Math/PI 1/4))))
        "████████████████████████▌
█████████████████████████████████████████▊
█████████████████████████████████████████████████
█████████████████████████████████████████▊
████████████████████████▌
███████▎
▏
███████▎
████████████████████████▌
├-----------------------+-----------------------┤
-1.0                    0.0                   1.0"))

  (is (= (vspark '(0 1 2 3 4 5 6 7 8) :key (fn [x] (Math/sin (* x Math/PI 1/4)))
           :size 10)
        "████▌
███████▋
█████████
███████▋
████▌
█▍
▏
█▍
████▌
├-------┤
-1.0  1.0"))

  (is (= (vspark (range 0 15) :key fib)
        "▏
▏
▏
▎
▍
▌
▉
█▋
██▋
████▍
███████▏
███████████▌
██████████████████▋
██████████████████████████████▎
█████████████████████████████████████████████████
├-----------------------+-----------------------┤
0                    304.5                    609"))

  (is (= (vspark '(0 1 2 3 4 5 6 7 8) :key (fn [x] (Math/sin (* x Math/PI 1/4)))
           :size 20)
        "█████████▌
████████████████▎
███████████████████
████████████████▎
█████████▌
██▊
▏
██▊
█████████▌
├--------+--------┤
-1.0     0.0    1.0")))


;; Life expectancy by WHO region, 2011, bothsexes
;; see. http://apps.who.int/gho/data/view.main.690
(def life-expectancies
  '(("Africa" 56)
    ("Americans" 76)
    ("South-East Asia" 67)
    ("Europe" 76)
    ("Eastern Mediterranean" 68)
    ("Western Pacific" 76)
    ("Global" 70)))

(deftest vspark-newline-scale-labels
  (testing "vspark. newline? scale? labels arguments"
    (is (= (vspark life-expectancies :key second)
          "▏
█████████████████████████████████████████████████
██████████████████████████▉
█████████████████████████████████████████████████
█████████████████████████████▍
█████████████████████████████████████████████████
██████████████████████████████████▍
├-----------------------+-----------------------┤
56                     66.0                    76"))

    ;; newline?
    (is (= (vspark life-expectancies :key second :scale? nil :newline? nil)
          "▏
█████████████████████████████████████████████████
██████████████████████████▉
█████████████████████████████████████████████████
█████████████████████████████▍
█████████████████████████████████████████████████
██████████████████████████████████▍"))

    ;; scale?
    (is (= (vspark life-expectancies :key second :scale? nil)
          "▏
█████████████████████████████████████████████████
██████████████████████████▉
█████████████████████████████████████████████████
█████████████████████████████▍
█████████████████████████████████████████████████
██████████████████████████████████▍"))

    ;; labels
    (is (= (vspark life-expectancies :key second :labels (map first life-expectancies))
          "               Africa ▏
            Americans ████████████████████████████
      South-East Asia ███████████████▍
               Europe ████████████████████████████
Eastern Mediterranean ████████████████▊
      Western Pacific ████████████████████████████
               Global ███████████████████▋
                      ├-------------+------------┤
                      56          66.0          76"))

    ;; title
    (is (= (vspark life-expectancies
             :min 50 :max 80
             :key    second
             :labels (map first life-expectancies)
             :title "Life Expectancy")
          "                 Life Expectancy                                
               Africa █████▋
            Americans ████████████████████████▎
      South-East Asia ███████████████▉
               Europe ████████████████████████▎
Eastern Mediterranean ████████████████▊
      Western Pacific ████████████████████████▎
               Global ██████████████████▋
                      ├-------------+------------┤
                      50          65.0          80"))))


;;====================================================================

(deftest waterfall-various
  (is (= (waterfall
           (seq "ababvbvbbababbvbbhhhdhhhbaeeidddd")
           :size 18
           :title "Char usage rate")
        " Char usage rate                
b ████████████████
h ████████▏
d ██████▍
a ██████▍
v ███▎
e █▋
i ▏
  ├-------+------┤
  1     6.0     11"))
  (is (= (waterfall
           (seq "ababvbvbbababbvbbhhhdhhhbaeeidddd")
           :size 1
           :title "Char usage rate")
        "b █
h ▌
d ▍
a ▍
v ▎
e ▏
i ▏"))
  (is
    (= (waterfall
         (mapcat
           (fn [[country n]] (repeat n country))
           life-expectancies)
         :size 40
         :scale? false)
      "      Western Pacific ██████████████████
               Europe ██████████████████
            Americans ██████████████████
               Global ████████████▋
Eastern Mediterranean ██████████▊
      South-East Asia █████████▉
               Africa ▏")))


