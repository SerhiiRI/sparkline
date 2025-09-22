(ns sparkline.core
  (:require [clojure.pprint :refer [cl-format]]))

(declare generate-bar)
(declare generate-horizontal-scale)
(declare generate-title)
(declare spark)
(declare vspark)

;;--------------------------------------------------------------------
;; Utils
;;--------------------------------------------------------------------

;; Geometric Shapes
(def unicode-geometric-shapes
  (map char (range 9632 9727)))

;; Box Drawing
(def unicode-box-drawing
  (map char (range 9472 9599)))

;; Block Element
(def unicode-box-element
  (map char (range 9600 9631)))

;; Mathematical symbols
(def unicode-math-symbols
  (map char (range 8704 8959)))

;; Arrows
(def unicode-arrows
  (map char (range 8592 8703)))

;;--------------------------------------------------------------------
;; Spark
;;--------------------------------------------------------------------

;; (char 9600) => \▀
;; (char 9620) => \▔
;; (char 9601) => \▁
;; (char 9602) => \▂
;; (char 9603) => \▃
;; (char 9604) => \▄
;; (char 9605) => \▅
;; (char 9606) => \▆
;; (char 9607) => \▇
;; (char 9608) => \█
;; (char 9135) => \⎯


(def ^:dynamic *ticks*
  "A simple-vector of characters for representation of sparklines.
  Default is [▁ ▂ ▃ ▄ ▅ ▆ ▇ █].

  Examples:

  (def ternary [-1 0 1 -1 1 0 -1 1 -1])

  (spark ternary)              => \"▁▄█▁█▄▁█▁\"

  (binding [*ticks* [\\_ \\- \\¯]]
    (spark ternary))           => \"_-¯_¯-_¯_\"

  (binding [*ticks* [\\▄ \\⎯ \\▀]]
    (spark ternary))           => \"▄⎯▀▄▀⎯▄▀▄\"
"
  (vector
    (char 9601) (char 9602) (char 9603)
    (char 9604) (char 9605) (char 9606)
    (char 9607) (char 9608)))


(defn spark
  "Generates a sparkline string for a list of real numbers.

  Usage: (spark <numbers> & {:keys [:min :max :key]})

  * <numbers> ::= <list> of <number>
  * :min      ::= { nil | <number> }, default is nil
  * :max      ::= { nil | <number> }, default is nil
  * :key      ::= <function>

  * <numbers> - data.
  * :min     - lower bound of output.
               'nil means the minimum value of the data.
  * :max     - upper bound of output.
               'nil means the maximum value of the data.
  * :key     - function for preparing data.

  Examples:

  (spark [1 0 1 0])     => \"█▁█▁\"
  (spark [1 0 1 0 0.5]) => \"█▁█▁▄\"
  (spark [1 0 1 0 -1])  => \"█▄█▄▁\"

  (spark [0 30 55 80 33 150])                 => \"▁▂▃▅▂█\"
  (spark [0 30 55 80 33 150] :min -100)       => \"▃▄▅▆▄█\"
  (spark [0 30 55 80 33 150] :max 50)         => \"▁▅██▅█\"
  (spark [0 30 55 80 33 150] :min 30 :max 80) => \"▁▁▄█▁█\"

  (spark [0 1 2 3 4 5 6 7 8] :key (fn [x] (Math/sin (* x Math/PI 1/4))))
  => \"▄▆█▆▄▂▁▂▄\"
  (spark [0 1 2 3 4 5 6 7 8] :key (fn [x] (Math/cos (* x Math/PI 1/4))))
  => \"█▆▄▂▁▂▄▆█\"

  "
  [numbers & {:keys [min max key]}]
  (if (empty? numbers)
    ""
    (let [numbers (cond->> numbers
                    key (map key)
                    min (map #(clojure.core/max % min))
                    max (map #(clojure.core/min % max)))
          min (if min min (reduce clojure.core/min numbers))
          max (if max max (reduce clojure.core/max numbers))]
      (when (< max min)
        (throw (ex-info (cl-format "max ~d < min ~d.") {:max max :min min})))
      (let [unit (/ (double (- max min)) (dec (count *ticks*)))
            unit (if (zero? unit) 1 unit)]
        (->> numbers
          (map (fn [n]
                 (conj 
                   (nth *ticks*
                     (.setScale
                       (new BigDecimal (/ (- n min) unit))
                       10 java.math.RoundingMode/HALF_UP)))))
          (apply str))))))

;;--------------------------------------------------------------------
;; Vspark
;;--------------------------------------------------------------------

;; (code-char 9615) => \▏
;; (code-char 9614) => \▎
;; (code-char 9613) => \▍
;; (code-char 9612) => \▌
;; (code-char 9611) => \▋
;; (code-char 9610) => \▊
;; (code-char 9609) => \▉
;; (code-char 9608) => \█
;; (code-char 9616) => \▐
;; (code-char 9621) => \▕

(def ^:dynamic *vticks*
  "A simple-vector of characters for representation of vartical
sparklines. Default is [▏ ▎ ▍ ▌ ▋ ▊ ▉ █].

Examples:

  ;; Japan GDP growth rate, annal
  ;; see. http://data.worldbank.org/indicator/NY.GDP.MKTP.KD.ZG
  (def growth-rate
    [[2007 2.192186]
     [2008 -1.041636]
     [2009 -5.5269766]
     [2010 4.652112]
     [2011 -0.57031655]
     [2012 1.945]])

  (vspark growth-rate :key second :labels (map first growth-rate))
  =>
  \"
  2007 ██████████████████████████████████▏
  2008 ███████████████████▏
  2009 ▏
  2010 █████████████████████████████████████████████
  2011 █████████████████████▏
  2012 █████████████████████████████████▏
       ├---------------------+---------------------┤
       -5.5269766    -0.4374323000000002    4.652112
  \"

  (binding [*vticks* [\\- \\0 \\+]]
    (vspark growth-rate 
      :key (fn [y-r] (let [r (second y-r)]
                      (cond
                        (= r 0) 0.0
                        (> r 0) 1.0
                        (< r 0) -1.0)))
      :labels (map first growth-rate)
      :size 1))
  =>
  \"
  2007 +
  2008 -
  2009 -
  2010 +
  2011 -
  2012 +
  \""
  (vector
    (char 9615) (char 9614) (char 9613)
    (char 9612) (char 9611) (char 9610)
    (char 9609) (char 9608)))

(defn vspark
  "
  Generates a vartical sparkline string for a list of real numbers.

  Usage: (vspark <numbers> & {:keys [:min :max :key :size :labels
                                     :title :scale :newline]

  * <numbers>  ::= <list> of <number>
  * :min       ::= { nil | <number> }, default is 'nil
  * :max       ::= { nil | <number> }, default is 'nil
  * :key       ::= <function>
  * :size      ::= <number>, must be 1+ default is 50
  * :labels    ::= <list> of <string>
  * :title     ::= <string>, default is NIL
  * :scale?    ::= <boolean>, default is 'true
  * :newline?  ::= <boolean>, default is 'true

  * <numbers> - data.
  * :min      - lower bound of output.
                'nil means the minimum value of the data.
  * :max      - upper bound of output.
                'nil means the maximum value of the data.
  * :key      - function for preparing data.
  * :size     - maximum number of output columns (contains label).
  * :labels   - labels for data.
  * :title    - If title is too big for size, it is not printed.
  * :scale?   - If 'true, output graph with scale for easy to see.
                If string length of min and max is too big for size,
                the scale is not printed.
  * :newline? - If 'true, output graph with newlines for easy to see.


  Examples:

  ;; Life expectancy by WHO region, 2011, bothsexes
  ;; see. http://apps.who.int/gho/data/view.main.690
  (def life-expectancies 
     [[\"Africa\" 56]
      [\"Americans\" 76]
      [\"South-East Asia\" 67]
      [\"Europe\" 76]
      [\"Eastern Mediterranean\" 68]
      [\"Western Pacific\" 76]
      [\"Global\" 70]])

  (vspark life-expectancies :key second :scale? nil :newline? nil)
  =>
  \"
  ▏
  ██████████████████████████████████████████████████
  ███████████████████████████▌
  ██████████████████████████████████████████████████
  ██████████████████████████████▏
  ██████████████████████████████████████████████████
  ███████████████████████████████████▏\"

  (vspark life-expectancies :min 50 :max 80
                            :key    second
                            :labels (map first life-expectancies)
                            :title \"Life Expectancy\")
  =>
  \"
                   Life Expectancy                                
                 Africa █████▏
              Americans ████████████████████████▏
        South-East Asia ███████████████▏
                 Europe ████████████████████████▏
  Eastern Mediterranean ████████████████▏
        Western Pacific ████████████████████████▏
                 Global ██████████████████▏
                        ├-------------+------------┤
                        50          65.0          80
  \"

  (vspark [0 1 2 3 4 5 6 7 8] :key (fn [x] (Math/sin (* x Math/PI 1/4)))
                              :size 20)
  \"
  ██████████▏
  █████████████████▏
  ████████████████████
  █████████████████▏
  ██████████▏
  ██▉
  ▏
  ██▉
  █████████▉
  ├--------+---------┤
  -1.0     0.0     1.0
  \"

  (vspark [0 1 2 3 4 5 6 7 8] :key (fn [x] (Math/sin (* x Math/PI 1/4)))
                              :size 10)
  =>
  \"
  █████▏
  ████████▏
  ██████████
  ████████▏
  █████▏
  █▏
  ▏
  █▏
  ████▏
  ├--------┤
  -1.0   1.0
  \"

  (vspark [0 1 2 3 4 5 6 7 8] :key (fn [x] (Math/sin (* x Math/PI 1/4)))
                              :size 1)
  =>
  \"
  ▌
  ▊
  █
  ▊
  ▌
  ▎
  ▏
  ▎
  ▌
  \""
  [numbers & {:keys [min max key size labels title scale? newline?]
              :or {size 50 scale? true newline? false}}]
  (assert (seqable? numbers))
  (assert (or (nil? min) (number? min)))
  (assert (or (nil? max) (number? max)))
  (assert (or (nil? key) (symbol? key) (fn? key)))
  (assert (>= size 1))
  (assert (or (nil? labels) (seqable? labels)))
  (if (empty? numbers)
    ""
    (let [numbers (cond->> numbers
                    key (map key)
                    min (map #(clojure.core/max % min))
                    max (map #(clojure.core/min % max)))

          min (if min min (reduce clojure.core/min numbers))
          max (if max max (reduce clojure.core/max numbers))
          _ (cond (< max min) (throw (ex-info (cl-format "max ~d < min ~d.") {:max max :min min})))
          max (if (= max min) (inc max) max)]
      
      (let [labels
            (when labels
              (let [diff (- (count numbers) (count labels))]
                (cond
                  (pos? diff) (concat labels (repeat diff ""))
                  (neg? diff) (drop-last (Math/abs diff) labels)
                  true labels)))

            max-lengeth-label
            (if-not labels 0
              (->> labels
                (map (fn [label] (count (str label))))
                (reduce clojure.core/max)))

            labels (when labels
                     (map (fn [label] (cl-format nil "~v@A " max-lengeth-label label)) labels))
            size (clojure.core/max 1 (- size 1 max-lengeth-label))]
        
        (let [num-content-ticks (dec (count *vticks*))
              unit (/ (- max min) (* size num-content-ticks))
              unit (if (zero? unit) 1 unit)
              result (new java.util.ArrayList)]

          (when title
            (if-let [ttl (generate-title title size max-lengeth-label)]
              (.add result ttl)))
          
          (doseq [[i n] (doall (map-indexed vector numbers))]
            (do
              (when labels
                (.add result (nth labels i)))
              (.add result (generate-bar n unit min max num-content-ticks))
              (.add result "\n")))

          (when scale?
            (if-let [scl (generate-horizontal-scale min max size max-lengeth-label)]
              (.add result scl)))

          (if newline?
            (do
              (.add result "\n")
              (cl-format nil "~{~A~}" result))
            (do 
              (clojure.string/trimr (cl-format nil "~{~A~}" result)))))))))

(defn- generate-bar [number unit min max num-content-ticks]
  (let [a (- number min)
        b (* unit num-content-ticks)
        units (if (zero? b) 0 (quot a b))
        frac (if (zero? b) 0 (mod a b))]
    (with-out-str
      (let [most-tick (nth *vticks* num-content-ticks)]
        (dotimes [i units] (print most-tick))
        (when-not (= number max)
          (print (nth *vticks* (if (zero? unit) 0 (quot frac unit)))))))))

(defn- generate-title [title size max-lengeth-label]
  (let [title-string (str title)
        mid (quot (- (if max-lengeth-label (+ 1 size max-lengeth-label) size)
                    (count title-string)) 2)]
    (when (pos? mid)
      (let [sp (repeat (if max-lengeth-label (+ 1 size max-lengeth-label) size) \space)]
       (cl-format nil "~A~%"
         (apply str
           (concat (take mid sp)
            title-string
            (drop (inc mid) sp))))))))

;; (char 9500) => \├
;; (char 9508) => \┤
(defn- generate-horizontal-scale [min max size max-lengeth-label]
  (let [min-string  (str (num min))
        max-string  (str (num max))
        num-padding (- size (count min-string) (count max-string))]
    (when (pos? num-padding)
      (let [mid        (/ (+ max min) 2)
            mid-string (str (double mid))
            num-indent (if (and max-lengeth-label (pos? max-lengeth-label))
                         (inc max-lengeth-label) 0)]
        (if (and (< (count mid-string) num-padding)
              (not= min mid)
              (not= mid max))
          ;; A. mid exist case:
          (cl-format nil "~V,0T~V,,,'-<~A~;~A~;~A~>~
                         ~%~V,0T~V<~A~;~A~;~A~>~%"
            num-indent size (char 9500) \+ (char 9508)
            num-indent size min-string mid-string max-string)
          ;; B. no mid exist case:
          (cl-format nil "~V,0T~V,,,'-<~A~;~A~>~
                          ~%~V,0T~V<~A~;~A~>~%"
            num-indent size (char 9500) (char 9508)
            num-indent size min-string max-string))))))


;;--------------------------------------------------------------------
;; waterfall (vspark)
;;--------------------------------------------------------------------

(defn waterfall
  "
  Generates vertical barchart for the number of elements used

  Usage: (waterfall <data> & {keys [:min :max :key :size
                                     :title :scale? :newline?]})

  * <data>     ::= <list> of <object>
  * :min       ::= { nil | <real-number> }, default is 'nil
  * :max       ::= { nil | <real-number> }, default is 'nil
  * :key       ::= <function>
  * :size      ::= <number>, must be 1+, default is 50
  * :title     ::= <object>, default is 'nil
  * :scale?    ::= <generalized-boolean>, default is 'true
  * :newline?  ::= <generalized-boolean>, default is 'true

  * :data      - data.
  * :min       - lower bound of output.
                 'nil means the minimum value of the data.
  * :max       - upper bound of output.
                 'nil means the maximum value of the data.
  * :key       - function for preparing data.
  * :size      - maximum number of output columns (contains label).
  * :title     - If title is too big for size, it is not printed.
  * :scale?    - If 'true, output graph with scale for easy to see.
                 If string length of min and max is too big for size,
                 the scale is not printed.
  * :newline?  - If 'true, output graph with newlines for easy to see.


  Examples:

  (waterfall
    (seq \"ababvbvbbababbvbbhhhdhhhbaeeidddd\") :size 18
                                                :title \"Char usage rate\")
  => 
  \"
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
  \"
  "
  [data & {:as margs}]
  (let [data (->> (frequencies data)
               (seq)
               (sort-by second)
               (reverse))]
    margs (dissoc margs :labels :key)
    (apply vspark data
      :labels (map first data)
      :key (fn [x] (second x))
      (apply concat margs))))

