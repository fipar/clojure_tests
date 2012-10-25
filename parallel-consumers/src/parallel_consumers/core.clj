(ns parallel-consumers.core)
(require '[clojure.java.jdbc :as sql])
(use '[clojure.tools.cli :only [cli]])

;; a single threaded producer reads a text file, enqueues its lines, and single threaded consumers execute concurrently
;; and insert the lines into a database. 
;; this is just a proof of concept!!
;; some things it is missing now:
;; - What happens if the file does not exist?
;; - What happens if the file goes away/changes size/name?
;; - What happens if the queue grows beyond size X because the consumers are slow?
;; - Make it exit gracefully after every future is done

;; I'll use a LinkedBlockingDeque to send data to consumers
(defn new-q [] (java.util.concurrent.LinkedBlockingDeque.))

;; queue 'API'
(defn add!
  "add data to the back of queue"
  [queue data]
  (.offer queue data) queue)

(defn take!
  "takes data from the front of queue, blocking if queue is empty"
  [queue] (.take queue))

;; define the queue

(def global-queue (new-q))

;; the single-threaded producer

(defn producer!
  "reads a file and adds each line to the queue"
  [file-name]
  (with-open [reader (java.io.BufferedReader. (java.io.FileReader. file-name))]
    (doseq [line (line-seq reader)] (add! global-queue line)))
  )

(defn init-db!
  "initializes the database connection, which is a global variable"
   [options]
   (def mysql-db {:subprotocol "mysql"
		  :subname (:url options)
		  :user (:user options)
		  :password (:passwd options)})  
  )

;; the single-threaded consumer
(defn consumer!
  "infinite loop to read a line from the queue and write it to mysql"
  []
  ((sql/with-connection mysql-db
     (loop []
       (do
         (sql/insert-records :log {:bag (take! global-queue)})
	 (recur)))))
  )

;; ;;;;;;;;;;;;;;

(defn -main [& args]
  (do
    (let [[options args banner]
	  (cli args
	       ["-file"]
	       ["-url" :default "//127.0.0.1:5527/test"]
	       ["-user" :default "msandbox"]
	       ["-passwd" :default "msandbox"]
	       ["-threads" :default 5])]
      (when
	  (nil? (:file options))
	(do
	  (println "-file is mandatory")
	  (System/exit 0)))
      (init-db! options)
      (future (producer! (:file options)))
      (for [x (range (Integer/parseInt (:threads options)))] (future (consumer!))))))
    
