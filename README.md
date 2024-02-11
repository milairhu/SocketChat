# SocketChat

This project was realized as part of the course **SR03** taught at the **University of Technology of Compiègne** by Pr. Ahmed Lounis in 2023. The goal was to get familiar with Sockets and Java.

## Description

This small project contains both server and client side of a chat application, which can be used in the terminal. The aim of the project is to learn about sockets and how to use them in Java.

## How to use

The server must be started before the clients. It can be started by running *App.java* in the *Server* folder.

The client can be started by running *App.java* in the *Client* folder. The user can start as many clients as wanted. Each client need to enter a username before being able to send messages. The server makes sure that the username is unique.

The users can send messages to the server by connectting to its port 8000. They can exit the chat by typing *exit* in the terminal.

## Features

- Send messages to all connected clients with a username
- Unique usernames
- Disconnections are handled by the server
