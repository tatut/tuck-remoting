## Tuck remoting

This library is a supplemental library for [Tuck](https://github.com/tatut/tuck) that
extends events to work between client and server.

You can define Tuck events to be sent to the server and implement the event on the server side.
The server will also receive a handle to send events back to the client.

This library works with WebSockets.

### Example

See in the `example` folder for a very simple multiuser chat application using this library.
