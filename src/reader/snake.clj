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
           VK_UP [0 -1]
           VK_DOWN [0 1]})

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
     :color (Color. 210 50 90)
     :type :apple})

(defn create-snake
  "Creates a snake containing a list of body points, a direction, colour, and type"
  []
    {:body (list [1 1])
     :dir (dirs VK_RIGHT)
     :type :snake
     :color (Color. 15 160 70)})

(defn move-snake
  "Moves the snake given one game tick."
  [{:keys [body dir] :as snake} & grow]
    (assoc snake :body (cons (add-points (first body) dir)
                             (if grow body (butlast body)))))

(defn win? [{body :body}] (>= (count body) win-length))

(defn head-overlaps-body?
  "Returns whether or not the head of the snake has crossed its body"
  [{[head & body] :body}]
    (contains? (set body) head))

(def lose? head-overlaps-body?)

(defn eats?
  "Whether or not the snake is eating an apple this tick"
  [{[snake-head] :body} {apple :location}]
    (= snake-head apple))

(defn turn
  "Gives the snake a new direction"
  [snake new-dir]
    (assoc snake :dir new-dir))

(defn reset-game
  "Resets the game"
  [snake apple]
    (dosync (ref-set apple (create-apple))
            (ref-set snake (create-snake)))
  nil)

(defn update-direction
  "Updates the direction of the given snake"
  [snake newdir]
    (when newdir (dosync (alter snake turn newdir))))

(defn update-positions
  "Updates the positions of the snake depending on apple"
  [snake apple]
    (dosync
      (if (eats? @snake @apple)
        (do (ref-set apple (create-apple))
            (alter snake move-snake :grow))
        (alter snake move-snake)))
    nil)

(defn fill-point
  "Draws a rectangle on the screen for a single point"
  [g pt color]
    (let [[x y width height] (point-to-screen-rect pt)]
      (.setColor g color)
      (.fillRect g x y width height)))

(defmulti paint
  "Paints a game object"
  (fn [g object & _] (:type object)))

(defmethod paint :apple [g {:keys [location color]}]
  (fill-point g location color))
  
(defmethod paint :snake [g {:keys [body color]}]
  (doseq [point body]
    (fill-point g point color)))

(defn game-panel
  "Returns an instance of JPanel with relevant handlers."
  [frame snake apple]
    (proxy [JPanel ActionListener KeyListener] []
      (paintComponent [g]
        (proxy-super paintComponent g)
        (paint g @snake)
        (paint g @apple))
      (actionPerformed [e]
        (update-positions snake apple)
        (when (lose? @snake)
          (reset-game snake apple)
          (JOptionPane/showMessageDialog frame "You lose!"))
        (when (win? @snake)
          (reset-game snake apple)
          (JOptionPane/showMessageDialog frame "You win!"))
        (.repaint this))
      (keyPressed [e]
        (update-direction snake (dirs (.getKeyCode e))))
      (getPreferredSize []
        (Dimension. (* (inc width) point-size)
                    (* (inc height) point-size)))
      (keyReleased [e])
      (keyTyped [e])))

(defn game
  "Function to start the game"
  []
    (let [snake (ref (create-snake))
          apple (ref (create-apple))
          frame (JFrame. "Snaaaaake")
          panel (game-panel frame snake apple)
          timer (Timer. turn-millis panel)]
      (doto panel
        (.setFocusable true)
        (.addKeyListener panel))
      (doto frame
        (.add panel)
        (.pack)
        (.setVisible true))
      (.start timer)
      [snake apple timer]))
