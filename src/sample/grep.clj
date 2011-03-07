(ns sample.grep
  "A simple complete Clojure program."
  (:use [clojure.contrib.command-line])
  (:use [clojure.contrib.io :only [read-lines]])
  (:import [java.io File])
  (:gen-class))

;; by Craig Andera
(defn dir-descendants [dir]
  (let [children (.listFiles (File. dir))]
    (lazy-cat 
     (map (memfn getPath) (filter (memfn isFile) children)) 
     (mapcat dir-descendants 
	     (map (memfn getPath) (filter (memfn isDirectory) children))))))


(defn numbered-lines [lines]
  (map vector (iterate inc 0) lines))

(defn grep-in-file [pattern file]
  {file (vec (filter #(re-find pattern (second %)) (numbered-lines (read-lines file))))})

(defn expand-file
  [file]
  (if (.isDirectory (java.io.File. file))
    (dir-descendants file)
    file))

(defn all-files
  [files]
  (if (string? files)
    (expand-file files)
    (flatten (map expand-file files))))

(defn grep-in-files [pattern files & {:keys [parallel]}]
  (let [map-fn (if parallel pmap map)]
    (apply merge (map-fn #(grep-in-file pattern %) (all-files files)))))

(defn print-matches [matches]
  (doseq [[fname submatches] matches
	  [line-no match] submatches]
    (printf "%s:%s:%s\n" fname line-no match)))

(defn -main [& args]
  (with-command-line args
    "Simple grep, written in Clojure"
    [[parallel? p? "Run in parallel" false]
     remaining]
    (let [[pattern & files] remaining]
      (if (or (nil? pattern) (empty? files))
	(println "Usage: grep <pattern> <file...>")
	(do 
	  (.println *err* (str "Running with pattern " pattern
			       ", parallel set to " parallel?
			       " and files "
			       (apply str (interpose " " files))))
	  (print-matches (grep-in-files (re-pattern pattern) files :parallel parallel?))
	  (shutdown-agents)
	  (.println *err* "Done."))))))