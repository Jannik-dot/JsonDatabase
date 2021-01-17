# JsonDatabase
This repository contains the code for a client and a server that can interact via json with each other.
The client:
- can pass commands via command line input to the server
- commands include set, get, delete, exit
- can pass a json file in the format: {"type":("set"/"get"/"delete"/"exit"),"key":"thekey",(optional)"value":"thevalue"}
  -the value can be a json
- by passing a key and/or a value a certain value can be accessed or saved at a certain spot in the database
- it is possible to chain keys to create a json of possibly infinite depth

The server:
- can handle multiple request at once (multithreading)
- can safely handle multiple request, because of read and write locks
- can save the input to a file and initialize a database on startup from this file
- can work with jsons of unknown depth, because of recursion
