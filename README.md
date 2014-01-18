# Introduction

The C Preprocessor is an interesting standard. It appears to be
derived from the de-facto behaviour of the first preprocessors, and
has evolved over the years. Implementation is therefore difficult.

JCPP is a complete, compliant, standalone, pure Java implementation
of the C preprocessor. It is intended to be of use to people writing
C-style compilers in Java using tools like sablecc, antlr, JLex,
CUP and so forth (although if you aren't using sablecc, you need your
head examined).

This project has has been used to successfully preprocess much of
the source code of the GNU C library. As of version 1.2.5, it can
also preprocess the Apple Objective C library.

# Documentation

* [JavaDoc API](http://shevek.github.io/jcpp/docs/javadoc/)
* [Coverage Report](http://shevek.github.io/jcpp/docs/cobertura/)
