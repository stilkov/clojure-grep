(ns sample.grep
  "A simple complete Clojure program."
  (:use [clojure.contrib.io :only [read-lines]])
  (:gen-class))

(defn numbered-lines [lines]
  (map vector (iterate inc 0) lines))

(defn grep-in-file [pattern file]
  {file (filter #(re-find pattern (second %)) (numbered-lines (read-lines file)))})

(defn grep-in-files [pattern files]
  (apply merge (map #(grep-in-file pattern %) files)))

(defn print-matches [matches]
  (doseq [[fname submatches] matches, [line-no, match] submatches]
    (println (str fname ":" line-no ":" match))))
	    
(defn -main [pattern & files]
  (if (or (nil? pattern) (empty? files))
    (println "Usage: grep <pattern> <file...>")
    (do 
      (println (format "grep started with pattern %s and file(s) %s" pattern (apply str (interpose ", " files))))
      (print-matches (grep-in-files (re-pattern pattern) files))
      (println "Done."))))
