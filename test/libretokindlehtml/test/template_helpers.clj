(ns libretokindlehtml.test.template_helpers
  (:require [clojure.test :refer :all]))

; This file tests the private helper functions in
; novel.clj and libreoffice.clj. There are two ways
; you can test private functions:
; 1) Use (ns-resolve 'enclosing-namespace 'function-name)
;    to get references to the functions and save them
;    somewhere. You might make a :once fixture that
;    defs the functions in the test namespace.
; 2) What I'm doing here: define the tests inside the file
;    where the functions are defined, using the test's (with-test)
;    macro, which attaches the tests to the function as metadata.
;    Then make one test inside the testing file (here) that calls
;    (run-tests 'the.namespace.of.the.private.fns). This is so
;    lein test will call them.

(deftest test-template-helpers
  (run-tests 'libretokindlehtml.novel 
             'libretokindlehtml.libreoffice))    
