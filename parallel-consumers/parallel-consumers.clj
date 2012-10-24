;; this is just a proof of concept!!
;; some things it is missing now:
;; - What happens if the file does not exist?
;; - What happens if the file goes away/changes size/name?
;; - What happens if the queue grows beyond size X because the consumers are slow?
;; - Consumer now just prints to stdout, eventually it should run a mysql query
;; - Parametrize everything that's parametrizable (at least, # of consumers, jdbc driver + url)

;; I'll use a LinkedBlockingDeque to send data to consumers
(defn new-q [] (java.util.concurrent.LinkedBlockingDeque.))

;; queue API
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

(defn parse-file!
  "reads a file and adds each line to the queue"
  [file-name]
  (with-open [reader (java.io.BufferedReader. (java.io.FileReader. file-name))]
    (doseq [line (line-seq reader)] (add! global-queue line)))
  )


;; the single-threaded consumer

(defn consume
  "gets a line and prints it to standard output"
  [line]
  (println line))

;; int main()

;; open the file, fire up the single threaded producer
(future (parse-file! "/var/log/system.log"))

(for [x (range 2)] #(while true (consume (take! global-queue))))

;; fire up 20 parallel single-threaded consumers
;; (for [x (range 20)] #(while true (consume (take! global-queue))))

