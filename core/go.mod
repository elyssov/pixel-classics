module github.com/elyssov/pixel-classics/core

go 1.25.0

require github.com/iskra-messenger/iskra v0.0.0

require (
	github.com/gorilla/websocket v1.5.3 // indirect
	github.com/miekg/dns v1.1.72 // indirect
	golang.org/x/crypto v0.49.0 // indirect
	golang.org/x/mobile v0.0.0-20260312152759-81488f6aeb60 // indirect
	golang.org/x/mod v0.34.0 // indirect
	golang.org/x/net v0.52.0 // indirect
	golang.org/x/sync v0.20.0 // indirect
	golang.org/x/sys v0.42.0 // indirect
	golang.org/x/tools v0.43.0 // indirect
)

// Use local Iskra source
replace github.com/iskra-messenger/iskra => C:\projects\Iskra
