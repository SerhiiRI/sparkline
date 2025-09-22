<div align="center">
<img width="30%" src="./logo/sparkline.png">
</div>
<br/>


Clojure port of the great Common Lisp library [cl-spark](https://github.com/tkych/cl-spark), which generates sparklines like "▁▂▅▃▂▇".
This is a Clojure implementation inspired by Zach Holman's [spark](https://github.com/holman/spark) and Gil Gonçalves' [vspark](https://github.com/LuRsT/vspark), with some additional features.

## Features

- Generate Unicode sparklines from numerical data
- Render Vertical Spark and Watterfall for numerical or categorical outputs
- Output customization

## Installation

```clojure
;; deps.edn
{org.clojars.funkcjonariusze/sparkline {:mvn/version "1.0.0"}}
;; leiningen
[org.clojars.funkcjonariusze/sparkline "1.0.0"]
```

## Usage

```clojure
(require [sparkline.core :refer [spark vspark watterfall]])

;;; Spark
(spark [1 1 2 3 5 8])  => "▁▁▂▃▅█"

;; float, minus
(spark [1 0 1 0])    => "█▁█▁"
(spark [1 0 1 0 0.5]) => "█▁█▁▄"
(spark [1 0 1 0 -1]) => "█▄█▄▁"

;; min, max
(spark [0 30 55 80 33 150])                 => "▁▂▃▅▂█"
(spark [0 30 55 80 33 150] :min -100)       => "▃▄▅▆▄█"
(spark [0 30 55 80 33 150] :max 50)         => "▁▅██▅█"
(spark [0 30 55 80 33 150] :min 30 :max 80) => "▁▁▄█▁█"

;; key
(spark [0 1 2 3 4 5 6 7 8] :key (fn [x] (Math/sin (* x Math/PI 1/4))))
=> "▄▆█▆▄▂▁▂▄"
(spark [0 1 2 3 4 5 6 7 8] :key (fn [x] (Math/cos (* x Math/PI 1/4))))
=> "█▆▄▂▁▂▄▆█"

;; in function
(defn look-bits [n]
  (spark (mapv (comp Integer/parseInt str) (Integer/toString n 2))))

(look-bits 42) => "█▁█▁█▁"
(look-bits 43) => "█▁█▁██"
(look-bits 44) => "█▁██▁▁"
(look-bits 45) => "█▁██▁█"

;; *ticks*
(def ternary [-1 0 1 -1 1 0 -1 1 -1])
(spark ternary)              => "▁▄█▁█▄▁█▁"

(binding [sparkline.core/*ticks* [\_ \- \¯]]
  (spark ternary))           => "_-¯_¯-_¯_"

(binding [sparkline.core/*ticks* [\▄ \⎯ \▀]]
  (spark ternary))           => "▄⎯▀▄▀⎯▄▀▄"


;;; Vspark

;; Life expectancy by WHO region, 2011, bothsexes
;; see. http://apps.who.int/gho/data/view.main.690
(def life-expectancies
  [["Africa" 56]
   ["Americans" 76]
   ["South-East Asia" 67]
   ["Europe" 76]
   ["Eastern Mediterranean" 68]
   ["Western Pacific" 76]
   ["Global" 70]])

(vspark life-expectancies :key second)
=>
"
▏
█████████████████████████████████████████████████
██████████████████████████▉
█████████████████████████████████████████████████
█████████████████████████████▍
█████████████████████████████████████████████████
██████████████████████████████████▍
├-----------------------+-----------------------┤
56                     66.0                    76
"

(vspark life-expectancies
  :key second
  :min 50 :max 80
  :labels (map first life-expectancies)
  :title "Life Expectancy")
=>
"
				 Life Expectancy
			   Africa █████▋
			Americans ████████████████████████▎
	  South-East Asia ███████████████▉
			   Europe ████████████████████████▎
Eastern Mediterranean ████████████████▊
	  Western Pacific ████████████████████████▎
			   Global ██████████████████▋
					  ├-------------+------------┤
					  50          65.0          80
"

;; labels, size
(vspark [1 0 0.5] :labels ["on" "off" "unknown"] :size 1)
=>
"
	 on █
	off ▏
unknown ▌
"

(vspark [1 0 0.5] :labels ["on" "off"] :size 1)
=>
"
 on █
off ▏
	▌
"

(vspark [1 0] :labels ["on" "off" "unknown"] :size 1)
=>
"
 on █
off ▏
"

;; auto-scale
(vspark [0 1 2 3 4 5 6 7 8]
  :key (fn [x] (Math/sin (* x Math/PI 1/4)))
  :size 20)
=>
"
█████████▌
████████████████▎
███████████████████
████████████████▎
█████████▌
██▊
▏
██▊
█████████▌
├--------+--------┤
-1.0     0.0    1.0
"

(vspark [0 1 2 3 4 5 6 7 8]
  :key (fn [x] (Math/sin (* x Math/PI 1/4)))
  :size 10)
=>
"
████▌
███████▋
█████████
███████▋
████▌
█▍
▏
█▍
████▌
├-------┤
-1.0  1.0
"
(vspark [0 1 2 3 4 5 6 7 8]
  :key (fn [x] (Math/sin (* x Math/PI 1/4)))
  :size 7))
=>
"
███▏
█████▏
██████
█████▏
███▏
▉
▏
▉
██▉
"

;; Watterfall

(watterfall
  (seq "ababvbvbbababbvbbhhhdhhhbaeeidddd")
  :size 18
  :title "Char usage rate")
=>
"
 Char usage rate
b ████████████████
h ████████▏
d ██████▍
a ██████▍
v ███▎
e █▋
i ▏
  ├-------+------┤
  1     6.0     11
"
```

# License

This project is licensed under the Eclipse Public License (EPL) v2.
