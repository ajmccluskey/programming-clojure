; START: namespace
(ns reader.snake
  (:import (java.awt Color Dimension) 
	   (javax.swing JPanel JFrame Timer JOptionPane)
           (java.awt.event ActionListener KeyListener))
  (:use examples.import-static))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)
; END: namespace

(def width 80)
(def height 60)
(def point-size 10)
(def turn-millis 75)
(def win-length 5)
(def dirs {VK_LEFT [-1 0]
           VK_RIGHT [1 0]
           VK_UP [0 1]
           VK_DOWN [0 -1]})

(defn add-points
  "Adds the components of the argument vectors, which should all be of the same dimensions"
  [& pts]
    (vec (apply map + pts)))

(defn point-to-screen-rect
  "Converts a 2d vector from game 'points' to pixels"
  [pt]
    (map #(* point-size %) [(pt 0) (pt 1) 1 1]))

(defn create-apple
  "Creates a prize that has a location, colour, and type"
  []
    {:location [(rand-int width) (rand-int height)]
     :colour (Color. 210 50 90)
     :type :apple})

(defn create-snake
  "Creates a snake containing a list of body points, a direction, colour, and type"
  []
    {:body (list [1 1])
     :dir (get dirs VK_RIGHT)
     :type :snake
     :colour (Color. 15 160 70)})

(defn move-snake
  "Moves the snake given one game tick."
  [{:keys [body dir] :as snake} & grow]
    (assoc snake :body (cons (add-points (first body) dir)
                             (if grow body (butlast body)))))

(defn won? [{body :body}] (>= (count body) win-length))

(defn head-overlaps-body?
  "Returns whether or not the head of the snake has crossed its body"
  [{[head & body] :body}]
    (contains? (set body) head))

(def lose head-overlaps-body?)

(defn eats?
  "Whether or not the snake is eating an apple this tick"
  [{[snake-head] :body} {apple :location}]
    (= snake-head apple))

(defn turn
  "Gives the snake a new direction"
  [snake new-dir]
    (assoc snake :dir new-dir))

(defn hello []
  (println "Hello, world!"))

; TODO: implement the Snake!
