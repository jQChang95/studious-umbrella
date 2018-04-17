# SUTD ISTD Term 5 CSE Coding Assignment 2

Java code to perfom a secure transfer of file (Requires Signature and Certificate Verification before sending files that are encrpyted then decrypted)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Get a copy of the repository and go into the repository

```
$ git clone https://github.com/junqingchang/studious-umbrella
$ cd studious-umbrella
```

### Usage

Ensure that you run on server and client on 2 seperate machine/terminals

#### CP1

Drop the file that you want to transfer into the directory ClientCP2

One runs
```
$ cd ServerCP1
$ javac *.java
$ java ServerWithSecurity
```
Another runs
```
$ cd ClientCP1
$ javac *.java
$ java ClientWithSecurity
```

The client side will prompt you which file you want to transfer. Enter the filename *with extension.*
The server side will receive the file in the folder /ServerCP1/recv

#### CP2

Drop the file that you want to transfer into the directory ClientCP2

One runs
```
$ cd ServerCP2
$ javac *.java
$ java ServerWithSecurity
```
Another runs
```
$ cd ClientCP2
$ javac *.java
$ java ClientWithSecurity
```

The client side will prompt you which file you want to transfer. Enter the filename *with extension.*
The server side will receive the file in the folder /ServerCP2/recv


## Built With

* [Java](https://java.com/en/)
* [Java Cryptography Extension](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html) - Crptography

## Authors

* **Chang Jun Qing**
* **Rachel Gan**

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## Acknowledgments

