(defproject parallel-consumers "1.0.0-SNAPSHOT"
  :description "POC implementation of a BP warm up helper"
  :dependencies [
		 [org.clojure/clojure "1.3.0"]
		 [org.clojure/java.jdbc "0.2.3"]
		 [mysql/mysql-connector-java "5.1.6"]
		 [org.clojure/tools.cli "0.2.2"]
		 ]
  :main parallel-consumers.core
  )
