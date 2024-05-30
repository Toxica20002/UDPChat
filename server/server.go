package main

import (
	"fmt"
	"net"
	"os"
	"strconv"
	"strings"
)

type ClientInfo struct {
	IP   string
	Name string
}

var clients = make(map[string]ClientInfo)

func main() {
	service := ":1200"
	udpAddr, err := net.ResolveUDPAddr("udp4", service)
	checkError(err)
	conn, err := net.ListenUDP("udp", udpAddr)
	checkError(err)
	for {
		handleClient(conn)
	}
}

func handleClient(conn *net.UDPConn) {
	var buf [512]byte
	n, addr, err := conn.ReadFromUDP(buf[0:])
	if err != nil {
		return
	}
	message := string(buf[:n])
	fmt.Println("Received: ", message)
	//Print IP address of the client
	fmt.Println("Client IP: ", addr.IP.String())
	fmt.Println("Client Port: ", addr.Port)
	//fmt.Println("Received: ", message)

	ClientIP := addr.IP.String()
	ClientPort := addr.Port
	if strings.HasPrefix(message, "@") {
		parts := strings.SplitN(message, " ", 4)
		command := parts[0][1:] // remove the '@'
		if len(parts) > 1 {
			msg := parts[3]
			option := parts[2]
			sender := parts[1]
			if command == "all" {
				if option == "message" {
					for addr, client := range clients {
						if client.Name == sender {
							continue
						}
						msg = "file (Public) " + sender + ": " + msg
						clientAddr, _ := net.ResolveUDPAddr("udp", addr) // assuming clients are listening on port 1200
						_, err := conn.WriteToUDP([]byte(msg), clientAddr)
						if err != nil {
							return
						}
					}
				} else if option == "file" {
					for addr, client := range clients {
						if client.Name == sender {
							continue
						}

						fileInfo := strings.SplitN(msg, " ", 2)
						fmt.Println(fileInfo[0] + " " + fileInfo[1])
						msg = "message (Public) " + sender + " want to share " + fileInfo[1] + " with size " + fileInfo[0] + " bytes"
						clientAddr, _ := net.ResolveUDPAddr("udp", addr) // assuming clients are listening on port 1200
						_, err := conn.WriteToUDP([]byte(msg), clientAddr)
						if err != nil {
							return
						}

						msg = "file " + ClientIP + " " + strconv.Itoa(ClientPort) + " " + fileInfo[0] + " " + fileInfo[1]
						clientAddr, _ = net.ResolveUDPAddr("udp", addr) // assuming clients are listening on port 1200
						_, err = conn.WriteToUDP([]byte(msg), clientAddr)
						if err != nil {
							return
						}
					}
				}
			} else {
				if option == "message" {
					msg = "message (Private) " + sender + ": " + msg
					name := command
					name = strings.TrimSpace(name)
					flag := false
					for addr, client := range clients {
						if client.Name == name {
							clientAddr, _ := net.ResolveUDPAddr("udp", addr) // assuming clients are listening on port 1200
							_, err := conn.WriteToUDP([]byte(msg), clientAddr)
							if err != nil {
								return
							}
							flag = true
						}
					}
					if !flag {
						errMsg := "(Public) Server: The user " + command + " does not exist."
						_, err := conn.WriteToUDP([]byte(errMsg), addr)
						if err != nil {
							return
						}
					}
				} else if option == "file" {
					msg = "file (Private) " + sender + " want to share " + msg
					name := command
					name = strings.TrimSpace(name)
					flag := false
					for addr, client := range clients {
						if client.Name == name {
							clientAddr, _ := net.ResolveUDPAddr("udp", addr) // assuming clients are listening on port 1200
							_, err := conn.WriteToUDP([]byte(msg), clientAddr)
							if err != nil {
								return
							}
							flag = true
						}
					}
					if !flag {
						errMsg := "(Public) Server: The user " + command + " does not exist."
						_, err := conn.WriteToUDP([]byte(errMsg), addr)
						if err != nil {
							return
						}
					}
				}
			}
		}
	} else if client, ok := clients[addr.String()]; ok {
		clients[addr.String()] = client
	} else {
		// fmt.Println("New client registered: " + message + " from " + addr.String())
		message = strings.TrimSpace(message)
		clients[addr.String()] = ClientInfo{IP: addr.IP.String(), Name: message}

		for addr := range clients {
			msg := "(Public) Server: User " + message + " has joined the chat."
			clientAddr, _ := net.ResolveUDPAddr("udp", addr) // assuming clients are listening on port 1200
			_, err := conn.WriteToUDP([]byte(msg), clientAddr)
			if err != nil {
				return
			}
		}
		fmt.Println("User " + message + " has joined the chat.")
	}
}

func checkError(err error) {
	if err != nil {
		_, err := fmt.Fprintf(os.Stderr, "Fatal error ", err.Error())
		if err != nil {
			return
		}
		os.Exit(1)
	}
}
